package fr.dreamin.dreamapi.core.time;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Extension of TickTask that directly manipulates a SimulateTime instance
 * — useful for building time-based simulation cycles.
 */
public final class TimeTickTask extends TickTask<TimeTickTask> {

  @Getter
  private final @NotNull SimulateTime time;
  private final double incrementPerTick;

  public TimeTickTask(final @NotNull SimulateTime start, double incrementPerTick) {
    this.time = start;
    this.incrementPerTick = incrementPerTick;
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void onTick() {
    time.advanceTime(incrementPerTick);
  }

}
