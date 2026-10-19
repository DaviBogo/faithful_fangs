package com.davibogo.faithfulfangs.registry;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.menu.WolfPanelMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Mod menu (container) types. */
public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, FaithfulFangs.MODID);

    public static final RegistryObject<MenuType<WolfPanelMenu>> WOLF_PANEL =
            MENU_TYPES.register("wolf_panel", () -> IForgeMenuType.create(WolfPanelMenu::new));

    private ModMenuTypes() {
    }
}
