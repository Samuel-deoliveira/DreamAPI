package fr.dreamin.dreamapi.plugin.cmd.admin.broadcast;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.EnumSet;

@Data
@Builder
@Accessors(chain = true)
public final class BroadcastContext {

  // Style global
  @Builder.Default private @NotNull Component prefix =
    Component.newline().append(Component.text("BROADCAST: ", NamedTextColor.RED, TextDecoration.BOLD));
  @Builder.Default private @NotNull Component suffix = Component.newline();
  @Builder.Default private @NotNull NamedTextColor color = NamedTextColor.WHITE;
  @Builder.Default private EnumSet<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);

  // Effets optionnels
  @Builder.Default private boolean showTitle = false;
  @Builder.Default private @Nullable Title.Times titleTimes =
    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
  @Builder.Default private @Nullable Sound sound = null;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void send(@NotNull String message) {
    Component msg = Component.text(message, color);

    for (var deco : decorations) msg = msg.decoration(deco, true);

    Component broadcast = prefix.append(msg).append(suffix);

    Bukkit.broadcast(broadcast);

    if (showTitle) {
      Title title = Title.title(msg, Component.empty(), titleTimes);
      Bukkit.getServer().showTitle(title);
    }

    if (sound != null) Bukkit.getServer().playSound(sound);
  }

  // Helpers fluides pour simplifier le code
  public BroadcastContext addDecoration(TextDecoration deco) {
    decorations.add(deco);
    return this;
  }

  public BroadcastContext clearDecorations() {
    decorations.clear();
    return this;
  }
}