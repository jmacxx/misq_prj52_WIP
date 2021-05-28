package com.misq.utils;


import com.google.common.util.concurrent.MoreExecutors;

import java.time.Duration;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import java.lang.reflect.InvocationTargetException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * Defines which thread is used as user thread. The user thread is the the main thread in the single threaded context.
 * For JavaFX it is usually the Platform::RunLater executor, for a headless application it is any single threaded
 * executor.
 * Additionally sets a timer class so JavaFX and headless applications can set different timers (UITimer for JavaFX
 * otherwise we use the default FrameRateTimer).
 * <p>
 * Provides also methods for delayed and periodic executions.
 */
@Slf4j
public class UserThread {
    private static Class<? extends Timer> timerClass;
    @Getter
    @Setter
    private static Executor executor;

    public static void setTimerClass(Class<? extends Timer> timerClass) {
        UserThread.timerClass = timerClass;
    }

    static {
        // If not defined we use same thread as caller thread
        executor = MoreExecutors.directExecutor();
        timerClass = FrameRateTimer.class;
    }

    public static void execute(Runnable command) {
        UserThread.executor.execute(command);
    }

    // Prefer FxTimer if a delay is needed in a JavaFx class (gui module)
    public static Timer runAfterRandomDelay(Runnable runnable, long minDelayInSec, long maxDelayInSec) {
        return UserThread.runAfterRandomDelay(runnable, minDelayInSec, maxDelayInSec, TimeUnit.SECONDS);
    }

    @SuppressWarnings("WeakerAccess")
    public static Timer runAfterRandomDelay(Runnable runnable, long minDelay, long maxDelay, TimeUnit timeUnit) {
        return UserThread.runAfter(runnable, new Random().nextInt((int) (maxDelay - minDelay)) + minDelay, timeUnit);
    }

    public static Timer runAfter(Runnable runnable, long delayInSec) {
        return UserThread.runAfter(runnable, delayInSec, TimeUnit.SECONDS);
    }

    public static Timer runAfter(Runnable runnable, long delay, TimeUnit timeUnit) {
        return getTimer().runLater(Duration.ofMillis(timeUnit.toMillis(delay)), runnable);
    }

    public static Timer runPeriodically(Runnable runnable, long intervalInSec) {
        return UserThread.runPeriodically(runnable, intervalInSec, TimeUnit.SECONDS);
    }

    public static Timer runPeriodically(Runnable runnable, long interval, TimeUnit timeUnit) {
        return getTimer().runPeriodically(Duration.ofMillis(timeUnit.toMillis(interval)), runnable);
    }

    private static Timer getTimer() {
        try {
            return timerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            String message = "Could not instantiate timer bsTimerClass=" + timerClass;
            System.out.println(message + e);
            throw new RuntimeException(message);
        }
    }
}
