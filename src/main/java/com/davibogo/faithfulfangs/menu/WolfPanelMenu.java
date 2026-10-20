package com.davibogo.faithfulfangs.menu;

import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.bond.BondRegistry;
import com.davibogo.faithfulfangs.registry.ModMenuTypes;
import com.davibogo.faithfulfangs.wolf.WolfCommands;
import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfSleep;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Wolf Panel (Shift + right click on your wolf). A container menu so that later phases can add
 * the armor slot and the player inventory. Health, mode, stance and "has bed" are synced live
 * through data slots; the buttons go through vanilla's container button packet.
 */
public class WolfPanelMenu extends AbstractContainerMenu {
    /** Button ids: mode buttons are MODE_BUTTONS + WolfMode ordinal, stance buttons STANCE_BUTTONS + WolfStance ordinal. */
    public static final int MODE_BUTTONS = 0;
    public static final int STANCE_BUTTONS = 10;

    private static final int DATA_HEALTH = 0;
    private static final int DATA_MAX_HEALTH = 1;
    private static final int DATA_MODE = 2;
    private static final int DATA_STANCE = 3;
    private static final int DATA_HAS_BED = 4;
    private static final int DATA_SLEEPING = 5;
    private static final int DATA_COUNT = 6;

    private static final double MAX_DISTANCE_SQR = 8.0 * 8.0;

    /** Bed position shown in the panel, taken when it opens. */
    public record BedInfo(Identifier dimension, BlockPos pos) {
    }

    private final @Nullable Wolf wolf; // server side only
    private final int wolfEntityId;
    private final @Nullable BedInfo bed;
    private final ContainerData data;

    /** Server side. */
    private WolfPanelMenu(int containerId, Wolf wolf, @Nullable Bond bond) {
        super(ModMenuTypes.WOLF_PANEL.get(), containerId);
        this.wolf = wolf;
        this.wolfEntityId = wolf.getId();
        this.bed = bond == null ? null : new BedInfo(bond.bed().dimension().identifier(), bond.bed().pos());
        MinecraftServer server = ((ServerLevel) wolf.level()).getServer();
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_HEALTH -> Mth.ceil(wolf.getHealth());
                    case DATA_MAX_HEALTH -> Mth.ceil(wolf.getMaxHealth());
                    case DATA_MODE -> WolfMode.current(wolf).ordinal();
                    case DATA_STANCE -> WolfData.getStance(wolf).ordinal();
                    case DATA_HAS_BED -> WolfCommands.hasBed(server, wolf) ? 1 : 0;
                    case DATA_SLEEPING -> WolfSleep.isSleeping(wolf) ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
        this.addDataSlots(this.data);
    }

    /** Client side, built from the extra data written in {@link #open}. */
    public WolfPanelMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.WOLF_PANEL.get(), containerId);
        this.wolf = null;
        this.wolfEntityId = extraData.readVarInt();
        this.bed = extraData.readBoolean() ? new BedInfo(extraData.readIdentifier(), extraData.readBlockPos()) : null;
        this.data = new SimpleContainerData(DATA_COUNT);
        this.addDataSlots(this.data);
    }

    public static void open(ServerPlayer player, Wolf wolf) {
        Bond bond = BondRegistry.get(player.level().getServer()).getByWolf(wolf.getUUID());
        player.openMenu(
                new SimpleMenuProvider((containerId, inventory, p) -> new WolfPanelMenu(containerId, wolf, bond),
                        wolf.getDisplayName()),
                buf -> {
                    buf.writeVarInt(wolf.getId());
                    buf.writeBoolean(bond != null);
                    if (bond != null) {
                        buf.writeIdentifier(bond.bed().dimension().identifier());
                        buf.writeBlockPos(bond.bed().pos());
                    }
                });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (this.wolf == null || !(this.wolf.level() instanceof ServerLevel level)) {
            return false;
        }
        int modeIndex = buttonId - MODE_BUTTONS;
        if (modeIndex >= 0 && modeIndex < WolfMode.values().length) {
            WolfCommands.setMode(level.getServer(), this.wolf, WolfMode.byOrdinal(modeIndex));
            return true;
        }
        int stanceIndex = buttonId - STANCE_BUTTONS;
        if (stanceIndex >= 0 && stanceIndex < WolfStance.values().length) {
            WolfCommands.setStance(this.wolf, WolfStance.byOrdinal(stanceIndex));
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.wolf == null
                || this.wolf.isAlive() && this.wolf.isOwnedBy(player) && player.distanceToSqr(this.wolf) <= MAX_DISTANCE_SQR;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY; // no slots yet (armor slot comes in phase 5)
    }

    // ------------------------------------------------------------ read by the screen (client)

    public int getWolfEntityId() {
        return this.wolfEntityId;
    }

    public @Nullable BedInfo getBed() {
        return this.bed;
    }

    /** Health points (1 point = half a heart), rounded up. */
    public int getHealth() {
        return this.data.get(DATA_HEALTH);
    }

    public int getMaxHealth() {
        return this.data.get(DATA_MAX_HEALTH);
    }

    public WolfMode getMode() {
        return WolfMode.byOrdinal(this.data.get(DATA_MODE));
    }

    public WolfStance getStance() {
        return WolfStance.byOrdinal(this.data.get(DATA_STANCE));
    }

    public boolean hasBed() {
        return this.data.get(DATA_HAS_BED) != 0;
    }

    /** Curled up asleep in its Dog Bed (Stay Home at night). */
    public boolean isSleeping() {
        return this.data.get(DATA_SLEEPING) != 0;
    }
}
