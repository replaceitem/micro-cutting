package net.replaceitem.microcutting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Recipe {
    private final String uuid;
    private final String texture;
    private final String itemId;
    private final String text;

    public String getFileName() {
        return text.toLowerCase().replace(' ', '_');
    }

    public Recipe(String uuid, String texture, String itemId, String text) {
        this.uuid = uuid;
        this.texture = texture;
        this.itemId = itemId;
        this.text = text;
    }

    private static final Pattern UUID_PATTERN = Pattern.compile("(?<=SkullOwner:\\{Id:\\[I;)(-?\\d*,?)*");
    private static final Pattern TEXTURE_PATTERN = Pattern.compile("(?<=\\{textures:\\[\\{Value:\").*(?=\")");
    private static final Pattern BLOCK_PATTERN = Pattern.compile("(?<=,buyB:\\{id:\").*(?=\",Count:1b})");
    private static final Pattern TEXT_PATTERN = Pattern.compile("(?<=\\{display:\\{Name:\"\\{\\\\\"text\\\\\":\\\\\"§r§e).*(?=\\\\\"}\"},)"); // beware - evil triple escape

    private static String findFirst(String string, Pattern pattern) {
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            throw new RuntimeException("No match for " + pattern + " in " + string);
        }
        return matcher.group();
    }

    public static Recipe parseFunction(String functionLine) {
        String uuidString = findFirst(functionLine, UUID_PATTERN);
        String textureString = findFirst(functionLine, TEXTURE_PATTERN);
        String blockString = findFirst(functionLine, BLOCK_PATTERN);
        String text = findFirst(functionLine, TEXT_PATTERN);
        return new Recipe(uuidString, textureString, blockString, text);
    }

    public String getCustomName() {
        JsonElement jsonElement = Converter.NAME_TEMPLATE.deepCopy();
        JsonUtil.setPath(jsonElement, new JsonPrimitive("block." + itemId.replace(':', '.')), "translate");
        return Converter.GSON.toJson(jsonElement);
    }

    public JsonArray getUuidArray() {
        JsonArray array = new JsonArray(4);
        Arrays.stream(uuid.split(",")).mapToInt(Integer::parseInt).mapToObj(JsonPrimitive::new).forEach(array::add);
        return array;
    }

    public JsonElement toJson() {
        JsonElement jsonElement = Converter.TEMPLATE.deepCopy();
        JsonUtil.setPath(jsonElement, getUuidArray(), "result", "components", "minecraft:profile", "id");
        JsonUtil.setPath(jsonElement, new JsonPrimitive(texture), "result", "components", "minecraft:profile", "properties", 0, "value");
        JsonUtil.setPath(jsonElement, new JsonPrimitive(itemId), "ingredient", "item");
        JsonUtil.setPath(jsonElement, new JsonPrimitive(getCustomName()), "result", "components", "minecraft:custom_name");
        return jsonElement;
    }
}
