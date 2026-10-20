package com.davibogo.faithfulfangs.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Ancient, Blazing and Ethereal Bones: rare finds of a wolf's nose, one per dimension. The tooltip
 * says where they are found ({@code <description id>.tooltip}).
 */
public class SpecialBoneItem extends Item {
    public SpecialBoneItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation") // Still called by ItemStack#addDetailsToTooltip in 26.3.
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}
