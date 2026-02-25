package club.serenityutils.packets.impl;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

// we use this packet to transfer info to the client with different headers.
// for example, we could do header: auth, message: gud boy.
// i rlly should make client and server packets :pray:.
@Getter
public class DataTransferPacket extends Packet {
    private String header, message;

    public DataTransferPacket() {
        super(5);
    }

    public DataTransferPacket(String header, String message) {
        super(5);
        this.header = header;
        this.message = message;
    }

    @Override
    public void fromJson(JsonObject jsonObject) {
        readBaseJson(jsonObject);
        this.header = jsonObject.get("header").getAsString();
        this.message = jsonObject.get("message").getAsString();
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();
        addPropertiesToJson(json,
                "header", header,
                "message", message
        );

         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}
