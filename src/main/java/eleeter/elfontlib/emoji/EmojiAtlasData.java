package eleeter.elfontlib.emoji;

import java.util.List;

public class EmojiAtlasData
{
    public AtlasInfo atlas;
    public String atlasImagePath;
    public List<EmojiEntry> emojis;

    public static class AtlasInfo
    {
        public int width;
        public int height;
        public int cellPadding;
        public float distanceRange;
    }

    public static class EmojiEntry
    {
        public int[] codepoints;
        public String[] shortcodes;
        public float x;
        public float y;
        public float width;
        public float height;
    }
}
