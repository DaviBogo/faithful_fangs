package com.davibogo.faithfulfangs.event;

import com.davibogo.faithfulfangs.bond.BondManager;
import com.davibogo.faithfulfangs.command.FaithfulFangsCommand;
import com.davibogo.faithfulfangs.item.DogTagItem;
import com.davibogo.faithfulfangs.item.EngravedTag;
import com.davibogo.faithfulfangs.menu.WolfPanelMenu;
import com.davibogo.faithfulfangs.registry.ModItems;
import com.davibogo.faithfulfangs.wolf.WolfHome;
import com.davibogo.faithfulfangs.wolf.ai.WolfGoals;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Listeners on the Forge (game) event buses. Some run on both logical sides. */
public final class ModEvents {
    private ModEvents() {
    }

    public static void register() {
        EntityJoinLevelEvent.BUS.addListener(ModEvents::onEntityJoinLevel);
        // Predicate listener: returning true cancels the event.
        PlayerInteractEvent.EntityInteractSpecific.BUS.addListener(
                (Predicate<PlayerInteractEvent.EntityInteractSpecific>) ModEvents::onEntityInteract);
        RegisterCommandsEvent.BUS.addListener(ModEvents::onRegisterCommands);
    }

    /**
     * A wolf that loads (or changes dimension) gets the mod's AI goals and catches up with bond
     * changes made while it was away.
     */
    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Wolf wolf) {
            WolfGoals.install(wolf);
            BondManager.syncWolf(level.getServer(), wolf);
            WolfHome.onJoin(wolf);
        }
    }

    /**
     * Right click on a wolf. Runs before the wolf's own interaction (which would make it sit),
     * on both sides. Returning true cancels the vanilla interaction.
     */
    private static boolean onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getTarget() instanceof Wolf wolf)) {
            return false;
        }
        ItemStack stack = event.getItemStack();

        // Dog Tag on a wolf: handled by the mod only (no sitting).
        if (stack.is(ModItems.DOG_TAG.get())) {
            if (event.getEntity() instanceof ServerPlayer player) {
                DogTagItem.useOnWolf(player, stack, wolf);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        // Shift + right click on your wolf: Wolf Panel. A plain right click stays vanilla (sit / stand, feed).
        Player player = event.getEntity();
        if (player.isSecondaryUseActive() && wolf.isTame() && wolf.isOwnedBy(player)) {
            if (player instanceof ServerPlayer serverPlayer) {
                WolfPanelMenu.open(serverPlayer, wolf);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        // Vanilla Name Tag on a wolf with a bed: the bed follows the new name. Vanilla still applies the tag.
        if (stack.is(Items.NAME_TAG) && player instanceof ServerPlayer serverPlayer) {
            Component customName = stack.get(DataComponents.CUSTOM_NAME);
            String name = customName == null ? null : EngravedTag.sanitizeName(customName.getString());
            if (name != null) {
                BondManager.rename(serverPlayer.level().getServer(), wolf.getUUID(), name);
            }
        }
        return false;
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        FaithfulFangsCommand.register(event.getDispatcher());
    }
}
