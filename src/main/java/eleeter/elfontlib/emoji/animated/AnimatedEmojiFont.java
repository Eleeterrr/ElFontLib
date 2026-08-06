package eleeter.elfontlib.emoji.animated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AnimatedEmojiFont
{
    private final AnimatedEmojiAtlasData data;
    private final Map<String, AnimatedEmojiGlyph> byCodepointKey = new HashMap<>();
    private final Map<String, AnimatedEmojiGlyph> byShortcode = new HashMap<>();
    private final Set<Integer> knownFirstCps = new HashSet<>();

    public AnimatedEmojiFont(AnimatedEmojiAtlasData data)
    {
        if (data == null)
            throw new IllegalArgumentException("AnimatedEmojiAtlasData must not be null");

        this.data = data;

        if (data.emojis == null) return;

        float atlasW = data.atlas != null ? data.atlas.width : 1f;
        float atlasH = data.atlas != null ? data.atlas.height : 1f;

        for (AnimatedEmojiAtlasData.AnimatedEmojiEntry entry : data.emojis)
        {
            if (entry.codepoints == null || entry.codepoints.length == 0) continue;
            if (entry.frames == null || entry.frames.isEmpty()) continue;

            AnimFrameInfo[] frameInfos = new AnimFrameInfo[entry.frames.size()];
            for (int i = 0; i < frameInfos.length; i++)
            {
                AnimatedEmojiAtlasData.AnimFrame f = entry.frames.get(i);
                frameInfos[i] = new AnimFrameInfo(
                        f.x / atlasW,
                        f.y / atlasH,
                        (f.x + f.width) / atlasW,
                        (f.y + f.height) / atlasH,
                        f.durationMillis
                );
            }

            AnimatedEmojiGlyph glyph = new AnimatedEmojiGlyph(
                    entry.codepoints,
                    entry.shortcodes,
                    entry.loopCount,
                    entry.pageIndex,
                    frameInfos,
                    entry.nativeWidth,
                    entry.nativeHeight
            );

            byCodepointKey.put(codepointKey(entry.codepoints), glyph);
            knownFirstCps.add(entry.codepoints[0]);

            if (entry.shortcodes != null)
                for (String sc : entry.shortcodes)
                    if (sc != null)
                        byShortcode.put(sc.toLowerCase(Locale.ROOT), glyph);
        }
    }

    public AnimatedEmojiGlyph getEmoji(int... codepoints)
    {
        return byCodepointKey.get(codepointKey(codepoints));
    }

    public AnimatedEmojiGlyph getEmojiByShortcode(String shortcode)
    {
        if (shortcode == null) return null;
        return byShortcode.get(shortcode.toLowerCase(Locale.ROOT));
    }

    public boolean isAnimatedEmojiCodepoint(int codepoint)
    {
        return knownFirstCps.contains(codepoint);
    }

    public int getAtlasWidth()
    {
        return data.atlas != null ? data.atlas.width : 0;
    }

    public int getAtlasHeight()
    {
        return data.atlas != null ? data.atlas.height : 0;
    }

    public int getPageCount()
    {
        return data.atlasPages != null ? data.atlasPages.size() : 0;
    }

    public List<String> getAtlasImagePaths()
    {
        return data.atlasPages != null ? data.atlasPages : new ArrayList<>();
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
