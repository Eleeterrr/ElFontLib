package eleeter.elfontlib.emoji.animated;

import eleeter.elfontlib.emoji.build.EmojiBuildException;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

public class GifFrameDecoder implements FrameDecoder
{
    private static final int DEFAULT_DELAY_MS = 100;

    @Override
    public List<DecodedFrame> decode(File sourceFile) throws EmojiBuildException
    {
        try (ImageInputStream stream = ImageIO.createImageInputStream(sourceFile))
        {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext())
                throw new EmojiBuildException("No GIF ImageReader found: " + sourceFile.getName());

            ImageReader reader = readers.next();
            reader.setInput(stream);

            int frameCount = reader.getNumImages(true);
            if (frameCount <= 0)
            {
                throw new EmojiBuildException("GIF has no frames: " + sourceFile.getName());
            }

            IIOMetadata firstMeta = reader.getImageMetadata(0);
            int canvasW = reader.getWidth(0);
            int canvasH = reader.getHeight(0);

            BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
            BufferedImage previous = null;

            List<DecodedFrame> frames = new ArrayList<>(frameCount);

            for (int i = 0; i < frameCount; i++)
            {
                BufferedImage rawFrame = reader.read(i);
                IIOMetadata frameMeta = reader.getImageMetadata(i);
                IIOMetadataNode root = (IIOMetadataNode) frameMeta.getAsTree("javax_imageio_gif_image_1.0");

                FrameMetadata meta = parseFrameMetadata(root, canvasW, canvasH);

                BufferedImage snapshot = null;
                if ("restoreToPrevious".equals(meta.disposalMethod))
                {
                    snapshot = copyImage(canvas);
                }

                Graphics2D g = canvas.createGraphics();
                g.setComposite(AlphaComposite.SrcOver);
                g.drawImage(rawFrame, meta.offsetX, meta.offsetY, null);
                g.dispose();

                frames.add(new DecodedFrame(copyImage(canvas), meta.delayMs));

                if ("restoreToBackgroundColor".equals(meta.disposalMethod))
                {
                    Graphics2D gc = canvas.createGraphics();
                    gc.setComposite(AlphaComposite.Clear);
                    gc.fillRect(meta.offsetX, meta.offsetY, rawFrame.getWidth(), rawFrame.getHeight());
                    gc.dispose();
                } else if ("restoreToPrevious".equals(meta.disposalMethod) && snapshot != null)
                {
                    canvas = snapshot;
                }

                previous = rawFrame;
            }

            reader.dispose();
            return frames;
        } catch (EmojiBuildException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new EmojiBuildException("Failed to decode GIF: " + sourceFile.getName(), e);
        }
    }

    private FrameMetadata parseFrameMetadata(IIOMetadataNode root, int canvasW, int canvasH)
    {
        FrameMetadata m = new FrameMetadata();

        IIOMetadataNode gce = firstChild(root, "GraphicControlExtension");
        if (gce != null)
        {
            String delay = gce.getAttribute("delayTime"); // in hundredths of a second
            if (delay != null && !delay.isEmpty())
            {
                try
                {
                    m.delayMs = Math.max(10, Integer.parseInt(delay) * 10);
                } catch (NumberFormatException ignored)
                {
                }
            }
            m.disposalMethod = gce.getAttribute("disposalMethod");
        }

        IIOMetadataNode desc = firstChild(root, "ImageDescriptor");
        if (desc != null)
        {
            m.offsetX = intAttr(desc, "imageLeftPosition", 0);
            m.offsetY = intAttr(desc, "imageTopPosition", 0);
        }

        return m;
    }

    private static IIOMetadataNode firstChild(IIOMetadataNode parent, String name)
    {
        for (int i = 0; i < parent.getLength(); i++)
        {
            org.w3c.dom.Node child = parent.item(i);
            if (name.equals(child.getNodeName()))
            {
                return (IIOMetadataNode) child;
            }
        }
        return null;
    }

    private static int intAttr(IIOMetadataNode node, String attr, int fallback)
    {
        String val = node.getAttribute(attr);
        if (val == null || val.isEmpty()) return fallback;
        try
        {
            return Integer.parseInt(val);
        } catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    private static BufferedImage copyImage(BufferedImage src)
    {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    private static class FrameMetadata
    {
        int delayMs = DEFAULT_DELAY_MS;
        int offsetX = 0;
        int offsetY = 0;
        String disposalMethod = "doNotDispose";
    }
}
