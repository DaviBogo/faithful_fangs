package com.davibogo.faithfulfangs.network;

import com.davibogo.faithfulfangs.FaithfulFangs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/** The mod's network channel and its packets. */
public final class ModNetwork {
    /** Bump when packets change in an incompatible way; client and server must match. */
    private static final int PROTOCOL_VERSION = 1;

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .simpleChannel();

    private ModNetwork() {
    }

    /** Registers all packets. Called once from the mod constructor. */
    public static void init() {
        CHANNEL.messageBuilder(OpenTagScreenPacket.class, NetworkDirection.PLAY_TO_CLIENT)
                .codec(OpenTagScreenPacket.STREAM_CODEC)
                .consumerMainThread(OpenTagScreenPacket::handle)
                .add();
        CHANNEL.messageBuilder(EngraveTagPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .codec(EngraveTagPacket.STREAM_CODEC)
                .consumerMainThread(EngraveTagPacket::handle)
                .add();
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(Object packet) {
        CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }
}
