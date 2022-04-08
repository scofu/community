package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.chat.Chat;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.FlowWindow;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

final class RankWindow extends FlowWindow {

  private final Design design;
  private final RankRepository rankRepository;

  public RankWindow(Design design, RankRepository rankRepository) {
    super(null);
    this.rankRepository = rankRepository;
    this.design = design;
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
            .withStaticItem(player().locale(), builder -> builder.ofType(Material.BOOK)
                .withName(text("List ranks"))
                .withDescription(text("View a list of all ranks.")))
            .onClick(event -> {
              event.setCancelled(true);
              design.bind(player(), new RankListWindow(design, rankRepository, this));
            }),

        Button.builder()
            .withStaticItem(player().locale(), builder -> builder.ofType(Material.NETHER_STAR)
                .withName(text("Create a new rank"))
                .withDescription(text("Create and edit a new rank.")))
            .onClick(event -> {
              event.setCancelled(true);
              player().closeInventory();
              player().sendMessage(newline().append(text("Enter rank name:")).append(newline()));
              design.bind(player(), new Chat())
                  .result()
                  .completeOnTimeout(null, 1, TimeUnit.MINUTES)
                  .thenAccept(result -> {
                    if (result == null || result.isEmpty() || result.isBlank()) {
                      design.bind(player(), this);
                      return;
                    }
                    if (rankRepository.byId(result).isPresent()) {
                      design.bind(player(), this);
                      player().sendMessage(text("Name taken."));
                      return;
                    }
                    design.bind(player(),
                        new RankEditWindow(design, rankRepository, this, new Rank(result)));
                  });
            }));
  }

  @Override
  protected Optional<List<? extends ButtonBuilder>> footer() {
    return Optional.empty();
  }
}
