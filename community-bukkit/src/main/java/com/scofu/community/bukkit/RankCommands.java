package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.command.model.Expansion;
import com.scofu.command.model.Identified;
import com.scofu.command.target.Escapable;
import com.scofu.command.validation.Permission;
import com.scofu.common.inject.Feature;
import com.scofu.common.json.Json;
import com.scofu.common.json.PeriodEscapedString;
import com.scofu.common.json.lazy.LazyFactory;
import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.community.UserRepository;
import com.scofu.community.bukkit.permission.WildcardPermissionChecker;
import com.scofu.design.bukkit.Design;
import com.scofu.network.document.Query;
import com.scofu.text.ThemeRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

final class RankCommands implements Feature {

  private final Design design;
  private final LazyFactory lazyFactory;
  private final ThemeRegistry themeRegistry;
  private final RankRepository rankRepository;
  private final UserRepository userRepository;
  private final WildcardPermissionChecker wildcardPermissionChecker;
  private final Json json;

  @Inject
  RankCommands(Design design, LazyFactory lazyFactory, ThemeRegistry themeRegistry,
      RankRepository rankRepository, UserRepository userRepository,
      WildcardPermissionChecker wildcardPermissionChecker, Json json) {
    this.design = design;
    this.lazyFactory = lazyFactory;
    this.themeRegistry = themeRegistry;
    this.rankRepository = rankRepository;
    this.userRepository = userRepository;
    this.wildcardPermissionChecker = wildcardPermissionChecker;
    this.json = json;
  }

  @Identified("ranks")
  @Permission("scofu.command.ranks")
  private void ranks(Expansion<Player> source) {
    final var player = source.orElseThrow();
    design.bind(player, new RankWindow(design, rankRepository, lazyFactory));
  }

  @Identified("deleteallranks")
  @Permission("scofu.command.deleteallranks")
  private void ranks(Expansion<Player> source, boolean confirm) {
    final var player = source.orElseThrow();
    final var theme = themeRegistry.byIdentified(player);
    if (!confirm) {
      player.sendMessage(text("Please confirm!").color(theme.brightRed()));
      return;
    }
    rankRepository.find(Query.empty())
        .thenCompose(ranks -> CompletableFuture.allOf(
            ranks.keySet().stream().map(rankRepository::delete).toArray(CompletableFuture[]::new)))
        .thenAccept(unused -> player.sendMessage(
            text("You've deleted all ranks!").color(theme.brightPurple())));
  }

  @Identified(value = "rank", futile = true)
  @Permission("scofu.command.rank")
  private void rank() {
  }

  @Identified("rank create")
  @Permission("scofu.command.rank.create")
  private void create(Expansion<Player> source, String id) {
    final var player = source.orElseThrow();
    //    if (rankRepository.byId(id).isPresent()) {
    //      player.sendMessage(translatable("A rank with id %s already exists.", text(id)));
    //      return;
    //    }
    //    player.sendMessage(translatable("Updating..."));
    //    rankRepository.update(new Rank(id)).whenComplete(((x, throwable) -> {
    //      if (throwable != null) {
    //        player.sendMessage(translatable("Error, something went wrong."));
    //      } else {
    //        player.sendMessage(translatable("Rank created!"));
    //      }
    //    }));
  }

  @Identified(value = "rank inherit", futile = true)
  @Permission("scofu.command.rank.inherit")
  private void inherit() {
  }

  @Identified("rank inherit add")
  @Permission("scofu.command.rank.inherit.add")
  private void inheritadd(Expansion<Player> source, Rank rank, Rank rankToInherit) {
    final var player = source.orElseThrow();

  }

  @Identified("rank inherit remove")
  @Permission("scofu.command.rank.inherit.remove")
  private void inheritremove(Expansion<Player> source, Rank rank, Rank rankToInherit) {
    final var player = source.orElseThrow();
    //    rank.inheritance().remove(rankToInherit.id());
    //    player.sendMessage(translatable("Updating..."));
    //    rankRepository.update(rank).whenComplete(((x, throwable) -> {
    //      if (throwable != null) {
    //        player.sendMessage(translatable("Error, something went wrong."));
    //      } else {
    //        player.sendMessage(translatable("%s no longer inherits from %s.", text(rank.id()),
    //            text(rankToInherit.id())));
    //      }
    //    }));
  }

  @Identified("rank inherit list")
  @Permission("scofu.command.rank.inherit.list")
  private void inheritlist(Expansion<Player> source, Rank rank) {
    final var player = source.orElseThrow();
    //    final var builder = text();
    //    builder.append(text(
    //            "Rank " + rank.id() + " inherits from " + rank.inheritance().size() + " rank" + (
    //                rank.inheritance().size() == 1 ? "" : "s")).color(NamedTextColor.GRAY))
    //        .append(newline());
    //    rank.inheritance()
    //        .stream()
    //        .map(rankRepository::byId)
    //        .filter(Optional::isPresent)
    //        .map(Optional::get)
    //        .sorted()
    //        .forEach(inheritedRank -> builder.append(
    //            text("(" + inheritedRank.priority() + ") " + inheritedRank.id()).color(
    //                NamedTextColor.GRAY)).append(newline()));
    //    player.sendMessage(builder.build());
  }

  @Identified(value = "rank perm", futile = true)
  @Permission("scofu.command.rank.perm")
  private void perm() {
  }

  @Identified("rank perm set")
  @Permission("scofu.command.rank.perm.set")
  private void permset(Expansion<Player> source, Rank rank, String permission, boolean value) {
    final var player = source.orElseThrow();
    rank.addPermission(new PeriodEscapedString(permission), value);
    player.sendMessage(translatable("Updating..."));
    rankRepository.update(rank).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Set permission %s to %s.", text(permission), text(value)));
      }
    }));
  }

  @Identified("rank perm remove")
  @Permission("scofu.command.rank.perm.remove")
  private void permremove(Expansion<Player> source, Rank rank, String permission) {
    final var player = source.orElseThrow();
    rank.removePermission(new PeriodEscapedString(permission));
    player.sendMessage(translatable("Updating..."));
    rankRepository.update(rank).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Removed permission %s.", text(permission)));
      }
    }));
  }

  @Identified("rank perm list")
  @Permission("scofu.command.rank.perm.list")
  private void permremove(Expansion<Player> source, Rank rank, boolean inheritance) {
    final var player = source.orElseThrow();
    var builder = text();
    formatPermissionList(builder, rank, inheritance);
    player.sendMessage(builder.build());
  }

  @Identified("rank perm check")
  @Permission("scofu.command.rank.perm.check")
  private void permCheck(Expansion<Player> source, Rank rank, String permission) {
    final var player = source.orElseThrow();
    wildcardPermissionChecker.check(rank, permission)
        .ifPresentOrElse(
            result -> player.sendMessage(translatable(permission + ", resolved: " + result)),
            () -> player.sendMessage(translatable(permission + ", unresolved")));
  }

  private void formatPermissionList(TextComponent.Builder builder, Rank rank, boolean inheritance) {
    rank.permissions()
        .orElse(Map.of())
        .forEach((key, value) -> builder.append(text(key + ": ").color(NamedTextColor.GRAY))
            .append(text(value).color(value ? NamedTextColor.GREEN : NamedTextColor.RED))
            .append(newline()));
    if (!inheritance) {
      return;
    }
    rank.inheritance()
        .orElse(Set.of())
        .stream()
        .map(rankRepository::byId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .sorted()
        .forEach(inheritedRank -> {
          builder.append(text("From " + inheritedRank.id() + ":").color(NamedTextColor.GRAY))
              .append(newline());
          formatPermissionList(builder, inheritedRank, true);
        });

  }

  @Identified(value = "rank set", futile = true)
  @Permission("scofu.command.rank.set")
  private void set() {
  }

  @Identified("rank set prefix")
  @Permission("scofu.command.rank.set.prefix")
  private void prefix(Expansion<Player> source, Rank rank, @Escapable String prefix) {
    final var player = source.orElseThrow();
    //    final var component = Optional.ofNullable(json.fromString(Component.class, prefix))
    //        .orElseGet(() -> text(prefix));
    //    rank.setPrefix(component);
    //    player.sendMessage(translatable("Updating..."));
    //    rankRepository.update(rank).whenComplete(((x, throwable) -> {
    //      if (throwable != null) {
    //        player.sendMessage(translatable("Error, something went wrong."));
    //      } else {
    //        player.sendMessage(translatable("Prefix set to %s!", component));
    //      }
    //    }));
  }

  @Identified("rank set tagprefix")
  @Permission("scofu.command.rank.set.tagprefix")
  private void tagprefix(Expansion<Player> source, Rank rank, @Escapable String tag) {
    final var player = source.orElseThrow();
    //    final var component = Tags.parseTag(tag, false, 2).append(space());
    //    rank.setPrefix(component);
    //    player.sendMessage(translatable("Updating..."));
    //    rankRepository.update(rank).whenComplete(((x, throwable) -> {
    //      if (throwable != null) {
    //        player.sendMessage(translatable("Error, something went wrong."));
    //      } else {
    //        player.sendMessage(translatable("Prefix set to %s!", component));
    //      }
    //    }));
  }

  @Identified("rank set priority")
  @Permission("scofu.command.rank.set.priority")
  private void priority(Expansion<Player> source, Rank rank, int priority) {
    final var player = source.orElseThrow();
    rank.setPriority(priority);
    player.sendMessage(translatable("Updating..."));
    rankRepository.update(rank).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Priority set to %s!", text(priority)));
      }
    }));
  }

  @Identified("rank set namecolor")
  @Permission("scofu.command.rank.set.namecolor")
  private void namecolor(Expansion<Player> source, Rank rank, @Escapable String namecolor) {
    final var player = source.orElseThrow();

  }

  @Identified("setuserchatcolor")
  @Permission("scofu.command.setuserchatcolor")
  private void setuserchatcolor(Expansion<Player> source, Player target,
      @Escapable String chatColor) {
    final var player = source.orElseThrow();
    final var user = userRepository.byId(target.getUniqueId().toString()).orElse(null);
    if (user == null) {
      player.sendMessage(translatable("Target player is not a user."));
      return;
    }
    final var color = json.fromString(TextColor.class, "\"" + chatColor + "\"");
    if (color == null) {
      player.sendMessage(translatable("%s is not a valid hex or named color.", text(chatColor)));
      return;
    }
    user.setChatColor(color);
    player.sendMessage(translatable("Updating..."));
    userRepository.update(user).whenComplete(((x, throwable) -> {
      if (throwable != null) {
        player.sendMessage(translatable("Error, something went wrong."));
      } else {
        player.sendMessage(translatable("Chat color of user %s set to %s!", text(target.getName()),
            text(chatColor).color(color)));
      }
    }));
  }

}
