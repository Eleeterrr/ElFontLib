package eleeter.elfontlib.emoji.animated;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eleeter.elfontlib.emoji.build.EmojiBuildException;
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

public class AnimatedBuildManifest
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

    public static String computeHash(List<AnimatedEmojiSource> sources)
    {
        StringBuilder sb = new StringBuilder();
        sources.stream()
                .sorted((a, b) -> a.sourceFile.getName().compareTo(b.sourceFile.getName()))
                .forEach(s -> sb.append(s.sourceFile.getName())
                        .append(':')
                        .append(s.sourceFile.lastModified())
                        .append(':')
                        .append(s.sourceFile.length())
                        .append('\n'));

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e)
        {
            throw new EmojiBuildException("SHA-256 not available", e);
        }
    }

    public static AnimatedBuildManifest fromSources(List<AnimatedEmojiSource> sources)
    {
        AnimatedBuildManifest m = new AnimatedBuildManifest();
        m.combinedHash = computeHash(sources);
        for (AnimatedEmojiSource s : sources)
            m.files.put(s.sourceFile.getName(), new FileSnapshot(s.sourceFile.lastModified(), s.sourceFile.length()));
        return m;
    }

    public static AnimatedBuildManifest load(File file) throws IOException
    {
        try (Reader reader = new FileReader(file))
        {
            return GSON.fromJson(reader, AnimatedBuildManifest.class);
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
