package com.davibogo.faithfulfangs.bond;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

/**
 * A bonded wolf that died and is recovering until it comes back to its bed.
 *
 * @param wolfId     UUID of the wolf
 * @param ownerId    owner to tell when it comes back
 * @param wolfName   its name when it died (for messages)
 * @param lastBed    its bed when it died; it comes back here if the bed is gone by then
 * @param returnTime overworld game time when it comes back
 * @param data       the whole wolf (variant, collar, armor, name, mode...), saved as when an entity is saved with its chunk
 */
public record Recovery(UUID wolfId, UUID ownerId, String wolfName, GlobalPos lastBed, long returnTime, CompoundTag data) {
    public static final Codec<Recovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("wolf").forGetter(Recovery::wolfId),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(Recovery::ownerId),
            Codec.STRING.fieldOf("name").forGetter(Recovery::wolfName),
            GlobalPos.CODEC.fieldOf("last_bed").forGetter(Recovery::lastBed),
            Codec.LONG.fieldOf("return_time").forGetter(Recovery::returnTime),
            CompoundTag.CODEC.fieldOf("data").forGetter(Recovery::data)
    ).apply(instance, Recovery::new));

    public Recovery withReturnTime(long newReturnTime) {
        return new Recovery(this.wolfId, this.ownerId, this.wolfName, this.lastBed, newReturnTime, this.data);
    }
}
