package com.davibogo.faithfulfangs.event;

import com.davibogo.faithfulfangs.bond.BondManager;
import com.davibogo.faithfulfangs.command.FaithfulFangsCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

/** Listeners on the Forge (game) event buses. */
public final class ServerEvents {
    private ServerEvents() {
    }

    public static void register() {
        EntityJoinLevelEvent.BUS.addListener(ServerEvents::onEntityJoinLevel);
        RegisterCommandsEvent.BUS.addListener(ServerEvents::onRegisterCommands);
    }

    /** A wolf that loads (or changes dimension) catches up with bond changes made while it was away. */
    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Wolf wolf) {
            BondManager.syncWolf(level.getServer(), wolf);
        }
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        FaithfulFangsCommand.register(event.getDispatcher());
    }
}
