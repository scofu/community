package com.scofu.community;

/** Time. */
public enum Time {
  MORNING(0),
  DAY(6000),
  EVENING(12500),
  NIGHT(18000);

  private final int ticks;

  Time(int ticks) {
    this.ticks = ticks;
  }

  public int ticks() {
    return ticks;
  }
}
