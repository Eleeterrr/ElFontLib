package eleeter.elfontlib.emoji.build;

import java.io.File;

public class SvgEmojiSource
{
    public final File     svgFile;
    public final int[]    codepoints;
    public final String[] shortcodes;

    public SvgEmojiSource(File svgFile, int[] codepoints, String[] shortcodes)
    {
        this.svgFile    = svgFile;
        this.codepoints = codepoints;
        this.shortcodes = shortcodes;
    }
}
