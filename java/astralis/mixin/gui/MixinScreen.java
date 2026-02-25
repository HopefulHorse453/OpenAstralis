package astralis.mixin.gui;

import cc.astralis.Astralis;
import cc.astralis.protection.Flags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Unique
    private long startTime;


/*
    @Inject(method = "init*", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen == Astralis.getInstance().getAuthScreen())
            return;

        if ((Flags.isNotAuthenticated || !Objects.equals(Flags.authStatus, "gud boy") || !Flags.authGuiShown)) {
            MinecraftClient.getInstance().setScreen(Astralis.getInstance().getAuthScreen());
            ci.cancel();
        }
    }
*/

    // this shit doesn't work :sob:
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("HEAD"), cancellable = true)
    private void renderShaderBackground(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      /*  if (shader == null) {
            try {
                shader = new GLSLSandBox(Identifier.of("astralis", "shaders/shader.fsh"));
                startTime = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
        int windowWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        int windowHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        shader.useShader(windowWidth, windowHeight, mouseX, mouseY, elapsedTime);

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        bufferBuilder.vertex(-1f, -1f, 0);
        bufferBuilder.vertex(-1f, 1f, 0);
        bufferBuilder.vertex(1f, 1f, 0);
        bufferBuilder.vertex(1f, -1f, 0);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.disableBlend();
        ci.cancel();*/
    }
}
