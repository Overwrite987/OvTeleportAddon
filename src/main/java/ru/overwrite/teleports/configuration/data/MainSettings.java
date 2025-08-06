package ru.overwrite.teleports.configuration.data;

public record MainSettings(
        int invulnerableAfterTeleport,
        boolean applyToSpawn,
        boolean applyToTpa,
        boolean applyToWarp,
        boolean applyToHome) {
}
