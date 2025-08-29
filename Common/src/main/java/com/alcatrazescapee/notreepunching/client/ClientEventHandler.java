package com.alcatrazescapee.notreepunching.client;

import com.alcatrazescapee.notreepunching.client.screen.LargeVesselScreen;
import com.alcatrazescapee.notreepunching.client.screen.SmallVesselScreen;
import com.alcatrazescapee.notreepunching.common.container.ModContainers;
import net.minecraft.client.gui.screens.MenuScreens;

public final class ClientEventHandler
{
    public static void clientSetup()
    {
        MenuScreens.register(ModContainers.LARGE_VESSEL.get(), LargeVesselScreen::new);
        MenuScreens.register(ModContainers.SMALL_VESSEL.get(), SmallVesselScreen::new);
    }
}