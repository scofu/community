package com.scofu.community.bukkit;

import static com.scofu.text.ContextualizedComponent.error;
import static com.scofu.text.ContextualizedComponent.info;
import static net.kyori.adventure.text.Component.text;

import com.scofu.common.json.PeriodEscapedString;
import com.scofu.community.Rank;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.chat.Chat;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.ConfirmWindow;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.design.bukkit.window.Window;
import java.util.List;
import java.util.Locale;
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
    populate(Button.builder()
        .at(45)
        .withStaticItem(viewer(), itemBuilder -> itemBuilder.ofType(Material.SCUTE)
            .withName(text("Add"))
            .withDescription(text("Add a new permission."))
            .withFooter(text("Click to add!")))
        .onClick(event -> {
          event.setCancelled(true);
          viewer().player().closeInventory();
          info().text("Please enter permission:")
              .prefixed()
              .renderTo(viewer().theme(), viewer().player()::sendMessage);
          design().bind(viewer().player(), new Chat())
              .result()
              .completeOnTimeout(null, 10, TimeUnit.MINUTES)
              .whenComplete(((result, throwable) -> {
                if (result == null) {
                  error().text("Timed out.")
                      .prefixed()
                      .renderTo(viewer().theme(), viewer().player()::sendMessage);
                  design().bind(viewer().player(), this);
                  return;
                }
                rank.addPermission(new PeriodEscapedString(result), true);
                design().bind(viewer().player(), this);
              }));
        })
        .build());
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Permissions");
  }

  @Override
  protected List<? extends ButtonBuilder> buttons(String search, int page) {
    return rank.permissions()
        .map(map -> map.entrySet().stream())
        .stream()
        .flatMap(Function.identity())
        .filter(entry -> !hasClearableSearch() || entry.getKey()
            .toString()
            .toLowerCase(Locale.ROOT)
            .contains(search.toLowerCase(Locale.ROOT)))
        .map(entry -> Button.builder()
            .withStaticItem(viewer(), itemBuilder -> itemBuilder.ofType(
                    entry.getValue() ? Material.LIME_DYE : Material.GRAY_DYE)
                .withName(text(entry.getKey().toString()))
                .withDescription(
                    entry.getValue() ? text("Positive permission.") : text("Negative permission."))
                .withFooter(text("Click to toggle!"))
                .withFooter(text("Shift+Click to remove!")))
            .onClick(event -> {
              event.setCancelled(true);
              if (event.isShiftClick()) {
                design().bind(viewer().player(), new ConfirmWindow(text("Are you sure?")))
                    .result()
                    .completeOnTimeout(null, 5, TimeUnit.MINUTES)
                    .whenComplete((result, throwable) -> {
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
            }))
        .toList();
  }
}
