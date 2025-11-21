package fr.dreamin.dreamapi.core.time.day.impl;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.time.SimulateTime;
import fr.dreamin.dreamapi.core.time.day.SimulatedDayCycle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@DreamAutoService(value= DayCycleService.class)
public final class DayCycleServiceImpl implements DayCycleService, DreamService {

  private final Map<String, SimulatedDayCycle> cycles = new ConcurrentHashMap<>();

  private Runnable onGlobalStart;
  private Runnable onGlobalStop;
  private Runnable onGlobalSunrise;
  private Runnable onGlobalSunset;
  private Runnable onGlobalMidnight;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public @NotNull SimulatedDayCycle addWorld(@NotNull World world, @NotNull SimulateTime sunrise, @NotNull SimulateTime sunset) {
    return addWorld(world, sunrise, sunset, new SimulateTime(0, 0, 0));
  }

  @Override
  public @NotNull SimulatedDayCycle addWorld(@NotNull World world, @NotNull SimulateTime sunrise, @NotNull SimulateTime sunset, @NotNull SimulateTime end) {
    if (isWorldRegistered(world.getName()))
      return this.cycles.get(world.getName());

    final var cycle = new SimulatedDayCycle(world)
      .sunrise(sunrise)
      .sunset(sunset)
      .end(end)
      .updateWorldTime(true)
      .autoStop(false)
      .onSunrise(() -> handleGlobalCallback(this.onGlobalSunrise, String.format("Sunrise in %s", world.getName())))
      .onSunset(() -> handleGlobalCallback(this.onGlobalSunset, String.format("Sunset in %s", world.getName())))
      .onMidnight(() -> handleGlobalCallback(this.onGlobalMidnight, String.format("Midnight in %s", world.getName())));

    this.cycles.put(world.getName(), cycle);
    DreamContext.getPlugin().getLogger().info(String.format("Registered day cycle for world: %s", world.getName()));
    return cycle;
  }

  @Override
  public @NotNull SimulatedDayCycle addWorld(@NotNull World world, @NotNull SimulatedDayCycle cycle) {
    if (isWorldRegistered(world.getName()))
      return this.cycles.get(world.getName());

    this.cycles.put(world.getName(), cycle);
    DreamContext.getPlugin().getLogger().info(String.format("Registered custom day cycle for world: %s", world.getName()));
    return cycle;
  }


  @Override
  public void removeWorld(@NotNull String worldName) {
    Optional.ofNullable(this.cycles.remove(worldName))
      .ifPresent(cycle -> {
        cycle.stop();
        DreamContext.getPlugin().getLogger().info(String.format("Removed day cycle for world: %s", worldName));
      });
  }

  @Override
  public void clearAll() {
    this.cycles.values().forEach(SimulatedDayCycle::stop);
    this.cycles.clear();
    DreamContext.getPlugin().getLogger().info("Cleared all simulated day cycles.");
  }

  @Override
  public Optional<SimulatedDayCycle> getCycle(@NotNull String worldName) {
    return Optional.ofNullable(this.cycles.get(worldName));
  }

  @Override
  public boolean exists(@NotNull String worldName) {
    return this.cycles.containsKey(worldName);
  }

  @Override
  public Collection<SimulatedDayCycle> all() {
    return Collections.unmodifiableCollection(this.cycles.values());
  }

  @Override
  public void startAll() {
    if (this.onGlobalStart != null) this.onGlobalStart.run();
    this.cycles.values().forEach(SimulatedDayCycle::start);
  }

  @Override
  public void stopAll() {
    this.cycles.values().forEach(SimulatedDayCycle::stop);
    if (this.onGlobalStop != null) this.onGlobalStop.run();
  }

  @Override
  public void pauseAll() {
    this.cycles.values().forEach(SimulatedDayCycle::pause);
  }

  @Override
  public void resumeAll() {
    this.cycles.values().forEach(SimulatedDayCycle::resume);
  }

  @Override
  public void forEach(Consumer<SimulatedDayCycle> action) {
    this.cycles.values().forEach(action);
  }

  @Override
  public DayCycleService onGlobalStart(Runnable action) {
    this.onGlobalStart = action;
    return this;
  }

  @Override
  public DayCycleService onGlobalStop(Runnable action) {
    this.onGlobalStop = action;
    return this;
  }

  @Override
  public DayCycleService onGlobalSunrise(Runnable action) {
    this.onGlobalSunrise = action;
    return this;
  }

  @Override
  public DayCycleService onGlobalSunset(Runnable action) {
    this.onGlobalSunset = action;
    return this;
  }

  @Override
  public DayCycleService onGlobalMidnight(Runnable action) {
    this.onGlobalMidnight = action;
    return this;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private boolean isWorldRegistered(@NotNull String worldName) {
    final var registered = this.cycles.containsKey(worldName);
    if (registered)
      DreamContext.getPlugin().getLogger().warning(String.format("World '%s' already has a registered day cycle.", worldName));

    return this.cycles.containsKey(worldName);
  }

  private void handleGlobalCallback(Runnable action, String debugMessage) {
    if (action != null) action.run();
    DreamContext.getPlugin().getLogger().info("[DayCycleService] " + debugMessage);
  }

}
