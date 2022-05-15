package com.scofu.community;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.scofu.common.json.PeriodEscapedString;
import com.scofu.common.json.lazy.Lazy;
import com.scofu.network.document.Document;
import com.scofu.text.Color;
import com.scofu.text.Renderable;
import com.scofu.text.json.Tag;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/** A rank. */
public interface Rank extends Lazy, Document, Renderable, Comparable<Rank> {

  static Rank empty() {
    return EmptyRank.INSTANCE;
  }

  String name();

  void setName(String name);

  Optional<Tag> tag();

  void setTag(Tag tag);

  int priority();

  int incrementPriority();

  int decrementPriority();

  void setPriority(int priority);

  Optional<Color> nameColor();

  void setNameColor(Color nameColor);

  Optional<Map<PeriodEscapedString, Boolean>> permissions();

  void setPermissions(Map<PeriodEscapedString, Boolean> permissions);

  /**
   * See {@link Map#put(Object, Object)}.
   *
   * @param string the string
   * @param value the value
   */
  default Boolean addPermission(PeriodEscapedString string, Boolean value) {
    var permissions = permissions().orElseGet(Maps::newConcurrentMap);
    final var previous = permissions.put(string, value);
    setPermissions(permissions);
    return previous;
  }

  /**
   * See {@link Map#remove(Object)}.
   *
   * @param string the string
   */
  default Boolean removePermission(PeriodEscapedString string) {
    final var permissions = permissions().orElse(null);
    if (permissions == null) {
      return null;
    }
    final var previous = permissions.remove(string);
    setPermissions(permissions);
    return previous;
  }

  Optional<Set<String>> inheritance();

  void setInheritance(Set<String> inheritance);

  /**
   * See {@link Set#add(Object)}.
   *
   * @param string the string
   */
  default boolean addInheritance(String string) {
    var inheritance = inheritance().orElseGet(Sets::newConcurrentHashSet);
    final var modified = inheritance.add(string);
    if (modified) {
      setInheritance(inheritance);
    }
    return modified;
  }

  /**
   * See {@link Set#remove(Object)}.
   *
   * @param string the string
   */
  default boolean removeInheritance(String string) {
    final var inheritance = inheritance().orElse(null);
    if (inheritance == null) {
      return false;
    }
    final var modified = inheritance.remove(string);
    if (modified) {
      setInheritance(inheritance);
    }
    return modified;
  }

  /**
   * Resolves and returns whether this rank has the given permission or not.
   *
   * @param key the key
   */
  default Optional<Boolean> resolvePermission(String key) {
    return permissions().map(permissions -> permissions.get(new PeriodEscapedString(key)));
  }

  @Override
  default int compareTo(@NotNull Rank o) {
    return Integer.compare(o.priority(), priority());
  }

  @Override
  default Optional<Component> render() {
    return tag().flatMap(Renderable::render);
  }
}
