package net.replaceitem.microcutting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;

public class JsonUtil {
    public static void setPath(JsonElement element, JsonElement value, Object... path) {
        Object thisPath = path[0];
        if(element instanceof JsonObject object) {
            if(!(thisPath instanceof String stringPath)) throw new RuntimeException("Expected string as path for object");
            if(path.length == 1) {
                object.add(stringPath, value);
            } else {
                setPath(object.get(stringPath), value, Arrays.copyOfRange(path, 1, path.length));
            }
        } else if(element instanceof JsonArray array) {
            if(!(thisPath instanceof Integer indexPath)) throw new RuntimeException("Expected int as path for object");
            if(path.length == 1) {
                array.set(indexPath, value);
            } else {
                setPath(array.get(indexPath), value, Arrays.copyOfRange(path, 1, path.length));
            }
        } else {
            throw new RuntimeException("Invalid path: " + Arrays.toString(path));
        }
    }
}
