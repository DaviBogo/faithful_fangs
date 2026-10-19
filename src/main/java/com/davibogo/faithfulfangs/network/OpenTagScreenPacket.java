package com.davibogo.faithfulfangs.network;

import com.davibogo.faithfulfangs.client.ClientPacketHandlers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Server → client: open the screen to type a wolf's name on a blank Dog Tag.
 *
 * @param wolfEntityId network id of the wolf
 * @param currentName  name to pre-fill (current bond name or vanilla name), may be empty
 * @param rename       true if the wolf already has a bed, so the name is changed right away
 */
public record OpenTagScreenPacket(int wolfEntityId, String currentName, boolean rename) {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTagScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenTagScreenPacket::wolfEntityId,
            ByteBufCodecs.STRING_UTF8, OpenTagScreenPacket::currentName,
            ByteBufCodecs.BOOL, OpenTagScreenPacket::rename,
            OpenTagScreenPacket::new);

    public static void handle(OpenTagScreenPacket packet, CustomPayloadEvent.Context context) {
        // Client-only classes are only touched on the physical client.
        if (FMLEnvironment.dist.isClient()) {
            ClientPacketHandlers.openTagScreen(packet);
        }
    }
}
