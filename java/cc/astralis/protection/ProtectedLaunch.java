package cc.astralis.protection;

import cc.astralis.Astralis;
import cc.astralis.util.io.ProtectionUtil;
import cc.astralis.util.math.TimeUtil;
import sun.misc.Unsafe;

import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class ProtectedLaunch {
    private static boolean tampering = false;
    /*   public interface User32 extends StdCallLibrary {
           User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

           boolean EnumWindows(WndEnumProc lpEnumFunc, Pointer arg);

           int GetWindowTextA(WinDef.HWND hWnd, byte[] lpString, int nMaxCount);

           boolean IsWindowVisible(WinDef.HWND hWnd);

           int GetWindowThreadProcessId(WinDef.HWND hWnd, IntByReference lpdwProcessId);
       }

       public interface WndEnumProc extends StdCallLibrary.StdCallCallback {
           boolean callback(WinDef.HWND hWnd, Pointer arg);
       }*/
    public static void init() throws Throwable {
        argsCheck();

        { // Hosts Check
            if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
                File file = new File("C:\\Windows\\System32\\drivers\\etc", "hosts");

                if (file.exists()) {
                    Scanner reader = new Scanner(file);

                    while (reader.hasNextLine()) {
                        String data = reader.nextLine().toLowerCase();

                        // todo (add actual ips)
                        if (data.contains("wss://ws.serenityutils.club") ||
                                data.contains("45.144.55.184")) {

                            Astralis.LOGGER.error("Runtime Standard Failed 0x02");

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
                            reader.close();
                            return;
                        }
                    }

                    reader.close();
                }
            }


           /* {
                new Thread(() -> {
                    TimeUtil timerUtil = new TimeUtil();
                    String[] blacklistedNames = {
                            "process hacker",
                            "system informer",
                            "wireshark",
                            "http toolkit"
                    };

                    while (true) {
                        if (timerUtil.finished(250)) {
                            User32.INSTANCE.EnumWindows((hWnd, arg) -> {
                                byte[] windowText = new byte[512];
                                User32.INSTANCE.GetWindowTextA(hWnd, windowText, 512);
                                String wText = Native.toString(windowText).trim();
                                if (!wText.isEmpty()) {
                                    for (String blacklistedName : blacklistedNames) {
                                        if (wText.toLowerCase().contains(blacklistedName)) {
                                            ProtectionUtil.crash("Runtime Standard Failed 0x03");
                                            return false;
                                        }
                                    }
                                }
                                return true;
                            }, null);

                            timerUtil.reset();
                        }
                    }
                }, "Niggers #1").start();
            }*/
        }

        if (!tampering) {
            Astralis.getInstance().getClient().start();
        }
    }

    private static void argsCheck() {
        // there is a reason for this obv.
        // basically so we can trick the compiler into not optimising this shi, and then we can run the patcher which removes
        // this return statement for the public build.
        if (System.currentTimeMillis() < 0 || "".isEmpty()) {
            return;
        }

        final String[] badFlags = {
                "-agentlib:jdwp",
                "-XBootclasspath",
                "-javaagent",
                "-Xdebug",
                "-agentlib",
                "-Xrunjdwp",
                "-Xnoagent",
                "-verbose",
                "-DproxySet",
                "-DproxyHost",
                "-DproxyPort",
                "-Djavax.net.ssl.trustStore",
                "-Djavax.net.ssl.trustStorePassword"
        };

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String jvmArgs = runtimeBean.getInputArguments().toString();

        boolean dashes = true, counter = true, detected = false;
        int i = 0;

        for (String str : badFlags) {
            if (!str.contains("-")) {
                dashes = false;
                break;
            }
            ++i;
        }

        if (i != 13) {
            counter = false;
        }

        for (String arg : badFlags) {
            if (jvmArgs.contains(arg)) {
                detected = true;
                break;
            }
        }

        if (!dashes || !counter || detected) {
            Astralis.LOGGER.error("Runtime Standards Failed 0x01");

            tampering = true;

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
        }
    }
}
