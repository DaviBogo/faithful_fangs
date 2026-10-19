package com.davibogo.faithfulfangs;

import com.davibogo.faithfulfangs.client.ClientSetup;
import com.davibogo.faithfulfangs.event.ModEvents;
import com.davibogo.faithfulfangs.network.ModNetwork;
import com.davibogo.faithfulfangs.registry.ModBlockEntities;
import com.davibogo.faithfulfangs.registry.ModBlocks;
import com.davibogo.faithfulfangs.registry.ModDataComponents;
import com.davibogo.faithfulfangs.registry.ModItems;
import com.davibogo.faithfulfangs.registry.ModMenuTypes;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Main class of Faithful Fangs.
 * The MODID value must match the modId in META-INF/mods.toml.
 */
@Mod(FaithfulFangs.MODID)
public final class FaithfulFangs {
    public static final String MODID = "faithful_fangs";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Same color order the vanilla creative tabs use for beds, wool, etc. */
    private static final List<DyeColor> CREATIVE_COLOR_ORDER = List.of(
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK);

    public FaithfulFangs(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        ModBlocks.BLOCKS.register(modBusGroup);
        ModItems.ITEMS.register(modBusGroup);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modBusGroup);
        ModDataComponents.DATA_COMPONENTS.register(modBusGroup);
        ModMenuTypes.MENU_TYPES.register(modBusGroup);

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        BuildCreativeModeTabContentsEvent.BUS.addListener(FaithfulFangs::addCreativeTabItems);
        ModEvents.register();
        ModNetwork.init();
        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.register(modBusGroup);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Faithful Fangs loaded! Your wolves thank you.");
    }

    private static void addCreativeTabItems(final BuildCreativeModeTabContentsEvent event) {
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            // Dog Tag right after the vanilla Name Tag.
            event.getEntries().putAfter(new ItemStack(Items.NAME_TAG), new ItemStack(ModItems.DOG_TAG.get()), visibility);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            // Dog Beds right after the vanilla beds (the straw bed is the last one).
            ItemStack previous = new ItemStack(Items.STRAW_BED);
            for (DyeColor color : CREATIVE_COLOR_ORDER) {
                ItemStack bed = new ItemStack(ModItems.DOG_BEDS.get(color).get());
                event.getEntries().putAfter(previous, bed, visibility);
                previous = bed;
            }
        }
    }
}
