package fr.dreamin.dreamapi.api.animation;

import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import org.bukkit.Location;

import java.time.Duration;

public interface KeyFrame {
  Location location();
  Duration duration();
  InterpolationType easing();
}
