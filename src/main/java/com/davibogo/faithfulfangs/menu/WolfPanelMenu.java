package com.davibogo.faithfulfangs.menu;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.bond.BondRegistry;
import com.davibogo.faithfulfangs.registry.ModMenuTypes;
import com.davibogo.faithfulfangs.wolf.WolfCommands;
import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfSleep;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import com.davibogo.faithfulfangs.wolf.WolfUpgrade;
import com.davibogo.faithfulfangs.wolf.WolfUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Wolf Panel (Shift + right click on your wolf). Slots: the wolf's armor (body slot, like the
 * horse screen; not for puppies, which cannot wear armor) and the player's inventory. Health, mode,
 * stance, "has bed" and the upgrade levels are synced live through data slots; the buttons go
 * through vanilla's container button packet.
 */
public class WolfPanelMenu extends AbstractContainerMenu {
    /** Button ids: mode buttons are MODE_BUTTONS + WolfMode ordinal, stance buttons STANCE_BUTTONS + WolfStance ordinal. */
    public static final int MODE_BUTTONS = 0;
    public static final int STANCE_BUTTONS = 10;
    /** Upgrade buttons: UPGRADE_BUTTONS + WolfUpgrade ordinal. */
    public static final int UPGRADE_BUTTONS = 20;

    private static final int DATA_HEALTH = 0;
    private static final int DATA_MAX_HEALTH = 1;
    private static final int DATA_MODE = 2;
    private static final int DATA_STANCE = 3;
    private static final int DATA_HAS_BED = 4;
    private static final int DATA_SLEEPING = 5;
    /** Upgrade levels: DATA_UPGRADES + WolfUpgrade ordinal. */
    private static final int DATA_UPGRADES = 6;
    private static final int DATA_COUNT = DATA_UPGRADES + WolfUpgrade.values().length;

    private static final double MAX_DISTANCE_SQR = 8.0 * 8.0;

    /** Slot layout: the armor slot, then the player's inventory (27 slots) and hotbar (9). */
    public static final int ARMOR_SLOT = 0;
    private static final int INVENTORY_START = 1;
    private static final int HOTBAR_START = INVENTORY_START + 27;
    private static final int INVENTORY_END = HOTBAR_START + 9;
    /** Slot positions in the screen (item corner, 1 pixel inside the slot frame). */
    public static final int ARMOR_SLOT_X = 8;
    public static final int ARMOR_SLOT_Y = 18;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = 164;
    /** Wolf armor silhouette shown in the empty armor slot. */
    private static final Identifier EMPTY_ARMOR_SLOT =
            Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, "container/slot/wolf_armor");

    /** Bed position shown in the panel, taken when it opens. */
    public record BedInfo(Identifier dimension, BlockPos pos) {
    }

    private final @Nullable Wolf wolf; // server side only
    private final @Nullable Player player; // server side only
    private final int wolfEntityId;
    private final @Nullable BedInfo bed;
    private final ContainerData data;

    /** Server side. */
    private WolfPanelMenu(int containerId, Player player, Wolf wolf, @Nullable Bond bond) {
        super(ModMenuTypes.WOLF_PANEL.get(), containerId);
        this.wolf = wolf;
        this.player = player;
        this.addSlots(player.getInventory(), wolf);
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
                    default -> index >= DATA_UPGRADES && index < DATA_COUNT
                            ? WolfUpgrades.level(wolf, WolfUpgrade.byOrdinal(index - DATA_UPGRADES))
                            : 0;
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
        this.player = null;
        this.wolfEntityId = extraData.readVarInt();
        this.bed = extraData.readBoolean() ? new BedInfo(extraData.readIdentifier(), extraData.readBlockPos()) : null;
        this.addSlots(playerInventory,
                playerInventory.player.level().getEntity(this.wolfEntityId) instanceof Wolf clientWolf ? clientWolf : null);
        this.data = new SimpleContainerData(DATA_COUNT);
        this.addDataSlots(this.data);
    }

    /** Same slots on both sides: the client builds its armor slot on its copy of the wolf. */
    private void addSlots(Inventory playerInventory, @Nullable Wolf wolf) {
        if (wolf != null) {
            this.addSlot(new ArmorSlot(wolf.createEquipmentSlotContainer(EquipmentSlot.BODY), wolf, EquipmentSlot.BODY,
                    0, ARMOR_SLOT_X, ARMOR_SLOT_Y, EMPTY_ARMOR_SLOT) {
                @Override
                public boolean isActive() {
                    return !wolf.isBaby();
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !wolf.isBaby() && super.mayPlace(stack);
                }
            });
        } else {
            // The wolf is not loaded on this client (should not happen): an inert slot keeps the layout.
            this.addSlot(new Slot(new SimpleContainer(1), 0, ARMOR_SLOT_X, ARMOR_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        this.addStandardInventorySlots(playerInventory, INVENTORY_X, INVENTORY_Y);
    }

    public static void open(ServerPlayer player, Wolf wolf) {
        Bond bond = BondRegistry.get(player.level().getServer()).getByWolf(wolf.getUUID());
        player.openMenu(
                new SimpleMenuProvider((containerId, inventory, p) -> new WolfPanelMenu(containerId, p, wolf, bond),
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
        int upgradeIndex = buttonId - UPGRADE_BUTTONS;
        if (upgradeIndex >= 0 && upgradeIndex < WolfUpgrade.values().length && player instanceof ServerPlayer serverPlayer) {
            return WolfUpgrades.upgrade(serverPlayer, this.wolf, WolfUpgrade.byOrdinal(upgradeIndex))
                    == WolfUpgrades.Result.UPGRADED;
        }
        return false;
    }

    /**
     * The panel only shows the main inventory and hotbar; also send changes to the rest (offhand,
     * worn armor), where bones may be taken from too, so the bone counts stay right.
     */
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.player != null) {
            this.player.inventoryMenu.broadcastChanges();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.wolf == null
                || this.wolf.isAlive() && this.wolf.isOwnedBy(player) && player.distanceToSqr(this.wolf) <= MAX_DISTANCE_SQR;
    }

    /** Shift + click: armor to the inventory; wolf armor from the inventory to the wolf; otherwise inventory <-> hotbar. */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        Slot armorSlot = this.slots.get(ARMOR_SLOT);
        if (slotIndex == ARMOR_SLOT) {
            if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (armorSlot.isActive() && !armorSlot.hasItem() && armorSlot.mayPlace(stack)) {
            if (!this.moveItemStackTo(stack, ARMOR_SLOT, ARMOR_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < HOTBAR_START) {
            if (!this.moveItemStackTo(stack, HOTBAR_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
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

    /** Upgrade level (0 to 5). */
    public int getUpgradeLevel(WolfUpgrade upgrade) {
        return this.data.get(DATA_UPGRADES + upgrade.ordinal());
    }

    /** Curled up asleep in its Dog Bed (Stay Home at night). */
    public boolean isSleeping() {
        return this.data.get(DATA_SLEEPING) != 0;
    }
}
