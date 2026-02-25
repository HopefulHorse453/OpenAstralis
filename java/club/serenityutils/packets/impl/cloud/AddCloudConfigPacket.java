package club.serenityutils.packets.impl.cloud;

import cc.astralis.protection.util.Base64;
import club.serenityutils.cloudconfigs.CloudConfig;
import club.serenityutils.cloudconfigs.api.ICloudConfig;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class AddCloudConfigPacket extends Packet {
    private ICloudConfig cloudConfig;

    public AddCloudConfigPacket() {
        super(11);
    }

    public AddCloudConfigPacket(ICloudConfig cloudConfig) {
        super(11);
        this.cloudConfig = cloudConfig;
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);

        this.cloudConfig = CloudConfig.fromJsonObject(json);
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();

        addPropertiesToJson(json,
                "name", cloudConfig.getName(),
                "description", cloudConfig.getDescription(),
                "userUid", cloudConfig.getUser().getUid(),
                "userName", cloudConfig.getUser().getName(),
                "uploadDate", cloudConfig.getUploadDate(),
                "configId", cloudConfig.getConfigId(),
                "configData", cloudConfig.getConfigData().toString()
        );

         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}