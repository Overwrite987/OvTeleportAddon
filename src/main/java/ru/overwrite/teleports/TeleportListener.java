package ru.overwrite.teleports;

import com.earth2me.essentials.IUser;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.ess3.api.events.TPARequestEvent;
import net.essentialsx.api.v2.events.TeleportRequestResponseEvent;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.configuration.data.Restrictions;
import ru.overwrite.teleports.utils.Utils;

public class TeleportListener implements Listener {

    private final TeleportManager teleportManager;
    private final Config pluginConfig;
    private final ReferenceList<String> tpaHerePlayers = new ReferenceArrayList<>();

    public TeleportListener(OvTeleportAddon plugin) {
        this.teleportManager = plugin.getTeleportManager();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleportRequest(TPARequestEvent e) {
        if (e.isTeleportHere()) {
            // Дамы и господа. Это был самый УЕБАНСКИЙ костыль, который я когда-либо делал в своей сука жизни.
            tpaHerePlayers.add(e.getRequester().getPlayer().getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleportRequestResponse(TeleportRequestResponseEvent e) {
        if (!e.isAccept()) {
            return;
        }
        Player requester = e.getRequester().getBase();
        Player requestee = e.getRequestee().getBase();
        IUser.TpaRequest request = e.getTpaRequest();
        if (tpaHerePlayers.contains(request.getName())) {
            tpaHerePlayers.remove(request.getName());
            requester = e.getRequestee().getBase();
            requestee = e.getRequester().getBase();
        }
        teleportManager.preTeleport(requester, requestee, request.getLocation());
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) {
            return;
        }
        Player player = e.getPlayer();
        String playerName = player.getName();
        if (teleportManager.hasActiveTasks(playerName)) {
            if (pluginConfig.getRestrictions().restrictMove()) {
                Utils.sendMessage(pluginConfig.getMessages().movedOnTeleport(), player);
                this.cancelTeleportation(playerName);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }
        Player player = e.getPlayer();
        String playerName = player.getName();
        if (teleportManager.hasActiveTasks(playerName)) {
            if (pluginConfig.getRestrictions().restrictTeleport()) {
                Utils.sendMessage(pluginConfig.getMessages().teleportedOnTeleport(), player);
                this.cancelTeleportation(playerName);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        String playerName = player.getName();
        if (teleportManager.hasActiveTasks(playerName)) {
            Restrictions restrictions = pluginConfig.getRestrictions();
            if (restrictions.restrictDamage() && !restrictions.damageCheckOnlyPlayers()) {
                Utils.sendMessage(pluginConfig.getMessages().damagedOnTeleport(), player);
                this.cancelTeleportation(playerName);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damagerEntity = e.getDamager();
        Entity damagedEntity = e.getEntity();

        if (damagerEntity instanceof Player damager) {
            this.handleDamagerPlayer(damager, damagedEntity);
        }
        if (damagedEntity instanceof Player damaged) {
            this.handleDamagedPlayer(damagerEntity, damaged);
        }
    }

    private void handleDamagerPlayer(Player damager, Entity damagedEntity) {
        String damagerName = damager.getName();
        if (teleportManager.hasActiveTasks(damagerName)) {
            Restrictions restrictions = pluginConfig.getRestrictions();
            if (restrictions.restrictDamageOthers()) {
                if (restrictions.damageCheckOnlyPlayers() && !(damagedEntity instanceof Player)) {
                    return;
                }
                Utils.sendMessage(pluginConfig.getMessages().damagedOtherOnTeleport(), damager);
                this.cancelTeleportation(damagerName);
            }
        }
    }

    private void handleDamagedPlayer(Entity damagerEntity, Player damaged) {
        String damagedName = damaged.getName();
        if (teleportManager.hasActiveTasks(damagedName)) {
            Restrictions restrictions = pluginConfig.getRestrictions();
            if (restrictions.restrictDamage()) {
                Player damager = getDamager(damagerEntity);
                if (damager == null && restrictions.damageCheckOnlyPlayers()) {
                    return;
                }
                Utils.sendMessage(pluginConfig.getMessages().damagedOnTeleport(), damaged);
                this.cancelTeleportation(damagedName);
            }
        }
    }

    private Player getDamager(Entity damagerEntity) {
        if (damagerEntity instanceof Player player) {
            return player;
        }
        if (damagerEntity instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
        }
        if (damagerEntity instanceof AreaEffectCloud areaEffectCloud) {
            ProjectileSource source = areaEffectCloud.getSource();
            if (source instanceof Player player) {
                return player;
            }
        }
        if (damagerEntity instanceof TNTPrimed tntPrimed) {
            Entity source = tntPrimed.getSource();
            if (source instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        this.handlePlayerLeave(player);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        this.handlePlayerLeave(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        Player player = e.getPlayer();
        this.handlePlayerLeave(player);
    }

    private void handlePlayerLeave(Player player) {
        String playerName = player.getName();
        if (teleportManager.hasActiveTasks(playerName)) {
            this.cancelTeleportation(playerName);
        }
        tpaHerePlayers.remove(playerName);
    }

    private void cancelTeleportation(String playerName) {
        teleportManager.getPerPlayerActiveTeleportTask().get(playerName).cancel();
    }
}
