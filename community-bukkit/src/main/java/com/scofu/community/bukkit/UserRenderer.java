package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.scofu.community.Grant;
import com.scofu.community.RankRepository;
import com.scofu.community.Session;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import com.scofu.mojang.profile.Profile;
import com.scofu.mojang.profile.ProfileRepository;
import com.scofu.text.Renderer;
import com.scofu.text.Theme;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;

/**
 * Renders players.
 */
public class UserRenderer implements Renderer<UUID> {

  private final ProfileRepository profileRepository;
  private final UserRepository userRepository;
  private final RankRepository rankRepository;

  @Inject
  UserRenderer(ProfileRepository profileRepository, UserRepository userRepository,
      RankRepository rankRepository) {
    this.profileRepository = profileRepository;
    this.userRepository = userRepository;
    this.rankRepository = rankRepository;
  }

  @Override
  public Optional<Component> render(Theme theme, UUID uuid) {
    final var username = profileRepository.byUsernameOrId(uuid.toString())
        .map(Profile::username)
        .orElse("<unknown>");
    final var name = text(username).color(theme.white());
    final var user = userRepository.byId(uuid.toString());
    final var formattedName = user.flatMap(User::session)
        .flatMap(Session::grant)
        .map(Grant::rankId)
        .flatMap(rankRepository::byId)
        .map(rank -> {
          final var coloredName = rank.nameColor()
              .map(color -> color.render(theme, name))
              .orElse(name);
          return rank.prefix()
              .flatMap(prefix -> prefix.render(theme))
              .<Component>map(prefix -> translatable("%s %s", prefix, coloredName))
              .orElse(coloredName);
        })
        .orElse(name);
    return Optional.of(formattedName);
  }
}
