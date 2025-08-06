package ru.overwrite.teleports.configuration.data;

public record Messages(
        String movedOnTeleport,
        String teleportedOnTeleport,
        String damagedOnTeleport,
        String damagedOtherOnTeleport,
        String warpNotFound,
        String cancelled,
        String reload,
        String noPerms) {
}
