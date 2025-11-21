package fr.dreamin.dreamapi.core.animation;

import fr.dreamin.dreamapi.api.animation.*;
import fr.dreamin.dreamapi.api.animation.camera.CameraSegment;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.animation.camera.CameraSegmentImpl;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@DreamAutoService(value=AnimationService.class)
public class AnimationServiceImpl implements AnimationService, DreamService {

  private final Map<String, Cinematic> cinematics = new HashMap<>();

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public CinematicBuilder cinematic(@NotNull String name) {
    return new CinematicBuilderImpl(name);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  @RequiredArgsConstructor
  private static final class CinematicBuilderImpl implements CinematicBuilder {
    private final String name;
    private final List<CameraSegment> segments = new ArrayList<>();

    private boolean returnToStart = false;
    private Location endAt = null;
    private GameMode forcedGameMode = null;
    private boolean copyInventory = false;
    private ReconnectBehavior reconnectBehavior = ReconnectBehavior.RESTART;


    private Consumer<Player> onStart;
    private Consumer<Player> onEnd;
    private BiConsumer<Player, Integer> onSegmentChange;

    // ###############################################################
    // -------------------------- METHODS ----------------------------
    // ###############################################################

    @Override
    public CinematicBuilder camera(@NotNull Location start, @NotNull Location end, @NotNull Duration duration) {
      return camera(start, end, duration, InterpolationType.LINEAR);
    }

    @Override
    public CinematicBuilder camera(@NotNull Location start, @NotNull Location end, @NotNull Duration duration, @NotNull InterpolationType type) {
      segments.add(new CameraSegmentImpl(start, end, duration, type));
      return this;
    }

    @Override public CinematicBuilder returnToStart(boolean enabled) { this.returnToStart = enabled; return this; }
    @Override public CinematicBuilder endAt(@NotNull Location loc) { this.endAt = loc; return this; }
    @Override public CinematicBuilder gameMode(@NotNull GameMode gm) { this.forcedGameMode = gm; return this; }
    @Override public CinematicBuilder copyInventory(boolean enabled) { this.copyInventory = enabled; return this; }
    @Override public CinematicBuilder reconnectBehavior(@NotNull ReconnectBehavior behavior) { this.reconnectBehavior = behavior; return this; }

    @Override public CinematicBuilder onStart(@NotNull Consumer<org.bukkit.entity.Player> c) { this.onStart = c; return this; }
    @Override public CinematicBuilder onEnd(@NotNull Consumer<org.bukkit.entity.Player> c) { this.onEnd = c; return this; }
    @Override public CinematicBuilder onSegmentChange(@NotNull BiConsumer<org.bukkit.entity.Player, Integer> c) { this.onSegmentChange = c; return this; }

    @Override
    public Cinematic build() {
      return new CinematicImpl(
        this.name, this.segments,
        this.returnToStart, this.endAt,
        this.forcedGameMode, this.copyInventory,
        this.reconnectBehavior,
        this.onStart, this.onEnd, this.onSegmentChange
      );
    }
  }
}