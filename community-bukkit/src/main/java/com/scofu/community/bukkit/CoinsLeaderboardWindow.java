package com.scofu.community.bukkit;

import static com.scofu.network.document.Filter.exists;
import static com.scofu.network.document.Filter.where;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.community.Stats;
import com.scofu.community.StatsRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.design.bukkit.window.Window;
import com.scofu.network.document.Book;
import com.scofu.network.document.Page;
import com.scofu.network.document.Query;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

final class CoinsLeaderboardWindow extends PaginatedWindow {

  private final Design design;
  private final Book<Stats> coinsLeaderboard;
  private final StatsRepository statsRepository;
  private Page<Stats> page;

  public CoinsLeaderboardWindow(Design design, Book<Stats> coinsLeaderboard,
      StatsRepository statsRepository) {
    super(TickSpeed.NORMAL, null, design);
    this.design = design;
    this.coinsLeaderboard = coinsLeaderboard;
    this.statsRepository = statsRepository;
  }

  @Override
  public void populate() {
    page = coinsLeaderboard.page(page());
    super.populate();
    final var button = Button.builder()
        .at(9 * 5)
        .withItem(player().locale(), builder -> builder.ofType(Material.CLOCK)
            .withName(text("Next Refresh"))
            .withDescription(translatable("Next refresh is in {0}s.",
                text(coinsLeaderboard.durationUntilNextRefresh().toSeconds()))))
        .onClick(event -> {
          event.setCancelled(true);
        })
        .<Window>build();
    populate(button);
    //    populate(Button.dynamic(button, item -> {
    //      item.setLore(1,
    //          itemText(coinsLeaderboard.durationUntilNextRefresh().toSeconds() + "s.").color(
    //              NamedTextColor.GRAY));
    //    }));
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Ranks: (" + page + "/" + pages + ")" + (search.isEmpty() || search.isBlank() ? ""
        : " \"" + search + "\""));
  }

  @Override
  protected List<? extends ButtonBuilder> buttons(String search, int page) {
    if (hasClearableSearch()) {
      return IntStream.rangeClosed(1, maxPagesInBook())
          .mapToObj(coinsLeaderboard::page)
          .map(Page::documents)
          .map(Map::entrySet)
          .flatMap(Set::stream)
          .filter(entry -> entry.getKey()
              .playerId()
              .toLowerCase(Locale.ROOT)
              .contains(search.toLowerCase(Locale.ROOT)))
          .map(entry -> createButton(entry.getKey(), entry.getValue()))
          .toList();
    }
    final var placement = new AtomicInteger((page - 1) * 45);
    return this.page.documents().entrySet().stream()
        //        .filter(stats -> search.isEmpty() || search.isBlank() ||
        //            stats.playerId().toLowerCase(Locale.ROOT).contains(search.toLowerCase
        //            (Locale.ROOT)))
        .map(entry -> createButton(entry.getKey(), entry.getValue())).toList();
  }

  @Override
  public int maxPages(int buttons) {
    if (hasClearableSearch()) {
      return super.maxPages(buttons);
    }
    return maxPagesInBook();
  }

  private int maxPagesInBook() {
    final var total = statsRepository.count(
        Query.builder().filter(where("references.coins", exists(true))).build()).join();
    return (int) Math.ceil(total / (double) PaginatedWindow.ITEMS_PER_PAGE);
  }

  private ButtonBuilder createButton(Stats stats, int placement) {
    return Button.builder()
        .withStaticItem(player().locale(), builder -> builder.ofType(Material.PLAYER_HEAD)
            .withName(translatable("%s %s", text(placement + "."), text(stats.playerId())))
            .withDescription(text(stats.getLong("coins").orElse(0L))))
        .onClick(event -> {
          event.setCancelled(true);
        });
  }
}
