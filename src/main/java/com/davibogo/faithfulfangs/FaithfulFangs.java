package com.davibogo.faithfulfangs;

import com.mojang.logging.LogUtils;
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

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Faithful Fangs loaded! Your wolves thank you.");
    }
}
