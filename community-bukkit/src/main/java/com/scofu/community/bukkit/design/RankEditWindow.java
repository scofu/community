package com.scofu.community.bukkit.design;

import static com.scofu.design.bukkit.item.Button.backButton;
import static com.scofu.design.bukkit.item.Button.button;
import static com.scofu.text.ContextualizedComponent.error;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.sign.Sign;
import com.scofu.design.bukkit.window.ColorPickerWindow;
import com.scofu.design.bukkit.window.FlowWindow;
import com.scofu.design.bukkit.window.TagCreatorWindow;
import com.scofu.design.bukkit.window.Window;
import com.scofu.text.Color;
import com.scofu.text.json.TagFactory;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

final class RankEditWindow extends FlowWindow {

  private final Design design;
  private final RankRepository rankRepository;
  private final TagFactory tagFactory;
  private final Window parent;
  private final Rank rank;

  public RankEditWindow(
      Design ui, RankRepository rankRepository, TagFactory tagFactory, Window parent, Rank rank) {
    super(null);
    this.rankRepository = rankRepository;
    this.design = ui;
    this.tagFactory = tagFactory;
    this.parent = parent;
    this.rank = rank;
  }

  @Override
  protected Component title() {
    return text("Ranks");
  }

  @Override
  protected Optional<List<? extends ButtonBuilder<Void>>> header() {
    final var rankButton =
        button()
            .item(viewer())
            .material(Material.NAME_TAG)
            .name(text(rank.name()))
            .endItem()
            .event(event -> event.setCancelled(true));
    return Optional.of(rank).map(rank -> List.of(rankButton));
  }

  @Override
  protected List<? extends ButtonBuilder<Void>> buttons() {
    final var nameButton =
        button()
            .item(viewer())
            .material(Material.SPRUCE_SIGN)
            .name(text("Name"))
            .description(text("Change the name."))
            .tag(translatable("Current: %s", text(rank.name())))
            .footer(text("Click to rename!"))
            .endItem()
            .event(
                event -> {
                  event.setCancelled(true);
                  design
                      .request(viewer().player(), new Sign(), text("Enter the rank name!"))
                      .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                      .thenAcceptAsync(
                          result -> {
                            if (result == null) {
                              error()
                                  .text("Timed out.")
                                  .prefixed()
                                  .render(viewer().player()::sendMessage);
                              design.bind(viewer().player(), this);
                              return;
                            }
                            if (rankRepository.findByName(result).join().isPresent()) {
                              error()
                                  .text("A rank with the name %s already exists.", result)
                                  .prefixed()
                                  .render(viewer().player()::sendMessage);
                              design.bind(viewer().player(), this);
                              return;
                            }
                            rank.setName(result);
                            design.bind(viewer().player(), this);
                          });
                });
    final var priorityButton =
        button()
            .item(viewer())
            .material(Material.PIGLIN_BANNER_PATTERN)
            .name(text("Priority"))
            .description(text("Increase or decrease the priority."))
            .tag(translatable("Current: %s", text(rank.priority())))
            .footer(text("Left-Click to increase!"))
            .footer(text("Right-Click to decrease!"))
            .endItem()
            .event(
                event -> {
                  event.setCancelled(true);
                  if (event.isLeftClick()) {
                    rank.incrementPriority();
                  } else if (event.isRightClick()) {
                    rank.decrementPriority();
                  }
                  design.bind(viewer().player(), this);
                });
    final var tagButton =
        button()
            .item(viewer())
            .material(Material.PIGLIN_BANNER_PATTERN)
            .name(text("Tag"))
            .description(text("Change the tag."))
            .tag(translatable("Current: %s", rank.render().orElse(empty())))
            .footer(text("Click to set the tag!"))
            .endItem()
            .event(
                event -> {
                  event.setCancelled(true);
                  design
                      .request(
                          viewer().player(),
                          new TagCreatorWindow(design, tagFactory),
                          rank.tag()
                              .orElseGet(
                                  () ->
                                      tagFactory.create(
                                          rank.name().toLowerCase(Locale.ROOT),
                                          Color.BRIGHT_WHITE,
                                          Color.BLACK)))
                      .completeOnTimeout(null, 20, TimeUnit.MINUTES)
                      .thenAcceptAsync(
                          result -> {
                            if (result == null) {
                              error()
                                  .text("Timed out.")
                                  .prefixed()
                                  .render(viewer().player()::sendMessage);
                              design.bind(viewer().player(), this);
                              return;
                            }
                            rank.setTag(result);
                            design.bind(viewer().player(), this);
                          });
                });
    final var nameColorButton =
        button()
            .item(viewer())
            .material(Material.WHITE_DYE)
            .name(text("Name Color"))
            .description(text("Change the name color."))
            .tag(
                translatable(
                    "Current: %s",
                    rank.nameColor().map(color -> text("THIS").color(color)).orElse(empty())))
            .endItem()
            .event(
                event -> {
                  event.setCancelled(true);
                  design
                      .request(viewer().player(), new ColorPickerWindow(design), null)
                      .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                      .thenAcceptAsync(
                          result -> {
                            if (result == null) {
                              error()
                                  .text("Timed out.")
                                  .prefixed()
                                  .render(viewer().player()::sendMessage);
                              design.bind(viewer().player(), this);
                              return;
                            }
                            rank.setNameColor(result);
                            design.bind(viewer().player(), this);
                          });
                });
    final var permissionsButton =
        button()
            .item(viewer())
            .material(Material.BOOK)
            .name(text("Permissions"))
            .description(text("Edit permissions."))
            .footer(text("Click to edit permissions!"))
            .endItem()
            .event(
                event -> {
                  event.setCancelled(true);
                  design.bind(viewer().player(), new RankPermissionListWindow(this, design, rank));
                });
    final var saveButton =
        button()
            .item(viewer())
            .material(Material.HEART_OF_THE_SEA)
            .name(text("Save"))
            .description(text("Save any current changes."))
            .endItem()
            .event(
                event -> {
                  event.setCancelled(true);
                  rankRepository.update(rank);
                });
    return List.of(
        nameButton, priorityButton, tagButton, nameColorButton, permissionsButton, saveButton);
  }

  @Override
  protected Optional<List<? extends ButtonBuilder<Void>>> footer() {
    return Optional.ofNullable(parent)
        .map(parent -> backButton(viewer(), parent, design))
        .map(List::of);
  }
}
