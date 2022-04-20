package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.scofu.common.inject.Feature;
import com.scofu.design.bukkit.Container.TickSpeed;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.tablist.Tablist;
import com.scofu.design.bukkit.tablist.TablistEntry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class TablistListener implements Listener, Feature {

  private final PlayerRenderer playerRenderer;
  private final Design design;

  @Inject
  TablistListener(PlayerRenderer playerRenderer, Design design) {
    this.playerRenderer = playerRenderer;
    this.design = design;
  }

  @EventHandler
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    design.bind(player, new Tablist(TickSpeed.NORMAL)).use((viewer, entries) -> {
      int slot = 0;
      for (var onlinePlayer : player.getServer().getOnlinePlayers()) {
        final var textures = onlinePlayer.getPlayerProfile()
            .getProperties()
            .stream()
            .filter(property -> property.getName().equalsIgnoreCase("textures"))
            .findFirst()
            .orElse(null);
        final var entry = new TablistEntry(
            playerRenderer.render(viewer.theme(), onlinePlayer).orElse(empty()),
            onlinePlayer.getPing(), textures == null ? null : textures.getValue(),
            textures == null ? null : textures.getSignature());
        entries[slot++] = entry;
      }
      slot++;
      entries[slot] = TablistEntry.of(viewer.theme()
          .render(theme -> translatable("Your ping: %s",
              text(player.getPing()).color(theme.brightGreen())).color(theme.brightWhite())));
    });
  }

}
