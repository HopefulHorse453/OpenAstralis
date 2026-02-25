package cc.astralis.util.io;

import cc.astralis.Astralis;
import cc.astralis.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class KeyBoardUtil {
    private static final Map<Integer, String> KEY_NAME_MAP = new HashMap<>();
    private static final Map<String, Integer> KEY_CODE_MAP = new HashMap<>();

    private KeyBoardUtil() {}

    static {
        for (String s: "0=48;1=49;2=50;3=51;4=52;5=53;6=54;7=55;8=56;9=57;A=65;B=66;C=67;D=68;E=69;F=70;G=71;H=72;I=73;J=74;K=75;L=76;M=77;N=78;O=79;P=80;Q=81;R=82;S=83;T=84;U=85;V=86;W=87;X=88;Y=89;Z=90;F1=290;F2=291;F3=292;F4=293;F5=294;F6=295;F7=296;F8=297;F9=298;F10=299;F11=300;F12=301;NUMLOCK=282;NUMPAD0=320;NUMPAD1=321;NUMPAD2=322;NUMPAD3=323;NUMPAD4=324;NUMPAD5=325;NUMPAD6=326;NUMPAD7=327;NUMPAD8=328;NUMPAD9=329;NUMPADCOMMA=330;NUMPADENTER=335;NUMPADEQUALS=336;DOWN=264;LEFT=263;RIGHT=262;UP=265;ADD=334;APOSTROPHE=39;BACKSLASH=92;COMMA=44;EQUALS=61;GRAVE=96;LBRACKET=91;MINUS=45;MULTIPLY=332;PERIOD=46;RBRACKET=93;SEMICOLON=59;SLASH=47;SPACE=32;TAB=258;LALT=342;LCONTROL=341;LSHIFT=340;LWIN=343;RALT=346;RCONTROL=345;RSHIFT=344;RWIN=347;RETURN=257;BACKSPACE=259;DELETE=261;END=269;HOME=268;INSERT=260;PAGEDOWN=267;PAGEUP=266;CAPSLOCK=280;PAUSE=284;SCROLLLOCK=281;PRINTSCREEN=283".split(";")) {
            String[] entry = s.split("=");
            String name = entry[0];
            int key = Integer.parseInt(entry[1]);
            KEY_NAME_MAP.put(key, name);
            KEY_CODE_MAP.put(name, key);
        }
    }

    public static void keyPress(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return;

        Astralis.getInstance().getModuleManager().getObjects().stream().filter(module -> module.getKeyCode() == key).forEach(Module::toggle);
    }

    public static String getKeyName(int key) {
        return KEY_NAME_MAP.get(key);
    }

    public static int getKeyCode(String name) {
        if (name == null) return GLFW.GLFW_KEY_UNKNOWN;
        Integer code = KEY_CODE_MAP.get(name.toUpperCase(Locale.ROOT));
        return code != null ? code : GLFW.GLFW_KEY_UNKNOWN;
    }
}
