package com.scofu.community.bukkit;

import com.scofu.common.inject.Feature;
import com.scofu.community.Stats;
import com.scofu.community.StatsRepository;
import java.time.Instant;
import javax.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class GenericStatsListener implements Listener, Feature {

  private final StatsRepository statsRepository;

  @Inject
  GenericStatsListener(StatsRepository statsRepository) {
    this.statsRepository = statsRepository;
  }

  @EventHandler
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    final var statsId = Stats.id(player.getUniqueId().toString(), "generic");
    statsRepository.delete(statsId).join();
    final var stats = statsRepository.byId(statsId).orElse(new Stats(statsId));
    stats.set("lastLogin", Instant.now(), Instant.class);
    statsRepository.update(stats);
  }
}
