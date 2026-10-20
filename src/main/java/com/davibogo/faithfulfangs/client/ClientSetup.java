package com.davibogo.faithfulfangs.client;

import com.davibogo.faithfulfangs.client.wolf.ClosedEyesTextures;
import com.davibogo.faithfulfangs.client.wolf.CurlingWolfRenderer;
import com.davibogo.faithfulfangs.registry.ModBlockEntities;
import com.davibogo.faithfulfangs.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.EntityTypes;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only registrations. Only called on the physical client. */
public final class ClientSetup {
    private ClientSetup() {
    }

    public static void register(BusGroup modBusGroup) {
        FMLClientSetupEvent.getBus(modBusGroup).addListener(ClientSetup::onClientSetup);
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(ClientSetup::onRegisterRenderers);
        RegisterClientReloadListenersEvent.BUS.addListener(
                event -> event.registerReloadListener(ClosedEyesTextures.INSTANCE));
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.WOLF_PANEL.get(), WolfPanelScreen::new));
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DOG_BED.get(), DogBedRenderer::new);
        // Vanilla wolf renderer + the curled-up sleeping pose.
        event.registerEntityRenderer(EntityTypes.WOLF, CurlingWolfRenderer::new);
    }
}
