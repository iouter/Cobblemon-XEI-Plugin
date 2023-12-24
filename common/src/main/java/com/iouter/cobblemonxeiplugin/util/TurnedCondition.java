package com.iouter.cobblemonxeiplugin.util;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TurnedCondition {
    private static int nowCondition = 0;
    private static long lastTurnedTime = System.currentTimeMillis();

    public static int getTurnedPage(int conditionSize) {
        if ((System.currentTimeMillis() - lastTurnedTime > 300) && Screen.hasShiftDown()) {
            lastTurnedTime = System.currentTimeMillis();
            if (nowCondition + 1 >= conditionSize) {
                nowCondition = 0;
            } else {
                nowCondition++;
            }
        }
        return nowCondition;
    }
}
