package com.davibogo.faithfulfangs.wolf;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The wolf's attribute upgrades (Phase 4), bought in the Wolf Panel with special bones. Each has
 * {@link #MAX_LEVEL} levels; the price of a level is the same for every attribute ({@link #cost}).
 */
public enum WolfUpgrade {
    /** +2 hearts per level. */
    HEALTH("health", Attributes.MAX_HEALTH, 4.0, AttributeModifier.Operation.ADD_VALUE, 0.5),
    /** +1 attack damage per level. */
    STRENGTH("strength", Attributes.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_VALUE, 1.0),
    /** +5% speed per level. */
    AGILITY("agility", Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 100.0),
    /** +15% scent per level: the owner's exploring counts that much more towards finds. */
    SCENT("scent", null, 0.15, null, 100.0);

    public static final int MAX_LEVEL = 5;

    private final String id;
    private final @Nullable Holder<Attribute> attribute;
    private final double perLevel;
    private final AttributeModifier.@Nullable Operation operation;
    private final Identifier modifierId;
    /** Turns the raw bonus into the number shown to the player (hearts, damage, percent). */
    private final double displayFactor;

    WolfUpgrade(String id, @Nullable Holder<Attribute> attribute, double perLevel,
                AttributeModifier.@Nullable Operation operation, double displayFactor) {
        this.id = id;
        this.attribute = attribute;
        this.perLevel = perLevel;
        this.operation = operation;
        this.displayFactor = displayFactor;
        this.modifierId = Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, "upgrade_" + id);
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return "upgrade.faithful_fangs." + this.id;
    }

    /** The attribute it raises, or null for Scent (handled by {@link WolfScent}). */
    public @Nullable Holder<Attribute> attribute() {
        return this.attribute;
    }

    public double perLevel() {
        return this.perLevel;
    }

    public Identifier modifierId() {
        return this.modifierId;
    }

    /** Total bonus at {@code level} as shown to the player: hearts, attack damage or percent. */
    public long displayTotal(int level) {
        return Math.round(this.perLevel * level * this.displayFactor);
    }

    /** The attribute modifier for {@code level} (attribute upgrades only). */
    public AttributeModifier modifier(int level) {
        if (this.operation == null) {
            throw new IllegalStateException(this.id + " is not an attribute upgrade");
        }
        return new AttributeModifier(this.modifierId, this.perLevel * level, this.operation);
    }

    public static WolfUpgrade byOrdinal(int ordinal) {
        WolfUpgrade[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : HEALTH;
    }

    /** Price of reaching {@code level} (1 to 5). */
    public static Cost cost(int level) {
        return switch (level) {
            case 1 -> new Cost(ModItems.ANCIENT_BONE, 1);
            case 2 -> new Cost(ModItems.ANCIENT_BONE, 2);
            case 3 -> new Cost(ModItems.BLAZING_BONE, 1);
            case 4 -> new Cost(ModItems.BLAZING_BONE, 2);
            default -> new Cost(ModItems.ETHEREAL_BONE, 1);
        };
    }

    /** Some special bones, taken from anywhere in the player's inventory (free in creative). */
    public record Cost(Supplier<Item> bone, int count) {
        public Item item() {
            return this.bone.get();
        }

        public boolean canPay(Player player) {
            return player.hasInfiniteMaterials() || countBones(player, this.item()) >= this.count;
        }

        /** Takes the bones; call only after {@link #canPay}. */
        public void pay(Player player) {
            if (player.hasInfiniteMaterials()) {
                return;
            }
            Inventory inventory = player.getInventory();
            int left = this.count;
            for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.is(this.item())) {
                    int taken = Math.min(left, stack.getCount());
                    stack.shrink(taken);
                    left -= taken;
                }
            }
            inventory.setChanged();
        }
    }

    /** How many of {@code bone} the player carries (whole inventory, armor and offhand included). */
    public static int countBones(Player player, Item bone) {
        Inventory inventory = player.getInventory();
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(bone)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
