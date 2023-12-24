package com.iouter.cobblemonxeiplugin.forge;

import com.iouter.cobblemonxeiplugin.CobblemonXEIPluginExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class CobblemonXEIPluginExpectPlatformImpl {
    /**
     * This is our actual method to {@link CobblemonXEIPluginExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
