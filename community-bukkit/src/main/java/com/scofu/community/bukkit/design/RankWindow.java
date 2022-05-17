package com.scofu.community.bukkit.design;

import static com.scofu.design.bukkit.item.Button.button;
import static com.scofu.text.ContextualizedComponent.error;
import static net.kyori.adventure.text.Component.text;

import com.google.inject.Inject;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.sign.Sign;
import com.scofu.design.bukkit.window.FlowWindow;
import com.scofu.text.json.TagFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

/** Rank window. */
public final class RankWindow extends FlowWindow {

  private final Design design;
  private final RankRepository rankRepository;
  private final LazyFactory lazyFactory;
  private final TagFactory tagFactory;

  @Inject
  RankWindow(
      Design design,
      RankRepository rankRepository,
      LazyFactory lazyFactory,
      TagFactory tagFactory) {
    super(null);
    this.rankRepository = rankRepository;
    this.design = design;
    this.lazyFactory = lazyFactory;
    this.tagFactory = tagFactory;
  }

  @Override
  protected Component title() {
    return text("Ranks");
  }

  @Override
  protected Optional<List<? extends ButtonBuilder<Void>>> header() {
    return Optional.empty();
  }

  @Override
  protected List<? extends ButtonBuilder<Void>> buttons() {
    final var listButton =
        button()
            .item(viewer())
            .material(Material.BOOK)
            .name(text("List Ranks"))
            .description(text("View a list of all ranks."))
            .endItem()
            .event(
                event -> {
                  event.setCancelled(true);
                  design.bind(
                      viewer().player(),
                      new RankListWindow(design, rankRepository, tagFactory, this));
                });
    final var createButton =
        button()
            .item(viewer())
            .material(Material.NETHER_STAR)
            .name(text("Create a new rank"))
            .description(text("Create and edit a new rank."))
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
                            final var rank =
                                lazyFactory.create(
                                    Rank.class,
                                    Rank::id,
                                    UUID.randomUUID().toString(),
                                    Rank::name,
                                    result);
                            design.bind(
                                viewer().player(),
                                new RankEditWindow(design, rankRepository, tagFactory, this, rank));
                          });
                });
    return List.of(listButton, createButton);
  }

  @Override
  protected Optional<List<? extends ButtonBuilder<Void>>> footer() {
    return Optional.empty();
  }
}
