package net.replaceitem.microcutting;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Config {
    
    private static final Properties DEFAULT_PROPERTIES = getDefaultProperties();
    
    
    public Config(Properties properties) {
        try {
            this.headCount = Integer.parseInt(properties.getProperty("head-count"));
        } catch (NumberFormatException e) {
            MicroCutting.LOGGER.error("Invalid property for 'head-count', has to be a number");
            this.headCount = 4;
        }
        this.writeName = Boolean.parseBoolean(properties.getProperty("write-name"));
    }
    
    public int headCount;
    public boolean writeName;
    
    public static Config loadConfig() {
        try {
            File file = FabricLoader.getInstance().getConfigDir().resolve("microcutting.properties").toFile();
            Properties properties = new Properties(DEFAULT_PROPERTIES);
            if(!file.exists()) {
                DEFAULT_PROPERTIES.store(new FileOutputStream(file), "MicroCutting configuration");
                return new Config(DEFAULT_PROPERTIES);
            }
            InputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            boolean changed = false;
            for(Map.Entry<Object,Object> entry : DEFAULT_PROPERTIES.entrySet()) {
                if(!properties.containsKey(entry.getKey())) {
                    properties.put(entry.getKey(),entry.getValue());
                    changed = true;
                }
            }
            if(changed) properties.store(new FileOutputStream(file), "MicroCutting configuration");
            return new Config(properties);
        } catch (IOException e) {
            MicroCutting.LOGGER.error("Could not load or create config", e);
            return null;
        }
    }
    
    
    private static Properties getDefaultProperties() {
        Properties defaults = new Properties();
        defaults.setProperty("head-count","4");
        defaults.setProperty("write-name","true");
        return defaults;
    }
}
