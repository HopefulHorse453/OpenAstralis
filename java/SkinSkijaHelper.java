/*
package cc.astralis.skija.io;

import io.github.humbleui.skija.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class SkinSkijaHelper {
    private SkinSkijaHelper() {}

    private static final Map<Identifier, WeakReference<Image>> CACHE = new WeakHashMap<>();

    */
/** Returns a Skija Image of the entity's head (face + hat). Null if texture not available yet. *//*

    public static Image getHeadImage(LivingEntity entity) {
        Identifier skinId = resolveTextureId(entity);
        if (skinId == null) return null;

        Image cached = CACHE.getOrDefault(skinId, new WeakReference<>(null)).get();
        if (cached != null) return cached;

        NativeImage full = readNativeImage(skinId);
        if (full == null) return null;

        // Compute UVs (8x8 face, 8x8 hat) scaled by resolution (64px or 128px).
        int w = full.getWidth(), h = full.getHeight();
        int unit = Math.max(1, w / 64);

        int faceX = clamp(8 * unit, 0, w - 1);
        int faceY = clamp(8 * unit, 0, h - 1);
        int faceW = clamp(8 * unit, 1, w - faceX);
        int faceH = clamp(8 * unit, 1, h - faceY);

        int hatX  = clamp(40 * unit, 0, w - 1);
        int hatY  = clamp(8  * unit, 0, h - 1);
        int hatW  = clamp(8  * unit, 1, w - hatX);
        int hatH  = clamp(8  * unit, 1, h - hatY);

        // Compose a small 32x32 head image in-memory using NativeImage (ARGB).
        NativeImage out = new NativeImage(NativeImage.Format.RGBA, 32, 32, true);
        try {
            blitScaled(full, faceX, faceY, faceW, faceH, out, 0, 0, 32, 32, false);
            blitScaled(full, hatX,  hatY,  hatW,  hatH,  out, 0, 0, 32, 32, true);

            // Convert to premultiplied RGBA bytes and create a Skija raster image.
            Image skijaImg = makeSkijaFromNativeImage(out);
            CACHE.put(skinId, new WeakReference<>(skijaImg));
            return skijaImg;
        } finally {
            out.close();
        }
    }

    // -------- internals --------

    private static Identifier resolveTextureId(LivingEntity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (entity instanceof AbstractClientPlayerEntity p) {
            return p.getSkinTextures().texture(); // main player skin
        }
        // Mobs/others: ask renderer for its texture
        var dispatcher = mc.getEntityRenderDispatcher();
        if (dispatcher == null) return null;
        @SuppressWarnings({ "rawtypes", "unchecked" })
        EntityRenderer<LivingEntity, ?> renderer = (EntityRenderer) dispatcher.getRenderer(entity);
        return renderer != null ? renderer.getTexture(entity) : null;
    }

    private static NativeImage readNativeImage(Identifier id) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Prefer the bound GL texture if it's a NativeImageBackedTexture
        AbstractTexture tex = mc.getTextureManager().getTexture(id);
        if (tex instanceof NativeImageBackedTexture nibt) {
            NativeImage img = nibt.getImage();
            if (img != null) return img;
            // fall through if still loading
        }

        // Fallback: load from resources
        try {
            var opt = mc.getResourceManager().getResource(id);
            if (opt.isEmpty()) return null;
            try (var in = opt.get().getInputStream()) {
                return NativeImage.read(in);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static void blitScaled(NativeImage src, int sx, int sy, int sw, int sh,
                                   NativeImage dst, int dx, int dy, int dw, int dh, boolean alphaBlend) {
        for (int y = 0; y < dh; y++) {
            int srcY = sy + (y * sh) / dh;
            for (int x = 0; x < dw; x++) {
                int srcX = sx + (x * sw) / dw;
                int sARGB = src.getColorArgb(srcX, srcY); // ARGB
                if (alphaBlend) {
                    int dARGB = dst.getColorArgb(dx + x, dy + y);
                    dst.setColorArgb(dx + x, dy + y, blendARGB(dARGB, sARGB));
                } else {
                    dst.setColorArgb(dx + x, dy + y, sARGB);
                }
            }
        }
    }

    private static int blendARGB(int dst, int src) {
        int sa = (src >>> 24) & 0xFF;
        if (sa == 0) return dst;
        if (sa == 255) return src;

        int sr = (src >>> 16) & 0xFF, sg = (src >>> 8) & 0xFF, sb = src & 0xFF;
        int da = (dst >>> 24) & 0xFF, dr = (dst >>> 16) & 0xFF, dg = (dst >>> 8) & 0xFF, db = dst & 0xFF;

        int outA = sa + ((da * (255 - sa) + 127) / 255);
        int outR = (sr * sa + dr * (255 - sa) + 127) / 255;
        int outG = (sg * sa + dg * (255 - sa) + 127) / 255;
        int outB = (sb * sa + db * (255 - sa) + 127) / 255;

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }

    private static Image makeSkijaFromNativeImage(NativeImage ni) {
        int w = ni.getWidth(), h = ni.getHeight();
        byte[] rgba = new byte[w * h * 4];

        int i = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = ni.getColorArgb(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8)  & 0xFF;
                int b =  argb         & 0xFF;

                // Premultiply
                int pr = (r * a + 127) / 255;
                int pg = (g * a + 127) / 255;
                int pb = (b * a + 127) / 255;

                rgba[i++] = (byte) pr;
                rgba[i++] = (byte) pg;
                rgba[i++] = (byte) pb;
                rgba[i++] = (byte) a;
            }
        }

        ImageInfo info = new ImageInfo(w, h, ColorType.RGBA_8888, ColorAlphaType.PREMUL);
        Data data = Data.makeFromBytes(rgba);
        return Image.makeRasterFromData(info, data, w * 4);
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
*/
