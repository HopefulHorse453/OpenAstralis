package club.serenityutils.packets.impl;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class AuthPacket extends Packet {
    private int uid;

    public AuthPacket() {
        super(0);
    }

    public AuthPacket(int uid) {
        super(0);
        this.uid = uid;
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);
        this.uid = json.get("uid").getAsInt();
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();

        addPropertiesToJson(json,
                "uid", uid
        );

         return EncryptionUtil.encrypt(json.toString(),
                 Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                 Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}