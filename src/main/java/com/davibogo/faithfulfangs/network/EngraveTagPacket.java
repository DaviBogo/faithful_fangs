package com.davibogo.faithfulfangs.network;

import com.davibogo.faithfulfangs.item.DogTagItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Client → server: the player confirmed a name in the Dog Tag screen.
 * The server checks everything again before engraving or renaming.
 */
public record EngraveTagPacket(int wolfEntityId, String name) {
    /** Generous limit for the raw text; the real limit is checked by EngravedTag.sanitizeName. */
    private static final int MAX_RAW_LENGTH = 256;

    public static final StreamCodec<RegistryFriendlyByteBuf, EngraveTagPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EngraveTagPacket::wolfEntityId,
            ByteBufCodecs.stringUtf8(MAX_RAW_LENGTH), EngraveTagPacket::name,
            EngraveTagPacket::new);

    public static void handle(EngraveTagPacket packet, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            DogTagItem.handleNameConfirmed(player, packet.wolfEntityId(), packet.name());
        }
    }
}
