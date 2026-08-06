package eleeter.elfontlib.emoji;

import java.util.List;

public class EmojiLayout
{
    private final List<PositionedEmoji> emojis;
    private final float width;
    private final float height;

    public EmojiLayout(List<PositionedEmoji> emojis, float width, float height)
    {
        this.emojis = emojis;
        this.width = width;
        this.height = height;
    }

    public List<PositionedEmoji> getEmojis()
    {
        return emojis;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }
}
