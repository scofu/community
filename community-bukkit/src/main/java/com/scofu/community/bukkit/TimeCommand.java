package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.common.inject.Feature;
import com.scofu.community.Time;
import com.scofu.community.UserRepository;
import com.scofu.text.ThemeRegistry;
import org.bukkit.entity.Player;

final class TimeCommand implements Feature {

  private final UserRepository userRepository;
  private final ThemeRegistry themeRegistry;

  @Inject
  TimeCommand(UserRepository userRepository, ThemeRegistry themeRegistry) {
    this.userRepository = userRepository;
    this.themeRegistry = themeRegistry;
  }

  @Identified(value = "time", async = true)
  private void time(Expansion<Player> source, Time time) {
    final var player = source.orElseThrow();
    player.setPlayerTime(time.ticks(), false);
    final var message = themeRegistry.byIdentified(player)
        .render(theme -> translatable("Time set to %s.",
            text(time.name()).color(theme.brightYellow())).color(theme.brightGreen()));
    player.sendMessage(message);
    userRepository.byId(player.getUniqueId().toString()).ifPresent(user -> {
      user.setTime(time);
      userRepository.update(user);
    });
  }

}
