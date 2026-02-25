package cc.astralis.module.impl.client;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.radio.RadioChannel;
import cc.astralis.radio.RadioPlayer;
import cc.astralis.util.math.RandomUtil;
import cc.astralis.util.render.ChatUtil;

public class RadioModule extends Module {
    private final ModeProperty radioMode = new ModeProperty(
            "Radio Mode",
            "I Love Radio", " I Love Radio",
            "I Love Dance", "I Love Deutschrap",
            "I Love Pop Hits", "I Love 2000s",
            "I Love 2010s", "I Love Bass"
    );

    private final RadioPlayer radioPlayer = new RadioPlayer();
    private RadioChannel lastChannel = null;

    public RadioModule() {
        super(Category.VISUAL);
        this.registerProperties(radioMode);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        RadioChannel selectedChannel = RadioChannel.fromModeSelection(radioMode.getProperty());

        if (selectedChannel != null && selectedChannel != lastChannel) {
            radioPlayer.playRadio(selectedChannel);
            lastChannel = selectedChannel;
        }

        if (selectedChannel == null && lastChannel != null) {
            radioPlayer.stopRadio();
            lastChannel = null;
        }
    }

    @Override
    public void onEnable() {
        RadioChannel selectedChannel = RadioChannel.fromModeSelection(radioMode.getProperty());
        if (selectedChannel != null) {
            radioPlayer.playRadio(selectedChannel);
            lastChannel = selectedChannel;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        radioPlayer.stopRadio();
        lastChannel = null;

        super.onDisable();
    }
}