package eleeter.elfontlib.emoji.watch;

import eleeter.elfontlib.emoji.EmojiFont;
import eleeter.elfontlib.emoji.animated.AnimatedEmojiAtlasBuilder;
import eleeter.elfontlib.emoji.animated.AnimatedEmojiFont;
import eleeter.elfontlib.emoji.build.EmojiAtlasBuilder;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

public class EmojiHotReloadWatcher
{
    private final String staticSvgFolder;
    private final String staticCacheFolder;
    private final String animatedSourceFolder;
    private final String animatedCacheFolder;
    private final Listener listener;

    private final DebouncedRebuildScheduler scheduler = new DebouncedRebuildScheduler(300);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread watchThread;

    public interface Listener
    {
        void onStaticEmojiFontUpdated(EmojiFont newFont);

        void onAnimatedEmojiFontUpdated(AnimatedEmojiFont newFont);

        void onBuildError(Throwable error, String context);
    }

    public EmojiHotReloadWatcher(
            String staticSvgFolder, String staticCacheFolder,
            String animatedSourceFolder, String animatedCacheFolder,
            Listener listener)
    {
        this.staticSvgFolder = staticSvgFolder;
        this.staticCacheFolder = staticCacheFolder;
        this.animatedSourceFolder = animatedSourceFolder;
        this.animatedCacheFolder = animatedCacheFolder;
        this.listener = listener;
    }

    public void start()
    {
        if (!isRunning.compareAndSet(false, true))
        {
            return;
        }

        triggerStaticBuild();
        triggerAnimatedBuild();

        this.watchThread = new Thread(this::runWatchLoop, "Emoji-Watch-Thread");
        this.watchThread.setDaemon(true);
        this.watchThread.start();
    }

    public void stop()
    {
        if (!this.isRunning.compareAndSet(true, false))
        {
            return;
        }

        if (this.watchThread != null)
        {
            this.watchThread.interrupt();
        }

        this.scheduler.shutdown();
    }

    private void runWatchLoop()
    {
        try (WatchService watcher = FileSystems.getDefault().newWatchService())
        {
            Path staticPath = Paths.get(this.staticSvgFolder);
            Path animatedPath = Paths.get(this.animatedSourceFolder);

            if (Files.exists(staticPath))
            {
                staticPath.register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
            }

            if (Files.exists(animatedPath))
            {
                animatedPath.register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
            }

            while (this.isRunning.get())
            {
                WatchKey key = watcher.take();
                Path dir = (Path) key.watchable();

                boolean staticChanged = false;
                boolean animatedChanged = false;

                for (WatchEvent<?> event : key.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                    if (dir.equals(staticPath))
                    {
                        staticChanged = true;
                    }

                    else if (dir.equals(animatedPath))
                    {
                        animatedChanged = true;
                    }
                }

                if (staticChanged)
                {
                    this.scheduler.scheduleStatic(this::triggerStaticBuild);
                }

                if (animatedChanged)
                {
                    this.scheduler.scheduleAnimated(this::triggerAnimatedBuild);
                }

                if (!key.reset())
                {
                    break;
                }
            }
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        } catch (IOException e)
        {
            this.listener.onBuildError(e, "WatchService setup failed");
        }
    }

    private void triggerStaticBuild()
    {
        try
        {
            EmojiFont font = EmojiAtlasBuilder.buildOrLoad(this.staticSvgFolder, this.staticCacheFolder);
            this.listener.onStaticEmojiFontUpdated(font);
        } catch (Exception e)
        {
            this.listener.onBuildError(e, "Static Emoji Build");
        }
    }

    private void triggerAnimatedBuild()
    {
        try
        {
            AnimatedEmojiFont font = AnimatedEmojiAtlasBuilder.buildOrLoad(this.animatedSourceFolder, this.animatedCacheFolder);
            this.listener.onAnimatedEmojiFontUpdated(font);
        } catch (Exception e)
        {
            this.listener.onBuildError(e, "Animated Emoji Build");
        }
    }
}
