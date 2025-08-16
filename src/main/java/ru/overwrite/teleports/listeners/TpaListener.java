package ru.overwrite.teleports.listeners;

import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import net.ess3.api.events.TPARequestEvent;
import net.essentialsx.api.v2.events.TeleportRequestResponseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.TeleportManager;
import ru.overwrite.teleports.configuration.Config;

public class TpaListener implements Listener {

    private final TeleportManager teleportManager;
    private final Config pluginConfig;

    public TpaListener(OvTeleportAddon plugin) {
        this.teleportManager = plugin.getTeleportManager();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleportRequest(TPARequestEvent e) {
        if (!pluginConfig.getMainSettings().applyToTpa()) {
            return;
        }
        if (e.isTeleportHere()) {
            // Дамы и господа. Это был самый УЕБАНСКИЙ костыль, который я когда-либо делал в своей сука жизни.
            teleportManager.getTpaHerePlayers().add(e.getRequester().getPlayer().getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTpa(TeleportRequestResponseEvent e) {
        if (!pluginConfig.getMainSettings().applyToTpa()) {
            return;
        }
        if (!e.isAccept()) {
            return;
        }
        User requester = (User) e.getRequester();
        User requestee = (User) e.getRequestee();
        IUser.TpaRequest request = e.getTpaRequest();
        if (teleportManager.getTpaHerePlayers().contains(request.getName())) {
            teleportManager.getTpaHerePlayers().remove(request.getName());
            requester = (User) e.getRequestee();
            requestee = (User) e.getRequester();
        }
        teleportManager.preTeleport(requester.getBase(), requestee.getName(), request.getLocation(), pluginConfig.getTpaSettings());
        requestee.removeTpaRequest(requester.getName());
        e.setCancelled(true);
    }
}
