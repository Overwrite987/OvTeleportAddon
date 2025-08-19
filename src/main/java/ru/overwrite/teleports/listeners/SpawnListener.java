package ru.overwrite.teleports.listeners;

import net.essentialsx.api.v2.events.UserTeleportSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.TeleportManager;
import ru.overwrite.teleports.configuration.Config;

public class SpawnListener implements Listener {

    private final TeleportManager teleportManager;
    private final Config pluginConfig;

    public SpawnListener(OvTeleportAddon plugin) {
        this.teleportManager = plugin.getTeleportManager();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(UserTeleportSpawnEvent e) {
        Player player = e.getUser().getBase();
        if (teleportManager.hasActiveTasks(player.getName())) {
            e.setCancelled(true);
            return;
        }
        teleportManager.preTeleport(player, "spawn", e.getSpawnLocation(), pluginConfig.getSpawnSettings());
        e.setCancelled(true);
    }
}
