package club.serenityutils.packets.impl.cloud;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class FetchCloudConfigsPacket extends Packet {
    public FetchCloudConfigsPacket() {
        super(9);
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();
         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}