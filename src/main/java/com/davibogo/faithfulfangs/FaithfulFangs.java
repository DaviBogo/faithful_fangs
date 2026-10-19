package com.davibogo.faithfulfangs;

import com.davibogo.faithfulfangs.registry.ModDataComponents;
import com.davibogo.faithfulfangs.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Main class of Faithful Fangs.
 * The MODID value must match the modId in META-INF/mods.toml.
 */
@Mod(FaithfulFangs.MODID)
public final class FaithfulFangs {
    public static final String MODID = "faithful_fangs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FaithfulFangs(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        ModDataComponents.DATA_COMPONENTS.register(modBusGroup);
        ModItems.ITEMS.register(modBusGroup);

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        BuildCreativeModeTabContentsEvent.BUS.addListener(FaithfulFangs::addCreativeTabItems);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Faithful Fangs loaded! Your wolves thank you.");
    }

    private static void addCreativeTabItems(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            // Place the Dog Tag right after the vanilla Name Tag.
            event.getEntries().putAfter(
                    new ItemStack(Items.NAME_TAG),
                    new ItemStack(ModItems.DOG_TAG.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
