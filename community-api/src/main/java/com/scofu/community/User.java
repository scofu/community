package com.scofu.community;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import com.scofu.network.document.Document;
import java.time.Instant;
import java.util.Optional;
import net.kyori.adventure.text.format.TextColor;

/**
 * A user.
 */
public class User implements Document {

  @JsonProperty("_id")
  private final String id;
  private final Instant firstJoinAt;
  private Session session;
  private TextColor chatColor;

  @JsonCreator
  public User(String id, Instant firstJoinAt) {
    this.id = id;
    this.firstJoinAt = firstJoinAt;
  }

  @Override
  public String id() {
    return id;
  }

  public boolean isOnline() {
    return session != null && session.isActive();
  }

  public Instant firstJoinAt() {
    return firstJoinAt;
  }

  public Optional<Session> session() {
    return Optional.ofNullable(session);
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public boolean hasSession() {
    return session != null;
  }

  public Optional<TextColor> chatColor() {
    return Optional.ofNullable(chatColor);
  }

  public void setChatColor(TextColor chatColor) {
    this.chatColor = chatColor;
  }
}
