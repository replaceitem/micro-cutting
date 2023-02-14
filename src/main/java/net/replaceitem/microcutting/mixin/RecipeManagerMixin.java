package net.replaceitem.microcutting.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import net.replaceitem.microcutting.MicroCutting;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Ljava/util/Map;entrySet()Ljava/util/Set;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void injectRecipes(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci, Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> map2, ImmutableMap.Builder<Identifier, Recipe<?>> builder) {
        Map<Identifier, StonecuttingRecipe> stonecuttingRecipes = MicroCutting.getStonecuttingRecipes();
        stonecuttingRecipes.forEach((identifier, stonecuttingRecipe) -> {
            map2.computeIfAbsent(stonecuttingRecipe.getType(), recipeType -> ImmutableMap.builder()).put(identifier, stonecuttingRecipe);
            builder.put(identifier, stonecuttingRecipe);
        });
    }
}
