package com.scofu.community.bukkit;

import com.google.inject.Inject;
import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.common.inject.Feature;
import com.scofu.community.UserRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.text.ThemeRegistry;
import org.bukkit.entity.Player;

final class ThemeCommand implements Feature {

  private final UserRepository userRepository;
  private final ThemeRegistry themeRegistry;
  private final Design design;

  @Inject
  ThemeCommand(UserRepository userRepository, ThemeRegistry themeRegistry, Design design) {
    this.userRepository = userRepository;
    this.themeRegistry = themeRegistry;
    this.design = design;
  }

  @Identified(value = "theme", async = true)
  private void theme(Expansion<Player> source) {
    final var player = source.orElseThrow();
    design.bind(player, new ThemeSelectWindow(design, themeRegistry, userRepository));
  }

}
