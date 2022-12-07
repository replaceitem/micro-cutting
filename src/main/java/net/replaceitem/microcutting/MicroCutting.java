package net.replaceitem.microcutting;

import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MicroCutting implements ModInitializer, RecipeHolder {

    private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
    private Map<Identifier, Recipe<?>> stonecuttingRecipes;
    public static final Logger LOGGER = LogManager.getLogger("net/replaceitem/microcutting");
    public static Config config;

    @Override
    public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes() {
        return recipes;
    }


    private void createMicroblockRecipe(Item item, String texture, UUID uuid, int index) {
        NbtCompound nbt = new NbtCompound();
        NbtCompound textureValue = new NbtCompound();
        textureValue.putString("Value",texture);
        NbtList textureList = new NbtList();
        textureList.add(textureValue);
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        properties.put("textures",textureList);
        skullOwner.put("Properties",properties);
        skullOwner.putUuid("Id",uuid);
        nbt.put(SkullItem.SKULL_OWNER_KEY,skullOwner);
        ItemStack output = new ItemStack(Items.PLAYER_HEAD, config.headCount);
        output.setNbt(nbt);
        Ingredient input = Ingredient.ofStacks(item.getDefaultStack());
        StonecuttingRecipe recipe = new StonecuttingRecipe(new Identifier("microcutting",item + "_microblock_" + index),"microblocks", input, output);
        stonecuttingRecipes.put(recipe.getId(), recipe);
    }

    @Override
    public void onInitialize() {
        config = Config.loadConfig();
        recipes = new HashMap<>();
        stonecuttingRecipes = new HashMap<>();
        loadBlocksFromJson();
        recipes.put(RecipeType.STONECUTTING, stonecuttingRecipes);
        InjectableRecipes.register(this);
    }
    
    private void loadBlocksFromJson() {
        Gson gson = new Gson();
        InputStream stream = MinecraftServer.class.getClassLoader().getResourceAsStream("assets/microcutting/heads.json");
        if(stream == null) {
            LOGGER.error("Could not load heads.json");
            return;
        }
        InputStreamReader reader = new InputStreamReader(stream);
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, String>>> map = (Map<String, List<Map<String, String>>>) gson.fromJson(reader, Map.class);
        map.forEach((itemId, microblocks) -> {
            Item item = Registries.ITEM.get(Identifier.tryParse(itemId));
            for (int i = 0, microblocksSize = microblocks.size(); i < microblocksSize; i++) {
                Map<String, String> microblock = microblocks.get(i);
                String uuidString = microblock.get("uuid");
                String texture = microblock.get("texture");
                UUID uuid = UUID.fromString(uuidString);
                createMicroblockRecipe(item, texture, uuid, i);
            }
        });
    }
}
