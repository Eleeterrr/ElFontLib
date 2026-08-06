package eleeter.elfontlib.emoji.animated;

import java.util.ArrayList;
import java.util.List;

public class AnimatedEmojiShaper
{
    public AnimatedEmojiLayout shape(List<AnimatedTextSegment> segments, float fontSize,
                                     float lineHeight, float startX, float startY)
    {
        List<PositionedAnimatedEmoji> result = new ArrayList<>();

        float cursorX = startX;
        float emojiSize = lineHeight;
        float maxWidth = startX;

        for (AnimatedTextSegment seg : segments)
        {
            if (!seg.isAnimatedEmoji()) continue;

            AnimatedEmojiGlyph glyph = seg.asAnimatedEmoji().glyph;

            float aspect = (glyph.nativeHeight > 0) ? glyph.nativeWidth / glyph.nativeHeight : 1.0f;
            float displayWidth = emojiSize * aspect;

            result.add(new PositionedAnimatedEmoji(glyph, cursorX, startY, emojiSize));

            cursorX += displayWidth;
            maxWidth = Math.max(maxWidth, cursorX);
        }

        float width = maxWidth - startX;
        float height = result.isEmpty() ? 0f : emojiSize;

        return new AnimatedEmojiLayout(result, width, height);
    }
}
