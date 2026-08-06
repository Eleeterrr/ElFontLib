package eleeter.elfontlib.emoji.build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eleeter.elfontlib.emoji.EmojiAtlasData;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;


public class AtlasPacker
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    public static EmojiAtlasData pack(
            List<SvgEmojiSource> sources,
            List<File> bitmaps,
            EmojiBuildConfig config,
            File cacheFolder) throws IOException
    {
        if (sources.isEmpty())
            throw new EmojiBuildException("No SVG emoji sources provided");

        int cellSize = config.atlasCellSize;
        int padding = Math.max(0, config.cellPadding);
        int stride = cellSize + 2 * padding;

        int maxCols = Math.max(1, config.maxAtlasWidth / stride);
        int cols = Math.min(sources.size(), maxCols);
        int rows = (sources.size() + cols - 1) / cols;

        int atlasWidth = cols * stride;
        int atlasHeight = rows * stride;

        if (atlasWidth > config.maxAtlasWidth || atlasHeight > config.maxAtlasHeight)
            throw new EmojiBuildException(" ");

        cacheFolder.mkdirs();

        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = atlas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.Src);

        List<EmojiAtlasData.EmojiEntry> entries = new ArrayList<>(sources.size());

        for (int i = 0; i < sources.size(); i++)
        {
            int col = i % cols;
            int row = i / cols;
            int px = col * stride + padding;
            int py = row * stride + padding;

            File bmp = bitmaps.get(i);
            if (bmp.exists())
            {
                BufferedImage cell = ImageIO.read(bmp);
                if (cell != null)
                {
                    g2d.drawImage(cell, px, py, cellSize, cellSize, null);
                    if (padding > 0)
                        bleedEdges(atlas, px, py, cellSize, cellSize, padding);
                }
            }

            SvgEmojiSource src = sources.get(i);
            EmojiAtlasData.EmojiEntry entry = new EmojiAtlasData.EmojiEntry();
            entry.codepoints = src.codepoints;
            entry.shortcodes = src.shortcodes;
            entry.x = px;
            entry.y = py;
            entry.width = cellSize;
            entry.height = cellSize;
            entries.add(entry);
        }

        g2d.dispose();

        File atlasPng = new File(cacheFolder, "atlas.png");
        File atlasJson = new File(cacheFolder, "atlas.json");

        if (!ImageIO.write(atlas, "PNG", atlasPng))
            throw new EmojiBuildException(
                    "ImageIO found no writer for PNG format — this is unexpected on a standard JDK.");

        EmojiAtlasData.AtlasInfo atlasInfo = new EmojiAtlasData.AtlasInfo();
        atlasInfo.width = atlasWidth;
        atlasInfo.height = atlasHeight;
        atlasInfo.cellPadding = config.cellPadding;
        atlasInfo.distanceRange = config.distanceRange;

        EmojiAtlasData data = new EmojiAtlasData();
        data.atlas = atlasInfo;
        data.atlasImagePath = atlasPng.getAbsolutePath();
        data.emojis = entries;

        try (Writer writer = new FileWriter(atlasJson))
        {
            GSON.toJson(data, writer);
        }

        return data;
    }


    private static void bleedEdges(BufferedImage atlas, int x, int y, int w, int h, int padding)
    {
        for (int py = 0; py < h; py++)
        {
            int leftRGB = atlas.getRGB(x, y + py);
            int rightRGB = atlas.getRGB(x + w - 1, y + py);
            for (int p = 1; p <= padding; p++)
            {
                atlas.setRGB(x - p, y + py, leftRGB);
                atlas.setRGB(x + w - 1 + p, y + py, rightRGB);
            }
        }
        for (int px = -padding; px < w + padding; px++)
        {
            int srcX = Math.max(x, Math.min(x + w - 1, x + px));
            int topRGB = atlas.getRGB(srcX, y);
            int bottomRGB = atlas.getRGB(srcX, y + h - 1);
            for (int p = 1; p <= padding; p++)
            {
                atlas.setRGB(x + px, y - p, topRGB);
                atlas.setRGB(x + px, y + h - 1 + p, bottomRGB);
            }
        }
    }
}
