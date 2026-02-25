package cc.astralis.skija.gl;

public final class GlProperties {
    private final int[] lastActiveTexture = new int[1];
    private final int[] lastProgram = new int[1];
    private final int[] lastTexture = new int[1];
    private final int[] lastSampler = new int[1];
    private final int[] lastArrayBuffer = new int[1];
    private final int[] lastVertexArrayObject = new int[1];

    public int[] lastVertexArrayObject() {
        return lastVertexArrayObject;
    }

    public int[] lastArrayBuffer() {
        return lastArrayBuffer;
    }

    public int[] lastSampler() {
        return lastSampler;
    }

    public int[] lastTexture() {
        return lastTexture;
    }

    public int[] lastProgram() {
        return lastProgram;
    }

    public int[] lastActiveTexture() {
        return lastActiveTexture;
    }

    public int[] lastPolygonMode() {
        return lastPolygonMode;
    }

    public int[] lastViewport() {
        return lastViewport;
    }

    public int[] lastScissorBox() {
        return lastScissorBox;
    }

    public int[] lastBlendSrcRgb() {
        return lastBlendSrcRgb;
    }

    public int[] lastBlendDstRgb() {
        return lastBlendDstRgb;
    }

    public int[] lastBlendSrcAlpha() {
        return lastBlendSrcAlpha;
    }

    public int[] lastBlendDstAlpha() {
        return lastBlendDstAlpha;
    }

    public int[] lastBlendEquationRgb() {
        return lastBlendEquationRgb;
    }

    public int[] lastBlendEquationAlpha() {
        return lastBlendEquationAlpha;
    }

    public int[] lastPixelUnpackBufferBinding() {
        return lastPixelUnpackBufferBinding;
    }

    public int[] lastUnpackAlignment() {
        return lastUnpackAlignment;
    }

    public int[] lastUnpackRowLength() {
        return lastUnpackRowLength;
    }

    public int[] lastUnpackSkipPixels() {
        return lastUnpackSkipPixels;
    }

    public int[] lastUnpackSkipRows() {
        return lastUnpackSkipRows;
    }

    public int[] lastPackSwapBytes() {
        return lastPackSwapBytes;
    }

    public int[] lastPackLsbFirst() {
        return lastPackLsbFirst;
    }

    public int[] lastPackRowLength() {
        return lastPackRowLength;
    }

    public int[] lastPackImageHeight() {
        return lastPackImageHeight;
    }

    public int[] lastPackSkipPixels() {
        return lastPackSkipPixels;
    }

    public int[] lastPackSkipRows() {
        return lastPackSkipRows;
    }

    public int[] lastPackSkipImages() {
        return lastPackSkipImages;
    }

    public int[] lastPackAlignment() {
        return lastPackAlignment;
    }

    public int[] lastUnpackSwapBytes() {
        return lastUnpackSwapBytes;
    }

    public int[] lastUnpackLsbFirst() {
        return lastUnpackLsbFirst;
    }

    public int[] lastUnpackImageHeight() {
        return lastUnpackImageHeight;
    }

    public int[] lastUnpackSkipImages() {
        return lastUnpackSkipImages;
    }

    public boolean lastEnableBlend() {
        return lastEnableBlend;
    }

    public void lastEnableBlend(final boolean lastEnableBlend) {
        this.lastEnableBlend = lastEnableBlend;
    }

    public boolean lastEnableCullFace() {
        return lastEnableCullFace;
    }

    public void lastEnableCullFace(final boolean lastEnableCullFace) {
        this.lastEnableCullFace = lastEnableCullFace;
    }

    public boolean lastEnableDepthTest() {
        return lastEnableDepthTest;
    }

    public void lastEnableDepthTest(final boolean lastEnableDepthTest) {
        this.lastEnableDepthTest = lastEnableDepthTest;
    }

    public boolean lastEnableStencilTest() {
        return lastEnableStencilTest;
    }

    public void lastEnableStencilTest(final boolean lastEnableStencilTest) {
        this.lastEnableStencilTest = lastEnableStencilTest;
    }

    public boolean lastEnableScissorTest() {
        return lastEnableScissorTest;
    }

    public void lastEnableScissorTest(final boolean lastEnableScissorTest) {
        this.lastEnableScissorTest = lastEnableScissorTest;
    }

    public boolean lastEnablePrimitiveRestart() {
        return lastEnablePrimitiveRestart;
    }

    public void lastEnablePrimitiveRestart(final boolean lastEnablePrimitiveRestart) {
        this.lastEnablePrimitiveRestart = lastEnablePrimitiveRestart;
    }

    public boolean lastDepthMask() {
        return lastDepthMask;
    }

    public void lastDepthMask(final boolean lastDepthMask) {
        this.lastDepthMask = lastDepthMask;
    }

    private final int[] lastPolygonMode = new int[2];
    private final int[] lastViewport = new int[4];
    private final int[] lastScissorBox = new int[4];
    private final int[] lastBlendSrcRgb = new int[1];
    private final int[] lastBlendDstRgb = new int[1];
    private final int[] lastBlendSrcAlpha = new int[1];
    private final int[] lastBlendDstAlpha = new int[1];
    private final int[] lastBlendEquationRgb = new int[1];
    private final int[] lastBlendEquationAlpha = new int[1];

    // Skia
    private final int[] lastPixelUnpackBufferBinding = new int[1];
    private final int[] lastUnpackAlignment = new int[1];
    private final int[] lastUnpackRowLength = new int[1];
    private final int[] lastUnpackSkipPixels = new int[1];
    private final int[] lastUnpackSkipRows = new int[1];
    private final int[] lastPackSwapBytes = new int[1];
    private final int[] lastPackLsbFirst = new int[1];
    private final int[] lastPackRowLength = new int[1];
    private final int[] lastPackImageHeight = new int[1];
    private final int[] lastPackSkipPixels = new int[1];
    private final int[] lastPackSkipRows = new int[1];
    private final int[] lastPackSkipImages = new int[1];
    private final int[] lastPackAlignment = new int[1];
    private final int[] lastUnpackSwapBytes = new int[1];
    private final int[] lastUnpackLsbFirst = new int[1];
    private final int[] lastUnpackImageHeight = new int[1];
    private final int[] lastUnpackSkipImages = new int[1];

    private boolean lastEnableBlend = false;
    private boolean lastEnableCullFace = false;
    private boolean lastEnableDepthTest = false;
    private boolean lastEnableStencilTest = false;
    private boolean lastEnableScissorTest = false;
    private boolean lastEnablePrimitiveRestart = false;
    private boolean lastDepthMask;
}