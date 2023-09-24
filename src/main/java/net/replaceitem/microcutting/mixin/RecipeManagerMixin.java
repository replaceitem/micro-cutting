package net.replaceitem.microcutting.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.replaceitem.microcutting.MicroCutting;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.List;
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
    public void injectRecipes(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci, Map<RecipeType<?>, ImmutableMap.Builder<Identifier,RecipeEntry<StonecuttingRecipe>>> map2, ImmutableMap.Builder<Identifier, RecipeEntry<?>> builder) {
        List<RecipeEntry<StonecuttingRecipe>> stonecuttingRecipes = MicroCutting.loadBlocksFromJson();
        if(stonecuttingRecipes == null) return;
        for (RecipeEntry<StonecuttingRecipe> recipeEntry : stonecuttingRecipes) {
            Identifier identifier = recipeEntry.id();
            map2.computeIfAbsent(recipeEntry.value().getType(), recipeType -> ImmutableMap.builder()).put(identifier, recipeEntry);
            builder.put(identifier, recipeEntry);
        }
    }
}
