package denimred.infinidisc.util;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import denimred.infinidisc.Infinidisc;
import denimred.infinidisc.audio.InfinidiscDisc;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DiscHelper {
    public static final ImmutableList<FileType> SUPPORTED_FILE_TYPES = ImmutableList.of(FileType.OGG);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern DISC_NAME_PATTERN = Pattern.compile("(?i)(?:\\Q" + Infinidisc.MOD_ID + "\\E(?:\\[(?<hash>.+?)])?)");
    private static final HashFunction HASH_FUNCTION = Hashing.murmur3_32(Infinidisc.MOD_ID.hashCode());
    private static final Map<String, InfinidiscDisc> DISCS = new HashMap<>();
    private static final Map<BlockPos, CompletableFuture<InfinidiscDisc.PlayingSound>> PREPARED_SOUNDS = new HashMap<>();

    public static void addDisc(InfinidiscDisc disc) {
        final String hash = disc.getHash();
        if (DISCS.containsKey(hash)) {
            throw new IllegalArgumentException("Disc already exists: " + hash);
        }
        DISCS.put(hash, disc);
    }

    @Nullable
    public static InfinidiscDisc getDisc(String hash) {
        return DISCS.get(hash);
    }

    public static void prepareSound(BlockPos pos, String hash) {
        PREPARED_SOUNDS.put(pos, CompletableFuture.supplyAsync(() -> {
            final InfinidiscDisc disc;
            if (!hash.isEmpty()) {
                disc = getDisc(hash);
            } else {
                disc = selectAudioFile().thenApply(InfinidiscDisc::fromFile).join();
            }
            if (disc != null) {
                try {
                    return disc.new PlayingSound(pos);
                } catch (IOException e) {
                    LOGGER.error("Failed to play Infinidisc sound", e);
                }
            }
            return null;
        }));
    }

    @Nullable
    public static CompletableFuture<InfinidiscDisc.PlayingSound> getPreparedSound(BlockPos pos) {
        return PREPARED_SOUNDS.get(pos);
    }

    public static void removePreparedSound(BlockPos pos) {
        PREPARED_SOUNDS.remove(pos);
    }

    public static String hashFile(File file) {
        try {
            return Integer.toUnsignedString(HASH_FUNCTION.hashBytes(Files.readAllBytes(file.toPath())).asInt(), 16).toLowerCase();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static String getStackHash(ItemStack stack) {
        final Matcher matcher = DISC_NAME_PATTERN.matcher(stack.getDisplayName().getUnformattedComponentText());
        if (matcher.find()) {
            final String hash = matcher.group("hash");
            return hash == null ? "" : hash.toLowerCase();
        }
        return null;
    }

    public static CompletableFuture<File> selectAudioFile() {
        return CompletableFuture.supplyAsync(() -> {
            final String rawPath = chooseAudioFiles(false);
            if (rawPath != null) {
                return new File(rawPath);
            }
            return null;
        });
    }

    public static CompletableFuture<List<File>> selectAudioFiles() {
        return CompletableFuture.supplyAsync(() -> {
            final String rawPaths = chooseAudioFiles(true);
            if (rawPaths != null) {
                return Arrays.stream(rawPaths.split("\\|")).map(File::new).collect(Collectors.toList());
            }
            return new ArrayList<>();
        });
    }

    /**
     * This will block the current thread until the dialog is closed.
     */
    @Nullable
    private static String chooseAudioFiles(boolean multiple) {
        final boolean pause = Minecraft.getInstance().gameSettings.pauseOnLostFocus;
        Minecraft.getInstance().gameSettings.pauseOnLostFocus = false;
        final MemoryStack stack = MemoryStack.stackGet();
        final int pointer = stack.getPointer();
        try {
            final PointerBuffer filterBuffer = BufferUtils.createPointerBuffer(SUPPORTED_FILE_TYPES.size());
            final StringBuilder filterDesc = new StringBuilder("Audio Files (");
            for (final FileType type : SUPPORTED_FILE_TYPES) {
                stack.nUTF8Safe(type.filter, true);
                filterBuffer.put(stack.getPointerAddress());
                filterDesc.append(type.filter).append(';');
            }
            filterBuffer.position(0);
            filterDesc.setCharAt(filterDesc.length() - 1, ')');
            return TinyFileDialogs.tinyfd_openFileDialog("Infinidisc: Choose Audio File(s)", null, filterBuffer, filterDesc, multiple);
        } catch (Throwable t) {
            LOGGER.error("Error choosing file(s)", t);
            return null;
        } finally {
            stack.setPointer(pointer);
            Minecraft.getInstance().gameSettings.pauseOnLostFocus = pause;
        }
    }
}
