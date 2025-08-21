package ru.overwrite.teleports.actions.impl;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.actions.ActionType;

import java.util.Objects;

public final class DelayedActionActionType implements ActionType {

    private static final Key KEY = Key.key("ovteleportaddon:delayed_action");

    @Override
    public @NotNull Action instance(@NotNull String context, @NotNull OvTeleportAddon plugin) {
        int spaceIndex = context.indexOf(' ');
        long delay = Integer.parseInt(context.substring(0, spaceIndex));
        Action action = Objects.requireNonNull(
                plugin.getTeleportManager().getActionRegistry().resolveAction(context.substring(spaceIndex + 1)),
                "Type doesn't exist");
        return new DelayedActionAction(
                plugin,
                delay,
                action);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    private record DelayedActionAction(
            @NotNull OvTeleportAddon plugin,
            long delay,
            @NotNull Action action
    ) implements Action {
        @Override
        public void perform(@NotNull Player player, @NotNull String[] searchList, @NotNull String[] replacementList) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> action.perform(player, searchList, replacementList), delay);
        }
    }
}