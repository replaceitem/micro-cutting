package microcutting.microcutting.mixin;

import com.google.common.collect.ImmutableMap;
import microcutting.microcutting.InjectableRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
    Logger logger = LogManager.getLogger("injectable-recipes");

    @Inject(method = "apply", at = @At(value = "TAIL"))
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
                logger.warn("Recipe with ID {} failed to merge. Are you registering the same ID twice?", id.toString());
                failedMerges.getAndIncrement();
            }
        }));

        logger.info("Successfully merged {} recipes into the game", successfulMerges.get());
        logger.info("Failed to merge {} recipes", failedMerges.get());

        return existingRecipes;
    }

    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> copyRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes){
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> map = new HashMap<>();
        recipes.forEach((type, map1) -> {
            if(map1 instanceof ImmutableMap){
                Map<Identifier, Recipe<?>> identifierRecipeMap = new HashMap<>();
                map1.forEach(identifierRecipeMap::put);
                map.put(type, identifierRecipeMap);
            } else {
                map.put(type, map1);
            }
        });
        return map;
    }
}
