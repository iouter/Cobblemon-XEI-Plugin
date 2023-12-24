package com.iouter.cobblemonxeiplugin.forge;

import com.iouter.cobblemonxeiplugin.CobblemonXEIPlugin;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CobblemonXEIPlugin.MOD_ID)
public class CobblemonXEIPluginForge {
    public CobblemonXEIPluginForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(CobblemonXEIPlugin.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CobblemonXEIPlugin.init();
    }
}
