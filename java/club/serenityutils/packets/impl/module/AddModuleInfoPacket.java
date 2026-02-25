package club.serenityutils.packets.impl.module;

import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class AddModuleInfoPacket extends Packet {
    private String moduleName, moduleDescription;

    public AddModuleInfoPacket() {
        super(108);
    }

    public AddModuleInfoPacket(String moduleName, String moduleDescription) {
        super(108);
        this.moduleName = moduleName;
        this.moduleDescription = moduleDescription;
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);
        this.moduleName = json.get("name").getAsString();
        this.moduleDescription = json.get("description").getAsString();
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();

        addPropertiesToJson(json,
                "name", moduleName,
                "description", moduleDescription
        );

         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}