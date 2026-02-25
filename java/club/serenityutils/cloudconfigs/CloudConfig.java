package club.serenityutils.cloudconfigs;

import club.serenityutils.cloudconfigs.api.ICloudConfig;
import club.serenityutils.user.User;
import club.serenityutils.user.api.IUser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloudConfig implements ICloudConfig {
    private String name, description;
    private long uploadDate;
    private JsonObject configData;
    private int configId;

    // User related info this class stores the username and uid :pray:
    private IUser user;

    public CloudConfig(String name, String description, int userUid, String userName, JsonObject configData) {
        this.name = name;
        this.description = description;
        this.configData = configData;
        this.uploadDate = System.currentTimeMillis();
        this.user = new User(userName, userUid);
        this.configId = (name + userName +  userUid + uploadDate).hashCode();
    }

    public CloudConfig(String name, String description, int userUid, String userName, long uploadDate, JsonObject configData, int configId) {
        this.name = name;
        this.description = description;
        this.uploadDate = uploadDate;
        this.configData = configData;
        this.configId = configId;
        this.user = new User(userName, userUid);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("userUid", user.getUid());
        jsonObject.addProperty("userName", user.getName());
        jsonObject.addProperty("uploadDate", uploadDate);
        jsonObject.addProperty("configId", configId);
        jsonObject.add("configData", configData);
        return jsonObject;
    }

    public static CloudConfig fromJsonObject(JsonObject obj) {
        JsonObject configData = new JsonObject();

        if (obj.has("configData")) {
            JsonElement element = obj.get("configData");

            if (element.isJsonObject()) {
                configData = element.getAsJsonObject();
            } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                try {
                    configData = JsonParser.parseString(element.getAsString()).getAsJsonObject();
                } catch (Exception e) {
                    configData = new JsonObject();
                }
            }
        }

        return new CloudConfig(
                obj.get("name").getAsString(),
                obj.has("description") ? obj.get("description").getAsString() : "",
                obj.get("userUid").getAsInt(),
                obj.get("userName").getAsString(),
                obj.get("uploadDate").getAsLong(),
                configData,
                obj.has("configId") ? obj.get("configId").getAsInt() : -1
        );
    }
}
