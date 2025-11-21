package fr.dreamin.dreamapi.core.msg;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Creates a color gradient over a given text using a sequence of colors.
 * <p>
 * The gradient can be animated (shifted) over time for dynamic color effects.
 *
 * <pre>
 * Example:
 * GradientText gradient = new GradientText("DreamAPI", List.of("#ff0000", "#00ff00", "#0000ff"));
 * Bukkit.broadcastMessage(gradient.getGradientText());
 * </pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
@Getter @Setter
public final class GradientText {

  private final @NotNull String baseText;
  private final List<ChatColor> gradientColors;
  private int shiftIndex = 0;
  private boolean loop = true;

  public GradientText(final @NotNull String text, final @NotNull List<String> colorHexList) {
    if (colorHexList.size() < 2)
      throw new IllegalArgumentException("GradientText requires at least two colors.");
    this.baseText = text;
    this.gradientColors = generateGradient(colorHexList, text.length());
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Generates the gradient-applied text, shifting the gradient each call if looping is enabled.
   *
   * @return Colored gradient text string.
   */
  public String getGradientText() {
    final var sb = new StringBuilder(baseText.length() * 12); // 12 = avg color code + char
    final var len = baseText.length();

    for (var i = 0; i < len; i++) {
      final var index = (shiftIndex + i) % gradientColors.size();
      sb.append(gradientColors.get(index)).append(baseText.charAt(i));
    }

    if (loop)
      shiftIndex = (shiftIndex + 1) % gradientColors.size();

    return sb.toString();
  }

  /**
   * Resets the gradient animation to its initial state.
   */
  public void reset() {
    this.shiftIndex = 0;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Generates the gradient by interpolating between each consecutive pair of colors.
   *
   * @param colorHexList color hex strings
   * @param steps        number of steps (characters)
   * @return a list of interpolated ChatColors
   */
  private List<ChatColor> generateGradient(final @NotNull List<String> colorHexList, final int steps) {
    final var result = new ArrayList<ChatColor>();
    final var segments = colorHexList.size() - 1;
    final var stepsPerSegment = Math.max(1, steps / segments);

    for (var i = 0; i < segments; i++) {
      final var start = ChatColor.of(colorHexList.get(i)).getColor();
      final var end = ChatColor.of(colorHexList.get(i + 1)).getColor();

      for (var j = 0; j < stepsPerSegment; j++) {
        final var ratio = (double) j / stepsPerSegment;
        final var red = interpolate(start.getRed(), end.getRed(), ratio);
        final var green = interpolate(start.getGreen(), end.getGreen(), ratio);
        final var blue = interpolate(start.getBlue(), end.getBlue(), ratio);
        result.add(ChatColor.of(new Color(red, green, blue)));
      }
    }

    // Ensure at least as many colors as characters
    while (result.size() < steps)
      result.add(result.get(result.size() - 1));

    return result;
  }

  /**
   * Interpolates a single RGB component.
   */
  private int interpolate(final int start, final int end, final double ratio) {
    return (int) Math.round(start + (end - start) * ratio);
  }

  // ###############################################################
  // --------------------------- BUILDER ---------------------------
  // ###############################################################

  /**
   * Creates a new builder for GradientText.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String text;
    private final List<String> colors = new ArrayList<>();
    private boolean loop = true;

    public Builder text(final @NotNull String text) {
      this.text = text;
      return this;
    }

    public Builder addColor(final @NotNull String hexColor) {
      Objects.requireNonNull(hexColor, "Color cannot be null");
      this.colors.add(hexColor);
      return this;
    }

    public Builder loop(final boolean loop) {
      this.loop = loop;
      return this;
    }

    public GradientText build() {
      final var gt = new GradientText(text, colors);
      gt.setLoop(loop);
      return gt;
    }
  }
}