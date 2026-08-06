package eleeter.elfontlib.emoji;

public abstract class TextSegment
{
    private TextSegment()
    {
    }

    public static TextSegment text(String content)
    {
        return new Text(content);
    }

    public static TextSegment emoji(EmojiGlyph g)
    {
        return new Emoji(g);
    }

    public boolean isText()
    {
        return this instanceof Text;
    }

    public boolean isEmoji()
    {
        return this instanceof Emoji;
    }

    public Text asText()
    {
        return (Text) this;
    }

    public Emoji asEmoji()
    {
        return (Emoji) this;
    }

    public static final class Text extends TextSegment
    {
        public final String content;

        Text(String content)
        {
            this.content = content;
        }
    }

    public static final class Emoji extends TextSegment
    {
        public final EmojiGlyph glyph;

        Emoji(EmojiGlyph glyph)
        {
            this.glyph = glyph;
        }
    }
}
