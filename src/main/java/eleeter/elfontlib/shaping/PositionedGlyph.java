package eleeter.elfontlib.shaping;

import eleeter.elfontlib.font.GlyphMetrics;


public class PositionedGlyph
{
    private final GlyphMetrics metrics;
    private final float x;
    private final float y;
    private final float scale;

    public PositionedGlyph(GlyphMetrics metrics, float x, float y, float scale)
    {
        this.metrics = metrics;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public GlyphMetrics getMetrics()
    {
        return this.metrics;
    }
    public float getX()
    {
        return this.x;
    }
    public float getY()
    {
        return this.y;
    }
    public float getScale()
    {
        return this.scale;
    }
}
