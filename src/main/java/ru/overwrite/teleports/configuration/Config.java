package ru.overwrite.teleports.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.configuration.data.MainSettings;
import ru.overwrite.teleports.configuration.data.Messages;
import ru.overwrite.teleports.configuration.data.Settings;
import ru.overwrite.teleports.utils.Utils;

import java.io.File;

@Getter
public class Config {

    @Getter(AccessLevel.NONE)
    private final OvTeleportAddon plugin;

    public Config(OvTeleportAddon plugin) {
        this.plugin = plugin;
    }

    private Settings template;

    private Settings spawnSettings, tpaSettings, warpSettings;

    public static String timeHours, timeMinutes, timeSeconds;

    public void setupConfig(FileConfiguration config) {
        setupMainSettings(config.getConfigurationSection("main_settings"));
        template = Settings.create(plugin, config, this, null, false);
        spawnSettings = Settings.create(plugin, getFile(plugin.getDataFolder().getAbsolutePath(), "spawn.yml"), this, template, true);
        tpaSettings = Settings.create(plugin, getFile(plugin.getDataFolder().getAbsolutePath(), "tpa.yml"), this, template, true);
        warpSettings = Settings.create(plugin, getFile(plugin.getDataFolder().getAbsolutePath(), "warp.yml"), this, template, true);
        setupMessages(config.getConfigurationSection("messages"));
    }

    private MainSettings mainSettings;

    private void setupMainSettings(ConfigurationSection mainSettings) {
        this.mainSettings = new MainSettings(
                mainSettings.getInt("invulnerable_after_teleport", 12),
                mainSettings.getBoolean("apply_to_spawn", true),
                mainSettings.getBoolean("apply_to_tpa", true),
                mainSettings.getBoolean("apply_to_warp", true)
        );
    }

    public boolean isNullSection(ConfigurationSection section) {
        return section == null;
    }

    private String messagesPrefix;

    private Messages messages;

    private void setupMessages(ConfigurationSection messages) {

        messagesPrefix = Utils.COLORIZER.colorize(messages.getString("prefix", "messages.prefix"));

        this.messages = new Messages(
                getPrefixed(messages.getString("moved_on_teleport", "messages.moved_on_teleport"), messagesPrefix),
                getPrefixed(messages.getString("teleported_on_teleport", "messages.teleported_on_teleport"), messagesPrefix),
                getPrefixed(messages.getString("damaged_on_teleport", "messages.damaged_on_teleport"), messagesPrefix),
                getPrefixed(messages.getString("damaged_other_on_teleport", "messages.damaged_other_on_teleport"), messagesPrefix),
                getPrefixed(messages.getString("warp_not_found", "messages.warp_not_found"), messagesPrefix),
                getPrefixed(messages.getString("cancelled", "messages.cancelled"), messagesPrefix),
                getPrefixed(messages.getString("reload", "messages.reload"), messagesPrefix),
                getPrefixed(messages.getString("no_perms", "messages.no_perms"), messagesPrefix)
        );

        final ConfigurationSection time = messages.getConfigurationSection("placeholders.time");
        timeHours = Utils.COLORIZER.colorize(time.getString("hours", " ч."));
        timeMinutes = Utils.COLORIZER.colorize(time.getString("minutes", " мин."));
        timeSeconds = Utils.COLORIZER.colorize(time.getString("seconds", " сек."));
    }

    public String getPrefixed(String message, String prefix) {
        if (message == null || prefix == null) {
            return message;
        }
        return Utils.COLORIZER.colorize(message.replace("%prefix%", prefix));
    }

    public FileConfiguration getFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

}
