package com.davibogo.faithfulfangs.registry;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.item.EngravedTag;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Custom item data components. */
public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, FaithfulFangs.MODID);

    /** Present only on engraved Dog Tags. */
    public static final RegistryObject<DataComponentType<EngravedTag>> ENGRAVED_TAG =
            DATA_COMPONENTS.register("engraved_tag", () -> DataComponentType.<EngravedTag>builder()
                    .persistent(EngravedTag.CODEC)
                    .networkSynchronized(EngravedTag.STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }
}
