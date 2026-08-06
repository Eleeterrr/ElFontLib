package eleeter.elfontlib.shaping;

import eleeter.elfontlib.font.Font;
import eleeter.elfontlib.font.GlyphMetrics;

import java.util.ArrayList;
import java.util.List;

public class SimpleTextShaper implements TextShaper
{
    @Override
    public TextLayout shape(String text, Font font, float fontSize)
    {
        List<PositionedGlyph> positionedGlyphs = new ArrayList<>(text.length());
        float scale = fontSize;
        
        float cursorX = 0.0f;
        float cursorY = 0.0f;
        float lineHeight = font.getLineHeight() * scale;
        
        float maxWidth = 0.0f;
        float minHeight = 0.0f;

        for (int i = 0; i < text.length(); i++)
        {
            int c = text.codePointAt(i);
            if (Character.isSupplementaryCodePoint(c)) i++;

            if (c == '\n')
            {
                cursorX = 0.0f;
                cursorY -= Math.round(lineHeight);
                minHeight = Math.min(minHeight, cursorY);
                continue;
            }

            GlyphMetrics metrics = font.getGlyph(c);
            if (metrics == null) continue;

            if (metrics.getWidth() > 0 || metrics.getHeight() > 0)
            {
                positionedGlyphs.add(new PositionedGlyph(metrics, Math.round(cursorX), Math.round(cursorY), scale));
            }

            cursorX += (metrics.getXAdvance() * scale);
            maxWidth = Math.max(maxWidth, cursorX);
        }

        float totalHeight = Math.abs(minHeight) + lineHeight;
        return new TextLayout(positionedGlyphs, maxWidth, totalHeight);
    }
}
