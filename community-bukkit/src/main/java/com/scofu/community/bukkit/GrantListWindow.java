package com.scofu.community.bukkit;

import static com.scofu.design.bukkit.item.Button.button;
import static com.scofu.design.bukkit.item.ItemTextComponent.itemText;
import static com.scofu.text.ContextualizedComponent.error;
import static com.scofu.text.ContextualizedComponent.info;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.community.Grant;
import com.scofu.community.GrantRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.chat.Chat;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.item.ItemBuilder;
import com.scofu.design.bukkit.window.ConfirmWindow;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.design.bukkit.window.Window;
import com.scofu.text.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

final class GrantListWindow extends PaginatedWindow {

  private final List<Grant> grants;
  private final GrantRepository grantRepository;

  public GrantListWindow(
      Window parent, Design design, List<Grant> grants, GrantRepository grantRepository) {
    super(null, parent, design);
    this.grants = grants;
    this.grantRepository = grantRepository;
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Grants");
  }

  @Override
  protected List<? extends ButtonBuilder> buttons(String search, int page) {
    return grants.stream()
        .map(
            grant ->
                button()
                    .withStaticItem(
                        viewer(),
                        builder ->
                            builder
                                .ofType(
                                    grant.isRevoked()
                                        ? Material.RED_BANNER
                                        : grant.hasExpired()
                                            ? Material.YELLOW_BANNER
                                            : Material.GREEN_BANNER)
                                .withName(
                                    itemText(
                                            Time.formatDate(
                                                grant.issuedAt(), ZoneId.systemDefault()))
                                        .color(
                                            grant.isRevoked()
                                                ? viewer().theme().brightRed()
                                                : grant.hasExpired()
                                                    ? viewer().theme().brightYellow()
                                                    : viewer().theme().brightGreen()))
                                .adopt(childBuilder -> formatMeta(grant, childBuilder)))
                    .onClick(
                        event -> {
                          event.setCancelled(true);
                          if (event.isShiftClick()) {
                            design()
                                .request(
                                    viewer().player(), new ConfirmWindow(), text("Are you sure?"))
                                .completeOnTimeout(false, 10, TimeUnit.MINUTES)
                                .thenAcceptAsync(
                                    result -> {
                                      if (result) {
                                        grantRepository.delete(grant.id());
                                        grants.remove(grant);
                                      }
                                      design().bind(viewer().player(), this);
                                    });
                            return;
                          }
                          if (grant.isRevoked()) {
                            return;
                          }
                          design()
                              .request(
                                  viewer().player(),
                                  new Chat(),
                                  info().text("Please enter reason:").prefixed())
                              .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                              .thenAcceptAsync(
                                  reason -> {
                                    if (reason == null) {
                                      error()
                                          .text("Timed out.")
                                          .prefixed()
                                          .renderTo(
                                              viewer().theme(), viewer().player()::sendMessage);
                                      design().bind(viewer().player(), this);
                                      return;
                                    }
                                    design()
                                        .request(
                                            viewer().player(),
                                            new ConfirmWindow(),
                                            text("Are you sure?"))
                                        .completeOnTimeout(false, 10, TimeUnit.MINUTES)
                                        .thenAcceptAsync(
                                            confirm -> {
                                              if (confirm) {
                                                grant.revoke(
                                                    viewer().player().getUniqueId().toString(),
                                                    reason);
                                                grantRepository.update(grant);
                                              }
                                              design().bind(viewer().player(), this);
                                            });
                                  });
                        }))
        .toList();
  }

  private ItemBuilder formatMeta(Grant grant, ItemBuilder builder) {
    builder = builder.withTag(text("Issued:"));
    builder = builder.withTag(translatable(" By: %s", text(grant.issuerId())));
    builder = builder.withTag(translatable(" Reason: %s", text(grant.reason())));

    final var expiration =
        grant
            .expireAt()
            .map(
                instant ->
                    Time.formatDate(instant, ZoneId.systemDefault())
                        .append(text(" ("))
                        .append(
                            grant.hasExpired()
                                ? Time.asPast(Duration.between(instant, Instant.now()))
                                : Time.asFuture(grant.durationLeft().orElse(Duration.ZERO)))
                        .append(text(")")))
            .orElse(text("Permanent"));

    builder = builder.withTag(translatable(" Expiration: %s", expiration));
    builder = builder.withTag(translatable(" Rank: %s", text(grant.rankId())));

    builder = builder.withTag(space());
    builder = builder.withTag(text("Status:"));

    if (grant.isActive()) {
      builder = builder.withTag(translatable(" [Active]"));
    }

    if (grant.hasExpired()) {
      builder = builder.withTag(translatable(" [Expired]"));
    }

    if (grant.isRevoked()) {
      builder = builder.withTag(translatable(" [Revoked]"));
      builder = builder.withTag(translatable("  By: %s", text(grant.revokerId().orElse("N/A"))));
      builder =
          builder.withTag(translatable("  Reason: %s", text(grant.revokeReason().orElse("N/A"))));
      builder =
          builder.withTag(
              translatable(
                  "  At: %s",
                  grant
                      .revokedAt()
                      .map(instant -> Time.formatDate(instant, ZoneId.systemDefault()))
                      .orElse(text("N/A"))));
    }

    if (!grant.isRevoked()) {
      builder = builder.withFooter(text("Click to revoke!"));
    }

    builder = builder.withFooter(text("Shift+Click to delete!"));

    return builder;
  }
}
