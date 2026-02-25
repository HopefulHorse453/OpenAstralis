package cc.astralis.util.io;

import lombok.NonNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtil {
    private static final ScheduledExecutorService RUNNABLE_POOL = Executors.newScheduledThreadPool(3, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "Multithreading Thread " + counter.incrementAndGet());
        }
    });

    public static ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "Multithreading Thread " + counter.incrementAndGet());
        }
    });

    public static void schedule(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        RUNNABLE_POOL.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
        return ThreadUtil.RUNNABLE_POOL.schedule(r, delay, unit);
    }

    public static int getTotal() {
        return ((ThreadPoolExecutor) ThreadUtil.POOL).getActiveCount();
    }

    public static void runAsync(Runnable runnable) {
        POOL.execute(runnable);
    }
}
