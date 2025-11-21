package fr.dreamin.dreamapi.core.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MojangAPI {

  private static final String PROFILE_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
  private static final String UUID_API_URL = "https://api.mojang.com/users/profiles/minecraft/";

  /**
   * Retrieves the player name from Mojang API based on their UUID.
   *
   * @param uuid The UUID of the player.
   * @return The player's name.
   */
  public static String getPlayerName(final @NotNull String uuid)  {
    try {
      final var url = new URL(PROFILE_API_URL + uuid);
      final var conn = (HttpURLConnection) url.openConnection();

      conn.setRequestMethod("GET");

      final var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      final var builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
      reader.close();

      final var response = builder.toString();
      if (response.isEmpty()) return null;

      final var json = new JsonParser().parse(response).getAsJsonObject();
      if (!json.has("name")) return null;
      return json.get("name").getAsString();
    } catch (IOException e) {
      return null;
    }
  }

  public static String getUUID(final @NotNull String playerName) {
    try {
      final var url = new URL(UUID_API_URL + playerName);
      final var conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");

      final var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      final var builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
      reader.close();

      final var response = builder.toString();
      if (response.isEmpty()) return null;

      final var json = JsonParser.parseString(response).getAsJsonObject();
      return json.get("id").getAsString();
    } catch (IOException e) {
      return null;
    }
  }

  public static SkinProperty getSkinProperty(final @NotNull String uuid) throws Exception {
    final var url = new URL(PROFILE_API_URL + uuid + "?unsigned=false");
    final var conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    final var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    final var builder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    reader.close();

    final var response = builder.toString();
    if (response.isEmpty())
      throw new Exception("Empty response from Mojang.");

    final var json = JsonParser.parseString(response).getAsJsonObject();
    if (json.has("properties")) {
      for (var i = 0; i < json.getAsJsonArray("properties").size(); i++) {
        final var property = json.getAsJsonArray("properties").get(i).getAsJsonObject();
        if ("textures".equals(property.get("name").getAsString())) {
          final var value = property.get("value").getAsString();
          final var signature = property.has("signature") ? property.get("signature").getAsString() : null;
          return new SkinProperty("textures", value, signature);
        }
      }
    }

    throw new Exception("No textures found in Mojang response.");
  }

  public static SkinProperty getSkinPropertyByName(final @NotNull String playerName) throws Exception {
    final var uuid = getUUID(playerName);
    if (uuid == null) throw new Exception("Cannot fetch UUID for player: " + playerName);
    return getSkinProperty(uuid);
  }

  public static List<SkinProperty> getSkinProperties(final @NotNull String uuid) throws Exception {
    final var list = new ArrayList<SkinProperty>();

    final var url = new URL(PROFILE_API_URL + uuid + "?unsigned=false");
    final var conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    final var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    final var builder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    reader.close();

    final var response = builder.toString();
    if (response.isEmpty()) throw new Exception("Empty Mojang response");

    final var json = JsonParser.parseString(response).getAsJsonObject();
    if (json.has("properties")) {
      for (var el : json.getAsJsonArray("properties")) {
        final var prop = el.getAsJsonObject();
        final var name = prop.get("name").getAsString();
        final var value = prop.get("value").getAsString();
        final var signature = prop.has("signature") ? prop.get("signature").getAsString() : null;
        list.add(new SkinProperty(name, value, signature));
      }
    }

    return list;
  }

  /**
   * Retrieves the base64 encoded skin data for a player from their UUID.
   *
   * @param uuid The UUID of the player.
   * @return The base64 encoded skin data.
   * @throws Exception If there is an error while retrieving data from Mojang API.
   */
  public static String getSkinBase64(final @NotNull String uuid) throws Exception {
    final var url = new URL(PROFILE_API_URL + uuid);
    final var conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    final var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    final var builder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    reader.close();

    String response = builder.toString();
    if (response.isEmpty())
      throw new Exception("Response from server is empty.");

    final var json = new JsonParser().parse(response).getAsJsonObject();
    if (json.has("properties")) {
      for (var i = 0; i < json.getAsJsonArray("properties").size(); i++) {
        final var property = json.getAsJsonArray("properties").get(i).getAsJsonObject();
        if ("textures".equals(property.get("name").getAsString())) return property.get("value").getAsString();
      }
    }

    throw new Exception("Cannot find 'textures' value.");
  }

}