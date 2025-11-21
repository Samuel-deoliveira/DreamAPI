package fr.dreamin.dreamapi.plugin.cmd.admin.broadcast;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import fr.dreamin.dreamapi.plugin.DreamPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class AdminBroadcastCmd {

  private final @NotNull DreamPlugin dreamPlugin;

  @CommandDescription("Broadcast cmd")
  @CommandMethod("broadcast <msg>")
  @CommandPermission("dreamapi.cmd.broadcast")
  private void broadcast(
    final @NotNull CommandSender sender,
    @Argument("msg") @Greedy String msg
  ) {
    this.dreamPlugin.getBroadcastContext().send(msg);
  }

}
