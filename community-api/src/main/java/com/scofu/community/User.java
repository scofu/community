package com.scofu.community;

import com.scofu.common.json.lazy.Lazy;
import com.scofu.network.document.Document;
import java.time.Instant;
import java.util.Optional;

/** A user. */
public interface User extends Lazy, Document {

  default boolean isOnline() {
    return session().map(Session::isActive).isPresent();
  }

  Instant firstJoinAt();

  Optional<Session> session();

  void setSession(Session session);

  boolean hasSession();

  Optional<Time> time();

  void setTime(Time time);

  Optional<String> theme();

  void setTheme(String theme);
}
