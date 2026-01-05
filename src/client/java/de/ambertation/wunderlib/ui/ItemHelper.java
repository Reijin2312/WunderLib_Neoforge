package de.ambertation.wunderlib.ui;

import de.ambertation.wunderlib.WunderLib;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.io.File;
import java.util.stream.Stream;
import net.neoforged.neoforge.client.ClientHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class ItemHelper {
    private ItemHelper() {
    }

    public static void renderAll(
            @NotNull Stream<Item> items,
            @NotNull File folder
    ) {
        renderAll(items, 8.f, folder);
    }

    public static void renderAll(
            @NotNull Stream<Item> items,
            float scale,
            @NotNull File folder
    ) {
        folder.mkdirs();
        items.forEach(item -> {
            var id = BuiltInRegistries.ITEM.getKey(item);
            File subFolder = new File(folder, id.getNamespace());
            subFolder.mkdirs();
            ItemStack stack = new ItemStack(item);
            var file = new File(subFolder, id.getPath() + ".png");
            renderToFile(stack, scale, file);
        });
    }

    public static void renderToFile(
            @NotNull ItemLike item,
            @NotNull File file
    ) {
        renderToFile(new ItemStack(item), null, 8.f, file);
    }

    public static void renderToFile(
            @NotNull ItemStack stack,
            @NotNull File file
    ) {
        renderToFile(stack, null, 8.f, file);
    }

    public static void renderToFile(
            @NotNull ItemStack stack,
            float scale,
            @NotNull File file
    ) {
        renderToFile(stack, null, scale, file);
    }

    public static void renderToFile(
            @NotNull ItemStack stack,
            @Nullable String overlayText,
            float scale,
            @NotNull File file
    ) {
        executeRender(stack, overlayText, scale, file);
    }

    private static void executeRender(ItemStack stack, String overlayText, float scale, File file) {
        // Calculate size based on scale - standard item is 16x16
        int size = (int) (16 * scale);

        // Create a render target for our item
        RenderTarget framebuffer = new TextureTarget(size, size, true, Minecraft.ON_OSX);
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();

        try {
            framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            framebuffer.clear(Minecraft.ON_OSX);
            framebuffer.bindWrite(true);
            renderItemToFramebuffer(stack, overlayText, scale, framebuffer);
            framebuffer.unbindWrite();
            mainTarget.bindWrite(true);
            writeFramebufferToFile(framebuffer, file);
        } finally {
            // Clean up the framebuffer
            framebuffer.destroyBuffers();
            mainTarget.bindWrite(true);
        }
    }

    private static void renderItemToFramebuffer(ItemStack stack, String text, float scale, RenderTarget framebuffer) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.assertOnRenderThreadOrInit();

        // Setup lighting for 3D items like in the GUI
        Lighting.setupFor3DItems();

        RenderSystem.backupProjectionMatrix();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        try {
            Matrix4f projection = new Matrix4f()
                    .setOrtho(
                            0.0F,
                            (float) framebuffer.viewWidth,
                            (float) framebuffer.viewHeight,
                            0.0F,
                            1000.0F,
                            ClientHooks.getGuiFarPlane()
                    );
            RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
            modelViewStack.translation(0.0F, 0.0F, 10000.0F - ClientHooks.getGuiFarPlane());
            RenderSystem.applyModelViewMatrix();

            // Create graphics context like in Gui rendering
            GuiGraphics guiGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());

            // Clear background to transparent
            guiGraphics.fill(0, 0, framebuffer.width, framebuffer.height, 0x00000000);

            // Apply scaling transformation
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1.0F);

            // Render the item at (0,0) - this will be scaled by our transformation
            guiGraphics.renderFakeItem(stack, 0, 0);

            // Render text overlay if needed (like count or custom text)
            if (stack.getCount() > 1 && text == null) text = String.valueOf(stack.getCount());
            if (text != null) {
                guiGraphics.renderItemDecorations(minecraft.font, stack, 0, 0, text);
            }

            // Restore transformation
            guiGraphics.pose().popPose();
            guiGraphics.flush();
        } finally {
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    /**
     * Write the framebuffer contents to a file using the Screenshot API
     */
    private static void writeFramebufferToFile(RenderTarget framebuffer, File file) {
        try {
            // Use the Screenshot API to capture the framebuffer contents
            var nativeImage = Screenshot.takeScreenshot(framebuffer);
            Util.ioPool().execute(() -> {
                try {
                    // The NativeImage already contains exactly what we rendered
                    nativeImage.writeToFile(file);
                    WunderLib.LOGGER.info("Successfully saved item render to: " + file.getAbsolutePath());
                } catch (Exception exception) {
                    WunderLib.LOGGER.warn("Couldn't save item render", exception);
                } finally {
                    nativeImage.close();
                }
            });
        } catch (Exception e) {
            WunderLib.LOGGER.error("Failed to capture item render", e);
        }
    }

    /**
     * Render an item within an existing GUI context (most reliable method)
     * Based on the renderSlot method from Gui class
     */
    public static void renderToExistingContext(
            GuiGraphics guiGraphics,
            ItemStack stack,
            @Nullable String overlayText,
            float scale,
            int x, int y
    ) {
        if (stack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);

        // Render the item using the same method as the hotbar
        guiGraphics.renderFakeItem(stack, 0, 0);

        // Render decorations (count, durability bar, cooldown overlay)
        String text = overlayText;
        if (stack.getCount() > 1 && text == null) text = String.valueOf(stack.getCount());
        if (text != null) {
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, 0, 0, text);
        }

        guiGraphics.pose().popPose();
    }

    /**
     * Alternative method that renders to a specific area within an existing framebuffer
     * Useful for creating item grids or inventories
     */
    public static void renderItemGrid(
            GuiGraphics guiGraphics,
            ItemStack[] items,
            int startX, int startY,
            int itemSize, int spacing,
            int columns
    ) {
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                int col = i % columns;
                int row = i / columns;
                int x = startX + col * (itemSize + spacing);
                int y = startY + row * (itemSize + spacing);

                float scale = itemSize / 16.0f; // 16 is the standard item size
                renderToExistingContext(guiGraphics, items[i], null, scale, x, y);
            }
        }
    }

    /**
     * Utility method to render a single item at standard size (16x16)
     */
    public static void renderStandardItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        renderToExistingContext(guiGraphics, stack, null, 1.0f, x, y);
    }
}
