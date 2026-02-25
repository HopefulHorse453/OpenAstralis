package club.serenityutils.packets.impl;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class MessagePacket extends Packet {
    private String message;

    public MessagePacket() {
        super(1);
    }

    public MessagePacket(String message) {
        super(1);
        this.message = message;
    }

    // when this is reach the json object will always alr be encrypted
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
