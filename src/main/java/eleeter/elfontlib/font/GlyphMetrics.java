package eleeter.elfontlib.font;


public class GlyphMetrics
{
    private final int id;
    private final float width;
    private final float height;
    private final float xOffset;
    private final float yOffset;
    private final float xAdvance;
    
    private final float u0;
    private final float v0;
    private final float u1;
    private final float v1;

    public GlyphMetrics(int id, float width, float height, float xOffset, float yOffset, float xAdvance, float u0, float v0, float u1, float v1) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xAdvance = xAdvance;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    public int getId()
    {
        return this.id;
    }
    public float getWidth()
    {
        return this.width;
    }
    public float getHeight()
    {
        return this.height;
    }
    public float getXOffset()
    {
        return this.xOffset;
    }
    public float getYOffset()
    {
        return this.yOffset;
    }
    public float getXAdvance()
    {
        return this.xAdvance;
    }
    public float getU0()
    {
        return this.u0;
    }
    public float getV0()
    {
        return this.v0;
    }
    public float getU1()
    {
        return this.u1;
    }
    public float getV1()
    {
        return this.v1;
    }
}
