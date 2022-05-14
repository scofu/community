package com.scofu.community;

import com.scofu.common.json.lazy.Lazy;
import com.scofu.network.document.Document;
import java.time.Instant;
import java.util.Optional;

/** Generic stats. */
public interface GenericStats extends Lazy, Document {

  int coins();

  int incrementCoins();

  int decrementCoins();

  void setCoins(int coins);

  Optional<Instant> lastLoginAt();

  void setLastLoginAt(Instant instant);
}
