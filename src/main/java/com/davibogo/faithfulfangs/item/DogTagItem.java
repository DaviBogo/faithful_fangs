package com.davibogo.faithfulfangs.item;

import com.davibogo.faithfulfangs.registry.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

/**
 * Dog Tag: blank when crafted. Using it on your wolf engraves a name on it
 * (stored in the {@link EngravedTag} component); the engraved tag is then used on a Dog Bed.
 */
public class DogTagItem extends Item {
    public DogTagItem(Item.Properties properties) {
        super(properties);
    }

    public static @Nullable EngravedTag getEngraving(ItemStack stack) {
        return stack.get(ModDataComponents.ENGRAVED_TAG.get());
    }

    public static boolean isEngraved(ItemStack stack) {
        return getEngraving(stack) != null;
    }

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
