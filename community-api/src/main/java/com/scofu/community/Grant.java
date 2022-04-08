package com.scofu.community;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import com.scofu.network.document.Document;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * A grant.
 */
public class Grant implements Document {

  @JsonProperty("_id")
  private final String id;
  private final Instant issuedAt;
  private final String issuerId;
  private final String reason;
  private final String userId;
  private final String rankId;
  private final Instant expireAt;
  private Instant revokedAt;
  private String revokerId;
  private String revokeReason;

  /**
   * Constructs a new grant.
   *
   * @param id       the id
   * @param issuedAt the issued at
   * @param issuerId the issuer id
   * @param reason   the reason
   * @param userId   the user id
   * @param rankId   the rank id
   * @param expireAt the expire at
   */
  @JsonCreator
  public Grant(String id, Instant issuedAt, String issuerId, String reason, String userId,
      String rankId, @Nullable Instant expireAt) {
    this.id = id;
    this.issuedAt = issuedAt;
    this.issuerId = issuerId;
    this.reason = reason;
    this.userId = userId;
    this.rankId = rankId;
    this.expireAt = expireAt;
  }

  /**
   * Returns the optional duration left.
   */
  public Optional<Duration> durationLeft() {
    return expireAt().map(instant -> Duration.between(Instant.now(), instant));
  }

  /**
   * Returns whether this has expired or not.
   */
  public boolean hasExpired() {
    return durationLeft().filter(duration -> duration.isNegative() || duration.isZero())
        .isPresent();
  }

  /**
   * Returns whether this is permanent or not.
   */
  public boolean isPermanent() {
    return expireAt == null;
  }

  /**
   * Returns whether this has been revoked or not.
   */
  public boolean isRevoked() {
    return revokedAt != null;
  }

  /**
   * Returns whether this rank is active or not.
   */
  public boolean isActive() {
    return !isRevoked() && !hasExpired();
  }

  @Override
  public String id() {
    return id;
  }

  /**
   * Returns the issued at.
   */
  public Instant issuedAt() {
    return issuedAt;
  }

  /**
   * Returns the issuer id.
   */
  public String issuerId() {
    return issuerId;
  }

  /**
   * Returns the reason.
   */
  public String reason() {
    return reason;
  }

  /**
   * Returns the user id.
   */
  public String userId() {
    return userId;
  }

  /**
   * Returns the rank id.
   */
  public String rankId() {
    return rankId;
  }

  /**
   * Returns the optional expire at.
   */
  public Optional<Instant> expireAt() {
    return Optional.ofNullable(expireAt);
  }

  /**
   * Revokes this grant.
   *
   * @param revokerId    the revoker id
   * @param revokeReason the revoke reason
   */
  public void revoke(String revokerId, String revokeReason) {
    this.revokedAt = Instant.now();
    this.revokerId = revokerId;
    this.revokeReason = revokeReason;
  }

  /**
   * Returns the optional revoked at.
   */
  public Optional<Instant> revokedAt() {
    return Optional.ofNullable(revokedAt);
  }

  /**
   * Returns the optional revoker id.
   */
  public Optional<String> revokerId() {
    return Optional.ofNullable(revokerId);
  }

  /**
   * Returns the optional revoke reason.
   */
  public Optional<String> revokeReason() {
    return Optional.ofNullable(revokeReason);
  }
}
