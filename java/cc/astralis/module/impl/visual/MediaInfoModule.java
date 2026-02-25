package cc.astralis.module.impl.visual;

import cc.astralis.Astralis;
import cc.astralis.drag.Draggable;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.font.FontManager;
import cc.astralis.interfaces.access.IDrag;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.media.MediaPlayerAccessor;
import cc.astralis.util.render.ColorUtil;
import cc.astralis.skija.utils.SkijaUtil;
import cc.astralis.skija.io.ImageUtil;
import dev.redstones.mediaplayerinfo.MediaInfo;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.model.geom.builders.UVPair;

public class MediaInfoModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Modern", "Modern", "Legacy");

    private final Draggable draggable;

    private io.github.humbleui.skija.Image artwork;

    public final Animation progressAnimation = new Animation(Easing.EASE_IN_OUT_QUAD, 500);
    private double lastProgressValue = 0.0;

    public MediaInfoModule() {
        super(Category.VISUAL);
        draggable = new Draggable("Media Info", 50, 50, 200, 60);
        IDrag.draggables.add(draggable);
        this.registerProperties(mode);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        this.setSuffix(mode.getProperty());

        MediaInfo info = MediaPlayerAccessor.session != null ? MediaPlayerAccessor.session.getMedia() : null;

        if (info == null) {
            return;
        }

        byte[] png = info.getArtworkPng();
        if (png != null && png.length > 0) {
            artwork = ImageUtil.loadFromStream(new ByteArrayInputStream(png));
        } else {
            artwork = null;
        }

        int x = (int) draggable.getX(), y = (int) draggable.getY();
        double calculatedProgress = info.getDuration() > 0 ? (double) info.getPosition() / info.getDuration() : 0;

        if (calculatedProgress != lastProgressValue) {
            progressAnimation.setStartPoint(progressAnimation.getValue());
            progressAnimation.setEndPoint(calculatedProgress);
            progressAnimation.reset();
            lastProgressValue = calculatedProgress;
        }

        progressAnimation.run(calculatedProgress);

        final String title = normalizeFancyText(fixMojibake(info.getTitle()));
        final String artist = normalizeFancyText(fixMojibake(info.getArtist()));
        final String timeText = formatTime(info.getPosition()) + " / " + formatTime(info.getDuration());
        final double animatedProgressRatio = progressAnimation.getValue();

        final int width = "Modern".equals(mode.getProperty()) ? 250 : 210,
                height = 60;

        Color firstColor, secondColor;

        if ("Legacy".equals(mode.getProperty())) {
            final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);

            firstColor = switch (hud.colorMode.getProperty()) {
                case "Rainbow" -> HudModule.getRainbow(3000, 0, 0.7f, 1);
                default -> ColorUtil.getAccentColor(new UVPair(0, 6), hud.firstColor.getProperty(), hud.secondColor.getProperty());
            };

            secondColor = switch (hud.colorMode.getProperty()) {
                case "Rainbow" -> HudModule.getRainbow(4000, 0, 1f, 1);
                default -> ColorUtil.getAccentColor(new UVPair(0, 6), hud.firstColor.getProperty(), hud.secondColor.getProperty());
            };

            drawLegacyMediaInfo(x, y, width, height, title, artist, timeText, animatedProgressRatio, firstColor, secondColor);
        } else {
            drawModernMediaInfo(x, y, width, height, title, artist, animatedProgressRatio);
        }
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        MediaInfo info = MediaPlayerAccessor.session != null ? MediaPlayerAccessor.session.getMedia() : null;

        if (info == null) {
            return;
        }

        final int x = (int) draggable.getX(), y = (int) draggable.getY();

        final int width = "Modern".equals(mode.getProperty()) ? 250 : 210,
                height = 60;

        switch (mode.getProperty()) {
            case "Modern" -> {
                final float cornerRadius = 16f;
                SkijaUtil.drawShaderRoundRectangle(x, y, width, height, cornerRadius);
            }
            case "Legacy" -> {
                SkijaUtil.drawShaderRectangle(x, y, width, height);
            }
        }
    }

    private void drawLegacyMediaInfo(float x, float y, int width, int height, String title,
                                     String artist, String timeText, double animatedProgressRatio,
                                     Color firstColor, Color secondColor) {
        final int padding = 3, artworkWidth = height - padding * 2;
        float startX = x + artworkWidth + padding * 2;

        HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        SkijaUtil.drawRectPoint4(x, y, x + width, y + height, new Color(30, 30, 30, hud.backgroundAlpha.getProperty().intValue()));

        final float progressBarWidth = width - ((startX - x) + padding * 2) + 4;
        final float animatedProgressWidth = (float) (progressBarWidth * animatedProgressRatio);
        final int progressBarHeight = 8;

        SkijaUtil.drawRectPoint4(startX, y + height - (progressBarHeight + padding),
                startX + progressBarWidth, y + height - padding,
                new Color(25, 25, 25, 220));

        SkijaUtil.rectangleGradient(
                startX, y + height - (progressBarHeight + padding),
                animatedProgressWidth,
                progressBarHeight,
                firstColor, secondColor, false
        );

        if (artwork != null) {
            SkijaUtil.renderRoundedImage(x + padding, y + padding, artworkWidth, artworkWidth, 3,
                    artwork, ColorUtil.withAlpha(Color.white, 255));
        } else {
            SkijaUtil.drawRectPoint4(x + padding, y + padding,
                    x + padding + artworkWidth, y + padding + artworkWidth,
                    new Color(60, 60, 60, 200));
        }

        if (title.length() > 25)
            title = title.substring(0, 25) + "...";

        FontManager.getFont("Sf-Ui", 12).drawString(title,
                startX, y + padding, Color.white);

        if (artist.length() > 30)
            artist = artist.substring(0, 30) + "...";

        FontManager.getFont("Sf-Ui", 10).drawString(
                artist, startX, y + padding + 14, new Color(180, 180, 180));

        FontManager.getFont("Sf-Ui", 9).drawString(
                timeText, startX, y + padding + 28, new Color(160, 160, 160));
    }

    private void drawModernMediaInfo(float x, float y, int width, int height, String title,
                                     String artist, double animatedProgressRatio) {
        final float padding = 10f;
        final float cornerRadius = 16f;
        final float artworkSize = height - padding * 2;
        final float artworkRadius = 12f;

        HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        SkijaUtil.roundedRectangle(x, y, width, height, cornerRadius,
                new Color(15, 15, 18, hud.backgroundAlpha.getProperty().intValue()));

       /* SkijaUtil.roundedRectangleOutline(x, y, width, height, cornerRadius,
                new Color(0, 0, 0, 0), 1.5f, new Color(255, 255, 255, 20));*/

        float contentStartX = x + artworkSize + padding * 2.5f;
        float contentWidth = width - artworkSize - padding * 3.5f;

        if (artwork != null) {
            // this kinda kills fps
            SkijaUtil.renderRoundedImage(x + padding, y + padding, artworkSize, artworkSize, artworkRadius,
                    artwork, null);
        } else {
            SkijaUtil.roundedRectangleGradient(x + padding, y + padding, artworkSize, artworkSize,
                    artworkRadius, new Color(55, 55, 65), new Color(35, 35, 45), true);
        }

        SkijaUtil.roundedRectangleOutline(x + padding, y + padding, artworkSize, artworkSize,
                artworkRadius, new Color(0, 0, 0, 0), 2f, new Color(255, 255, 255, 35));

        if (title.length() > 20)
            title = title.substring(0, 20) + "...";

        FontManager.getFont("Product Sans Bold", 14).drawString(title,
                contentStartX, y + padding, Color.WHITE);

        if (artist.length() > 24)
            artist = artist.substring(0, 24) + "...";

        FontManager.getFont("Product Sans Regular", 12).drawString(artist,
                contentStartX, y + padding + 16, new Color(170, 170, 175));

        float progressBarY = y + height - padding - 7;
        final float progressBarHeight = 8;
        final float progressBarRadius = 5;

        SkijaUtil.roundedRectangle(contentStartX, progressBarY, contentWidth,
                progressBarHeight, progressBarRadius, new Color(30, 30, 35));

        final float animatedProgressWidth = (float) (contentWidth * animatedProgressRatio);
        if (animatedProgressWidth > 0)
            SkijaUtil.roundedRectangleGradient(
                    contentStartX, progressBarY,
                    animatedProgressWidth, progressBarHeight,
                    progressBarRadius,
                    Astralis.getInstance().getFirstColor(),
                    Astralis.getInstance().getSecondColor(),
                    false
            );
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static String normalizeFancyText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder sb = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if ((c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9') ||
                    (c >= ' ' && c <= '/') ||
                    (c >= ':' && c <= '@') ||
                    (c >= '[' && c <= '`') ||
                    (c >= '{' && c <= '~')) {
                sb.append(c);
            }
            else if (c == '©' || c == '®' || c == '™' || c == '°') {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String fixMojibake(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        try {
            byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
            String utf8 = new String(bytes, StandardCharsets.UTF_8);

            if (!utf8.equals(input) && !utf8.contains("�")) {
                return utf8;
            }

            bytes = input.getBytes(StandardCharsets.ISO_8859_1);
            utf8 = new String(bytes, "Windows-1252");
            if (!utf8.equals(input) && !utf8.contains("�")) {
                return utf8;
            }
            return input;
        } catch (Exception e) {
            return input;
        }
    }
}