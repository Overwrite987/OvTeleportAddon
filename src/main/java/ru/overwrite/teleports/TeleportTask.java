package ru.overwrite.teleports;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.animations.impl.BasicAnimation;
import ru.overwrite.teleports.configuration.data.Actions;
import ru.overwrite.teleports.configuration.data.Bossbar;
import ru.overwrite.teleports.configuration.data.Settings;
import ru.overwrite.teleports.utils.Utils;

import java.util.List;

@RequiredArgsConstructor
public class TeleportTask {

    private final OvTeleportAddon plugin;
    private final TeleportManager teleportManager;
    private final Player teleportingPlayer;
    private final String teleportTo;
    private final int finalPreTeleportCooldown;
    @Getter
    private final Settings settings;

    private int preTeleportCooldown;
    private BossBar bossBar;
    private BukkitTask countdownTask;
    private BukkitTask animationTask;

    public void startPreTeleportTimer(Location location) {
        this.preTeleportCooldown = this.finalPreTeleportCooldown;
        if (settings.bossbar().bossbarEnabled()) {
            this.setupBossBar(settings.bossbar());
        }
        if (settings.particles().preTeleportEnabled()) {
            this.animationTask = new BasicAnimation(this.teleportingPlayer, preTeleportCooldown * 20, settings.particles()).runTaskTimerAsynchronously(plugin, 0, 1);
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
        teleportManager.addActiveTask(this.teleportingPlayer.getName(), this);
    }

    private void setupBossBar(Bossbar bossbar) {
        String title = Utils.COLORIZER.colorize(bossbar.bossbarTitle().replace("%time%", Utils.getTime(finalPreTeleportCooldown)));
        this.bossBar = Bukkit.createBossBar(title, bossbar.bossbarColor(), bossbar.bossbarStyle());
        this.bossBar.addPlayer(this.teleportingPlayer);
        if (bossbar.smoothProgress()) {
            new BukkitRunnable() {
                final int totalTicks = finalPreTeleportCooldown * 20;
                int ticksLeft = totalTicks;

                @Override
                public void run() {
                    if (ticksLeft <= 0) {
                        this.cancel();
                        return;
                    }
                    ticksLeft--;
                    double progress = (double) ticksLeft / totalTicks;
                    if (progress < 0) {
                        progress = 0;
                    }
                    bossBar.setProgress(progress);

                }
            }.runTaskTimerAsynchronously(plugin, 0L, 0L);
        }
    }

    private void cleanupAndTeleport(Location location) {
        if (bossBar != null) {
            bossBar.removeAll();
        }
        teleportManager.teleportPlayer(this.teleportingPlayer, this.teleportTo, location, settings);
        this.cancel();
    }

    private void updateBossBar() {
        if (bossBar == null) {
            return;
        }
        if (!this.settings.bossbar().smoothProgress()) {
            double progress = (double) preTeleportCooldown / finalPreTeleportCooldown;
            if (progress < 1 && progress > 0) {
                bossBar.setProgress(progress);
            }
        }
        String title = Utils.COLORIZER.colorize(settings.bossbar().bossbarTitle().replace("%time%", Utils.getTime(preTeleportCooldown)));
        bossBar.setTitle(title);
    }

    private void handleCooldownActions() {
        Actions actions = settings.actions();
        if (actions.onCooldownActions().isEmpty()) {
            return;
        }
        List<Action> actionList = actions.onCooldownActions().get(preTeleportCooldown);
        if (actionList != null) {
            teleportManager.executeActions(this.teleportingPlayer, this.teleportTo, finalPreTeleportCooldown, actionList);
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
        teleportManager.removeActiveTask(this.teleportingPlayer.getName());
    }
}
