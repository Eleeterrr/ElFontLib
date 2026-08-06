package eleeter.elfontlib.font.msdf;

import eleeter.elfontlib.font.GlyphMetrics;

public class MsdfGlyph extends GlyphMetrics
{
    public float planeLeft;
    public float planeBottom;
    public float planeRight;
    public float planeTop;

    public MsdfGlyph(int id, float width, float height, float xOffset, float yOffset, float xAdvance, float u0, float v0, float u1, float v1)
    {
        super(id, width, height, xOffset, yOffset, xAdvance, u0, v0, u1, v1);
    }

    public MsdfGlyph()
    {
        super(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
