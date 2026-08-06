package eleeter.elfontlib.emoji;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EmojiFont
{
    private final EmojiAtlasData data;
    private final Map<String, EmojiGlyph> byCodepointKey = new HashMap<>();
    private final Map<String, EmojiGlyph> byShortcode = new HashMap<>();
    private final Set<Integer> knownFirstCodepoints = new HashSet<>();

    public EmojiFont(EmojiAtlasData data)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("EmojiAtlasData must not be null");
        }

        if (data.atlas == null)
        {
            throw new IllegalArgumentException("EmojiAtlasData.atlas section is missing!");
        }

        if (data.emojis == null)
        {
            throw new IllegalArgumentException("EmojiAtlasData.emojis list is missing!");
        }

        this.data = data;

        float atlasW = data.atlas.width;
        float atlasH = data.atlas.height;

        for (EmojiAtlasData.EmojiEntry entry : data.emojis)
        {
            if (entry.codepoints == null || entry.codepoints.length == 0) continue;

            float u0 = entry.x / atlasW;
            float v0 = entry.y / atlasH;
            float u1 = (entry.x + entry.width) / atlasW;
            float v1 = (entry.y + entry.height) / atlasH;

            EmojiGlyph glyph = new EmojiGlyph(
                    entry.codepoints,
                    u0, v0, u1, v1,
                    entry.width, entry.height,
                    entry.shortcodes
            );

            byCodepointKey.put(codepointKey(entry.codepoints), glyph);
            knownFirstCodepoints.add(entry.codepoints[0]);

            if (entry.shortcodes != null)
            {
                for (String code : entry.shortcodes)
                {
                    if (code != null)
                        byShortcode.put(code.toLowerCase(Locale.ROOT), glyph);
                }
            }
        }
    }

    public EmojiGlyph getEmoji(int... codepoints)
    {
        return byCodepointKey.get(codepointKey(codepoints));
    }

    public EmojiGlyph getEmojiByShortcode(String shortcode)
    {
        if (shortcode == null) return null;
        return byShortcode.get(shortcode.toLowerCase(Locale.ROOT));
    }

    public boolean isEmojiCodepoint(int codepoint)
    {
        return knownFirstCodepoints.contains(codepoint);
    }

    public float getDistanceRange()
    {
        return data.atlas.distanceRange;
    }

    public int getAtlasWidth()
    {
        return data.atlas.width;
    }

    public int getAtlasHeight()
    {
        return data.atlas.height;
    }

    public String getAtlasImagePath()
    {
        return data.atlasImagePath;
    }

    static String codepointKey(int[] codepoints)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codepoints.length; i++)
        {
            if (i > 0) sb.append('-');
            sb.append(codepoints[i]);
        }
        return sb.toString();
    }
}
