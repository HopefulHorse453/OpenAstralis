package cc.astralis.ui.progress;

import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressBar {
    private final String id;
    private float progress = 0f;
    private float displayed = 0f;
    private final Animation popAnimation = new Animation(Easing.EASE_OUT_BACK, 300);

    private boolean completed = false;
    private boolean removing = false;

    public ProgressBar(String id) {
        this.id = id;
        this.progress = 0f;
    }

    public void setProgress(float value) {
        this.progress = Math.max(0f, Math.min(1f, value));
    }
}
