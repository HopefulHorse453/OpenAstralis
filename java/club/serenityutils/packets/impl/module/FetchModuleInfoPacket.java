package club.serenityutils.packets.impl.module;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class FetchModuleInfoPacket extends Packet {
    public FetchModuleInfoPacket() {
        super(6);
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();

/*        addPropertiesToJson(json,
                "index", index
        );*/

         return EncryptionUtil.encrypt(json.toString(), 
                 Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}