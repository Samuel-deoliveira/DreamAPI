package fr.dreamin.dreamapi.api.interpolation;

/**
 * Defines interpolation curve behavior.
 */
public enum InterpolationType {
  /** Constant speed from start to end. */
  LINEAR,
  /** Starts slow, speeds up progressively. */
  EASE_OUT,
  /** Starts fast, slows down progressively. */
  EASE_IN_OUT,
  /** Slow at start & end, fast in middle. */
  EASE_IN
}