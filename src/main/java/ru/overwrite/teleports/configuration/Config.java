package ru.overwrite.teleports.configuration;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.configuration.data.*;
import ru.overwrite.teleports.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
public class Config {

    @Getter(AccessLevel.NONE)
    private final OvTeleportAddon plugin;

    public Config(OvTeleportAddon plugin) {
        this.plugin = plugin;
    }

    private int invulnerableAfterTeleport;

    public static String timeHours, timeMinutes, timeSeconds;

    public void setupConfig(FileConfiguration config) {
        invulnerableAfterTeleport = config.getInt("main_settings.invulnerable_after_teleport");
        setupCooldown(config.getConfigurationSection("cooldown"));
        setupBossBar(config.getConfigurationSection("bossbar"));
        setupParticles(config.getConfigurationSection("particles"));
        setupRestrictions(config.getConfigurationSection("restrictions"));
        setupActions(config.getConfigurationSection("actions"));
        setupMessages(config.getConfigurationSection("messages"));
    }

    private Cooldown cooldown;

    private void setupCooldown(ConfigurationSection cooldown) {
        boolean isNullSection = isNullSection(cooldown);
        Object2IntSortedMap<String> preTeleportCooldownsMap = new Object2IntLinkedOpenHashMap<>();
        boolean useLastGroupCooldown = !isNullSection && cooldown.getBoolean("use_last_group_cooldown", false);
        int defaultPreTeleportCooldown = cooldown.getInt("default_pre_teleport_cooldown", -1);
        ConfigurationSection preTeleportGroupCooldowns = cooldown.getConfigurationSection("pre_teleport_group_cooldowns");
        defaultPreTeleportCooldown = processCooldownSection(preTeleportGroupCooldowns, preTeleportCooldownsMap, useLastGroupCooldown, defaultPreTeleportCooldown);
        this.cooldown = new Cooldown(defaultPreTeleportCooldown, preTeleportCooldownsMap);
    }

    private int processCooldownSection(ConfigurationSection section, Object2IntSortedMap<String> map, boolean useLastGroup, int currentDefault) {
        if (plugin.getPerms() != null) {
            for (String groupName : section.getKeys(false)) {
                map.put(groupName, section.getInt(groupName));
            }
            if (!map.isEmpty() && useLastGroup) {
                List<String> keys = new ArrayList<>(map.keySet());
                currentDefault = section.getInt(keys.get(keys.size() - 1));
            }
        }
        return currentDefault;
    }

    private Bossbar bossbar;

    private void setupBossBar(ConfigurationSection bossbar) {
        if (isNullSection(bossbar)) {
            this.bossbar = new Bossbar(false, null, null, null);
        }
        boolean enabled = bossbar.getBoolean("enabled");
        String title = Utils.COLORIZER.colorize(bossbar.getString("title"));
        BarColor color = BarColor.valueOf(bossbar.getString("color").toUpperCase(Locale.ENGLISH));
        BarStyle style = BarStyle.valueOf(bossbar.getString("style").toUpperCase(Locale.ENGLISH));

        this.bossbar = new Bossbar(enabled, title, color, style);
    }

    private Particles particles;

    private void setupParticles(ConfigurationSection particles) {
        if (isNullSection(particles)) {
            this.particles = new Particles(
                    false, false, null, -1, -1, -1, -1, false, false, false,
                    false, false, null, -1, -1, -1);
        }
        boolean preTeleportEnabled = false;
        boolean preTeleportSendOnlyToPlayer = false;
        List<Particles.ParticleData> preTeleportParticles = null;
        int preTeleportDots = 0;
        double preTeleportRadius = 0;
        double preTeleportParticleSpeed = 0;
        double preTeleportSpeed = 0;
        boolean preTeleportInvert = false;
        boolean preTeleportJumping = false;
        boolean preTeleportMoveNear = false;
        boolean afterTeleportParticleEnabled = false;
        boolean afterTeleportSendOnlyToPlayer = false;
        Particles.ParticleData afterTeleportParticle = null;
        int afterTeleportCount = 0;
        double afterTeleportRadius = 0;
        double afterTeleportParticleSpeed = 0;
        final ConfigurationSection preTeleport = particles.getConfigurationSection("pre_teleport");
        if (!isNullSection(preTeleport)) {
            preTeleportEnabled = preTeleport.getBoolean("enabled", false);
            preTeleportSendOnlyToPlayer = preTeleport.getBoolean("send_only_to_player", false);
            if (preTeleport.contains("id")) {
                ImmutableList.Builder<Particles.ParticleData> builder = ImmutableList.builder();
                for (String particleId : preTeleport.getStringList("id")) {
                    builder.add(Utils.createParticleData(particleId));
                }
                preTeleportParticles = builder.build();
            }
            preTeleportDots = preTeleport.getInt("dots");
            preTeleportRadius = preTeleport.getDouble("radius");
            preTeleportParticleSpeed = preTeleport.getDouble("particle_speed");
            preTeleportSpeed = preTeleport.getDouble("speed");
            preTeleportInvert = preTeleport.getBoolean("invert");
            preTeleportJumping = preTeleport.getBoolean("jumping");
            preTeleportMoveNear = preTeleport.getBoolean("move_near");
        }
        final ConfigurationSection afterTeleport = particles.getConfigurationSection("after_teleport");
        if (!isNullSection(afterTeleport)) {
            afterTeleportParticleEnabled = afterTeleport.getBoolean("enabled", false);
            afterTeleportSendOnlyToPlayer = afterTeleport.getBoolean("send_only_to_player", false);
            if (afterTeleport.contains("id")) {
                afterTeleportParticle = Utils.createParticleData(afterTeleport.getString("id"));
            }
            afterTeleportCount = afterTeleport.getInt("count");
            afterTeleportRadius = afterTeleport.getDouble("radius");
            afterTeleportParticleSpeed = afterTeleport.getDouble("particle_speed");
        }

        this.particles = new Particles(
                preTeleportEnabled, preTeleportSendOnlyToPlayer, preTeleportParticles, preTeleportDots, preTeleportRadius, preTeleportParticleSpeed, preTeleportSpeed, preTeleportInvert, preTeleportJumping, preTeleportMoveNear,
                afterTeleportParticleEnabled, afterTeleportSendOnlyToPlayer, afterTeleportParticle, afterTeleportCount, afterTeleportRadius, afterTeleportParticleSpeed);
    }

    private Restrictions restrictions;

    private void setupRestrictions(ConfigurationSection restrictions) {
        boolean isNullSection = isNullSection(restrictions);
        boolean restrictMove = !isNullSection && restrictions.getBoolean("move", false);
        boolean restrictTeleport = !isNullSection && restrictions.getBoolean("teleport", false);
        boolean restrictDamage = !isNullSection && restrictions.getBoolean("damage", false);
        boolean restrictDamageOthers = !isNullSection && restrictions.getBoolean("damage_others", false);
        boolean damageCheckOnlyPlayers = !isNullSection && restrictions.getBoolean("damage_check_only_players", false);

        this.restrictions = new Restrictions(restrictMove, restrictTeleport, restrictDamage, restrictDamageOthers, damageCheckOnlyPlayers);
    }

    private Actions actions;

    private void setupActions(ConfigurationSection actions) {
        boolean isNullSection = isNullSection(actions);
        List<Action> preTeleportActions = isNullSection ? List.of() : getActionList(actions.getStringList("pre_teleport"));
        Int2ObjectMap<List<Action>> onCooldownActions = new Int2ObjectOpenHashMap<>();
        final ConfigurationSection cooldownActions = actions.getConfigurationSection("on_cooldown");
        if (!isNullSection(cooldownActions)) {
            for (String actionId : cooldownActions.getKeys(false)) {
                if (!Utils.isNumeric(actionId)) {
                    continue;
                }
                int time = Integer.parseInt(actionId);
                List<Action> actionList = getActionList(cooldownActions.getStringList(actionId));
                onCooldownActions.put(time, actionList);
            }
        }
        List<Action> afterTeleportActions = isNullSection ? List.of() : getActionList(actions.getStringList("after_teleport"));

        this.actions = new Actions(preTeleportActions, onCooldownActions, afterTeleportActions);
    }

    private ImmutableList<Action> getActionList(List<String> actionStrings) {
        List<Action> actions = new ArrayList<>(actionStrings.size());
        for (String actionStr : actionStrings) {
            try {
                actions.add(Objects.requireNonNull(plugin.getTeleportManager().getActionRegistry().resolveAction(actionStr), "Type doesn't exist"));
            } catch (Exception ex) {
                plugin.getSLF4JLogger().warn("Couldn't create action for string '{}'", actionStr, ex);
            }
        }
        return ImmutableList.copyOf(actions);
    }

    private boolean isNullSection(ConfigurationSection section) {
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
                getPrefixed(messages.getString("cancelled", "messages.cancelled"), messagesPrefix)
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

}
