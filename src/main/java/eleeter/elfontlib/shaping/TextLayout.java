package eleeter.elfontlib.shaping;

import java.util.List;


public class TextLayout
{
    private final List<PositionedGlyph> glyphs;
    private final float width;
    private final float height;

    public TextLayout(List<PositionedGlyph> glyphs, float width, float height)
    {
        this.glyphs = glyphs;
        this.width = width;
        this.height = height;
    }

    public List<PositionedGlyph> getGlyphs()
    {
        return this.glyphs;
    }

    public float getWidth()
    {
        return this.width;
    }

    public float getHeight()
    {
        return this.height;
    }
}
