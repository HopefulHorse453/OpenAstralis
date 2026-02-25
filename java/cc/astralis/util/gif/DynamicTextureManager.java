package cc.astralis.util.gif;

import cc.astralis.interfaces.IAccess;
import com.mojang.blaze3d.platform.NativeImage;
import astralis.mixin.accessor.mc.IdentifierAccessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class DynamicTextureManager implements IAccess {
    public static final Map<String, ResourceLocation> textureCache = new HashMap<>();

    public static ResourceLocation registerImageTexture(String imagePath) throws IOException {
        if (textureCache.containsKey(imagePath)) {
            return textureCache.get(imagePath);
        }

        File imageFile = new File(imagePath);
        try (InputStream in = new FileInputStream(imageFile)) {
            NativeImage image = NativeImage.read(in);
            ResourceLocation id = IdentifierAccessor.createIdentifier("astralis", imagePath);

            mc.getTextureManager().registerForNextReload(
                    id
            );

            textureCache.put(imagePath, id);
            return id;
        }
       // return IdentifierAccessor.createIdentifier("astralis", "fuckoff");
    }

    public static void cleanup() {
        textureCache.values().forEach(id ->
               mc.getTextureManager().release(id)
        );
        textureCache.clear();
    }
}
