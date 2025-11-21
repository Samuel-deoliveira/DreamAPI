package fr.dreamin.dreamapi.core.utils;

import net.kyori.adventure.title.Title;

import java.time.Duration;

public class MinecraftUtils {

  /**
   *
   * @param fadeInt
   * @param stay
   * @param fadeOut
   * @return Title.Times
   */
  public static Title.Times tickToTimes(final int fadeInt, final int stay, final int fadeOut) {
    return Title.Times.times(Duration.ofMillis(fadeInt*50L), Duration.ofMillis(stay*20L), Duration.ofMillis(fadeOut*50L));
  }

  /**
   *
   * @param fadeInt
   * @param stay
   * @param fadeOut
   * @return Title.Times
   */
  public static Title.Times secondsToTimes(final int fadeInt, final int stay, final int fadeOut) {
    return Title.Times.times(Duration.ofSeconds(fadeInt), Duration.ofSeconds(stay), Duration.ofSeconds(fadeOut));
  }

}