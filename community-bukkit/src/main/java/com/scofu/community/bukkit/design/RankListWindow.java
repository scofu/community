package com.scofu.community.bukkit.design;

import static com.scofu.design.bukkit.item.Button.button;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
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

  private final RankRepository rankRepository;
  private final TagFactory tagFactory;

  public RankListWindow(
      Design design, RankRepository rankRepository, TagFactory tagFactory, Window parent) {
    super(TickSpeed.NORMAL, parent, design);
    this.rankRepository = rankRepository;
    this.tagFactory = tagFactory;
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Ranks");
  }

  @Override
  protected List<? extends ButtonBuilder<Void>> buttons(String search, int page) {
    // TODO: use cached values or query with search filter
    return rankRepository.find(Query.empty()).join().values().stream()
        .sorted()
        .filter(
            rank ->
                !hasClearableSearch()
                    || rank.id().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
        .map(this::createButton)
        .toList();
  }

  private ButtonBuilder<Void> createButton(Rank rank) {
    return button()
        .item(viewer())
        .material(Material.NAME_TAG)
        .name(text(rank.name()).color(rank.nameColor().orElse(Color.WHITE)))
        .tag(translatable("Priority: %s", text(rank.priority())))
        .tag(translatable("Tag: %s", rank.render().orElseGet(Component::empty)))
        .footer(text("Click to edit!"))
        .footer(text("Shift+Click to delete!"))
        .endItem()
        .event(
            event -> {
              event.setCancelled(true);
              if (event.isShiftClick()) {
                // TODO
                return;
              }
              design()
                  .bind(
                      viewer().player(),
                      new RankEditWindow(design(), rankRepository, tagFactory, this, rank));
            });
  }
}
