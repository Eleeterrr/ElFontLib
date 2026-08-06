package eleeter.elfontlib.emoji.build;

import org.lwjgl.nanovg.NSVGImage;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoSVG.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SvgRasterizer
{
    /**
     * How many times larger than the final cell size we rasterize the SVG at before
     * downsampling.
     */
    private static final int SUPERSAMPLE_FACTOR = 4;


    public void rasterize(File input, File output, int size)
    {
        output.getParentFile().mkdirs();

        int superSize = size * SUPERSAMPLE_FACTOR;

        NSVGImage svg = nsvgParseFromFile(input.getAbsolutePath(), "px", 96.0f);
        if (svg == null)
        {
            throw new EmojiBuildException("NanoSVG failed to parse SVG: " + input.getName());
        }

        long rast = nsvgCreateRasterizer();
        if (rast == NULL)
        {
            nsvgDelete(svg);
            throw new EmojiBuildException("NanoSVG failed to create rasterizer.");
        }

        ByteBuffer superImage = memAlloc(superSize * superSize * 4);
        try
        {
            float scaleX = superSize / svg.width();
            float scaleY = superSize / svg.height();
            float scale  = Math.min(scaleX, scaleY);

            float tx = (superSize - svg.width() * scale) / 2.0f;
            float ty = (superSize - svg.height() * scale) / 2.0f;

            nsvgRasterize(rast, svg, tx, ty, scale, superImage, superSize, superSize, superSize * 4);

            BufferedImage hiRes = toBufferedImage(superImage, superSize);
            BufferedImage crisp = downsample(hiRes, size);

            if (!ImageIO.write(crisp, "PNG", output))
            {
                throw new EmojiBuildException("Failed to write rasterized PNG: " + output.getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            throw new EmojiBuildException("Failed to write rasterized PNG: " + output.getAbsolutePath());
        }
        finally
        {
            memFree(superImage);
            nsvgDeleteRasterizer(rast);
            nsvgDelete(svg);
        }
    }

    /** Converts the raw RGBA buffer wrote into a BufferedImage. */
    private static BufferedImage toBufferedImage(ByteBuffer rgba, int size)
    {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int[] row = new int[size];
        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                int i = (y * size + x) * 4;
                int r = rgba.get(i)     & 0xFF;
                int g = rgba.get(i + 1) & 0xFF;
                int b = rgba.get(i + 2) & 0xFF;
                int a = rgba.get(i + 3) & 0xFF;
                row[x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
            img.setRGB(0, y, size, 1, row, 0, size);
        }
        return img;
    }

    private static BufferedImage downsample(BufferedImage src, int targetSize)
    {
        int srcSize = src.getWidth();

        BufferedImage premultiplied = new BufferedImage(srcSize, srcSize, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D pg = premultiplied.createGraphics();
        pg.setComposite(AlphaComposite.Src);
        pg.drawImage(src, 0, 0, null);
        pg.dispose();

        BufferedImage scaledPre = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = scaledPre.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(premultiplied, 0, 0, targetSize, targetSize, null);
        g2d.dispose();

        BufferedImage result = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D rg = result.createGraphics();
        rg.setComposite(AlphaComposite.Src);
        rg.drawImage(scaledPre, 0, 0, null);
        rg.dispose();

        return result;
    }
}
