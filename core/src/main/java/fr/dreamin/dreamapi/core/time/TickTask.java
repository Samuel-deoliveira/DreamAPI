package fr.dreamin.dreamapi.core.time;

import fr.dreamin.dreamapi.core.DreamContext;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * TickTask - a flexible task system designed for Minecraft ticks.
 * <p>
 * Can be used in two ways:
 * <pre>
 * // 1 Builder pattern
 * TickTask.builder(plugin)
 *   .every(1)
 *   .limit(100)
 *   .onTick(t -> Bukkit.broadcastMessage("Tick " + t.current()))
 *   .onEnd(t -> Bukkit.broadcastMessage("Done!"))
 *   .start();
 *
 * // 2 Extend class
 * public class MyTimer extends TickTask {
 *   public MyTimer(Plugin plugin) { super(plugin); }
 *
 *   @Override public void onTick() { Bukkit.broadcastMessage("Tick: " + current()); }
 *   @Override public void onEnd() { Bukkit.broadcastMessage("Timer finished!"); }
 * }
 *
 * new MyTimer(plugin).start();
 * </pre>
 */
@Getter
@Accessors(fluent = true)
public abstract class TickTask<T extends TickTask<T>> {

  private @Nullable BukkitTask task;
  private long current = 0;
  private long startAt = 0;
  private long limit = -1;
  private long delay = 0;
  private long every = 1;
  private long tickCounter = 0;
  private boolean paused = false;
  private boolean async = false;
  private boolean running = false;
  private boolean autoStop = false;

  // Listeners (optional via builder)
  private Consumer<T> onStartListener, onTickListener, onPauseListener, onResumeListener, onStopListener, onEndListener;

  // ###############################################################
  // -------------------------- CONSTRUCTORS -----------------------
  // ###############################################################

  public TickTask() {}

  public TickTask(@NotNull Builder builder) {
    this.startAt = builder.start;
    this.limit = builder.limit;
    this.delay = builder.delay;
    this.every = builder.interval;
    this.async = builder.async;
    this.autoStop = builder.autoStop;

    this.onStartListener = builder.onStart;
    this.onTickListener = builder.onTick;
    this.onPauseListener = builder.onPause;
    this.onResumeListener = builder.onResume;
    this.onStopListener = builder.onStop;
    this.onEndListener = builder.onEnd;
  }

  // ###############################################################
  // -------------------------- CORE METHODS -----------------------
  // ###############################################################

  /** Called once when the task starts. */
  public void onStart() {}

  /** Called each tick (interval). */
  public void onTick() {}

  /** Called when the task is paused. */
  public void onPause() {}

  /** Called when the task resumes. */
  public void onResume() {}

  /** Called when the task is manually or automatically stopped. */
  public void onStop() {}

  /** Called when the tick reaches the limit (if defined). */
  public void onEnd() {}

  // ###############################################################
  // --------------------- CALLBACK REGISTRATION -------------------
  // ###############################################################

  @SuppressWarnings("unchecked")
  public T onStart(Consumer<T> callback) { this.onStartListener = callback; return (T) this; }

  @SuppressWarnings("unchecked")
  public T onTick(Consumer<T> callback) { this.onTickListener = callback; return (T) this; }

  @SuppressWarnings("unchecked")
  public T onPause(Consumer<T> callback) { this.onPauseListener = callback; return (T) this; }

  @SuppressWarnings("unchecked")
  public T onResume(Consumer<T> callback) { this.onResumeListener = callback; return (T) this; }

  @SuppressWarnings("unchecked")
  public T onStop(Consumer<T> callback) { this.onStopListener = callback; return (T) this; }

  @SuppressWarnings("unchecked")
  public T onEnd(Consumer<T> callback) { this.onEndListener = callback; return (T) this; }

  // ###############################################################
  // ---------------------- FLUENT SETTERS --------------------------
  // ###############################################################

  @SuppressWarnings("unchecked")
  public T autoStop(boolean autoStop) { this.autoStop = autoStop; return (T) this; }

  @SuppressWarnings("unchecked")
  public T async(boolean async) { this.async = async; return (T) this; }

  @SuppressWarnings("unchecked")
  public T every(long ticks) { this.every = ticks > 0 ? ticks : 1; return (T) this; }

  @SuppressWarnings("unchecked")
  public T limit(long limit) { this.limit = limit; return (T) this; }

  @SuppressWarnings("unchecked")
  public T startAt(long startAt) { this.startAt = startAt; return (T) this; }

  @SuppressWarnings("unchecked")
  public T delay(long delay) { this.delay = delay; return (T) this; }

  // ###############################################################
  // -------------------------- CONTROL FLOW -----------------------
  // ###############################################################

  /** Starts the ticking process. */
  @SuppressWarnings("unchecked")
  public T start() {
    if (running) stop();

    running = true;
    current = startAt;

    if (onStartListener != null) onStartListener.accept((T) this);
    onStart();

    BukkitRunnable runnable = new BukkitRunnable() {
      @Override
      public void run() {
        if (paused || !running) return;

        if (++tickCounter < every) return;
        tickCounter = 0;

        current++;
        if (onTickListener != null) onTickListener.accept((T) TickTask.this);
        onTick();

        if (limit > 0 && current >= limit) {
          if (onEndListener != null) onEndListener.accept((T) TickTask.this);
          onEnd();
          if (autoStop) stop();
        }
      }
    };

    this.tickCounter = 0;
    this.task = async
      ? runnable.runTaskTimerAsynchronously(DreamContext.getPlugin(), delay, 1L)
      : runnable.runTaskTimer(DreamContext.getPlugin(), delay, 1L);

    return (T) this;
  }

  /** Stops the task completely. */
  @SuppressWarnings("unchecked")
  public T stop() {
    if (!running) return (T) this;

    running = false;
    paused = false;

    if (task != null) {
      task.cancel();
      task = null;
    }

    if (onStopListener != null) onStopListener.accept((T) this);
    onStop();
    return (T) this;
  }

  /** Pauses the ticking process. */
  @SuppressWarnings("unchecked")
  public T pause() {
    if (!running || paused) return (T) this;
    paused = true;
    if (onPauseListener != null) onPauseListener.accept((T) this);
    onPause();
    return (T) this;
  }

  /** Resumes the ticking process. */
  @SuppressWarnings("unchecked")
  public T resume() {
    if (!running || !paused) return (T) this;
    paused = false;
    if (onResumeListener != null) onResumeListener.accept((T) this);
    onResume();
    return (T) this;
  }

  /** Resets the tick counter without restarting the task. */
  @SuppressWarnings("unchecked")
  public T reset() { this.current = startAt; return (T) this; }

  /** Checks if the task is active (running and not paused). */
  public boolean isActive() { return running && !paused; }

  // ###############################################################
  // --------------------------- BUILDER ---------------------------
  // ###############################################################

  @Getter
  @Accessors(fluent = true)
  public static class Builder<T extends TickTask<T>, B extends Builder<T, B>> {
    protected long start = 0;
    protected long limit = -1;
    protected long delay = 0;
    protected long interval = 1;
    protected boolean async = false;
    protected boolean autoStop = true;

    protected Consumer<T> onStart, onTick, onPause, onResume, onStop, onEnd;

    public Builder() {
    }

    @SuppressWarnings("unchecked") public B startAt(long start) { this.start = start; return (B) this; }
    @SuppressWarnings("unchecked") public B limit(long limit) { this.limit = limit; return (B) this; }
    @SuppressWarnings("unchecked") public B delay(long delay) { this.delay = delay; return (B) this; }
    @SuppressWarnings("unchecked") public B every(long ticks) { this.interval = ticks > 0 ? ticks : 1; return (B) this; }
    @SuppressWarnings("unchecked") public B async(boolean async) { this.async = async; return (B) this; }
    @SuppressWarnings("unchecked") public B autoStop(boolean autoStop) { this.autoStop = autoStop; return (B) this; }

    @SuppressWarnings("unchecked") public B onStart(Consumer<T> onStart) { this.onStart = onStart; return (B) this; }
    @SuppressWarnings("unchecked") public B onTick(Consumer<T> onTick) { this.onTick = onTick; return (B) this; }
    @SuppressWarnings("unchecked") public B onPause(Consumer<T> onPause) { this.onPause = onPause; return (B) this; }
    @SuppressWarnings("unchecked") public B onResume(Consumer<T> onResume) { this.onResume = onResume; return (B) this; }
    @SuppressWarnings("unchecked") public B onStop(Consumer<T> onStop) { this.onStop = onStop; return (B) this; }
    @SuppressWarnings("unchecked") public B onEnd(Consumer<T> onEnd) { this.onEnd = onEnd; return (B) this; }

    /** Build instance (must be overridden by subclasses). */
    public T build() { throw new UnsupportedOperationException("Use subclass-specific builder"); }
  }

  /** Shortcut for creating a new generic builder. */
  public static Builder builder() { return new Builder(); }
}