package eleeter.elfontlib.emoji.animated;

import eleeter.elfontlib.emoji.build.EmojiBuildException;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ApngFrameDecoder implements FrameDecoder
{
    private static final int DEFAULT_DELAY_MS = 100;

    private static final int TYPE_IHDR = type("IHDR");
    private static final int TYPE_IDAT = type("IDAT");
    private static final int TYPE_IEND = type("IEND");
    private static final int TYPE_acTL = type("acTL");
    private static final int TYPE_fcTL = type("fcTL");
    private static final int TYPE_fdAT = type("fdAT");

    private static final byte[] PNG_SIG = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};

    @Override
    public List<DecodedFrame> decode(File sourceFile) throws EmojiBuildException
    {
        try
        {
            byte[] data = readAllBytes(sourceFile);
            validatePng(data, sourceFile.getName());

            if (!hasAcTL(data))
                return singleFrameFallback(sourceFile, sourceFile.getName());

            return decodeApng(data, sourceFile.getName());
        } catch (EmojiBuildException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new EmojiBuildException("Failed to decode APNG: " + sourceFile.getName(), e);
        }
    }


    private List<DecodedFrame> decodeApng(byte[] data, String name) throws IOException, EmojiBuildException
    {
        int canvasW = readBE32(data, 16);
        int canvasH = readBE32(data, 20);

        List<PngChunk> chunks = collectChunks(data);

        List<PngChunk> sharedChunks = new ArrayList<>();
        for (PngChunk c : chunks)
        {
            if (c.type == TYPE_IHDR
                    || c.type == type("PLTE")
                    || c.type == type("tRNS")
                    || c.type == type("gAMA"))
                sharedChunks.add(c);
        }

        BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
        List<DecodedFrame> frames = new ArrayList<>();

        FcTLData currentFctl = null;

        for (PngChunk chunk : chunks)
        {
            if (chunk.type == TYPE_fcTL)
            {
                currentFctl = parseFcTL(chunk.data);
            } else if (chunk.type == TYPE_IDAT && currentFctl == null)
            {
                BufferedImage frame = decodeChunksAsPng(sharedChunks, List.of(chunk), canvasW, canvasH, data);
                if (frame != null)
                {
                    Graphics2D g = canvas.createGraphics();
                    g.setComposite(AlphaComposite.Src);
                    g.drawImage(frame, 0, 0, null);
                    g.dispose();
                }
            } else if (chunk.type == TYPE_fdAT && currentFctl != null)
            {
                byte[] idatData = new byte[chunk.data.length - 4];
                System.arraycopy(chunk.data, 4, idatData, 0, idatData.length);

                PngChunk fakeIdat = new PngChunk(TYPE_IDAT, idatData);
                BufferedImage frame = decodeChunksAsPng(
                        sharedChunks, List.of(fakeIdat),
                        currentFctl.w, currentFctl.h, data);

                if (frame != null)
                {
                    applyFrame(canvas, frame, currentFctl);
                    frames.add(new DecodedFrame(copyImage(canvas), currentFctl.delayMs));
                    applyDispose(canvas, currentFctl);
                }
                currentFctl = null;
            }
        }

        if (frames.isEmpty())
        {
            return singleFrameFallback(null, name);
        }

        return frames;
    }

    private void applyFrame(BufferedImage canvas, BufferedImage frame, FcTLData fctl)
    {
        Graphics2D g = canvas.createGraphics();
        if (fctl.blendOp == 0)
            g.setComposite(AlphaComposite.Src);
        else
            g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(frame, fctl.x, fctl.y, null);
        g.dispose();
    }

    private void applyDispose(BufferedImage canvas, FcTLData fctl)
    {
        if (fctl.disposeOp == 1)
        {
            Graphics2D g = canvas.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(fctl.x, fctl.y, fctl.w, fctl.h);
            g.dispose();
        }
    }

    private BufferedImage decodeChunksAsPng(List<PngChunk> shared, List<PngChunk> frameChunks,
                                            int w, int h, byte[] originalData) throws IOException
    {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        baos.write(PNG_SIG);

        PngChunk ihdr = null;
        for (PngChunk c : shared)
            if (c.type == TYPE_IHDR)
            {
                ihdr = c;
                break;
            }
        if (ihdr != null)
        {
            byte[] ihdrData = ihdr.data.clone();
            writeBE32(ihdrData, 0, w);
            writeBE32(ihdrData, 4, h);
            writeChunk(baos, TYPE_IHDR, ihdrData);
        }

        for (PngChunk c : shared)
            if (c.type != TYPE_IHDR)
                writeChunk(baos, c.type, c.data);

        for (PngChunk c : frameChunks)
            writeChunk(baos, TYPE_IDAT, c.data);

        writeChunk(baos, TYPE_IEND, new byte[0]);

        return ImageIO.read(new java.io.ByteArrayInputStream(baos.toByteArray()));
    }

    private List<DecodedFrame> singleFrameFallback(File sourceFile, String name) throws IOException, EmojiBuildException
    {
        BufferedImage img = sourceFile != null
                ? ImageIO.read(sourceFile)
                : null;
        if (img == null)
            throw new EmojiBuildException("Could not decode PNG as single frame: " + name);
        return List.of(new DecodedFrame(ensureArgb(img), DEFAULT_DELAY_MS));
    }



    private List<PngChunk> collectChunks(byte[] data)
    {
        List<PngChunk> list = new ArrayList<>();
        int pos = 8; // skip PNG signature
        while (pos + 12 <= data.length)
        {
            int len = readBE32(data, pos);
            int type = readBE32(data, pos + 4);
            byte[] chunkData = new byte[Math.max(0, len)];
            if (len > 0)
                System.arraycopy(data, pos + 8, chunkData, 0, len);
            list.add(new PngChunk(type, chunkData));
            pos += 12 + len; // length + type + data + CRC
            if (type == TYPE_IEND) break;
        }
        return list;
    }

    private FcTLData parseFcTL(byte[] d)
    {
        FcTLData f = new FcTLData();
        f.w = readBE32(d, 4);
        f.h = readBE32(d, 8);
        f.x = readBE32(d, 12);
        f.y = readBE32(d, 16);
        int delayNum = readBE16(d, 20);
        int delayDen = readBE16(d, 22);
        f.disposeOp = d[24] & 0xFF;
        f.blendOp = d[25] & 0xFF;

        if (delayDen == 0) delayDen = 100;
        f.delayMs = Math.max(10, (int) ((delayNum * 1000L) / delayDen));
        return f;
    }

    private boolean hasAcTL(byte[] data)
    {
        int pos = 8;
        while (pos + 12 <= data.length)
        {
            int len = readBE32(data, pos);
            int type = readBE32(data, pos + 4);
            if (type == TYPE_acTL) return true;
            if (type == TYPE_IDAT || type == TYPE_IEND) break;
            pos += 12 + len;
        }
        return false;
    }


    private void writeChunk(java.io.OutputStream out, int type, byte[] data) throws IOException
    {
        byte[] lenBytes = new byte[4];
        writeBE32(lenBytes, 0, data.length);
        out.write(lenBytes);

        byte[] typeBytes = new byte[4];
        writeBE32(typeBytes, 0, type);
        out.write(typeBytes);

        out.write(data);

        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(typeBytes);
        crc.update(data);
        byte[] crcBytes = new byte[4];
        writeBE32(crcBytes, 0, (int) crc.getValue());
        out.write(crcBytes);
    }



    private static void validatePng(byte[] data, String name) throws EmojiBuildException
    {
        if (data.length < 8)
            throw new EmojiBuildException("File too short to be a PNG: " + name);
        for (int i = 0; i < 8; i++)
            if (data[i] != PNG_SIG[i])
                throw new EmojiBuildException("Not a valid PNG file: " + name);
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

    private static int readBE32(byte[] b, int off)
    {
        return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16)
                | ((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
    }

    private static int readBE16(byte[] b, int off)
    {
        return ((b[off] & 0xFF) << 8) | (b[off + 1] & 0xFF);
    }

    private static void writeBE32(byte[] b, int off, int val)
    {
        b[off] = (byte) (val >> 24);
        b[off + 1] = (byte) (val >> 16);
        b[off + 2] = (byte) (val >> 8);
        b[off + 3] = (byte) val;
    }

    private static int type(String s)
    {
        return ((s.charAt(0) & 0xFF) << 24) | ((s.charAt(1) & 0xFF) << 16)
                | ((s.charAt(2) & 0xFF) << 8) | (s.charAt(3) & 0xFF);
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
        if (img.getType() == BufferedImage.TYPE_INT_ARGB) return img;
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return out;
    }



    private static class PngChunk
    {
        final int type;
        final byte[] data;

        PngChunk(int type, byte[] data)
        {
            this.type = type;
            this.data = data;
        }
    }

    private static class FcTLData
    {
        int w, h, x, y;
        int delayMs = DEFAULT_DELAY_MS;
        int disposeOp = 0;
        int blendOp = 0;
    }
}
