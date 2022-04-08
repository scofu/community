package com.scofu.community.bukkit;

import static com.scofu.network.document.Filter.empty;
import static com.scofu.network.document.Filter.exists;
import static com.scofu.network.document.Filter.where;
import static com.scofu.network.document.Sort.by;
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
import com.scofu.community.Stats;
import com.scofu.community.StatsRepository;
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
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class StatsCommands implements Feature {

  private final Plugin plugin;
  private final StatsRepository statsRepository;
  private final ProfileRepository profileRepository;
  private final Book<Stats> coinsLeaderboard;
  private final Book<Stats> coinsLeaderboardTest;
  private final Design design;

  @Inject
  StatsCommands(Plugin plugin, StatsRepository statsRepository, ProfileRepository profileRepository,
      Design design) {
    this.plugin = plugin;
    this.statsRepository = statsRepository;
    coinsLeaderboard = Book.of(Query.builder()
        .filter(empty())
        .sort(by(Stats.field("coins", Long.class), Order.HIGHEST_TO_LOWEST))
        .build(), PaginatedWindow.ITEMS_PER_PAGE, statsRepository, Duration.ofSeconds(60));
    coinsLeaderboardTest = Book.of(Query.builder()
        .filter(empty())
        .sort(by(Stats.field("coins", Long.class), Order.HIGHEST_TO_LOWEST))
        .build(), 10, statsRepository, Duration.ofSeconds(60));
    this.profileRepository = profileRepository;
    this.design = design;
  }

  @Identified("stats")
  @Permission("scofu.command.stats")
  private void stats(Expansion<Player> source, Player target, String category) {
    final var player = source.orElseThrow();
    player.sendMessage(text("Loading stats...").color(NamedTextColor.GRAY));
    statsRepository.byId(Stats.id(target.getUniqueId().toString(), category))
        .ifPresentOrElse(stats -> {
          var component = text(
              "Viewing " + stats.references().size() + " stat" + (stats.references().size() == 1
                  ? "" : "s") + ":").append(newline())
              .append(stats.references()
                  .entrySet()
                  .stream()
                  .map(entry -> text(entry.getKey() + ": ").color(NamedTextColor.WHITE)
                      .append(text(
                          entry.getValue().value() + " (" + entry.getValue().type() + ")").color(
                          NamedTextColor.GREEN)))
                  .collect(toComponent(newline())));
          player.sendMessage(component);
        }, () -> {
          player.sendMessage(text("No stats found."));
        });
  }

  @Identified("setcoins")
  private void setcoins(Expansion<Player> source, Player target, int coins) {
    final var player = source.orElseThrow();
    final var statsId = Stats.id(target.getUniqueId().toString(), "generic");
    final var stats = statsRepository.byId(statsId).orElse(new Stats(statsId));
    stats.setLong("coins", coins);
    statsRepository.update(stats).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Coins set to " + coins + "!"));
      }
    }));
  }

  @Identified("timetest")
  private void timetest(Expansion<Player> source, Time time) {
    source.orElseThrow().setPlayerTime(time.time(), false);
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
            page.documents().forEach((stats, placement) -> {
              lines.add(text(placement + ". ").color(NamedTextColor.YELLOW)
                  .append(text(stats.playerId()).color(NamedTextColor.WHITE)
                      .append(text(" - ").color(NamedTextColor.GRAY)
                          .append(text(stats.getLong("coins").orElse(0L) + "").color(
                              NamedTextColor.YELLOW)))));
            });
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
        .through(Npcs.class, () -> new Npcs(plugin))
        .withKey(UUID.randomUUID().toString())
        .to(new Npc(null, plugin) {
          @Override
          public void populate() {
            setLocation(() -> npcLocation);
            editProfile(playerProfile -> {
              playerProfile.setProperty(
                  new ProfileProperty("textures", profile.textures().raw().value(),
                      profile.textures().raw().signature()));
            });
          }

          @Override
          public void handleUse(ServerboundInteractPacket packet) {
            player.sendMessage(text("bing bong").color(NamedTextColor.LIGHT_PURPLE));
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
        .mapToObj(i -> new Stats(Stats.id("random" + i, "generic")))
        .peek(stats -> stats.setLong("coins", ThreadLocalRandom.current().nextInt(1, 10_000)))
        .map(statsRepository::update)
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
      design.bind(player, new CoinsLeaderboardWindow(design, coinsLeaderboard, statsRepository));
      return;
    }

    final var total = statsRepository.count(
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
                        .append(text(entry.getKey().playerId()).color(NamedTextColor.WHITE)))
                    .append(text(" - ").color(NamedTextColor.GRAY))
                    .append(text(entry.getKey().getLong("coins").orElse(0L))))
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
