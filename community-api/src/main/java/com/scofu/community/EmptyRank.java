package com.scofu.community;

import com.jsoniter.any.Any;
import com.scofu.common.json.PeriodEscapedString;
import com.scofu.text.Color;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class EmptyRank implements Rank {

  static final EmptyRank INSTANCE = new EmptyRank();

  @Override
  public String name() {
    return null;
  }

  @Override
  public void setName(String name) {

  }

  @Override
  public Optional<RankPrefix> prefix() {
    return Optional.empty();
  }

  @Override
  public void setPrefix(RankPrefix prefix) {

  }

  @Override
  public int priority() {
    return 0;
  }

  @Override
  public void setPriority(int priority) {

  }

  @Override
  public Optional<Color> nameColor() {
    return Optional.empty();
  }

  @Override
  public void setNameColor(Color nameColor) {

  }

  @Override
  public Optional<Map<PeriodEscapedString, Boolean>> permissions() {
    return Optional.empty();
  }

  @Override
  public void setPermissions(Map<PeriodEscapedString, Boolean> permissions) {

  }

  @Override
  public Optional<Set<String>> inheritance() {
    return Optional.empty();
  }

  @Override
  public void setInheritance(Set<String> inheritance) {

  }

  @Override
  public Any any() {
    return null;
  }

  @Override
  public String id() {
    return null;
  }
}
