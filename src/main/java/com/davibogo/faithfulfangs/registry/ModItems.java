package com.davibogo.faithfulfangs.registry;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.item.DogTagItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Mod items. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, FaithfulFangs.MODID);

    public static final RegistryObject<Item> DOG_TAG = ITEMS.register("dog_tag",
            () -> new DogTagItem(new Item.Properties().setId(ITEMS.key("dog_tag"))));

    private ModItems() {
    }
}
