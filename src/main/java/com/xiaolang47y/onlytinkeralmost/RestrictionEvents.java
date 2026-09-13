package com.xiaolang47y.onlytinkeralmost;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Enforces the "Tinkers' Construct only" rule on both logical sides so it works
 * for the local player and on a dedicated server.
 *
 * @author Xiaolang47y
 */
@Mod.EventBusSubscriber(modid = OnlyTinkerAlmost.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RestrictionEvents {

    private RestrictionEvents() {}

    private static boolean restricted(Player player, ItemStack stack) {
        if (player == null || !Config.RESTRICT_ITEM_USE.get()) {
            return false;
        }
        if (Config.IGNORE_CREATIVE.get() && player.isCreative()) {
            return false;
        }
        return ItemClassifier.isRestricted(stack);
    }

    /** Right-click with a restricted item in hand (flint & steel, shears, bow, etc.). */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (restricted(event.getEntity(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
    }

    /** Right-click a block while holding a restricted item — block the item use only. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (restricted(event.getEntity(), event.getItemStack())) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    /** Left-click a block to break it with a restricted tool. */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (Config.RESTRICT_BLOCK_BREAKING.get()
                && restricted(event.getEntity(), event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    /** Interact with an entity (e.g. shearing a sheep) with a restricted item. */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (restricted(event.getEntity(), event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    /** Slow / stop breaking blocks with a restricted tool (covers survival mining). */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!Config.RESTRICT_BLOCK_BREAKING.get()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        if (restricted(player, tool)) {
            event.setNewSpeed(0.0F);
        }
    }

    /** Melee attacks with a restricted weapon deal no damage. */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!Config.RESTRICT_MELEE_ATTACK.get()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        if (Config.IGNORE_CREATIVE.get() && player.isCreative()) {
            return;
        }
        if (ItemClassifier.isRestricted(player.getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    /** Restricted shields block no damage. */
    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (!Config.RESTRICT_SHIELD.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (Config.IGNORE_CREATIVE.get() && player.isCreative()) {
            return;
        }
        ItemStack shield = player.getUseItem();
        if (shield.isEmpty()) {
            shield = player.getOffhandItem();
        }
        if (ItemClassifier.isRestricted(shield)) {
            event.setBlockedDamage(0.0F);
            event.setShieldTakesDamage(false);
        }
    }

    /** Unequip restricted armor (including elytra) the moment it is worn. */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!Config.RESTRICT_ARMOR.get()) {
            return;
        }
        if (Config.IGNORE_CREATIVE.get() && player.isCreative()) {
            return;
        }
        if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) {
            return;
        }
        ItemStack to = event.getTo();
        if (ItemClassifier.isRestricted(to)) {
            // Move the illegal piece into the inventory (drop if full).
            player.setItemSlot(event.getSlot(), ItemStack.EMPTY);
            if (!player.getInventory().add(to)) {
                player.drop(to, false);
            }
        }
    }

    /** Add the "only for crafting" tooltip to restricted items. */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!Config.SHOW_TOOLTIP.get()) {
            return;
        }
        Player player = event.getEntity();
        if (Config.IGNORE_CREATIVE.get() && player != null && player.isCreative()) {
            return;
        }
        if (ItemClassifier.isRestricted(event.getItemStack())) {
            event.getToolTip().add(Component.translatable("tooltip." + OnlyTinkerAlmost.MODID + ".restricted")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
