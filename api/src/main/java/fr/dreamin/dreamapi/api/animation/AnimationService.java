package fr.dreamin.dreamapi.api.animation;

import org.jetbrains.annotations.NotNull;

public interface AnimationService {
  CinematicBuilder cinematic(final @NotNull String name);
}
