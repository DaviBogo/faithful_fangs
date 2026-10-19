package com.davibogo.faithfulfangs.registry;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.item.DogTagItem;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Mod items. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, FaithfulFangs.MODID);

    public static final RegistryObject<Item> DOG_TAG = ITEMS.register("dog_tag",
            () -> new DogTagItem(new Item.Properties().setId(ITEMS.key("dog_tag"))));

    /** Block items for the Dog Beds; they stack to 1 like vanilla beds. */
    public static final Map<DyeColor, RegistryObject<BlockItem>> DOG_BEDS = registerDogBeds();

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
