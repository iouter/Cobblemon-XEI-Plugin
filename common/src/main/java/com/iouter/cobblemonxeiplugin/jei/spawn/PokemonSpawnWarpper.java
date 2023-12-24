package com.iouter.cobblemonxeiplugin.jei.spawn;

import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.PossibleHeldItem;
import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.math.QuaternionUtilsKt;
import kotlin.ranges.IntRange;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class PokemonSpawnWarpper implements IRecipeCategoryExtension, IRecipeSlotTooltipCallback {
    protected final PokemonSpawnDetail spawnDetail;
    protected final FormData form;
    protected final Species species;


    public PokemonSpawnWarpper(PokemonSpawnDetail spawnDetail, FormData form, Species species) {
        this.spawnDetail = spawnDetail;
        this.form = form;
        this.species = species;
    }

    public DropTable getDrops() {
        return species.getDrops();
    }

    public List<PossibleHeldItem> getHeldItems() {
        return spawnDetail.getHeldItems();
    }

    @Override
    public void onTooltip(IRecipeSlotView iRecipeSlotView, List<Component> list) {
        String slotName = iRecipeSlotView.getSlotName().isPresent() ? iRecipeSlotView.getSlotName().get(): "";
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


    public static void drawItem(GuiGraphics graphics, Item item, double mouseX, double mouseY) {
    }
}