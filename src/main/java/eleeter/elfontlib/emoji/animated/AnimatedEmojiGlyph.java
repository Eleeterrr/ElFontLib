package eleeter.elfontlib.emoji.animated;

public class AnimatedEmojiGlyph
{
    public final int[] codepoints;
    public final String[] shortcodes;
    public final int loopCount;
    public final int pageIndex;
    public final AnimFrameInfo[] frames;
    public final float nativeWidth;
    public final float nativeHeight;

    private final int totalDurationMillis;

    public AnimatedEmojiGlyph(int[] codepoints, String[] shortcodes, int loopCount,
                              int pageIndex, AnimFrameInfo[] frames,
                              float nativeWidth, float nativeHeight)
    {
        this.codepoints = codepoints;
        this.shortcodes = shortcodes != null ? shortcodes : new String[0];
        this.loopCount = loopCount;
        this.pageIndex = pageIndex;
        this.frames = frames;
        this.nativeWidth = nativeWidth;
        this.nativeHeight = nativeHeight;

        int total = 0;
        for (AnimFrameInfo f : frames) total += f.durationMillis;
        this.totalDurationMillis = Math.max(1, total);
    }

    public int getTotalDurationMillis()
    {
        return totalDurationMillis;
    }
}
