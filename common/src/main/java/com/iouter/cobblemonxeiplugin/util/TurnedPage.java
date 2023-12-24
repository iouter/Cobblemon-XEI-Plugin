package com.iouter.cobblemonxeiplugin.util;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TurnedPage {
    private static int nowPage = 0;
    private static long lastTurnedTime = System.currentTimeMillis();

    public static List<Component> getTurnedPage(List<Component> list, int limit) {
        if (list.size() < limit + 2) {
            return list;
        }
        List<Component> turnedList = new ArrayList<>();
        turnedList.add(Component.translatable("jei.pokemon_spawn.condition.turnpage.explain"));
        if (Math.min(limit * (nowPage + 1) - 1, list.size() - 1) > limit * nowPage) {
            turnedList.addAll(list.subList(limit * nowPage, Math.min(limit * (nowPage + 1) - 1, list.size() - 1)));
        }
        turnedList.add(Component.translatable("jei.pokemon_spawn.condition.turnpage.page_number", nowPage + 1, (int) Math.ceil((float) list.size() / limit)));
        if ((System.currentTimeMillis() - lastTurnedTime > 300) && Screen.hasShiftDown()) {
            lastTurnedTime = System.currentTimeMillis();
            nowPage++;
            if (limit * nowPage > list.size() - 1) {
                nowPage = 0;
            }
        }
        return turnedList;
    }
}
