package de.ambertation.wunderlib.ui;

import de.ambertation.wunderlib.WunderLib;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GlyphRenderState;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.io.File;
import java.util.stream.Stream;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ItemHelper {
    private static @Nullable CachedOrthoProjectionMatrixBuffer ITEM_PROJECTION;
    private static @Nullable CachedOrthoProjectionMatrixBuffer GUI_PROJECTION;

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

        RenderSystem.assertOnRenderThread();

        // Create a render target for our item
        RenderTarget framebuffer = new TextureTarget("wunderlib_item", size, size, true);

        try {
            clearRenderTarget(framebuffer);
            renderItemToFramebuffer(stack, overlayText, scale, framebuffer);
            writeFramebufferToFile(framebuffer, file);
        } finally {
            framebuffer.destroyBuffers();
        }
    }

    private static void renderItemToFramebuffer(ItemStack stack, String text, float scale, RenderTarget framebuffer) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.assertOnRenderThread();

        RenderSystem.backupProjectionMatrix();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();

        var previousColorOverride = RenderSystem.outputColorTextureOverride;
        var previousDepthOverride = RenderSystem.outputDepthTextureOverride;
        var previousLights = RenderSystem.getShaderLights();

        try {
            RenderSystem.outputColorTextureOverride = framebuffer.getColorTextureView();
            RenderSystem.outputDepthTextureOverride = framebuffer.getDepthTextureView();

            RenderSystem.setProjectionMatrix(itemProjectionBuffer().getBuffer(framebuffer.width, framebuffer.height), ProjectionType.ORTHOGRAPHIC);

            TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
            minecraft.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.GUI, minecraft.level, null, 0);

            if (renderState.usesBlockLight()) {
                minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
            } else {
                minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
            }

            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();
            float size = framebuffer.width;
            poseStack.translate(size / 2.0F, size / 2.0F, 0.0F);
            poseStack.scale(size, -size, size);

            RenderSystem.enableScissorForRenderTypeDraws(0, framebuffer.height - (int) size, (int) size, (int) size);
            renderState.submit(poseStack, minecraft.gameRenderer.getSubmitNodeStorage(), 15728880, OverlayTexture.NO_OVERLAY, 0);
            minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
            minecraft.renderBuffers().bufferSource().endBatch();
            RenderSystem.disableScissorForRenderTypeDraws();
            poseStack.popPose();

            renderItemDecorationsToTarget(stack, text, scale, framebuffer);
        } finally {
            RenderSystem.outputColorTextureOverride = previousColorOverride;
            RenderSystem.outputDepthTextureOverride = previousDepthOverride;
            if (previousLights != null) {
                RenderSystem.setShaderLights(previousLights);
            }
            modelViewStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    /**
     * Write the framebuffer contents to a file using the Screenshot API
     */
    private static void writeFramebufferToFile(RenderTarget framebuffer, File file) {
        try {
            // Use the Screenshot API to capture the framebuffer contents
            Screenshot.takeScreenshot(framebuffer, nativeImage -> Util.ioPool().execute(() -> {
                try {
                    // The NativeImage already contains exactly what we rendered
                    nativeImage.writeToFile(file);
                    WunderLib.LOGGER.info("Successfully saved item render to: " + file.getAbsolutePath());
                } catch (Exception exception) {
                    WunderLib.LOGGER.warn("Couldn't save item render", exception);
                } finally {
                    nativeImage.close();
                }
            }));
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

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float) x, (float) y);
        guiGraphics.pose().scale(scale, scale);

        // Render the item using the same method as the hotbar
        guiGraphics.renderFakeItem(stack, 0, 0);

        // Render decorations (count, durability bar, cooldown overlay)
        String text = overlayText;
        if (stack.getCount() > 1 && text == null) text = String.valueOf(stack.getCount());
        if (text != null) {
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, 0, 0, text);
        }

        guiGraphics.pose().popMatrix();
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

    private static void clearRenderTarget(RenderTarget framebuffer) {
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorAndDepthTextures(framebuffer.getColorTexture(), 0, framebuffer.getDepthTexture(), 1.0);
    }

    private static CachedOrthoProjectionMatrixBuffer itemProjectionBuffer() {
        if (ITEM_PROJECTION == null) {
            ITEM_PROJECTION = new CachedOrthoProjectionMatrixBuffer("wunderlib_items", -1000.0F, 1000.0F, true);
        }
        return ITEM_PROJECTION;
    }

    private static CachedOrthoProjectionMatrixBuffer guiProjectionBuffer() {
        if (GUI_PROJECTION == null) {
            GUI_PROJECTION = new CachedOrthoProjectionMatrixBuffer("wunderlib_gui", 1000.0F, 11000.0F, true);
        }
        return GUI_PROJECTION;
    }

    private static void renderItemDecorationsToTarget(ItemStack stack, @Nullable String overlayText, float scale, RenderTarget framebuffer) {
        Minecraft minecraft = Minecraft.getInstance();

        GuiRenderState guiRenderState = new GuiRenderState();
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, guiRenderState, 0, 0);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);

        String text = overlayText;
        if (stack.getCount() > 1 && text == null) text = String.valueOf(stack.getCount());
        guiGraphics.renderItemDecorations(minecraft.font, stack, 0, 0, text);

        guiGraphics.pose().popMatrix();

        guiRenderState.forEachText(textState -> textState.ensurePrepared().visit(new Font.GlyphVisitor() {
            @Override
            public void acceptGlyph(TextRenderable.Styled renderable) {
                guiRenderState.submitGlyphToCurrentLayer(new GlyphRenderState(textState.pose, renderable, textState.scissor));
            }

            @Override
            public void acceptEffect(TextRenderable renderable) {
                guiRenderState.submitGlyphToCurrentLayer(new GlyphRenderState(textState.pose, renderable, textState.scissor));
            }
        }));

        RenderSystem.setProjectionMatrix(guiProjectionBuffer().getBuffer(framebuffer.width, framebuffer.height), ProjectionType.ORTHOGRAPHIC);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f().setTranslation(0.0F, 0.0F, -11000.0F), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

        guiRenderState.forEachElement(
                element -> drawGuiElement(framebuffer, element, dynamicTransforms),
                GuiRenderState.TraverseRange.ALL
        );
    }

    private static void drawGuiElement(RenderTarget framebuffer, GuiElementRenderState element, GpuBufferSlice dynamicTransforms) {
        RenderPipeline pipeline = element.pipeline();
        TextureSetup textureSetup = element.textureSetup();

        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
        BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        element.buildVertices(bufferBuilder);
        MeshData mesh = bufferBuilder.build();
        if (mesh == null) {
            byteBufferBuilder.close();
            return;
        }

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "WunderLib GUI element",
                        framebuffer.getColorTextureView(),
                        java.util.OptionalInt.empty(),
                        framebuffer.getDepthTextureView(),
                        java.util.OptionalDouble.empty()
                )) {
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setPipeline(pipeline);

            if (textureSetup.texure0() != null) {
                renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());
            }
            if (textureSetup.texure1() != null) {
                renderPass.bindTexture("Sampler1", textureSetup.texure1(), textureSetup.sampler1());
            }
            if (textureSetup.texure2() != null) {
                renderPass.bindTexture("Sampler2", textureSetup.texure2(), textureSetup.sampler2());
            }

            ScreenRectangle scissor = element.scissorArea();
            if (scissor != null) {
                int x = scissor.left();
                int y = framebuffer.height - scissor.bottom();
                renderPass.enableScissor(x, y, scissor.width(), scissor.height());
            } else {
                renderPass.disableScissor();
            }

            GpuBuffer vertexBuffer = pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
            GpuBuffer indexBuffer;
            VertexFormat.IndexType indexType;
            if (mesh.indexBuffer() == null) {
                RenderSystem.AutoStorageIndexBuffer autoIndex = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
                indexBuffer = autoIndex.getBuffer(mesh.drawState().indexCount());
                indexType = autoIndex.type();
            } else {
                indexBuffer = pipeline.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
                indexType = mesh.drawState().indexType();
            }

            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.setIndexBuffer(indexBuffer, indexType);
            renderPass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
        } finally {
            mesh.close();
            byteBufferBuilder.close();
        }
    }
}
