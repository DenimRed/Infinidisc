package denimred.infinidisc.util;

import denimred.infinidisc.audio.MonoOggAudioStream;
import net.minecraft.client.audio.IAudioStream;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileType {
    public static final FileType OGG = new FileType("ogg", "audio/ogg", "*.ogg", MonoOggAudioStream::new);
    private static final Map<String, FileType> MIME_MAP = new HashMap<>();
    public final String name;
    public final String mime;
    public final String filter;
    private final IOFunction<File, IAudioStream> audioFileReader;

    private FileType(String name, String mime, String filter, IOFunction<File, IAudioStream> audioFileReader) {
        if (MIME_MAP.containsKey(mime)) {
            throw new IllegalArgumentException("Type with MIME " + mime + " already exists");
        }
        MIME_MAP.put(mime, this);

        this.name = name;
        this.mime = mime;
        this.filter = filter;
        this.audioFileReader = audioFileReader;
    }

    @Nullable
    public static FileType fromMime(String mime) {
        return MIME_MAP.get(mime);
    }

    @Nullable
    public static FileType fromFile(File file) {
        try {
            return fromMime(Files.probeContentType(file.toPath()));
        } catch (IOException e) {
            return null;
        }
    }

    public IAudioStream loadAudioStream(File file) throws IOException {
        return audioFileReader.apply(file);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final FileType other = (FileType) o;
            return name.equals(other.name) && mime.equals(other.mime) && filter.equals(other.filter);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, mime, filter);
    }
}
