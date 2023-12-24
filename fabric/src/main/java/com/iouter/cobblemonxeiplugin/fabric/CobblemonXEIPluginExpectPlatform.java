package com.iouter.cobblemonxeiplugin.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class CobblemonXEIPluginExpectPlatform {
    /**
     * This is our actual method to {@link CobblemonXEIPluginExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
