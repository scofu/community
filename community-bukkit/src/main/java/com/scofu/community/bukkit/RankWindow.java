package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;

import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.chat.Chat;
import com.scofu.design.bukkit.item.Button;
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
                      design.bind(viewer().player(), RankWindow.this);
                      return;
                    }
                    if (rankRepository.findByName(name).join().isPresent()) {
                      design.bind(viewer().player(), RankWindow.this);
                      viewer().player().sendMessage(text("Name taken."));
                      return;
                    }
                    final var rank = lazyFactory.create(Rank.class, Rank::id,
                        UUID.randomUUID().toString(), Rank::name, name);
                    design.bind(viewer().player(),
                        new RankEditWindow(design, rankRepository, tagFactory, this, rank));
                  });

              design.bind(viewer().player(), new Chat())
                  .result()
                  .completeOnTimeout(null, 1, TimeUnit.MINUTES)
                  .thenAccept(name -> {
                    if (name == null || name.isEmpty() || name.isBlank()) {
                      design.bind(viewer().player(), this);
                      return;
                    }
                    if (rankRepository.findByName(name).join().isPresent()) {
                      design.bind(viewer().player(), this);
                      viewer().player().sendMessage(text("Name taken."));
                      return;
                    }
                    final var rank = lazyFactory.create(Rank.class, Rank::id,
                        UUID.randomUUID().toString(), Rank::name, name);
                    design.bind(viewer().player(),
                        new RankEditWindow(design, rankRepository, tagFactory, this, rank));
                  });
            }));
  }

  @Override
  protected Optional<List<? extends ButtonBuilder>> footer() {
    return Optional.empty();
  }
}
