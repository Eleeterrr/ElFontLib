package eleeter.elfontlib.font.msdf;

import java.util.List;

public class MsdfFontData
{
    public AtlasData atlas;
    public MetricsData metrics;
    public List<GlyphData> glyphs;

    public static class AtlasData
    {
        public String type;
        public float distanceRange;
        public float size;
        public int width;
        public int height;
        public String yOrigin;
    }

    public static class MetricsData
    {
        public float emSize;
        public float lineHeight;
        public float ascender;
        public float descender;
        public float underlineY;
        public float underlineThickness;
    }

    public static class GlyphData
    {
        public int unicode;
        public int index;
        public float advance;
        public BoundsData planeBounds;
        public BoundsData atlasBounds;
    }

    public static class BoundsData
    {
        public float left;
        public float bottom;
        public float right;
        public float top;
    }
}
