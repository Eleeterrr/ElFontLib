package eleeter.elfontlib.emoji.animated;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class AnimatedEmojiAtlasDataJsonParser
{
    private static final Gson GSON = new Gson();

    public static AnimatedEmojiAtlasData load(String filePath) throws IOException
    {
        try (Reader reader = new FileReader(filePath))
        {
            return GSON.fromJson(reader, AnimatedEmojiAtlasData.class);
        }
    }
}
