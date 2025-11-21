package fr.dreamin.dreamapi.core.downloader;

import com.bergerkiller.bukkit.common.map.MapTexture;
import fr.dreamin.dreamapi.core.DreamContext;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Utility class for downloading and manipulating images,
 * especially for converting them into {@link MapTexture} instances.
 */
public final class PictureMapDownload {

  private static final Logger LOGGER = getLogger();

  /**
   * Downloads an image from a URL and resizes it before converting to a {@link MapTexture}.
   *
   * @param imageUrl The URL of the image
   * @param width    The target width
   * @param height   The target height
   * @return The {@link MapTexture} generated from the resized image
   * @throws IOException if downloading or reading fails
   */
  public static MapTexture loadImageFromUrl(final @NotNull String imageUrl, int width, int height) throws IOException {
    final var image = downloadImage(imageUrl);
    final var resizedImage = resizeImage(image, width, height);
    return MapTexture.fromImage(resizedImage);
  }

  /**
   * Downloads an image from a URL and converts it directly to a {@link MapTexture}.
   *
   * @param imageUrl The URL of the image
   * @return The {@link MapTexture} generated from the image
   * @throws IOException if downloading or reading fails
   */
  public static MapTexture loadImageFromUrl(final @NotNull String imageUrl) throws IOException {
    final var image = downloadImage(imageUrl);
    return MapTexture.fromImage(image);
  }

  /**
   * Downloads an image from the given URL.
   *
   * @param imageUrl The image URL
   * @return The {@link BufferedImage} downloaded
   * @throws IOException if download or decoding fails
   */
  public static BufferedImage downloadImage(String imageUrl) throws IOException {
    HttpURLConnection connection = null;
    try {
      final var url = new URL(imageUrl);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(10000);

      int response = connection.getResponseCode();
      if (response != HttpURLConnection.HTTP_OK) {
        LOGGER.warning(String.format("Failed to download image from %s (HTTP %d)", imageUrl, response));
        throw new IOException("Invalid HTTP response: " + response);
      }

      try (InputStream inputStream = connection.getInputStream()) {
        final var image = ImageIO.read(inputStream);
        if (image == null) throw new IOException("Failed to decode image: " + imageUrl);
        LOGGER.info(String.format("Image downloaded successfully from %s", imageUrl));
        return image;
      }
    } finally {
      if (connection != null) connection.disconnect();
    }
  }

  /**
   * Resizes a BufferedImage.
   *
   * @param originalImage The source image
   * @param targetWidth   Target width
   * @param targetHeight  Target height
   * @return The resized image
   */
  public static BufferedImage resizeImage(final @NotNull BufferedImage originalImage, int targetWidth, int targetHeight) {
    final var resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    final var g = resizedImage.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    g.dispose();
    return resizedImage;
  }

  /**
   * Rotates an image around its center.
   *
   * @param image The source image
   * @param angle The rotation angle in radians
   * @return A rotated copy of the image
   */
  public static BufferedImage rotateImage(final @NotNull BufferedImage image, double angle) {
    final var width = image.getWidth();
    final var height = image.getHeight();

    final var rotatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final var g = rotatedImage.createGraphics();

    AffineTransform transform = new AffineTransform();
    transform.rotate(angle, width / 2.0, height / 2.0);
    g.setTransform(transform);
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return rotatedImage;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################


  /**
   * Safely retrieves a logger from DreamAPI or provides a fallback.
   */
  private static Logger getLogger() {
    try {
      return DreamContext.getPlugin().getLogger();
    } catch (Exception ignored) {}
    return Logger.getLogger("DreamAPI-PictureMapDownloader");
  }

}
