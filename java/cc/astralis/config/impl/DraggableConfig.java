package cc.astralis.config.impl;

import cc.astralis.config.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cc.astralis.drag.Draggable;
import cc.astralis.interfaces.access.IDrag;
import cc.astralis.util.io.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class DraggableConfig extends Config {
    private final File path = new File(baseDirectory, "drag.json");

    @Override
    public void writeConfig() {
        JsonObject mainObject = new JsonObject();

        for (Draggable draggable : IDrag.draggables) {
            JsonObject draggableJsonObject = new JsonObject();

            draggableJsonObject.addProperty("xPos", draggable.getX());
            draggableJsonObject.addProperty("yPos", draggable.getY());
            mainObject.add(draggable.getName(), draggableJsonObject);
        }

        FileUtil.writeJsonToFile(mainObject, path.getAbsolutePath());
    }

    @Override
    public void loadConfig() {
        try (Reader reader = new FileReader(path.getAbsolutePath())) {
            JsonElement jsonElement = JsonParser.parseReader(reader);

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    String draggableName = entry.getKey();
                    JsonObject dragObject = entry.getValue().getAsJsonObject();

                    for (Draggable draggable : IDrag.draggables) {
                        if (draggable.getName().equalsIgnoreCase(draggableName)) {
                            draggable.setX(dragObject.get("xPos").getAsFloat());
                            draggable.setY(dragObject.get("yPos").getAsFloat());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
