package ru.overwrite.teleports.configuration.data;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.actions.Action;
import ru.overwrite.teleports.actions.ActionRegistry;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public record Settings(
        Cooldown cooldown,
        Bossbar bossbar,
        Particles particles,
        Restrictions restrictions,
        Actions actions) {

    public static Settings create(OvTeleportAddon plugin, ConfigurationSection config, Config pluginConfig, Settings template, boolean applyTemplate) {
        return new Settings(
                setupCooldown(plugin, config.getConfigurationSection("cooldown"), template, pluginConfig, applyTemplate),
                setupBossBar(config.getConfigurationSection("bossbar"), template, pluginConfig, applyTemplate),
                setupParticles(config.getConfigurationSection("particles"), template, pluginConfig, applyTemplate),
                setupRestrictions(config.getConfigurationSection("restrictions"), template, pluginConfig, applyTemplate),
                setupActions(plugin, config.getConfigurationSection("actions"), template, pluginConfig, applyTemplate)
        );
    }

    public static Cooldown setupCooldown(OvTeleportAddon plugin, ConfigurationSection cooldown, Settings template, Config pluginConfig, boolean applyTemplate) {
        boolean isNullSection = pluginConfig.isNullSection(cooldown);
        if (isNullSection && !applyTemplate) {
            return null;
        }
        boolean hasTemplateCooldown = template != null && template.cooldown() != null;

        Object2IntSortedMap<String> preTeleportCooldownsMap = new Object2IntLinkedOpenHashMap<>();

        boolean useLastGroupCooldown = !isNullSection && cooldown.getBoolean("use_last_group_cooldown", false);

        IntSupplier preTeleportCooldownTemplateValue = () -> getOrDefaultValue(hasTemplateCooldown, () -> template.cooldown().defaultPreTeleportCooldown(), -1);
        int defaultPreTeleportCooldown = getOrDefaultValue(!isNullSection, () -> cooldown.getInt("default_pre_teleport_cooldown", preTeleportCooldownTemplateValue.getAsInt()), preTeleportCooldownTemplateValue.getAsInt());

        ConfigurationSection preTeleportGroupCooldowns = getOrDefaultValue(!isNullSection, () -> cooldown.getConfigurationSection("pre_teleport_group_cooldowns"), null);
        if (!pluginConfig.isNullSection(preTeleportGroupCooldowns)) {
            defaultPreTeleportCooldown = processCooldownSection(plugin, preTeleportGroupCooldowns, preTeleportCooldownsMap, useLastGroupCooldown, defaultPreTeleportCooldown);
        } else if (hasTemplateCooldown) {
            preTeleportCooldownsMap.putAll(template.cooldown().preTeleportCooldowns());
        }

        return new Cooldown(defaultPreTeleportCooldown, preTeleportCooldownsMap);
    }

    private static int processCooldownSection(OvTeleportAddon plugin, ConfigurationSection section, Object2IntSortedMap<String> map, boolean useLastGroup, int currentDefault) {
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

    public static Bossbar setupBossBar(ConfigurationSection bossbar, Settings template, Config pluginConfig, boolean applyTemplate) {
        boolean isNullSection = pluginConfig.isNullSection(bossbar);
        if (isNullSection && !applyTemplate) {
            return null;
        }
        boolean hasTemplateBossbar = template != null && template.bossbar() != null;

        boolean enabledDefault = hasTemplateBossbar && template.bossbar().bossbarEnabled();
        boolean enabled = !isNullSection ? bossbar.getBoolean("enabled", enabledDefault) : enabledDefault;

        String title = getOrDefaultValue(!isNullSection && bossbar.contains("title"), () -> Utils.COLORIZER.colorize(bossbar.getString("title")), getOrDefaultValue(hasTemplateBossbar, () -> template.bossbar().bossbarTitle(), null));

        BarColor color = getOrDefaultValue(!isNullSection && bossbar.contains("color"), () -> BarColor.valueOf(bossbar.getString("color", "WHITE").toUpperCase()), getOrDefaultValue(hasTemplateBossbar, () -> template.bossbar().bossbarColor(), null));

        BarStyle style = getOrDefaultValue(!isNullSection && bossbar.contains("style"), () -> BarStyle.valueOf(bossbar.getString("style", "SEGMENTED_12").toUpperCase()), getOrDefaultValue(hasTemplateBossbar, () -> template.bossbar().bossbarStyle(), null));

        return new Bossbar(enabled, title, color, style);
    }

    public static Particles setupParticles(ConfigurationSection particles, Settings template, Config pluginConfig, boolean applyTemplate) {
        boolean isNullSection = pluginConfig.isNullSection(particles);
        if (isNullSection && !applyTemplate) {
            return null;
        }
        boolean hasTemplateParticles = template != null && template.particles() != null;

        boolean preTeleportEnabled = hasTemplateParticles && template.particles().preTeleportEnabled();
        boolean preTeleportSendOnlyToPlayer = hasTemplateParticles && template.particles().preTeleportSendOnlyToPlayer();
        List<Particles.ParticleData> preTeleportParticles = getOrDefaultValue(hasTemplateParticles, () -> template.particles().preTeleportParticles(), null);
        int preTeleportDots = getOrDefaultValue(hasTemplateParticles, () -> template.particles().preTeleportDots(), 0);
        double preTeleportRadius = getOrDefaultValue(hasTemplateParticles, () -> template.particles().preTeleportRadius(), 0.0D);
        double preTeleportParticleSpeed = getOrDefaultValue(hasTemplateParticles, () -> template.particles().preTeleportParticleSpeed(), 0.0D);
        double preTeleportSpeed = getOrDefaultValue(hasTemplateParticles, () -> template.particles().preTeleportSpeed(), 0.0D);
        boolean preTeleportInvert = hasTemplateParticles && template.particles().preTeleportInvert();
        boolean preTeleportJumping = hasTemplateParticles && template.particles().preTeleportJumping();
        boolean preTeleportMoveNear = hasTemplateParticles && template.particles().preTeleportMoveNear();

        boolean afterTeleportParticleEnabled = hasTemplateParticles && template.particles().afterTeleportEnabled();
        boolean afterTeleportSendOnlyToPlayer = hasTemplateParticles && template.particles().afterTeleportSendOnlyToPlayer();
        Particles.ParticleData afterTeleportParticle = getOrDefaultValue(hasTemplateParticles, () -> template.particles().afterTeleportParticle(), null);
        int afterTeleportCount = getOrDefaultValue(hasTemplateParticles, () -> template.particles().afterTeleportCount(), 0);
        double afterTeleportRadius = getOrDefaultValue(hasTemplateParticles, () -> template.particles().afterTeleportRadius(), 0.0D);
        double afterTeleportParticleSpeed = getOrDefaultValue(hasTemplateParticles, () -> template.particles().afterTeleportParticleSpeed(), 0.0D);

        ConfigurationSection preTeleport = getOrDefaultValue(!isNullSection, () -> particles.getConfigurationSection("pre_teleport"), null);

        if (!pluginConfig.isNullSection(preTeleport)) {
            preTeleportEnabled = preTeleport.getBoolean("enabled", preTeleportEnabled);
            preTeleportSendOnlyToPlayer = preTeleport.getBoolean("send_only_to_player", preTeleportSendOnlyToPlayer);
            if (preTeleport.contains("id")) {
                ImmutableList.Builder<Particles.ParticleData> builder = ImmutableList.builder();
                for (String particleId : preTeleport.getStringList("id")) {
                    builder.add(Utils.createParticleData(particleId));
                }
                preTeleportParticles = builder.build();
            }
            preTeleportDots = preTeleport.getInt("dots", preTeleportDots);
            preTeleportRadius = preTeleport.getDouble("radius", preTeleportRadius);
            preTeleportParticleSpeed = preTeleport.getDouble("particle_speed", preTeleportParticleSpeed);
            preTeleportSpeed = preTeleport.getDouble("speed", preTeleportSpeed);
            preTeleportInvert = preTeleport.getBoolean("invert", preTeleportInvert);
            preTeleportJumping = preTeleport.getBoolean("jumping", preTeleportJumping);
            preTeleportMoveNear = preTeleport.getBoolean("move_near", preTeleportMoveNear);
        }

        ConfigurationSection afterTeleport = getOrDefaultValue(!isNullSection, () -> particles.getConfigurationSection("after_teleport"), null);

        if (!pluginConfig.isNullSection(afterTeleport)) {
            afterTeleportParticleEnabled = afterTeleport.getBoolean("enabled", afterTeleportParticleEnabled);
            afterTeleportSendOnlyToPlayer = afterTeleport.getBoolean("send_only_to_player", afterTeleportSendOnlyToPlayer);
            if (afterTeleport.contains("id")) {
                afterTeleportParticle = Utils.createParticleData(afterTeleport.getString("id"));
            }
            afterTeleportCount = afterTeleport.getInt("count", afterTeleportCount);
            afterTeleportRadius = afterTeleport.getDouble("radius", afterTeleportRadius);
            afterTeleportParticleSpeed = afterTeleport.getDouble("particle_speed", afterTeleportParticleSpeed);
        }

        return new Particles(preTeleportEnabled, preTeleportSendOnlyToPlayer, preTeleportParticles, preTeleportDots, preTeleportRadius, preTeleportParticleSpeed, preTeleportSpeed, preTeleportInvert, preTeleportJumping, preTeleportMoveNear, afterTeleportParticleEnabled, afterTeleportSendOnlyToPlayer, afterTeleportParticle, afterTeleportCount, afterTeleportRadius, afterTeleportParticleSpeed);
    }

    public static Restrictions setupRestrictions(ConfigurationSection restrictions, Settings template, Config pluginConfig, boolean applyTemplate) {
        boolean isNullSection = pluginConfig.isNullSection(restrictions);
        if (isNullSection && !applyTemplate) {
            return null;
        }
        boolean hasTemplateRestrictions = template != null && template.restrictions() != null;

        boolean templateRestrictMove = hasTemplateRestrictions && template.restrictions().restrictMove();
        boolean restrictMove = getOrDefaultValue(!isNullSection, () -> restrictions.getBoolean("move", templateRestrictMove), templateRestrictMove);

        boolean templateRestrictTeleport = hasTemplateRestrictions && template.restrictions().restrictTeleport();
        boolean restrictTeleport = getOrDefaultValue(!isNullSection, () -> restrictions.getBoolean("teleport", templateRestrictTeleport), templateRestrictTeleport);

        boolean templateRestrictDamage = hasTemplateRestrictions && template.restrictions().restrictDamage();
        boolean restrictDamage = getOrDefaultValue(!isNullSection, () -> restrictions.getBoolean("damage", templateRestrictDamage), templateRestrictDamage);

        boolean templateRestrictDamageOthers = hasTemplateRestrictions && template.restrictions().restrictDamageOthers();
        boolean restrictDamageOthers = getOrDefaultValue(!isNullSection, () -> restrictions.getBoolean("damage_others", templateRestrictDamageOthers), templateRestrictDamageOthers);

        boolean templateDamageCheckOnlyPlayers = hasTemplateRestrictions && template.restrictions().damageCheckOnlyPlayers();
        boolean damageCheckOnlyPlayers = getOrDefaultValue(!isNullSection, () -> restrictions.getBoolean("damage_check_only_players", templateDamageCheckOnlyPlayers), templateDamageCheckOnlyPlayers);

        return new Restrictions(restrictMove, restrictTeleport, restrictDamage, restrictDamageOthers, damageCheckOnlyPlayers);
    }

    public static Actions setupActions(OvTeleportAddon plugin, ConfigurationSection actionsSection, Settings template, Config pluginConfig, boolean applyTemplate) {
        boolean isNullSection = pluginConfig.isNullSection(actionsSection);
        if (isNullSection && !applyTemplate) {
            return null;
        }
        boolean hasTemplateActions = template != null && template.actions() != null;

        ActionRegistry actionRegistry = plugin.getTeleportManager().getActionRegistry();

        List<Action> preTeleportActions = getOrDefaultValue(
                !isNullSection && actionsSection.contains("pre_teleport"),
                () -> getActionList(plugin, actionRegistry, actionsSection.getStringList("pre_teleport")),
                getOrDefaultValue(
                        hasTemplateActions,
                        () -> template.actions().preTeleportActions(),
                        List.of()
                )
        );

        Int2ObjectMap<List<Action>> onCooldownActions = new Int2ObjectOpenHashMap<>();
        if (!isNullSection && actionsSection.contains("on_cooldown")) {
            ConfigurationSection cdSection = actionsSection.getConfigurationSection("on_cooldown");
            if (!pluginConfig.isNullSection(cdSection)) {
                for (String key : cdSection.getKeys(false)) {
                    if (Utils.isNumeric(key)) {
                        onCooldownActions.put(
                                Integer.parseInt(key),
                                getActionList(plugin, actionRegistry, cdSection.getStringList(key))
                        );
                    }
                }
            }
        } else if (hasTemplateActions) {
            onCooldownActions.putAll(template.actions().onCooldownActions());
        }

        List<Action> afterTeleportActions = getOrDefaultValue(
                !isNullSection && actionsSection.contains("after_teleport"),
                () -> getActionList(plugin, actionRegistry, actionsSection.getStringList("after_teleport")),
                getOrDefaultValue(
                        hasTemplateActions,
                        () -> template.actions().afterTeleportActions(),
                        List.of()
                )
        );

        return new Actions(preTeleportActions, onCooldownActions, afterTeleportActions);
    }

    private static <T> T getOrDefaultValue(boolean hasValue, Supplier<T> supplier, T defaultValue) {
        return hasValue ? supplier.get() : defaultValue;
    }

    private static int getOrDefaultValue(boolean hasValue, IntSupplier supplier, int defaultValue) {
        return hasValue ? supplier.getAsInt() : defaultValue;
    }

    private static double getOrDefaultValue(boolean hasValue, DoubleSupplier supplier, double defaultValue) {
        return hasValue ? supplier.getAsDouble() : defaultValue;
    }

    private static ImmutableList<Action> getActionList(OvTeleportAddon plugin, ActionRegistry actionRegistry, List<String> actionStrings) {
        ImmutableList.Builder<Action> builder = ImmutableList.builder();
        for (String actionStr : actionStrings) {
            try {
                builder.add(Objects.requireNonNull(actionRegistry.resolveAction(actionStr), "Type doesn't exist"));
            } catch (Exception ex) {
                plugin.getSLF4JLogger().warn("Couldn't create action for string '{}'", actionStr, ex);
            }
        }
        return builder.build();
    }
}
