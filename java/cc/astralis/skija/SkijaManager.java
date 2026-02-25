package cc.astralis.skija;

import cc.astralis.Astralis;
import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Kawase
 * @since 16.08.2025
 */
public class SkijaManager {
    @Getter
    private static final Queue<Runnable> callbackQueue = new ConcurrentLinkedQueue<>();

    public static void addCallback(Runnable callback) {
        if (callback != null) {
            callbackQueue.offer(callback);
        }
    }

    // this is used for rendering guis etc, it's a bit junkie but wtv.
    public static void runCallbacks() {
        final var skia = Astralis.getInstance().getSkija();
        if (callbackQueue.isEmpty()) return;

        skia.begin();

        try {
            Runnable[] callbackArray = callbackQueue.toArray(new Runnable[0]);
            callbackQueue.clear();

            for (Runnable callback : callbackArray) {
                try {
                    callback.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            skia.end();
        }
    }
}
