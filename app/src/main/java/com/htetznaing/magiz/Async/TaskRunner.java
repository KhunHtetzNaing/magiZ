package com.htetznaing.magiz.Async;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onTaskCompleted(R result);
        void onTaskFailure(String error);
    }

    public <R> void executeAsync(final Callable<R> callable, final Callback<R> callback) {
        executor.execute(() -> {
            final R result;
            try {
                result = callable.call();
                handler.post(() -> callback.onTaskCompleted(result));
            } catch (Exception e) {
                handler.post(() -> callback.onTaskFailure(e.getMessage()));
            }
        });
    }
}
