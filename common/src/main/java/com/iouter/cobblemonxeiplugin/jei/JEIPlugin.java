package com.iouter.cobblemonxeiplugin.jei;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.spawning.BestSpawner;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository;
import com.cobblemon.mod.common.pokemon.Species;
import com.iouter.cobblemonxeiplugin.CobblemonXEIPlugin;
import com.iouter.cobblemonxeiplugin.jei.spawn.PokemonSpawnCategory;
import com.iouter.cobblemonxeiplugin.jei.spawn.PokemonSpawnWarpper;
import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static IJeiHelpers iJeiHelpers;

    public static IJeiHelpers getJeiHelpers() {
        return iJeiHelpers;
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(CobblemonXEIPlugin.MOD_ID);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PokemonSpawnWarpper> spawns = new ArrayList<>();
        List<PokemonSpawnDetail> pokemonSpawnDetails = BestSpawner.INSTANCE.getSpawnerManagers().get(0).getSpawners().get(0).getSpawnPool().getDetails().stream().map(s -> {
            if (s instanceof PokemonSpawnDetail pokemonSpawnDetail) {
                return pokemonSpawnDetail;
            }
            return null;
        }).toList();
        for (var spawnDetail : pokemonSpawnDetails) {
            if (spawnDetail != null) {
                Species species = null;
                if (spawnDetail.getPokemon().getSpecies() != null) {
                    species = PokemonSpecies.INSTANCE.getByName(spawnDetail.getPokemon().getSpecies());
                }
                if (species != null) {
                    var baseTexture = PokemonModelRepository.INSTANCE.getTexture(species.getResourceIdentifier(), Set.copyOf(species.getStandardForm().getAspects()), 0);
                    var forms = species.getForms().isEmpty() ? List.of(species.getStandardForm()) : species.getForms();
                    for (var form : forms) {
                        var formTexture = PokemonModelRepository.INSTANCE.getTexture(species.getResourceIdentifier(), Set.copyOf(form.getAspects()), 0);
                        var isSubstitute = formTexture.getPath().contains("substitute");
                        var isBaseForm = form.getName().equals(species.getStandardForm().getName());
                        var hasNewTexture = baseTexture != formTexture;
                        if (isSubstitute || (!isBaseForm && !hasNewTexture)) {
                            continue;
                        }
                        spawns.add(new PokemonSpawnWarpper(spawnDetail, form, species));
                    }
                }
            }
        }
        LogUtils.getLogger().info(String.format("Cobblemon XEI Plugin: Loaded %d Pokemon Spawn Details, Show %d Details", pokemonSpawnDetails.size(), spawns.size()));
        registration.addRecipes(PokemonSpawnCategory.POKEMON_SPAWN_TYPE, spawns);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        JEIPlugin.iJeiHelpers = registration.getJeiHelpers();
        registration.addRecipeCategories(new PokemonSpawnCategory());
    }
}
