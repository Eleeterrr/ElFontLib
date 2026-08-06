package eleeter.elfontlib.emoji.animated;

public abstract class AnimatedTextSegment
{
    private AnimatedTextSegment()
    {
    }

    public static AnimatedTextSegment text(String content)
    {
        return new Text(content);
    }

    public static AnimatedTextSegment animatedEmoji(AnimatedEmojiGlyph g)
    {
        return new AnimatedEmoji(g);
    }

    public boolean isText()
    {
        return this instanceof Text;
    }

    public boolean isAnimatedEmoji()
    {
        return this instanceof AnimatedEmoji;
    }

    public Text asText()
    {
        return (Text) this;
    }

    public AnimatedEmoji asAnimatedEmoji()
    {
        return (AnimatedEmoji) this;
    }

    public static final class Text extends AnimatedTextSegment
    {
        public final String content;

        Text(String content)
        {
            this.content = content;
        }
    }

    public static final class AnimatedEmoji extends AnimatedTextSegment
    {
        public final AnimatedEmojiGlyph glyph;

        AnimatedEmoji(AnimatedEmojiGlyph glyph)
        {
            this.glyph = glyph;
        }
    }
}
