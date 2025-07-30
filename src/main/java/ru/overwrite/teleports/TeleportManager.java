package ru.overwrite.teleports;

import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.actions.ActionRegistry;
import ru.overwrite.teleports.actions.impl.*;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.configuration.data.Particles;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Getter
public final class TeleportManager {

    @Getter(AccessLevel.NONE)
    private final OvTeleportAddon plugin;
    @Getter(AccessLevel.NONE)
    private final Config pluginConfig;

    private final ActionRegistry actionRegistry;

    private final Map<String, TeleportTask> perPlayerActiveTeleportTask = new IdentityHashMap<>();

    public TeleportManager(OvTeleportAddon plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.actionRegistry = new ActionRegistry(plugin);
        this.registerDefaultActions();
    }

    private void registerDefaultActions() {
        actionRegistry.register(new ActionBarActionType());
        actionRegistry.register(new ConsoleActionType());
        actionRegistry.register(new EffectActionType());
        actionRegistry.register(new MessageActionType());
        actionRegistry.register(new PlayerActionType());
        actionRegistry.register(new SoundActionType());
        actionRegistry.register(new TitleActionType());
    }

    public boolean hasActiveTasks(String playerName) {
        return !perPlayerActiveTeleportTask.isEmpty() && perPlayerActiveTeleportTask.containsKey(playerName);
    }

    public void preTeleport(Player requester, Player requestee, Location loc) {
        int channelPreTeleportCooldown = getCooldown(requester, pluginConfig.getCooldown().defaultPreTeleportCooldown(), pluginConfig.getCooldown().preTeleportCooldowns());
        if (channelPreTeleportCooldown > 0) {
            executeActions(requester, requestee, channelPreTeleportCooldown, pluginConfig.getActions().preTeleportActions());
            TeleportTask teleportTask = new TeleportTask(plugin, this, pluginConfig, requester, requestee, channelPreTeleportCooldown);
            teleportTask.startPreTeleportTimer(loc);
            return;
        }
        teleportPlayer(requester, requestee, loc);
    }

    public void teleportPlayer(Player requester, Player requestee, Location loc) {
        if (pluginConfig.getInvulnerableAfterTeleport() > 0) {
            requester.setInvulnerable(true);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> requester.setInvulnerable(false), pluginConfig.getInvulnerableAfterTeleport());
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            requester.teleport(loc);
            this.spawnParticleSphere(requester, pluginConfig.getParticles());
            this.executeActions(requester, requestee, 0, pluginConfig.getActions().afterTeleportActions());
        });
    }

    public void spawnParticleSphere(Player player, Particles particles) {
        if (!particles.afterTeleportEnabled()) {
            return;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            final Location loc = player.getLocation();
            loc.add(0, 1, 0);
            final World world = loc.getWorld();

            final double goldenAngle = Math.PI * (3 - Math.sqrt(5));

            final List<Player> receivers = particles.afterTeleportSendOnlyToPlayer() ? List.of(player) : null;

            for (int i = 0; i < particles.afterTeleportCount(); i++) {
                double yOffset = 1 - (2.0 * i) / (particles.afterTeleportCount() - 1);
                double radiusAtHeight = Math.sqrt(1 - yOffset * yOffset);

                double theta = goldenAngle * i;

                double afterTeleportRadius = particles.afterTeleportRadius();

                double xOffset = afterTeleportRadius * radiusAtHeight * Math.cos(theta);
                double zOffset = afterTeleportRadius * radiusAtHeight * Math.sin(theta);

                Location particleLocation = loc.clone().add(xOffset, yOffset * afterTeleportRadius, zOffset);

                world.spawnParticle(
                        particles.afterTeleportParticle().particle(),
                        receivers,
                        player,
                        particleLocation.getX(),
                        particleLocation.getY(),
                        particleLocation.getZ(),
                        1,
                        0,
                        0,
                        0,
                        particles.afterTeleportParticleSpeed(),
                        particles.afterTeleportParticle().dustOptions());
            }
        }, 1L);
    }

    public int getCooldown(Player player, int defaultCooldown, Object2IntSortedMap<String> groupCooldowns) {
        if (defaultCooldown < 0) {
            return -1;
        }
        if (groupCooldowns.isEmpty()) {
            return defaultCooldown;
        }
        final String playerGroup = plugin.getPerms().getPrimaryGroup(player);
        return groupCooldowns.getOrDefault(playerGroup, defaultCooldown);
    }

    @Getter(AccessLevel.NONE)
    private final String[] searchList = {"%teleporting_player%", "%player_teleport_to%", "%time%"};

    public void executeActions(Player teleportingPlayer, Player playerTeleportTo, int cooldown, List<Action> actionList) {
        if (actionList.isEmpty()) {
            return;
        }
        final String[] replacementList = {teleportingPlayer.getName(), playerTeleportTo.getName(), Integer.toString(cooldown)};
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Action action : actionList) {
                action.perform(teleportingPlayer, searchList, replacementList);
            }
        });
    }

    public void cancelAllTasks() {
        for (TeleportTask task : perPlayerActiveTeleportTask.values()) {
            task.cancel();
        }
    }
}
