package eleeter.elfontlib.emoji.animated;

public class PositionedAnimatedEmoji
{
    public final AnimatedEmojiGlyph glyph;
    public final float x;
    public final float y;
    public final float scale;

    public PositionedAnimatedEmoji(AnimatedEmojiGlyph glyph, float x, float y, float scale)
    {
        this.glyph = glyph;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
}
