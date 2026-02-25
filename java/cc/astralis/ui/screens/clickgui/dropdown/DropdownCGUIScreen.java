package cc.astralis.ui.screens.clickgui.dropdown;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.Category;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.ui.screens.clickgui.dropdown.impl.TabComponent;
import cc.astralis.ui.cloud.CloudConfigComponent;
import cc.astralis.skija.SkijaManager;
import cc.astralis.skija.utils.SkijaUtil;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class DropdownCGUIScreen extends Screen implements IAccess {
    private final List<TabComponent> tabComponentList = new ArrayList<>();
    private final List<ClickAnimation> clickAnimations = new ArrayList<>();

    private final Animation popInAnimation = new Animation(Easing.EASE_IN_BACK, 500);

    private static final long ANIMATION_DURATION = 800;
    private static final float MAX_RADIUS = 10f;

    public DropdownCGUIScreen() {
        super(Component.nullToEmpty("Dropdown"));

        int tabX = 50, tabY = 50;
        for (Category category : Category.values()) {
            tabComponentList.add(new TabComponent(category, tabX, tabY, 129, 20));
            tabX += 140;
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    private void addClickAnimation(double mouseX, double mouseY) {
        clickAnimations.add(new ClickAnimation((float) mouseX, (float) mouseY));
        if (clickAnimations.size() > 8) {
            clickAnimations.removeFirst();
        }
    }

    private void updateAndRenderClickAnimations() {
        Iterator<ClickAnimation> iterator = clickAnimations.iterator();
        while (iterator.hasNext()) {
            ClickAnimation animation = iterator.next();
            animation.update();
            if (animation.isFinished()) {
                iterator.remove();
            } else {
                animation.render();
            }
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        SkijaManager.addCallback(() -> {
         /*   SkijaUtil.push();
            SkijaUtil.scale((float) popInAnimation.getValue(), (float) popInAnimation.getValue());*/
            tabComponentList.forEach(component -> component.render(mouseX, mouseY));
            updateAndRenderClickAnimations();
            CloudConfigComponent.getInstance().render(mouseX, mouseY);
           /* SkijaUtil.pop();*/
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (CloudConfigComponent.getInstance().mouseClicked(click, doubled)) {
            return true;
        }

        addClickAnimation(click.x(), click.y());
        tabComponentList.forEach(component -> component.click(click.x(), click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        tabComponentList.forEach(component -> component.release(click.x(), click.y(), click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (CloudConfigComponent.getInstance().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        for (TabComponent tab : tabComponentList) {
            if (tab.isHovered(mouseX, mouseY))
                tab.scroll(verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (CloudConfigComponent.getInstance().keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (CloudConfigComponent.getInstance().charTyped(input)) {
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (CloudConfigComponent.getInstance().mouseDragged(click, offsetX, offsetY)) {
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public void onClose() {
        Astralis.getInstance().getEventManager().unregister(this);
        super.onClose();
    }

    @Override
    public void init() {
        this.popInAnimation.reset();
        this.popInAnimation.setStartPoint(0);
        this.popInAnimation.setEndPoint(1);
        this.popInAnimation.setValue(0);
    }

    private static class ClickAnimation {
        private final float x, y;
        private final Animation radiusAnimation;
        private final Animation alphaAnimation;
        private final Color color;
        private final Color secondaryColor;

        public ClickAnimation(float x, float y) {
            this.x = x;
            this.y = y;
            this.color = Astralis.getInstance().getFirstColor();
            this.secondaryColor = Astralis.getInstance().getSecondColor();

            this.radiusAnimation = new Animation(Easing.EASE_OUT_BACK, ANIMATION_DURATION);
            this.alphaAnimation = new Animation(Easing.EASE_IN_OUT_QUAD, ANIMATION_DURATION);

            this.radiusAnimation.setStartPoint(0);
            this.radiusAnimation.setEndPoint(MAX_RADIUS);
            this.radiusAnimation.setValue(0);

            this.alphaAnimation.setStartPoint(1.0);
            this.alphaAnimation.setEndPoint(0.0);
            this.alphaAnimation.setValue(1.0);
        }

        public void update() {
            radiusAnimation.run(radiusAnimation.getEndPoint());
            alphaAnimation.run(alphaAnimation.getEndPoint());
        }

        public boolean isFinished() {
            return radiusAnimation.isFinished() && alphaAnimation.isFinished();
        }

        public void render() {
            float r = (float) radiusAnimation.getValue();
            float a = (float) alphaAnimation.getValue();

            if (a <= 0.01f || r <= 0) return;

            SkijaUtil.drawCircleOutline(x, y, r, 2.5f,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (a * 120)));

            if (r > 10) {
                SkijaUtil.drawCircleOutline(x, y, r * 0.6f, 1.5f,
                        new Color(secondaryColor.getRed(), secondaryColor.getGreen(), secondaryColor.getBlue(), (int) (a * 80)));
            }

            if (r < MAX_RADIUS * 0.3f) {
                SkijaUtil.drawCircle(x, y, Math.max(1, r * 0.1f),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (a * 200)));
            }

            if (r > MAX_RADIUS * 0.7f) {
                SkijaUtil.drawCircleOutline(x, y, r * 1.2f, 1.0f,
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (a * 40)));
            }
        }
    }
}