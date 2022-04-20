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
import com.scofu.text.Color;
import com.scofu.text.json.TagFactory;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

final class RankListWindow extends PaginatedWindow {

  private final Design ui;
  private final RankRepository rankRepository;
  private final TagFactory tagFactory;
  private final Window parent;

  public RankListWindow(Design design, RankRepository rankRepository, TagFactory tagFactory,
      Window parent) {
    super(TickSpeed.NORMAL, parent, design);
    this.ui = design;
    this.rankRepository = rankRepository;
    this.tagFactory = tagFactory;
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
        .map(this::toButton)
        .toList();


  }

  private ButtonBuilder toButton(Rank rank) {
    return Button.builder()
        .withItem(viewer(), builder -> builder.ofType(Material.NAME_TAG)
            .withName(text(rank.name()).color(
                rank.nameColor().orElse(Color.WHITE).toColor(viewer().theme())))
            .withTag(translatable("Priority: %s", text(rank.priority())))
            .withTag(translatable("Tag: %s", rank.render(viewer().theme()).orElse(empty())))
            .withFooter(text("Click to edit!"))
            .withFooter(text("Shift+Click to delete!")))
        .onClick(event -> {
          event.setCancelled(true);
          if (event.isShiftClick()) {
            //TODO
            return;
          }
          design().bind(viewer().player(),
              new RankEditWindow(design(), rankRepository, tagFactory, this, rank));
        });
  }
}
