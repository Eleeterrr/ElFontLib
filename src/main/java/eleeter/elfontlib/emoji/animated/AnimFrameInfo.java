package eleeter.elfontlib.emoji.animated;


public class AnimFrameInfo
{
    /**
     * Left UV coordinate
     */
    public float u0;
    /**
     * Top UV coordinate
     */
    public float v0;
    /**
     * Right UV coordinate
     */
    public float u1;
    /**
     * Bottom UV coordinate
     */
    public float v1;

    /**
     * How long this frame should be visible in milliseconds.
     */
    public final int durationMillis;

    public AnimFrameInfo(float u0, float v0, float u1, float v1, int durationMillis)
    {
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.durationMillis = Math.max(1, durationMillis);
    }
}
