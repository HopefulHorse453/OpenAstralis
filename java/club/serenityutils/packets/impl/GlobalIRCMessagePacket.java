package club.serenityutils.packets.impl;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class GlobalIRCMessagePacket extends Packet {
    private String message;

    public GlobalIRCMessagePacket() {
        super(13);
    }

    public GlobalIRCMessagePacket(String message) {
        super(13);
        this.message = message;
    }

    @Override
    public void fromJson(JsonObject jsonObject) {
        readBaseJson(jsonObject);
        this.message = jsonObject.get("message").getAsString();
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();
        addPropertyToJson(json, "message", message);

        return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}
