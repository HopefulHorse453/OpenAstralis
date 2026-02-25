package cc.astralis.ui.cloud;

import cc.astralis.Astralis;
import cc.astralis.interfaces.Fonts;
import cc.astralis.protection.Flags;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.util.client.ConfigUtil;
import cc.astralis.util.io.FileUtil;
import cc.astralis.skija.utils.SkijaUtil;
import club.serenityutils.cloudconfigs.CloudConfig;
import club.serenityutils.cloudconfigs.api.ICloudConfig;
import club.serenityutils.packets.impl.cloud.AddCloudConfigPacket;
import club.serenityutils.packets.impl.cloud.FetchCloudConfigsPacket;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudConfigComponent {
    private final int rectWidth = 700, rectHeight = 400, buttonSize = 30, margin = 15;
    private float windowX, windowY;
    private boolean dragging = false;
    @Getter
    private boolean visible = false;
    private boolean showUploadDialog = false;
    private int dragOffsetX, dragOffsetY, scrollOffset = 0, activeInputField = 0;
    private String configName = "", configDescription = "";
    private final Animation windowAnimation = new Animation(Easing.EASE_IN_OUT_SINE, 500);
    private final Animation dialogAnimation = new Animation(Easing.EASE_IN_OUT_SINE, 300);

    private final Minecraft client = Minecraft.getInstance();

    // ermmm tbh i should've just created a thing in the uhhh main class but lazy.
    private static CloudConfigComponent INSTANCE;

    public static CloudConfigComponent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CloudConfigComponent();
        }

        return INSTANCE;
    }

    private CloudConfigComponent() {
        windowX = client.getWindow().getGuiScaledWidth();
        windowY = (client.getWindow().getGuiScaledHeight() - rectHeight) / 2;
    }

    public void render(int mouseX, int mouseY) {
        windowAnimation.run(visible ? 1 : 0);
        dialogAnimation.run(showUploadDialog ? 1 : 0);

        float animProgress = (float) windowAnimation.getValue();
        float targetX = (float) (client.getWindow().getGuiScaledWidth() - rectWidth) / 2;
        windowX = client.getWindow().getGuiScaledWidth() + (targetX - client.getWindow().getGuiScaledWidth()) * animProgress;
        windowY = (float) (client.getWindow().getGuiScaledHeight() - rectHeight) / 2;

        float buttonX = client.getWindow().getGuiScaledWidth() - buttonSize - margin;
        float buttonY = client.getWindow().getGuiScaledHeight() - buttonSize - margin;
        boolean hovered = mouseX >= buttonX && mouseY >= buttonY && mouseX <= buttonX + buttonSize && mouseY <= buttonY + buttonSize;

        SkijaUtil.roundedRectangle(buttonX, buttonY, buttonSize, buttonSize, 8, hovered ? Astralis.getInstance().getFirstColor() : new Color(30, 30, 30));
        SkijaUtil.line(buttonX + 10, buttonY + 15, buttonX + 20, buttonY + 15, 2, Color.WHITE);
        if (visible) {
            SkijaUtil.line(buttonX + 15, buttonY + 10, buttonX + 15, buttonY + 20, 2, Color.WHITE);
        } else {
            SkijaUtil.line(buttonX + 15, buttonY + 10, buttonX + 15, buttonY + 20, 2, Color.WHITE);
        }

        if (animProgress > 0.01f || !windowAnimation.isFinished()) {
            SkijaUtil.roundedRectangle(windowX, windowY, rectWidth, rectHeight, 8, new Color(18, 18, 18, 250));
            SkijaUtil.roundedRectangleGradientVarying(windowX, windowY, rectWidth, 12, 8, 8, 0, 0, Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(), false);

            final float headerX = windowX + 20, headerY = windowY + 25;
            Fonts.product_bold_11.drawString("Cloud Configurations", headerX, headerY, Color.WHITE);
            Fonts.product_regular_8.drawString("Browse and download community configurations", headerX, headerY + 15, new Color(160, 160, 160));

            final float uploadButtonWidth = 70, uploadButtonHeight = 20;
            float uploadButtonX = windowX + rectWidth - uploadButtonWidth - 20, uploadButtonY = headerY - 2;

            SkijaUtil.roundedRectangle(uploadButtonX, uploadButtonY, uploadButtonWidth, uploadButtonHeight, 4, new Color(Astralis.getInstance().getFirstColor().getRed(), Astralis.getInstance().getFirstColor().getGreen(), Astralis.getInstance().getFirstColor().getBlue(), 200));
            Fonts.product_regular_9.drawString("Upload", uploadButtonX + 20, uploadButtonY + 4, Color.WHITE);

            SkijaUtil.roundedRectangle(headerX, headerY + 30, rectWidth - 40, 1, 0.5f, new Color(40, 40, 40));

            final float startX = windowX + 25, startY = windowY + 70, cardWidth = 320, cardHeight = 90, cardSpacing = 10;
            SkijaUtil.scissored(windowX + 20, windowY + 70, rectWidth - 40, rectHeight - 100, () -> {
                for (int i = 0; i < Flags.cloudConfigs.size(); i++) {
                    ICloudConfig config = Flags.cloudConfigs.get(i);
                    float cardX = startX + (i % 2) * (cardWidth + cardSpacing), cardY = startY + (i / 2) * (cardHeight + cardSpacing) - scrollOffset;
                    renderConfigCard(config, cardX, cardY, cardWidth, cardHeight);
                }
            });

            if (dialogAnimation.getValue() > 0.01) {
                renderUploadDialogAnimated();
            }
        }
    }

    private void renderUploadDialogAnimated() {
        float dialogWidth = 400, dialogHeight = 300;
        float dialogX = (client.getWindow().getGuiScaledWidth() - dialogWidth) / 2;
        float dialogY = (client.getWindow().getGuiScaledHeight() - dialogHeight) / 2;

        float scale = (float) dialogAnimation.getValue(), alpha = scale;
        float currentWidth = dialogWidth * scale, currentHeight = dialogHeight * scale;
        float currentX = dialogX + (dialogWidth - currentWidth) / 2, currentY = dialogY + (dialogHeight - currentHeight) / 2;

        SkijaUtil.roundedRectangle(0, 0, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), 0, new Color(0, 0, 0, (int) (100 * alpha)));
        SkijaUtil.roundedRectangle(currentX, currentY, currentWidth, currentHeight, 8 * scale, new Color(25, 25, 25));
        SkijaUtil.roundedRectangleGradientVarying(currentX, currentY, currentWidth, 12 * scale, 8 * scale, 8 * scale, 0, 0, Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(), false);

        Fonts.product_bold_11.drawString("Upload Configuration", currentX + 20 * scale, currentY + 25 * scale, Color.WHITE);

        float fieldY = currentY + 60 * scale;
        Fonts.product_regular_9.drawString("Configuration Name:", currentX + 20 * scale, fieldY, Color.WHITE);
        fieldY += 20 * scale;

        Color nameFieldColor = (activeInputField == 0) ? Astralis.getInstance().getFirstColor() : new Color(50, 50, 50);
        SkijaUtil.roundedRectangle(currentX + 20 * scale, fieldY, (dialogWidth - 40) * scale, 25 * scale, 4 * scale, nameFieldColor);
        SkijaUtil.roundedRectangle(currentX + 22 * scale, fieldY + 2 * scale, (dialogWidth - 44) * scale, 21 * scale, 3 * scale, new Color(35, 35, 35));
        Fonts.product_regular_9.drawString(configName + (activeInputField == 0 ? "_" : ""), currentX + 28 * scale, fieldY + 8 * scale, Color.WHITE);

        fieldY += 40 * scale;
        Fonts.product_regular_9.drawString("Description:", currentX + 20 * scale, fieldY, Color.WHITE);
        fieldY += 20 * scale;

        Color descFieldColor = (activeInputField == 1) ? Astralis.getInstance().getFirstColor() : new Color(50, 50, 50);
        SkijaUtil.roundedRectangle(currentX + 20 * scale, fieldY, (dialogWidth - 40) * scale, 60 * scale, 4 * scale, descFieldColor);
        SkijaUtil.roundedRectangle(currentX + 22 * scale, fieldY + 2 * scale, (dialogWidth - 44) * scale, 56 * scale, 3 * scale, new Color(35, 35, 35));
        renderWrappedTextInDialog(configDescription + (activeInputField == 1 ? "_" : ""), currentX + 28 * scale, fieldY + 8 * scale, (dialogWidth - 56) * scale, 12 * scale, Color.WHITE);

        fieldY += 80 * scale;
        final float buttonWidth = 80 * scale, buttonHeight = 25 * scale;
        float cancelButtonX = currentX + currentWidth - 180 * scale, uploadButtonX = currentX + currentWidth - 90 * scale;

        SkijaUtil.roundedRectangle(cancelButtonX, fieldY, buttonWidth, buttonHeight, 4 * scale, new Color(60, 60, 60));
        Fonts.product_regular_10.drawString("Cancel", cancelButtonX + 24 * scale, fieldY + 7 * scale, Color.WHITE);

        SkijaUtil.roundedRectangle(uploadButtonX, fieldY, buttonWidth, buttonHeight, 4 * scale, new Color(Astralis.getInstance().getFirstColor().getRed(), Astralis.getInstance().getFirstColor().getGreen(), Astralis.getInstance().getFirstColor().getBlue(), 200));
        Fonts.product_regular_10.drawString("Upload", uploadButtonX + 24 * scale, fieldY + 7 * scale, Color.WHITE);
    }

    private void renderConfigCard(ICloudConfig config, float x, float y, float width, float height) {
        SkijaUtil.roundedRectangle(x + 1, y + 1, width, height, 6, new Color(0, 0, 0, 40));
        SkijaUtil.roundedRectangle(x, y, width, height, 6, new Color(28, 28, 28));
        SkijaUtil.roundedRectangle(x, y, 3, height, 6, Astralis.getInstance().getFirstColor());

        float textX = x + 12, textY = y + 12;
        Fonts.product_bold_11.drawString(config.getName(), textX, textY, Color.WHITE);

        textY += 16;
        Fonts.product_regular_10.drawString(config.getUser().getName(), textX, textY, Astralis.getInstance().getFirstColor());

        final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        String dateStr = " • " + sdf.format(new Date(config.getUploadDate()));
        Fonts.product_regular_8.drawString(dateStr, textX + Fonts.product_regular_10.getStringWidth(config.getUser().getName()), textY, new Color(120, 120, 120));

        textY += 14;
        renderWrappedText(config.getDescription().isEmpty() || config.getDescription().isBlank() ? "No Description Provided" : config.getDescription(), textX, textY, width - 24, 10, Color.LIGHT_GRAY);

        float bottomY = y + height - 20;
        Fonts.product_regular_8.drawString("#" + config.getConfigId(), textX, bottomY, new Color(100, 100, 100));

        final float buttonWidth = 60, buttonHeight = 16;
        float buttonX = x + width - buttonWidth - 12, buttonY = bottomY - 4;
        SkijaUtil.roundedRectangle(buttonX, buttonY, buttonWidth, buttonHeight, 3, new Color(Astralis.getInstance().getFirstColor().getRed(), Astralis.getInstance().getFirstColor().getGreen(), Astralis.getInstance().getFirstColor().getBlue(), 180));
        Fonts.product_regular_8.drawString("Download", buttonX + 11.5f, buttonY + 3, Color.WHITE);
    }

    private void renderWrappedText(String text, float x, float y, float maxWidth, float lineHeight, Color color) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        float currentY = y;
        int lineCount = 0;

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (Fonts.product_regular_8.getStringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                String lineText = currentLine.toString();
                if (lineCount == 1 && !words[words.length - 1].equals(word)) {
                    lineText = lineText.substring(0, Math.min(lineText.length(), 30)) + "...";
                }
                Fonts.product_regular_8.drawString(lineText, x, currentY, color);
                lineCount++;
                if (lineCount >= 2) break;
                currentLine = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                currentLine.append(word).append(" ");
            }
        }

        if (lineCount < 2 && currentLine.length() > 0) {
            Fonts.product_regular_8.drawString(currentLine.toString().trim(), x, currentY, color);
        }
    }

    private void renderWrappedTextInDialog(String text, float x, float y, float maxWidth, float lineHeight, Color color) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        float currentY = y;
        int lineCount = 0;

        for (String word : words) {
            String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
            if (Fonts.product_regular_9.getStringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                Fonts.product_regular_9.drawString(currentLine.toString(), x, currentY, color);
                lineCount++;
                if (lineCount >= 4) break;
                currentLine = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                currentLine.append(word).append(" ");
            }
        }

        if (lineCount < 4 && !currentLine.isEmpty()) {
            Fonts.product_regular_9.drawString(currentLine.toString().trim(), x, currentY, color);
        }
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            float buttonX = client.getWindow().getGuiScaledWidth() - buttonSize - margin;
            float buttonY = client.getWindow().getGuiScaledHeight() - buttonSize - margin;

            if (mouseX >= buttonX && mouseY >= buttonY && mouseX <= buttonX + buttonSize && mouseY <= buttonY + buttonSize) {
                visible = !visible;
                if (visible) {
                    new FetchCloudConfigsPacket().sendPacket(Astralis.getInstance().getClient());
                } else {
                    showUploadDialog = false;
                }
                return true;
            }

            if (!visible) return false;

            // upload button
            final float uploadButtonWidth = 70, uploadButtonHeight = 20;
            float uploadButtonX = windowX + rectWidth - uploadButtonWidth - 20, uploadButtonY = windowY + 23;
            if (mouseX >= uploadButtonX && mouseX <= uploadButtonX + uploadButtonWidth && mouseY >= uploadButtonY && mouseY <= uploadButtonY + uploadButtonHeight) {
                showUploadDialog = true;
                configName = "";
                configDescription = "";
                activeInputField = 0;
                return true;
            }

            if (mouseX >= windowX && mouseX <= windowX + rectWidth && mouseY >= windowY && mouseY <= windowY + 40) {
                dragging = true;
                dragOffsetX = (int) mouseX - (int) windowX;
                dragOffsetY = (int) mouseY - (int) windowY;
                return true;
            }

            final float startX = windowX + 25, startY = windowY + 70, cardWidth = 320, cardHeight = 90, cardSpacing = 10;
            for (int i = 0; i < Flags.cloudConfigs.size(); i++) {
                ICloudConfig config = Flags.cloudConfigs.get(i);
                float cardX = startX + (i % 2) * (cardWidth + cardSpacing);
                float cardY = startY + (i / 2) * (cardHeight + cardSpacing) - scrollOffset;

                final float buttonWidth = 60;
                final float downloadButtonX = cardX + cardWidth - buttonWidth - 12;
                final float downloadButtonY = cardY + cardHeight - 20 - 4;

                final File cfgFile = new File(
                        Minecraft.getInstance().gameDirectory,
                        "/" + Astralis.NAME.toLowerCase() + "/Configs"
                );

                if (mouseX >= downloadButtonX && mouseX <= downloadButtonX + buttonWidth &&
                        mouseY >= downloadButtonY && mouseY <= downloadButtonY + 16) {
                    FileUtil.writeJsonToFile(config.getConfigData(), cfgFile.getAbsolutePath() + "/" + config.getName() + ".astralis");
                    return true;
                }
            }

            if (showUploadDialog && dialogAnimation.getValue() > 0.01) {
                return handleUploadDialogClick(mouseX, mouseY);
            }
          }
        return false;
    }

    private boolean handleUploadDialogClick(double mouseX, double mouseY) {
        float dialogWidth = 400, dialogHeight = 300;
        float dialogX = (client.getWindow().getGuiScaledWidth() - dialogWidth) / 2;
        float dialogY = (client.getWindow().getGuiScaledHeight() - dialogHeight) / 2;

        float scale = (float) dialogAnimation.getValue();
        float currentWidth = dialogWidth * scale, currentHeight = dialogHeight * scale;
        float currentX = dialogX + (dialogWidth - currentWidth) / 2, currentY = dialogY + (dialogHeight - currentHeight) / 2;

        if (mouseX < currentX || mouseX > currentX + currentWidth || mouseY < currentY || mouseY > currentY + currentHeight) {
            showUploadDialog = false;
            return true;
        }

        float fieldY = currentY + 80 * scale;
        if (mouseX >= currentX + 20 * scale && mouseX <= currentX + (dialogWidth - 20) * scale && mouseY >= fieldY && mouseY <= fieldY + 25 * scale) {
            activeInputField = 0;
            return true;
        }

        fieldY += 60 * scale;
        if (mouseX >= currentX + 20 * scale && mouseX <= currentX + (dialogWidth - 20) * scale && mouseY >= fieldY && mouseY <= fieldY + 60 * scale) {
            activeInputField = 1;
            return true;
        }

        fieldY += 80 * scale;
        float cancelButtonX = currentX + currentWidth - 180 * scale, uploadButtonX = currentX + currentWidth - 90 * scale, buttonHeight = 25 * scale;

        if (mouseX >= cancelButtonX && mouseX <= cancelButtonX + 80 * scale && mouseY >= fieldY && mouseY <= fieldY + buttonHeight) {
            showUploadDialog = false;
            return true;
        }

        final float startX = windowX + 25, startY = windowY + 70, cardWidth = 320, cardHeight = 90, cardSpacing = 10;
        for (int i = 0; i < Flags.cloudConfigs.size(); i++) {
            ICloudConfig config = Flags.cloudConfigs.get(i);
            float cardX = startX + (i % 2) * (cardWidth + cardSpacing);
            float cardY = startY + (i / 2) * (cardHeight + cardSpacing) - scrollOffset;

            final float buttonWidth = 60, buttonHeight2 = 16;
            float downloadButtonX = cardX + cardWidth - buttonWidth - 12;
            float downloadButtonY = cardY + cardHeight - 20 - 4;

            if (mouseX >= downloadButtonX && mouseX <= downloadButtonX + buttonWidth &&
                    mouseY >= downloadButtonY && mouseY <= downloadButtonY + buttonHeight2) {

                System.out.println("Download clicked for config: " + config.getName());
                // here you could trigger your download logic
                return true;
            }
        }

        if (mouseX >= uploadButtonX && mouseX <= uploadButtonX + 80 * scale && mouseY >= fieldY && mouseY <= fieldY + buttonHeight) {
            new AddCloudConfigPacket(new CloudConfig(
                    configName,
                    configDescription,
                    Flags.user.getUid(), Flags.user.getName(),
                    ConfigUtil.getCurrentConfig()
            )).sendPacket(Astralis.getInstance().getClient());
            new FetchCloudConfigsPacket().sendPacket(Astralis.getInstance().getClient());
            showUploadDialog = false;
            return true;
        }

        return true;
    }

    public boolean keyPressed(KeyEvent keyInput) {
        final int keyCode = keyInput.key();

        if (showUploadDialog) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                showUploadDialog = false;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_TAB) {
                activeInputField = activeInputField == 0 ? 1 : 0;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                new AddCloudConfigPacket(new CloudConfig(
                        configName,
                        configDescription,
                        Flags.user.getUid(), Flags.user.getName(),
                        ConfigUtil.getCurrentConfig()
                )).sendPacket(Astralis.getInstance().getClient());
                new FetchCloudConfigsPacket().sendPacket(Astralis.getInstance().getClient());
                showUploadDialog = false;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (activeInputField == 0 && !configName.isEmpty()) {
                    configName = configName.substring(0, configName.length() - 1);
                } else if (activeInputField == 1 && !configDescription.isEmpty()) {
                    configDescription = configDescription.substring(0, configDescription.length() - 1);
                }
                return true;
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(CharacterEvent input) {
        if (showUploadDialog) {
            if (input.isAllowedChatCharacter()) {
                String chr = input.codepointAsString();

                if (activeInputField == 0 && configName.length() < 50) {
                    configName += chr;
                } else if (activeInputField == 1 && configDescription.length() < 500) {
                    configDescription += chr;
                }
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (dragging && visible && !showUploadDialog) {
            windowX = (float) (click.x() - dragOffsetX);
            windowY = (float) (click.y() - dragOffsetY);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (visible && !showUploadDialog && mouseX >= windowX && mouseX <= windowX + rectWidth && mouseY >= windowY + 70 && mouseY <= windowY + rectHeight) {
            scrollOffset -= (int) (verticalAmount * 20);
            scrollOffset = Math.max(0, scrollOffset);
            return true;
        }
        return false;
    }

    public void toggle() {
        visible = !visible;
        if (visible) {
            new FetchCloudConfigsPacket().sendPacket(Astralis.getInstance().getClient());
        } else {
            showUploadDialog = false;
        }
    }
}