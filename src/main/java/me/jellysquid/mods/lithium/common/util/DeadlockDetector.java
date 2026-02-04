package me.jellysquid.mods.lithium.common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Detects deadlocks in the application and logs thread dumps.
 */
public class DeadlockDetector {
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static ScheduledExecutorService watchdog;
    private static volatile boolean enabled = true;
    
    private DeadlockDetector() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void start() {
        if (watchdog != null) {
            return; // Already started
        }
        
        watchdog = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "Lithium-Deadlock-Detector");
            t.setDaemon(true);
            return t;
        });
        
        // Check for deadlocks every 5 seconds
        watchdog.scheduleAtFixedRate(() -> {
            if (!enabled) return;
            
            try {
                long[] deadlockedThreads = threadBean.findDeadlockedThreads();
                if (deadlockedThreads != null && deadlockedThreads.length > 0) {
                    System.err.println("========================================");
                    System.err.println("DEADLOCK DETECTED!");
                    System.err.println("========================================");
                    System.err.println("Number of deadlocked threads: " + deadlockedThreads.length);
                    
                    ThreadInfo[] threadInfos = threadBean.getThreadInfo(deadlockedThreads, true, true);
                    for (ThreadInfo info : threadInfos) {
                        if (info != null) {
                            printThreadInfo(info);
                        }
                    }
                    
                    System.err.println("========================================");
                    System.err.println("FULL THREAD DUMP:");
                    System.err.println("========================================");
                    dumpAllThreads();
                }
            } catch (Exception e) {
                System.err.println("Error in deadlock detector: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        System.out.println("[Lithium] Deadlock detector started");
    }
    
    public static void stop() {
        if (watchdog != null) {
            watchdog.shutdownNow();
            watchdog = null;
            System.out.println("[Lithium] Deadlock detector stopped");
        }
    }
    
    public static void enable() {
        enabled = true;
    }
    
    public static void disable() {
        enabled = false;
    }
    
    public static void dumpAllThreads() {
        ThreadInfo[] threads = threadBean.dumpAllThreads(true, true);
        for (ThreadInfo info : threads) {
            printThreadInfo(info);
        }
    }
    
    private static void printThreadInfo(ThreadInfo info) {
        System.err.println("\nThread: " + info.getThreadName() + 
                          " (ID=" + info.getThreadId() + 
                          ", State=" + info.getThreadState() + ")");
        
        if (info.getLockName() != null) {
            System.err.println("  Waiting on lock: " + info.getLockName());
        }
        
        if (info.getLockOwnerName() != null) {
            System.err.println("  Owned by: " + info.getLockOwnerName() + 
                              " (ID=" + info.getLockOwnerId() + ")");
        }
        
        System.err.println("  Stack trace:");
        for (StackTraceElement element : info.getStackTrace()) {
            System.err.println("    at " + element);
        }
        
        if (info.getLockedMonitors().length > 0) {
            System.err.println("  Locked monitors: " + info.getLockedMonitors().length);
        }
        
        if (info.getLockedSynchronizers().length > 0) {
            System.err.println("  Locked synchronizers: " + info.getLockedSynchronizers().length);
        }
    }
}
