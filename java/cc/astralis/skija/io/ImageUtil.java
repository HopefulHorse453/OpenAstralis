package cc.astralis.skija.io;

import cc.astralis.util.network.AccountUtil;
import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.DirectContext;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.SurfaceOrigin;
import org.lwjgl.opengl.GL11;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ImageUtil {
    private static final int MAX_ENTRIES = 256;

    private static final Map<Integer, Entry> CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, cc.astralis.skija.io.ImageUtil.Entry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public static Image fetchImage(DirectContext ctx,
                                   int glTexId,
                                   int w,
                                   int h,
                                   boolean alpha) {

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTexId);

        Entry e = CACHE.get(glTexId);

        if (e == null || !e.matches(w, h, alpha)) {
            Image img = adopt(ctx, glTexId, w, h, alpha);
            e = new Entry(img, w, h, alpha);
            CACHE.put(glTexId, e);
        }

        return e.image;
    }

    private static Image adopt(DirectContext ctx,
                               int glTexId,
                               int w,
                               int h,
                               boolean alpha) {
        ColorType ct = alpha ? ColorType.RGBA_8888 : ColorType.RGB_888X;
        return Image.adoptGLTextureFrom(
                ctx,
                glTexId,
                GL11.GL_TEXTURE_2D,
                w,
                h,
                GL11.GL_RGBA8,
                SurfaceOrigin.BOTTOM_LEFT,
                ct
        );
    }

    private static final class Entry {
        final Image image;
        final int width;
        final int height;
        final boolean hasAlpha;

        Entry(Image image, int width, int height, boolean hasAlpha) {
            this.image = Objects.requireNonNull(image, "image");
            this.width = width;
            this.height = height;
            this.hasAlpha = hasAlpha;
        }

        boolean matches(int w, int h, boolean alpha) {
            return this.width == w && this.height == h && this.hasAlpha == alpha;
        }
    }

    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    public static Image loadFromStream(InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        try (InputStream autoClose = is) {
            byte[] data = autoClose.readAllBytes();
            return Image.makeDeferredFromEncodedBytes(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Skija Image from stream", e);
        }
    }

    public static Image loadFromResource(String resourcePath) {
        return IMAGE_CACHE.computeIfAbsent(resourcePath, path -> {
            InputStream is = ImageUtil.class.getResourceAsStream("assets/astralis/" + path);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            return loadFromStream(is);
        });
    }

    public static Image loadFromAbsolutePath(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) {
            throw new IllegalArgumentException("Absolute path cannot be null or empty");
        }
        return IMAGE_CACHE.computeIfAbsent(absolutePath, path -> {
            try (InputStream is = new FileInputStream(path)) {
                return loadFromStream(is);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load Skija Image from path: " + path, e);
            }
        });
    }

    public static void clearImageCache() {
        for (Image img : IMAGE_CACHE.values()) {
            if (img != null) {
                img.close();
            }
        }

        IMAGE_CACHE.clear();
    }

    public static Image loadSkin(String uuid) {
        return ImageUtil.loadFromStream(AccountUtil.getSkinInputStream(uuid));
    }
}
