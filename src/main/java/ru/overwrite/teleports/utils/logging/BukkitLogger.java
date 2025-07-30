package ru.overwrite.teleports.utils.logging;

import ru.overwrite.teleports.OvTeleportAddon;

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
