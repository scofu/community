package com.scofu.community.bukkit.design;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.scofu.common.inject.Feature;
import com.scofu.design.bukkit.Container.TickSpeed;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.sidebar.event.SidebarBoundEvent;
import com.scofu.design.bukkit.tablist.Tablist;
import com.scofu.design.bukkit.tablist.TablistEntry;
import com.scofu.network.instance.bukkit.LocalInstanceProvider;
import com.scofu.text.RendererRegistry;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class DesignListener implements Listener, Feature {

  private static final Supplier<String> DATE_SUPPLIER;

  static {
    final var format = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.US);
    final var zoneId = ZoneId.of("Europe/Stockholm");
    DATE_SUPPLIER =
        Suppliers.memoizeWithExpiration(
            () -> StringUtils.capitalize(format.format(ZonedDateTime.now(zoneId))),
            5,
            TimeUnit.MINUTES);
  }

  private final RendererRegistry rendererRegistry;
  private final Design design;
  private final LocalInstanceProvider localInstanceProvider;

  @Inject
  DesignListener(
      RendererRegistry rendererRegistry,
      Design design,
      LocalInstanceProvider localInstanceProvider) {
    this.rendererRegistry = rendererRegistry;
    this.design = design;
    this.localInstanceProvider = localInstanceProvider;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  private void onSidebarBoundEvent(SidebarBoundEvent event) {
    final var instance = localInstanceProvider.get().join();
    event
        .sidebar()
        .setTitle(
            viewer ->
                text(instance.deployment().name().toUpperCase())
                    .decoration(TextDecoration.BOLD, State.TRUE)
                    .color(viewer.theme().brightYellow()));
    event
        .sidebar()
        .use(
            (viewer, components) -> {
              components.add(
                  text(DATE_SUPPLIER.get())
                      .color(viewer.theme().white())
                      .append(text(" " + instance.id()).color(viewer.theme().brightBlack())));
            });
  }

  @EventHandler
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    design
        .bind(player, new Tablist(TickSpeed.NORMAL))
        .use(
            (viewer, entries) -> {
              int slot = 0;
              for (var onlinePlayer : player.getServer().getOnlinePlayers()) {
                final var textures =
                    onlinePlayer.getPlayerProfile().getProperties().stream()
                        .filter(property -> property.getName().equalsIgnoreCase("textures"))
                        .findFirst()
                        .orElse(null);
                final var entry =
                    new TablistEntry(
                        rendererRegistry
                            .render(viewer.theme(), Player.class, onlinePlayer)
                            .orElse(empty()),
                        onlinePlayer.getPing(),
                        textures == null ? null : textures.getValue(),
                        textures == null ? null : textures.getSignature());
                entries[slot++] = entry;
              }
              slot++;
              entries[slot] =
                  TablistEntry.of(
                      viewer
                          .theme()
                          .render(
                              theme ->
                                  translatable(
                                          "Your ping: %s",
                                          text(player.getPing()).color(theme.brightGreen()))
                                      .color(theme.brightWhite())));
            });
  }
}
