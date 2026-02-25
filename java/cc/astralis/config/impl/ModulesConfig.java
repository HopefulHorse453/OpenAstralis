package cc.astralis.config.impl;

import cc.astralis.config.Config;
import cc.astralis.util.client.ConfigUtil;
import cc.astralis.Astralis;
import cc.astralis.util.render.ChatUtil;
import cc.astralis.util.io.FileUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ModulesConfig extends Config {
    private final String configName;

    public ModulesConfig(String configName) {
        this.configName = configName;
    }

    @Override
    public void writeConfig() {
        FileUtil.writeJsonToFile(
                ConfigUtil.getCurrentConfig(),
                new File(moduleDirectory, configName + ".astralis").getAbsolutePath()
        );
    }

    @Override
    public void loadConfig() {
        File file = new File(moduleDirectory, configName + ".astralis");
        if (!file.exists()) {
            Astralis.LOGGER.warn("Config file {} not found, skipping load.", file.getAbsolutePath());
            return;
        }

        try (Reader fr = new FileReader(file);
             JsonReader jr = new JsonReader(fr)) {

            jr.setLenient(true);

            JsonElement root = JsonParser.parseReader(jr);
            if (root == null || !root.isJsonObject()) {
                Astralis.LOGGER.error("Config {} is not a JSON object. Skipping.", file.getName());
                return;
            }

            JsonObject obj = root.getAsJsonObject();
            ConfigUtil.loadConfig(obj);
        } catch (JsonSyntaxException | EOFException e) {
            try {
                File backup = new File(file.getAbsolutePath() + ".corrupt");
                Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                ChatUtil.print("§cConfig '" + configName + "' is corrupted (" + e.getMessage() + "). " +
                        "A backup was saved as '" + backup.getName() + "'. Loading defaults.");
            } catch (IOException ioEx) {
                Astralis.LOGGER.error("Failed to backup corrupt config: {}", ioEx.getMessage());
            }
            writeConfig();
        } catch (IOException e) {
            Astralis.LOGGER.error("Failed to load config '{}': {}", file.getName(), e.getMessage());
        }
    }
}
