package fr.dreamin.dreamapi.core.team;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.DreamContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@DreamAutoService(value = TeamService.class)
public class TeamServiceImpl implements TeamService, DreamService {

  private final @NotNull Scoreboard scoreboard;

  public TeamServiceImpl() {
    this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager().getMainScoreboard());
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public @NotNull Team createOrGetTeam(@NotNull Entity entity) {
    final var key = keyFor(entity.getUniqueId());
    var team = this.scoreboard.getTeam(key);

    if (team == null)
      team = this.scoreboard.registerNewTeam(key);

    if ((!team.hasEntity(entity))) team.addEntity(entity);
    return team;
  }

  @Override
  public void removeTeam(@NotNull Entity entity) {
    final var key = keyFor(entity.getUniqueId());
    final var team = this.scoreboard.getTeam(key);
    if (team != null) team.unregister();
  }

  @Override
  public void clearAllTeams() {
    for (var team : this.scoreboard.getTeams()) {
      team.unregister();
    }
  }

  @Override
  public @Nullable Team getTeam(@NotNull UUID entityId) {
    return this.scoreboard.getTeam(keyFor(entityId));
  }

  @Override
  public @Nullable Team getTeamByName(@NotNull String name) {
    return this.scoreboard.getTeam(name);
  }

  @Override
  public @NotNull Collection<Team> getAllTeams() {
    return Collections.unmodifiableCollection(this.scoreboard.getTeams());
  }

  // ###############################################################
  // --------------------------- MEMBERS ---------------------------
  // ###############################################################

  @Override
  public void addEntityToTeam(@NotNull Team team, @NotNull Entity entity) {
    if (!team.hasEntity(entity)) team.addEntity(entity);
  }

  @Override
  public void removeEntityFromTeam(@NotNull Team team, @NotNull Entity entity) {
    if (team.hasEntity(entity)) team.removeEntity(entity);
  }

  @Override
  public boolean isInTeam(@NotNull Team team, @NotNull Entity entity) {
    return team.hasEntity(entity);
  }

  // ###############################################################
  // --------------------- STYLE & VISIBILITY ----------------------
  // ###############################################################

  @Override
  public void setNametagVisible(@NotNull Team team, boolean visible) {
    team.setOption(Team.Option.NAME_TAG_VISIBILITY,
      visible ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
  }

  @Override
  public void setCollision(@NotNull Team team, boolean allowed) {
    team.setOption(Team.Option.COLLISION_RULE,
      allowed ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
  }

  @Override
  public void setFriendlyFire(@NotNull Team team, boolean enabled) {
    team.setAllowFriendlyFire(enabled);
  }

  @Override
  public void setPrefix(@NotNull Team team, @NotNull Component prefix) {
    team.prefix(prefix);
  }

  @Override
  public void setSuffix(@NotNull Team team, @NotNull Component suffix) {
    team.suffix(suffix);
  }

  @Override
  public void setColor(@NotNull Team team, @NotNull NamedTextColor color) {
    team.color(color);
  }

  @Override
  public void updatePlayerDisplay(Player player, String prefix, String suffix) {
    final var team = createOrGetTeam(player);

    final var prefixComponent = (prefix == null || prefix.isBlank()) ?
      Component.empty() :
      DreamContext.LEGACY_COMPONENT_SERIALIZER.deserialize(prefix);

    final var suffixComponent = (suffix == null || suffix.isBlank()) ?
      Component.empty() :
      DreamContext.LEGACY_COMPONENT_SERIALIZER.deserialize(suffix);

    team.prefix(prefixComponent.color(NamedTextColor.WHITE));
    team.suffix(suffixComponent.color(NamedTextColor.WHITE));

    player.setScoreboard(scoreboard);

  }

  // ###############################################################
  // ---------------------------- UTILS ----------------------------
  // ###############################################################

  @Override
  public @NotNull Team createCustomTeam(@NotNull String name) {
    var team = this.scoreboard.getTeam(name);
    if (team == null) {
      team = this.scoreboard.registerNewTeam(name);
      team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }
    return team;
  }

  @Override
  public void removeTeamByName(@NotNull String name) {
    final var team = this.scoreboard.getTeam(name);
    if (team != null) team.unregister();
  }

  @Override
  public void cleanupEmptyTeams() {
    for (var team : this.scoreboard.getTeams()) {
      if (team.getEntries().isEmpty())
        team.unregister();
    }
  }

  @Override
  public boolean exists(@NotNull String name) {
    return this.scoreboard.getTeam(name) != null;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private String keyFor(final @NotNull UUID uuid) {
    return "h" + uuid.toString().replace("-", "").substring(0, 14);
  }

}
