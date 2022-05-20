package com.scofu.community.bukkit;

import static com.scofu.design.bukkit.item.Button.button;
import static com.scofu.text.Components.wrap;
import static com.scofu.text.ContextualizedComponent.error;
import static com.scofu.text.ContextualizedComponent.info;
import static com.scofu.text.ContextualizedComponent.success;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.common.collect.Lists;
import com.scofu.command.model.Identified;
import com.scofu.command.validation.Permission;
import com.scofu.common.Expansion;
import com.scofu.common.inject.Feature;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.Grant;
import com.scofu.community.GrantRepository;
import com.scofu.community.Rank;
import com.scofu.community.bukkit.design.GrantListWindow;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.text.Color;
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

  @Identified("grants")
  @Permission("scofu.command.grants")
  private void grants(Expansion<Player> source, Player target) {
    final var player = source.orElseThrow();
    final var collector = Collectors.<Grant, List<Grant>>toCollection(Lists::newArrayList);
    info().text("Loading grants...").prefixed().render(player::sendMessage);
    grantRepository
        .allByUserId(target.getUniqueId().toString())
        .apply(Stream::sorted, () -> Comparator.comparing(Grant::issuedAt))
        .apply(Stream::collect, () -> collector)
        .map(grants -> new GrantListWindow(null, design, grants, grantRepository))
        .accept(window -> design.bind(player, window));
  }

  @Identified("grant")
  @Permission("scofu.command.grant")
  private void grant(
      Expansion<Player> source,
      Player target,
      Rank rank,
      String reason,
      Optional<Duration> duration) {
    final var player = source.orElseThrow();
    final var expireAt = duration.map(x -> Instant.now().plus(x)).orElse(null);
    final Grant grant;
    if (expireAt == null) {
      grant =
          lazyFactory.create(
              Grant.class,
              Map.of(
                  Grant::id,
                  UUID.randomUUID().toString(),
                  Grant::issuedAt,
                  Instant.now(),
                  Grant::issuerId,
                  player.getUniqueId().toString(),
                  Grant::reason,
                  reason,
                  Grant::userId,
                  target.getUniqueId().toString(),
                  Grant::rankId,
                  rank.id()));
    } else {
      grant =
          lazyFactory.create(
              Grant.class,
              Map.of(
                  Grant::id,
                  UUID.randomUUID().toString(),
                  Grant::issuedAt,
                  Instant.now(),
                  Grant::issuerId,
                  player.getUniqueId().toString(),
                  Grant::reason,
                  reason,
                  Grant::userId,
                  target.getUniqueId().toString(),
                  Grant::rankId,
                  rank.id(),
                  Grant::expireAt,
                  expireAt));
    }
    info().text("Updating...").prefixed().render(player::sendMessage);
    grantRepository
        .update(grant)
        .map(
            unused ->
                success().text("Granted %s rank %s.", target.getName(), rank).prefixed().render())
        .accept(player::sendMessage);
  }

  @Identified("grant revoke")
  @Permission("scofu.command.grant.revoke")
  private void revoke(Expansion<Player> source, String grantId, String reason) {
    final var player = source.orElseThrow();
    final var grant = grantRepository.byId(grantId).orElse(null);
    if (grant == null) {
      error().text("No grant with id %s.", grantId).prefixed().render(player::sendMessage);
      return;
    }
    if (grant.isRevoked()) {
      error().text("Grant has already been revoked.").prefixed().render(player::sendMessage);
      return;
    }
    grant.revoke(player.getUniqueId().toString(), reason);
    info().text("Updating...").prefixed().render(player::sendMessage);
    grantRepository
        .update(grant)
        .map(unused -> success().text("Revoked grant.").prefixed().render())
        .accept(player::sendMessage);
  }

  @Identified("paginatedtest")
  private void paginatedTest(Expansion<Player> source) {
    final var player = source.orElseThrow();
    design.bind(
        player,
        new PaginatedWindow(null, null, design) {

          @Override
          protected Component title(String search, int page, int pages) {
            return text("Materials");
          }

          @Override
          protected List<? extends ButtonBuilder<Void>> buttons(String search, int page) {
            return Stream.of(Material.values())
                .filter(material -> !material.isLegacy())
                .filter(
                    material ->
                        search.isEmpty()
                            || search.isBlank()
                            || material
                                .name()
                                .toLowerCase(Locale.ROOT)
                                .contains(search.toLowerCase(Locale.ROOT)))
                .map(
                    material ->
                        button()
                            .item(viewer())
                            .material(material)
                            .name(translatable(material.translationKey()))
                            .endItem()
                            .event(event -> event.setCancelled(true)))
                .toList();
          }
        });
  }

  @Identified("permtest")
  private void permTest(Expansion<Player> source, String command) {
    final var player = source.orElseThrow();
    final var actualCommand = player.getServer().getCommandMap().getCommand(command);
    if (actualCommand == null) {
      error().text("%s is not a command.", command).prefixed().render(player::sendMessage);
      return;
    }
    if (actualCommand.getPermission() == null) {
      error().text("That command has no permission.").prefixed().render(player::sendMessage);
      return;
    }
    success()
        .text("Permission: %s", actualCommand.getPermission())
        .prefixed()
        .render(player::sendMessage);
  }

  @Identified("bottest")
  private void bottest(Expansion<Player> source) {
    final var player = source.orElseThrow();
    final var husk = (Husk) player.getWorld().spawnEntity(player.getLocation(), EntityType.HUSK);
    //    husk.setInvisible(true);
    husk.setInvulnerable(true);
    husk.setAI(true);
    final var head = new ItemStack(Material.PLAYER_HEAD);
    head.editMeta(
        SkullMeta.class, meta -> meta.setOwningPlayer(Bukkit.getOfflinePlayer("everful")));
    husk.getEquipment().setHelmet(head);
    husk.setTarget(player);
    husk.getEquipment().setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));
    husk.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
  }

  @Identified("testsplit")
  private void testsplit(
      Expansion<Player> source, int targetWidth, int padding, String format, String thing) {
    final var player = source.orElseThrow();
    final var component =
        translatable(format, text(thing).color(Color.PURPLE)).color(Color.WHITE).compact();
    final var translated = GlobalTranslator.render(component, player.locale());
    player.sendMessage(space());
    wrap(translated, player.locale(), targetWidth, padding).forEach(player::sendMessage);
    player.sendMessage(space());
  }
}
