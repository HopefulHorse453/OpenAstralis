package cc.astralis.module.impl.visual;

import cc.astralis.Astralis;
import cc.astralis.drag.Draggable;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.types.Priority;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.interfaces.access.IDrag;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.*;
import cc.astralis.protection.Flags;
import cc.astralis.util.gif.GifFrameLoadingUtil;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.render.RenderUtil;
import com.mojang.blaze3d.platform.Window;
import cc.astralis.skija.utils.SkijaUtil;
import cc.astralis.skija.io.ImageUtil;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;

public class HudModule extends Module {
    public final BooleanProperty bloom = new BooleanProperty("Bloom", true);
    public final NumberProperty bloomRadius = new NumberProperty("Bloom Radius", 7, 1, 30, 1);
    public final BooleanProperty blur = new BooleanProperty("Blur", true);
    public final NumberProperty blurRadius = new NumberProperty("Blur Radius", 4, 1, 10, 1);
    public final ModeProperty fontMode = new ModeProperty("Font Mode", "Sf-Ui", "Sf-Ui",
            "Product Sans Regular", "Product Sans Bold",
            "JetBrains-Regular", "JetBrains-Bold",
            "Poppins-Regular", "Poppins-Bold",
            "Sf-Regular", "Sf-Bold", "Tahoma",
            "mojangles-extended"
    );
    public final ModeProperty colorMode = new ModeProperty("Color Mode", "Client", "Client", "Rainbow");
    private final ModeProperty watermarkMode = new ModeProperty("Water Mark Mode", "Text", "Text", "Astralis", "Adjust", "Gif", "Sense", "None");
    private final BooleanProperty alternativeColors = new BooleanProperty("Alternative Color", true);
    private final InputProperty clientName = new InputProperty("Client Name", "Astralis");
    private final NumberProperty speed = new NumberProperty("Speed", 100, 1, 300, 1);
    private final FileProperty fileProperty = new FileProperty("Gif", "astralis/dancing-random.gif");
    public ColorProperty firstColor = new ColorProperty("First Color", new Color(78, 73, 165)),
            secondColor = new ColorProperty("Second Color", new Color(-16764448));
    public final NumberProperty backgroundAlpha = new NumberProperty("Background Alpha", 150, 1, 255, 5);

    private final Draggable draggable;

    private final TimeUtil timeUtil = new TimeUtil();
    private static String CLOSE_RESOURCE = null;
    private final Map<String, io.github.humbleui.skija.Image> cachedImages = new HashMap<>();

    private int currentFrame = 0;

    private String[] cachedPaths = null;

    public HudModule() {
        super(Category.VISUAL);
        registerProperties(bloom, bloomRadius, blur, blurRadius,
                fontMode, colorMode, watermarkMode,
                alternativeColors.setVisible(() -> watermarkMode.is("Sense")),
                speed.setVisible(() -> watermarkMode.is("PSD") || watermarkMode.is("Gif")),
                fileProperty.setVisible(() -> watermarkMode.is("Gif")), clientName, firstColor, secondColor, backgroundAlpha
        );

        draggable = new Draggable("Watermark", 5, 5, 100, 50);
        IDrag.draggables.add(draggable);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @EventTarget(value = Priority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        setSuffix(watermarkMode.getProperty());
        UIFont uiFont = FontManager.getFont(fontMode.getProperty(), 9);

        Window window = mc.getWindow();

        if (!(mc.screen instanceof ChatScreen)) {
            String info = "User: " + Flags.user.getName() + " (" + Flags.user.getUid() + ") BPS: " + MoveUtil.getBPS();
            RenderUtil.drawGradientString(product_regular_10, info, 5, window.getGuiScaledHeight() - (product_regular_10.getStringHeight(info) + 5),
                    Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(), true
            );
        }

        switch (watermarkMode.getProperty()) {
            case "Text": {
                final UIFont watermarkUIFont = FontManager.getFont("Product Sans Bold", 20);
                final String watermarkText = clientName.getProperty();
                RenderUtil.drawGradientString(watermarkUIFont, watermarkText, 3, 3, Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(), false);
                break;
            }

            case "Gif":
                try {
                    String[] frames = GifFrameLoadingUtil.cacheGifFrames(fileProperty.getProperty());
                    cachedPaths = new String[frames.length];

                    for (int i = 0; i < frames.length; i++) {
                        String correctedPath = mc.gameDirectory.getAbsolutePath() + "/" + frames[i];
                        cachedPaths[i] = correctedPath;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (timeUtil.finished(speed.getProperty().longValue())) {
                    currentFrame++;

                    if (currentFrame > cachedPaths.length - 1) {
                        currentFrame = 0;
                    }

                    CLOSE_RESOURCE = cachedPaths[currentFrame];
                    if (!cachedImages.containsKey(CLOSE_RESOURCE))
                        cachedImages.put(CLOSE_RESOURCE, ImageUtil.loadFromAbsolutePath(CLOSE_RESOURCE));

                    timeUtil.reset();
                }

                SkijaUtil.renderImage(draggable.getX(), draggable.getY(), 80, 80, cachedImages.get(CLOSE_RESOURCE), null);
                break;
            case "Adjust":
                RenderUtil.drawGradientString(tahoma_bold_10, "A", draggable.getX(), draggable.getY(), 0,
                        Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(), true);

                tahoma_regular_10.drawStringWithShadow("djust", draggable.getX() + tahoma_bold_10.getStringWidth("A"), draggable.getY(),
                        Color.white);
                break;
            case "Astralis":
                String info = "Astralis " + ChatFormatting.WHITE + Astralis.VERSION + " - " + (mc.isLocalServer() ? "none" : censorIfLiquidProxy(mc.getCurrentServer().ip)) + " - FPS " + mc.getFps() + " - " + Flags.user.getName();
                float infoWidth = uiFont.getStringWidth(info) + 6;

                SkijaUtil.roundedRectangleGradientOutline(
                        draggable.getX(), draggable.getY(),
                        infoWidth, 18, 3,
                        Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(),
                        false, 1.5f, new Color(0, 0, 0, 100)
                );

                uiFont.drawString(info, draggable.getX() + 3.5f, draggable.getY() + 3, Astralis.getInstance().getFirstColor());
                break;
            case "Sense":
                String clientText = "§f" + clientName.getProperty();
                String senseText = "Sense";
                String restText = " §f- (§7" + Astralis.VERSION + "§f) - " + (mc.isLocalServer() ? "none" : censorIfLiquidProxy(mc.getCurrentServer().ip));

                float clientWidth = uiFont.getStringWidth(clientText);
                float senseWidth = uiFont.getStringWidth(senseText);
                float restWidth = uiFont.getStringWidth(restText);
                float totalWidth = clientWidth + senseWidth + restWidth + 4.5f;

                Color outColor = !alternativeColors.getProperty() ? new Color(59, 57, 57).darker() : new Color(59, 57, 57);
                Color backGroundColor = alternativeColors.getProperty() ? new Color(33, 29, 29) : new Color(23, 23, 23);
                float x = 4, y = 3;

                // First rectangle (outline)
                SkijaUtil.drawRectPoint4(x, y, totalWidth + 7, 18, outColor);
                SkijaUtil.drawRectPoint4(x + 1, y + 1, totalWidth + 6, 17, backGroundColor);
                SkijaUtil.drawRectPoint4(x + 1.5f, y + 1.5f, totalWidth + 5.5f, 16.5f, outColor);
                SkijaUtil.drawRectPoint4(x + 2.5f, y + 2.5f, totalWidth + 4.5f, 15.5f, backGroundColor);

                // Bottom gradient line
                SkijaUtil.drawRectPoint4HorizontalGradient(x + 2.5f, 15, totalWidth + 4.5f, 15, firstColor.getProperty(), secondColor.getProperty());

                uiFont.drawString(clientText, 8, 4.5f, Astralis.getInstance().getFirstColor());
                RenderUtil.drawGradientString(uiFont, senseText, 8 + clientWidth, 4.5f, 0f, Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(), false);
                uiFont.drawString(restText, 8 + clientWidth + senseWidth, 4.5f, Astralis.getInstance().getFirstColor());
                break;
        }

    }

    public static String censorIfLiquidProxy(String serverIp) {
        String domain = "liquidproxy.net";
        if (serverIp == null) return null;

        if (serverIp.endsWith(domain)) {
            int index = serverIp.lastIndexOf(domain);
            return "***" + serverIp.substring(index);
        }

        return serverIp;
    }

    public static Color getRainbow(final long ms, final int offset, final float saturation, final float brightness) {
        return new Color(Color.HSBtoRGB(((System.currentTimeMillis() + offset) % ms) / (float) ms, saturation, brightness));
    }

    public static Color getRainbow(final int offset, final float saturation, final float brightness) {
        return getRainbow(4500, offset, saturation, brightness);
    }

    public int getPing() {
        if (mc.getConnection() != null) {
            PlayerInfo playerListEntry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (playerListEntry != null) {
                return playerListEntry.getLatency();
            }
        }

        return -1;
    }
}