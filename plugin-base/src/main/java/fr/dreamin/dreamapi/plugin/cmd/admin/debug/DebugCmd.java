package fr.dreamin.dreamapi.plugin.cmd.admin.debug;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import logger.DebugService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DebugCmd {

  private final DebugService debug = DreamAPI.getAPI().getService(DebugService.class);

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @CommandDescription("Debug control center")
  @CommandMethod("debug")
  @CommandPermission("dreamapi.cmd.debug")
  private void onRoot(CommandSender sender) {
    sender.sendMessage("DreamAPI Debug System. Use /debug info to see settings.");
  }

  @CommandDescription("Show debug configuration")
  @CommandMethod("debug info")
  @CommandPermission("dreamapi.cmd.debug.info")
  private void onInfo(CommandSender sender) {

    final Component[] rs = {Component.newline()
      .append(Component.text("DreamAPI Debug Settings", NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text("Global: " + (this.debug.isGlobalDebug() ? "ON" : "OFF"), NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text("Retention Days: " + this.debug.getRetentionDays(), NamedTextColor.GOLD))
      .appendNewline()};

    if (this.debug.getCategories().isEmpty())
      rs[0] = rs[0].append(Component.text("Categories: none", NamedTextColor.GOLD));
    else {
      rs[0] = rs[0].append(Component.text("Categories: ", NamedTextColor.GOLD));

      this.debug.getCategories().forEach((cat, active) -> {
        rs[0] = rs[0].append(Component.text(String.format(" - %s = %s", cat, (active ? "ON" : "OFF")), NamedTextColor.GOLD));
      });
    }

    rs[0] = rs[0].appendNewline();

    sender.sendMessage(rs[0]);
  }

  @CommandDescription("Enable or disable global debug")
  @CommandMethod("debug global <state>")
  @CommandPermission("dreamapi.cmd.debug.global")
  private void onGlobal(CommandSender sender, @Argument(value = "state", suggestions = "onoff") String state) {
    final var enabled = state.equalsIgnoreCase("on");

    this.debug.setGlobalDebug(enabled);

    sender.sendMessage(Component.text(String.format("Global debug = %s", (enabled ? "ON": "OFF")), NamedTextColor.GOLD));
  }

  @Suggestions("onoff")
  public List<String> suggestOnOff(CommandContext<CommandSender> sender, String input) {
    return List.of("on", "off");
  }

  @CommandDescription("Enable or disable a debug category")
  @CommandMethod("debug category <name> <state>")
  @CommandPermission("dreamapi.cmd.debug.category")
  private void onCategory(
    CommandSender sender,
    @Argument(value = "name", suggestions = "categories") String category,
    @Argument(value = "state", suggestions = "onoff") String state
  ) {
    final var enabled = state.equalsIgnoreCase("on");
    this.debug.setCategory(category, enabled);

    sender.sendMessage(Component.text(String.format("Category %s = %s", category, (enabled ? "ON" : "OFF")), NamedTextColor.GOLD));
  }

  @Suggestions("categories")
  public List<String> suggestCategories(CommandContext<CommandSender> sender, String input) {
    if (this.debug.getCategories().isEmpty()) return List.of();
    return this.debug.getCategories().keySet().stream().toList();
  }

  @CommandDescription("Set how many days worth of logs to retain")
  @CommandMethod("debug retention <days>")
  @CommandPermission("dreamapi.cmd.debug.retention")
  private void onRetention(CommandSender sender, @Argument("days") int days) {
    this.debug.setRetentionDays(days);
    sender.sendMessage(Component.text(String.format("Log retention set to %s days.", days), NamedTextColor.GOLD));
  }

  @CommandDescription("Manually clean up old logs")
  @CommandMethod("debug cleanup")
  @CommandPermission("dreamapi.cmd.debug.cleanup")
  private void onCleanup(CommandSender sender) {
    this.debug.cleanupOldLogs();
    sender.sendMessage(Component.text("Old logs cleaned successfully.", NamedTextColor.GOLD));
  }

}
