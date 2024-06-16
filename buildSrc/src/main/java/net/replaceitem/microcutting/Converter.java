package net.replaceitem.microcutting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Converter {
    
    public static JsonElement TEMPLATE;
    public static JsonElement NAME_TEMPLATE;
    public static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static Gson PRETTY_JSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final String PACK_ID = "microcutting";
    
    public static void createPack(Path packRoot, File addTradeFunctionFile) throws IOException {
        try(InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Converter.class.getClassLoader().getResourceAsStream("template.json")))) {
            TEMPLATE = JsonParser.parseReader(reader);
        }
        try(InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Converter.class.getClassLoader().getResourceAsStream("name_template.json")))) {
            NAME_TEMPLATE = JsonParser.parseReader(reader);
        }
        
        Path recipesPath = packRoot.resolve("data").resolve(PACK_ID).resolve("recipe");
        Files.createDirectories(recipesPath);
        try(BufferedReader reader = new BufferedReader(new FileReader(addTradeFunctionFile, StandardCharsets.UTF_8))) {
            reader.lines()
                    .filter(s -> !s.isBlank())
                    .filter(s -> s.charAt(0) != '#')
                    .filter(s -> s.contains("{id:\"minecraft:player_head\",Count:8"))
                    .map(Recipe::parseFunction)
                    .forEach(entry -> {
                        File file = recipesPath.resolve(entry.getFileName() + ".json").toFile();
                        try {
                            try(JsonWriter writer = new JsonWriter(new FileWriter(file))) {
                                writer.setIndent("    ");
                                PRETTY_JSON.toJson(entry.toJson(), writer);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
