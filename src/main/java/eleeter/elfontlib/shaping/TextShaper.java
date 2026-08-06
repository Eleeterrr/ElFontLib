package eleeter.elfontlib.shaping;

import eleeter.elfontlib.font.Font;


public interface TextShaper
{
    TextLayout shape(String text, Font font, float fontSize);
}
