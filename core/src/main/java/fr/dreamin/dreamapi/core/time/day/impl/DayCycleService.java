package fr.dreamin.dreamapi.core.time.day.impl;

import fr.dreamin.dreamapi.core.time.SimulateTime;
import fr.dreamin.dreamapi.core.time.day.SimulatedDayCycle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public interface DayCycleService {

  @NotNull SimulatedDayCycle addWorld(@NotNull World world, @NotNull SimulateTime sunrise, @NotNull SimulateTime sunset);
  @NotNull SimulatedDayCycle addWorld(@NotNull World world, @NotNull SimulateTime sunrise, @NotNull SimulateTime sunset, @NotNull SimulateTime end);
  @NotNull SimulatedDayCycle addWorld(@NotNull World world, @NotNull SimulatedDayCycle cycle);

  void removeWorld(@NotNull String worldName);
  void clearAll();
  Optional<SimulatedDayCycle> getCycle(@NotNull String worldName);
  boolean exists(@NotNull String worldName);
  Collection<SimulatedDayCycle> all();

  void startAll();
  void stopAll();
  void pauseAll();
  void resumeAll();

  void forEach(Consumer<SimulatedDayCycle> action);

  DayCycleService onGlobalStart(Runnable action);
  DayCycleService onGlobalStop(Runnable action);
  DayCycleService onGlobalSunrise(Runnable action);
  DayCycleService onGlobalSunset(Runnable action);
  DayCycleService onGlobalMidnight(Runnable action);


}
