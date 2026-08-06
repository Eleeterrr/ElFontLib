package eleeter.elfontlib.emoji.animated;

public class AnimatedBuildConfig
{
    public final String sourceFolder;
    public final String cacheFolder;

    public int atlasCellSize = 128;
    public int cellPadding = 2;
    public int maxAtlasWidth = 4096;
    public int maxAtlasHeight = 4096;

    public AnimatedBuildConfig(String sourceFolder, String cacheFolder)
    {
        this.sourceFolder = sourceFolder;
        this.cacheFolder = cacheFolder;
    }
}
