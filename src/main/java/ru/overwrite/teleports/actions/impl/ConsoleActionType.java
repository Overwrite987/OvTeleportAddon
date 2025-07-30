package ru.overwrite.teleports.actions.impl;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.actions.ActionType;
import ru.overwrite.teleports.utils.Utils;

public final class ConsoleActionType implements ActionType {

    private static final Key KEY = Key.key("ovrandomteleport:console");

    @Override
    public @NotNull Action instance(@NotNull String context, @NotNull OvTeleportAddon plugin) {
        return new ConsoleAction(context);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    private record ConsoleAction(@NotNull String command) implements Action {
        @Override
        public void perform(@NotNull Player player, @NotNull String[] searchList, @NotNull String[] replacementList) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.replaceEach(command, searchList, replacementList));
        }
    }
}
