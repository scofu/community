package com.scofu.community.bukkit.design;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.scofu.design.bukkit.item.Button.button;
import static com.scofu.network.document.Filter.exists;
import static com.scofu.network.document.Filter.where;
import static com.scofu.network.document.Query.query;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.community.GenericStats;
import com.scofu.community.GenericStatsRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.network.document.Book;
import com.scofu.network.document.Page;
import com.scofu.text.Time;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

/** Coins leaderboard window. */
public final class CoinsLeaderboardWindow extends PaginatedWindow {

  private final Design design;
  private final Book<GenericStats> coinsLeaderboard;
  private final GenericStatsRepository genericStatsRepository;
  private Page<GenericStats> page;
  private Button refreshButton;

  /**
   * Constructs a new coins leaderboard window.
   *
   * @param design the design
   * @param coinsLeaderboard the coins leaderboard
   * @param genericStatsRepository the generic stats repository
   */
  public CoinsLeaderboardWindow(
      Design design,
      Book<GenericStats> coinsLeaderboard,
      GenericStatsRepository genericStatsRepository) {
    super(TickSpeed.NORMAL, null, design);
    checkNotNull(design, "design");
    checkNotNull(coinsLeaderboard, "coinsLeaderboard");
    checkNotNull(genericStatsRepository, "genericStatsRepository");
    this.design = design;
    this.coinsLeaderboard = coinsLeaderboard;
    this.genericStatsRepository = genericStatsRepository;
  }

  @Override
  public void populate() {
    page = coinsLeaderboard.page(page());
    super.populate();
    button()
        .ticking(true)
        .slot(9 * 5)
        .item(viewer())
        .material(Material.CLOCK)
        .name(text("Next Refresh"))
        .description(
            translatable(
                "The next refresh is in %s.",
                Time.formatDurationDetailed(coinsLeaderboard.durationUntilNextRefresh())))
        .endItem()
        .event(event -> event.setCancelled(true))
        .build(this::populate);
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Coins");
  }

  @Override
  protected List<? extends ButtonBuilder<Void>> buttons(String search, int page) {
    if (hasClearableSearch()) {
      return IntStream.rangeClosed(1, maxPagesInBook())
          .mapToObj(coinsLeaderboard::page)
          .map(Page::documents)
          .map(Map::entrySet)
          .flatMap(Set::stream)
          .filter(
              entry ->
                  entry
                      .getKey()
                      .id()
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
        .map(entry -> createButton(entry.getKey(), entry.getValue()))
        .toList();
  }

  @Override
  public int maxPages(int buttons) {
    if (hasClearableSearch()) {
      return super.maxPages(buttons);
    }
    return maxPagesInBook();
  }

  private int maxPagesInBook() {
    final var total =
        genericStatsRepository.count(query().filter(where("coins", exists(true))).build()).join();
    return (int) Math.ceil(total / (double) PaginatedWindow.ITEMS_PER_PAGE);
  }

  private ButtonBuilder<Void> createButton(GenericStats stats, int placement) {
    return button()
        .item(viewer())
        .material(Material.PLAYER_HEAD)
        .name(translatable("%s. %s", text(placement), text(stats.id())))
        .description(text(stats.coins()))
        .endItem()
        .event(event -> event.setCancelled(true));
  }
}
