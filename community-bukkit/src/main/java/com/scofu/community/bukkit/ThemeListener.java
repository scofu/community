package com.scofu.community.bukkit;

import com.google.inject.Inject;
import com.scofu.common.inject.Feature;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import com.scofu.design.bukkit.Container.DrawReason;
import com.scofu.design.bukkit.Design;
import com.scofu.text.ThemeRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class ThemeListener implements Listener, Feature {

  private final UserRepository userRepository;
  private final ThemeRegistry themeRegistry;
  private final Design design;

  @Inject
  ThemeListener(UserRepository userRepository, ThemeRegistry themeRegistry, Design design) {
    this.userRepository = userRepository;
    this.themeRegistry = themeRegistry;
    this.design = design;
  }

  @EventHandler
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    userRepository
        .byId(player.getUniqueId().toString())
        .flatMap(User::theme)
        .flatMap(themeRegistry::byName)
        .or(() -> themeRegistry.byName("Vanilla"))
        .ifPresent(
            theme -> {
              themeRegistry.set(player, theme);
              design
                  .streamContainers(player)
                  .forEach(container -> container.draw(DrawReason.OTHER, true, true));
            });
  }
}
