package denimred.infinidisc.mixin;

import com.google.common.collect.Multimap;
import denimred.infinidisc.audio.InfinidiscDisc;
import net.minecraft.client.audio.*;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private boolean loaded;
    @Shadow
    private int ticks;
    @Shadow
    @Final
    private Listener listener;
    @Shadow
    @Final
    private ChannelManager channelManager;
    @Shadow
    @Final
    private Map<ISound, ChannelManager.Entry> playingSoundsChannel;
    @Shadow
    @Final
    private Multimap<SoundCategory, ISound> categorySounds;
    @Shadow
    @Final
    private Map<ISound, Integer> playingSoundsStopTime;
    @Shadow
    @Final
    private List<ISoundEventListener> listeners;
    @Shadow
    @Final
    private List<ITickableSound> tickableSounds;

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    public void injectPlay(ISound sound, CallbackInfo ci) {
        if (sound instanceof InfinidiscDisc.PlayingSound) {
            try {
                this.playInfinidiscSound((InfinidiscDisc.PlayingSound) sound);
                ci.cancel();
            } catch (Throwable t) {
                LOGGER.error("Error playing Infinidisc sound", t);
            }
        }
    }

    private void playInfinidiscSound(@Nullable final InfinidiscDisc.PlayingSound sound) {
        if (this.loaded && sound != null && sound.shouldPlaySound()) {
            final SoundEventAccessor accessor = sound.createAccessor(null);
            if (accessor != null) {
                float attenuation = Math.max(sound.getVolume(), 1.0F) * sound.getAttenuationDistance();
                final SoundCategory category = sound.getCategory();
                float volume = this.callGetClampedVolume(sound);
                float pitch = this.callGetClampedPitch(sound);
                final ISound.AttenuationType type = sound.getAttenuationType();
                final boolean isGlobal = sound.isGlobal();
                if (volume != 0.0F || sound.canBeSilent()) {
                    final Vector3d pos = new Vector3d(sound.getX(), sound.getY(), sound.getZ());
                    if (!this.listeners.isEmpty()) {
                        final boolean canHear = isGlobal || type == ISound.AttenuationType.NONE || this.listener.getClientLocation().squareDistanceTo(pos) < (double) (attenuation * attenuation);
                        if (canHear) {
                            for (final ISoundEventListener eventListener : this.listeners) {
                                eventListener.onPlaySound(sound, accessor);
                            }
                        }
                    }

                    if (this.listener.getGain() > 0.0F) {
                        final ChannelManager.Entry entry = this.channelManager.requestSoundEntry(SoundSystem.Mode.STREAMING).join();
                        if (entry == null) {
                            LOGGER.warn("Failed to create new sound handle");
                        } else {
                            this.playingSoundsStopTime.put(sound, this.ticks + 20);
                            this.playingSoundsChannel.put(sound, entry);
                            this.categorySounds.put(category, sound);
                            entry.runOnSoundExecutor((source) -> {
                                source.setPitch(pitch);
                                source.setGain(volume);
                                if (!isGlobal && type == ISound.AttenuationType.LINEAR) {
                                    source.setLinearAttenuation(attenuation);
                                } else {
                                    source.setNoAttenuation();
                                }

                                source.setLooping(false);
                                source.updateSource(pos);
                                source.setRelative(isGlobal);

                                source.playStreamableSounds(sound.getAudioStream());
                                source.play();
                            });

                            if (sound instanceof ITickableSound) {
                                this.tickableSounds.add((ITickableSound) sound);
                            }
                        }
                    }
                }
            }
        }
    }

    @Invoker
    abstract public float callGetClampedPitch(ISound p_sound);

    @Invoker
    abstract public float callGetClampedVolume(ISound p_sound);
}
