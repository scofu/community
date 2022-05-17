package com.scofu.community.bukkit.design;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.scofu.design.bukkit.item.Button.button;
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
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

/** Grant list window. */
public final class GrantListWindow extends PaginatedWindow {

  private final List<Grant> grants;
  private final GrantRepository grantRepository;

  /**
   * Constructs a new grant list window.
   *
   * @param parent the parent
   * @param design the design
   * @param grants the grants
   * @param grantRepository the grant repository
   */
  public GrantListWindow(
      @Nullable Window parent, Design design, List<Grant> grants, GrantRepository grantRepository) {
    super(null, parent, design);
    checkNotNull(design, "design");
    checkNotNull(grants, "grants");
    checkNotNull(grantRepository, "grantRepository");
    this.grants = grants;
    this.grantRepository = grantRepository;
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Grants");
  }

  @Override
  protected List<? extends ButtonBuilder<Void>> buttons(String search, int page) {
    return grants.stream().map(this::createButton).toList();
  }

  private ButtonBuilder<Void> createButton(Grant grant) {
    return button()
        .item(viewer())
        .material(
            grant.isRevoked()
                ? Material.RED_BANNER
                : grant.hasExpired() ? Material.YELLOW_BANNER : Material.GREEN_BANNER)
        .name(text(grant.isRevoked() ? "Revoked" : grant.hasExpired() ? "Expired" : "Active"))
        .header(Time.formatDate(grant.issuedAt(), ZoneId.systemDefault()))
        .adopt(builder -> formatMeta(builder, grant))
        .endItem()
        .event(
            event -> {
              event.setCancelled(true);
              if (event.isShiftClick()) {
                design()
                    .request(viewer().player(), new ConfirmWindow(), text("Are you sure?"))
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
                      viewer().player(), new Chat(), info().text("Please enter reason:").prefixed())
                  .completeOnTimeout(null, 10, TimeUnit.MINUTES)
                  .thenAcceptAsync(
                      reason -> {
                        if (reason == null) {
                          error()
                              .text("Timed out.")
                              .prefixed()
                              .render(viewer().player()::sendMessage);
                          design().bind(viewer().player(), this);
                          return;
                        }
                        design()
                            .request(viewer().player(), new ConfirmWindow(), text("Are you sure?"))
                            .completeOnTimeout(false, 10, TimeUnit.MINUTES)
                            .thenAcceptAsync(
                                confirm -> {
                                  if (confirm) {
                                    grant.revoke(
                                        viewer().player().getUniqueId().toString(), reason);
                                    grantRepository.update(grant);
                                  }
                                  design().bind(viewer().player(), this);
                                });
                      });
            });
  }

  private ItemBuilder<ButtonBuilder<Void>> formatMeta(
      ItemBuilder<ButtonBuilder<Void>> builder, Grant grant) {
    builder = builder.tag(text("Issued:"));
    builder = builder.tag(translatable(" By: %s", text(grant.issuerId())));
    builder = builder.tag(translatable(" Reason: %s", text(grant.reason())));

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

    builder = builder.tag(translatable(" Expiration: %s", expiration));
    builder = builder.tag(translatable(" Rank: %s", text(grant.rankId())));

    builder = builder.tag(space());
    builder = builder.tag(text("Status:"));

    if (grant.isActive()) {
      builder = builder.tag(translatable(" [Active]"));
    }

    if (grant.hasExpired()) {
      builder = builder.tag(translatable(" [Expired]"));
    }

    if (grant.isRevoked()) {
      builder = builder.tag(translatable(" [Revoked]"));
      builder = builder.tag(translatable("  By: %s", text(grant.revokerId().orElse("N/A"))));
      builder = builder.tag(translatable("  Reason: %s", text(grant.revokeReason().orElse("N/A"))));
      builder =
          builder.tag(
              translatable(
                  "  At: %s",
                  grant
                      .revokedAt()
                      .map(instant -> Time.formatDate(instant, ZoneId.systemDefault()))
                      .orElse(text("N/A"))));
    }

    if (!grant.isRevoked()) {
      builder = builder.footer(text("Click to revoke!"));
    }

    builder = builder.footer(text("Shift+Click to delete!"));

    return builder;
  }
}
