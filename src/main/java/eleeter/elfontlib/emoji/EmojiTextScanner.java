package eleeter.elfontlib.emoji;

import java.util.ArrayList;
import java.util.List;

public class EmojiTextScanner
{
    private final EmojiFont emojiFont;

    public EmojiTextScanner(EmojiFont emojiFont)
    {
        this.emojiFont = emojiFont;
    }

    public List<TextSegment> scan(String text)
    {
        List<TextSegment> segments = new ArrayList<>();
        if (text == null || text.isEmpty())
        {
            return segments;
        }

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
                    EmojiGlyph shortcodeGlyph = emojiFont.getEmojiByShortcode(candidate);
                    if (shortcodeGlyph != null)
                    {
                        flushText(textBuffer, segments);
                        segments.add(TextSegment.emoji(shortcodeGlyph));
                        i = closeColon + 1;
                        continue;
                    }
                }
            }

            int cp = text.codePointAt(i);
            int cpLen = Character.charCount(cp);

            if (emojiFont.isEmojiCodepoint(cp))
            {
                int[] collected = collectCodepoints(text, i);
                EmojiGlyph match = greedyMatch(collected);

                if (match != null)
                {
                    flushText(textBuffer, segments);
                    segments.add(TextSegment.emoji(match));
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

    private EmojiGlyph greedyMatch(int[] codepoints)
    {
        for (int len = codepoints.length; len >= 1; len--)
        {
            int[] sub = new int[len];
            System.arraycopy(codepoints, 0, sub, 0, len);
            EmojiGlyph g = emojiFont.getEmoji(sub);
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

    private void flushText(StringBuilder buffer, List<TextSegment> segments)
    {
        if (buffer.length() > 0)
        {
            segments.add(TextSegment.text(buffer.toString()));
            buffer.setLength(0);
        }
    }
}
