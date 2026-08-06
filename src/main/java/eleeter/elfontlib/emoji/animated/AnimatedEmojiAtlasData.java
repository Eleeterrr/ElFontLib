package eleeter.elfontlib.emoji.animated;

import java.util.List;


public class AnimatedEmojiAtlasData
{
    /**
     * Global atlas dimensions and cell metrics.
     */
    public AtlasInfo atlas;

    /**
     * Absolute or relative paths to the atlas page PNG files.
     *
     */
    public List<String> atlasPages;

    /**
     * All animated emoji baked into this atlas set.
     */
    public List<AnimatedEmojiEntry> emojis;


    public static class AtlasInfo
    {
        public int width;
        public int height;
        public int cellSize;
        public int cellPadding;
    }


    public static class AnimatedEmojiEntry
    {

        public int[] codepoints;

        public String[] shortcodes;

        /**
         * How many times the animation loops.
         **/
        public int loopCount;


        public int pageIndex;
        public float nativeWidth;
        public float nativeHeight;
        public List<AnimFrame> frames;
    }


    public static class AnimFrame
    {
        public float x;
        public float y;
        public float width;
        public float height;
        public int durationMillis;
    }
}
