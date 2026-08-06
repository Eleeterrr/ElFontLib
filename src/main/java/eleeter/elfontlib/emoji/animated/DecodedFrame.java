package eleeter.elfontlib.emoji.animated;

import java.awt.image.BufferedImage;

public class DecodedFrame
{
    /**
     * Full-colour RGBA bitmap for this frame.
     */
    public final BufferedImage image;

    /**
     * How long this frame should be displayed, in milliseconds.
     */
    public final int durationMillis;

    public DecodedFrame(BufferedImage image, int durationMillis)
    {
        if (image == null)
        {
            throw new IllegalArgumentException("DecodedFrame.image must not be null");
        }
        this.image = image;
        this.durationMillis = Math.max(1, durationMillis);
    }
}
