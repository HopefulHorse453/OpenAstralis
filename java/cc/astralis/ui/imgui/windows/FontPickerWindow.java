package cc.astralis.ui.imgui.windows;

import cc.astralis.util.io.FileUtil;
import imgui.ImGui;
import lombok.Getter;

import java.awt.*;

@Getter
public class FontPickerWindow {
    private String path = "None";

    public void showInline() {
        if (ImGui.beginPopup("Font Picker")) {
            for (FileUtil.FontEntry fontEntry : FileUtil.getAllInstalledFonts()) {
                if (ImGui.button(fontEntry.name())) {
                    path = fontEntry.path();
                    ImGui.closeCurrentPopup();
                }
            }

            ImGui.endPopup();
        }
    }
}

