package com.davibogo.faithfulfangs.bond;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * World-wide list of wolf/bed bonds, saved with the world (data/faithful_fangs/bonds.dat).
 * This is the source of truth: the copies kept on the wolf and on the bed are reconciled
 * against it whenever they load, so changes made while they were unloaded are applied later.
 */
public final class BondRegistry extends SavedData {
    public static final Codec<BondRegistry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Bond.CODEC.listOf().optionalFieldOf("bonds", List.of()).forGetter(BondRegistry::bondList)
    ).apply(instance, BondRegistry::new));

    public static final SavedDataType<BondRegistry> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, "bonds"),
            BondRegistry::new,
            CODEC,
            null); // Forge allows a null data fixer: we keep track of our own format.

    private final Map<UUID, Bond> byWolf = new LinkedHashMap<>();
    private final Map<GlobalPos, UUID> byBed = new HashMap<>();

    public BondRegistry() {
    }

    private BondRegistry(List<Bond> bonds) {
        for (Bond bond : bonds) {
            this.putInternal(bond);
        }
    }

    public static BondRegistry get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    private List<Bond> bondList() {
        return List.copyOf(this.byWolf.values());
    }

    public Collection<Bond> all() {
        return Collections.unmodifiableCollection(this.byWolf.values());
    }

    public @Nullable Bond getByWolf(UUID wolfId) {
        return this.byWolf.get(wolfId);
    }

    public @Nullable Bond getByBed(GlobalPos bed) {
        UUID wolfId = this.byBed.get(bed);
        return wolfId == null ? null : this.byWolf.get(wolfId);
    }

    /** Adds or replaces the bond of {@code bond.wolfId()}. The wolf's previous bed (if any) becomes free. */
    public void put(Bond bond) {
        this.putInternal(bond);
        this.setDirty();
    }

    public @Nullable Bond remove(UUID wolfId) {
        Bond removed = this.byWolf.remove(wolfId);
        if (removed != null) {
            this.byBed.remove(removed.bed(), wolfId);
            this.setDirty();
        }
        return removed;
    }

    private void putInternal(Bond bond) {
        Bond previous = this.byWolf.put(bond.wolfId(), bond);
        if (previous != null) {
            this.byBed.remove(previous.bed(), previous.wolfId());
        }
        UUID displaced = this.byBed.put(bond.bed(), bond.wolfId());
        if (displaced != null && !displaced.equals(bond.wolfId())) {
            // One wolf per bed: should never happen, callers check first.
            this.byWolf.remove(displaced);
            FaithfulFangs.LOGGER.warn("Bed {} was bound to two wolves; dropped the bond of {}", bond.bed(), displaced);
        }
    }
}
