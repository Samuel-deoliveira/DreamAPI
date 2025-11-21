package fr.dreamin.dreamapi.api.animation;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Cinematic {
  String name();
  void play(final @NotNull Player player);
  void play(final @NotNull List<@NotNull Player> players);
  void play();

  void stop(final @NotNull Player player);
  void stop(final @NotNull List<@NotNull Player> players);
  void stop();

  boolean isPlaying(final @NotNull Player player);

}
