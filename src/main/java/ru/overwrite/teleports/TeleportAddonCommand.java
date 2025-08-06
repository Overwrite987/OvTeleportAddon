package ru.overwrite.teleports;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.teleports.configuration.Config;

public class TeleportAddonCommand implements CommandExecutor {

    private final OvTeleportAddon plugin;
    private final TeleportManager teleportManager;
    private final Config pluginConfig;

    public TeleportAddonCommand(OvTeleportAddon plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("teleportaddon.admin")) {
            sender.sendMessage(pluginConfig.getMessages().noPerms());
            return true;
        }
        teleportManager.cancelAllTasks();
        plugin.reloadConfig();
        pluginConfig.setupConfig(plugin.getConfig());
        sender.sendMessage(pluginConfig.getMessages().reload());
        return true;
    }
}
