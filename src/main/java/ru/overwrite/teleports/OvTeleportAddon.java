package ru.overwrite.teleports;

import lombok.AccessLevel;
import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.utils.Utils;
import ru.overwrite.teleports.utils.VersionUtils;
import ru.overwrite.teleports.utils.logging.BukkitLogger;
import ru.overwrite.teleports.utils.logging.Logger;
import ru.overwrite.teleports.utils.logging.PaperLogger;

@Getter
public final class OvTeleportAddon extends JavaPlugin {

    @Getter(AccessLevel.NONE)
    private final Server server = getServer();

    private final Logger pluginLogger = VersionUtils.SUB_VERSION >= 19 ? new PaperLogger(this) : new BukkitLogger(this);

    private final Config pluginConfig = new Config(this);

    private final TeleportManager teleportManager = new TeleportManager(this);

    private Permission perms;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final FileConfiguration config = getConfig();
        final ConfigurationSection mainSettings = config.getConfigurationSection("main_settings");
        Utils.setupColorizer(mainSettings);
        PluginManager pluginManager = server.getPluginManager();
        if (pluginManager.isPluginEnabled("Vault")) {
            ServicesManager servicesManager = server.getServicesManager();
            setupPerms(servicesManager);
        }
        pluginConfig.setupConfig(config);
        pluginManager.registerEvents(new TeleportListener(this), this);
        getCommand("canceltp").setExecutor(new TeleportCancelCommand(this));
        if (mainSettings.getBoolean("enable_metrics")) {
            new Metrics(this, 26709);
        }
        checkForUpdates(mainSettings);
    }

    public void checkForUpdates(ConfigurationSection mainSettings) {
        if (!mainSettings.getBoolean("update_checker", true)) {
            return;
        }
        Utils.checkUpdates(this, version -> {
            pluginLogger.info("§6========================================");
            if (getDescription().getVersion().equals(version)) {
                pluginLogger.info("§aВы используете последнюю версию плагина!");
            } else {
                pluginLogger.info("§aВы используете устаревшую плагина!");
                pluginLogger.info("§aВы можете скачать новую версию здесь:");
                pluginLogger.info("§bgithub.com/Overwrite987/OvTeleportAddon/releases/");
                pluginLogger.info("");
            }
            pluginLogger.info("§6========================================");
        });
    }

    private void setupPerms(ServicesManager servicesManager) {
        perms = getPermissionProvider(servicesManager);
        if (perms != null) {
            pluginLogger.info("§aМенеджер прав подключён!");
        }
    }

    private Permission getPermissionProvider(ServicesManager servicesManager) {
        final RegisteredServiceProvider<Permission> provider = servicesManager.getRegistration(Permission.class);
        return provider != null ? provider.getProvider() : null;
    }

    @Override
    public void onDisable() {
        teleportManager.cancelAllTasks();
        server.getScheduler().cancelTasks(this);
    }
}
