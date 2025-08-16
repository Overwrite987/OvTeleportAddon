package ru.overwrite.teleports.configuration.data;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public record Bossbar(
        boolean bossbarEnabled,
        String bossbarTitle,
        BarColor bossbarColor,
        BarStyle bossbarStyle,
        boolean smoothProgress) {
}
