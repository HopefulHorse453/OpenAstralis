package cc.astralis.ui.imgui.windows;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import cc.astralis.Astralis;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.Objects;

@AllArgsConstructor
public class ConfigManagerWindow {
    private ImString configName;

    public void showInline() {
        if (ImGui.beginChild("ConfigFilesChild", 200, 200, true)) {
            File configDir = Astralis.getInstance().getConfigManager().directory;
            if (configDir.exists() && configDir.isDirectory()) {
                for (File file : Objects.requireNonNull(configDir.listFiles())) {
                    if (file.isFile() && file.toString().endsWith(".astralis")) {
                        String configName = file.getName().replace(".astralis", "");
                        if (ImGui.button(configName + "##" + configName)) {
                            Astralis.getInstance().getConfigManager().loadConfig(configName);
                        }

                        ImGui.sameLine();
                        if (ImGui.button("Delete##" + configName)) {
                            Astralis.getInstance().getConfigManager().deleteConfig(configName);
                        }
                    }
                }
            }
            ImGui.endChild();
        }

        ImGui.inputTextWithHint("##ConfigName", "Config Name", this.configName, ImGuiInputTextFlags.None);
        
        ImGui.sameLine();
        if (ImGui.button("Save")) {
            Astralis.getInstance().getConfigManager().saveConfig(configName.get());
            Astralis.getInstance().getConfigManager().addConfigsToHashMap();
        }

        ImGui.sameLine();
        if (ImGui.button("Load")) {
            Astralis.getInstance().getConfigManager().loadConfig(configName.get());
        }
    }
}

