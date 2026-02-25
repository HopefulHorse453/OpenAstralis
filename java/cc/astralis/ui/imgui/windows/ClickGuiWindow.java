package cc.astralis.ui.imgui.windows;

import cc.astralis.module.impl.visual.ClickGuiModule;
import cc.astralis.property.properties.*;
import cc.astralis.skija.SkijaManager;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.Property;
import cc.astralis.property.properties.body.BodyProperty;
import cc.astralis.ui.cloud.CloudConfigComponent;
import cc.astralis.ui.imgui.ImGuiImpl;
import cc.astralis.util.io.StringUtil;
import cc.astralis.util.render.ColorUtil;
import cc.astralis.util.render.ImguiUtil;
import org.lwjgl.glfw.GLFW;

public class ClickGuiWindow extends Screen implements IAccess {
    private final ImString searchText = new ImString(500), configName = new ImString(500);
    private boolean shouldSetKey = false, showRotationWindow = false, showFileExplorer = false, showFontLoader;

    private FileExplorerWindow fileExplorerWindow;
    private BodyProperty selectedBodyProperty;

    private Category category;
    private Module module;
    private String currentTab = "";

    public ClickGuiWindow() {
        super(Component.nullToEmpty("Click Gui Window"));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGuiModule = Astralis.getInstance().getModuleManager().getModule(ClickGuiModule.class);

        ImGuiImpl.draw(io -> {
            ImGui.setNextWindowSize(1300, 600);

            ImGui.begin("Click Gui", ImGuiWindowFlags.NoResize);

            ImVec2 pos = ImGui.getWindowPos();
            ImVec2 size = ImGui.getWindowSize();

            if (ImGui.beginTabBar("Categories")) {
                for (Category category1 : Category.values()) {
                    if (ImGui.beginTabItem(category1.getName() + "##tab")) {
                        currentTab = category1.getName();
                        category = category1;
                        ImGui.endTabItem();
                    }
                }

                if (ImGui.beginTabItem("Configs")) {
                    currentTab = "Configs";
                    category = null;

                    ImguiUtil.setDarkMode(false, 3, clickGuiModule.imguiAlpha.getProperty().intValue());
                    CloudConfigComponent.getInstance().render(mouseX, mouseY);

                    ImGui.endTabItem();
                }

                ImGui.endTabBar();
            }

            if (!currentTab.equals("Configs") && category != null) {
                ImGui.columns(2);
                ImGui.setColumnWidth(0, 175);

                ImGui.beginChild("ModulesList", 0, 0, false, ImGuiWindowFlags.NoBackground);
                ImguiUtil.setDarkMode(false, 6, clickGuiModule.imguiAlpha.getProperty().intValue());

                for (Module module : Astralis.getInstance().getModuleManager().getModulesFromCategory(category)) {
                    ImGui.pushID(module.getName());

                    boolean isSelected = this.module == module;
                    ImGui.beginDisabled(isSelected);

                    float width = ImGui.getContentRegionAvail().x;
                    if (ImGui.button(module.getName(), width, 25)) {
                        this.module = module;
                    }

                    ImGui.endDisabled();

                    if (ImGui.isItemHovered()) {
                        ImGui.setTooltip(module.getDesc());
                    }

                    ImGui.popID();
                }

                ImGui.endChild();

                if (module != null) {
                    ImGui.nextColumn();

                    ImGui.beginChild("ModuleOptions", 0, 0, true, ImGuiWindowFlags.NoBackground);
                    ImguiUtil.setDarkMode(false, 3, clickGuiModule.imguiAlpha.getProperty().intValue());

                    if (ImGui.checkbox("Toggle", module.isToggled()))
                        module.toggle();

                    ImGui.sameLine();

                    if (ImGui.checkbox("Hidden", module.isHidden()))
                        module.setHidden(!module.isHidden());

                    ImGui.dummy(0, 1);
                    ImGui.separator();
                    ImGui.dummy(0, 1);

                    if (!module.getPropertyList().isEmpty()) {
                        for (Property<?> property : module.getPropertyList()) {
                            if (!property.getVisible().get()) continue;

                            ImGui.pushID(property.getName());

                            if (property instanceof BooleanProperty booleanProperty) {
                                if (ImGui.checkbox(property.getName(), booleanProperty.getProperty())) {
                                    booleanProperty.setProperty(!booleanProperty.getProperty());
                                }
                            }

                            if (property instanceof NumberProperty numberProperty) {
                                float[] value = numberProperty.getProperty().getData();
                                String label = "##" + numberProperty.getName();
                                String displayFormat = numberProperty.getName() + " %.3f";

                                if (ImGui.sliderFloat(label, value, numberProperty.getMin(), numberProperty.getMax(), displayFormat)) {
                                    numberProperty.setCalculatedValue(value[0], true);
                                }

                                if (ImGui.isItemEdited()) {
                                    numberProperty.setCalculatedValue(value[0], false);
                                }

                                numberProperty.getProperty().getData()[0] = numberProperty.getProperty().floatValue();
                            }

                            if (property instanceof TextProperty textProperty) {
                                ImGui.text(textProperty.getProperty());
                            }

                            if (property instanceof ColorProperty colorProperty) {
                                float[] color = ColorUtil.toGLColor(colorProperty.getProperty().getRGB());

                                if (ImGui.colorEdit4(property.getName(), color, ImGuiColorEditFlags.AlphaBar)) {
                                    colorProperty.setProperty(ColorUtil.toColor(color));
                                }
                            }

                            if (property instanceof InputProperty inputProperty) {
                                if (ImGui.inputTextWithHint(property.getName(), "You May Type Whatever You Wish Here <3", inputProperty.getImString(), ImGuiInputTextFlags.None)) {
                                    inputProperty.setProperty(inputProperty.getImString().get());
                                }
                            }

                            if (property instanceof ModeProperty modeProperty) {
                                if (ImGui.beginCombo(modeProperty.getName(), modeProperty.getProperty())) {
                                    ImGui.inputTextWithHint("##" + modeProperty.getProperty(), "Search For Modes.", searchText, ImGuiInputTextFlags.None);
                                    String search = searchText.get().toLowerCase();

                                    for (String mode : modeProperty.getModes()) {
                                        if (search.isEmpty() || mode.toLowerCase().contains(search)) {
                                            if (ImGui.selectable(mode)) {
                                                modeProperty.setProperty(mode);
                                                searchText.set(new ImString(500));
                                            }
                                        }
                                    }

                                    ImGui.endCombo();
                                }
                            }

                            if (property instanceof BooleanListProperty boolListProperty) {
                                if (ImGui.collapsingHeader(boolListProperty.getName())) {
                                    ImGui.pushID(boolListProperty.getName());

                                    ImGui.inputTextWithHint("##" + boolListProperty.getName() + "_search", "Search entries...", searchText, ImGuiInputTextFlags.None);
                                    String search = searchText.get().toLowerCase();

                                    ImGui.separator();

                                    int index = 0;
                                    for (var entry : boolListProperty.getEntries().values()) {
                                        String entryName = entry.name();
                                        if (search.isEmpty() || entryName.toLowerCase().contains(search)) {
                                            boolean currentValue = entry.value();
                                            if (ImGui.checkbox(entryName, currentValue)) {
                                                boolListProperty.setValueToEntry(entryName, !currentValue);
                                            }

                                            if (index % 2 == 0)
                                                ImGui.sameLine();
                                        }

                                        index++;
                                    }

                                    ImGui.popID();
                                    ImGui.spacing();
                                }
                            }

                            if (property instanceof ClassModeProperty classModeProperty) {
                                SubModule currentMode = classModeProperty.getProperty();
                                String currentName = currentMode != null ? currentMode.getFormatedName() : "None";

                                if (ImGui.beginCombo(classModeProperty.getName(), currentName)) {
                                    ImGui.inputTextWithHint("##search", "Search modes...", searchText);
                                    String search = searchText.get().toLowerCase();

                                    for (SubModule mode : classModeProperty.getClassModes().values()) {
                                        String modeName = mode.getFormatedName();

                                        if (search.isEmpty() || modeName.toLowerCase().contains(search)) {
                                            boolean isSelected = mode == currentMode;

                                            if (ImGui.selectable(modeName, isSelected)) {
                                                if (!isSelected) {
                                                    classModeProperty.setProperty(mode);

                                                    if (currentMode != null) {
                                                        currentMode.setSelected(false);
                                                    }
                                                    mode.setSelected(true);

                                                    searchText.set("");
                                                }
                                            }
                                        }
                                    }
                                    ImGui.endCombo();
                                }
                            }

                            if (property instanceof FileProperty fileProperty) {
                                if (!showFileExplorer) {
                                    ImGui.text("Current Path: " + fileProperty.getProperty());

                                    if (ImGui.button("Choose Path")) {
                                        showFileExplorer = true;
                                        fileExplorerWindow = new FileExplorerWindow(System.getProperty("user.home"), fileProperty.getExtensionFilter());
                                    }
                                } else {
                                    if (ImGui.button("Cancel Selection")) {
                                        showFileExplorer = false;
                                        fileExplorerWindow.reset();
                                    }

                                    fileExplorerWindow.showInline();

                                    if (fileExplorerWindow.isShouldClose()) {
                                        fileProperty.setProperty(fileExplorerWindow.getSelectedFile());
                                        showFileExplorer = false;
                                        fileExplorerWindow.reset();
                                    }
                                }
                            }

                            if (property instanceof BodyProperty bodyProperty) {
                                ImGui.text("Currently Aiming For The " + StringUtil.formatEnum(bodyProperty.getProperty().toString()));
                                showRotationWindow = true;
                                selectedBodyProperty = bodyProperty;
                            }

                            ImGui.popID();
                            ImGui.spacing();
                        }
                    } else {
                        ImGui.textColored(255, 165, 0, 255, "This module has no settings");
                    }

                    ImGui.endChild();

                }
            }

            ImGui.end();
        });

        SkijaManager.addCallback(() -> {
            CloudConfigComponent.getInstance().render(mouseX, mouseY);
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (CloudConfigComponent.getInstance().mouseClicked(click, doubled)) {
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (CloudConfigComponent.getInstance().mouseDragged(click, offsetX, offsetY)) {
            return true;
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (CloudConfigComponent.getInstance().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (CloudConfigComponent.getInstance().keyPressed(input)) {
            return true;
        }

        if (input.input() == GLFW.GLFW_KEY_ESCAPE && !shouldSetKey) {
            mc.setScreen(null);
        }

        if (shouldSetKey) {
            module.setKeyCode(input.input() == GLFW.GLFW_KEY_ESCAPE ? 0 : input.input());
            shouldSetKey = false;
        }

        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (CloudConfigComponent.getInstance().charTyped(input)) {
            return true;
        }

        return super.charTyped(input);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}