package cc.astralis.config;

import cc.astralis.Astralis;
import cc.astralis.config.impl.ModulesConfig;
import cc.astralis.interfaces.IAccess;
import cc.astralis.util.render.ChatUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class ConfigManager implements IAccess {
    public final File directory = new File(mc.gameDirectory, "/" + Astralis.NAME.toLowerCase() + "/Configs");
    public HashMap<String, ModulesConfig> configs = new HashMap<>();

    public void addConfigsToHashMap() {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".astralis")) {
                String fileName = file.getName().substring(0, file.getName().length() - ".astralis".length());
                configs.put(fileName, new ModulesConfig(fileName));
            }
        }
    }

    public void init() {
        if (!directory.exists()) directory.mkdirs();
        addConfigsToHashMap();

        if (!configs.containsKey("default")) {
            writeDefaultConfig();
        } else {
            loadConfig("default");
        }
    }

    public void writeDefaultConfig() {
        saveConfig("default");
    }

    public void loadConfig(String configName) {
        ModulesConfig modulesConfig = configs.get(configName);
        if (modulesConfig == null) {
            ChatUtil.print("Failed To Load Config " + configName);
            return;
        }

        ChatUtil.print("Loaded Config " + configName);

        modulesConfig.loadConfig();
    }

    public void saveConfig(String configName) {
        ModulesConfig modulesConfig = configs.get(configName);
        if (modulesConfig == null) {
            modulesConfig = new ModulesConfig(configName);
            configs.put(configName, modulesConfig);
        }
        modulesConfig.writeConfig();
    }

    public void deleteConfig(String configName) {
        ModulesConfig modulesConfig = configs.get(configName);
        if (modulesConfig != null) {
            File configFile = new File(directory, configName + ".astralis");
            if (configFile.exists() && configFile.delete()) {
                configs.remove(configName);
            }
        }
    }
}
