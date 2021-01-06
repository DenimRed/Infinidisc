package denimred.infinidisc.mixin;

import denimred.infinidisc.audio.InfinidiscDisc;
import denimred.infinidisc.util.DiscHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private Map<BlockPos, ISound> mapSoundPositions;
    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private ClientWorld world;

    @Inject(method = "playRecord(Lnet/minecraft/util/SoundEvent;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/MusicDiscItem;)V", at = @At("HEAD"), cancellable = true, remap = false)
    public void injectPlayRecord(@Nullable SoundEvent soundEvent, BlockPos pos, @Nullable MusicDiscItem disc, CallbackInfo ci) {
        final CompletableFuture<InfinidiscDisc.PlayingSound> preparedSound = DiscHelper.getPreparedSound(pos);
        if (preparedSound != null) {
            preparedSound.thenAccept(sound -> {
                if (sound != null) {
                    if (soundEvent == null) {
                        this.mc.getSoundHandler().stop(sound);
                        DiscHelper.removePreparedSound(pos); // Need to remove here so that vanilla discs behave normally
                        this.mapSoundPositions.remove(pos);
                    } else {
                        if (disc != null) {
                            this.mc.ingameGUI.func_238451_a_(sound.getDescription());
                        }
                        this.mapSoundPositions.put(pos, sound);
                        this.mc.getSoundHandler().play(sound);
                    }
                    this.callSetPartying(this.world, pos, soundEvent != null);
                } else {
                    // Edge case where the sound failed to be prepared because the hash was invalid or the user canceled the file select
                    DiscHelper.removePreparedSound(pos);
                    this.callPlayRecord(soundEvent, pos, disc);
                }
            });
            ci.cancel();
        }
    }

    @Invoker
    public abstract void callSetPartying(World worldIn, BlockPos pos, boolean isPartying);

    @Invoker(remap = false)
    public abstract void callPlayRecord(@Nullable SoundEvent soundEvent, BlockPos pos, @Nullable MusicDiscItem disc);
}
