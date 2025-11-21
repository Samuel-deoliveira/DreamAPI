package fr.dreamin.dreamapi.core.animation;

import fr.dreamin.dreamapi.api.animation.KeyFrame;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import org.bukkit.Location;

import java.time.Duration;

public record KeyFrameImpl(Location location, Duration duration, InterpolationType easing) implements KeyFrame {}
