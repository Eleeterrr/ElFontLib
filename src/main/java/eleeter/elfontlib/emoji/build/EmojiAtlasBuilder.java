package eleeter.elfontlib.emoji.build;

import eleeter.elfontlib.emoji.EmojiAtlasData;
import eleeter.elfontlib.emoji.EmojiFont;
import eleeter.elfontlib.emoji.EmojiJsonParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class EmojiAtlasBuilder
{
    public static EmojiFont buildOrLoad(String svgFolder, String cacheFolder) throws IOException
    {
        return buildOrLoad(new EmojiBuildConfig(svgFolder, cacheFolder));
    }

    public static EmojiFont buildOrLoad(EmojiBuildConfig config) throws IOException
    {
        File svgDir = new File(config.svgFolder);
        File cacheDir = new File(config.cacheFolder);

        List<SvgEmojiSource> sources = SvgEmojiIndexer.scan(svgDir);

        if (sources.isEmpty())
        {
            System.err.println("[EmojiAtlasBuilder] No SVG files found in " + svgDir.getAbsolutePath());
            return new EmojiFont(emptyAtlasData(config));
        }

        File manifestFile = new File(cacheDir, "manifest.json");
        File atlasPng = new File(cacheDir, "atlas.png");
        File atlasJson = new File(cacheDir, "atlas.json");

        String currentHash = BuildManifest.computeHash(sources);

        if (manifestFile.exists() && atlasPng.exists() && atlasJson.exists())
        {
            BuildManifest stored = BuildManifest.load(manifestFile);
            if (currentHash.equals(stored.combinedHash))
                return new EmojiFont(EmojiJsonParser.load(atlasJson.getAbsolutePath()));
        }

        SvgRasterizer rasterizer = new SvgRasterizer();
        File tmpDir = new File(cacheDir, "tmp");
        tmpDir.mkdirs();

        List<File> bitmaps = new ArrayList<>(sources.size());
        for (SvgEmojiSource source : sources)
        {
            String baseName = source.svgFile.getName().replace(".svg", ".png");
            File outputPng = new File(tmpDir, baseName);
            rasterizer.rasterize(source.svgFile, outputPng, config.atlasCellSize);
            bitmaps.add(outputPng);
        }

        AtlasPacker.pack(sources, bitmaps, config, cacheDir);

        BuildManifest.fromSources(sources).save(manifestFile);

        deleteTmpDir(tmpDir);

        return new EmojiFont(EmojiJsonParser.load(atlasJson.getAbsolutePath()));
    }

    private static EmojiAtlasData emptyAtlasData(EmojiBuildConfig config)
    {
        EmojiAtlasData.AtlasInfo info = new EmojiAtlasData.AtlasInfo();
        info.width = 1;
        info.height = 1;
        info.cellPadding = config.cellPadding;
        info.distanceRange = config.distanceRange;

        EmojiAtlasData data = new EmojiAtlasData();
        data.atlas = info;
        data.atlasImagePath = "";
        data.emojis = List.of();
        return data;
    }

    private static void deleteTmpDir(File dir)
    {
        File[] files = dir.listFiles();
        if (files != null)
            for (File f : files)
                f.delete();
        dir.delete();
    }
}
