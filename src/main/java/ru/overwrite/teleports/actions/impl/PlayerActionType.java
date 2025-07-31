package ru.overwrite.teleports.actions.impl;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.actions.ActionType;

public final class PlayerActionType implements ActionType {

    private static final Key KEY = Key.key("ovteleportaddon:player");

    @Override
    public @NotNull Action instance(@NotNull String context, @NotNull OvTeleportAddon plugin) {
        return new PlayerAction(context);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    private record PlayerAction(@NotNull String command) implements Action {
        @Override
        public void perform(@NotNull Player player, @NotNull String[] searchList, @NotNull String[] replacementList) {
            player.chat("/" + command);
        }
    }
}
