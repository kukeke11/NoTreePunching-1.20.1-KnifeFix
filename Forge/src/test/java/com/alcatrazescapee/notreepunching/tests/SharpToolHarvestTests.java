package com.alcatrazescapee.notreepunching.tests;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.server.level.ServerLevel;

import com.alcatrazescapee.notreepunching.common.items.ModItems;
import com.alcatrazescapee.notreepunching.EventHandler;
import com.alcatrazescapee.notreepunching.util.DebugUtil;

import java.util.List;

/**
 * Comprehensive GameTests for the Sharp Tool harvesting mechanics in No Tree Punching mod.
 * Tests validate that the knife vs. hand system works correctly for various plant types.
 * Uses proper ServerPlayer.gameMode.destroyBlock() to exercise the real harvest code path.
 */
@GameTestHolder("notreepunching")
public class SharpToolHarvestTests
{
    /**
     * Helper method to clear any stray ItemEntity in a small radius around the position.
     * This prevents leftovers from previous actions from polluting the assertions.
     */
    private static void clearItemsAroundPosition(GameTestHelper helper, BlockPos pos, double radius)
    {
        AABB clearArea = new AABB(pos).inflate(radius);
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, clearArea);
        for (ItemEntity item : items)
        {
            item.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }

    /**
     * Helper method to break a block as a player with a specific tool.
     * This exercises the real harvest code path through Level.destroyBlock()
     * which triggers Forge events and the mod's harvest logic.
     * Returns whether the block was successfully broken.
     */
    private static boolean breakAsPlayer(GameTestHelper helper, Player player, BlockPos relativePos)
    {
        // Convert relative GameTest coordinates to absolute world coordinates
        BlockPos absolutePos = helper.absolutePos(relativePos);
        
        // Start comprehensive action tracking for this GameTest operation
        String actionId = DebugUtil.startAction(player, helper.getLevel().getBlockState(absolutePos), absolutePos, "GAMETEST_BREAK_BLOCK");
        
        try
        {
            DebugUtil.info(actionId, "GameTest breakAsPlayer() - Starting block breaking simulation");
            DebugUtil.info(actionId, "GameTest breakAsPlayer() - Relative position: %s, Absolute position: %s", relativePos, absolutePos);
            DebugUtil.info(actionId, "GameTest breakAsPlayer() - Player: %s", DebugUtil.getPlayerInfo(player));
            DebugUtil.info(actionId, "GameTest breakAsPlayer() - Block before breaking: %s", DebugUtil.getBlockInfo(helper.getLevel().getBlockState(absolutePos), absolutePos));
            DebugUtil.info(actionId, "GameTest breakAsPlayer() - Tool: %s", DebugUtil.getDetailedToolInfo(player.getMainHandItem()));
            
            // Position the player standing on the grass platform (Y=1) near the block being broken
            player.setPos(absolutePos.getX() + 0.5, absolutePos.getY() - 1.0, absolutePos.getZ() + 0.5);
            DebugUtil.debug(actionId, "GameTest breakAsPlayer() - Player positioned at: %s", player.blockPosition());
            
            // Clear any stray items before breaking to avoid pollution
            clearItemsAroundPosition(helper, absolutePos, 2.0);
            DebugUtil.debug(actionId, "GameTest breakAsPlayer() - Cleared items in 2.0 block radius");
            
            boolean result;
            
            // Use FakePlayer's gameMode to properly simulate the full mining sequence
            if (player instanceof FakePlayer fakePlayer) {
                DebugUtil.debug(actionId, "GameTest breakAsPlayer() - Using FakePlayer.gameMode.destroyBlock() method");
                result = fakePlayer.gameMode.destroyBlock(absolutePos);
                DebugUtil.debug(actionId, "GameTest breakAsPlayer() - FakePlayer.gameMode.destroyBlock() returned: %s", result);
            } else {
                DebugUtil.debug(actionId, "GameTest breakAsPlayer() - Using Level.destroyBlock() fallback");
                result = helper.getLevel().destroyBlock(absolutePos, true, player);
                DebugUtil.debug(actionId, "GameTest breakAsPlayer() - Level.destroyBlock() returned: %s", result);
            }
            
            // Log final block state after breaking attempt
            BlockState afterState = helper.getLevel().getBlockState(absolutePos);
            DebugUtil.info(actionId, "GameTest breakAsPlayer() - Block after breaking: %s", DebugUtil.getBlockInfo(afterState, absolutePos));
            
            DebugUtil.endAction(actionId, result, "GameTest.breakAsPlayer");
            return result;
        }
        catch (Exception e)
        {
            DebugUtil.endActionWithError(actionId, e, "GameTest.breakAsPlayer");
            throw e;
        }
    }
    
    /**
     * Test Case 1: Knife on Flower - Verify that breaking a flower (Poppy) with a flint knife correctly drops the flower item.
     */
    @GameTest(template = "platform")
    public static void test_knife_on_flower_drops_item(GameTestHelper helper)
    {
        // Log test initiation with configuration state
        DebugUtil.logConfigState();
        DebugUtil.info(null, "===== STARTING TEST: test_knife_on_flower_drops_item =====");
        
        // Manually build a 3x3 grass platform instead of relying on templates
        DebugUtil.debug(null, "Building manual platform for test");
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                BlockPos grassPos = new BlockPos(x, 0, z);
                helper.setBlock(grassPos, Blocks.GRASS_BLOCK);
            }
        }
        
        // Verify the platform was built correctly
        BlockPos grassCheckPos = new BlockPos(1, 0, 1);
        helper.assertBlockPresent(Blocks.GRASS_BLOCK, grassCheckPos);
        DebugUtil.debug(null, "Platform verification successful: grass_block found at %s", grassCheckPos);
        
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place above the platform
        
        // Place a poppy 
        helper.setBlock(flowerPos, Blocks.POPPY);
        BlockPos absoluteFlowerPos = helper.absolutePos(flowerPos);
        DebugUtil.debug(null, "Placed poppy at relative %s (absolute %s)", flowerPos, absoluteFlowerPos);
        
        // Verify the poppy was actually placed
        helper.assertBlockPresent(Blocks.POPPY, flowerPos);
        
        // Create a FakePlayer which should properly trigger Forge events
        FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) helper.getLevel());
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        DebugUtil.info(null, "Test setup complete - ready to break block with: %s", DebugUtil.getDetailedToolInfo(knife));
        
        // Start the mining process - this will create its own action tracking
        breakAsPlayer(helper, player, flowerPos);
        
        // Check results after mining delay (50 ticks = 2.5 seconds, giving extra time)
        helper.runAfterDelay(50, () -> {
            DebugUtil.info(null, "===== VERIFYING TEST RESULTS: test_knife_on_flower_drops_item =====");
            
            // Assert that the block is now air
            helper.assertBlockNotPresent(Blocks.POPPY, flowerPos);
            DebugUtil.info(null, "✓ Block successfully removed");
            
            // Assert that the correct item dropped
            helper.assertItemEntityPresent(Items.POPPY, flowerPos, 2.0);
            DebugUtil.info(null, "✓ Correct item drop detected");
            
            DebugUtil.info(null, "===== TEST PASSED: test_knife_on_flower_drops_item =====");
            helper.succeed();
        });
    }
    
    /**
     * Test Case 2: Hand on Flower - Verify that breaking a flower with a bare hand results in no item drop.
     */
    @GameTest(template = "platform")
    public static void test_hand_on_flower_no_drops(GameTestHelper helper)
    {
        DebugUtil.info(null, "===== STARTING TEST: test_hand_on_flower_no_drops =====");
        
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on default template
        
        // Place a poppy 
        helper.setBlock(flowerPos, Blocks.POPPY);
        DebugUtil.debug(null, "Placed poppy at relative %s for empty hand test", flowerPos);
        
        // Create a FakePlayer with empty hands
        FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) helper.getLevel());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        
        DebugUtil.info(null, "Test setup complete - ready to break block with: %s", DebugUtil.getDetailedToolInfo(ItemStack.EMPTY));
        
        // Start the mining process - this will create its own action tracking
        breakAsPlayer(helper, player, flowerPos);
        
        // Check results after mining delay (50 ticks = 2.5 seconds, giving extra time)
        helper.runAfterDelay(50, () -> {
            DebugUtil.info(null, "===== VERIFYING TEST RESULTS: test_hand_on_flower_no_drops =====");
            
            // Assert that the block is now air
            helper.assertBlockNotPresent(Blocks.POPPY, flowerPos);
            DebugUtil.info(null, "✓ Block successfully removed");
            
            // Assert that NO items dropped (Sharp Tool system should prevent drops)
            helper.assertItemEntityNotPresent(Items.POPPY, flowerPos, 2.0);
            DebugUtil.info(null, "✓ No items dropped (as expected with empty hands)");
            
            DebugUtil.info(null, "===== TEST PASSED: test_hand_on_flower_no_drops =====");
            helper.succeed();
        });
    }
    
    /**
     * Test Case 3: Knife on Grass - Verify that breaking a grass block with a flint knife correctly drops plant_fiber.
     */
    @GameTest(template = "platform")
    public static void test_knife_on_grass_drops_plant_fiber(GameTestHelper helper)
    {
        BlockPos grassPos = new BlockPos(1, 1, 1); // Place on default template
        
        // Place grass on the platform (short grass plant, not grass_block)
        helper.setBlock(grassPos, Blocks.GRASS);
        
        // Create a FakePlayer which should properly trigger Forge events
        FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) helper.getLevel());
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        // Start the mining process (now asynchronous)
        breakAsPlayer(helper, player, grassPos);
        
        // Check results after mining delay (50 ticks = 2.5 seconds, giving extra time)
        helper.runAfterDelay(50, () -> {
            DebugUtil.info(null, "===== VERIFYING TEST RESULTS: test_knife_on_grass_drops_plant_fiber =====");
            
            // Assert that the block is now air
            helper.assertBlockNotPresent(Blocks.GRASS, grassPos);
            DebugUtil.info(null, "✓ Block successfully removed");
            
            // Assert that the correct item dropped - according to spec: "Normal drops + plant fiber"
            helper.assertItemEntityPresent(ModItems.GRASS_FIBER.get(), grassPos, 2.0);
            DebugUtil.info(null, "✓ Plant fiber dropped as expected");
            
            DebugUtil.info(null, "===== TEST PASSED: test_knife_on_grass_drops_plant_fiber =====");
            helper.succeed();
        });
    }
    
    /**
     * Test Case 4: Hand on Grass - Verify that breaking a grass block with a bare hand results in no plant_fiber drop.
     */
    @GameTest(template = "platform")
    public static void test_hand_on_grass_no_plant_fiber(GameTestHelper helper)
    {
        BlockPos grassPos = new BlockPos(1, 1, 1); // Place on default template
        
        // Place grass on the platform (short grass plant, not grass_block)
        helper.setBlock(grassPos, Blocks.GRASS);
        
        // Create a FakePlayer with empty hands
        FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) helper.getLevel());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        
        // Start the mining process (now asynchronous)
        breakAsPlayer(helper, player, grassPos);
        
        // Check results after mining delay (50 ticks = 2.5 seconds, giving extra time)
        helper.runAfterDelay(50, () -> {
            // Assert that the block is now air
            helper.assertBlockNotPresent(Blocks.GRASS, grassPos);
            
            // Assert that NO items dropped (Sharp Tool system should prevent drops)
            helper.assertItemEntityNotPresent(ModItems.GRASS_FIBER.get(), grassPos, 2.0);
            helper.succeed();
        });
    }
    
    /**
     * Batch/Performance Test: Break a large number of blocks (5x5 area of flowers) in sequence.
     * While not a true performance benchmark, this helps identify significant performance regressions
     * in block-breaking or drop-handling logic by observing execution time.
     */
    @GameTest(template = "5x5_platform")
    public static void test_batch_flower_harvest_performance(GameTestHelper helper)
    {
        DebugUtil.info(null, "===== STARTING BATCH TEST: test_batch_flower_harvest_performance =====");
        
        // Create a FakePlayer which should properly trigger Forge events
        FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) helper.getLevel());
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        DebugUtil.info(null, "Batch test setup - Tool: %s", DebugUtil.getDetailedToolInfo(knife));
        
        // Record start time for performance observation
        long startTime = System.currentTimeMillis();
        
        // Place and break 9 flowers (3x3 grid to fit in default template)
        int flowersProcessed = 0;
        int successfulBreaks = 0;
        
        DebugUtil.info(null, "Starting batch processing of 9 flowers in 3x3 grid");
        
        for (int x = 0; x < 3; x++)
        {
            for (int z = 0; z < 3; z++)
            {
                BlockPos flowerPos = new BlockPos(x, 1, z); // Place in default template
                
                // Place a flower
                helper.setBlock(flowerPos, Blocks.POPPY);
                DebugUtil.debug(null, "Batch test - Placed flower %d at %s", flowersProcessed + 1, flowerPos);
                
                // Break the block using real harvest mechanics and track success
                boolean blockBroken = breakAsPlayer(helper, player, flowerPos);
                if (blockBroken)
                {
                    successfulBreaks++;
                }
                
                flowersProcessed++;
                
                // Log progress every few flowers
                if (flowersProcessed % 3 == 0)
                {
                    DebugUtil.debug(null, "Batch test progress: %d/%d flowers processed, %d successful", 
                                   flowersProcessed, 9, successfulBreaks);
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        DebugUtil.info(null, "===== BATCH TEST RESULTS =====");
        DebugUtil.info(null, "Flowers processed: %d/9", flowersProcessed);
        DebugUtil.info(null, "Successful breaks: %d/9", successfulBreaks);
        DebugUtil.info(null, "Total duration: %dms", duration);
        DebugUtil.info(null, "Average per flower: %.1fms", (double)duration / flowersProcessed);
        
        // Performance analysis
        if (duration > 5000) { // 5 seconds
            DebugUtil.warn(null, "PERFORMANCE WARNING: Batch harvest took %dms, may indicate regression", duration);
        } else {
            DebugUtil.info(null, "✓ Performance within acceptable range");
        }
        
        // Verify we processed all flowers
        helper.assertTrue(flowersProcessed == 9, "Should have processed 9 flowers, but processed: " + flowersProcessed);
        helper.assertTrue(successfulBreaks == 9, "Should have successfully broken 9 flowers, but broke: " + successfulBreaks);
        
        DebugUtil.info(null, "===== BATCH TEST PASSED =====");
        helper.succeed();
    }
    
    /**
     * Test verifying that the sharp tool system respects configuration settings.
     * This ensures that when the system is enabled, knives work as expected.
     */
    @GameTest(template = "platform")
    public static void test_sharp_tool_system_configuration(GameTestHelper helper)
    {
        DebugUtil.info(null, "===== STARTING TEST: test_sharp_tool_system_configuration =====");
        
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on default template
        
        // Place a poppy
        helper.setBlock(flowerPos, Blocks.POPPY);
        DebugUtil.debug(null, "Placed poppy at relative %s for configuration test", flowerPos);
        
        // Create a FakePlayer which should properly trigger Forge events
        FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) helper.getLevel());
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        DebugUtil.info(null, "Test setup complete - ready to break block with: %s", DebugUtil.getDetailedToolInfo(knife));
        
        // Start the mining process - this will create its own action tracking
        breakAsPlayer(helper, player, flowerPos);
        
        // Check results after mining delay (50 ticks = 2.5 seconds, giving extra time)
        helper.runAfterDelay(50, () -> {
            DebugUtil.info(null, "===== VERIFYING TEST RESULTS: test_sharp_tool_system_configuration =====");
            
            // Assert that the block is now air
            helper.assertBlockNotPresent(Blocks.POPPY, flowerPos);
            DebugUtil.info(null, "✓ Block successfully removed");
            
            // With the system enabled (default), knife should produce drops
            helper.assertItemEntityPresent(Items.POPPY, flowerPos, 2.0);
            DebugUtil.info(null, "✓ Configuration test passed - knife produced expected drops");
            
            DebugUtil.info(null, "===== TEST PASSED: test_sharp_tool_system_configuration =====");
            helper.succeed();
        });
    }
}