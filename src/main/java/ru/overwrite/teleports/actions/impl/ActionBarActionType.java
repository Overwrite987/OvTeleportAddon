package ru.overwrite.teleports.actions.impl;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.actions.ActionType;
import ru.overwrite.teleports.utils.Utils;

public final class ActionBarActionType implements ActionType {

    private static final Key KEY = Key.key("ovrandomteleport:actionbar");

    @Override
    public @NotNull Action instance(@NotNull String context, @NotNull OvTeleportAddon plugin) {
        String text = Utils.COLORIZER.colorize(context);
        return new ActionBarAction(text);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    private record ActionBarAction(@NotNull String message) implements Action {
        @Override
        public void perform(@NotNull Player player, @NotNull String[] searchList, @NotNull String[] replacementList) {
            player.sendActionBar(Utils.replaceEach(message, searchList, replacementList));
        }
    }
}
