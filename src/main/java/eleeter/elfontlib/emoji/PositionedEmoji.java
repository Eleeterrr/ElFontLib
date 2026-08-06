package eleeter.elfontlib.emoji;

public class PositionedEmoji
{
    public final EmojiGlyph glyph;
    public final float x;
    public final float y;
    public final float scale;

    public PositionedEmoji(EmojiGlyph glyph, float x, float y, float scale)
    {
        this.glyph = glyph;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
}
