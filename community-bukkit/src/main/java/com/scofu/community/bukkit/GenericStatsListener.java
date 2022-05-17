package com.scofu.community.bukkit;

import com.scofu.common.inject.Feature;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.GenericStats;
import com.scofu.community.GenericStatsRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
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
    final var id = player.getUniqueId().toString();
    final Supplier<GenericStats> factory =
        () ->
            lazyFactory.create(
                GenericStats.class, GenericStats::id, player.getUniqueId().toString());
    genericStatsRepository
        .delete(id)
        .flatMap(unused -> genericStatsRepository.byIdAsync(id))
        .apply(Optional::orElseGet, () -> factory)
        .accept(GenericStats::setLastLoginAt, Instant::now)
        .flatMap(genericStatsRepository::update);
  }
}
