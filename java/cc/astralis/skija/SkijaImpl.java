package cc.astralis.skija;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.interfaces.access.GlTextureItf;
import cc.astralis.interfaces.IAccess;
import cc.astralis.skija.gl.GlStates;
import cc.astralis.skija.gl.GLBackendTarget;
import cc.astralis.skija.utils.SkijaHelperUtil;
import cc.astralis.skija.utils.SkijaUtil;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.impl.Stats;
import org.lwjgl.opengl.GL11;

import java.util.Objects;
import net.minecraft.client.Minecraft;

public class SkijaImpl implements IAccess {
    private static final GLBackendState[] STATES = {
            GLBackendState.BLEND,
            GLBackendState.VERTEX,
            GLBackendState.PIXEL_STORE,
            GLBackendState.TEXTURE_BINDING,
            GLBackendState.MISC
    };

    private DirectContext context;
    private Surface surface;
    private Canvas canvas;
    private GLBackendTarget renderTarget;
    private Image framebufferImage;
    private int width, height;

    public SkijaImpl() {
        Astralis.getInstance().getEventManager().register(this);
    }

    public void init(final int width, final int height) {
        if (Objects.isNull(this.context)) {
            this.context = DirectContext.makeGL();
        }

        this.width = width;
        this.height = height;

        if (Objects.nonNull(this.surface))
            this.surface.close();

        if (Objects.nonNull(this.renderTarget))
            this.renderTarget.close();

        final RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
        final int previousFramebuffer = ((GlTexture) framebuffer.getColorTexture()).getFbo(((GlDevice) RenderSystem.getDevice()).directStateAccess(), mc.getMainRenderTarget().getDepthTexture());

        GLBackendTarget.Specs specs = GLBackendTarget.Specs.builder(width, height)
                .samples(1)
                .stencilBits(8)
                .framebufferId(previousFramebuffer)
                .framebufferFormat(GLBackendTarget.FramebufferFormat.RGBA8)
                .build();

        this.renderTarget = GLBackendTarget.fromGL(specs);

        this.surface = Surface.wrapBackendRenderTarget(
                this.context,
                this.renderTarget,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.getSRGB()
        );

        this.canvas = this.surface.getCanvas();
        SkijaUtil.setCanvas(canvas);
    }

    public void begin() {
        Stats.enabled = true;
        if (this.context == null || this.surface == null || this.canvas == null || this.renderTarget == null) {
            return;
        }

        final var win = mc.getWindow();
        SkijaHelperUtil.setUiScale((float) win.getGuiScale());

        this.context.resetGLAll();
        
        GlStates.push();
        
        this.canvas.save();

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0f, 0f, 0f, 0f);

        this.context.resetGL(STATES);

        final GlTexture colorAttachment = (GlTexture) mc.getMainRenderTarget().getColorTexture();

        this.framebufferImage = ((GlTextureItf) colorAttachment)
                .astralis$getOrCreateSkikoImage(context, mc.getMainRenderTarget(), false);
    }

    public void render() {
        final var win = mc.getWindow();

        SkijaUtil.beginShaderFrame();
        Astralis.getInstance().getEventManager().call(new ShaderEvent(width, height));
        SkijaUtil.drawShaders();

        Astralis.getInstance().getEventManager().call(new Render2DEvent());
    }

    public void end() {
        this.canvas.restore();
        
        this.surface.flushAndSubmit();
        
        GlStates.pop();
        
        Stats.nativeCalls = 0;
    }

    public Image framebufferImage() {
        return this.framebufferImage;
    }
}
