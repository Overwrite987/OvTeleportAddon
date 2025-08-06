package ru.overwrite.teleports;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.utils.Utils;

public class TeleportCancelCommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    private final Config pluginConfig;

    public TeleportCancelCommand(OvTeleportAddon plugin) {
        this.teleportManager = plugin.getTeleportManager();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        TeleportTask task = teleportManager.getActiveTask(player.getName());
        if (task != null) {
            task.cancel();
            Utils.sendMessage(pluginConfig.getMessages().cancelled(), player);
        }
        return true;
    }
}
