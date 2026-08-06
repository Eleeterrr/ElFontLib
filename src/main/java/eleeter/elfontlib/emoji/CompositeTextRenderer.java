package eleeter.elfontlib.emoji;

import eleeter.elfontlib.font.Font;
import eleeter.elfontlib.render.MeshData;
import eleeter.elfontlib.render.TextMeshGenerator;
import eleeter.elfontlib.shaping.SimpleTextShaper;
import eleeter.elfontlib.shaping.TextLayout;
import java.util.ArrayList;
import java.util.List;

public class CompositeTextRenderer
{
    private final Font textFont;
    private final EmojiFont emojiFont;
    private final SimpleTextShaper textShaper = new SimpleTextShaper();
    private final EmojiShaper emojiShaper = new EmojiShaper();
    private final EmojiTextScanner scanner;

    public CompositeTextRenderer(Font textFont, EmojiFont emojiFont)
    {
        this.textFont = textFont;
        this.emojiFont = emojiFont;
        this.scanner = new EmojiTextScanner(emojiFont);
    }

    public CompositeMesh render(String text, float fontSize)
    {
        List<TextSegment> segments = scanner.scan(text);

        StringBuilder textOnly = new StringBuilder();
        List<Integer> insertionPoints = new ArrayList<>();
        List<TextSegment> orderedEmoji = new ArrayList<>();

        for (TextSegment seg : segments)
        {
            if (seg.isText())
            {
                textOnly.append(seg.asText().content);
            } else
            {
                insertionPoints.add(textOnly.length());
                orderedEmoji.add(seg);
            }
        }

        TextLayout textLayout = textShaper.shape(textOnly.toString(), textFont, fontSize);

        float lineHeight = textFont.getLineHeight() * fontSize;
        List<float[]> emojiPositions = resolveEmojiPositions(
                textOnly.toString(), textFont, fontSize, lineHeight, insertionPoints);

        List<PositionedEmoji> allPositioned = new ArrayList<>();
        for (int i = 0; i < orderedEmoji.size(); i++)
        {
            float[] pos = emojiPositions.get(i);
            EmojiLayout el = emojiShaper.shape(
                    List.of(orderedEmoji.get(i)), fontSize, lineHeight, pos[0], pos[1]);
            allPositioned.addAll(el.getEmojis());
        }

        EmojiLayout emojiLayout = new EmojiLayout(allPositioned, 0f, 0f);

        MeshData textMesh = TextMeshGenerator.generate(textLayout, textFont);
        MeshData emojiMesh = EmojiMeshGenerator.generate(emojiLayout);

        return new CompositeMesh(textMesh, emojiMesh);
    }

    private List<float[]> resolveEmojiPositions(String text, Font font, float fontSize,
                                                float lineHeight, List<Integer> insertionPoints)
    {
        List<float[]> positions = new ArrayList<>(insertionPoints.size());
        if (insertionPoints.isEmpty())
        {
            return positions;
        }

        float cursorX = 0f;
        float cursorY = 0f;
        int charIdx = 0;
        int nextSlot = 0;

        for (int i = 0; i < text.length() && nextSlot < insertionPoints.size(); )
        {
            while (nextSlot < insertionPoints.size()
                    && insertionPoints.get(nextSlot) == charIdx)
            {
                positions.add(new float[]{cursorX, cursorY});
                nextSlot++;
            }

            int cp = text.codePointAt(i);
            int cpLen = Character.charCount(cp);

            if (cp == '\n')
            {
                cursorX = 0f;
                cursorY -= Math.round(lineHeight);
            } else
            {
                var metrics = font.getGlyph(cp);
                if (metrics != null)
                {
                    cursorX += metrics.getXAdvance() * fontSize;
                }

            }

            i += cpLen;
            charIdx += cpLen;
        }

        while (nextSlot < insertionPoints.size())
        {
            positions.add(new float[]{cursorX, cursorY});
            nextSlot++;
        }

        return positions;
    }
}
