package com.xiaolang47y.onlytinkeralmost;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Configuration for OnlyTinkerAlmost.
 * File: config/onlytinkeralmost-common.toml
 *
 * @author Xiaolang47y
 */
public final class Config {

    private Config() {}

    public static final ForgeConfigSpec SPEC;

    // Behaviour toggles
    public static final ForgeConfigSpec.BooleanValue RESTRICT_BLOCK_BREAKING;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_MELEE_ATTACK;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_ITEM_USE;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_ARMOR;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_SHIELD;
    public static final ForgeConfigSpec.BooleanValue SHOW_TOOLTIP;
    public static final ForgeConfigSpec.BooleanValue IGNORE_CREATIVE;

    // Whitelist
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_MODS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_ITEMS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("OnlyTinkerAlmost — restricts the player to Tinkers' Construct gear.").push("general");

        RESTRICT_BLOCK_BREAKING = builder
                .comment("When true, blocks cannot be broken with non-whitelisted tools (bare hands still work).")
                .define("restrictBlockBreaking", true);

        RESTRICT_MELEE_ATTACK = builder
                .comment("When true, melee attacks with non-whitelisted weapons deal no damage.")
                .define("restrictMeleeAttack", true);

        RESTRICT_ITEM_USE = builder
                .comment("When true, using non-whitelisted tools/weapons is blocked " +
                        "(right-click: flint & steel, shears, fishing rod, bow, crossbow, trident, shield, etc.).")
                .define("restrictItemUse", true);

        RESTRICT_ARMOR = builder
                .comment("When true, non-whitelisted armor (including elytra) cannot be worn and is unequipped automatically.")
                .define("restrictArmor", true);

        RESTRICT_SHIELD = builder
                .comment("When true, non-whitelisted shields block no damage.")
                .define("restrictShield", true);

        SHOW_TOOLTIP = builder
                .comment("When true, restricted items show a red tooltip warning that they can only be used for crafting.")
                .define("showTooltip", true);

        IGNORE_CREATIVE = builder
                .comment("When true, restrictions are bypassed in creative mode (useful for testing / map making).")
                .define("ignoreCreative", true);

        builder.pop();

        builder.comment("Whitelist: items listed here are always usable, even if they are vanilla / other-mod gear.",
                "Entries are matched against the item registry name, e.g. \"minecraft:flint_and_steel\".",
                "Wildcards are supported: \"minecraft:horse_armor_*\" or \"somemod:*\" (whole namespace).")
                .push("whitelist");

        WHITELISTED_MODS = builder
                .comment("Mod namespaces whose items are always allowed. Tinkers' Construct is included by default.")
                .defineListAllowEmpty("whitelistedMods", List.of("tconstruct", "mantle"),
                        o -> o instanceof String s && !s.isBlank());

        WHITELISTED_ITEMS = builder
                .comment("Individual item IDs (or wildcards) that are always allowed, e.g. \"minecraft:flint_and_steel\".")
                .defineListAllowEmpty("whitelistedItems", List.of(),
                        o -> o instanceof String s && !s.isBlank());

        builder.pop();

        SPEC = builder.build();
    }
}
