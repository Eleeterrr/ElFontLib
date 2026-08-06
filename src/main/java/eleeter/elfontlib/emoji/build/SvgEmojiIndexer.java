package eleeter.elfontlib.emoji.build;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SvgEmojiIndexer
{
    private static final Gson GSON = new Gson();

    public static List<SvgEmojiSource> scan(File svgFolder) throws IOException
    {
        File[] svgFiles = svgFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".svg"));
        if (svgFiles == null)
            throw new EmojiBuildException("SVG folder does not exist or is not a directory: " + svgFolder);

        Arrays.sort(svgFiles, (a, b) -> a.getName().compareTo(b.getName()));

        Map<String, String[]> shortcodeMap = loadShortcodes(svgFolder);

        List<SvgEmojiSource> result = new ArrayList<>();
        for (File f : svgFiles)
        {
            int[] codepoints = parseFilename(f.getName());
            if (codepoints == null)
            {
                System.err.println("[EmojiAtlasBuilder] Skipping unrecognized filename: " + f.getName()
                        + " (expected hex codepoints separated by hyphens, e.g. 1f600.svg)");
                continue;
            }
            String[] shortcodes = shortcodeMap.getOrDefault(f.getName(), new String[0]);
            result.add(new SvgEmojiSource(f, codepoints, shortcodes));
        }
        return result;
    }

    private static int[] parseFilename(String filename)
    {
        String base = filename.substring(0, filename.lastIndexOf('.'));
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

    private static Map<String, String[]> loadShortcodes(File svgFolder) throws IOException
    {
        File shortcodesFile = new File(svgFolder, "shortcodes.json");
        if (!shortcodesFile.exists()) return Map.of();

        try (Reader reader = new FileReader(shortcodesFile))
        {
            Type type = new TypeToken<Map<String, String[]>>()
            {
            }.getType();
            Map<String, String[]> map = GSON.fromJson(reader, type);
            return map != null ? map : Map.of();
        }
    }
}
