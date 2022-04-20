package com.scofu.community.bukkit;

import com.scofu.common.inject.Feature;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.GenericStats;
import com.scofu.community.GenericStatsRepository;
import java.time.Instant;
import javax.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class GenericStatsListener implements Listener, Feature {

  private final GenericStatsRepository genericStatsRepository;
  private final LazyFactory lazyFactory;

  @Inject
  GenericStatsListener(GenericStatsRepository genericStatsRepository, LazyFactory lazyFactory) {
    this.genericStatsRepository = genericStatsRepository;
    this.lazyFactory = lazyFactory;
  }

  @EventHandler
  private void onPlayerJoinEvent(PlayerJoinEvent event) {
    final var player = event.getPlayer();
    genericStatsRepository.delete(player.getUniqueId().toString()).join();
    final var stats = genericStatsRepository.byId(player.getUniqueId().toString())
        .orElseGet(() -> lazyFactory.create(GenericStats.class, GenericStats::id,
            player.getUniqueId().toString()));
    stats.setLastLoginAt(Instant.now());
    genericStatsRepository.update(stats);
  }
}
