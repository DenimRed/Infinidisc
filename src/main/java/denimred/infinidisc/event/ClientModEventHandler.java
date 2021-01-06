package denimred.infinidisc.event;

import denimred.infinidisc.Infinidisc;
import denimred.infinidisc.util.DiscHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.Random;

import static denimred.infinidisc.Infinidisc.MOD_ID;

@Mod.EventBusSubscriber(modid = Infinidisc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventHandler {
    public static final ResourceLocation VALID_PROPERTY = new ResourceLocation(MOD_ID, "valid");

    @SubscribeEvent
    public static void onItemColor(final ColorHandlerEvent.Item event) {
        final ItemColors itemColors = event.getItemColors();
        ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item instanceof MusicDiscItem)
                .forEach(item -> {
                    ItemModelsProperties.registerProperty(item, VALID_PROPERTY, (stack, world, entity) -> {
                        final String hash = DiscHelper.getStackHash(stack);
                        if (hash != null) {
                            return !hash.isEmpty() && DiscHelper.getDisc(hash) == null ? 0.5F : 1.0F;
                        }
                        return 0.0F;
                    });
                    itemColors.register((stack, i) -> {
                        final String hash = DiscHelper.getStackHash(stack);
                        if (hash != null) {
                            if (hash.isEmpty()) {
                                final Minecraft mc = Minecraft.getInstance();
                                final ClientWorld world = mc.world;
                                final long ticks = world != null ? world.getGameTime() : 0;
                                final float time = MathHelper.lerp(mc.getRenderPartialTicks(), ticks - 1, ticks) * 0.02F;
                                if (i == 0) { // Disc
                                    return Color.HSBtoRGB(time, 0.5F, 1.0F);
                                } else if (i == 1 || i == 3) { // Ring
                                    return Color.HSBtoRGB(time, 0.5F, 1.0F);
                                } else if (i == 2) { // Center
                                    return Color.HSBtoRGB(time, 1.0F, 1.0F);
                                }
                            } else {
                                final int seed = hash.hashCode();
                                final Random rand = new Random(seed);
                                if (i == 0) { // Disc
                                    return Color.HSBtoRGB(rand.nextFloat(), 0.5F, 1.0F);
                                } else if (i == 1 || i == 3) { // Ring
                                    rand.setSeed(seed * 31L);
                                    return Color.HSBtoRGB(rand.nextFloat(), 0.6F, 0.5F);
                                } else if (i == 2) { // Center
                                    rand.setSeed(seed * 31L * 31L);
                                    return Color.HSBtoRGB(rand.nextFloat(), 1.0F, 1.0F);
                                }
                            }
                        }
                        return -1;
                    }, item);
                });
    }
}
