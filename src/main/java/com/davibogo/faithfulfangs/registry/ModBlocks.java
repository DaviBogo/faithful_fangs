package com.davibogo.faithfulfangs.registry;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.block.DogBedBlock;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Mod blocks. */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, FaithfulFangs.MODID);

    /** One Dog Bed per wool color, named like vanilla beds: white_dog_bed, orange_dog_bed, ... */
    public static final Map<DyeColor, RegistryObject<DogBedBlock>> DOG_BEDS = registerDogBeds();

    private static Map<DyeColor, RegistryObject<DogBedBlock>> registerDogBeds() {
        Map<DyeColor, RegistryObject<DogBedBlock>> beds = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            String name = dogBedName(color);
            beds.put(color, BLOCKS.register(name, () -> new DogBedBlock(color, BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key(name))
                    .mapColor(color.getMapColor())
                    .sound(SoundType.WOOL)
                    .strength(0.2F)
                    .noOcclusion())));
        }
        return Collections.unmodifiableMap(beds);
    }

    public static String dogBedName(DyeColor color) {
        return color.getName() + "_dog_bed";
    }

    private ModBlocks() {
    }
}
