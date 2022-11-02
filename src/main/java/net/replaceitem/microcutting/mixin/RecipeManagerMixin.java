package net.replaceitem.microcutting.mixin;

import com.google.common.collect.ImmutableMap;
import net.replaceitem.microcutting.InjectableRecipes;
import net.replaceitem.microcutting.MicroCutting;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/*
        Source: https://github.com/s0vi/injectable-recipes/blob/master/src/main/java/me/orangemonkey68/injectablerecipes/mixins/RecipeManagerMixin.java
 */


@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "TAIL"))
    public void injectRecipes(CallbackInfo ci){
        //This probably doesn't NEED to be Immutable, but it's what Mojang uses, so better safe than sorry.
        recipes = ImmutableMap.copyOf(mergeRecipes(InjectableRecipes.getAllRecipes(), copyRecipes(recipes)));
    }

    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> mergeRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> newRecipes, Map<RecipeType<?>, Map<Identifier, Recipe<?>>> existingRecipes){
        AtomicInteger successfulMerges = new AtomicInteger();
        AtomicInteger failedMerges = new AtomicInteger();

        newRecipes.forEach((type, map) -> map.forEach((id, recipe) -> {
            existingRecipes.computeIfAbsent(type, f -> new HashMap<>());

            if(existingRecipes.get(type).get(id) == null){
                existingRecipes.get(type).put(id, recipe);
                successfulMerges.getAndIncrement();
            }else {
                MicroCutting.LOGGER.warn("Recipe with ID {} failed to merge. Are you registering the same ID twice?", id.toString());
                failedMerges.getAndIncrement();
            }
        }));

        MicroCutting.LOGGER.info("Successfully merged {} recipes into the game", successfulMerges.get());
        MicroCutting.LOGGER.info("Failed to merge {} recipes", failedMerges.get());

        return existingRecipes;
    }

    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> copyRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes){
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> map = new HashMap<>();
        recipes.forEach((type, map1) -> {
            if(map1 instanceof ImmutableMap){
                Map<Identifier, Recipe<?>> identifierRecipeMap = new HashMap<>(map1);
                map.put(type, identifierRecipeMap);
            } else {
                map.put(type, map1);
            }
        });
        return map;
    }
}
