package com.scofu.community.bukkit;

import static com.scofu.network.document.Filter.empty;
import static com.scofu.network.document.Filter.exists;
import static com.scofu.network.document.Filter.where;
import static com.scofu.network.document.Sort.by;
import static com.scofu.text.ContextualizedComponent.debug;
import static java.util.concurrent.CompletableFuture.allOf;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.toComponent;
import static net.kyori.adventure.text.Component.translatable;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.command.validation.Permission;
import com.scofu.common.inject.Feature;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.GenericStats;
import com.scofu.community.GenericStatsRepository;
import com.scofu.design.bukkit.Container.TickSpeed;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.hologram.TextHologram;
import com.scofu.design.bukkit.hologram.TextHolograms;
import com.scofu.design.bukkit.npc.Npc;
import com.scofu.design.bukkit.npc.Npcs;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.mojang.profile.ProfileRepository;
import com.scofu.network.document.Book;
import com.scofu.network.document.Order;
import com.scofu.network.document.Query;
import com.scofu.text.json.TagFactory;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class StatsCommands implements Feature {

  private final Plugin plugin;
  private final GenericStatsRepository genericStatsRepository;
  private final LazyFactory lazyFactory;
  private final ProfileRepository profileRepository;
  private final Book<GenericStats> coinsLeaderboard;
  private final Book<GenericStats> coinsLeaderboardTest;
  private final Design design;
  private final TagFactory tagFactory;

  @Inject
  StatsCommands(Plugin plugin, GenericStatsRepository genericStatsRepository,
      LazyFactory lazyFactory, ProfileRepository profileRepository, Design design,
      TagFactory tagFactory) {
    this.plugin = plugin;
    this.genericStatsRepository = genericStatsRepository;
    coinsLeaderboard = Book.of(
        Query.builder().filter(empty()).sort(by("coins", Order.HIGHEST_TO_LOWEST)).build(),
        PaginatedWindow.ITEMS_PER_PAGE, genericStatsRepository, Duration.ofSeconds(60));
    coinsLeaderboardTest = Book.of(
        Query.builder().filter(empty()).sort(by("coins", Order.HIGHEST_TO_LOWEST)).build(), 10,
        genericStatsRepository, Duration.ofSeconds(60));
    this.lazyFactory = lazyFactory;
    this.profileRepository = profileRepository;
    this.design = design;
    this.tagFactory = tagFactory;
  }

  @Identified("genericstats")
  @Permission("scofu.command.genericstats")
  private void stats(Expansion<Player> source, Player target) {
    final var player = source.orElseThrow();
    player.sendMessage(text("Loading stats...").color(NamedTextColor.GRAY));
    genericStatsRepository.byId(target.getUniqueId().toString())
        .ifPresentOrElse(
            stats -> player.sendMessage(translatable("Coins: %s", text(stats.coins()))),
            () -> player.sendMessage(text("No stats found.")));
  }

  @Identified("setcoins")
  private void setcoins(Expansion<Player> source, Player target, int coins) {
    final var player = source.orElseThrow();
    final var stats = genericStatsRepository.byId(target.getUniqueId().toString())
        .orElseGet(() -> lazyFactory.create(GenericStats.class, GenericStats::id,
            target.getUniqueId().toString()));
    stats.setCoins(coins);
    genericStatsRepository.update(stats).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Coins set to " + coins + "!"));
      }
    }));
  }

  @Identified("holotest")
  private void holotest(Expansion<Player> source) {
    final var player = source.orElseThrow();
    final var location = player.getLocation().clone();

    design.bind(player)
        .through(TextHolograms.class, TextHolograms::new)
        .withKey(UUID.randomUUID().toString())
        .withTickSpeed(TickSpeed.NORMAL)
        .to(hologram -> {
          hologram.setLines(lines -> {
            lines.add(text("Hello!"));
            lines.add(text(
                "You are at: " + (player.getLocation().getBlockX() + ", " + player.getLocation()
                    .getBlockY() + ", " + player.getLocation().getBlockZ())));
          });
          hologram.setLocation(() -> location);
        });
  }

  @Identified("hololbtest")
  private void hololbtest(Expansion<Player> source) {
    final var player = source.orElseThrow();
    final var location = player.getLocation().clone().add(0, TextHologram.HOLOGRAM_HEIGHT * 16, 0);
    design.bind(player)
        .through(TextHolograms.class, TextHolograms::new)
        .withKey(UUID.randomUUID().toString())
        .withTickSpeed(TickSpeed.NORMAL)
        .to(hologram -> {
          final var page = coinsLeaderboardTest.page(1);
          hologram.setLines(lines -> {
            lines.add(text("Coins Leaderboard").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, State.TRUE));
            page.documents()
                .forEach((stats, placement) -> lines.add(
                    text(placement + ". ").color(NamedTextColor.YELLOW)
                        .append(text(stats.id()).color(NamedTextColor.WHITE)
                            .append(text(" - ").color(NamedTextColor.GRAY)
                                .append(text(stats.coins() + "").color(NamedTextColor.YELLOW))))));
            lines.add(Component.empty());
            lines.add(text(
                "Refreshes in " + coinsLeaderboardTest.durationUntilNextRefresh().toSeconds()
                    + "s.").color(NamedTextColor.GRAY));
          });
          hologram.setLocation(() -> location);
        });
  }

  @Identified("npctest")
  private void npctest(Expansion<Player> source, String skin, String hologramName) {
    final var player = source.orElseThrow();
    final var profile = profileRepository.byUsernameOrId(skin).orElse(null);
    if (profile == null) {
      player.sendMessage("not a player: " + skin);
      return;
    }
    final var npcLocation = player.getLocation().clone();
    final var hologramLocation = npcLocation.clone().subtract(0, TextHologram.HOLOGRAM_HEIGHT, 0);
    design.bind(player)
        .through(Npcs.class, () -> new Npcs(plugin, tagFactory))
        .withKey(UUID.randomUUID().toString())
        .to(new Npc(null, plugin, tagFactory) {
          @Override
          public void populate() {
            setLocation(() -> npcLocation);
            editProfile(playerProfile -> {
              playerProfile.setProperty(
                  new ProfileProperty("textures", profile.textures().raw().value(),
                      profile.textures().raw().signature()));
            });
            onClick(() -> {
              debug().text("bing bong")
                  .prefixed()
                  .renderTo(viewer().theme(), viewer().player()::sendMessage);
            });
          }
        });
    design.bind(player)
        .through(TextHolograms.class, TextHolograms::new)
        .withKey(UUID.randomUUID().toString())
        .to(hologram -> {
          hologram.setLines(lines -> lines.add(text(hologramName).color(NamedTextColor.YELLOW)
              .decoration(TextDecoration.BOLD, State.TRUE)));
          hologram.setLocation(() -> hologramLocation);
        });
  }

  @Identified("createrandomstats")
  private void create(Expansion<Player> source) {
    final var player = source.orElseThrow();
    player.sendMessage(text("Working..."));
    final var futures = IntStream.range(0, 125)
        .mapToObj(i -> lazyFactory.create(GenericStats.class, GenericStats::id, "random" + i))
        .peek(stats -> stats.setCoins(ThreadLocalRandom.current().nextInt(1, 10_000)))
        .map(genericStatsRepository::update)
        .toArray(CompletableFuture[]::new);
    allOf(futures).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        throwable.printStackTrace();
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Done."));
      }
    }));
  }

  @Identified("refreshcoinsleaderboard")
  private void refresh(Expansion<Player> source) {
    final var player = source.orElseThrow();
    player.sendMessage(text("Refreshing..."));
    coinsLeaderboard.tryRefresh(true);
    player.sendMessage(text("Refreshed."));
  }

  @Identified("coinsleaderboard")
  private void coinsLeaderboard(Expansion<Player> source, Optional<Integer> page) {
    final var player = source.orElseThrow();
    if (true) {
      design.bind(player,
          new CoinsLeaderboardWindow(design, coinsLeaderboard, genericStatsRepository));
      return;
    }

    final var total = genericStatsRepository.count(
        Query.builder().filter(where("references.coins", exists(true))).build()).join();
    final var pages = (int) Math.ceil(total / 10D);
    final int actualPage = page.orElse(1);
    if (actualPage > pages || actualPage < 1) {
      player.sendMessage(text("Page must be between 1 and " + pages + "."));
      return;
    }
    final var placement = new AtomicInteger((actualPage - 1) * 10);
    player.sendMessage(
        text("Coins leaderboard (Page " + actualPage + "/" + pages + ")").append(newline())
            .append(coinsLeaderboard.page(actualPage)
                .documents()
                .entrySet()
                .stream()
                .map(entry -> text(placement.getAndIncrement() + ". ").color(NamedTextColor.YELLOW)
                    .append(text(entry.getValue() + ". ").color(NamedTextColor.YELLOW)
                        .append(text(entry.getKey().id()).color(NamedTextColor.WHITE)))
                    .append(text(" - ").color(NamedTextColor.GRAY))
                    .append(text(entry.getKey().coins())))
                .collect(toComponent(newline())))
            .append(newline())
            .append(text(actualPage > 1 ? "[Previous page] " : "").clickEvent(
                ClickEvent.runCommand("/coinsleaderboard " + (actualPage - 1))))
            .append(text(
                "Refreshes in " + coinsLeaderboard.durationUntilNextRefresh().toSeconds() + "s."))
            .append(text(actualPage < pages ? " [Next page]" : "").clickEvent(
                ClickEvent.runCommand("/coinsleaderboard " + (actualPage + 1)))));
  }

}
