package cc.astralis.ui.screens.altmanager.microsoft;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MicrosoftClient {
    private static final List<MicrosoftClient> CLIENTS = new ArrayList<>();

    private final String id;
    private final String name;
    private final ChatFormatting color;

    public MicrosoftClient(String id, String name, ChatFormatting color) {
        this.id = id;
        this.name = name;
        this.color = color;

        CLIENTS.add(this);
    }

    public static List<MicrosoftClient> getClients() {
        return CLIENTS;
    }

    public static MicrosoftClient getByName(String name) {
        return CLIENTS.stream()
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}