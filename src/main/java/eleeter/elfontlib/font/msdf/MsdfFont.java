package eleeter.elfontlib.font.msdf;

import eleeter.elfontlib.font.Font;
import eleeter.elfontlib.font.GlyphMetrics;
import java.util.HashMap;
import java.util.Map;

public class MsdfFont implements Font
{
    private final MsdfFontData fontData;
    private final Map<Integer, MsdfGlyph> glyphs;
    private float pxRange = 2.0f;

    public MsdfFont(MsdfFontData fontData)
    {
        this.fontData = fontData;
        this.glyphs = new HashMap<>();

        if (fontData.atlas == null)
        {
            throw new IllegalArgumentException("MsdfFontData.atlas is null");
        }
        if (fontData.metrics == null)
        {
            throw new IllegalArgumentException("MsdfFontData.metrics is null");
        }
        if (fontData.glyphs == null)
        {
            throw new IllegalArgumentException("MsdfFontData.glyphs is null");
        }

        this.pxRange = fontData.atlas.distanceRange;

        float atlasW = fontData.atlas.width;
        float atlasH = fontData.atlas.height;
        float ascender = fontData.metrics.ascender;

        for (MsdfFontData.GlyphData glyphData : fontData.glyphs)
        {
            int charId = (glyphData.unicode != 0) ? glyphData.unicode : glyphData.index;

            float u0 = 0, v0 = 0, u1 = 0, v1 = 0;
            float planeLeft = 0, planeBottom = 0, planeRight = 0, planeTop = 0;
            float width = 0, height = 0, xOffset = 0, yOffset = 0;

            if (glyphData.atlasBounds != null && glyphData.planeBounds != null)
            {
                u0 = glyphData.atlasBounds.left / atlasW;
                v0 = glyphData.atlasBounds.top / atlasH;
                u1 = glyphData.atlasBounds.right / atlasW;
                v1 = glyphData.atlasBounds.bottom / atlasH;

                planeLeft = glyphData.planeBounds.left;
                planeBottom = glyphData.planeBounds.bottom;
                planeRight = glyphData.planeBounds.right;
                planeTop = glyphData.planeBounds.top;

                width = planeRight - planeLeft;
                height = planeTop - planeBottom;

                xOffset = planeLeft;
                yOffset = ascender - planeTop;
            }

            MsdfGlyph glyph = new MsdfGlyph(charId, width, height, xOffset, yOffset, glyphData.advance, u0, v0, u1, v1);
            glyph.planeLeft = planeLeft;
            glyph.planeBottom = planeBottom;
            glyph.planeRight = planeRight;
            glyph.planeTop = planeTop;

            glyphs.put(charId, glyph);
        }
    }

    @Override
    public GlyphMetrics getGlyph(int id)
    {
        return glyphs.get(id);
    }

    @Override
    public float getLineHeight()
    {
        return fontData.metrics.lineHeight;
    }

    @Override
    public float getBaseline()
    {
        return fontData.metrics.ascender;
    }

    @Override
    public float getNativeSize()
    {
        return fontData.atlas.size;
    }

    public float getPxRange()
    {
        return pxRange;
    }

    public float getDistanceRange()
    {
        return pxRange;
    }

    public MsdfFontData getFontData()
    {
        return fontData;
    }
}
