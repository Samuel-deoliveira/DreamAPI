package fr.dreamin.dreamapi.core.particle;

import fr.dreamin.dreamapi.core.time.TickTask;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

/**
 * ParticleSequenceItem - Represents a single step in a ParticleSequence.
 * <p>
 * It holds the animation task and optional delays (in ticks) to wait
 * before starting the task and before starting the next item in the sequence.
 */
@Getter
@Accessors(fluent = true)
public final class ParticleSequenceItem {

  private final TickTask<?> task;
  private final int delayBefore; // Delay in ticks before this task starts
  private final int delayAfter; // Delay in ticks after this task finishes before the next one starts

  private ParticleSequenceItem(@NotNull TickTask<?> task, int delayBefore, int delayAfter) {
    this.task = task;
    this.delayBefore = delayBefore;
    this.delayAfter = delayAfter;
  }

  /**
   * Creates a sequence item with an animation task and a delay before the next item.
   * @param task The animation task (ParticleAnimation, ParticleShapeMorph, or ParticleChainMorph).
   * @param delayBefore The delay in ticks before this task starts.
   * @param delayAfter The delay in ticks after this task finishes before the next one starts.
   * @return A new ParticleSequenceItem instance.
   */
  public static ParticleSequenceItem of(@NotNull TickTask<?> task, int delayBefore, int delayAfter) {
    return new ParticleSequenceItem(task, delayBefore, delayAfter);
  }

  /**
   * Creates a sequence item with an animation task and a delay after it finishes.
   * @param task The animation task (ParticleAnimation, ParticleShapeMorph, or ParticleChainMorph).
   * @param delayAfter The delay in ticks after this task finishes before the next one starts.
   * @return A new ParticleSequenceItem instance.
   */
  public static ParticleSequenceItem of(@NotNull TickTask<?> task, int delayAfter) {
    return new ParticleSequenceItem(task, 0, delayAfter);
  }

  /**
   * Creates a sequence item with an animation task and no delay.
   * @param task The animation task (ParticleAnimation, ParticleShapeMorph, or ParticleChainMorph).
   * @return A new ParticleSequenceItem instance.
   */
  public static ParticleSequenceItem of(@NotNull TickTask<?> task) {
    return new ParticleSequenceItem(task, 0, 0);
  }


}