package eleeter.elfontlib.emoji.animated;

import java.io.File;


public class AnimatedEmojiSource
{
    public final File sourceFile;


    public final int[] codepoints;


    public final String[] shortcodes;


    public final String format;

    public AnimatedEmojiSource(File sourceFile, int[] codepoints,
                               String[] shortcodes, String format)
    {
        this.sourceFile = sourceFile;
        this.codepoints = codepoints;
        this.shortcodes = shortcodes != null ? shortcodes : new String[0];
        this.format = format;
    }
}
