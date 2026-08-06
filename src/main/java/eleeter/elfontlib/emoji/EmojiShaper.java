package eleeter.elfontlib.emoji;

import java.util.ArrayList;
import java.util.List;

public class EmojiShaper
{
    public EmojiLayout shape(List<TextSegment> segments, float fontSize, float lineHeight,
                             float startX, float startY)
    {
        List<PositionedEmoji> result = new ArrayList<>();

        float cursorX = startX;
        float emojiSize = lineHeight;
        float maxWidth = startX;

        for (TextSegment seg : segments)
        {
            if (!seg.isEmoji()) continue;

            EmojiGlyph glyph = seg.asEmoji().glyph;

            float aspect = (glyph.nativeHeight > 0) ? glyph.nativeWidth / glyph.nativeHeight : 1.0f;
            float displayWidth = emojiSize * aspect;

            result.add(new PositionedEmoji(glyph, cursorX, startY, emojiSize));

            cursorX += displayWidth;
            maxWidth = Math.max(maxWidth, cursorX);
        }

        float width = maxWidth - startX;
        float height = result.isEmpty() ? 0f : emojiSize;

        return new EmojiLayout(result, width, height);
    }
}
