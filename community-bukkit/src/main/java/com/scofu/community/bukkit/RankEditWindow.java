package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.Rank;
import com.scofu.community.RankPrefix;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.chat.Chat;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.FlowWindow;
import com.scofu.design.bukkit.window.Window;
import com.scofu.text.Color;
import com.scofu.text.Tags;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

final class RankEditWindow extends FlowWindow {

  private final Design design;
  private final RankRepository rankRepository;
  private final LazyFactory lazyFactory;
  private final Window parent;
  private final Rank rank;

  public RankEditWindow(Design ui, RankRepository rankRepository, LazyFactory lazyFactory,
      Window parent, Rank rank) {
    super(TickSpeed.NORMAL);
    this.rankRepository = rankRepository;
    this.design = ui;
    this.lazyFactory = lazyFactory;
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
          .withItem(viewer(),
              builder -> builder.ofType(Material.NAME_TAG).withName(text(rank.id())))
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
              //TODO;
            }),

        Button.builder()
            .withItem(viewer(), builder -> builder.ofType(Material.PIGLIN_BANNER_PATTERN)
                .withName(text("Priorty"))
                .withDescription(text("Increase or decrease the priority."))
                .withTag(translatable("Current: %s", text(rank.priority())))
                .withFooter(text("Left-Click to increase!"))
                .withFooter(text("Right-Click to decrease!")))
            .onClick(event -> {
              event.setCancelled(true);
              if (event.isLeftClick()) {
                rank.setPriority(rank.priority() + 1);
              } else if (event.isRightClick()) {
                rank.setPriority(rank.priority() - 1);
              }
            }),

        Button.builder()
            .withItem(viewer(), builder -> builder.ofType(Material.PIGLIN_BANNER_PATTERN)
                .withName(text("Prefix"))
                .withDescription(text("Change the prefix."))
                .withTag(translatable("Current: %s", rank.prefix()
                    .flatMap(prefix -> prefix.render(viewer().theme()))
                    .orElse(empty())))
                .withFooter(text("Click to set the prefix!")))
            .onClick(event -> {
              event.setCancelled(true);
              viewer().player().closeInventory();
              viewer().player()
                  .sendMessage(newline().append(text("Enter prefix:").append(newline())));
              design.bind(viewer().player(), new Chat())
                  .result()
                  .completeOnTimeout(null, 1, TimeUnit.MINUTES)
                  .thenAccept(result -> {
                    if (result == null || result.isEmpty() || result.isBlank()) {
                      design.bind(viewer().player(), this);
                      return;
                    }
                    try {
                      final var prefix = lazyFactory.create(RankPrefix.class);
                      prefix.setTag(
                          Tags.tag(result, NamedTextColor.WHITE, NamedTextColor.BLACK, false, 2));
                      rank.setPrefix(prefix);
                    } catch (Exception e) {
                      e.printStackTrace();
                      viewer().player().sendMessage(text("Error parsing tag."));
                    }
                    design.bind(viewer().player(), this);
                  });
            }),

        Button.builder()
            .withItem(viewer(), builder -> builder.ofType(Material.WHITE_DYE)
                .withName(text("Name color"))
                .withDescription(text("Change the name color."))
                .withTag(translatable("Current: %s", rank.nameColor()
                    .map(color -> text("THIS").color(color.toColor(viewer().theme())))
                    .orElse(empty())))
                .withFooter(text("Click to set the name color!")))
            .onClick(event -> {
              event.setCancelled(true);
              viewer().player().closeInventory();
              viewer().player()
                  .sendMessage(newline().append(text("Enter name color:").append(newline())));
              design.bind(viewer().player(), new Chat())
                  .result()
                  .completeOnTimeout(null, 1, TimeUnit.MINUTES)
                  .thenAccept(result -> {
                    if (result == null || result.isEmpty() || result.isBlank()) {
                      design.bind(viewer().player(), this);
                      return;
                    }
                    try {
                      final var color = Color.byName(result).orElseThrow();
                      rank.setNameColor(color);
                    } catch (Exception e) {
                      e.printStackTrace();
                      viewer().player().sendMessage(text("Error parsing color."));
                    }
                    design.bind(viewer().player(), this);
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
