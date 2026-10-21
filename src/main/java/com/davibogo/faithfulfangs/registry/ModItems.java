package com.davibogo.faithfulfangs.registry;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.item.DogTagItem;
import com.davibogo.faithfulfangs.item.SpecialBoneItem;
import com.davibogo.faithfulfangs.item.WolfArmorTier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Mod items. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, FaithfulFangs.MODID);

    public static final RegistryObject<Item> DOG_TAG = ITEMS.register("dog_tag",
            () -> new DogTagItem(new Item.Properties().setId(ITEMS.key("dog_tag"))));

    /** Special bones a wolf sniffs out: one per dimension, rarer the farther it is. */
    public static final RegistryObject<Item> ANCIENT_BONE = registerBone("ancient_bone", Rarity.UNCOMMON, false);
    /** Like other Nether treasures, it does not burn in fire or lava. */
    public static final RegistryObject<Item> BLAZING_BONE = registerBone("blazing_bone", Rarity.RARE, true);
    public static final RegistryObject<Item> ETHEREAL_BONE = registerBone("ethereal_bone", Rarity.EPIC, false);

    /** Wolf armor tiers made from the vanilla Wolf Armor at the smithing table. */
    public static final Map<WolfArmorTier, RegistryObject<Item>> WOLF_ARMORS = registerWolfArmors();

    /** Block items for the Dog Beds; they stack to 1 like vanilla beds. */
    public static final Map<DyeColor, RegistryObject<BlockItem>> DOG_BEDS = registerDogBeds();

    private static RegistryObject<Item> registerBone(String name, Rarity rarity, boolean fireResistant) {
        return ITEMS.register(name, () -> {
            Item.Properties properties = new Item.Properties().setId(ITEMS.key(name)).rarity(rarity);
            return new SpecialBoneItem(fireResistant ? properties.fireResistant() : properties);
        });
    }

    private static Map<WolfArmorTier, RegistryObject<Item>> registerWolfArmors() {
        Map<WolfArmorTier, RegistryObject<Item>> armors = new EnumMap<>(WolfArmorTier.class);
        for (WolfArmorTier tier : WolfArmorTier.values()) {
            String name = tier.itemName();
            armors.put(tier, ITEMS.register(name, () -> {
                Item.Properties properties = new Item.Properties().setId(ITEMS.key(name)).wolfArmor(tier.material());
                return new Item(tier.fireResistant() ? properties.fireResistant() : properties);
            }));
        }
        return Collections.unmodifiableMap(armors);
    }

    private static Map<DyeColor, RegistryObject<BlockItem>> registerDogBeds() {
        Map<DyeColor, RegistryObject<BlockItem>> beds = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            String name = ModBlocks.dogBedName(color);
            beds.put(color, ITEMS.register(name, () -> new BlockItem(ModBlocks.DOG_BEDS.get(color).get(),
                    new Item.Properties().setId(ITEMS.key(name)).useBlockDescriptionPrefix().stacksTo(1))));
        }
        return Collections.unmodifiableMap(beds);
    }

    private ModItems() {
    }
}
