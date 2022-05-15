package com.scofu.community.bukkit.text;

import com.google.inject.Inject;
import com.scofu.text.Color;
import com.scofu.text.Renderer;
import com.scofu.text.RendererRegistry;
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
  public Optional<Component> render(Entity entity) {
    if (entity instanceof Player player) {
      return rendererRegistry.render(Player.class, player);
    }
    return Optional.ofNullable(entity.customName())
        .or(() -> Optional.of(entity.name()))
        .map(component -> component.colorIfAbsent(Color.WHITE));
  }
}
