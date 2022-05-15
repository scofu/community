package com.scofu.community.bukkit;

import static com.scofu.text.ContextualizedComponent.info;

import com.google.inject.Inject;
import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.common.inject.Feature;
import com.scofu.community.Time;
import com.scofu.community.UserRepository;
import org.bukkit.entity.Player;

final class TimeCommand implements Feature {

  private final UserRepository userRepository;

  @Inject
  TimeCommand(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Identified(value = "time", async = true)
  private void time(Expansion<Player> source, Time time) {
    final var player = source.orElseThrow();
    player.setPlayerTime(time.ticks(), false);
    info().text("Time set to %s.", time.name()).prefixed().render(player::sendMessage);
    userRepository
        .byId(player.getUniqueId().toString())
        .ifPresent(
            user -> {
              user.setTime(time);
              userRepository.update(user);
            });
  }
}
