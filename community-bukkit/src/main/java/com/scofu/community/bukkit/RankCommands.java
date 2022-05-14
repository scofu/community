package com.scofu.community.bukkit;

import static com.scofu.text.ContextualizedComponent.info;
import static com.scofu.text.ContextualizedComponent.success;

import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.command.validation.Permission;
import com.scofu.common.inject.Feature;
import com.scofu.common.json.Json;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.RankRepository;
import com.scofu.community.UserRepository;
import com.scofu.community.bukkit.permission.WildcardPermissionChecker;
import com.scofu.design.bukkit.Design;
import com.scofu.network.document.Query;
import com.scofu.text.ThemeRegistry;
import com.scofu.text.json.TagFactory;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.bukkit.entity.Player;

final class RankCommands implements Feature {

  private final Design design;
  private final LazyFactory lazyFactory;
  private final TagFactory tagFactory;
  private final ThemeRegistry themeRegistry;
  private final RankRepository rankRepository;
  private final UserRepository userRepository;
  private final WildcardPermissionChecker wildcardPermissionChecker;
  private final Json json;

  @Inject
  RankCommands(
      Design design,
      LazyFactory lazyFactory,
      TagFactory tagFactory,
      ThemeRegistry themeRegistry,
      RankRepository rankRepository,
      UserRepository userRepository,
      WildcardPermissionChecker wildcardPermissionChecker,
      Json json) {
    this.design = design;
    this.lazyFactory = lazyFactory;
    this.tagFactory = tagFactory;
    this.themeRegistry = themeRegistry;
    this.rankRepository = rankRepository;
    this.userRepository = userRepository;
    this.wildcardPermissionChecker = wildcardPermissionChecker;
    this.json = json;
  }

  @Identified("ranks")
  @Permission("scofu.command.ranks")
  private void ranks(Expansion<Player> source) {
    final var player = source.orElseThrow();
    design.bind(player, new RankWindow(design, rankRepository, lazyFactory, tagFactory));
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
