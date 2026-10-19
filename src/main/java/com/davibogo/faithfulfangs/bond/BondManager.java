package com.davibogo.faithfulfangs.bond;

import com.davibogo.faithfulfangs.block.DogBedBlockEntity;
import com.davibogo.faithfulfangs.wolf.WolfData;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jspecify.annotations.Nullable;

/**
 * All changes to bonds go through here. The {@link BondRegistry} is updated first; then the
 * copies on the bed (block entity) and on the wolf are updated if they are loaded right now.
 * If they are not, {@link #syncBed} and {@link #syncWolf} fix them when they load.
 */
public final class BondManager {
    private BondManager() {
    }

    /** Result of {@link #bind}. {@code takenBy} is set when the bed already belongs to another wolf. */
    public record BindResult(boolean success, @Nullable Bond takenBy, @Nullable Bond previous) {
    }

    /**
     * Binds a wolf to the Dog Bed at {@code bedPos}. If the wolf already had another bed, that bed
     * becomes free. Fails if the bed belongs to a different wolf.
     */
    public static BindResult bind(ServerLevel bedLevel, BlockPos bedPos, UUID wolfId, UUID ownerId, String name) {
        MinecraftServer server = bedLevel.getServer();
        BondRegistry registry = BondRegistry.get(server);
        GlobalPos bed = GlobalPos.of(bedLevel.dimension(), bedPos.immutable());

        Bond occupant = registry.getByBed(bed);
        if (occupant != null && !occupant.wolfId().equals(wolfId)) {
            return new BindResult(false, occupant, null);
        }

        Bond previous = registry.getByWolf(wolfId);
        registry.put(new Bond(wolfId, ownerId, name, bed));

        if (previous != null && !previous.bed().equals(bed)) {
            refreshBedIfLoaded(server, previous.bed());
        }
        refreshBedIfLoaded(server, bed);
        refreshWolfIfLoaded(server, wolfId);
        return new BindResult(true, null, previous);
    }

    /** Removes the bond of a wolf (its bed becomes free and the wolf loses the bond's name). */
    public static @Nullable Bond unbind(MinecraftServer server, UUID wolfId) {
        Bond removed = BondRegistry.get(server).remove(wolfId);
        if (removed != null) {
            refreshBedIfLoaded(server, removed.bed());
            refreshWolfIfLoaded(server, wolfId);
        }
        return removed;
    }

    /**
     * The bed at {@code pos} is being removed: its wolf loses the bond and the name (upgrades stay,
     * they live on the wolf). Returns the removed bond, or null if the bed was free.
     */
    public static @Nullable Bond onBedRemoved(ServerLevel level, BlockPos pos) {
        MinecraftServer server = level.getServer();
        BondRegistry registry = BondRegistry.get(server);
        Bond bond = registry.getByBed(GlobalPos.of(level.dimension(), pos));
        if (bond != null) {
            registry.remove(bond.wolfId());
            refreshWolfIfLoaded(server, bond.wolfId());
        }
        return bond;
    }

    /** Changes the name of a bound wolf, on the wolf and on its bed. Returns false if the wolf has no bond. */
    public static boolean rename(MinecraftServer server, UUID wolfId, String newName) {
        BondRegistry registry = BondRegistry.get(server);
        Bond bond = registry.getByWolf(wolfId);
        if (bond == null) {
            return false;
        }
        registry.put(bond.withName(newName));
        refreshBedIfLoaded(server, bond.bed());
        refreshWolfIfLoaded(server, wolfId);
        return true;
    }

    /** Makes the bed's copy of the bond match the registry. Called when the bed loads and after changes. */
    public static void syncBed(ServerLevel level, DogBedBlockEntity bed) {
        GlobalPos pos = GlobalPos.of(level.dimension(), bed.getBlockPos());
        bed.applyBond(BondRegistry.get(level.getServer()).getByBed(pos));
    }

    /**
     * Makes the wolf match the registry. Called when the wolf joins a level and after changes.
     * <ul>
     *   <li>Bound wolf whose name was set (or changed) while it was away: gets the bond's name.</li>
     *   <li>Wolf whose bond is gone: loses the name, unless the player renamed it since (vanilla Name Tag).</li>
     * </ul>
     */
    public static void syncWolf(MinecraftServer server, Wolf wolf) {
        Bond bond = BondRegistry.get(server).getByWolf(wolf.getUUID());
        String appliedName = WolfData.getBondName(wolf);
        if (bond != null) {
            if (!bond.wolfName().equals(appliedName)) {
                wolf.setCustomName(Component.literal(bond.wolfName()));
                WolfData.setBondName(wolf, bond.wolfName());
            }
        } else if (appliedName != null) {
            Component current = wolf.getCustomName();
            if (current != null && current.getString().equals(appliedName)) {
                wolf.setCustomName(null);
            }
            WolfData.setBondName(wolf, null);
        }
    }

    /** Finds a wolf by UUID in any loaded dimension, without loading anything. */
    public static @Nullable Wolf findLoadedWolf(MinecraftServer server, UUID wolfId) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(wolfId) instanceof Wolf wolf) {
                return wolf;
            }
        }
        return null;
    }

    private static void refreshWolfIfLoaded(MinecraftServer server, UUID wolfId) {
        Wolf wolf = findLoadedWolf(server, wolfId);
        if (wolf != null) {
            syncWolf(server, wolf);
        }
    }

    private static void refreshBedIfLoaded(MinecraftServer server, GlobalPos bed) {
        ServerLevel level = server.getLevel(bed.dimension());
        if (level != null && level.isLoaded(bed.pos())
                && level.getBlockEntity(bed.pos()) instanceof DogBedBlockEntity bedEntity) {
            syncBed(level, bedEntity);
        }
    }
}
