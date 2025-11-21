package fr.dreamin.dreamapi.core.luckperms;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LuckPermsService {

  boolean isEnabled();

  void setEnabled(boolean enabled);

  Component getMsgKickError();

  void setMsgKickError(Component msgKickError);

  CompletableFuture<String> getPrimaryGroup(final @NotNull UUID uuid);

  CompletableFuture<String> getPrefix(final @NotNull UUID uuid);

  CompletableFuture<String> getSuffix(final @NotNull UUID uuid);

  CompletableFuture<Void> setPermission(final @NotNull UUID uuid, final @NotNull String permission, final boolean value);

  CompletableFuture<Void> removePermission(final @NotNull UUID uuid, final @NotNull String permission);

  CompletableFuture<Void> applyTemporaryPermission(final @NotNull UUID uuid, final @NotNull String permission, final boolean value, final @NotNull Duration duration);

  CompletableFuture<Void> schedulePermissionChange(final @NotNull UUID uuid, final @NotNull Runnable action, final @NotNull Duration delay);

  void refreshPlayerVisuals(final @NotNull Player player);

}
