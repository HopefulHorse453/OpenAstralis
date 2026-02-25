package club.serenityutils.packets.impl;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ClientKeepAlivePacket extends Packet {
    private long receivedTime, sentTime;
    private String sessionToken;

    public ClientKeepAlivePacket() {
        super(2);
    }

    public ClientKeepAlivePacket(String sessionToken) {
        super(2);
        this.sessionToken = sessionToken;
    }

    @Override
    public void fromJson(JsonObject jsonObject) {
        readBaseJson(jsonObject);
        this.sentTime = jsonObject.get("sentTime").getAsLong();
        this.receivedTime = System.currentTimeMillis();
        this.sessionToken = jsonObject.get("token").getAsString();
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();

        addPropertiesToJson(json,
                "sentTime", System.currentTimeMillis(),
                "token", sessionToken != null ? sessionToken : ""
        );
         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}