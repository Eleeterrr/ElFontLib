package eleeter.elfontlib.emoji.animated;

import eleeter.elfontlib.emoji.build.EmojiBuildException;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;


public class WebpFrameDecoder implements FrameDecoder
{
    private static final int DEFAULT_DELAY_MS = 100;

    /* Four-CC constants */
    private static final int FCC_RIFF = fcc("RIFF");
    private static final int FCC_WEBP = fcc("WEBP");
    private static final int FCC_VP8X = fcc("VP8X");
    private static final int FCC_ANIM = fcc("ANIM");
    private static final int FCC_ANMF = fcc("ANMF");
    private static final int FCC_VP8 = fcc("VP8 ");
    private static final int FCC_VP8L = fcc("VP8L");
    private static final int FCC_ALPH = fcc("ALPH");

    @Override
    public List<DecodedFrame> decode(File sourceFile) throws EmojiBuildException
    {
        try
        {
            byte[] data = readAllBytes(sourceFile);
            validateRiffWebp(data, sourceFile.getName());

            if (!isAnimated(data))
            {
                return singleFrame(data, sourceFile.getName());
            }

            return decodeAnimated(data, sourceFile.getName());
        } catch (EmojiBuildException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new EmojiBuildException("Failed to decode WebP: " + sourceFile.getName(), e);
        }
    }


    private List<DecodedFrame> decodeAnimated(byte[] data, String name) throws IOException, EmojiBuildException
    {
        int canvasW = 0, canvasH = 0;
        int[] vp8xRange = findChunk(data, 12, FCC_VP8X);
        if (vp8xRange != null)
        {
            int off = vp8xRange[0] + 8;
            canvasW = (readLE24(data, off + 4) + 1);
            canvasH = (readLE24(data, off + 7) + 1);
        }


        List<DecodedFrame> frames = new ArrayList<>();
        BufferedImage canvas = canvasW > 0 && canvasH > 0 ? new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB) : null;

        int pos = 12;
        while (pos + 8 <= data.length)
        {
            int chunkId = readLE32(data, pos);
            int chunkSize = readLE32(data, pos + 4);
            int dataStart = pos + 8;
            int nextChunk = dataStart + chunkSize + (chunkSize & 1);

            if (chunkId == FCC_ANMF)
            {
                DecodedFrame frame = decodeAnmfChunk(data, dataStart, chunkSize, canvas, canvasW, canvasH, name);
                if (frame != null)
                {
                    frames.add(frame);
                }
            }

            pos = nextChunk;
            if (pos <= dataStart)
            {
                break;
            }
        }

        if (frames.isEmpty())
        {
            throw new EmojiBuildException("Animated WebP has no ANMF frames: " + name);
        }

        return frames;
    }

    private DecodedFrame decodeAnmfChunk(byte[] data, int start, int chunkSize, BufferedImage canvas, int canvasW, int canvasH, String name) throws IOException
    {
        if (start + 16 > data.length)
        {
            return null;
        }

        int frameX = readLE24(data, start) * 2;
        int frameY = readLE24(data, start + 3) * 2;
        int frameW = readLE24(data, start + 6) + 1;
        int frameH = readLE24(data, start + 9) + 1;
        int duration = readLE24(data, start + 12); /* milliseconds */
        int flags = data[start + 15] & 0xFF;

        boolean useAlphaBlending = (flags & 0x02) == 0;
        boolean disposeToBackground = (flags & 0x01) != 0;

        int frameDataStart = start + 16;
        int frameDataLen = chunkSize - 16;

        if (frameDataLen <= 0)
        {
            return null;
        }

        byte[] frameBitstream = Arrays.copyOfRange(data, frameDataStart, frameDataStart + frameDataLen);

        int innerFcc = readLE32(frameBitstream, 0);
        byte[] standaloneWebp = buildStandaloneWebp(frameBitstream, innerFcc, frameW, frameH);

        BufferedImage frameImg = ImageIO.read(new ByteArrayInputStream(standaloneWebp));
        if (frameImg == null)
        {
            return null;
        }

        BufferedImage output;
        if (canvas != null && canvasW > 0 && canvasH > 0)
        {
            Graphics2D g = canvas.createGraphics();
            if (useAlphaBlending)
                g.setComposite(AlphaComposite.SrcOver);
            else
                g.setComposite(AlphaComposite.Src);
            g.drawImage(frameImg, frameX, frameY, null);
            g.dispose();

            output = copyImage(canvas);

            if (disposeToBackground)
            {
                Graphics2D gc = canvas.createGraphics();
                gc.setComposite(AlphaComposite.Clear);
                gc.fillRect(frameX, frameY, frameW, frameH);
                gc.dispose();
            }
        } else
        {
            output = ensureArgb(frameImg);
        }

        int delayMs = duration <= 0 ? DEFAULT_DELAY_MS : duration;
        return new DecodedFrame(output, delayMs);
    }

    private byte[] buildStandaloneWebp(byte[] bitstream, int innerFcc, int w, int h)
    {
        boolean hasChunkHeader = (innerFcc == FCC_VP8 || innerFcc == FCC_VP8L || innerFcc == FCC_ALPH);

        byte[] payload;
        if (hasChunkHeader)
        {
            payload = bitstream;
        } else
        {
            /* Wrap VP8 data in a VP8 chunk */
            payload = new byte[bitstream.length + 8];
            writeLE32(payload, 0, FCC_VP8);
            writeLE32(payload, 4, bitstream.length);
            System.arraycopy(bitstream, 0, payload, 8, bitstream.length);
        }

        int riffSize = 4 + payload.length; // "WEBP" + payload
        byte[] out = new byte[12 + payload.length];
        writeLE32(out, 0, FCC_RIFF);
        writeLE32(out, 4, riffSize);
        writeLE32(out, 8, FCC_WEBP);
        System.arraycopy(payload, 0, out, 12, payload.length);
        return out;
    }


    private List<DecodedFrame> singleFrame(byte[] data, String name) throws IOException, EmojiBuildException
    {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        if (img == null)
        {
            throw new EmojiBuildException("ImageIO could not decode WebP: " + name);
        }
        return List.of(new DecodedFrame(ensureArgb(img), DEFAULT_DELAY_MS));
    }


    private boolean isAnimated(byte[] data)
    {
        return findChunk(data, 12, FCC_ANIM) != null;
    }

    private void validateRiffWebp(byte[] data, String name) throws EmojiBuildException
    {
        if (data.length < 12 || readLE32(data, 0) != FCC_RIFF || readLE32(data, 8) != FCC_WEBP)
        {
            throw new EmojiBuildException("Not a valid WebP file: " + name);
        }
    }


    private int[] findChunk(byte[] data, int start, int fcc)
    {
        int pos = start;
        while (pos + 8 <= data.length)
        {
            int id = readLE32(data, pos);
            int size = readLE32(data, pos + 4);
            if (id == fcc)
                return new int[]{pos, pos + 8 + size};
            pos += 8 + size + (size & 1);
            if (size == 0) break;
        }
        return null;
    }

    private static byte[] readAllBytes(File f) throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(f, "r"))
        {
            byte[] buf = new byte[(int) raf.length()];
            raf.readFully(buf);
            return buf;
        }
    }

    private static int readLE32(byte[] b, int off)
    {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }

    private static int readLE24(byte[] b, int off)
    {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16);
    }

    private static void writeLE32(byte[] b, int off, int val)
    {
        b[off] = (byte) val;
        b[off + 1] = (byte) (val >> 8);
        b[off + 2] = (byte) (val >> 16);
        b[off + 3] = (byte) (val >> 24);
    }

    private static int fcc(String s)
    {
        return (s.charAt(0) & 0xFF) | ((s.charAt(1) & 0xFF) << 8) | ((s.charAt(2) & 0xFF) << 16) | ((s.charAt(3) & 0xFF) << 24);
    }

    private static BufferedImage copyImage(BufferedImage src)
    {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    private static BufferedImage ensureArgb(BufferedImage img)
    {
        if (img.getType() == BufferedImage.TYPE_INT_ARGB)
        {
            return img;
        }
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return out;
    }
}
