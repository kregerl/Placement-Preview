package com.loucaskreger.placementpreview.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RenderUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final List<Identifier> renderLayerIdentifiers = Arrays.asList(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, TexturedRenderLayers.CHEST_ATLAS_TEXTURE, TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, TexturedRenderLayers.BEDS_ATLAS_TEXTURE);
    private static final Random random = new Random();
    public static boolean enabled = false;
    public static BlockPos pos;
    public static BlockState state;


    /**
     * Main render method, must be BEFORE_ENTITIES
     *
     * @param context
     */
    public static void render(WorldRenderContext context) {
        if (enabled) {
            var camera = context.camera();
            var blockRenderer = mc.getBlockRenderManager();
            var modelRenderer = blockRenderer.getModelRenderer();
            var blockEntityRenderDisatcher = mc.getBlockEntityRenderDispatcher();
            var matrixStack = context.matrixStack();

            if (state != null && pos != null) {
                int red = 255, green = 255, blue = 255;
                matrixStack.push();
                matrixStack.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);

                // Render the block
                var blockRenderType = state.getRenderType();
                System.out.println(blockRenderType);
                switch (blockRenderType) {

                    case MODEL:
                        blockRenderer.renderBlock(state, pos, mc.world, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                red, green, blue, Math.round(255 * 0.5f), false, null).getBuffer(RenderLayer.getEntityTranslucentCull(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)), false, random);
                        break;
                    case ENTITYBLOCK_ANIMATED:
                        blockRenderer.renderBlockAsEntity(state, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                red, green, blue, Math.round(255 * 0.5f), true, getRenderLayerForBlock(state.getBlock())), 0xFFFFFF, OverlayTexture.DEFAULT_UV);
                        break;
                    case INVISIBLE:
                        var blockEntity = getBlockEntityFromBlock(state.getBlock());
                        if (blockEntity != null) {
                            /** for banners use {@link BannerBlockEntityRenderer} **/
                            if (blockEntity instanceof BannerBlockEntity bannerEntity) {
                                System.out.println("Here");
                                var bannerRenderer = (BannerBlockEntityRenderer) blockEntityRenderDisatcher.get(bannerEntity);
                                bannerRenderer.render(bannerEntity, context.tickDelta(), matrixStack, mc.getBufferBuilders().getEntityVertexConsumers(), 0xFFFFFF, OverlayTexture.DEFAULT_UV);

//                                blockEntityRenderDisatcher.renderEntity(bannerEntity, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
//                                        red, green, blue, Math.round(255 * 0.5f), true, getRenderLayerForBlock(state.getBlock())), 0xFFFFFF, OverlayTexture.DEFAULT_UV);
                            } else {
                                blockEntityRenderDisatcher.renderEntity(blockEntity, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                        red, green, blue, Math.round(255 * 0.5f), true, getRenderLayerForBlock(state.getBlock())), 0xFFFFFF, OverlayTexture.DEFAULT_UV);
                            }

                        }
                        break;
                    default:

                }

                matrixStack.pop();
            }

        }

    }

    @Nullable
    private static BlockEntity getBlockEntityFromBlock(Block block) {
        if (block instanceof BlockEntityProvider blockEntityProvider) {
            return blockEntityProvider.createBlockEntity(pos, state);
        }
        return null;
    }

    /**
     * @param block
     * @return
     */
    private static RenderLayer getRenderLayerForBlock(Block block) {
        int index = 0;
        if (block instanceof AbstractChestBlock) {
            index = 1;
        } else if (block instanceof ShulkerBoxBlock) {
            index = 2;
        } else if (block instanceof SignBlock) {
            index = 3;
        } else if (block instanceof BannerBlock) {
            index = 4;
        } else if (block instanceof BedBlock) {
            index = 5;
        }
        System.out.println(index);
        return RenderLayer.getEntityTranslucentCull(renderLayerIdentifiers.get(index));
    }

    public static void clear() {
        state = null;
        pos = null;
    }
}
