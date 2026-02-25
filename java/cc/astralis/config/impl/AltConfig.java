package cc.astralis.config.impl;

import cc.astralis.config.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cc.astralis.ui.screens.altmanager.AltManagerScreen;
import cc.astralis.ui.screens.altmanager.alts.Alt;
import cc.astralis.util.io.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class AltConfig extends Config {
    private final File path = new File(baseDirectory, "alts.json");
    private final boolean encryption;

    public AltConfig(boolean encryption) {
        this.encryption = encryption;
    }

    @Override
    public void writeConfig() {
        JsonObject altObject = new JsonObject();

        AltManagerScreen.getAlts().forEach(alt -> {
            JsonObject altAttributes = new JsonObject();
            altAttributes.addProperty("token", alt.getToken());
            altAttributes.addProperty("uuid", alt.getUuid());
            // todo: this should be in the alt class but me lazy so ima do it later cuz i wanna play phasmophobia.
            altAttributes.addProperty("encryption", encryption);

            altObject.add(alt.getName(), altAttributes);
        });

        FileUtil.writeJsonToFile(altObject, path.getAbsolutePath());
    }

    @Override
    public void loadConfig() {
        try {
            try (Reader reader = new FileReader(path.getAbsolutePath())) {
                JsonElement jsonElement = JsonParser.parseReader(reader);

                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        String altName = entry.getKey();
                        JsonObject altObject = entry.getValue().getAsJsonObject();

                        Alt alt = new Alt(altName,
                                altObject.get("token").getAsString(),
                                altObject.get("uuid").getAsString(), !altObject.get("token").getAsString().isEmpty());

                        AltManagerScreen.getAlts().add(alt);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
