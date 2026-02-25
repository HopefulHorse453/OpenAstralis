package club.serenityutils.packets.impl;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class HandShakePacket extends Packet {
    private String clientTypeName;
    private String hash;
    private double version;
    private String hwid;

    public HandShakePacket() {
        super(3);
    }

    public HandShakePacket(String clientTypeName, String hwid, String hash, double version) {
        super(3);
        this.clientTypeName = clientTypeName;
        this.hash = hash;
        this.version = version;
        this.hwid = hwid;
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);
        this.clientTypeName = json.get("clientName").getAsString();
        this.hash = json.get("hash").getAsString();
        this.version = json.get("version").getAsDouble();
        this.hwid = json.get("hwid").getAsString();
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();

        addPropertiesToJson(json,
                "clientName", clientTypeName,
                "hash", hash,
                "version", String.valueOf(version),
                "hwid", hwid
        );

         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}