package ru.overwrite.teleports;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.overwrite.teleports.animations.BasicAnimation;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.configuration.data.Actions;
import ru.overwrite.teleports.configuration.data.Bossbar;
import ru.overwrite.teleports.utils.Utils;

@RequiredArgsConstructor
public class TeleportTask {

    private final OvTeleportAddon plugin;
    private final TeleportManager teleportManager;
    private final Config pluginConfig;
    private final Player teleportingPlayer;
    private final Player playerTeleportTo;
    private final int finalPreTeleportCooldown;

    private int preTeleportCooldown;
    private BossBar bossBar;
    private BukkitTask countdownTask;
    private BukkitTask animationTask;

    public void startPreTeleportTimer(Location location) {
        this.preTeleportCooldown = this.finalPreTeleportCooldown;
        if (pluginConfig.getBossbar().bossbarEnabled()) {
            this.setupBossBar(pluginConfig.getBossbar());
        }
        if (pluginConfig.getParticles().preTeleportEnabled()) {
            this.animationTask = new BasicAnimation(this.teleportingPlayer, preTeleportCooldown * 20, pluginConfig.getParticles()).runTaskTimerAsynchronously(plugin, 0, 1);
        }
        this.countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                preTeleportCooldown--;
                if (preTeleportCooldown <= 0) {
                    cleanupAndTeleport(location);
                    return;
                }
                updateBossBar();
                handleCooldownActions();
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L);
        teleportManager.getPerPlayerActiveTeleportTask().put(this.teleportingPlayer.getName(), this);
    }

    private void setupBossBar(Bossbar bossbar) {
        String title = Utils.COLORIZER.colorize(bossbar.bossbarTitle().replace("%time%", Utils.getTime(finalPreTeleportCooldown)));
        this.bossBar = Bukkit.createBossBar(title, bossbar.bossbarColor(), bossbar.bossbarStyle());
        this.bossBar.addPlayer(this.teleportingPlayer);
    }

    private void cleanupAndTeleport(Location location) {
        if (bossBar != null) {
            bossBar.removeAll();
        }
        teleportManager.teleportPlayer(this.teleportingPlayer, this.playerTeleportTo, location);
        this.cancel();
    }

    private void updateBossBar() {
        if (bossBar == null) {
            return;
        }
        double progress = (double) preTeleportCooldown / finalPreTeleportCooldown;
        if (progress < 1 && progress > 0) {
            bossBar.setProgress(progress);
        }
        String title = Utils.COLORIZER.colorize(pluginConfig.getBossbar().bossbarTitle().replace("%time%", Utils.getTime(preTeleportCooldown)));
        bossBar.setTitle(title);
    }

    private void handleCooldownActions() {
        Actions actions = pluginConfig.getActions();
        if (actions.onCooldownActions().isEmpty()) {
            return;
        }
        for (int time : actions.onCooldownActions().keySet()) {
            if (time == preTeleportCooldown) {
                teleportManager.executeActions(this.teleportingPlayer, this.playerTeleportTo, finalPreTeleportCooldown, actions.onCooldownActions().get(time));
            }
        }
    }

    public void cancel() {
        if (bossBar != null) {
            bossBar.removeAll();
        }
        if (animationTask != null) {
            animationTask.cancel();
        }
        countdownTask.cancel();
        teleportManager.getPerPlayerActiveTeleportTask().remove(this.teleportingPlayer.getName());
    }
}
