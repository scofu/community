package com.scofu.community;

import com.scofu.common.json.lazy.Lazy;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** A session. */
public interface Session extends Lazy {

  default Duration playtime() {
    return Duration.between(startedAt(), stoppedAt().orElseGet(Instant::now));
  }

  default boolean isActive() {
    return stoppedAt().isEmpty();
  }

  Instant startedAt();

  Optional<Instant> stoppedAt();

  void setStoppedAt(Instant stoppedAt);

  Optional<String> networkId();

  void setNetworkId(String networkId);

  Optional<String> instanceId();

  void setInstanceId(String instanceId);

  Optional<Grant> grant();

  void setGrant(Grant grant);
}
