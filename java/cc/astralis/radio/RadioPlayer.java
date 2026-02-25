package cc.astralis.radio;

import cc.astralis.util.render.ChatUtil;
import javazoom.jl.player.advanced.AdvancedPlayer;
import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;

public class RadioPlayer {
    private AdvancedPlayer player;
    private Thread playerThread;

    public void playRadio(RadioChannel channel) {
        stopRadio();

        playerThread = new Thread(() -> {
            try {
                InputStream stream = new BufferedInputStream(
                        URI.create(channel.getUrl()).toURL().openStream()
                );

                player = new AdvancedPlayer(stream);
                ChatUtil.printDebug("Now playing: " + channel.getName());
                player.play();
            } catch (Exception e) {
                ChatUtil.printDebug("Error playing radio: " + e.getMessage());
            }
        });

        playerThread.start();
    }

    public void stopRadio() {
        if (player != null)
            player.close();

        if (playerThread != null)
            playerThread.interrupt();
    }
}