package eleeter.elfontlib.emoji.animated;

import java.util.List;

public class AnimatedEmojiLayout
{
    private final List<PositionedAnimatedEmoji> emojis;
    private final float width;
    private final float height;

    public AnimatedEmojiLayout(List<PositionedAnimatedEmoji> emojis, float width, float height)
    {
        this.emojis = emojis;
        this.width = width;
        this.height = height;
    }

    public List<PositionedAnimatedEmoji> getEmojis()
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
