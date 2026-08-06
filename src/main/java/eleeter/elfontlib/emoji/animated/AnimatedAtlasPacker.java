package eleeter.elfontlib.emoji.animated;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eleeter.elfontlib.emoji.build.EmojiBuildException;
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

public class AnimatedAtlasPacker
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final GifFrameDecoder GIF_DECODER = new GifFrameDecoder();
    private static final WebpFrameDecoder WEBP_DECODER = new WebpFrameDecoder();
    private static final ApngFrameDecoder APNG_DECODER = new ApngFrameDecoder();

    public static AnimatedEmojiAtlasData pack(
            List<AnimatedEmojiSource> sources,
            AnimatedBuildConfig config,
            File cacheFolder) throws IOException
    {
        if (sources.isEmpty())
            throw new EmojiBuildException("No animated emoji sources provided.");

        cacheFolder.mkdirs();

        int cellSize = config.atlasCellSize;
        int padding = Math.max(0, config.cellPadding);
        int stride = cellSize + 2 * padding;
        int maxCols = Math.max(1, config.maxAtlasWidth / stride);
        int maxRows = Math.max(1, config.maxAtlasHeight / stride);
        int cellsPerPage = maxCols * maxRows;

        record EmojiFrames(AnimatedEmojiSource source, List<DecodedFrame> frames, int loopCount)
        {
        }
        List<EmojiFrames> allEmoji = new ArrayList<>();

        for (AnimatedEmojiSource src : sources)
        {
            try
            {
                FrameDecoder decoder = decoderFor(src.format);
                List<DecodedFrame> frames = decoder.decode(src.sourceFile);
                int loopCount = extractLoopCount(src);
                allEmoji.add(new EmojiFrames(src, frames, loopCount));
            } catch (EmojiBuildException e)
            {
                System.err.println("[AnimatedAtlasPacker] Skipping " + src.sourceFile.getName()
                        + ": " + e.getMessage());
            }
        }

        if (allEmoji.isEmpty())
            throw new EmojiBuildException("All animated emoji sources failed to decode.");

        List<BufferedImage> atlasPages = new ArrayList<>();
        List<String> atlasPagePaths = new ArrayList<>();
        List<AnimatedEmojiAtlasData.AnimatedEmojiEntry> entries = new ArrayList<>();

        int currentPage = -1;
        int cellsUsedOnPage = 0;
        BufferedImage currentCanvas = null;
        Graphics2D currentG = null;

        for (EmojiFrames ef : allEmoji)
        {
            int frameCount = ef.frames().size();
            BufferedImage firstFrame = ef.frames().get(0).image;
            int srcW = firstFrame.getWidth();
            int srcH = firstFrame.getHeight();

            if (currentPage < 0 || cellsUsedOnPage + frameCount > cellsPerPage)
            {
                if (currentG != null) currentG.dispose();
                int pageW = maxCols * stride;
                int pageH = maxRows * stride;
                currentCanvas = new BufferedImage(pageW, pageH, BufferedImage.TYPE_INT_ARGB);
                currentG = currentCanvas.createGraphics();
                currentG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                currentG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                currentG.setComposite(AlphaComposite.Src);
                atlasPages.add(currentCanvas);
                currentPage++;
                cellsUsedOnPage = 0;
            }

            AnimatedEmojiAtlasData.AnimatedEmojiEntry entry = new AnimatedEmojiAtlasData.AnimatedEmojiEntry();
            entry.codepoints = ef.source().codepoints;
            entry.shortcodes = ef.source().shortcodes;
            entry.loopCount = ef.loopCount();
            entry.pageIndex = currentPage;
            entry.nativeWidth = srcW;
            entry.nativeHeight = srcH;
            entry.frames = new ArrayList<>();

            for (DecodedFrame df : ef.frames())
            {
                int cellIndex = cellsUsedOnPage;
                int col = cellIndex % maxCols;
                int row = cellIndex / maxCols;
                int px = col * stride + padding;
                int py = row * stride + padding;

                currentG.setComposite(AlphaComposite.Src);
                currentG.drawImage(df.image, px, py, cellSize, cellSize, null);
                bleedEdges(currentCanvas, px, py, cellSize, cellSize, padding);

                AnimatedEmojiAtlasData.AnimFrame frame = new AnimatedEmojiAtlasData.AnimFrame();
                frame.x = px;
                frame.y = py;
                frame.width = cellSize;
                frame.height = cellSize;
                frame.durationMillis = df.durationMillis;
                entry.frames.add(frame);

                cellsUsedOnPage++;
            }

            entries.add(entry);
        }

        if (currentG != null) currentG.dispose();

        for (int i = 0; i < atlasPages.size(); i++)
        {
            File pageFile = new File(cacheFolder, "animated_atlas_" + i + ".png");
            if (!ImageIO.write(atlasPages.get(i), "PNG", pageFile))
                throw new EmojiBuildException("ImageIO PNG write failed for page " + i);
            atlasPagePaths.add(pageFile.getAbsolutePath());
        }

        AnimatedEmojiAtlasData.AtlasInfo atlasInfo = new AnimatedEmojiAtlasData.AtlasInfo();
        atlasInfo.width = maxCols * stride;
        atlasInfo.height = maxRows * stride;
        atlasInfo.cellSize = cellSize;
        atlasInfo.cellPadding = padding;

        AnimatedEmojiAtlasData data = new AnimatedEmojiAtlasData();
        data.atlas = atlasInfo;
        data.atlasPages = atlasPagePaths;
        data.emojis = entries;

        File jsonFile = new File(cacheFolder, "animated_atlas.json");
        try (Writer w = new FileWriter(jsonFile))
        {
            GSON.toJson(data, w);
        }

        return data;
    }

    private static FrameDecoder decoderFor(String format)
    {
        return switch (format)
        {
            case "webp" -> WEBP_DECODER;
            case "apng" -> APNG_DECODER;
            default -> GIF_DECODER;
        };
    }

    private static int extractLoopCount(AnimatedEmojiSource src)
    {
        return 0;
    }

    private static void bleedEdges(BufferedImage atlas, int x, int y, int w, int h, int padding)
    {
        if (padding <= 0) return;
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
            int botRGB = atlas.getRGB(srcX, y + h - 1);
            for (int p = 1; p <= padding; p++)
            {
                atlas.setRGB(x + px, y - p, topRGB);
                atlas.setRGB(x + px, y + h - 1 + p, botRGB);
            }
        }
    }
}
