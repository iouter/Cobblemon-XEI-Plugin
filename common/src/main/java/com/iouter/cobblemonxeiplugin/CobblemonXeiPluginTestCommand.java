package com.iouter.cobblemonxeiplugin;

import com.cobblemon.mod.common.api.spawning.BestSpawner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CobblemonXeiPluginTestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("cobblemonxeiplugintest").executes(CobblemonXeiPluginTestCommand::run));
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        var map = BestSpawner.INSTANCE;
        return 1;
    }
}
