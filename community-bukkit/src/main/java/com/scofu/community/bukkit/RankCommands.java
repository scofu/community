package com.scofu.community.bukkit;

import static com.scofu.text.ContextualizedComponent.error;
import static com.scofu.text.ContextualizedComponent.info;
import static com.scofu.text.ContextualizedComponent.success;

import com.scofu.command.model.Identified;
import com.scofu.command.validation.Permission;
import com.scofu.common.Expansion;
import com.scofu.common.inject.Feature;
import com.scofu.community.RankRepository;
import com.scofu.community.bukkit.design.RankWindow;
import com.scofu.design.bukkit.Design;
import com.scofu.network.document.Query;
import com.scofu.network.message.Result;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;
import org.bukkit.entity.Player;

final class RankCommands implements Feature {

  private final Design design;
  private final RankRepository rankRepository;
  private final Provider<RankWindow> rankWindowProvider;

  @Inject
  RankCommands(
      Design design, RankRepository rankRepository, Provider<RankWindow> rankWindowProvider) {
    this.design = design;
    this.rankRepository = rankRepository;
    this.rankWindowProvider = rankWindowProvider;
  }

  @Identified("ranks")
  @Permission("scofu.command.ranks")
  private void ranks(Expansion<Player> source) {
    final var player = source.orElseThrow();
    design.bind(player, rankWindowProvider.get());
  }

  @Identified("deleteallranks")
  @Permission("scofu.command.deleteallranks")
  private void ranks(Expansion<Player> source, boolean confirm) {
    final var player = source.orElseThrow();
    if (!confirm) {
      info().text("Please confirm!").prefixed().render(player::sendMessage);
      return;
    }
    rankRepository
        .find(Query.empty())
        .map(map -> map.keySet().stream())
        .apply(Stream::map, () -> (Function<String, Result<Void>>) rankRepository::delete)
        .flatMap(stream -> stream.collect(Result.toResult()))
        .map(Stream::count)
        .map(count -> success().text("Ranks deleted: %s", count).render())
        .timeoutAfter(1, TimeUnit.MINUTES, () -> error().text("Timed out.").prefixed().render())
        .accept(player::sendMessage);
  }
}
