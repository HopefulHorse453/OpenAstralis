package cc.astralis.ui.imgui.elements;

import cc.astralis.Astralis;
import com.mojang.blaze3d.systems.RenderSystem;
import imgui.ImGui;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageRenderer {
    private int textureId;
    private String textureName;

    public ImageRenderer(String imagePath, String textureName) {
        this.textureId = loadTexture(imagePath);
        this.textureName = textureName;
    }

    public int loadTexture(String imagePath) {
      /*  try {
            NativeImage nativeImage = NativeImage.read(Files.newInputStream(Paths.get(imagePath)));
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
            TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
            Identifier identifier = textureManager.registerDynamicTexture(textureName, texture);

            RenderSystem.setShaderTexture(0, identifier);
            int textureId = texture.getGlId();

            return textureId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image: " + e.getMessage(), e);
        }*/
        return 0;
    }

    public void renderImage(float sizeX, float sizeY, float x, float y) {
      //  ImGui.setCursorPos(x, y);

    /*    if (textureId != 0) {
            //RenderSystem.bindTexture(textureId);
            ImGui.imageButton(textureId, sizeX, sizeY);
        } else {
            Astralis.LOGGER.error("uhh shi dont exist g");
        }*/
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
}
