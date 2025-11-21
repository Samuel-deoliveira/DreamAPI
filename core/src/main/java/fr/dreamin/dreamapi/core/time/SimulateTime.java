package fr.dreamin.dreamapi.core.time;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
@Accessors(fluent = true)
public final class SimulateTime {

  /** Total simulated time in seconds. */
  private double seconds;

  // ###############################################################
  // -------------------------- CONSTRUCTORS -----------------------
  // ###############################################################

  /**
   * Constructs a SimulateTime instance using hours, minutes, and seconds.
   *
   * @param hour   the hour value (0–23)
   * @param minute the minute value (0–59)
   * @param second the second value (0–59)
   */
  public SimulateTime(final int hour, final int minute, final int second) {
    this.seconds = second + (minute * 60D) + (hour * 3600D);
    normalize();
  }

  /**
   * Constructs a copy of another SimulateTime instance.
   *
   * @param time the SimulateTime to copy
   */
  public SimulateTime(final @NotNull SimulateTime time) {
    this.seconds = time.seconds;
    normalize();
  }

  /**
   * Constructs a SimulateTime instance directly from total seconds.
   *
   * @param totalSeconds the total seconds (e.g., 5400 = 1h30)
   */
  public SimulateTime(final double totalSeconds) {
    this.seconds = totalSeconds;
    normalize();
  }

  // ###############################################################
  // ------------------------ TIME ACCESSORS -----------------------
  // ###############################################################

  /** @return hours (0–23) */
  public int getHours() {
    return (int) (seconds / 3600);
  }

  /** @return minutes (0–59) */
  public int getMinutes() {
    return (int) ((seconds % 3600) / 60);
  }

  /** @return seconds (0–59) */
  public int getSeconds() {
    return (int) (seconds % 60);
  }

  /** @return the total time in seconds */
  public double getTotalSeconds() {
    return seconds;
  }

  // ###############################################################
  // ------------------------- TIME LOGIC --------------------------
  // ###############################################################

  /**
   * Calculates the number of seconds to advance per tick,
   * based on the duration of a full cycle and the next target time.
   *
   * @param cycleDurationInTicks duration of one full cycle (e.g., 24000 ticks)
   * @param nextCycleTime the next target simulated time
   * @return the number of seconds to add per tick
   */
  public double calculateIncrementPerTick(int cycleDurationInTicks, SimulateTime nextCycleTime) {
    final var current = this.seconds;
    final var next = nextCycleTime.seconds;

    var diff = next - current;
    if (diff < 0) diff += 24 * 3600; // handle rollover to next day

    return diff / cycleDurationInTicks;
  }

  /**
   * Advances the simulated time by a given number of seconds.
   * Wraps around after 24 hours.
   *
   * @param incrementSeconds seconds to advance
   */
  public void advanceTime(double incrementSeconds) {
    this.seconds += incrementSeconds;
    normalize();
  }

  /**
   * Normalizes the internal time value to ensure it stays within a 24h range.
   */
  private void normalize() {
    if (this.seconds < 0) this.seconds += 24 * 3600;
    this.seconds %= 24 * 3600;
  }

  // ###############################################################
  // --------------------------- UTILITY ---------------------------
  // ###############################################################

  @Override
  public String toString() {
    return String.format("%02dh%02dm%02ds", getHours(), getMinutes(), getSeconds());
  }

  /**
   * @return a compact HH:mm string (useful for UI)
   */
  public String toShortString() {
    return String.format("%02d:%02d", getHours(), getMinutes());
  }

  /**
   * @param other another simulated time
   * @return difference in seconds between this and another time
   */
  public double difference(SimulateTime other) {
    var diff = other.seconds - this.seconds;
    if (diff < 0) diff += 24 * 3600;
    return diff;
  }

  /**
   * @param other another time to compare
   * @return true if this time is before the given time
   */
  public boolean isBefore(SimulateTime other) {
    return this.seconds < other.seconds;
  }

  /**
   * @param other another time to compare
   * @return true if this time is after the given time
   */
  public boolean isAfter(SimulateTime other) {
    return this.seconds > other.seconds;
  }

  /**
   * @param other another time
   * @return true if both times represent the same second
   */
  public boolean equalsTime(SimulateTime other) {
    return Math.abs(this.seconds - other.seconds) < 0.0001;
  }
}