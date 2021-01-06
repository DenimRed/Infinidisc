package denimred.infinidisc.audio;

import denimred.infinidisc.config.InfinidiscConfig;
import net.minecraft.client.audio.OggAudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

public class MonoOggAudioStream extends OggAudioStream {
    private final AudioFormat monoFormat;

    public MonoOggAudioStream(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public MonoOggAudioStream(InputStream oggInputStream) throws IOException {
        super(oggInputStream);
        final AudioFormat format = super.getAudioFormat();
        this.monoFormat = format.getChannels() == 1 ? format : new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), 1, format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED, format.isBigEndian());
    }

    @Override
    protected void copyFromDualChannels(FloatBuffer one, FloatBuffer two, OggAudioStream.Buffer buffer) {
        if (InfinidiscConfig.CLIENT.global.get()) {
            super.copyFromDualChannels(one, two, buffer);
        } else {
            while (one.hasRemaining() && two.hasRemaining()) {
                buffer.appendOggAudioBytes((one.get() + two.get()) / 2.0F);
            }
        }
    }

    @Override
    public AudioFormat getAudioFormat() {
        if (InfinidiscConfig.CLIENT.global.get()) {
            return super.getAudioFormat();
        } else {
            return monoFormat;
        }
    }
}
