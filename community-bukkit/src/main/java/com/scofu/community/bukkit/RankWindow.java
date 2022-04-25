package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;

import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.sign.Sign;
import com.scofu.design.bukkit.window.FlowWindow;
import com.scofu.text.json.TagFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

final class RankWindow extends FlowWindow {

  private final Design design;
  private final RankRepository rankRepository;
  private final LazyFactory lazyFactory;
  private final TagFactory tagFactory;

  public RankWindow(Design design, RankRepository rankRepository, LazyFactory lazyFactory,
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
  protected Optional<List<? extends ButtonBuilder>> header() {
    return Optional.empty();
  }

  @Override
  protected List<? extends ButtonBuilder> buttons() {
    return List.of(

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.BOOK)
                .withName(text("List ranks"))
                .withDescription(text("View a list of all ranks.")))
            .onClick(event -> {
              event.setCancelled(true);
              design.bind(viewer().player(),
                  new RankListWindow(design, rankRepository, tagFactory, this));
            }),

        Button.builder()
            .withStaticItem(viewer(), builder -> builder.ofType(Material.NETHER_STAR)
                .withName(text("Create a new rank"))
                .withDescription(text("Create and edit a new rank.")))
            .onClick(event -> {
              event.setCancelled(true);
              design.bind(viewer().player(),
                  Sign.builder().withInput(text("Enter the rank name!")).onInput(result -> {
                    if (rankRepository.findByName(result).join().isPresent()) {
                      design.bind(viewer().player(), this);
                      viewer().player().sendMessage(text("Name taken."));
                      return;
                    }
                    final var rank = lazyFactory.create(Rank.class, Rank::id,
                        UUID.randomUUID().toString(), Rank::name, result);
                    design.bind(viewer().player(),
                        new RankEditWindow(design, rankRepository, tagFactory, this, rank));
                  }).onTimeout(() -> design.bind(viewer().player(), this)).build());
            }));
  }

  @Override
  protected Optional<List<? extends ButtonBuilder>> footer() {
    return Optional.empty();
  }
}
