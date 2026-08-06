package eleeter.elfontlib.emoji.build;


public class EmojiBuildConfig
{
    public final String svgFolder;

    public final String cacheFolder;

    public int atlasCellSize = 256;

    public int cellPadding = 4;

    public float distanceRange = 4.0f;

    public String msdfgenBinaryOverride = null;

    public int maxAtlasWidth = 2048;

    public int maxAtlasHeight = 2048;

    public EmojiBuildConfig(String svgFolder, String cacheFolder)
    {
        this.svgFolder = svgFolder;
        this.cacheFolder = cacheFolder;
    }
}
