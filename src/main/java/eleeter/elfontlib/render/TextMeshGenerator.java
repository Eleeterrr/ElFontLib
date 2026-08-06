package eleeter.elfontlib.render;

import eleeter.elfontlib.font.Font;
import eleeter.elfontlib.font.GlyphMetrics;
import eleeter.elfontlib.shaping.PositionedGlyph;
import eleeter.elfontlib.shaping.TextLayout;
import java.util.ArrayList;
import java.util.List;


public class TextMeshGenerator
{

    public static MeshData generate(TextLayout layout, Font font)
    {
        return generate(layout, font, false);
    }

    public static MeshData generate(TextLayout layout, Font font, boolean snapToPixels)
    {
        List<PositionedGlyph> glyphs = layout.getGlyphs();

        List<Float> vertexList = new ArrayList<>(glyphs.size() * 4 * 5);
        List<Integer> indexList = new ArrayList<>(glyphs.size() * 6);

        int indexOffset = 0;
        float baseOffset = font.getBaseline();

        for (PositionedGlyph pg : glyphs)
        {
            GlyphMetrics metrics = pg.getMetrics();
            float scale = pg.getScale();

            float yTop = pg.getY() + (baseOffset - metrics.getYOffset()) * scale;
            float yBottom = yTop - (metrics.getHeight() * scale);

            float x0 = pg.getX() + (metrics.getXOffset() * scale);
            float x1 = x0 + (metrics.getWidth() * scale);

            if (snapToPixels)
            {
                x0 = Math.round(x0);
                yTop = Math.round(yTop);
                x1 = x0 + Math.round(metrics.getWidth() * scale);
                yBottom = yTop - Math.round(metrics.getHeight() * scale);
            }

            /* Bottom-Left */
            addVertex(vertexList, x0, yBottom, 0.0f, metrics.getU0(), metrics.getV1());

            /* Bottom-Right */
            addVertex(vertexList, x1, yBottom, 0.0f, metrics.getU1(), metrics.getV1());

            /* Top-Right */
            addVertex(vertexList, x1, yTop, 0.0f, metrics.getU1(), metrics.getV0());

            /* Top-Left */
            addVertex(vertexList, x0, yTop, 0.0f, metrics.getU0(), metrics.getV0());

            /* Quad Indices */
            indexList.add(indexOffset + 0);
            indexList.add(indexOffset + 1);
            indexList.add(indexOffset + 2);

            indexList.add(indexOffset + 0);
            indexList.add(indexOffset + 2);
            indexList.add(indexOffset + 3);

            indexOffset += 4;
        }

        float[] vArray = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) vArray[i] = vertexList.get(i);

        int[] iArray = new int[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) iArray[i] = indexList.get(i);

        return new MeshData(vArray, iArray);
    }

    private static void addVertex(List<Float> list, float x, float y, float z, float u, float v)
    {
        list.add(x);
        list.add(y);
        list.add(z);
        list.add(u);
        list.add(v);
    }
}
