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
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MicroCutting implements ModInitializer {
    private static Map<Identifier, StonecuttingRecipe> stonecuttingRecipes;
    public static final Logger LOGGER = LogManager.getLogger("MicroCutting");
    public static Config config;

    public static Map<Identifier, StonecuttingRecipe> getStonecuttingRecipes() {
        return stonecuttingRecipes;
    }

    @Override
    public void onInitialize() {
        config = Config.loadConfig();
        stonecuttingRecipes = new HashMap<>();
        loadBlocksFromJson();
    }

    private void loadBlocksFromJson() {
        InputStream stream = MinecraftServer.class.getClassLoader().getResourceAsStream("assets/microcutting/heads.json");
        if(stream == null) {
            LOGGER.error("Could not load heads.json");
            return;
        }
        InputStreamReader reader = new InputStreamReader(stream);
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, String>>> map = (Map<String, List<Map<String, String>>>) new Gson().fromJson(reader, Map.class);
        map.forEach((itemId, microblocks) -> {
            Optional<Item> item = Registries.ITEM.getOrEmpty(Identifier.tryParse(itemId));
            if(item.isEmpty()) return;
            int microblockCount = microblocks.size();
            for (int i = 0; i < microblockCount; i++) {
                Map<String, String> microblock = microblocks.get(i);
                String uuidString = microblock.get("uuid");
                String texture = microblock.get("texture");
                UUID uuid = UUID.fromString(uuidString);
                createMicroblockRecipe(item.get(), texture, uuid, i);
            }
        });
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
        if(config.writeName) output.setCustomName(item.getName().copy().append(" Microblock").styled(style -> style.withItalic(false)));
        Ingredient input = Ingredient.ofStacks(item.getDefaultStack());
        String identifierPath = item + "_microblock" + ((index != 0) ? ("_" + index) : (""));
        StonecuttingRecipe recipe = new StonecuttingRecipe(new Identifier("microcutting",identifierPath),"microblocks", input, output);
        stonecuttingRecipes.put(recipe.getId(), recipe);
    }
}
