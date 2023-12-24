package com.iouter.cobblemonxeiplugin.jei.spawn;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.spawning.TimeRange;
import com.cobblemon.mod.common.api.spawning.condition.AreaSpawningCondition;
import com.cobblemon.mod.common.api.spawning.condition.GroundedSpawningCondition;
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition;
import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.cobblemon.mod.common.registry.BiomeIdentifierCondition;
import com.cobblemon.mod.common.registry.BiomeTagCondition;
import com.cobblemon.mod.common.registry.BlockIdentifierCondition;
import com.cobblemon.mod.common.registry.BlockTagCondition;
import com.cobblemon.mod.common.util.math.QuaternionUtilsKt;
import com.iouter.cobblemonxeiplugin.CobblemonXEIPlugin;
import com.iouter.cobblemonxeiplugin.jei.JEIPlugin;
import com.iouter.cobblemonxeiplugin.util.TurnedCondition;
import com.iouter.cobblemonxeiplugin.util.TurnedPage;
import kotlin.ranges.IntRange;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PokemonSpawnCategory implements IRecipeCategory<PokemonSpawnWarpper> {

    public static final int HEIGHT = 145;
    private static final int WIDTH = 180;
    private static final String NAME = "pokemon_spawn";
    public static final ResourceLocation POKEMON_SPAWN = new ResourceLocation(CobblemonXEIPlugin.MOD_ID, PokemonSpawnCategory.NAME);
    public static final RecipeType<PokemonSpawnWarpper> POKEMON_SPAWN_TYPE = new RecipeType<>(POKEMON_SPAWN, PokemonSpawnWarpper.class);
    private static final IDrawable ICON = JEIPlugin.getJeiHelpers().getGuiHelper().createDrawableItemStack(CobblemonItems.POKE_BALL.getDefaultInstance());
    private static final IDrawable GUI = JEIPlugin.getJeiHelpers().getGuiHelper().createDrawable(new ResourceLocation(CobblemonXEIPlugin.MOD_ID, "textures/gui/pokemon_spawn.png"), 0, 0, WIDTH, HEIGHT);
    private static final int AMOUNT_ITEMS_IN_A_ROW = 7;
    private final static ResourceLocation SUN = new ResourceLocation("minecraft", "textures/environment/sun.png");
    private final static ResourceLocation MOON_PHASE = new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
    private final static ResourceLocation RAIN = new ResourceLocation("minecraft", "textures/environment/rain.png");
    private final static ResourceLocation PORTAL = new ResourceLocation("minecraft", "textures/block/nether_portal.png");
    private final static int INTERVAL = 18;
    private PokemonFloatingState state;
    private long last;
    private int y;

    public static Component getAnswerByBoolean(Boolean b) {
        if (b != null) {
            if (b) {
                return Component.translatable("jei.pokemon_spawn.condition.true");
            } else {
                return Component.translatable("jei.pokemon_spawn.condition.false");
            }
        } else {
            return Component.translatable("jei.pokemon_spawn.condition.any");
        }
    }

    public static Component getCompared(Number min, Number max) {
        if (min == null && max == null) {
            return Component.translatable("jei.pokemon_spawn.condition.any");
        }
        if (min != null && max == null) {
            return Component.translatable("jei.pokemon_spawn.condition.compare.more", min);
        }
        if (min == null) {
            return Component.translatable("jei.pokemon_spawn.condition.compare.less", max);
        }
        return Component.translatable("jei.pokemon_spawn.condition.compare.between", min, max);
    }

    public static int[] getTime(int time) {
        return new int[]{time / 1000, ((time % 1000) / 100) * 6};
    }

    private static ResourceLocation getBiomeId(Registry<Biome> biomeReg, Holder<Biome> h) {
        try {
            if (h.kind() == Holder.Kind.DIRECT) {
                return biomeReg.getKey(h.value());
            } else {
                return ((Holder.Reference<Biome>) h).key().location();
            }
        } catch (Exception e) {
            return new ResourceLocation("null");
        }
    }

    public static boolean isArea(double mouseX, double mouseY, int startX, int startY, int serialX, int serialY) {
        return mouseX >= startX + INTERVAL * serialX && mouseX < startX + INTERVAL * (serialX + 1) && (mouseY >= startY + INTERVAL * serialY && mouseY < startY + INTERVAL * (serialY + 1));
    }

    public static String manuallyAddZero(int n) {
        return String.format("%02d", n);
    }

    @Override
    public @NotNull RecipeType<PokemonSpawnWarpper> getRecipeType() {
        return POKEMON_SPAWN_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei." + NAME + ".title");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return GUI;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return ICON;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, PokemonSpawnWarpper pokemonSpawnWarpper, IFocusGroup iFocusGroup) {
        // drops
        int xDropsOffset = 0;
        var drops = pokemonSpawnWarpper.getDrops();
        if (drops != null) {
            var entitys = drops.getEntries();
            var dropCount = Math.min(entitys.size(), AMOUNT_ITEMS_IN_A_ROW);
            for (var slotNumber = 0; slotNumber < AMOUNT_ITEMS_IN_A_ROW; slotNumber++) {
                if (slotNumber < dropCount) {
                    var entity = entitys.get(slotNumber);
                    try {
                        var field = entity.getClass().getDeclaredField("item");
                        field.setAccessible(true);
                        ResourceLocation item = (ResourceLocation) field.get(entity);
                        ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(item));
                        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 66 + xDropsOffset, 21)
                                .setSlotName("drops_" + slotNumber)
                                .addTooltipCallback(pokemonSpawnWarpper)
                                .addItemStack(itemStack);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                xDropsOffset += INTERVAL;
            }
        } else {
            iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 66, 21)
                    .setSlotName("none")
                    .addTooltipCallback(pokemonSpawnWarpper)
                    .addItemStack(new ItemStack(Items.BARRIER));
        }
//        //Held Items -- looks like it does not exist.
//        int xHeldItemsOffset = 0;
//        var heldItems = pokemonSpawnWarpper.getHeldItems();
//        if (heldItems != null) {
//            var heldItemCount = Math.min(heldItems.size(), AMOUNT_ITEMS_IN_A_ROW);
//            for (int slotNumber = 0; slotNumber < AMOUNT_ITEMS_IN_A_ROW; slotNumber++) {
//                if (slotNumber < heldItemCount) {
//                    var heldItem = heldItems.get(slotNumber);
//                    ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(heldItem.getItem())));
//                    iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 66 + xHeldItemsOffset, 45)
//                            .setSlotName("held_items_" + slotNumber)
//                            .addTooltipCallback(pokemonSpawnWarpper)
//                            .addItemStack(itemStack);
//                }
//                xHeldItemsOffset += INTERVAL;
//            }
//        } else {
//            iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 66, 45)
//                    .setSlotName("none")
//                    .addTooltipCallback(pokemonSpawnWarpper)
//                    .addItemStack(new ItemStack(Items.BARRIER));
//        }
    }

    @Override
    public void draw(PokemonSpawnWarpper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        recipe.drawInfo(getBackground().getWidth(), getBackground().getHeight(), graphics, mouseX, mouseY);
        // render the pokemon and its name, copied from cobblemon integrations
        var species = recipe.species;
        var form = recipe.form;
        var spawnDetail = recipe.spawnDetail;

        if (state == null) {
            state = new PokemonFloatingState();
        }
        var elapsed = System.currentTimeMillis() - last;
        last = System.currentTimeMillis();
        var partialTicks = Mth.clamp(elapsed / 100F, 0F, 1F);

        var pose = graphics.pose();

        pose.pushPose();
        var component = species.getTranslatedName();
        if (species.getStandardForm() != form) {
            component.append(Component.literal(String.format(" (%s)", form.getName())));
        }
        graphics.drawString(Minecraft.getInstance().font, component, 2, 1, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
        pose.popPose();

        var pokemon = new RenderablePokemon(species, new HashSet<>(form.getAspects()));

        var m1 = pose.last().pose();
        var l1 = m1.m30();
        var t1 = m1.m31();

        pose.pushPose();

        graphics.enableScissor((int) l1 + 2, (int) t1 + 13, (int) l1 + 61, (int) t1 + 92);

        var rotationY = -30F;
        pose.translate(31, 13, 0);
        pose.scale(1F, 1F, 1F);
        pose.pushPose();
        PokemonGuiUtilsKt.drawProfilePokemon(
                pokemon,
                pose,
                QuaternionUtilsKt.fromEulerXYZDegrees(new Quaternionf(), new Vector3f(13F, rotationY, 0F)),
                state,
                partialTicks,
                40F
        );
        pose.popPose();
        graphics.disableScissor();

        pose.popPose();
        // render other parts
        // drops
        y = 13;
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.pokemon_spawn.drops"), 66, y, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
        y += 24;
        // held items --looks like it does not exist.
//        graphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.pokemon_spawn.held_items"), 66, y, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
//        y += 24;
        // level range
        var theClass = spawnDetail.getClass();
        try {
            var field = theClass.getDeclaredField("levelRange");
            field.setAccessible(true);
            IntRange intRange = (IntRange) field.get(spawnDetail);
            graphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.pokemon_spawn.level_range", intRange.getStart(), intRange.getEndInclusive()), 66, y, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
            y += 10;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.pokemon_spawn.weight", spawnDetail.getWeight()), 66, y, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
        y += 10;
        List<SpawningCondition<?>> conditionList = spawnDetail.getConditions();
        List<SpawningCondition<?>> antiConditionList = spawnDetail.getAnticonditions();
        drawSpawnCondition(conditionList, "jei.pokemon_spawn.whitelist", graphics);
        drawSpawnCondition(antiConditionList, "jei.pokemon_spawn.blacklist", graphics);
    }

    public void drawSpawnCondition(List<SpawningCondition<?>> spawningConditions, String conditionName, GuiGraphics graphics) {
        if (!spawningConditions.isEmpty()) {
            int conditionNumber = 0;
            graphics.drawString(Minecraft.getInstance().font, Component.translatable(conditionName, ""), 66, y, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
            y += 10;
            // base blocks
            graphics.renderItem(new ItemStack(Items.GRASS_BLOCK), 66 + INTERVAL * 0, y);
            // nearby blocks
            graphics.renderItem(new ItemStack(Items.MYCELIUM), 66 + INTERVAL * 1, y);
            // dimensions
            graphics.blit(PORTAL, 66 + INTERVAL * 2, y, 0, 0, 0, 16, 16, 16, 16);
            // biomes
            graphics.renderItem(new ItemStack(Items.OAK_SAPLING), 66 + INTERVAL * 3, y);
            // moon_phase
            graphics.blit(MOON_PHASE, 66 + INTERVAL * 4, y, 0, 0, 8, 16, 16, 64, 64);
            // can_see_sky
            graphics.blit(SUN, 66 + INTERVAL * 5, y, 0, 8, 8, 16, 16, 32, 32);
            y += INTERVAL;
            // coordinates
            graphics.renderItem(new ItemStack(Items.COMPASS), 66 + INTERVAL * 0, y);
            // light
            graphics.renderItem(new ItemStack(Items.GLOWSTONE), 66 + INTERVAL * 1, y);
            // raining
            graphics.blit(RAIN, 66 + INTERVAL * 2, y, 0, 32, 0, 16, 16, 64, 64);
            // thundering
            graphics.renderItem(new ItemStack(Items.LIGHTNING_ROD), 66 + INTERVAL * 3, y);
            // time_range
            graphics.renderItem(new ItemStack(Items.CLOCK), 66 + INTERVAL * 4, y);
            // structures
            graphics.renderItem(new ItemStack(Items.FILLED_MAP), 66 + INTERVAL * 5, y);
            y += INTERVAL;
        }
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(PokemonSpawnWarpper recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> list = new ArrayList<>();
        var conditions = recipe.spawnDetail.getConditions();
        if (!conditions.isEmpty()) {
            list.addAll(getSpawnConditionsTooltip(conditions, mouseX, mouseY, 66, 67, "jei.pokemon_spawn.whitelist"));
        }
        var anticonditions = recipe.spawnDetail.getAnticonditions();
        if (!anticonditions.isEmpty()) {
            list.addAll(getSpawnConditionsTooltip(anticonditions, mouseX, mouseY, 66, 113, "jei.pokemon_spawn.blacklist"));
        }
        return list;
    }

    public List<Component> getSpawnConditionsTooltip(List<SpawningCondition<?>> spawningConditions, double mouseX, double mouseY, int startX, int startY, String str) {
        List<Component> list = new ArrayList<>();
        int serial;
        String temp;
        boolean area = (mouseX >= startX) && (mouseX < startX + INTERVAL * 6) && ((mouseY >= startY) && (mouseY < startY + INTERVAL * 2));
        if (spawningConditions.size() > 1) {
            serial = TurnedCondition.getTurnedPage(spawningConditions.size());
            temp = " " + serial;
            if (area) {
                list.add(Component.translatable("jei.pokemon_spawn.condition.turnpage.explain"));
            }
        } else {
            serial = 0;
            temp = "";
        }
        if (area) {
            list.add(Component.translatable(str, temp));
        }
        if (serial < spawningConditions.size()) {
            list.addAll(getSpawnConditionTooltip(spawningConditions.get(serial), mouseX, mouseY, startX, startY));
        }
        return list;
    }

    public List<Component> getSpawnConditionTooltip(SpawningCondition<?> spawningCondition, double mouseX, double mouseY, int startX, int startY) {
        List<Component> list = new ArrayList<>();
        if (isArea(mouseX, mouseY, startX, startY, 0, 0)) {
            // base blocks
            list.add(Component.translatable("jei.pokemon_spawn.condition.blocks.needed_base_blocks"));
            if (spawningCondition instanceof GroundedSpawningCondition groundedSpawningCondition) {
                var blockList = groundedSpawningCondition.getNeededBaseBlocks();
                if (blockList != null) {
                    List<Component> blockComponent = new ArrayList<>();
                    for (var b : blockList) {
                        if (b instanceof BlockIdentifierCondition identifierCondition) {
                            blockComponent.add(Component.translatable("block." + identifierCondition.getIdentifier().toString().replace(":", ".")));
                        } else if (b instanceof BlockTagCondition tagCondition) {
                            blockComponent.add(Component.translatable("jei.pokemon_spawn.condition.block.tag", Component.translatable(tagCondition.getTag().location().toString())));
                        }
                    }
                    list.addAll(TurnedPage.getTurnedPage(blockComponent, 16));
                } else {
                    list.add(Component.translatable("jei.pokemon_spawn.none"));
                }
            } else {
                list.add(Component.translatable("jei.pokemon_spawn.none"));
            }
        } else if (isArea(mouseX, mouseY, startX, startY, 1, 0)) {
            // base blocks
            list.add(Component.translatable("jei.pokemon_spawn.condition.blocks.needed_nearby_blocks"));
            if (spawningCondition instanceof AreaSpawningCondition areaSpawningCondition) {
                var blockList = areaSpawningCondition.getNeededNearbyBlocks();
                if (blockList != null) {
                    List<Component> blockComponent = new ArrayList<>();
                    for (var b : blockList) {
                        if (b instanceof BlockIdentifierCondition identifierCondition) {
                            blockComponent.add(Component.translatable("block." + identifierCondition.getIdentifier().toString().replace(":", ".")));
                        } else if (b instanceof BlockTagCondition tagCondition) {
                            blockComponent.add(Component.translatable("jei.pokemon_spawn.condition.block.tag", Component.translatable(tagCondition.getTag().location().toString())));
                        }
                    }
                    list.addAll(TurnedPage.getTurnedPage(blockComponent, 16));
                } else {
                    list.add(Component.translatable("jei.pokemon_spawn.none"));
                }
            } else {
                list.add(Component.translatable("jei.pokemon_spawn.none"));
            }
        } else if (isArea(mouseX, mouseY, startX, startY, 2, 0)) {
            // dimensions
            list.add(Component.translatable("jei.pokemon_spawn.condition.dimensions"));
            var dimesions = spawningCondition.getDimensions();
            if (dimesions != null) {
                List<Component> dilist = new ArrayList<>();
                for (var dimesion : dimesions) {
                    dilist.add(Component.translatable(dimesion.toString()));
                }
                list.addAll(TurnedPage.getTurnedPage(dilist, 16));
            } else {
                list.add(Component.translatable("jei.pokemon_spawn.condition.any"));
            }
        } else if (isArea(mouseX, mouseY, startX, startY, 3, 0)) {
            // biomes
            list.add(Component.translatable("jei.pokemon_spawn.condition.biomes"));
            var spawnBiomes = spawningCondition.getBiomes();
            if (spawnBiomes != null) {
                List<String> filteredBiome = new ArrayList<>();
                Registry<Biome> allBiomes = Objects.requireNonNull(Minecraft.getInstance().getConnection()).registryAccess().registryOrThrow(Registries.BIOME);
                for (RegistryLikeCondition<Biome> s : spawnBiomes) {
                    if (s instanceof BiomeTagCondition biomeTagCondition) {
                        TagKey<Biome> biomeTagKey = biomeTagCondition.getTag();
                        var showedBiomes = allBiomes.getTag(biomeTagKey).map(a -> a.stream().map(b -> getBiomeId(allBiomes, b)).map(c -> "biome." + c.getNamespace() + "." + c.getPath()).collect(Collectors.toList()));
                        showedBiomes.map(filteredBiome::addAll);
                    } else if (s instanceof BiomeIdentifierCondition biomeIdentifierCondition) {
                        filteredBiome.add("biome." + ((BiomeIdentifierCondition) s).getIdentifier().toString().replace(":", "."));
                    }
                }
                list.addAll(TurnedPage.getTurnedPage(filteredBiome.stream().map(Component::translatable).collect(Collectors.toList()), 16));
            } else {
                list.add(Component.translatable("jei.pokemon_spawn.condition.any"));
            }
        } else if (isArea(mouseX, mouseY, startX, startY, 4, 0)) {
            // moon_phase
            list.add(Component.translatable("jei.pokemon_spawn.condition.moon_phase"));
            var moon = spawningCondition.getMoonPhase();
            if (moon != null) {
                for (IntRange intRange : moon.getRanges()) {
                    for (int i = intRange.getStart(); i == intRange.getEndInclusive(); ) {
                        list.add(Component.translatable("jei.pokemon_spawn.condition.moon." + i));
                        if (i >= 7) {
                            i = 0;
                        } else {
                            i++;
                        }
                    }
                }
            } else {
                list.add(Component.translatable("jei.pokemon_spawn.condition.any"));
            }
        } else if (isArea(mouseX, mouseY, startX, startY, 5, 0)) {
            // can_see_sky
            list.add(Component.translatable("jei.pokemon_spawn.condition.can_see_sky"));
            list.add(getAnswerByBoolean(spawningCondition.getCanSeeSky()));
        } else if (isArea(mouseX, mouseY, startX, startY, 0, 1)) {
            // coordinates
            list.add(Component.translatable("jei.pokemon_spawn.condition.coordinates"));
            list.add(Component.translatable("jei.pokemon_spawn.condition.coordinates.x", getCompared(spawningCondition.getMinX(), spawningCondition.getMaxX())));
            list.add(Component.translatable("jei.pokemon_spawn.condition.coordinates.y", getCompared(spawningCondition.getMinY(), spawningCondition.getMaxY())));
            list.add(Component.translatable("jei.pokemon_spawn.condition.coordinates.z", getCompared(spawningCondition.getMinY(), spawningCondition.getMaxZ())));
        } else if (isArea(mouseX, mouseY, startX, startY, 1, 1)) {
            // light
            list.add(Component.translatable("jei.pokemon_spawn.condition.light"));
            list.add(getCompared(spawningCondition.getMinLight(), spawningCondition.getMaxLight()));
        } else if (isArea(mouseX, mouseY, startX, startY, 2, 1)) {
            // raining
            list.add(Component.translatable("jei.pokemon_spawn.condition.raining"));
            list.add(getAnswerByBoolean(spawningCondition.isRaining()));
        } else if (isArea(mouseX, mouseY, startX, startY, 3, 1)) {
            // thundering
            list.add(Component.translatable("jei.pokemon_spawn.condition.thundering"));
            list.add(getAnswerByBoolean(spawningCondition.isThundering()));
        } else if (isArea(mouseX, mouseY, startX, startY, 4, 1)) {
            // time_range
            list.add(Component.translatable("jei.pokemon_spawn.condition.time_range"));
            TimeRange timeRange = spawningCondition.getTimeRange();
            if (timeRange != null) {
                for (IntRange range : timeRange.getRanges()) {
                    int[] start = getTime(range.getStart());
                    int[] end = getTime(range.getEndInclusive());
                    list.add(Component.translatable("jei.pokemon_spawn.condition.time_range.show", manuallyAddZero(start[0]), manuallyAddZero(start[1]), manuallyAddZero(end[0]), manuallyAddZero(end[1])));
                }
            } else {
                list.add(Component.translatable("jei.pokemon_spawn.condition.any"));
            }
        } else if (isArea(mouseX, mouseY, startX, startY, 5, 1)) {
            // structures
            list.add(Component.translatable("jei.pokemon_spawn.condition.structures"));
            var structures = spawningCondition.getStructures();
            if (structures != null) {
                List<Component> struct = new ArrayList<>();
                for (var st : structures) {
                    st.ifLeft(s -> struct.add(Component.translatable(st.left().get().toString()))).ifRight(s -> struct.add(Component.translatable("jei.pokemon_spawn.condition.structures.tag", st.right().get().location())));
                }
                list.addAll(TurnedPage.getTurnedPage(struct, 16));
            } else {
                list.add(Component.translatable("jei.pokemon_spawn.condition.any"));
            }
        }
        return list;
    }
}
