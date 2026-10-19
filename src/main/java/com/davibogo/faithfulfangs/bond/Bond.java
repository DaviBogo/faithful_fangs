package com.davibogo.faithfulfangs.bond;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;

/**
 * A wolf bound to a Dog Bed.
 *
 * @param wolfId   UUID of the wolf
 * @param ownerId  UUID of the wolf's owner when the bond was made (gets the chat warnings)
 * @param wolfName name given through the Dog Tag
 * @param bed      dimension and position of the Dog Bed
 */
public record Bond(UUID wolfId, UUID ownerId, String wolfName, GlobalPos bed) {
    public static final Codec<Bond> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("wolf").forGetter(Bond::wolfId),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(Bond::ownerId),
            Codec.STRING.fieldOf("name").forGetter(Bond::wolfName),
            GlobalPos.CODEC.fieldOf("bed").forGetter(Bond::bed)
    ).apply(instance, Bond::new));

    public Bond withName(String newName) {
        return new Bond(this.wolfId, this.ownerId, newName, this.bed);
    }
}
