package denimred.infinidisc.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static denimred.infinidisc.Infinidisc.MOD_ID;

@SuppressWarnings("deprecation")
public class AnvilScreenOverlay {
    public static final ResourceLocation ANVIL_OVERLAY_TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/container/anvil_overlay.png");
    private static ITextComponent title;
    private static Button button;

    public static Button createOverlayButton(AnvilScreen gui) {
        final int x = (gui.width - gui.getXSize()) / 2;
        final int y = (gui.height - gui.getYSize()) / 2;
        final Button button = new Button(x + 60, y + 4, 93, 14, new TranslationTextComponent(MOD_ID.concat(".anvil_overlay.button")), b -> {
        }, (b, matrixStack, mouseX, mouseY) -> gui.renderTooltip(matrixStack, new TranslationTextComponent(MOD_ID.concat(".anvil_overlay.button.tooltip")), mouseX, mouseY)) {
            @Override
            public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
                Minecraft mc = Minecraft.getInstance();
                mc.getTextureManager().bindTexture(ANVIL_OVERLAY_TEXTURE);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                final boolean hovered = this.isHovered();
                blit(matrixStack, this.x, this.y, 0, 16 + (hovered ? 14 : 0), this.width, this.height, 128, 128);
                drawCenteredString(matrixStack, mc.fontRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, this.getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24);
                if (hovered) {
                    this.renderToolTip(matrixStack, mouseX, mouseY);
                }
            }
        };
        button.visible = false;
        AnvilScreenOverlay.button = button;
        return button;
    }

    public static void renderOverlay(AnvilScreen gui, MatrixStack matrixStack) {
        final ItemStack stack;
        final RepairContainer container = gui.getContainer();
        final ItemStack in1 = container.getSlot(0).getStack();
        final ItemStack in2 = container.getSlot(1).getStack();
        final ItemStack out = container.getSlot(2).getStack();
        if (out.getItem() instanceof MusicDiscItem) {
            stack = out;
        } else if (in1.getItem() instanceof MusicDiscItem) {
            stack = in1;
        } else if (!in1.isEmpty() && in2.getItem() instanceof MusicDiscItem) {
            stack = in2;
        } else {
            stack = null;
        }
        if (stack != null) {
            if (button != null && !button.visible) {
                button.visible = true;
            }
            if (gui.title != StringTextComponent.EMPTY) {
                title = gui.title;
                gui.title = StringTextComponent.EMPTY;
            }
            final Minecraft mc = gui.getMinecraft();
            mc.getTextureManager().bindTexture(ANVIL_OVERLAY_TEXTURE);
            final int x = (gui.width - gui.getXSize()) / 2;
            final int y = (gui.height - gui.getYSize()) / 2;
            AbstractGui.blit(matrixStack, x + 17, y + 6, 0, 44, 32, 32, 128, 128);
            AbstractGui.blit(matrixStack, x + 59, y + 3, 0, 0, 107, 16, 128, 128);
            RenderSystem.pushMatrix();
            final int scale = 2;
            RenderSystem.scaled(scale, scale, scale);
            mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, (x + 17) / scale, (y + 6) / scale);
            RenderSystem.popMatrix();
        } else {
            if (button != null && button.visible) {
                button.visible = false;
            }
            if (gui.title == StringTextComponent.EMPTY && title != null) {
                gui.title = title;
            }
        }
    }
}
