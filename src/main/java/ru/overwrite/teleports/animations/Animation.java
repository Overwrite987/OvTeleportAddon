package ru.overwrite.teleports.animations;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.overwrite.teleports.configuration.data.Particles;

public abstract class Animation extends BukkitRunnable {

    protected final Player player;
    protected final int duration;
    protected final Particles particles;

    protected Animation(Player player, int duration, Particles particles) {
        this.player = player;
        this.duration = duration;
        this.particles = particles;
    }

}
