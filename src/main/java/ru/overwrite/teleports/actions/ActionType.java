package ru.overwrite.teleports.actions;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.teleports.OvTeleportAddon;

public interface ActionType extends Keyed {

    @NotNull Action instance(@NotNull String context, @NotNull OvTeleportAddon plugin);

}
