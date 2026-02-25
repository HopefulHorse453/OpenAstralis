package club.serenityutils.packets.api;

import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;

public interface IPacket {
    int getId();
    void fromJson(JsonObject json);
    String dataToString();
    String getReceiver();
    void setReceiver(String receiver);
    void sendPacket(WebSocketClient printWriter);
}