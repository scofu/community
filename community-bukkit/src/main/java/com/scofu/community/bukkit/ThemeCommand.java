package com.scofu.community.bukkit;

import com.google.inject.Inject;
import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.common.inject.Feature;
import com.scofu.community.bukkit.design.ThemeSelectWindow;
import com.scofu.design.bukkit.Design;
import javax.inject.Provider;
import org.bukkit.entity.Player;

final class ThemeCommand implements Feature {

  private final Design design;
  private final Provider<ThemeSelectWindow> themeSelectWindowProvider;

  @Inject
  ThemeCommand(Design design, Provider<ThemeSelectWindow> themeSelectWindowProvider) {
    this.design = design;
    this.themeSelectWindowProvider = themeSelectWindowProvider;
  }

  @Identified(value = "theme", async = true)
  private void theme(Expansion<Player> source) {
    design.bind(source.orElseThrow(), themeSelectWindowProvider.get());
  }
}
