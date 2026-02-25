package club.serenityutils.packets.impl;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ServerKeepAlivePacket extends Packet {
    private long receivedTime, sentTime;

    public ServerKeepAlivePacket() {
        super(7);
    }

    @Override
    public void fromJson(JsonObject jsonObject) {
        readBaseJson(jsonObject);
        this.sentTime = jsonObject.get("sentTime").getAsLong();
        this.receivedTime = System.currentTimeMillis();
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();

         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}