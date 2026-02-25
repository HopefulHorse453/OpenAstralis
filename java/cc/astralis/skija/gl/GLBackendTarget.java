package cc.astralis.skija.gl;

import io.github.humbleui.skija.BackendRenderTarget;
import io.github.humbleui.skija.impl.Stats;
import lombok.Getter;

import org.jetbrains.annotations.NotNull;

// immutable
// why did I over-complicate this so much :broken_heart:.
public final class GLBackendTarget extends BackendRenderTarget {
    @Getter
    public static final class Specs {
        private final int width;
        private final int height;
        private final int samples;
        private final int stencilBits;
        private final int framebufferId;
        private final FramebufferFormat framebufferFormat;

        private Specs(Builder b) {
            this.width = b.width;
            this.height = b.height;
            this.samples = b.samples;
            this.stencilBits = b.stencilBits;
            this.framebufferId = b.framebufferId;
            this.framebufferFormat = b.framebufferFormat;
            validate();
        }

        private void validate() {
            if (width <= 0 || height <= 0)
                throw new IllegalArgumentException("width/height must be > 0");
            
            if (samples < 1)
                throw new IllegalArgumentException("samples must be >= 1");

            if (stencilBits < 0)
                throw new IllegalArgumentException("stencilBits must be >= 0");

            if (framebufferId < 0)
                throw new IllegalArgumentException("framebufferId must be >= 0");

            if (framebufferFormat == null)
                throw new IllegalArgumentException("framebufferFormat must not be null");
        }

        public static Builder builder(int width, int height) {
            return new Builder(width, height);
        }

        public static final class Builder {
            private final int width;
            private final int height;
            private int samples = 1;
            private int stencilBits = 0;
            private int framebufferId = 0;
            private FramebufferFormat framebufferFormat = FramebufferFormat.OTHER;

            private Builder(int width, int height) {
                this.width = width;
                this.height = height;
            }

            public Builder samples(int v) {
                this.samples = v;
                return this;
            }

            public Builder stencilBits(int v) {
                this.stencilBits = v;
                return this;
            }

            public Builder framebufferId(int v) {
                this.framebufferId = v;
                return this;
            }

            public Builder framebufferFormat(@NotNull FramebufferFormat f) {
                this.framebufferFormat = f;
                return this;
            }

            public Specs build() {
                return new Specs(this);
            }
        }
    }

    public enum FramebufferFormat {
        RGBA8(0x8058),          // GL_RGBA8
        SRGB8_ALPHA8(0x8C43),   // GL_SRGB8_ALPHA8
        RGB8(0x8051),           // GL_RGB8
        OTHER(-1);

        private final int code;

        FramebufferFormat(int code) {
            this.code = code;
        }

        public int glEnum() {
            return code;
        }

        public static FramebufferFormat fromCode(int code) {
            for (FramebufferFormat f : values()) if (f.code == code) return f;
            return OTHER;
        }
    }

    private final Specs specs;
    private final long nativePtr;

    private GLBackendTarget(@NotNull Specs specs, long ptr) {
        super(ptr);
        this.specs = specs;
        this.nativePtr = ptr;
    }

    public static GLBackendTarget fromGL(@NotNull Specs specs) {
        Stats.onNativeCall();
        long ptr = BackendRenderTarget._nMakeGL(
                specs.getWidth(), specs.getHeight(),
                specs.getSamples(), specs.getStencilBits(),
                specs.getFramebufferId(), specs.getFramebufferFormat().glEnum()
        );

        return new GLBackendTarget(specs, ptr);
    }

    public int width() {
        return specs.getWidth();
    }

    public int height() {
        return specs.getHeight();
    }

    @Override
    public String toString() {
        return "GLBackendTarget{" +
                "size=" + specs.getWidth() + "x" + specs.getHeight() +
                ", samples=" + specs.getSamples() +
                ", stencilBits=" + specs.getStencilBits() +
                ", fbId=" + specs.getFramebufferId() +
                ", fbFormat=" + specs.getFramebufferFormat() +
                ", ptr=0x" + Long.toHexString(nativePtr) +
                '}';
    }
}