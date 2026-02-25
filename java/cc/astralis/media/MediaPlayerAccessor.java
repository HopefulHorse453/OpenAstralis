package cc.astralis.media;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class MediaPlayerAccessor {
    public static IMediaSession session;

    public void run() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            var mediaSessions = MediaPlayerInfo.Instance.getMediaSessions();
            IMediaSession mediaSession = null;

            for (IMediaSession session : mediaSessions) {
                if (!session.getMedia().getPlaying())
                    continue;

                mediaSession = session;
            }

            MediaPlayerAccessor.session = mediaSession;
        }, 0, 1, TimeUnit.SECONDS);
    }
}