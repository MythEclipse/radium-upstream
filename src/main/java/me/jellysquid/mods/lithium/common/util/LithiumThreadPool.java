package me.jellysquid.mods.lithium.common.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LithiumThreadPool {
    private static final AtomicInteger ENTITY_WORKER_ID = new AtomicInteger(0);
    private static final AtomicInteger CHUNK_WORKER_ID = new AtomicInteger(0);
    private static final AtomicInteger MISC_WORKER_ID = new AtomicInteger(0);
    private static volatile boolean shuttingDown = false;

    private static final int AVAILABLE_CPUS = Runtime.getRuntime().availableProcessors();
    private static final int WORKER_BUDGET = Math.max(2, AVAILABLE_CPUS - 2);
    private static final int ENTITY_PARALLELISM = Math.max(1, WORKER_BUDGET / 2);
    private static final int CHUNK_PARALLELISM = Math.max(1, WORKER_BUDGET - ENTITY_PARALLELISM);
    private static final int MISC_PARALLELISM = Math.max(1, WORKER_BUDGET / 4);

    // Private constructor to prevent instantiation
    private LithiumThreadPool() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static ForkJoinPool createPool(String prefix, AtomicInteger counter, int parallelism) {
        return new ForkJoinPool(
            parallelism,
            pool -> {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName(prefix + "-" + counter.getAndIncrement());
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    System.err.println("Uncaught exception in thread " + t.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                });
                return thread;
            },
            (t, e) -> {
                // Global exception handler for the pool
                System.err.println("Uncaught exception in " + prefix + " pool: " + e.getMessage());
                e.printStackTrace();
            },
            true // Async mode for better parallelism
        );
    }

    public static final ForkJoinPool ENTITY_POOL = createPool("Lithium-Entity", ENTITY_WORKER_ID, ENTITY_PARALLELISM);
    public static final ForkJoinPool CHUNK_POOL = createPool("Lithium-Chunk", CHUNK_WORKER_ID, CHUNK_PARALLELISM);
    public static final ForkJoinPool MISC_POOL = createPool("Lithium-Misc", MISC_WORKER_ID, MISC_PARALLELISM);

    static {
        // Start deadlock detector
        DeadlockDetector.start();
        
        // Register shutdown hook to gracefully shutdown the pool
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shuttingDown = true;
            DeadlockDetector.stop();
            shutdownPool(ENTITY_POOL);
            shutdownPool(CHUNK_POOL);
            shutdownPool(MISC_POOL);
        }, "Lithium-ThreadPool-Shutdown"));
    }

    private static void shutdownPool(ForkJoinPool pool) {
        try {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static ForkJoinPool getEntityPool() {
        return ENTITY_POOL;
    }

    public static ForkJoinPool getChunkPool() {
        return CHUNK_POOL;
    }

    public static ForkJoinPool getMiscPool() {
        return MISC_POOL;
    }

    public static boolean isShuttingDown() {
        return shuttingDown;
    }
}
