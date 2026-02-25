package cc.astralis.protection;

import cc.astralis.util.math.TimeUtil;
import club.serenityutils.cloudconfigs.api.ICloudConfig;
import club.serenityutils.user.User;
import club.serenityutils.user.api.IUser;

import java.util.ArrayList;
import java.util.List;

public class Flags {
    public static boolean isNotAuthenticated = true; // false if the user authed true if he didn't.
    public static String authStatus = null; // equals to gud boy if the user authed otherwise null
    public static boolean authGuiShown = false; // true if the auth gui was shown
    public static boolean authPacketSent = false; // true if the auth packet was sent
    public static String sessionToken = "none";

    public static boolean keepAliveWorking = true; // false if keep alive packets have got fcuekd

    public static final TimeUtil reconnectTime = new TimeUtil();
    public static boolean didDisconnect = false, didReconnect = false;

    public static IUser user = new User("fag", 512383); // if the uid is equal to 512383 we didnt auth otherwise its gona be the user's uid and if the name is fag we didn't auth.

    public static boolean firstThreadRunning = false, secondThreadRunning = false;
    public static boolean didSendFetchModuleInfoPacket = false, gotModuleInfo = false;

    // this isnt rlly auth releated but wtv.
    public static List<ICloudConfig> cloudConfigs = new ArrayList<>();
}