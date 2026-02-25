package cc.astralis.skija.gl;

import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL45.*;

public class GlState {

    private final int glVersion;
    private final GlProperties glProps;

    public GlState(final int glVersion) {
        this.glVersion = glVersion;
        this.glProps = new GlProperties();
    }

    public final GlState push() {
        glGetIntegerv(GL_ACTIVE_TEXTURE, this.glProps.lastActiveTexture());
        glActiveTexture(GL_TEXTURE0);
        glGetIntegerv(GL_CURRENT_PROGRAM, this.glProps.lastProgram());
        glGetIntegerv(GL_TEXTURE_BINDING_2D, this.glProps.lastTexture());
        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            glGetIntegerv(GL_SAMPLER_BINDING, this.glProps.lastSampler());
        }
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, this.glProps.lastArrayBuffer());
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, this.glProps.lastVertexArrayObject());
        if (glVersion >= 200) {
            glGetIntegerv(GL_POLYGON_MODE, this.glProps.lastPolygonMode());
        }
        glGetIntegerv(GL_VIEWPORT, this.glProps.lastViewport());
        glGetIntegerv(GL_SCISSOR_BOX, this.glProps.lastScissorBox());
        glGetIntegerv(GL_BLEND_SRC_RGB, this.glProps.lastBlendSrcRgb());
        glGetIntegerv(GL_BLEND_DST_RGB, this.glProps.lastBlendDstRgb());
        glGetIntegerv(GL_BLEND_SRC_ALPHA, this.glProps.lastBlendSrcAlpha());
        glGetIntegerv(GL_BLEND_DST_ALPHA, this.glProps.lastBlendDstAlpha());
        glGetIntegerv(GL_BLEND_EQUATION_RGB, this.glProps.lastBlendEquationRgb());
        glGetIntegerv(GL_BLEND_EQUATION_ALPHA, this.glProps.lastBlendEquationAlpha());
        this.glProps.lastEnableBlend(glIsEnabled(GL_BLEND));
        this.glProps.lastEnableCullFace(glIsEnabled(GL_CULL_FACE));
        this.glProps.lastEnableDepthTest(glIsEnabled(GL_DEPTH_TEST));
        this.glProps.lastEnableStencilTest(glIsEnabled(GL_STENCIL_TEST));
        this.glProps.lastEnableScissorTest(glIsEnabled(GL_SCISSOR_TEST));
        if (glVersion >= 310) {
            this.glProps.lastEnablePrimitiveRestart(glIsEnabled(GL_PRIMITIVE_RESTART));
        }

        this.glProps.lastDepthMask(glGetBoolean(GL_DEPTH_WRITEMASK));

        glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, this.glProps.lastPixelUnpackBufferBinding());
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

        glGetIntegerv(GL_PACK_SWAP_BYTES, this.glProps.lastPackSwapBytes());
        glGetIntegerv(GL_PACK_LSB_FIRST, this.glProps.lastPackLsbFirst());
        glGetIntegerv(GL_PACK_ROW_LENGTH, this.glProps.lastPackRowLength());
        glGetIntegerv(GL_PACK_SKIP_PIXELS, this.glProps.lastPackSkipPixels());
        glGetIntegerv(GL_PACK_SKIP_ROWS, this.glProps.lastPackSkipRows());
        glGetIntegerv(GL_PACK_ALIGNMENT, this.glProps.lastPackAlignment());

        glGetIntegerv(GL_UNPACK_SWAP_BYTES, this.glProps.lastUnpackSwapBytes());
        glGetIntegerv(GL_UNPACK_LSB_FIRST, this.glProps.lastUnpackLsbFirst());
        glGetIntegerv(GL_UNPACK_ALIGNMENT, this.glProps.lastUnpackAlignment());
        glGetIntegerv(GL_UNPACK_ROW_LENGTH, this.glProps.lastUnpackRowLength());
        glGetIntegerv(GL_UNPACK_SKIP_PIXELS, this.glProps.lastUnpackSkipPixels());
        glGetIntegerv(GL_UNPACK_SKIP_ROWS, this.glProps.lastUnpackSkipRows());

        if (glVersion >= 120) {
            glGetIntegerv(GL_PACK_IMAGE_HEIGHT, this.glProps.lastPackImageHeight());
            glGetIntegerv(GL_PACK_SKIP_IMAGES, this.glProps.lastPackSkipImages());
            glGetIntegerv(GL_UNPACK_IMAGE_HEIGHT, this.glProps.lastUnpackImageHeight());
            glGetIntegerv(GL_UNPACK_SKIP_IMAGES, this.glProps.lastUnpackSkipImages());
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        return this;
    }

    public final GlState pop() {
        glUseProgram(this.glProps.lastProgram()[0]);
        glBindTexture(GL_TEXTURE_2D, this.glProps.lastTexture()[0]);

        if (this.glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            glBindSampler(0, this.glProps.lastSampler()[0]);
        }
        glActiveTexture(this.glProps.lastActiveTexture()[0]);
        glBindVertexArray(this.glProps.lastVertexArrayObject()[0]);
        glBindBuffer(GL_ARRAY_BUFFER, this.glProps.lastArrayBuffer()[0]);
        glBlendEquationSeparate(this.glProps.lastBlendEquationRgb()[0], this.glProps.lastBlendEquationAlpha()[0]);
        glBlendFuncSeparate(this.glProps.lastBlendSrcRgb()[0], this.glProps.lastBlendDstRgb()[0], this.glProps.lastBlendSrcAlpha()[0], this.glProps.lastBlendDstAlpha()[0]);
        if (this.glProps.lastEnableBlend()) glEnable(GL_BLEND);
        else glDisable(GL_BLEND);
        if (this.glProps.lastEnableCullFace()) glEnable(GL_CULL_FACE);
        else glDisable(GL_CULL_FACE);
        if (this.glProps.lastEnableDepthTest()) glEnable(GL_DEPTH_TEST);
        else glDisable(GL_DEPTH_TEST);
        if (this.glProps.lastEnableStencilTest()) glEnable(GL_STENCIL_TEST);
        else glDisable(GL_STENCIL_TEST);
        if (this.glProps.lastEnableScissorTest()) glEnable(GL_SCISSOR_TEST);
        else glDisable(GL_SCISSOR_TEST);
        if (this.glVersion >= 310) {
            if (this.glProps.lastEnablePrimitiveRestart()) glEnable(GL_PRIMITIVE_RESTART);
            else glDisable(GL_PRIMITIVE_RESTART);
        }
        if (this.glVersion >= 200) {
            glPolygonMode(GL_FRONT_AND_BACK, this.glProps.lastPolygonMode()[0]);
        }
        glViewport(this.glProps.lastViewport()[0], this.glProps.lastViewport()[1], this.glProps.lastViewport()[2], this.glProps.lastViewport()[3]);
        glScissor(this.glProps.lastScissorBox()[0], this.glProps.lastScissorBox()[1], this.glProps.lastScissorBox()[2], this.glProps.lastScissorBox()[3]);

        glPixelStorei(GL_PACK_SWAP_BYTES, this.glProps.lastPackSwapBytes()[0]);
        glPixelStorei(GL_PACK_LSB_FIRST, this.glProps.lastPackLsbFirst()[0]);
        glPixelStorei(GL_PACK_ROW_LENGTH, this.glProps.lastPackRowLength()[0]);
        glPixelStorei(GL_PACK_SKIP_PIXELS, this.glProps.lastPackSkipPixels()[0]);
        glPixelStorei(GL_PACK_SKIP_ROWS, this.glProps.lastPackSkipRows()[0]);
        glPixelStorei(GL_PACK_ALIGNMENT, this.glProps.lastPackAlignment()[0]);

        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, this.glProps.lastPixelUnpackBufferBinding()[0]);
        glPixelStorei(GL_UNPACK_SWAP_BYTES, this.glProps.lastUnpackSwapBytes()[0]);
        glPixelStorei(GL_UNPACK_LSB_FIRST, this.glProps.lastUnpackLsbFirst()[0]);
        glPixelStorei(GL_UNPACK_ALIGNMENT, this.glProps.lastUnpackAlignment()[0]);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, this.glProps.lastUnpackRowLength()[0]);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, this.glProps.lastUnpackSkipPixels()[0]);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, this.glProps.lastUnpackSkipRows()[0]);

        if (this.glVersion >= 120) {
            glPixelStorei(GL_PACK_IMAGE_HEIGHT, this.glProps.lastPackImageHeight()[0]);
            glPixelStorei(GL_PACK_SKIP_IMAGES, this.glProps.lastPackSkipImages()[0]);
            glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, this.glProps.lastUnpackImageHeight()[0]);
            glPixelStorei(GL_UNPACK_SKIP_IMAGES, this.glProps.lastUnpackSkipImages()[0]);
        }

        glDepthMask(this.glProps.lastDepthMask());
        return this;
    }
}
