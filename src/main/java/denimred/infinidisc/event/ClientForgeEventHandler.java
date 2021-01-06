package denimred.infinidisc.event;

import denimred.infinidisc.gui.screen.inventory.AnvilScreenOverlay;
import denimred.infinidisc.util.DiscHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static denimred.infinidisc.Infinidisc.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGuiScreenInitPost(GuiScreenEvent.InitGuiEvent.Post event) {
        final Screen gui = event.getGui();
        if (gui instanceof AnvilScreen) {
            event.addWidget(AnvilScreenOverlay.createOverlayButton((AnvilScreen) gui));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGuiContainerDrawBackground(GuiContainerEvent.DrawBackground event) {
        final Screen gui = event.getGuiContainer();
        if (gui instanceof AnvilScreen) {
            AnvilScreenOverlay.renderOverlay((AnvilScreen) gui, event.getMatrixStack());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemTooltip(ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof MusicDiscItem) {
            final List<ITextComponent> toolTip = event.getToolTip();
            final String hash = DiscHelper.getStackHash(stack);
            if (hash != null) {
                if (toolTip.size() >= 2) {
                    toolTip.remove(1);
                }
                if (toolTip.size() >= 1) {
                    toolTip.remove(0);
                }

                if (!hash.isEmpty()) {
                    boolean valid = DiscHelper.getDisc(hash) != null;
                    toolTip.add(0, new TranslationTextComponent(MOD_ID.concat(".disc.song.hash"), hash).mergeStyle(TextFormatting.GRAY));
                    if (!valid) {
                        toolTip.add(1, new TranslationTextComponent(MOD_ID.concat(".file_not_found")).mergeStyle(TextFormatting.RED, TextFormatting.BOLD));
                    }
                    final IFormattableTextComponent title = new TranslationTextComponent(MOD_ID.concat(".disc.song")).mergeStyle(TextFormatting.GOLD);
                    toolTip.add(0, valid ? title : title.mergeStyle(TextFormatting.STRIKETHROUGH));
                } else {
                    toolTip.add(0, new TranslationTextComponent(MOD_ID.concat(".disc")).mergeStyle(TextFormatting.GOLD));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        final ItemStack stack = event.getPlayer().getHeldItem(event.getHand());
        if (stack.getItem() instanceof MusicDiscItem) {
            final World world = event.getWorld();
            if (world.isRemote) {
                final BlockPos pos = event.getPos();
                final BlockState state = world.getBlockState(pos);
                if (state.isIn(Blocks.JUKEBOX) && !state.get(JukeboxBlock.HAS_RECORD)) {
                    final String hash = DiscHelper.getStackHash(stack);
                    if (hash != null) {
                        DiscHelper.prepareSound(pos, hash);
                    }
                }
            }
        }
    }
}
