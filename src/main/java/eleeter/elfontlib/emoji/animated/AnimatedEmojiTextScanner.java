package eleeter.elfontlib.emoji.animated;

import java.util.ArrayList;
import java.util.List;

public class AnimatedEmojiTextScanner
{
    private final AnimatedEmojiFont animFont;

    public AnimatedEmojiTextScanner(AnimatedEmojiFont animFont)
    {
        this.animFont = animFont;
    }

    public List<AnimatedTextSegment> scan(String text)
    {
        List<AnimatedTextSegment> segments = new ArrayList<>();
        if (text == null || text.isEmpty()) return segments;

        StringBuilder textBuffer = new StringBuilder();
        int i = 0;

        while (i < text.length())
        {
            if (text.charAt(i) == ':')
            {
                int closeColon = text.indexOf(':', i + 1);
                if (closeColon > i + 1)
                {
                    String candidate = text.substring(i, closeColon + 1);
                    AnimatedEmojiGlyph shortcodeGlyph = animFont.getEmojiByShortcode(candidate);
                    if (shortcodeGlyph != null)
                    {
                        flushText(textBuffer, segments);
                        segments.add(AnimatedTextSegment.animatedEmoji(shortcodeGlyph));
                        i = closeColon + 1;
                        continue;
                    }
                }
            }

            int cp = text.codePointAt(i);
            int cpLen = Character.charCount(cp);

            if (animFont.isAnimatedEmojiCodepoint(cp))
            {
                int[] collected = collectCodepoints(text, i);
                AnimatedEmojiGlyph match = greedyMatch(collected);

                if (match != null)
                {
                    flushText(textBuffer, segments);
                    segments.add(AnimatedTextSegment.animatedEmoji(match));
                    i += charsForCodepoints(text, i, match.codepoints.length);
                    continue;
                }
            }

            textBuffer.appendCodePoint(cp);
            i += cpLen;
        }

        flushText(textBuffer, segments);
        return segments;
    }

    private AnimatedEmojiGlyph greedyMatch(int[] codepoints)
    {
        for (int len = codepoints.length; len >= 1; len--)
        {
            int[] sub = new int[len];
            System.arraycopy(codepoints, 0, sub, 0, len);
            AnimatedEmojiGlyph g = animFont.getEmoji(sub);
            if (g != null) return g;
        }
        return null;
    }

    private int[] collectCodepoints(String text, int charIndex)
    {
        List<Integer> list = new ArrayList<>();
        int i = charIndex;
        while (i < text.length() && list.size() < 16)
        {
            int cp = text.codePointAt(i);
            list.add(cp);
            i += Character.charCount(cp);
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private int charsForCodepoints(String text, int charIndex, int count)
    {
        int total = 0;
        int i = charIndex;
        for (int c = 0; c < count && i < text.length(); c++)
        {
            int cp = text.codePointAt(i);
            int len = Character.charCount(cp);
            total += len;
            i += len;
        }
        return total;
    }

    private void flushText(StringBuilder buffer, List<AnimatedTextSegment> segments)
    {
        if (buffer.length() > 0)
        {
            segments.add(AnimatedTextSegment.text(buffer.toString()));
            buffer.setLength(0);
        }
    }
}
