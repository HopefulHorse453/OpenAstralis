package cc.astralis;

import cc.astralis.commands.CommandManager;
import cc.astralis.component.ComponentManager;
import cc.astralis.config.ConfigManager;
import cc.astralis.config.impl.AltConfig;
import cc.astralis.config.impl.DraggableConfig;
import cc.astralis.event.EventManager;
import cc.astralis.friends.FriendManager;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.ModuleManager;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.protection.AuthScreen;
import cc.astralis.protection.ProtectedLaunch;
import cc.astralis.protection.auth.Client;
import cc.astralis.media.MediaPlayerAccessor;
import cc.astralis.skija.SkijaImpl;
import club.serenityutils.clientprofile.ClientProfileMeta;
import club.serenityutils.clientprofile.ClientProfileType;
import club.serenityutils.clientprofile.api.IClientProfileMeta;
import club.serenityutils.packets.PacketManager;
import cc.astralis.ui.animations.AnimationManager;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Astralis implements IAccess {
    @Getter
    private static final Astralis instance = new Astralis();
    public static final Logger LOGGER = LoggerFactory.getLogger("astralis");
    public static final String NAME = "Astralis", BY = "Developed by Kawase, Badaiim & IHasseDich";
    public static final double VERSION = 1.27;

    private final IClientProfileMeta clientProfileMeta = new ClientProfileMeta(ClientProfileType.ASTRALIS, Astralis.VERSION);

    public static String commitInfo, commitID;

    private ModuleManager moduleManager;
    private EventManager eventManager;
    private ComponentManager componentManager;
    private AnimationManager animationManager;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private FriendManager friendManager;
    private AltConfig altConfig;
    private DraggableConfig draggableConfig;
    private SkijaImpl skija;

    // auth ;3
    private PacketManager packetManager;
    private Client client;
    private AuthScreen authScreen;

    public void init() {
      /*  LOGGER.info("initialized " + NAME + " With Success");*/

        File astralisDir = new File(mc.gameDirectory, "/" + NAME.toLowerCase());
        if (!astralisDir.exists()) {
            if (astralisDir.mkdirs()) {
                LOGGER.info("/astralis directory created successfully.");
            } else {
                LOGGER.error("Failed to create /astralis directory.");
            }
        }

        eventManager = new EventManager();
        animationManager = new AnimationManager();
        moduleManager = new ModuleManager();
        componentManager = new ComponentManager();
        configManager = new ConfigManager();
        commandManager = new CommandManager();
        friendManager = new FriendManager();
        skija = new SkijaImpl();

        // auth
        packetManager = new PacketManager();
        authScreen = new AuthScreen();

        altConfig = new AltConfig(false);
        draggableConfig = new DraggableConfig();

        MediaPlayerAccessor.run();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "Shutdown Thread"));

        try {
            client = new Client("wss://ws.serenityutils.club");
            ProtectedLaunch.init();
        } catch (Throwable ignored) {
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

            } catch (Throwable ignored2) {
                for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                    --l;
                }
            }
        }
    }

    public void shutdown() {
        //Astralis.LOGGER.error("shut");
        altConfig.writeConfig();
        configManager.writeDefaultConfig();
        draggableConfig.writeConfig();
    }

    public Color getFirstColor() {
        return getModuleManager().getModule(HudModule.class).firstColor.getProperty();
    }

    public Color getSecondColor() {
        return getModuleManager().getModule(HudModule.class).secondColor.getProperty();
    }
}