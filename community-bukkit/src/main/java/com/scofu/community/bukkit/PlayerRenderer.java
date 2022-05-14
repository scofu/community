package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.scofu.community.Grant;
import com.scofu.community.RankRepository;
import com.scofu.community.Session;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import com.scofu.text.Renderer;
import com.scofu.text.Theme;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

final class PlayerRenderer implements Renderer<Player> {

  private final UserRepository userRepository;
  private final RankRepository rankRepository;

  @Inject
  PlayerRenderer(UserRepository userRepository, RankRepository rankRepository) {
    this.userRepository = userRepository;
    this.rankRepository = rankRepository;
  }

  @Override
  public Class<Player> type() {
    return Player.class;
  }

  @Override
  public Optional<Component> render(Theme theme, Player player) {
    final var name = text(player.getName()).color(theme.white());
    final var user = userRepository.byId(player.getUniqueId().toString());
    final var formattedName =
        user.flatMap(User::session)
            .flatMap(Session::grant)
            .map(Grant::rankId)
            .flatMap(rankRepository::byId)
            .map(
                rank -> {
                  final var coloredName =
                      rank.nameColor().map(color -> color.render(theme, name)).orElse(name);
                  return rank.render(theme)
                      .<Component>map(tag -> translatable("%s %s", tag, coloredName))
                      .orElse(coloredName);
                })
            .orElse(name);
    return Optional.of(formattedName);
  }
}
