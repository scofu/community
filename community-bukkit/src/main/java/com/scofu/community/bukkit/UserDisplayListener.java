package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.common.inject.Feature;
import com.scofu.community.Grant;
import com.scofu.community.GrantRepository;
import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.community.Session;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import com.scofu.network.document.DocumentStateListener;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class UserDisplayListener implements Listener, Feature {

  private final RankRepository rankRepository;
  private final UserRepository userRepository;

  @Inject
  UserDisplayListener(RankRepository rankRepository, UserRepository userRepository, Server server,
      GrantRepository grantRepository) {
    this.rankRepository = rankRepository;
    this.userRepository = userRepository;

    userRepository.addStateListener(new DocumentStateListener<>() {
      @Override
      public void onUpdate(User user, boolean cached) {
        Optional.ofNullable(server.getPlayer(UUID.fromString(user.id())))
            .filter(Player::isOnline)
            .ifPresent(player -> updateDisplay(player, Optional.of(user)));
      }
    });
    rankRepository.addStateListener(new DocumentStateListener<>() {
      @Override
      public void onUpdate(Rank rank, boolean cached) {
        grantRepository.byRankId(rank.id())
            .thenAccept(grants -> grants.findFirst().ifPresent(grant -> {
              Optional.ofNullable(server.getPlayer(UUID.fromString(grant.userId())))
                  .filter(Player::isOnline)
                  .ifPresent(player -> updateDisplay(player, userRepository.byId(grant.userId())));
            }));
      }
    });
  }

  @EventHandler(priority = EventPriority.LOW)
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    updateDisplay(player, userRepository.byId(player.getUniqueId().toString()));
  }

  private void updateDisplay(Player player, Optional<User> user) {
    final var name = text(player.getName()).color(NamedTextColor.GRAY);
    final var formattedName = user.flatMap(User::session)
        .flatMap(Session::grant)
        .map(Grant::rankId)
        .flatMap(rankRepository::byId)
        .map(rank -> {
          var coloredName = rank.nameColor().map(name::color).orElse(name);
          return rank.prefix()
              .<Component>map(prefix -> translatable("%s %s", prefix, coloredName))
              .orElse(coloredName);
        })
        .orElse(name);
    player.displayName(formattedName);
    player.playerListName(formattedName);
  }

}
