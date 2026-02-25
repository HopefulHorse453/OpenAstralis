package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.ScoreboardModule;
import cc.astralis.ui.screens.clickgui.astralis.AstralisClickGUI;
import cc.astralis.ui.screens.clickgui.dropdown.DropdownCGUIScreen;
import cc.astralis.ui.imgui.windows.ClickGuiWindow;
import cc.astralis.util.Data;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cc.astralis.interfaces.IAccess.mc;

@Mixin(Gui.class)
public class MixinInGameHud {
    @Inject(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderScoreboardSidebarHook(GuiGraphics context, Objective objective, CallbackInfo ci) {
        ScoreboardModule scoreboardModule = Astralis.getInstance().getModuleManager().getModule(ScoreboardModule.class);
        if (scoreboardModule.isToggled() && scoreboardModule.noScoreBoard.getProperty()) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0), index = 4)
    private int changeHeaderColor(int color) {
        ScoreboardModule scoreboardModule = Astralis.getInstance().getModuleManager().getModule(ScoreboardModule.class);
        if (scoreboardModule.isToggled() && scoreboardModule.changeColor.getProperty()) {
            return scoreboardModule.headColor.getProperty().getRGB();
        }

        return color;
    }

    @ModifyArg(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1), index = 4)
    private int changeBodyColor(int color) {
        ScoreboardModule scoreboardModule = Astralis.getInstance().getModuleManager().getModule(ScoreboardModule.class);
        if (scoreboardModule.isToggled() && scoreboardModule.changeColor.getProperty()) {
            return scoreboardModule.bodyColor.getProperty().getRGB();
        }

        return color;
    }

    // ts is so ahhh.
    @Inject(
            method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V",
            at = @At("HEAD")
    )
    private void onRenderScoreboardSidebar(GuiGraphics context, Objective objective, CallbackInfo ci) {
        if (objective == null) return;

        var mc = Minecraft.getInstance();
        var scoreboard = objective.getScoreboard();
        var tr = mc.font;

        var entries = scoreboard.listPlayerScores(objective)
                .stream()
                .filter(s -> !s.isHidden())
                .limit(15)
                .toList();

        int maxWidth = tr.width(objective.getDisplayName());
        int colonWidth = tr.width(": ");

        for (var entry : entries) {
            var team = scoreboard.getPlayersTeam(entry.owner());
            var nameText = PlayerTeam.formatNameForTeam(team, entry.ownerName());
            var scoreText = entry.formatValue(objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT));
            maxWidth = Math.max(maxWidth, tr.width(nameText) + (tr.width(scoreText) > 0 ? colonWidth + tr.width(scoreText) : 0));
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int lineHeight = 9;
        int lines = entries.size();
        int height = lines * lineHeight;

        int x = screenWidth - maxWidth - 3;
        int y = screenHeight / 2 + height / 3 - lines * 9;

        Astralis.getInstance().getModuleManager().getModule(ScoreboardModule.class)
                .setBounds(new ScoreboardModule.Bounds(x, y, maxWidth, height));
    }

    @ModifyVariable(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V", at = @At(value = "STORE", ordinal = 0))
    private NumberFormat removeScore(NumberFormat numberFormat, GuiGraphics context, Objective objective) {
        ScoreboardModule scoreboardModule = Astralis.getInstance().getModuleManager().getModule(ScoreboardModule.class);
        if (scoreboardModule.isToggled() && scoreboardModule.noScore.getProperty()) {
                return BlankFormat.INSTANCE;
        }
        return numberFormat;
    }

    @ModifyExpressionValue(
            method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/scores/Objective;numberFormatOrDefault(Lnet/minecraft/network/chat/numbers/NumberFormat;)Lnet/minecraft/network/chat/numbers/NumberFormat;"
            )
    )
    private NumberFormat changeScoreColor(NumberFormat original) {
        ScoreboardModule scoreboardModule = Astralis.getInstance().getModuleManager().getModule(ScoreboardModule.class);
        if (scoreboardModule.isToggled() && scoreboardModule.changeColor.getProperty()) {
            return original == BlankFormat.INSTANCE ? original : new StyledFormat(Style.EMPTY.withColor(scoreboardModule.scoreColor.getProperty().getRGB()));
        }
        return original;
    }

    @Inject(method = "renderTabList(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void handleRenderPlayerList(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo callbackInfo) {
        /* SkijaUtil.scoped(() -> Astralis.getInstance().getEventManager().call(new Render2DEvent()));*/
        Data.drawContext = context;

        final var skia = Astralis.getInstance().getSkija();

        skia.begin();

        try {
            skia.render();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            skia.end();
        }
    }

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    private void onRenderVignetteOverlay(GuiGraphics context, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (mc.screen instanceof DropdownCGUIScreen || mc.screen instanceof AstralisClickGUI || mc.screen instanceof ClickGuiWindow) {
            ci.cancel();
        }
    }
}
