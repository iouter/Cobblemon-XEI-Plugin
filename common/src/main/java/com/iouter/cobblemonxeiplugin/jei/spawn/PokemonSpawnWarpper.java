package com.iouter.cobblemonxeiplugin.jei.spawn;

import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.PossibleHeldItem;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

public class PokemonSpawnWarpper implements IRecipeCategoryExtension, IRecipeSlotTooltipCallback {
    protected final PokemonSpawnDetail spawnDetail;
    protected final FormData form;
    protected final Species species;


    public PokemonSpawnWarpper(PokemonSpawnDetail spawnDetail, FormData form, Species species) {
        this.spawnDetail = spawnDetail;
        this.form = form;
        this.species = species;
    }

    public static void drawItem(GuiGraphics graphics, Item item, double mouseX, double mouseY) {
    }

    public DropTable getDrops() {
        return species.getDrops();
    }

    public List<PossibleHeldItem> getHeldItems() {
        return spawnDetail.getHeldItems();
    }

    @Override
    public void onTooltip(IRecipeSlotView iRecipeSlotView, List<Component> list) {
        String slotName = iRecipeSlotView.getSlotName().isPresent() ? iRecipeSlotView.getSlotName().get() : "";
        if ("none".equals(slotName)) {
            list.add(Component.translatable("jei.pokemon_spawn.none"));
        } else if (slotName.startsWith("drops_")) {
            String s = slotName.replace("drops_", "");
            int slotNumber = Integer.parseInt(s);
            var chance = getDrops().getEntries().get(slotNumber).getPercentage();
            list.add(Component.translatable("jei.pokemon_spawn.chance", chance));
        }
//        List<SpawningCondition<?>> conditionList = spawnDetail.getConditions();
//        List<SpawningCondition<?>> antiConditionList = spawnDetail.getAnticonditions();
    }
}