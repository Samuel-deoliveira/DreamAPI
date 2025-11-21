package fr.dreamin.dreamapi.core.particle;

import fr.dreamin.dreamapi.core.time.TickTask;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ParticleSequence - Manages a sequence of particle animations (ParticleAnimation, ParticleShapeMorph, or ParticleChainMorph).
 * <p>
 * This class allows chaining multiple particle animations to create complex,
 * continuous effects, including infinite loops and controlled delays between steps.
 */
@Getter
@Accessors(fluent = true)
public final class ParticleSequence extends TickTask<ParticleSequence> {

  private final List<ParticleSequenceItem> items;
  private final boolean loop;
  private int currentItemIndex = 0;
  private TickTask<?> currentDelayTask = null;

  private ParticleSequence(Builder builder) {
    super(builder);
    this.items = builder.items;
    this.loop = builder.loop;
    this.limit(-1); // Sequence is potentially infinite
    this.autoStop(false);
    this.every(1); // Check status every tick
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onStart() {
    this.currentItemIndex = 0;
    if (items.isEmpty()) {
      stop();
      return;
    }
    startCurrentItem();
  }

  @Override
  public void onStop() {
    if (currentDelayTask != null) {
      currentDelayTask.stop();
      currentDelayTask = null;
    }
    // Stop the currently running animation task if it exists
    if (currentItemIndex < items.size()) {
      items.get(currentItemIndex).task().stop();
    }
  }

  @Override
  public void onTick() {
    // onTick is mainly used to keep the task alive and check for external stop.
    // The transition logic is handled by the onEnd listener of the sub-tasks.
  }

  private void startCurrentItem() {
    if (currentItemIndex >= items.size()) {
      if (loop) {
        currentItemIndex = 0;
      } else {
        stop();
        return;
      }
    }

    ParticleSequenceItem currentItem = items.get(currentItemIndex);
    TickTask<?> currentTask = currentItem.task();

    // 1. Handle the delay before the current item starts
    if (currentItem.delayBefore() > 0) {
      currentDelayTask = TickTask.builder()
        .limit(currentItem.delayBefore())
        .onEnd(delayTask -> {
          currentDelayTask = null;
          startItemTask(currentItem, currentTask);
        })
        .build();
      currentDelayTask.start();
    } else {
      startItemTask(currentItem, currentTask);
    }
  }

  private void startItemTask(ParticleSequenceItem currentItem, TickTask<?> currentTask) {
    // 2. Configure the task to advance to the next item when it finishes
    currentTask.onEnd(tick -> {
      // 3. Handle the delay after the current item finishes
      if (currentItem.delayAfter() > 0) {
        currentDelayTask = TickTask.builder()
          .limit(currentItem.delayAfter())
          .onEnd(delayTask -> {
            currentDelayTask = null;
            currentItemIndex++;
            startCurrentItem();
          })
          .build();
        currentDelayTask.start();
      } else {
        currentItemIndex++;
        startCurrentItem();
      }
    });

    // 4. Start the task
    // If it's a ParticleSequence, we just start it (it handles its own play logic)
    if (currentTask instanceof ParticleSequence sequence)
      sequence.start();
    else if (currentTask instanceof ParticleAnimation animation)
      animation.play();
    else if (currentTask instanceof ParticleShapeMorph morph)
      morph.play();
    else if (currentTask instanceof ParticleChainMorph chainMorph)
      chainMorph.play();
    else
      currentTask.start();
  }

  /**
   * Starts the sequence for all players who can see the particles.
   * <p>
   * This is the default play method when no specific viewer is provided.
   */
  public void play() {
    this.start();
  }

  /**
   * Starts the sequence for a specific player.
   * <p>
   * Note: This only works if the sub-tasks support per-player rendering.
   *
   * @param player The player who will see the particles.
   */
  public void play(@NotNull Player player) {
    items.forEach(item -> {
      TickTask<?> task = item.task();
      if (task instanceof ParticleSequence sequence)
        sequence.play(player);
      else if (task instanceof ParticleAnimation animation)
        animation.play(player);
      else if (task instanceof ParticleShapeMorph morph)
        morph.play(player);
      else if (task instanceof ParticleChainMorph chainMorph)
        chainMorph.play(player);

    });
    this.start();
  }

  /**
   * Starts the sequence for a collection of players.
   * <p>
   * Note: This only works if the sub-tasks support per-player rendering.
   *
   * @param players The collection of players who will see the particles.
   */
  public void play(@NotNull Collection<Player> players) {
    items.forEach(item -> {
      TickTask<?> task = item.task();
      if (task instanceof ParticleSequence sequence)
        sequence.play(players);
      else if (task instanceof ParticleAnimation animation)
        animation.play(players);
      else if (task instanceof ParticleShapeMorph morph)
        morph.play(players);
      else if (task instanceof ParticleChainMorph chainMorph)
        chainMorph.play(players);
    });
    this.start();
  }

  // ###############################################################
  // --------------------------- BUILDER ---------------------------
  // ###############################################################

  public static Builder create() {
    return new Builder();
  }



  @Getter
  @Accessors(fluent = true)
  public static class Builder extends TickTask.Builder<ParticleSequence, Builder> {
    private final List<ParticleSequenceItem> items = new ArrayList<>();
    private boolean loop = false;

    /**
     * Adds a single animation task to the sequence with no delay after it finishes.
     * @param task The animation task.
     * @return The builder instance.
     */
    public Builder add(TickTask<?> task) {
      this.items.add(ParticleSequenceItem.of(task));
      return this;
    }

    /**
     * Adds a single animation task to the sequence with a delay before the next item starts.
     * @param task The animation task.
     * @param delayAfter The delay in ticks after this task finishes.
     * @return The builder instance.
     */
    public Builder add(TickTask<?> task, int delayAfter) {
      this.items.add(ParticleSequenceItem.of(task, delayAfter));
      return this;
    }

    /**
     * Adds a pre-configured sequence item.
     * @param item The sequence item.
     * @return The builder instance.
     */
    public Builder add(ParticleSequenceItem item) {
      this.items.add(item);
      return this;
    }

    public Builder loop(boolean loop) {
      this.loop = loop;
      return this;
    }

    @Override
    public ParticleSequence build() {
      return new ParticleSequence(this);
    }
  }
}