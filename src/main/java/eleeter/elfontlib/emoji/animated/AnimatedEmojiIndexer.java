package eleeter.elfontlib.emoji.animated;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eleeter.elfontlib.emoji.build.EmojiBuildException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnimatedEmojiIndexer
{
    private static final Gson GSON = new Gson();
    private static final Set<String> EXTS = Set.of("gif", "webp", "apng", "png");

    public static List<AnimatedEmojiSource> scan(File sourceFolder) throws IOException
    {
        File[] files = sourceFolder.listFiles((dir, name) ->
        {
            String lower = name.toLowerCase();
            int dot = lower.lastIndexOf('.');
            return dot >= 0 && EXTS.contains(lower.substring(dot + 1));
        });

        if (files == null)
            throw new EmojiBuildException("Animated emoji folder not found: " + sourceFolder);

        Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));

        Map<String, String[]> shortcodeMap = loadShortcodes(sourceFolder);

        List<AnimatedEmojiSource> result = new ArrayList<>();
        for (File f : files)
        {
            int[] codepoints = parseFilename(f.getName());
            if (codepoints == null)
            {
                System.err.println("[AnimatedEmojiIndexer] Skipping: " + f.getName()
                        + " (filename not parseable as hex codepoints, e.g. 1f600.gif)");
                continue;
            }
            String ext = extension(f.getName());
            String format = "png".equals(ext) ? "apng" : ext;
            String[] shorts = shortcodeMap.getOrDefault(f.getName(), new String[0]);
            result.add(new AnimatedEmojiSource(f, codepoints, shorts, format));
        }
        return result;
    }

    private static int[] parseFilename(String filename)
    {
        int dot = filename.lastIndexOf('.');
        String base = dot >= 0 ? filename.substring(0, dot) : filename;
        String[] parts = base.split("-");
        try
        {
            int[] codepoints = new int[parts.length];
            for (int i = 0; i < parts.length; i++)
                codepoints[i] = Integer.parseInt(parts[i], 16);
            return codepoints;
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static String extension(String filename)
    {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }

    private static Map<String, String[]> loadShortcodes(File folder) throws IOException
    {
        File f = new File(folder, "shortcodes.json");
        if (!f.exists()) return Map.of();
        try (Reader r = new FileReader(f))
        {
            Type type = new TypeToken<Map<String, String[]>>()
            {
            }.getType();
            Map<String, String[]> map = GSON.fromJson(r, type);
            return map != null ? map : Map.of();
        }
    }
}
