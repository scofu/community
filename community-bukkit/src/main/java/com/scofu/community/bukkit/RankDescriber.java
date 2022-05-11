package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.translatable;

import com.scofu.command.model.Parameter;
import com.scofu.command.text.Describer;
import com.scofu.community.Rank;
import com.scofu.text.Theme;
import java.lang.reflect.Type;
import java.util.Optional;
import net.kyori.adventure.text.Component;

final class RankDescriber implements Describer<Rank> {

  @Override
  public boolean test(Type type) {
    return type instanceof Class rawType && Rank.class.isAssignableFrom(rawType);
  }

  @Override
  public Optional<Component> describe(Parameter<Rank> parameter, Theme theme) {
    return Optional.of(translatable(parameter.nameOrTranslation()));
  }
}
