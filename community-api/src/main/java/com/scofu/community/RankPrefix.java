package com.scofu.community;

import com.scofu.common.json.lazy.Lazy;
import com.scofu.text.Color;
import com.scofu.text.Renderable;
import com.scofu.text.Theme;
import java.util.Optional;
import net.kyori.adventure.text.Component;

/**
 * A rank's prefix.
 */
public interface RankPrefix extends Lazy, Renderable {

  Optional<Component> tag();

  void setTag(Component tag);

  Optional<Color> color();

  void setColor(Color color);

  @Override
  default Optional<Component> render(Theme theme) {
    return tag().map(
        component -> color().map(color -> color.render(theme, component)).orElse(component));
  }
}
