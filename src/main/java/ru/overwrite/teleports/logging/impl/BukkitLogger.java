package ru.overwrite.teleports.logging.impl;

import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.logging.Logger;

public class BukkitLogger implements Logger {

    private final java.util.logging.Logger logger;

    public BukkitLogger(OvTeleportAddon plugin) {
        this.logger = plugin.getLogger();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warning(msg);
    }

}
