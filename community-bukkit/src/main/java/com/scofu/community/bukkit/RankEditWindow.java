package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.Button;
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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

final class RankEditWindow extends FlowWindow {

  private final Design design;
  private final RankRepository rankRepository;
  private final TagFactory tagFactory;
  private final Window parent;
  private final Rank rank;

  public RankEditWindow(Design ui, RankRepository rankRepository, TagFactory tagFactory,
      Window parent, Rank rank) {
    super(TickSpeed.NORMAL);
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
  protected Optional<List<? extends ButtonBuilder>> header() {
    return Optional.of(rank).map(rank -> {
      return List.of(Button.builder()
          .withStaticItem(viewer(),
              builder -> builder.ofType(Material.NAME_TAG).withName(text(rank.name())))
          .onClick(event -> event.setCancelled(true))

      );
    });
  }

  @Override
  protected List<? extends ButtonBuilder> buttons() {
    return List.of(

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.SPRUCE_SIGN)
                .withName(text("Name"))
                .withDescription(text("Change the name."))
                .withTag(translatable("Current: %s", text(rank.name())))
                .withFooter(text("Click to rename!")))
            .onClick(event -> {
              event.setCancelled(true);
              viewer().player().closeInventory();
              design.bind(viewer().player(), new Sign() {
                    @Override
                    public void populate() {
                      lines()[1] = text("^^^^^^");
                      lines()[2] = text("Enter the");
                      lines()[3] = text("rank name!");
                    }
                  })
                  .result()
                  .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                  .whenComplete((result, throwable) -> {
                    if (result == null) {
                      viewer().player().sendMessage(text("Timed out..."));
                      return;
                    }
                    final var name = result[0];
                    if (name == null || name.isEmpty() || name.isBlank()) {
                      viewer().player().sendMessage(text("Invalid name."));
                      design.bind(viewer().player(), RankEditWindow.this);
                      return;
                    }
                    if (rankRepository.findByName(name).join().isPresent()) {
                      design.bind(viewer().player(), RankEditWindow.this);
                      viewer().player().sendMessage(text("Name taken."));
                      return;
                    }
                    rank.setName(name);
                    design.bind(viewer().player(), RankEditWindow.this);
                  });
            }),

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.PIGLIN_BANNER_PATTERN)
                .withName(text("Priorty"))
                .withDescription(text("Increase or decrease the priority."))
                .withTag(translatable("Current: %s", text(rank.priority())))
                .withFooter(text("Left-Click to increase!"))
                .withFooter(text("Right-Click to decrease!")))
            .onClick(event -> {
              event.setCancelled(true);
              if (event.isLeftClick()) {
                rank.incrementPriority();
              } else if (event.isRightClick()) {
                rank.decrementPriority();
              }
              design.bind(viewer().player(), this);
            }),

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.PIGLIN_BANNER_PATTERN)
                .withName(text("Tag"))
                .withDescription(text("Change the tag."))
                .withTag(translatable("Current: %s", rank.render(viewer().theme()).orElse(empty())))
                .withFooter(text("Click to set the tag!")))
            .onClick(event -> {
              event.setCancelled(true);
              viewer().player().closeInventory();
              design.bind(viewer().player(), new TagCreatorWindow(rank.tag()
                      .orElseGet(() -> tagFactory.create(rank.name().toLowerCase(Locale.ROOT),
                          Color.BRIGHT_WHITE, Color.BLACK)), design, tagFactory))
                  .result()
                  .completeOnTimeout(null, 20, TimeUnit.MINUTES)
                  .thenAccept(tag -> {
                    if (tag == null) {
                      viewer().player().sendMessage(text("Timed out."));
                      design.bind(viewer().player(), RankEditWindow.this);
                      return;
                    }
                    rank.setTag(tag);
                    design.bind(viewer().player(), RankEditWindow.this);
                  });
            }),

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.WHITE_DYE)
                .withName(text("Name color"))
                .withDescription(text("Change the name color."))
                .withTag(translatable("Current: %s", rank.nameColor()
                    .map(color -> text("THIS").color(color.toColor(viewer().theme())))
                    .orElse(empty())))
                .withFooter(text("Click to set the name color!")))
            .onClick(event -> {
              event.setCancelled(true);
              design.bind(viewer().player(), new ColorPickerWindow(design))
                  .result()
                  .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                  .thenAccept(color -> {
                    if (color == null) {
                      viewer().player().sendMessage(text("Timed out."));

                      design.bind(viewer().player(), RankEditWindow.this);
                      return;
                    }
                    rank.setNameColor(color);
                    design.bind(viewer().player(), RankEditWindow.this);
                  });
            }),

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.HEART_OF_THE_SEA)
                .withName(text("Save"))
                .withDescription(text("Save any current changes.")))
            .onClick(event -> {
              event.setCancelled(true);
              rankRepository.update(rank);
            }));
  }

  @Override
  protected Optional<List<? extends ButtonBuilder>> footer() {
    return Optional.ofNullable(parent).map(window -> List.of(

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.RED_DYE)
                .withName(text("Go back").color(NamedTextColor.RED))
                .withDescription(text("Return to the previous window.")))
            .onClick(event -> {
              event.setCancelled(true);
              design.bind(viewer().player(), parent);
            })

    ));
  }
}
