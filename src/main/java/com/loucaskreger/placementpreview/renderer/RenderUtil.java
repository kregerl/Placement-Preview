package com.loucaskreger.placementpreview.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.item.BannerItem;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
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
            var entityRenderer = mc.getEntityRenderDispatcher();
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
                        System.out.println(state);
                        if (state.getBlock() instanceof DoorBlock) {
                            var state2 = state.with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
                            var pos2 = pos.offset(Direction.UP);

                            System.out.println(pos);
                            System.out.println(pos2);

                            blockRenderer.renderBlock(state, pos, mc.world, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                    red, green, blue, Math.round(255 * 0.5f), false, null).getBuffer(RenderLayer.getEntityTranslucentCull(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)), false, random);

                            matrixStack.pop();
                            matrixStack.push();
                            matrixStack.translate(pos2.getX() - camera.getPos().x, pos2.getY() - camera.getPos().y, pos2.getZ() - camera.getPos().z);

                            blockRenderer.renderBlock(state2, pos2, mc.world, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                    red, green, blue, Math.round(255 * 0.5f), false, null).getBuffer(RenderLayer.getEntityTranslucentCull(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)), false, random);

                        } else {
                            blockRenderer.renderBlock(state, pos, mc.world, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                    red, green, blue, Math.round(255 * 0.5f), false, null).getBuffer(RenderLayer.getEntityTranslucentCull(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)), false, random);
                        }
                        break;
                    case ENTITYBLOCK_ANIMATED:

                        if (state.getBlock() instanceof BedBlock) {
                            var state2 = state.with(BedBlock.PART, BedPart.HEAD);
                            var pos2 = pos.offset(mc.player.getHorizontalFacing());

                            blockEntityRenderDisatcher.renderEntity(getBlockEntity(state, pos, context.world()), matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                    red, green, blue, Math.round(255 * 0.5f), true, getRenderLayerForBlock(state.getBlock())), 0xFFFFFF, OverlayTexture.DEFAULT_UV);

                            matrixStack.pop();
                            matrixStack.push();
                            matrixStack.translate(pos2.getX() - camera.getPos().x, pos2.getY() - camera.getPos().y, pos2.getZ() - camera.getPos().z);

                            blockEntityRenderDisatcher.renderEntity(getBlockEntity(state2, pos2, context.world()), matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                    red, green, blue, Math.round(255 * 0.5f), true, getRenderLayerForBlock(state2.getBlock())), 0xFFFFFF, OverlayTexture.DEFAULT_UV);

                        } else {
                            blockRenderer.renderBlockAsEntity(state, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                    red, green, blue, Math.round(255 * 0.5f), true, getRenderLayerForBlock(state.getBlock())), 0xFFFFFF, OverlayTexture.DEFAULT_UV);
                        }


                        break;
                    case INVISIBLE:

                        var blockEntity = getBlockEntity(state, pos, context.world());
                        System.out.println(state);
                        if (blockEntity != null) {
                            if (blockEntity instanceof BannerBlockEntity bannerBlockEntity) {
                                if (mc.player.getMainHandStack().getItem() instanceof BannerItem bannerItem) {
                                    bannerBlockEntity.readFrom(mc.player.getMainHandStack(), bannerItem.getColor());
                                    System.out.println(bannerBlockEntity.getPatterns() != null);
                                    blockEntityRenderDisatcher.renderEntity(blockEntity, matrixStack, new VertexConsumerProviderWrapper(mc.getBufferBuilders().getEntityVertexConsumers(),
                                            red, green, blue, Math.round(255 * 0.5f), true, getRenderLayerForBlock(state.getBlock())), 0xFFFFFF, OverlayTexture.DEFAULT_UV);

                                }
                            }

                        }
                        break;
                    default:

                }

                matrixStack.pop();
            }

        }

    }

    // Sets the world of the blockEntity as the world is null by default
    @Nullable
    private static BlockEntity getBlockEntity(BlockState blockState, BlockPos blockPos, World world) {
        if (blockState.getBlock() instanceof BlockEntityProvider blockEntityProvider) {
            var blockEntity = blockEntityProvider.createBlockEntity(blockPos, blockState);
            blockEntity.setWorld(world);
            return blockEntity;
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
        return RenderLayer.getEntityTranslucentCull(renderLayerIdentifiers.get(index));
    }

    public static void clear() {
        state = null;
        pos = null;
    }
}
