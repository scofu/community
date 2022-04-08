package com.scofu.community;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import com.scofu.common.json.PeriodEscapedString;
import com.scofu.network.document.Document;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

/**
 * A rank.
 */
public class Rank implements Document, Comparable<Rank> {

  private static final Rank EMPTY = new Rank(null);

  @JsonProperty("_id")
  private final String id;
  private final Map<PeriodEscapedString, Boolean> permissions;
  private final Set<String> inheritance;
  private int priority;
  private Component prefix;
  private TextColor nameColor;

  /**
   * Constructs a new rank.
   *
   * @param id the id
   */
  @JsonCreator
  public Rank(String id) {
    this.id = id;
    this.permissions = Maps.newHashMap();
    this.inheritance = Sets.newHashSet();
  }

  /**
   * Returns the empty rank.
   */
  public static Rank empty() {
    return EMPTY;
  }

  @Override
  public String id() {
    return id;
  }

  /**
   * Returns the priority.
   */
  public int priority() {
    return priority;
  }

  /**
   * Sets the priority.
   *
   * @param priority the priority
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Returns the optional prefix.
   */
  public Optional<Component> prefix() {
    return Optional.ofNullable(prefix);
  }

  /**
   * Sets the prefix.
   *
   * @param prefix the prefix
   */
  public void setPrefix(Component prefix) {
    this.prefix = prefix;
  }

  /**
   * Returns the optional name color.
   */
  public Optional<TextColor> nameColor() {
    return Optional.ofNullable(nameColor);
  }

  /**
   * Sets the name color.
   *
   * @param nameColor the name color
   */
  public void setNameColor(TextColor nameColor) {
    this.nameColor = nameColor;
  }

  /**
   * Returns the permissions.
   */
  public Map<PeriodEscapedString, Boolean> permissions() {
    return permissions;
  }

  /**
   * Resolves and returns whether this rank has the given permission or not.
   *
   * @param key the key
   */
  public Optional<Boolean> resolvePermission(String key) {
    return Optional.ofNullable(permissions.get(new PeriodEscapedString(key)));
  }

  /**
   * Returns the inheritance.
   */
  public Set<String> inheritance() {
    return inheritance;
  }

  @Override
  public int compareTo(@NotNull Rank o) {
    return Integer.compare(o.priority, priority);
  }
}
