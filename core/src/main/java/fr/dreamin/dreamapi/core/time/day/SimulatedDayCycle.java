package fr.dreamin.dreamapi.core.time.day;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.core.time.SimulateTime;
import fr.dreamin.dreamapi.core.time.TickTask;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Advanced simulated day/night cycle that uses {@link SimulateTime} and {@link TickTask}
 * to emulate a time progression system with callbacks for sunrise, sunset, and midnight.
 */
@Getter
@Accessors(fluent = true)
public final class SimulatedDayCycle extends TickTask<SimulatedDayCycle> {

  private final World world;
  private SimulateTime sunrise = new SimulateTime(6, 0, 0);
  private SimulateTime sunset = new SimulateTime(18, 0, 0);
  private SimulateTime end = new SimulateTime(0, 0, 0);

  private SimulateTime simulateTime = new SimulateTime(6, 0, 0);
  private double incrementPerTick = 0.5;
  private boolean updateWorldTime = true;

  private Runnable onSunrise;
  private Runnable onSunset;
  private Runnable onMidnight;
  private Runnable onCycleEnd;

  /**
   * Constructs a new simulated day-night cycle handler.
   *
   * @param world  the world to update (can be null for simulation-only mode)
   */
  public SimulatedDayCycle(final @NotNull World world) {
    this.world = world;
  }

  // ###############################################################
  // ----------------------- CONFIGURATION -------------------------
  // ###############################################################

  public SimulatedDayCycle sunrise(final @NotNull SimulateTime sunrise) {
    this.sunrise = sunrise;
    recalculateIncrement();
    return this;
  }

  public SimulatedDayCycle sunset(final @NotNull SimulateTime sunset) {
    this.sunset = sunset;
    recalculateIncrement();
    return this;
  }

  public SimulatedDayCycle end(final @NotNull SimulateTime end) {
    this.end = end;
    recalculateIncrement();
    return this;
  }

  public SimulatedDayCycle startAt(final @NotNull SimulateTime start) {
    this.simulateTime = new SimulateTime(start);
    return this;
  }

  public SimulatedDayCycle incrementPerTick(final double increment) {
    this.incrementPerTick = increment;
    return this;
  }

  public SimulatedDayCycle updateWorldTime(final boolean update) {
    this.updateWorldTime = update;
    return this;
  }

  // ###############################################################
  // ---------------------- EVENT CALLBACKS ------------------------
  // ###############################################################

  public SimulatedDayCycle onSunrise(Runnable action) {
    this.onSunrise = action;
    return this;
  }

  public SimulatedDayCycle onSunset(Runnable action) {
    this.onSunset = action;
    return this;
  }

  public SimulatedDayCycle onMidnight(Runnable action) {
    this.onMidnight = action;
    return this;
  }

  public SimulatedDayCycle onCycleEnd(Runnable action) {
    this.onCycleEnd = action;
    return this;
  }

  // ###############################################################
  // -------------------------- LIFECYCLE --------------------------
  // ###############################################################

  @Override
  public void onStart() {
    recalculateIncrement();
    DreamAPI.getAPI().getLogger().info(String.format("[DayCycle] Started at %s", simulateTime.toShortString()));
  }

  @Override
  public void onTick() {
    simulateTime.advanceTime(incrementPerTick);

    if (updateWorldTime) {
      final var mcTime = (long) ((simulateTime.getTotalSeconds() / (24D * 3600D)) * 24000D);
      world.setTime(mcTime);
    }

    if (simulateTime.equalsTime(sunrise) && onSunrise != null) onSunrise.run();
    if (simulateTime.equalsTime(sunset) && onSunset != null) onSunset.run();
    if (simulateTime.equalsTime(end) && onMidnight != null) onMidnight.run();
  }

  @Override
  public void onEnd() {
    if (onCycleEnd != null) onCycleEnd.run();
    DreamAPI.getAPI().getLogger().info(String.format("[DayCycle] Ended at %s", simulateTime.toShortString()));
  }

  // ###############################################################
  // --------------------------- HELPERS ---------------------------
  // ###############################################################

  private void recalculateIncrement() {
    this.incrementPerTick = sunrise.calculateIncrementPerTick(24000, end);
  }
}