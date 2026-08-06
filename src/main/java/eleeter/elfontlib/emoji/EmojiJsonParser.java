package eleeter.elfontlib.emoji;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class EmojiJsonParser
{
    private static final Gson GSON = new Gson();

    public static EmojiAtlasData load(String filePath) throws IOException
    {
        try (Reader reader = new FileReader(filePath))
        {
            return GSON.fromJson(reader, EmojiAtlasData.class);
        }
    }
}
