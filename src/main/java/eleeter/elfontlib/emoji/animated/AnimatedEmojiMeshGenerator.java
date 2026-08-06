package eleeter.elfontlib.emoji.animated;

import eleeter.elfontlib.render.MeshData;
import java.util.ArrayList;
import java.util.List;

public class AnimatedEmojiMeshGenerator
{
    public static MeshData generate(AnimatedEmojiLayout layout, long elapsedMillis)
    {
        List<PositionedAnimatedEmoji> emojis = layout.getEmojis();

        List<Float> vertexList = new ArrayList<>(emojis.size() * 4 * 5);
        List<Integer> indexList = new ArrayList<>(emojis.size() * 6);

        int indexOffset = 0;

        for (PositionedAnimatedEmoji pe : emojis)
        {
            AnimatedEmojiGlyph g = pe.glyph;
            int frameIdx = AnimatedEmojiFrameClock.getFrameIndex(g, elapsedMillis);
            AnimFrameInfo frame = g.frames[frameIdx];

            float aspectRatio = (g.nativeHeight > 0) ? g.nativeWidth / g.nativeHeight : 1.0f;
            float w = pe.scale * aspectRatio;
            float h = pe.scale;
            float x0 = pe.x;
            float x1 = pe.x + w;
            float yTop = pe.y;
            float yBot = pe.y - h;

            addVertex(vertexList, x0, yBot, 0f, frame.u0, frame.v1);
            addVertex(vertexList, x1, yBot, 0f, frame.u1, frame.v1);
            addVertex(vertexList, x1, yTop, 0f, frame.u1, frame.v0);
            addVertex(vertexList, x0, yTop, 0f, frame.u0, frame.v0);

            indexList.add(indexOffset);
            indexList.add(indexOffset + 1);
            indexList.add(indexOffset + 2);
            indexList.add(indexOffset);
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
