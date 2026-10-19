package com.davibogo.faithfulfangs.registry;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.block.DogBedBlockEntity;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Mod block entity types. */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FaithfulFangs.MODID);

    /** Shared by the Dog Beds of every color. */
    public static final RegistryObject<BlockEntityType<DogBedBlockEntity>> DOG_BED =
            BLOCK_ENTITY_TYPES.register("dog_bed", () -> new BlockEntityType<>(DogBedBlockEntity::new, allDogBeds()));

    private static Set<Block> allDogBeds() {
        return ModBlocks.DOG_BEDS.values().stream()
                .map(bed -> (Block) bed.get())
                .collect(Collectors.toUnmodifiableSet());
    }

    private ModBlockEntities() {
    }
}
