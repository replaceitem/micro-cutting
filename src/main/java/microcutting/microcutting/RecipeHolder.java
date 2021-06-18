package microcutting.microcutting;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface RecipeHolder {
    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();
}