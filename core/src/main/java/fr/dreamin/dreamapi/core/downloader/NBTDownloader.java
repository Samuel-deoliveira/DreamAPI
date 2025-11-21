package fr.dreamin.dreamapi.core.downloader;

import fr.dreamin.dreamapi.core.DreamContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Utility class for downloading files and clearing directories.
 * <p>
 * Used for resource downloads (e.g., NBT structures, JSON configs, etc.).
 */
public final class NBTDownloader {

  private static final Logger LOGGER = getLogger();

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Downloads a file from a given URL to the specified destination.
   *
   * @param urlStr          The file URL
   * @param outputFileName  The output path for the downloaded file
   * @return The downloaded {@link File}
   * @throws IOException if an I/O error occurs
   */
  public static File downloadFileFromURL(final @NotNull String urlStr, final @NotNull String outputFileName) throws IOException {
    final var file = new File(outputFileName);

    if (file.exists()) {
      LOGGER.info(String.format("File already exists: %s", file.getAbsolutePath()));
      return file;
    }

    final var url = new URL(urlStr);
    final var connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(10000);

    // Vérifier le statut de la connexion
    int status = connection.getResponseCode();
    if (status != HttpURLConnection.HTTP_OK)
      LOGGER.warning(String.format("Failed to download file (%s). HTTP response code: %d", urlStr, status));

    try (InputStream inputStream = connection.getInputStream();
         FileOutputStream outputStream = new FileOutputStream(file)) {

      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    } finally {
      connection.disconnect();
    }

    LOGGER.info(String.format("File downloaded successfully: %s", file.getAbsolutePath()));
    return file;
  }

  /**
   * Deletes all files inside the specified folder.
   *
   * @param folderName The name or path of the folder
   */
  public static void clearFolder(final @NotNull String folderName) {
    final var folder = new File(folderName);
    final var log = getLogger();

    if (!folder.exists() || !folder.isDirectory()) {
      log.warning(String.format("Folder '%s' does not exist or is not a directory.", folderName));
      return;
    }

    final var files = folder.listFiles();
    if (files == null) return;

    for (var file : files) {
      if (!file.isFile()) continue;
      if (!file.delete())
        log.warning(String.format("Failed to delete file: %s", file.getName()));
      else
        log.info(String.format("Deleted file: %s", file.getName()));

    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Helper to safely get a logger (DreamAPI plugin or fallback).
   */
  private static Logger getLogger() {
    try {
      return DreamContext.getPlugin().getLogger();
    } catch (Exception ignored) {}
    return Logger.getLogger("DreamAPI-NBTDownloader");
  }


}