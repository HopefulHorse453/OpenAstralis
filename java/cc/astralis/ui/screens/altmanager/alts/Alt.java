package cc.astralis.ui.screens.altmanager.alts;

import com.mojang.util.UndashedUuid;
import cc.astralis.util.network.AccountUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.User;
import java.io.IOException;
import java.util.UUID;

@Getter
@Setter
public class Alt {
    private AltGUI altGUI;
    private String token, uuid, name;
    private boolean premium;

    public Alt(String name, String token, String uuid, boolean premium) {
        this.altGUI = new AltGUI();
        this.name = name;
        this.token = token;
        this.uuid = uuid;
        this.premium = premium;
    }

    public Alt(String name, String token, String uuid) {
        this.altGUI = new AltGUI();
        this.name = name;
        this.token = token;
        this.uuid = uuid;
        this.premium = false;
    }

    public Alt(User session)  {
        this.altGUI = new AltGUI();
        this.name = session.getName();
        this.token = session.getAccessToken();
        this.uuid =  UndashedUuid.toString(session.getProfileId());
        this.premium = !session.getAccessToken().isEmpty();
    }

    public UUID getConvertedUUID() {
        return AccountUtil.formatUUID(uuid);
    }
}
