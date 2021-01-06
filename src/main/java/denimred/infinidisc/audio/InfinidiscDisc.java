package denimred.infinidisc.audio;

import denimred.infinidisc.Infinidisc;
import denimred.infinidisc.config.InfinidiscConfig;
import denimred.infinidisc.util.DiscHelper;
import denimred.infinidisc.util.FileType;
import net.minecraft.client.audio.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class InfinidiscDisc extends Sound {
    private final File file;
    private final String hash;
    private final FileType fileType;
    private final ResourceLocation name;
    private final ITextComponent description;
    private final DummyAccessor dummyAccessor = new DummyAccessor();

    private InfinidiscDisc(File file, String hash, FileType fileType) {
        super(hash, 0.0F, 0.0F, 1, Type.FILE, true, false, 0);
        this.file = file;
        this.hash = hash;
        this.fileType = fileType;
        this.name = new ResourceLocation(Infinidisc.MOD_ID, "disc_" + hash);
        this.description = new TranslationTextComponent(Infinidisc.MOD_ID.concat(".now_playing"), hash);
    }

    public static InfinidiscDisc fromFile(File file) {
        final FileType fileType = FileType.fromFile(file);
        if (fileType != null && DiscHelper.SUPPORTED_FILE_TYPES.contains(fileType)) {
            final String hash = DiscHelper.hashFile(file);
            final InfinidiscDisc existing = DiscHelper.getDisc(hash);
            if (existing != null) {
                return existing;
            } else {
                final InfinidiscDisc disc = new InfinidiscDisc(file, hash, fileType);
                DiscHelper.addDisc(disc);
                return disc;
            }
        }
        throw new IllegalArgumentException("File is invalid or unsupported");
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return name;
    }

    @Override
    public ResourceLocation getSoundAsOggLocation() {
        return new ResourceLocation(name.getNamespace(), "sounds/" + name.getPath() + ".ogg");
    }

    @Override
    public float getVolume() {
        return 4.0F;
    }

    @Override
    public float getPitch() {
        return 1.0F;
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public Sound cloneEntry() {
        return this;
    }

    @Override
    public void enqueuePreload(SoundEngine engine) {
        // no-op
    }

    @Override
    public Type getType() {
        return Type.FILE;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public boolean shouldPreload() {
        return false;
    }

    @Override
    public int getAttenuationDistance() {
        return 16;
    }

    public File getFile() {
        return file;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getHash() {
        return hash;
    }

    public ITextComponent getDescription() {
        return description;
    }

    private class DummyAccessor extends SoundEventAccessor {
        public DummyAccessor() {
            super(name, null);
            this.addSound(InfinidiscDisc.this);
        }
    }

    public class PlayingSound implements ISound {
        private final double x;
        private final double y;
        private final double z;
        private final IAudioStream audioStream;

        public PlayingSound(BlockPos pos) throws IOException {
            this(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        public PlayingSound(double x, double y, double z) throws IOException {
            this.x = x;
            this.y = y;
            this.z = z;
            this.audioStream = fileType.loadAudioStream(file);
        }

        @Override
        public ResourceLocation getSoundLocation() {
            return InfinidiscDisc.this.getSoundLocation();
        }

        @Nullable
        @Override
        public SoundEventAccessor createAccessor(@Nullable SoundHandler handler) {
            return dummyAccessor;
        }

        @Override
        public Sound getSound() {
            return InfinidiscDisc.this;
        }

        @Override
        public SoundCategory getCategory() {
            return SoundCategory.RECORDS;
        }

        @Override
        public boolean canRepeat() {
            return false; // TODO: This is going to more work than just adding a config option
        }

        @Override
        public int getRepeatDelay() {
            return 0; // TODO: This is going to more work than just adding a config option
        }

        @Override
        public boolean isGlobal() {
            return InfinidiscConfig.CLIENT.global.get();
        }

        @Override
        public float getVolume() {
            return InfinidiscDisc.this.getVolume();
        }

        @Override
        public float getPitch() {
            return InfinidiscDisc.this.getPitch();
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public double getZ() {
            return z;
        }

        @Override
        public AttenuationType getAttenuationType() {
            return AttenuationType.LINEAR;
        }

        public ITextComponent getDescription() {
            return InfinidiscDisc.this.getDescription();
        }

        public float getAttenuationDistance() {
            return InfinidiscDisc.this.getAttenuationDistance();
        }

        public IAudioStream getAudioStream() {
            return audioStream;
        }
    }
}
