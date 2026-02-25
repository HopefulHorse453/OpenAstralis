package club.serenityutils.packets.impl.cloud;

import cc.astralis.protection.util.Base64;
import club.serenityutils.cloudconfigs.CloudConfig;
import club.serenityutils.cloudconfigs.api.ICloudConfig;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FetchCloudConfigsResponsePacket extends Packet {
    private List<ICloudConfig> cloudConfigList = new ArrayList<>();

    public FetchCloudConfigsResponsePacket() {
        super(10);
    }

    public FetchCloudConfigsResponsePacket(List<ICloudConfig> cloudConfigList) {
        super(10);
        this.cloudConfigList = cloudConfigList;
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);

        if (!json.has("configs"))
            return;

        JsonArray configArray = json.getAsJsonArray("configs");
        if (configArray == null)
            return;

        for (JsonElement element : configArray) {
            JsonObject configObject = element.getAsJsonObject();
            cloudConfigList.add(CloudConfig.fromJsonObject(configObject));
        }
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();
        JsonArray configArray = new JsonArray();

        for (ICloudConfig config : cloudConfigList) {
            JsonObject configObject = new JsonObject();

            addPropertiesToJson(configObject,
                    "name", config.getName(),
                    "description", config.getDescription(),
                    "userUid", config.getUser().getUid(),
                    "userName", config.getUser().getName(),
                    "uploadDate", config.getUploadDate(),
                    "configId", config.getConfigId(),
                    "configData", config.getConfigData().toString()
            );

            configArray.add(configObject);
        }

        json.add(EncryptionUtil.encrypt("configs", dynamicKey, validKeyRandom), configArray);
         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}
