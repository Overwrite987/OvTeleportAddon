package ru.overwrite.teleports.configuration.data;

import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;

public record Cooldown(
        int defaultPreTeleportCooldown,
        Object2IntSortedMap<String> preTeleportCooldowns) {
}
