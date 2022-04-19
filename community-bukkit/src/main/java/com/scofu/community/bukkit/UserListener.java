package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.chat.ParticipantRegistry;
import com.scofu.common.inject.Feature;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.GrantRepository;
import com.scofu.community.Session;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import com.scofu.community.bukkit.event.UserLoginEvent;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

final class UserListener implements Feature, Listener {

  private final UserRepository userRepository;
  private final GrantRepository grantRepository;
  private final InetAddress localHost;
  private final StaffChat staffChat;
  private final ParticipantRegistry participantRegistry;
  private final LazyFactory lazyFactory;

  @Inject
  UserListener(UserRepository userRepository, GrantRepository grantRepository,
      @Named("LocalHost") InetAddress localHost, StaffChat staffChat,
      ParticipantRegistry participantRegistry, LazyFactory lazyFactory) {
    this.userRepository = userRepository;
    this.grantRepository = grantRepository;
    this.localHost = localHost;
    this.staffChat = staffChat;
    this.participantRegistry = participantRegistry;
    this.lazyFactory = lazyFactory;
  }

  @EventHandler
  private void onPlayerLoginEvent(PlayerLoginEvent event) {
    if (event.getResult() != Result.ALLOWED) {
      return;
    }
    final var id = event.getPlayer().getUniqueId().toString();
    final var user = userRepository.byId(id)
        .orElseGet(
            () -> lazyFactory.create(User.class, Map.of("_id", id, "firstJoinAt", Instant.now())));
    Session session = null;
    if (user.hasSession()) {
      session = user.session().orElseThrow();
      if (!session.isActive()) {
        session = null;
      }
    }
    if (session == null) {
      session = new Session(Instant.now());
    }
    session.setInstanceId(localHost.getHostName());
    grantRepository.byUserId(user.id()).join().findFirst().ifPresent(session::setGrant);
    user.setSession(session);

    final var userLoginEvent = new UserLoginEvent(event.getPlayer(), event.getHostname(),
        event.getAddress(), event.getRealAddress(), user);
    event.getPlayer().getServer().getPluginManager().callEvent(userLoginEvent);
    userRepository.update(user);
  }

  @EventHandler(priority = EventPriority.HIGH)
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    if (!player.hasPermission("scofu.staffchat") && !player.isOp()) {
      return;
    }
    participantRegistry.get(player).join(staffChat);
    staffChat.sendRawMessage(
        translatable("%s joined %s.", text(player.getName()), text(localHost.getHostName())));
  }

}
