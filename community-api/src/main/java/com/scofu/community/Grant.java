package com.scofu.community;

import com.scofu.common.json.lazy.Lazy;
import com.scofu.network.document.Document;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * A grant.
 */
public interface Grant extends Lazy, Document {

  default Optional<Duration> durationLeft() {
    return expireAt().map(instant -> Duration.between(Instant.now(), instant));
  }

  default boolean hasExpired() {
    return durationLeft().filter(duration -> duration.isNegative() || duration.isZero())
        .isPresent();
  }

  default boolean isPermanent() {
    return expireAt().isEmpty();
  }

  default boolean isRevoked() {
    return revokedAt().isPresent();
  }

  default boolean isActive() {
    return !isRevoked() && !hasExpired();
  }

  Instant issuedAt();

  String issuerId();

  String reason();

  String userId();

  String rankId();

  Optional<Instant> expireAt();

  /**
   * Revokes this grant.
   *
   * @param revokerId    the revoker id
   * @param revokeReason the revoke reason
   */
  default void revoke(String revokerId, String revokeReason) {
    setRevokedAt(Instant.now());
    setRevokerId(revokerId);
    setRevokeReason(revokeReason);
  }

  Optional<Instant> revokedAt();

  void setRevokedAt(Instant revokedAt);

  Optional<String> revokerId();

  void setRevokerId(String revokerId);

  Optional<String> revokeReason();

  void setRevokeReason(String revokeReason);
}
