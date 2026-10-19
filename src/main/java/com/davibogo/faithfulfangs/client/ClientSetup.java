package com.davibogo.faithfulfangs.client;

import com.davibogo.faithfulfangs.registry.ModBlockEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** Client-only registrations. Only called on the physical client. */
public final class ClientSetup {
    private ClientSetup() {
    }

    public static void register() {
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(ClientSetup::onRegisterRenderers);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DOG_BED.get(), DogBedRenderer::new);
    }
}
