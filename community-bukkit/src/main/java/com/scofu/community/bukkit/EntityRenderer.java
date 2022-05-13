package com.scofu.community.bukkit;

import com.google.inject.Inject;
import com.scofu.text.Renderer;
import com.scofu.text.RendererRegistry;
import com.scofu.text.Theme;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

final class EntityRenderer implements Renderer<Entity> {

  private final RendererRegistry rendererRegistry;

  @Inject
  EntityRenderer(RendererRegistry rendererRegistry) {
    this.rendererRegistry = rendererRegistry;
  }

  @Override
  public Class<Entity> type() {
    return Entity.class;
  }

  @Override
  public Optional<Component> render(Theme theme, Entity entity) {
    if (entity instanceof Player player) {
      return rendererRegistry.render(theme, Player.class, player);
    }
    return Optional.ofNullable(entity.customName())
        .or(() -> Optional.of(entity.name()))
        .map(component -> component.colorIfAbsent(theme.white()));
  }
}
