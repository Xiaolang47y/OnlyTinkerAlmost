package com.xiaolang47y.onlytinkeralmost;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Decides whether an item is a tool / weapon / armor that the player is not
 * allowed to use. Tinkers' Construct gear (items implementing
 * {@code slimeknights.tconstruct.library.tools.item.IModifiable}) and anything
 * on the config whitelist are always allowed.
 *
 * @author Xiaolang47y
 */
public final class ItemClassifier {

    /** Interface implemented by every Tinkers' Construct 3 tool, weapon and armor piece. */
    private static final String IMODIFIABLE = "slimeknights.tconstruct.library.tools.item.IModifiable";

    /** Per-item cache of "is this equipment-like?" (item classes never change at runtime). */
    private static final Map<Item, Boolean> EQUIPMENT_CACHE = new ConcurrentHashMap<>();

    // --- whitelist caches (refreshed whenever the config lists change) ---
    private static List<? extends String> cachedMods;
    private static List<? extends String> cachedItems;
    private static Set<String> modSet = Set.of();
    private static List<Pattern> itemPatterns = List.of();

    private ItemClassifier() {}

    /** @return true if the player may not use this stack (it is only a crafting ingredient). */
    public static boolean isRestricted(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == Items.AIR) {
            return false;
        }
        // Tinkers' Construct tools / addons' TiC-style tools are always fine.
        if (isModifiable(item)) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || isWhitelisted(id)) {
            return false;
        }
        return isEquipmentLike(item);
    }

    /** True for items implementing TiC's IModifiable (checked by name, no hard dependency). */
    public static boolean isModifiable(Item item) {
        return implementsInterface(item.getClass(), IMODIFIABLE);
    }

    private static boolean implementsInterface(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Class<?> iface : c.getInterfaces()) {
                if (iface.getName().equals(name) || implementsInterface(iface, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** True if the item looks like a tool, weapon or armor piece (vanilla or modded). */
    public static boolean isEquipmentLike(Item item) {
        return EQUIPMENT_CACHE.computeIfAbsent(item, ItemClassifier::computeEquipmentLike);
    }

    private static boolean computeEquipmentLike(Item item) {
        // Block items are placed, not "used" as tools.
        if (item instanceof BlockItem) {
            return false;
        }
        // Vanilla tool / weapon / armor classes (subclasses included).
        if (item instanceof SwordItem || item instanceof PickaxeItem || item instanceof AxeItem
                || item instanceof ShovelItem || item instanceof HoeItem || item instanceof BowItem
                || item instanceof CrossbowItem || item instanceof FishingRodItem
                || item instanceof FlintAndSteelItem || item instanceof ShearsItem
                || item instanceof TridentItem || item instanceof ShieldItem
                || item instanceof ArmorItem || item instanceof ElytraItem) {
            return true;
        }
        // Modded classes that don't extend vanilla: detect melee weapons by their
        // attack-damage attribute on the main hand.
        Multimap<Attribute, AttributeModifier> modifiers = item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        for (AttributeModifier modifier : modifiers.get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getAmount() >= 1.0D) {
                return true;
            }
        }
        // Heuristic for custom armor classes named *ArmorItem / *Armor.
        for (Class<?> c = item.getClass(); c != null && c != Item.class; c = c.getSuperclass()) {
            String simple = c.getSimpleName();
            if (simple.endsWith("ArmorItem") || simple.endsWith("Armor") || simple.endsWith("Elytra")) {
                return true;
            }
        }
        return false;
    }

    /** Whitelist check against config (mod namespaces + item IDs with wildcards). */
    public static boolean isWhitelisted(ResourceLocation id) {
        refreshCache();
        if (modSet.contains(id.getNamespace())) {
            return true;
        }
        String full = id.toString();
        for (Pattern p : itemPatterns) {
            if (p.matcher(full).matches()) {
                return true;
            }
        }
        return false;
    }

    private static synchronized void refreshCache() {
        List<? extends String> mods = Config.WHITELISTED_MODS.get();
        List<? extends String> items = Config.WHITELISTED_ITEMS.get();
        if (mods != cachedMods) {
            cachedMods = mods;
            modSet = Set.copyOf(mods);
        }
        if (items != cachedItems) {
            cachedItems = items;
            List<Pattern> patterns = new ArrayList<>();
            for (String entry : items) {
                patterns.add(globToRegex(entry.trim()));
            }
            itemPatterns = List.copyOf(patterns);
        }
    }

    private static Pattern globToRegex(String glob) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                sb.append(".*");
            } else if (c == '?') {
                sb.append('.');
            } else {
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return Pattern.compile(sb.toString());
    }
}
