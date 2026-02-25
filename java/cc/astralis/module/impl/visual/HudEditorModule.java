package cc.astralis.module.impl.visual;

import cc.astralis.ui.screens.client.HudEditorScreen;
import cc.astralis.module.Category;
import cc.astralis.module.Module;

public class HudEditorModule extends Module {
    private final HudEditorScreen hudEditorScreen;

    public HudEditorModule() {
        super(Category.VISUAL);
        hudEditorScreen = new HudEditorScreen();
    }

    @Override
    public void onEnable() {
        mc.setScreen(hudEditorScreen);
        this.setToggled(false);
        super.onEnable();
    }
}
