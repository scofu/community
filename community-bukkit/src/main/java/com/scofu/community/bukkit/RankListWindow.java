package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.design.bukkit.window.Window;
import com.scofu.network.document.Query;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

final class RankListWindow extends PaginatedWindow {

  private final Design ui;
  private final RankRepository rankRepository;
  private final Window parent;

  public RankListWindow(Design design, RankRepository rankRepository, Window parent) {
    super(TickSpeed.NORMAL, parent, design);
    this.ui = design;
    this.rankRepository = rankRepository;
    this.parent = parent;
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Ranks");
  }

  @Override
  protected List<? extends ButtonBuilder> buttons(String search, int page) {
    // TODO: use cached values or query with search filter
    return rankRepository.find(Query.empty())
        .join()
        .values()
        .stream()
        .sorted()
        .filter(rank -> !hasClearableSearch() || rank.id()
            .toLowerCase(Locale.ROOT)
            .contains(search.toLowerCase(Locale.ROOT)))
        .map(this::createButton)
        .toList();


  }

  private ButtonBuilder createButton(Rank rank) {
    return Button.builder()
        .withItem(player().locale(), builder -> builder.ofType(Material.NAME_TAG)
            .withName(text(rank.id()).color(rank.nameColor().orElse(NamedTextColor.WHITE)))
            .withTag(translatable("Priority: %s", text(rank.priority())))
            .withTag(translatable("Prefix: %s", rank.prefix().orElse(empty())))
            .withFooter(text("Click to edit!"))
            .withFooter(text("Shift+Click to delete!")))
        .onClick(event -> {
          event.setCancelled(true);
        });
  }
}
