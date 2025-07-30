package ru.overwrite.teleports.utils.color;

import ru.overwrite.teleports.utils.Utils;

public class VanillaColorizer implements Colorizer {

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return Utils.translateAlternateColorCodes('&', message);
    }
}
