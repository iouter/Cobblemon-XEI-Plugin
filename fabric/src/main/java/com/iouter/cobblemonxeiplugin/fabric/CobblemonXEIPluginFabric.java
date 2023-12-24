package com.iouter.cobblemonxeiplugin.fabric;

import com.iouter.cobblemonxeiplugin.CobblemonXEIPlugin;
import net.fabricmc.api.ModInitializer;

public class CobblemonXEIPluginFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CobblemonXEIPlugin.init();
    }
}
