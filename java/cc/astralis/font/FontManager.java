package cc.astralis.font;

import cc.astralis.Astralis;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FontManager {
    public static boolean debugEnabled = true;

    private static final Map<String, UIFont> fonts = new ConcurrentHashMap<>();
    private static final Map<String, FontRenderer> nvgFonts = new ConcurrentHashMap<>();

    public static UIFont getFont(final String name, final int size) {
        if (name == null || name.isEmpty() || size <= 0) return null;

        final String key = name + '-' + size;
        return fonts.computeIfAbsent(key, k -> loadFont(name, size));
    }

    private static UIFont loadFont(final String name, final int size) {
        final String key = name + "-" + size;
        FontRenderer fr = nvgFonts.computeIfAbsent(key, k -> new FontRenderer(name, size));
        return new UIFont(fr, size);
    }

    public static synchronized void reinitializeAllFonts() {
        debugLog("Reinitializing all fonts...");
        Set<String> keys = new HashSet<>();
        keys.addAll(nvgFonts.keySet());
        keys.addAll(fonts.keySet());

        for (String key : keys) {
            NameSize ns = parseKey(key);
            if (ns == null) {
                debugError("Invalid key: " + key);
                continue;
            }
            FontRenderer old = nvgFonts.remove(key);
            safelyDispose(old);
            try {
                FontRenderer fresh = new FontRenderer(ns.name, ns.size);
                nvgFonts.put(key, fresh);
                fonts.put(key, new UIFont(fresh, ns.size));
                debugLog("Reinitialized: " + key);
            } catch (Exception e) {
                debugError("Failed to reinit " + key + ": " + e);
            }
        }
        debugLog("Reinit complete. NVG=" + nvgFonts.size() + " UI=" + fonts.size());
    }

    public static synchronized void disposeAllFonts() {
        debugLog("Disposing all fonts...");
        for (FontRenderer fr : nvgFonts.values()) safelyDispose(fr);
        nvgFonts.clear();
        fonts.clear();
        debugLog("Disposed.");
    }

    public static synchronized void disposeFont(String name, int size) {
        final String key = name + '-' + size;
        UIFont removedUI = fonts.remove(key);
        FontRenderer fr = nvgFonts.remove(key);
        safelyDispose(fr);
        if (removedUI == null && fr == null) {
            debugLog("disposeFont: nothing to dispose for " + key);
        } else {
            debugLog("Disposed font: " + key);
        }
    }

    private static NameSize parseKey(String key) {
        int cut = key.lastIndexOf('-');
        if (cut <= 0 || cut >= key.length() - 1) return null;
        String name = key.substring(0, cut);
        try {
            int size = Integer.parseInt(key.substring(cut + 1));
            return new NameSize(name, size);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void safelyDispose(FontRenderer renderer) {
        if (renderer == null) return;
        try {
            renderer.dispose();
        } catch (Throwable t) {
            debugError("Dispose error: " + t);
        }
    }

    private record NameSize(String name, int size) {}

    private static void debugLog(String message) {
        if (debugEnabled) {
            Astralis.LOGGER.info("[FontManager] " + message);
        }
    }

    private static void debugError(String message) {
        System.err.println("[FontManager ERROR] " + message);
    }

    public static void printLoadedFonts() {
        nvgFonts.keySet().forEach(name -> Astralis.LOGGER.error(" - " + name));
        fonts.keySet().forEach(key -> Astralis.LOGGER.error(" - " + key));
    }

    public static void clearCache() {
        debugLog("Clearing font cache (" + fonts.size() + " fonts, " + nvgFonts.size() + " NVG fonts)");
        fonts.clear();
        nvgFonts.clear();
    }
}
