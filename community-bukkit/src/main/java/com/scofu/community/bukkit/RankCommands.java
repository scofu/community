package com.scofu.community.bukkit;

import static com.scofu.text.ContextualizedComponent.info;
import static com.scofu.text.ContextualizedComponent.success;

import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.command.validation.Permission;
import com.scofu.common.inject.Feature;
import com.scofu.community.RankRepository;
import com.scofu.community.bukkit.design.RankWindow;
import com.scofu.design.bukkit.Design;
import com.scofu.network.document.Query;
import com.scofu.text.ThemeRegistry;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Provider;
import org.bukkit.entity.Player;

final class RankCommands implements Feature {

  private final Design design;
  private final ThemeRegistry themeRegistry;
  private final RankRepository rankRepository;
  private final Provider<RankWindow> rankWindowProvider;

  @Inject
  RankCommands(
      Design design,
      ThemeRegistry themeRegistry,
      RankRepository rankRepository,
      Provider<RankWindow> rankWindowProvider) {
    this.design = design;
    this.themeRegistry = themeRegistry;
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
      info()
          .text("Please confirm!")
          .prefixed()
          .renderTo(themeRegistry.byIdentified(player), player::sendMessage);
      return;
    }
    rankRepository
        .find(Query.empty())
        .thenCompose(
            ranks ->
                CompletableFuture.allOf(
                    ranks.keySet().stream()
                        .map(rankRepository::delete)
                        .toArray(CompletableFuture[]::new)))
        .thenAccept(
            unused ->
                success()
                    .text("You've deleted all ranks.")
                    .prefixed()
                    .renderTo(themeRegistry.byIdentified(player), player::sendMessage));
  }
}
