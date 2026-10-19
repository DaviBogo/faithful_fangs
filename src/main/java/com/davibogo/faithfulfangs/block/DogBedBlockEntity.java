package com.davibogo.faithfulfangs.block;

import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.bond.BondManager;
import com.davibogo.faithfulfangs.registry.ModBlockEntities;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Keeps which wolf a Dog Bed belongs to. The wolf name is a copy used for display
 * (floating name over the bed); it is synced to clients.
 */
public class DogBedBlockEntity extends BlockEntity {
    private static final String TAG_WOLF = "wolf";
    private static final String TAG_WOLF_NAME = "wolf_name";

    private @Nullable UUID wolfId;
    private String wolfName = "";

    public DogBedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DOG_BED.get(), pos, state);
    }

    public boolean isBound() {
        return this.wolfId != null;
    }

    public @Nullable UUID getWolfId() {
        return this.wolfId;
    }

    public String getWolfName() {
        return this.wolfName;
    }

    /**
     * Updates this bed's copy of its bond (null = free bed). Only {@link BondManager} should call this;
     * the {@link com.davibogo.faithfulfangs.bond.BondRegistry} is the source of truth.
     */
    public void applyBond(@Nullable Bond bond) {
        UUID newWolf = bond == null ? null : bond.wolfId();
        String newName = bond == null ? "" : bond.wolfName();
        if (!Objects.equals(this.wolfId, newWolf) || !this.wolfName.equals(newName)) {
            this.wolfId = newWolf;
            this.wolfName = newName;
            this.markUpdated();
        }
    }

    /** Called on the server when the bed block is removed for any reason (broken, exploded, replaced). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level instanceof ServerLevel serverLevel) {
            BondManager.onBedRemoved(serverLevel, pos);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // The bond may have changed while this chunk was unloaded (e.g. the wolf moved to another bed).
        if (this.level instanceof ServerLevel serverLevel) {
            BondManager.syncBed(serverLevel, this);
        }
    }

    /** Saves the change and sends it to clients tracking this block. */
    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.wolfId = input.read(TAG_WOLF, UUIDUtil.CODEC).orElse(null);
        this.wolfName = input.getStringOr(TAG_WOLF_NAME, "");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(TAG_WOLF, UUIDUtil.CODEC, this.wolfId);
        if (!this.wolfName.isEmpty()) {
            output.putString(TAG_WOLF_NAME, this.wolfName);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
