package eleeter.elfontlib.emoji.build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class BuildManifest
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String combinedHash;
    public Map<String, FileSnapshot> files = new LinkedHashMap<>();

    public static class FileSnapshot
    {
        public long lastModified;
        public long size;

        FileSnapshot(long lastModified, long size)
        {
            this.lastModified = lastModified;
            this.size = size;
        }
    }

    public static String computeHash(List<SvgEmojiSource> sources)
    {
        StringBuilder sb = new StringBuilder();
        sources.stream()
                .sorted((a, b) -> a.svgFile.getName().compareTo(b.svgFile.getName()))
                .forEach(s -> sb.append(s.svgFile.getName())
                        .append(':')
                        .append(s.svgFile.lastModified())
                        .append(':')
                        .append(s.svgFile.length())
                        .append('\n'));

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes)
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e)
        {
            throw new EmojiBuildException("SHA-256 not available on this JVM", e);
        }
    }

    public static BuildManifest fromSources(List<SvgEmojiSource> sources)
    {
        BuildManifest m = new BuildManifest();
        m.combinedHash = computeHash(sources);
        for (SvgEmojiSource s : sources)
            m.files.put(s.svgFile.getName(), new FileSnapshot(s.svgFile.lastModified(), s.svgFile.length()));
        return m;
    }

    public static BuildManifest load(File file) throws IOException
    {
        try (Reader reader = new FileReader(file))
        {
            return GSON.fromJson(reader, BuildManifest.class);
        }
    }

    public void save(File file) throws IOException
    {
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file))
        {
            GSON.toJson(this, writer);
        }
    }
}
