package eleeter.elfontlib.emoji.animated;

public class AnimatedEmojiFrameClock
{
    private AnimatedEmojiFrameClock()
    {
    }

    public static int getFrameIndex(AnimatedEmojiGlyph glyph, long elapsedMillis)
    {
        if (glyph.frames.length == 0) return 0;
        if (glyph.frames.length == 1) return 0;

        int total = glyph.getTotalDurationMillis();

        long t;
        if (glyph.loopCount == 0)
        {
            t = elapsedMillis % total;
        } else
        {
            long maxTime = (long) glyph.loopCount * total;
            if (elapsedMillis >= maxTime)
                return glyph.frames.length - 1;
            t = elapsedMillis % total;
        }

        long acc = 0;
        for (int i = 0; i < glyph.frames.length; i++)
        {
            acc += glyph.frames[i].durationMillis;
            if (t < acc) return i;
        }

        return glyph.frames.length - 1;
    }
}
