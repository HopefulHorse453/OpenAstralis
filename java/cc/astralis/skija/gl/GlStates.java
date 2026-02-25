package cc.astralis.skija.gl;

import cc.astralis.skija.utils.SkijaHelperUtil;

import java.util.Stack;

public class GlStates {
    private static final Stack<GlState> GL_STATES = new Stack<>();

    public static void push() {
        GlStates.GL_STATES.push(new GlState(SkijaHelperUtil.getGLVersion()).push());
    }

    public static void pop() {
        if (!GlStates.GL_STATES.empty()) {
            GlStates.GL_STATES.pop().pop();
        }
    }
}
