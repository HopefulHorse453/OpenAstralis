package cc.astralis.radio;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RadioChannel {
    ILOVE_RADIO("I Love Radio", "https://ilm.stream18.radiohost.de/ilm_iloveradio_mp3-192", 1),
    ILOVE_DANCE("I Love Dance", "https://ilm.stream39.radiohost.de/ilm_ilovedance_mp3-192", 2),
    ILOVE_DEUTSCHRAP("I Love Deutschrap", "https://ilm.stream12.radiohost.de/ilm_ilovedeutschrapbeste_mp3-192", 6),
    ILOVE_POPHITS("I Love Pop Hits", "https://ilm.stream35.radiohost.de/ilm_ilovenewpop_mp3-192", 16),
    ILOVE_2000S("I Love 2000s", "https://ilm.stream35.radiohost.de/ilm_ilove2000throwbacks_mp3-192", 37),
    ILOVE_2010S("I Love 2010s", "https://ilm.stream35.radiohost.de/ilm_ilove2010throwbacks_mp3-192", 38),
    ILOVE_BASS("I Love Bass", "https://ilm.stream12.radiohost.de/ilm_ilovebass_mp3-192", 39);

    private final String name;
    private final String url;
    private final int id;

    public static RadioChannel fromModeSelection(String modeSelection) {
        if (modeSelection == null) return null;

        String cleanSelection = modeSelection.trim();

        for (RadioChannel channel : values()) {
            if (channel.getName().equals(cleanSelection)) {
                return channel;
            }
        }

        for (RadioChannel channel : values()) {
            if (channel.getName().trim().equals(cleanSelection.trim())) {
                return channel;
            }
        }

        for (RadioChannel channel : values()) {
            if (channel.getName().equalsIgnoreCase(cleanSelection)) {
                return channel;
            }
        }

        for (RadioChannel channel : values()) {
            if (cleanSelection.toLowerCase().contains(channel.getName().toLowerCase()) ||
                    channel.getName().toLowerCase().contains(cleanSelection.toLowerCase())) {
                return channel;
            }
        }

        return null;
    }
}
