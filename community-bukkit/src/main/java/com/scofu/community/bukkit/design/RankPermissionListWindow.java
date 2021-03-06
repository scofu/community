package com.scofu.community.bukkit.design;

import static com.scofu.design.bukkit.item.Button.button;
import static com.scofu.text.ContextualizedComponent.error;
import static com.scofu.text.ContextualizedComponent.info;
import static net.kyori.adventure.text.Component.text;

import com.scofu.common.PeriodEscapedString;
import com.scofu.community.Rank;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.chat.Chat;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.ConfirmWindow;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.design.bukkit.window.Window;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

final class RankPermissionListWindow extends PaginatedWindow {

  private final Rank rank;

  public RankPermissionListWindow(Window parent, Design design, Rank rank) {
    super(null, parent, design);
    this.rank = rank;
  }

  @Override
  public void populate() {
    super.populate();

    button()
        .slot(45)
        .item(viewer())
        .material(Material.SCUTE)
        .name(text("Add"))
        .description(text("Add a new permission."))
        .footer(text("Click to add!"))
        .endItem()
        .event(
            event -> {
              event.setCancelled(true);
              viewer().player().closeInventory();
              design()
                  .request(
                      viewer().player(),
                      new Chat(),
                      info().text("Please enter permission:").prefixed())
                  .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                  .thenAcceptAsync(
                      result -> {
                        if (result == null) {
                          error()
                              .text("Timed out.")
                              .prefixed()
                              .render(viewer().player()::sendMessage);
                          design().bind(viewer().player(), this);
                          return;
                        }
                        rank.addPermission(new PeriodEscapedString(result), true);
                        design().bind(viewer().player(), this);
                      });
            })
        .build(this::populate);
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Permissions");
  }

  @Override
  protected List<? extends ButtonBuilder<Void>> buttons(String search, int page) {
    return rank.permissions().map(map -> map.entrySet().stream()).stream()
        .flatMap(Function.identity())
        .filter(
            entry ->
                !hasClearableSearch()
                    || entry
                        .getKey()
                        .toString()
                        .toLowerCase(Locale.ROOT)
                        .contains(search.toLowerCase(Locale.ROOT)))
        .map(this::createButton)
        .toList();
  }

  private ButtonBuilder<Void> createButton(Entry<PeriodEscapedString, Boolean> entry) {
    return button()
        .item(viewer())
        .material(entry.getValue() ? Material.LIME_DYE : Material.GRAY_DYE)
        .name(text(entry.getKey().toString()))
        .description(entry.getValue() ? text("Positive permission.") : text("Negative permission."))
        .footer(text("Click to toggle!"))
        .footer(text("Shift+Click to remove!"))
        .endItem()
        .event(
            event -> {
              event.setCancelled(true);
              if (event.isShiftClick()) {
                design()
                    .request(viewer().player(), new ConfirmWindow(), text("Are you sure?"))
                    .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                    .thenAcceptAsync(
                        result -> {
                          if (result == null || !result) {
                            design().bind(viewer().player(), this);
                            return;
                          }
                          rank.removePermission(entry.getKey());
                          design().bind(viewer().player(), this);
                        });
                return;
              }
              rank.addPermission(entry.getKey(), !entry.getValue());
              design().bind(viewer().player(), this);
            });
  }
}
