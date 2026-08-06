package eleeter.elfontlib.font.msdf;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


public class MsdfJsonParser
{

    private static final Gson GSON = new Gson();


    public static MsdfFontData loadFontData(String filePath) throws IOException
    {
        try (Reader reader = new FileReader(filePath))
        {
            return GSON.fromJson(reader, MsdfFontData.class);
        }
    }
}
