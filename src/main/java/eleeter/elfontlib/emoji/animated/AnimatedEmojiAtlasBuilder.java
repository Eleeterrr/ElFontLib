package eleeter.elfontlib.emoji.animated;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AnimatedEmojiAtlasBuilder
{
    public static AnimatedEmojiFont buildOrLoad(String sourceFolder, String cacheFolder) throws IOException
    {
        return buildOrLoad(new AnimatedBuildConfig(sourceFolder, cacheFolder));
    }

    public static AnimatedEmojiFont buildOrLoad(AnimatedBuildConfig config) throws IOException
    {
        File srcDir = new File(config.sourceFolder);
        File cacheDir = new File(config.cacheFolder);

        List<AnimatedEmojiSource> sources = AnimatedEmojiIndexer.scan(srcDir);

        if (sources.isEmpty())
        {
            System.err.println("[AnimatedEmojiAtlasBuilder] No animated emoji found in "
                    + srcDir.getAbsolutePath() + " — returning empty AnimatedEmojiFont.");
            return new AnimatedEmojiFont(emptyAtlasData());
        }

        File manifestFile = new File(cacheDir, "animated_manifest.json");
        File atlasJson = new File(cacheDir, "animated_atlas.json");

        String currentHash = AnimatedBuildManifest.computeHash(sources);

        if (manifestFile.exists() && atlasJson.exists())
        {
            AnimatedBuildManifest stored = AnimatedBuildManifest.load(manifestFile);
            if (currentHash.equals(stored.combinedHash))
                return new AnimatedEmojiFont(AnimatedEmojiAtlasDataJsonParser.load(atlasJson.getAbsolutePath()));
        }

        AnimatedAtlasPacker.pack(sources, config, cacheDir);
        AnimatedBuildManifest.fromSources(sources).save(manifestFile);

        return new AnimatedEmojiFont(AnimatedEmojiAtlasDataJsonParser.load(atlasJson.getAbsolutePath()));
    }

    private static AnimatedEmojiAtlasData emptyAtlasData()
    {
        AnimatedEmojiAtlasData.AtlasInfo info = new AnimatedEmojiAtlasData.AtlasInfo();
        info.width = 1;
        info.height = 1;
        info.cellSize = 1;
        info.cellPadding = 0;

        AnimatedEmojiAtlasData data = new AnimatedEmojiAtlasData();
        data.atlas = info;
        data.atlasPages = List.of();
        data.emojis = List.of();
        return data;
    }
}
