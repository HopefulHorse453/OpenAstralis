package club.serenityutils.cloudconfigs.api;

import club.serenityutils.user.api.IUser;
import com.google.gson.JsonObject;

public interface ICloudConfig {
    void setName(String name);
    String getName();

    long getUploadDate();
    void setUploadDate(long date);

    JsonObject getConfigData();
    void setConfigData(JsonObject configData);

    String getDescription();
    void setDescription(String description);

    int getConfigId();

    IUser getUser();

    JsonObject toJsonObject();
}
