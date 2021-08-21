package com.loucaskreger.placementpreview;

import com.loucaskreger.placementpreview.renderer.RenderUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.RailPlacementHelper;
import net.minecraft.block.enums.RailShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class PlacementPreview implements ModInitializer {


    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final String MOD_ID = "placementpreview";
    public static final KeyBinding preview = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            MOD_ID + ".key.preview", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, MOD_ID + ".key.categories"));


    @Override
    public void onInitialize() {
        WorldRenderEvents.BEFORE_ENTITIES.register(RenderUtil::render);
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
    }


    private void onClientTick(MinecraftClient client) {
        if (preview.wasPressed()) {
            RenderUtil.enabled = !RenderUtil.enabled;
        }
        if (client.player != null) {
            var heldStack = client.player.getMainHandStack();
            if (heldStack.getItem() instanceof BlockItem blockItem) {
                var hitResult = mc.crosshairTarget;
                if (hitResult != null) {
                    // Only set pos and state if looking at a block, otherwise reset them
                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        var blockHitResult = ((BlockHitResult) hitResult);
                        var context = new ItemPlacementContext(client.player, client.player.getActiveHand(), heldStack, blockHitResult);
                        var block = blockItem.getBlock();
                        var state = block.getPlacementState(context);


                        BlockPos placementPos;
                        var targetedBlockMaterial = context.getWorld().getBlockState(blockHitResult.getBlockPos()).getMaterial();
                        // Preview should render over replaceable plants
                        if (targetedBlockMaterial == Material.REPLACEABLE_PLANT || targetedBlockMaterial == Material.REPLACEABLE_UNDERWATER_PLANT) {
                            placementPos = blockHitResult.getBlockPos();
                        } else {
                            placementPos = blockHitResult.getBlockPos().add(blockHitResult.getSide().getVector());
                        }

                        if (state != null && block.canPlaceAt(state, context.getWorld(), placementPos)) {
                            RenderUtil.pos = placementPos;
                            RenderUtil.state = state;
                            if (blockItem.getBlock() instanceof AbstractRailBlock) {
                                RenderUtil.state = updateRailState(RenderUtil.state, RenderUtil.pos, context.getWorld());
                            }
                        } else {
                            RenderUtil.clear();
                        }
                    } else {
                        RenderUtil.clear();
                    }
                }
            } else {
                RenderUtil.clear();
            }
        }
    }

    /**
     * @param pos   Position to check for neighboring rails to connect to
     * @param world Required to get the block at the passed position
     * @return True if the rail has a neighbor it can conenct to
     */
    private static boolean canConnect(BlockPos pos, World world) {
        var blockState = world.getBlockState(pos);
        if (AbstractRailBlock.isRail(blockState)) {
            return true;
        } else {
            BlockPos blockPos = pos.up();
            blockState = world.getBlockState(blockPos);
            if (AbstractRailBlock.isRail(blockState)) {
                return true;
            } else {
                blockPos = pos.down();
                blockState = world.getBlockState(blockPos);
                return AbstractRailBlock.isRail(blockState);
            }
        }
    }

    /**
     * Special condition for rails since they do not determine state on placement, but are updated once placed.
     * Taken directly from {@link RailPlacementHelper#updateBlockState(boolean, boolean, RailShape)} adapted to my needs
     *
     * @param currentState The current state of the rail being placed
     * @param pos          The position the rail will be placed at
     * @param world        World, used to check neighboring blocks
     * @return - The corrected BlockState of the rail that's being placed.
     */
    public static BlockState updateRailState(BlockState currentState, BlockPos pos, World world) {
        AbstractRailBlock railBlock = (AbstractRailBlock) currentState.getBlock();
        RailShape railShape = currentState.get(railBlock.getShapeProperty());
        BlockPos blockPosNorth = pos.north();
        BlockPos blockPosSouth = pos.south();
        BlockPos blockPosWest = pos.west();
        BlockPos blockPosEast = pos.east();

        boolean bl = canConnect(blockPosNorth, world);
        boolean bl2 = canConnect(blockPosSouth, world);
        boolean bl3 = canConnect(blockPosWest, world);
        boolean bl4 = canConnect(blockPosEast, world);
        RailShape railShape2 = null;
        boolean bl5 = bl || bl2;
        boolean bl6 = bl3 || bl4;
        if (bl5 && !bl6) {
            railShape2 = RailShape.NORTH_SOUTH;
        }

        if (bl6 && !bl5) {
            railShape2 = RailShape.EAST_WEST;
        }

        boolean bl7 = bl2 && bl4;
        boolean bl8 = bl2 && bl3;
        boolean bl9 = bl && bl4;
        boolean bl10 = bl && bl3;
        if (!railBlock.canMakeCurves()) {
            if (bl7 && !bl && !bl3) {
                railShape2 = RailShape.SOUTH_EAST;
            }

            if (bl8 && !bl && !bl4) {
                railShape2 = RailShape.SOUTH_WEST;
            }

            if (bl10 && !bl2 && !bl4) {
                railShape2 = RailShape.NORTH_WEST;
            }

            if (bl9 && !bl2 && !bl3) {
                railShape2 = RailShape.NORTH_EAST;
            }
        }

        if (railShape2 == null) {
            if (bl5 && bl6) {
                railShape2 = railShape;
            } else if (bl5) {
                railShape2 = RailShape.NORTH_SOUTH;
            } else if (bl6) {
                railShape2 = RailShape.EAST_WEST;
            }

            if (!railBlock.canMakeCurves()) {
                if (world.isReceivingRedstonePower(pos)) {
                    if (bl7) {
                        railShape2 = RailShape.SOUTH_EAST;
                    }

                    if (bl8) {
                        railShape2 = RailShape.SOUTH_WEST;
                    }

                    if (bl9) {
                        railShape2 = RailShape.NORTH_EAST;
                    }

                    if (bl10) {
                        railShape2 = RailShape.NORTH_WEST;
                    }
                } else {
                    if (bl10) {
                        railShape2 = RailShape.NORTH_WEST;
                    }

                    if (bl9) {
                        railShape2 = RailShape.NORTH_EAST;
                    }

                    if (bl8) {
                        railShape2 = RailShape.SOUTH_WEST;
                    }

                    if (bl7) {
                        railShape2 = RailShape.SOUTH_EAST;
                    }
                }
            }
        }

        if (railShape2 == RailShape.NORTH_SOUTH) {
            if (AbstractRailBlock.isRail(world, blockPosNorth.up())) {
                railShape2 = RailShape.ASCENDING_NORTH;
            }

            if (AbstractRailBlock.isRail(world, blockPosSouth.up())) {
                railShape2 = RailShape.ASCENDING_SOUTH;
            }
        }

        if (railShape2 == RailShape.EAST_WEST) {
            if (AbstractRailBlock.isRail(world, blockPosEast.up())) {
                railShape2 = RailShape.ASCENDING_EAST;
            }

            if (AbstractRailBlock.isRail(world, blockPosWest.up())) {
                railShape2 = RailShape.ASCENDING_WEST;
            }
        }

        if (railShape2 == null) {
            railShape2 = railShape;
        }
        return currentState.with(railBlock.getShapeProperty(), railShape2);
    }


}
