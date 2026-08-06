package eleeter.elfontlib.emoji.watch;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DebouncedRebuildScheduler
{
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r ->
    {
        Thread t = new Thread(r, "Emoji-Watch-Scheduler");
        t.setDaemon(true);
        return t;
    });

    private final AtomicReference<ScheduledFuture<?>> pendingStatic = new AtomicReference<>();
    private final AtomicReference<ScheduledFuture<?>> pendingAnimated = new AtomicReference<>();

    private final long debounceMs;

    public DebouncedRebuildScheduler(long debounceMs)
    {
        this.debounceMs = debounceMs;
    }

    public void scheduleStatic(Runnable task)
    {
        schedule(pendingStatic, task);
    }

    public void scheduleAnimated(Runnable task)
    {
        schedule(pendingAnimated, task);
    }

    private void schedule(AtomicReference<ScheduledFuture<?>> ref, Runnable task)
    {
        ScheduledFuture<?> prev = ref.get();
        if (prev != null) prev.cancel(false);

        ScheduledFuture<?> next = scheduler.schedule(() ->
        {
            ref.set(null);
            task.run();
        }, debounceMs, TimeUnit.MILLISECONDS);

        ref.set(next);
    }

    public void shutdown()
    {
        scheduler.shutdownNow();
    }
}
