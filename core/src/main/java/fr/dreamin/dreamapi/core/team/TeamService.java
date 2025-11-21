package fr.dreamin.dreamapi.core.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface TeamService {

  // ###############################################################
  // ---------------------------- BASE -----------------------------
  // ###############################################################

  @NotNull Team createOrGetTeam(final @NotNull Entity entity);

  void removeTeam(final @NotNull Entity entity);

  void clearAllTeams();

  @Nullable Team getTeam(final @NotNull UUID uuid);

  @Nullable Team getTeamByName(final @NotNull String name);

  @NotNull Collection<Team> getAllTeams();

  // ###############################################################
  // --------------------------- MEMBERS ---------------------------
  // ###############################################################

  void addEntityToTeam(final @NotNull Team team, final @NotNull Entity entity);

  void removeEntityFromTeam(final @NotNull Team team, final @NotNull Entity entity);

  boolean isInTeam(final @NotNull Team team, final @NotNull Entity entity);

  // ###############################################################
  // --------------------- STYLE & VISIBILITY ----------------------
  // ###############################################################

  void setNametagVisible(final @NotNull Team team, final boolean visible);

  void setCollision(final @NotNull Team team, final boolean allowed);

  void setFriendlyFire(final @NotNull Team team, final boolean enabled);

  void setPrefix(final @NotNull Team team, final @NotNull Component prefix);

  void setSuffix(final @NotNull Team team, final @NotNull Component suffix);

  void setColor(final @NotNull Team team, final @NotNull NamedTextColor color);

  // ###############################################################
  // ---------------------------- UTILS ----------------------------
  // ###############################################################

  void updatePlayerDisplay(Player player, String prefix, String suffix);

  @NotNull Team createCustomTeam(final @NotNull String name);

  void removeTeamByName(final @NotNull String name);

  void cleanupEmptyTeams();

  boolean exists(final @NotNull String name);

}
