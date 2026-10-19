package com.davibogo.faithfulfangs.item;

import com.davibogo.faithfulfangs.block.DogBedBlock;
import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.bond.BondManager;
import com.davibogo.faithfulfangs.bond.BondRegistry;
import com.davibogo.faithfulfangs.network.ModNetwork;
import com.davibogo.faithfulfangs.network.OpenTagScreenPacket;
import com.davibogo.faithfulfangs.registry.ModDataComponents;
import com.davibogo.faithfulfangs.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Dog Tag: blank when crafted. Using it on your wolf opens a screen to type a name, which is
 * engraved on the tag ({@link EngravedTag} component). The engraved tag is then used on a Dog Bed
 * to bind the wolf to it. A blank tag used on a wolf that already has a bed renames it right away.
 */
public class DogTagItem extends Item {
    /** How far the wolf may be when the player confirms the name (squared blocks). */
    private static final double MAX_CONFIRM_DISTANCE_SQR = 10 * 10;

    public DogTagItem(Item.Properties properties) {
        super(properties);
    }

    public static @Nullable EngravedTag getEngraving(ItemStack stack) {
        return stack.get(ModDataComponents.ENGRAVED_TAG.get());
    }

    public static boolean isEngraved(ItemStack stack) {
        return getEngraving(stack) != null;
    }

    public static boolean isBlankTag(ItemStack stack) {
        return stack.is(ModItems.DOG_TAG.get()) && !isEngraved(stack);
    }

    // ---------------------------------------------------------------- tag used on a Dog Bed

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof DogBedBlock)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        EngravedTag engraving = getEngraving(stack);
        if (engraving == null) {
            overlay(player, Component.translatable("message.faithful_fangs.blank_tag_on_bed"));
            return InteractionResult.FAIL;
        }
        String name = EngravedTag.sanitizeName(engraving.wolfName());
        if (name == null) {
            overlay(player, Component.translatable("message.faithful_fangs.invalid_name"));
            return InteractionResult.FAIL;
        }

        // If the wolf is loaded we can check it still belongs to this player.
        MinecraftServer server = serverLevel.getServer();
        Wolf wolf = BondManager.findLoadedWolf(server, engraving.wolfId());
        if (wolf != null && (!wolf.isTame() || !wolf.isOwnedBy(player))) {
            overlay(player, Component.translatable("message.faithful_fangs.tag_not_owner", name));
            return InteractionResult.FAIL;
        }

        BondManager.BindResult result = BondManager.bind(serverLevel, pos, engraving.wolfId(), player.getUUID(), name);
        if (!result.success()) {
            overlay(player, Component.translatable("message.faithful_fangs.bed_taken", result.takenBy().wolfName()));
            return InteractionResult.FAIL;
        }

        stack.consume(1, player);
        Bond previous = result.previous();
        boolean moved = previous != null && !previous.bed().equals(GlobalPos.of(serverLevel.dimension(), pos));
        overlay(player, Component.translatable(moved ? "message.faithful_fangs.bed_moved" : "message.faithful_fangs.bed_bound", name));
        serverLevel.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    // ---------------------------------------------------------------- tag used on a wolf

    /**
     * A Dog Tag was used on a wolf (server side, called from the interaction event).
     * Blank tag on your wolf: ask the client to open the name screen.
     */
    public static void useOnWolf(ServerPlayer player, ItemStack stack, Wolf wolf) {
        EngravedTag engraving = getEngraving(stack);
        if (engraving != null) {
            String key = engraving.wolfId().equals(wolf.getUUID())
                    ? "message.faithful_fangs.tag_use_on_bed"
                    : "message.faithful_fangs.tag_other_wolf";
            overlay(player, Component.translatable(key, engraving.wolfName()));
            return;
        }
        if (!wolf.isTame() || !wolf.isOwnedBy(player)) {
            overlay(player, Component.translatable("message.faithful_fangs.not_your_wolf"));
            return;
        }

        Bond bond = BondRegistry.get(player.level().getServer()).getByWolf(wolf.getUUID());
        String currentName = bond != null ? bond.wolfName()
                : wolf.getCustomName() != null ? wolf.getCustomName().getString() : "";
        ModNetwork.sendToPlayer(new OpenTagScreenPacket(wolf.getId(), currentName, bond != null), player);
    }

    /**
     * The player confirmed a name in the Dog Tag screen. Checks everything again, then either renames
     * the wolf (it has a bed; the blank tag is used up) or engraves one blank tag for it.
     */
    public static void handleNameConfirmed(ServerPlayer player, int wolfEntityId, String rawName) {
        ServerLevel level = player.level();
        if (!(level.getEntity(wolfEntityId) instanceof Wolf wolf) || player.distanceToSqr(wolf) > MAX_CONFIRM_DISTANCE_SQR) {
            return;
        }
        if (!wolf.isTame() || !wolf.isOwnedBy(player)) {
            overlay(player, Component.translatable("message.faithful_fangs.not_your_wolf"));
            return;
        }
        String name = EngravedTag.sanitizeName(rawName);
        if (name == null) {
            overlay(player, Component.translatable("message.faithful_fangs.invalid_name"));
            return;
        }
        InteractionHand hand = isBlankTag(player.getMainHandItem()) ? InteractionHand.MAIN_HAND
                : isBlankTag(player.getOffhandItem()) ? InteractionHand.OFF_HAND : null;
        if (hand == null) {
            overlay(player, Component.translatable("message.faithful_fangs.need_blank_tag"));
            return;
        }
        ItemStack stack = player.getItemInHand(hand);

        if (BondManager.rename(level.getServer(), wolf.getUUID(), name)) {
            stack.consume(1, player);
            overlay(player, Component.translatable("message.faithful_fangs.wolf_renamed", name));
        } else {
            ItemStack engraved = stack.copyWithCount(1);
            engraved.set(ModDataComponents.ENGRAVED_TAG.get(), new EngravedTag(wolf.getUUID(), name));
            if (stack.getCount() == 1 && !player.hasInfiniteMaterials()) {
                player.setItemInHand(hand, engraved);
            } else {
                stack.consume(1, player);
                player.getInventory().placeItemBackInInventory(engraved, Prediction.SERVER_ONLY);
            }
            overlay(player, Component.translatable("message.faithful_fangs.tag_engraved", name));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void overlay(Player player, Component message) {
        player.sendOverlayMessage(message);
    }

    // ---------------------------------------------------------------- tooltip

    @Override
    @SuppressWarnings("deprecation") // Still called by ItemStack#addDetailsToTooltip in 26.3.
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        EngravedTag engraving = getEngraving(stack);
        if (engraving == null) {
            builder.accept(Component.translatable("item.faithful_fangs.dog_tag.tooltip.blank")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            builder.accept(Component.translatable("item.faithful_fangs.dog_tag.tooltip.engraved",
                    Component.literal(engraving.wolfName()).withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));
            builder.accept(Component.translatable("item.faithful_fangs.dog_tag.tooltip.use_on_bed")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
