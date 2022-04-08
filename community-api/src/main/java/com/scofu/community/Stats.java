package com.scofu.community;

import static com.scofu.common.json.KnownReference.known;

import com.google.common.collect.Maps;
import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import com.scofu.common.json.KnownReference;
import com.scofu.common.json.Periods;
import com.scofu.network.document.Document;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Stats.
 */
public class Stats implements Document {

  @JsonProperty("_id")
  private final String id;
  private final Map<String, KnownReference<?>> references;

  /**
   * Constructs a new stats.
   *
   * @param id the id
   */
  @JsonCreator
  public Stats(String id) {
    this.id = id;
    this.references = Maps.newConcurrentMap();
  }

  /**
   * Creates and returns an id for the given player id and category.
   *
   * @param playerId the player id
   * @param category the category
   */
  public static String id(String playerId, String category) {
    return playerId + "_" + category;
  }

  @Override
  public String id() {
    return id;
  }

  /**
   * Creates and returns a field for querying.
   *
   * @param key  the key
   * @param type the type
   * @param <T>  the type
   */
  public static <T> String field(String key, Class<T> type) {
    return "references." + key + "." + Periods.escape(type.getName());
  }

  /**
   * Returns the player id.
   */
  public String playerId() {
    return id.split("_", 2)[0];
  }

  /**
   * Returns the category.
   */
  public String category() {
    return id.split("_", 2)[1];
  }

  /**
   * Returns the references.
   */
  public Map<String, KnownReference<?>> references() {
    return references;
  }

  /**
   * Returns whether a statistic with the given key exists or not.
   *
   * @param key the key
   */
  public boolean has(String key) {
    return references.containsKey(key);
  }

  /**
   * Returns an optional statistic with the given key and type.
   *
   * @param key  the key
   * @param type the type
   * @param <T>  the type of the statistic
   */
  public <T> Optional<T> get(String key, Class<T> type) {
    return Optional.ofNullable(references.get(key)).map(KnownReference::value).map(type::cast);
  }

  /**
   * Returns an optional int.
   *
   * @param key the key
   */
  public OptionalInt getInt(String key) {
    final var reference = references.get(key);
    return reference == null ? OptionalInt.empty() : OptionalInt.of((int) reference.value());
  }

  /**
   * Returns an optional long.
   *
   * @param key the key
   */
  public OptionalLong getLong(String key) {
    final var reference = references.get(key);
    return reference == null ? OptionalLong.empty() : OptionalLong.of((long) reference.value());
  }

  /**
   * Returns an optional double.
   *
   * @param key the key
   */
  public OptionalDouble getDouble(String key) {
    final var reference = references.get(key);
    return reference == null ? OptionalDouble.empty()
        : OptionalDouble.of((double) reference.value());
  }

  /**
   * Sets a statistic with the given key and type to the given value.
   *
   * @param key   the key
   * @param value the value
   * @param type  the type
   * @param <T>   the type of the statistic
   */
  public <T> void set(String key, T value, Class<T> type) {
    references.put(key, known(type, value));
  }

  /**
   * Sets an int.
   *
   * @param key the key
   */
  public void setInt(String key, int i) {
    references.put(key, known(Integer.class, i));
  }

  /**
   * Sets a long.
   *
   * @param key the key
   */
  public void setLong(String key, long l) {
    references.put(key, known(Long.class, l));
  }

  /**
   * Sets a double.
   *
   * @param key the key
   */
  public void setDouble(String key, double d) {
    references.put(key, known(Double.class, d));
  }

  /**
   * Increments an int.
   *
   * @param key the key
   */
  public void incrementInt(String key) {
    setInt(key, getInt(key).orElse(0) + 1);
  }

  /**
   * Increments a long.
   *
   * @param key the key
   */
  public void incrementLong(String key) {
    setLong(key, getLong(key).orElse(0L) + 1);
  }

  /**
   * Increments a double.
   *
   * @param key the key
   */
  public void incrementDouble(String key) {
    setDouble(key, getDouble(key).orElse(0D) + 1);
  }

  /**
   * Decrements an int.
   *
   * @param key the key
   */
  public void decrementInt(String key) {
    setInt(key, getInt(key).orElse(0) - 1);
  }

  /**
   * Decrements a long.
   *
   * @param key the key
   */
  public void decrementLong(String key) {
    setLong(key, getLong(key).orElse(0L) - 1);
  }

  /**
   * Decrements a double.
   *
   * @param key the key
   */
  public void decrementDouble(String key) {
    setDouble(key, getDouble(key).orElse(0D) - 1);
  }
}
