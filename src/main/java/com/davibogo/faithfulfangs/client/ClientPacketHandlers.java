package com.davibogo.faithfulfangs.client;

import com.davibogo.faithfulfangs.network.OpenTagScreenPacket;
import net.minecraft.client.Minecraft;

/** Client-side handling of packets. Only called on the physical client. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void openTagScreen(OpenTagScreenPacket packet) {
        Minecraft.getInstance().gui.setScreen(
                new DogTagScreen(packet.wolfEntityId(), packet.currentName(), packet.rename()));
    }
}
