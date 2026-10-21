package com.davibogo.faithfulfangs.item;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.registry.ModItems;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.jspecify.annotations.Nullable;

/**
 * Wolf armor tiers made at the smithing table from the vanilla armadillo-scute Wolf Armor plus one
 * ingot or diamond. Like the vanilla one, each absorbs every hit until it breaks
 * ({@link com.davibogo.faithfulfangs.wolf.WolfArmor}); better tiers take more damage. They have
 * the same armor points as the vanilla one, cannot be enchanted and keep its dyeable stripes.
 */
public enum WolfArmorTier {
    /** Absorbs 96. */
    COPPER("copper", 6, ItemTags.REPAIRS_COPPER_ARMOR),
    /** Absorbs 128. */
    IRON("iron", 8, ItemTags.REPAIRS_IRON_ARMOR),
    /** Absorbs 112; piglins treat the owner as if wearing gold while this wolf is near. */
    GOLDEN("golden", 7, ItemTags.REPAIRS_GOLD_ARMOR),
    /** Absorbs 256. */
    DIAMOND("diamond", 16, ItemTags.REPAIRS_DIAMOND_ARMOR),
    /** Absorbs 384; the item does not burn in fire or lava. */
    NETHERITE("netherite", 24, ItemTags.REPAIRS_NETHERITE_ARMOR);

    /** Same armor points as the vanilla Wolf Armor (they only matter for damage the armor lets through). */
    private static final int BODY_DEFENSE = 11;

    private final String name;
    /** Body armor durability is 16 times this. */
    private final int durabilityFactor;
    private final TagKey<Item> repairItems;

    WolfArmorTier(String name, int durabilityFactor, TagKey<Item> repairItems) {
        this.name = name;
        this.durabilityFactor = durabilityFactor;
        this.repairItems = repairItems;
    }

    /** Registry name, like {@code iron_wolf_armor}; also the name of its equipment asset and textures. */
    public String itemName() {
        return this.name + "_wolf_armor";
    }

    public boolean fireResistant() {
        return this == NETHERITE;
    }

    public ArmorMaterial material() {
        return new ArmorMaterial(this.durabilityFactor, Map.of(ArmorType.BODY, BODY_DEFENSE), 0,
                SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, this.repairItems,
                ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, this.itemName())));
    }

    /** The material whose bits fly off when the armor cracks. */
    public Item crackParticle() {
        return switch (this) {
            case COPPER -> Items.COPPER_INGOT;
            case IRON -> Items.IRON_INGOT;
            case GOLDEN -> Items.GOLD_INGOT;
            case DIAMOND -> Items.DIAMOND;
            case NETHERITE -> Items.NETHERITE_INGOT;
        };
    }

    public Item item() {
        return ModItems.WOLF_ARMORS.get(this).get();
    }

    /** The tier of a Faithful Fangs wolf armor, or null for anything else (the vanilla one included). */
    public static @Nullable WolfArmorTier of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (WolfArmorTier tier : values()) {
            if (stack.is(tier.item())) {
                return tier;
            }
        }
        return null;
    }
}
