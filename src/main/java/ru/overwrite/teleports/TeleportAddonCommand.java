package ru.overwrite.teleports;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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
        final FileConfiguration config = pluginConfig.getFile(plugin.getDataFolder().getAbsolutePath(), "config.yml");
        pluginConfig.setupConfig(config);
        sender.sendMessage(pluginConfig.getMessages().reload());
        return true;
    }
}
