package ru.overwrite.teleports;

import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
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
import ru.overwrite.teleports.configuration.data.Settings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class TeleportManager {

    @Getter(AccessLevel.NONE)
    private final OvTeleportAddon plugin;
    @Getter(AccessLevel.NONE)
    private final Config pluginConfig;

    private final ActionRegistry actionRegistry;
    private final ReferenceList<String> tpaHerePlayers = new ReferenceArrayList<>();

    @Getter(AccessLevel.NONE)
    private final Map<String, TeleportTask> perPlayerActiveTeleportTask = new ConcurrentHashMap<>();

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

    public TeleportTask getActiveTask(String playerName) {
        if (perPlayerActiveTeleportTask.isEmpty()) {
            return null;
        }
        return perPlayerActiveTeleportTask.get(playerName);
    }

    public void addActiveTask(String playerName, TeleportTask teleportTask) {
        perPlayerActiveTeleportTask.put(playerName, teleportTask);
    }

    public void removeActiveTask(String playerName) {
        if (perPlayerActiveTeleportTask.isEmpty()) {
            return;
        }
        perPlayerActiveTeleportTask.remove(playerName);
    }

    public void preTeleport(Player requester, String teleportTo, Location loc, Settings settings) {
        int channelPreTeleportCooldown = getCooldown(requester, settings.cooldown().defaultPreTeleportCooldown(), settings.cooldown().preTeleportCooldowns());
        if (channelPreTeleportCooldown > 0) {
            executeActions(requester, teleportTo, channelPreTeleportCooldown, settings.actions().preTeleportActions());
            TeleportTask teleportTask = new TeleportTask(plugin, this, requester, teleportTo, channelPreTeleportCooldown, settings);
            teleportTask.startPreTeleportTimer(loc);
            return;
        }
        teleportPlayer(requester, teleportTo, loc, settings);
    }

    public void teleportPlayer(Player requester, String teleportTo, Location loc, Settings settings) {
        if (pluginConfig.getMainSettings().invulnerableAfterTeleport() > 0) {
            requester.setInvulnerable(true);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> requester.setInvulnerable(false), pluginConfig.getMainSettings().invulnerableAfterTeleport());
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            requester.teleport(loc);
            this.spawnParticleSphere(requester, settings.particles());
            this.executeActions(requester, teleportTo, 0, settings.actions().afterTeleportActions());
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

    public void executeActions(Player teleportingPlayer, String teleportTo, int cooldown, List<Action> actionList) {
        if (actionList.isEmpty()) {
            return;
        }
        final String[] replacementList = {teleportingPlayer.getName(), teleportTo, Integer.toString(cooldown)};
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
