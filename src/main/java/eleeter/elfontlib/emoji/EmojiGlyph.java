package eleeter.elfontlib.emoji;

public class EmojiGlyph
{

    public final int[] codepoints;
    public final float u0;
    public final float v0;
    public final float u1;
    public final float v1;

    public final float nativeWidth;
    public final float nativeHeight;
    public final String[] shortcodes;

    public EmojiGlyph(int[] codepoints, float u0, float v0, float u1, float v1,
                      float nativeWidth, float nativeHeight, String[] shortcodes)
    {
        this.codepoints = codepoints;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.nativeWidth = nativeWidth;
        this.nativeHeight = nativeHeight;
        this.shortcodes = shortcodes != null ? shortcodes : new String[0];
    }
}
