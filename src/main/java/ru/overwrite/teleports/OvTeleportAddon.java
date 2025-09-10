package ru.overwrite.teleports;

import com.earth2me.essentials.Essentials;
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
import ru.overwrite.teleports.commands.TeleportAddonCommand;
import ru.overwrite.teleports.commands.TeleportCancelCommand;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.listeners.*;
import ru.overwrite.teleports.logging.Logger;
import ru.overwrite.teleports.logging.impl.BukkitLogger;
import ru.overwrite.teleports.logging.impl.PaperLogger;
import ru.overwrite.teleports.utils.Utils;
import ru.overwrite.teleports.utils.VersionUtils;

@Getter
public final class OvTeleportAddon extends JavaPlugin {

    @Getter(AccessLevel.NONE)
    private final Server server = getServer();

    private final Logger pluginLogger = VersionUtils.SUB_VERSION >= 19 ? new PaperLogger(this) : new BukkitLogger(this);

    private final Config pluginConfig = new Config(this);

    private final TeleportManager teleportManager = new TeleportManager(this);

    private Permission perms;

    private Essentials essentials;

    @Override
    public void onEnable() {
        final FileConfiguration config = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "config.yml");
        final ConfigurationSection mainSettings = config.getConfigurationSection("main_settings");
        Utils.setupColorizer(mainSettings);
        PluginManager pluginManager = server.getPluginManager();
        if (pluginManager.isPluginEnabled("Vault")) {
            ServicesManager servicesManager = server.getServicesManager();
            setupPerms(servicesManager);
        }
        setupPapi(mainSettings, pluginManager);
        essentials = (Essentials) pluginManager.getPlugin("Essentials");
        pluginConfig.setupConfig(config);
        registerEvents(pluginManager);
        getCommand("canceltp").setExecutor(new TeleportCancelCommand(this));
        getCommand("ovteleportaddon").setExecutor(new TeleportAddonCommand(this));
        if (mainSettings.getBoolean("enable_metrics")) {
            new Metrics(this, 26709);
        }
        checkForUpdates(mainSettings);
    }

    public void registerEvents(PluginManager pluginManager) {
        if (pluginConfig.getMainSettings().applyToSpawn()) {
            pluginManager.registerEvents(new SpawnListener(this), this);
        }
        if (pluginConfig.getMainSettings().applyToTpa()) {
            pluginManager.registerEvents(new TpaListener(this), this);
        }
        if (pluginConfig.getMainSettings().applyToWarp()) {
            pluginManager.registerEvents(new WarpListener(this), this);
        }
        if (pluginConfig.getMainSettings().applyToHome()) {
            pluginManager.registerEvents(new HomeListener(this), this);
        }
        pluginManager.registerEvents(new TeleportListener(this), this);
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

    private void setupPapi(ConfigurationSection mainSettings, PluginManager pluginManager) {
        if (!mainSettings.getBoolean("papi_support", true) || !pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return;
        }
        Utils.USE_PAPI = true;
        pluginLogger.info("§eПлейсхолдеры подключены!");
    }

    @Override
    public void onDisable() {
        teleportManager.cancelAllTasks();
        server.getScheduler().cancelTasks(this);
    }
}
