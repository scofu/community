package com.scofu.community.bukkit;

import com.scofu.command.standard.TranslatedEnum;

/**
 * Time.
 */
public enum Time implements TranslatedEnum {

  MORNING(0, "time.morning"),
  DAY(6000, "time.day"),
  EVENING(12500, "time.evening"),
  NIGHT(18000, "time.night");

  private final int time;
  private final String translation;

  Time(int time, String translation) {
    this.time = time;
    this.translation = translation;
  }

  public int time() {
    return time;
  }

  @Override
  public String translation() {
    return translation;
  }
}
