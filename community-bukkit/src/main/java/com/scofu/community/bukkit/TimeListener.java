package com.scofu.community.bukkit;

import com.google.inject.Inject;
import com.scofu.common.inject.Feature;
import com.scofu.community.Time;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class TimeListener implements Listener, Feature {

  private final UserRepository userRepository;

  @Inject
  TimeListener(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @EventHandler
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    userRepository
        .byId(player.getUniqueId().toString())
        .flatMap(User::time)
        .map(Time::ticks)
        .ifPresent(ticks -> player.setPlayerTime(ticks, false));
  }
}
