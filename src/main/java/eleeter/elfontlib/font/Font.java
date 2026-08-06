package eleeter.elfontlib.font;


public interface Font
{

    /**
     * The unicode character ID.
     */
    GlyphMetrics getGlyph(int id);

    /**
     * return The standard line height in the font's native units.
     */
    float getLineHeight();

    /**
     * return The typographic baseline distance from the top, in the font's native units.
     */
    float getBaseline();

    /**
     * return The native size (e.g., pt or px size) the font was generated at.
     */
    float getNativeSize();
}
