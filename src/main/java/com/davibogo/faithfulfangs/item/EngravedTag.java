package com.davibogo.faithfulfangs.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringUtil;
import org.jspecify.annotations.Nullable;

/**
 * Data stored on an engraved Dog Tag: which wolf it belongs to and the name chosen for it.
 *
 * @param wolfId   UUID of the wolf the tag was engraved for
 * @param wolfName name engraved on the tag
 */
public record EngravedTag(UUID wolfId, String wolfName) {
    /** Longest name a player can engrave (same limit as the vanilla anvil). */
    public static final int MAX_NAME_LENGTH = 50;

    public static final Codec<EngravedTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("wolf").forGetter(EngravedTag::wolfId),
            Codec.STRING.fieldOf("name").forGetter(EngravedTag::wolfName)
    ).apply(instance, EngravedTag::new));

    public static final StreamCodec<ByteBuf, EngravedTag> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, EngravedTag::wolfId,
            ByteBufCodecs.STRING_UTF8, EngravedTag::wolfName,
            EngravedTag::new);

    /**
     * Cleans a name typed by a player (removes control/formatting characters and outer spaces).
     * Returns null if nothing is left or it is longer than {@link #MAX_NAME_LENGTH}.
     */
    public static @Nullable String sanitizeName(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String name = StringUtil.filterText(raw).trim();
        return name.isEmpty() || name.length() > MAX_NAME_LENGTH ? null : name;
    }
}
