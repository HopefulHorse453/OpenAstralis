package cc.astralis.module.impl.visual;

import cc.astralis.module.Category;
import cc.astralis.module.Module;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends Module {

    private float zoomFov = 0f;
    private float targetFov = 0f;
    private float originalFov = 0f;

    public ZoomModule() {
        super(Category.VISUAL);
    }

    @Override
    public void onEnable() {
        this.originalFov = mc.options.fov().get();
        this.zoomFov = originalFov;
        this.targetFov = originalFov;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        zoomFov = originalFov;
        targetFov = originalFov;
        super.onDisable();
    }


    public float getModifiedFov(float currentFov) {
        if (mc.screen != null) return currentFov;

        boolean zooming = GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;

        if (originalFov == 0) {
            originalFov = currentFov;
            zoomFov = currentFov;
        }

        if (zooming) {
            targetFov = 30.0f;
            float lerpSpeed = 0.05f;
            zoomFov += (targetFov - zoomFov) * lerpSpeed;
            return zoomFov;
        } else {
            zoomFov = currentFov;
            return currentFov;
        }
    }
}
