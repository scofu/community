package com.scofu.community.bukkit;

import static com.scofu.text.Components.wrap;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.common.collect.Lists;
import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.command.validation.Permission;
import com.scofu.common.inject.Feature;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.Grant;
import com.scofu.community.GrantRepository;
import com.scofu.community.Rank;
import com.scofu.design.bukkit.Container.TickSpeed;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.tablist.Tablist;
import com.scofu.design.bukkit.tablist.TablistEntry;
import com.scofu.design.bukkit.window.PaginatedWindow;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

final class GrantCommands implements Listener, Feature {

  private final GrantRepository grantRepository;
  private final Design design;
  private final LazyFactory lazyFactory;

  @Inject
  GrantCommands(GrantRepository grantRepository, Design design, LazyFactory lazyFactory) {
    this.grantRepository = grantRepository;
    this.design = design;
    this.lazyFactory = lazyFactory;
  }

  //  @Identified("emojis")
  //  private void emojis(@Source Player player) {
  //    final var packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a);
  //    packet.b()
  //        .add(new PlayerInfoData(new GameProfile(UUID.randomUUID(), ":smile:"), 0,
  //        EnumGamemode.a,
  //            null));
  //    packet.b()
  //        .add(new PlayerInfoData(new GameProfile(UUID.randomUUID(), ":frown:"), 0,
  //        EnumGamemode.a,
  //            null));
  //    ((CraftPlayer) player).getHandle().networkManager.a(packet);
  //  }

  @Identified("console")
  private void console(String command) {
    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
  }

  @Identified("grants")
  @Permission("scofu.command.grants")
  private void grants(Expansion<Player> source, Player target) {
    final var player = source.orElseThrow();
    player.sendMessage(translatable("Loading grants....").color(NamedTextColor.GRAY));
    grantRepository.allByUserId(target.getUniqueId().toString())
        .whenComplete(((grants, throwable) -> {
          if (throwable != null) {
            player.sendMessage(translatable("Error, something went wrong."));
          } else {
            player.sendMessage(text("Opening menu...").color(NamedTextColor.GRAY));
            try {
              design.bind(player, new GrantListWindow(null, design,
                  grants.sorted(Comparator.comparing(Grant::issuedAt))
                      .collect(Collectors.toCollection(Lists::newArrayList)), grantRepository));
            } catch (Throwable throwable1) {
              throwable1.printStackTrace();
              System.out.println(throwable1.getCause());
              player.sendMessage("error");
            }
            player.sendMessage("OKOKOK");
          }
        }));
  }

  @Identified("grant")
  @Permission("scofu.command.grant")
  private void grant(Expansion<Player> source, Player target, Rank rank, String reason,
      Optional<Duration> duration) {
    final var player = source.orElseThrow();
    final var expireAt = duration.map(x -> Instant.now().plus(x)).orElse(null);
    final Grant grant;
    if (expireAt == null) {
      grant = lazyFactory.create(Grant.class,
          Map.of(Grant::id, UUID.randomUUID().toString(), Grant::issuedAt, Instant.now(),
              Grant::issuerId, player.getUniqueId().toString(), Grant::reason, reason,
              Grant::userId, target.getUniqueId().toString(), Grant::rankId, rank.id()));
    } else {
      grant = lazyFactory.create(Grant.class,
          Map.of(Grant::id, UUID.randomUUID().toString(), Grant::issuedAt, Instant.now(),
              Grant::issuerId, player.getUniqueId().toString(), Grant::reason, reason,
              Grant::userId, target.getUniqueId().toString(), Grant::rankId, rank.id(),
              Grant::expireAt, expireAt));
    }
    player.sendMessage(translatable("Updating...").color(NamedTextColor.GRAY));
    grantRepository.update(grant).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(
            translatable("Granted %s rank %s.", text(target.getName()), text(rank.id())));
      }
    }));
  }

  @Identified("grant revoke")
  @Permission("scofu.command.grant.revoke")
  private void revoke(Expansion<Player> source, String grantId, String reason) {
    final var player = source.orElseThrow();
    final var grant = grantRepository.byId(grantId).orElse(null);
    if (grant == null) {
      player.sendMessage(translatable("No grant with id %s.", text(grantId)));
      return;
    }
    if (grant.isRevoked()) {
      player.sendMessage(translatable("Grant has already been revoked."));
      return;
    }
    grant.revoke(player.getUniqueId().toString(), reason);
    player.sendMessage(translatable("Updating..."));
    grantRepository.update(grant).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Revoked grant."));
      }
    }));
  }

  @Identified("paginatedtest")
  private void paginatedTest(Expansion<Player> source) {
    final var player = source.orElseThrow();
    design.bind(player, new PaginatedWindow(null, null, design) {

      @Override
      protected Component title(String search, int page, int pages) {
        return text("Test");
      }

      @Override
      protected List<? extends ButtonBuilder> buttons(String search, int page) {
        return Stream.of(Material.values())
            .filter(material -> !material.isLegacy())
            .filter(material -> search.isEmpty() || search.isBlank() || material.name()
                .toLowerCase(Locale.ROOT)
                .contains(search.toLowerCase(Locale.ROOT)))
            .map(material -> Button.builder()
                .withItem(viewer(), builder -> builder.ofType(material)
                    .withName(text(material.name().toLowerCase(Locale.ROOT))))
                .onClick(event -> {
                  event.setCancelled(true);
                  viewer().player().closeInventory();
                  viewer().player().sendActionBar(text("yoooo"));
                }))
            .toList();
      }
    });
  }

  @Identified("permtest")
  private void permTest(Expansion<Player> source, String command) {
    final var player = source.orElseThrow();
    final var actualCommand = player.getServer().getCommandMap().getCommand(command);
    if (actualCommand == null) {
      player.sendMessage(text("No such command."));
      return;
    }
    if (actualCommand.getPermission() == null) {
      player.sendMessage(text("Command has no permission."));
      return;
    }
    player.sendMessage(text("Permission: " + actualCommand.getPermission()));
  }

  @Identified("bottest")
  private void bottest(Expansion<Player> source) {
    final var player = source.orElseThrow();
    final var husk = (Husk) player.getWorld().spawnEntity(player.getLocation(), EntityType.HUSK);
    //    husk.setInvisible(true);
    husk.setInvulnerable(true);
    husk.setAI(true);
    final var head = new ItemStack(Material.PLAYER_HEAD);
    head.editMeta(SkullMeta.class,
        meta -> meta.setOwningPlayer(Bukkit.getOfflinePlayer("everful")));
    husk.getEquipment().setHelmet(head);
    husk.setTarget(player);
    husk.getEquipment().setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));
    husk.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
  }

  @Identified("testsplit")
  private void testsplit(Expansion<Player> source, int targetWidth, int padding, String format,
      String thing) {
    final var player = source.orElseThrow();
    final var component = translatable(format, text(thing).color(NamedTextColor.DARK_PURPLE)).color(
        NamedTextColor.GRAY).compact();
    final var translated = GlobalTranslator.render(component, player.locale());
    player.sendMessage(space());
    wrap(translated, player.locale(), targetWidth, padding).forEach(player::sendMessage);
    player.sendMessage(space());
  }

}
