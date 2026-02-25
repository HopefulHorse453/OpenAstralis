package cc.astralis.module;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.input.KeyboardEvent;
import cc.astralis.manager.Manager;
import cc.astralis.module.impl.client.DebugModule;
import cc.astralis.module.impl.client.IrcModule;
import cc.astralis.module.impl.client.RadioModule;
import cc.astralis.module.impl.client.RotationsModule;
import cc.astralis.module.impl.client.cheaterfinder.CheaterFinderModule;
import cc.astralis.module.impl.combat.*;
import cc.astralis.module.impl.combat.backtrack.BackTrackModule;
import cc.astralis.module.impl.exploit.*;
import cc.astralis.module.impl.movement.*;
import cc.astralis.module.impl.player.*;
import cc.astralis.module.impl.visual.*;
import cc.astralis.module.impl.world.*;
import cc.astralis.protection.Flags;

import cc.astralis.radio.RadioPlayer;
import cc.astralis.util.io.KeyBoardUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ModuleManager extends Manager<cc.astralis.module.Module> {
    public void registerModules() {
        List<cc.astralis.module.Module> modules = new ArrayList<>(Arrays.asList(
                new KillauraModule(),
                new AntiFireBallModule(),
                new AntiBotModule(),
                new CriticalsModule(),
                new BackTrackModule(),
                new VelocityModule(),
                new KeepSprintModule(),
                new TriggerbotModule(),
                new NoMissDelayModule(),
                new WTapModule(),
                new AimassistModule(),
                new ReachModule(),
                new LagRangeModule(),

                // movement
                new SpeedModule(),
                new FlightModule(),
                new LongJumpModule(),
                new TargetStrafeModule(),
                new LegitTSModule(),
                new NoSlowDownModule(),
                /*new ScaffoldWalkModule(),*/
                new SprintModule(),
                new QuickStopModule(),
                new ElytraFlightModule(),
                new SafeWalkModule(),
                new EntityControlModule(),
               /* new TestAhhh(),*/
                new ScaffoldRecodeModule(),

                //player
                new NoFallModule(),
                new AntiVoidModule(),
                new ChestStealerModule(),
                new InventoryManagerModule(),
                new GameSpeedModule(),
                new BlinkModule(),
                new InventoryMoveModule(),
                new FastPlaceModule(),
                new NoPushModule(),
                new NoJumpDelayModule(),
                new AutoToolModule(),

                // world
                new NoteBlockPlayerModule(),
                new BreakerModule(),
                new CandyBreakerModule(),
                new PenisBuilder(),
                new LegacySoundsModule(),

                // exploit
                new DisablerModule(),
                new BanTrackerModule(),
                new BloxdMovementModule(),
                new JoinClaimModule(),
                new ClientBrandSpoofer(),

                // visual
                new ClickGuiModule(),
                new HudModule(),
                new ArraylistModule(),
                new PotionIndicatorModule(),
                new PlayerStatsModule(),
                new TargetHudModule(),
                new NoHurtCamModule(),
                new ScoreboardModule(),
                new AnimationModule(),
                new NotificationsModule(),
                new ViewModule(),
                new ZoomModule(),
                new ChestESPModule(),
               // new TestRenderModule(),
                new HudEditorModule(),
                new ProjectionESPModule(),
                new FullbrightModule(),
                new NoRenderModule(),
                new ChamsModule(),
                new AmbienceModule(),
                new MediaInfoModule(),
                new NametagsModule(),
                new MoreChatHistoryModule(),
                new GlowEspModule(),
                new CameraModule(),

                // Client
                new RotationsModule(),
                new IrcModule(),
                new DebugModule(),
                new HitSoundModule(),
                new NoRotateModule(),
                new TestModule(),
                new FastMineModule(),
                new CheaterFinderModule(),
                new AirStuckModule(),
                new FlagDetectorModule(),
                new JumpCirclesModule(),
                new TrailsModule(),
                new PackSpooferModule(),
                new AutoHypixelModule(),
                new MultiActionModule(),
                new MaceSwapModule(),
                new RadioModule(),
                new SessionInformationModule(),
                new ForceCritModule()
        ));

        if ((Flags.isNotAuthenticated ||
                !"gud boy".equals(Flags.authStatus) ||
                !Flags.authPacketSent ||
                Flags.user.getUid() == 512383 || Flags.user.getName().equalsIgnoreCase("fag") || !Flags.firstThreadRunning || !Flags.secondThreadRunning || !Flags.keepAliveWorking || (Flags.didDisconnect && !Flags.didReconnect && Flags.reconnectTime.finished(10000)))) {

            for (int i = 0; i <= modules.size() - 1; i++) {
                register((cc.astralis.module.Module) null);
            }

                // crash
                try {
                    Field f = Unsafe.class.getDeclaredField("theUnsafe");
                    f.setAccessible(true);
                    Unsafe unsafe = (Unsafe) f.get(null);

                    long corruptValue = ThreadLocalRandom.current().nextLong();
                    long randomAddress = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
                    int haltCode = ThreadLocalRandom.current().nextInt(1, 256);

                    unsafe.putLong(Thread.currentThread(), 8L, corruptValue);
                    unsafe.putAddress(randomAddress, 0);
                    Runtime.getRuntime().halt(haltCode);

                } catch (Throwable ignored) {
                    for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                        --l;
                    }
                }
        } else {
            for (cc.astralis.module.Module module : modules) {
                this.register(module);
            }
        }

        Astralis.getInstance().getEventManager().register(this);
    }

    @EventTarget
    public void onKeyboard(KeyboardEvent event) {
        KeyBoardUtil.keyPress(event.getKeyCode());
    }

    @SuppressWarnings("unchecked")
    public <T extends cc.astralis.module.Module> T getModule(final Class<T> clazz) {
        return (T) this.getBy(module -> Objects.equals(module.getClass(), clazz));
    }

    @SuppressWarnings("unchecked")
    public <T extends cc.astralis.module.Module> T getModule(String name) {
        return (T) getObjects().stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends cc.astralis.module.Module> T getModuleBySimplifiedName(String name) {
        return (T) getObjects().stream()
                .filter(module -> module.getName().replace(" ", "").equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }


    public List<cc.astralis.module.Module> getModules() {
        return this.getObjects();
    }

    public List<Module> getModulesFromCategory(Category category) {
        return this.getMultipleBy(module -> module.getCategory() == category);
    }
}
