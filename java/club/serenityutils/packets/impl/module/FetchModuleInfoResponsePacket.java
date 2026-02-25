package club.serenityutils.packets.impl.module;

import cc.astralis.protection.util.Base64;
import club.serenityutils.modules.ModuleMetaData;
import club.serenityutils.packets.Packet;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FetchModuleInfoResponsePacket extends Packet {
    private List<ModuleMetaData> moduleMetaData = new ArrayList<>();

    public FetchModuleInfoResponsePacket() {
        super(8);
    }

    public FetchModuleInfoResponsePacket(List<ModuleMetaData> modules) {
        super(8);
        this.moduleMetaData = modules;
    }

    @Override
    public void fromJson(JsonObject json) {
        readBaseJson(json);

        if (!json.has("modules")) {
            return;
        }

        JsonArray modulesArray = json.getAsJsonArray("modules");
        if (modulesArray == null) {
            return;
        }

        for (JsonElement element : modulesArray) {
            JsonObject moduleObject = element.getAsJsonObject();
            moduleMetaData.add(new ModuleMetaData(
                    moduleObject.get("name").getAsString(),
                    moduleObject.get("description").getAsString()
            ));
        }
    }

    @Override
    public String dataToString() {
        JsonObject json = buildBaseJson();
        JsonArray moduleArray = new JsonArray();

        for (ModuleMetaData module : moduleMetaData) {
            JsonObject moduleObject = new JsonObject();

            addPropertiesToJson(moduleObject,
                    "name", module.getName(),
                    "description", module.getDescription()
            );

            moduleArray.add(moduleObject);
        }

        json.add(EncryptionUtil.encrypt("modules", dynamicKey, validKeyRandom), moduleArray);
         return EncryptionUtil.encrypt(json.toString(),
                Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE="),
                Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="));
    }
}