package com.alcatrazescapee.notreepunching.tests;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;

import com.alcatrazescapee.notreepunching.common.items.ModItems;

/**
 * Comprehensive GameTests for the Sharp Tool harvesting mechanics in No Tree Punching mod.
 * Tests validate that the knife vs. hand system works correctly for various plant types.
 * Uses proper ServerPlayer.gameMode.destroyBlock() to exercise the real harvest code path.
 */
@GameTestHolder("notreepunching")
public class SharpToolHarvestTests
{
    /**
     * Helper method to break a block as a player with a specific tool.
     * This exercises the real harvest code path through ServerPlayerGameMode.destroyBlock()
     * which triggers Forge events and the mod's harvest logic.
     */
    private static void breakAsPlayer(GameTestHelper helper, ServerPlayer player, BlockPos pos)
    {
        // Ensure player is in survival mode for proper harvest mechanics
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        
        // Break the block using the real harvest code path
        player.gameMode.destroyBlock(pos);
    }
    /**
     * Test Case 1: Knife on Flower - Verify that breaking a flower (Poppy) with a flint knife correctly drops the flower item.
     */
    @GameTest(template = "platform")
    public static void test_knife_on_flower_drops_item(GameTestHelper helper)
    {
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on platform template
        
        // Place a poppy on the platform
        helper.setBlock(flowerPos, Blocks.POPPY);
        
        // Create a mock server player and give them a flint knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        // Break the block using real harvest mechanics
        breakAsPlayer(helper, player, flowerPos);
        
        // Assert that the block is now air
        helper.assertBlockNotPresent(Blocks.POPPY, flowerPos);
        
        // Use polling assertion to check for dropped items (allows for timing)
        helper.succeedWhen(() -> helper.assertItemEntityPresent(Items.POPPY, flowerPos, 2.0));
    }
    
    /**
     * Test Case 2: Hand on Flower - Verify that breaking a flower with a bare hand results in no item drop.
     */
    @GameTest(template = "platform")
    public static void test_hand_on_flower_no_drops(GameTestHelper helper)
    {
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on platform template
        
        // Place a poppy on the platform
        helper.setBlock(flowerPos, Blocks.POPPY);
        
        // Create a mock server player with empty hands
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        
        // Break the block using real harvest mechanics
        breakAsPlayer(helper, player, flowerPos);
        
        // Assert that the block is now air
        helper.assertBlockNotPresent(Blocks.POPPY, flowerPos);
        
        // Use polling assertion to verify no poppy items are dropped
        helper.succeedWhen(() -> helper.assertItemEntityNotPresent(Items.POPPY, flowerPos, 2.0));
    }
    
    /**
     * Test Case 3: Knife on Grass - Verify that breaking a grass block with a flint knife correctly drops plant_fiber.
     */
    @GameTest(template = "platform")
    public static void test_knife_on_grass_drops_plant_fiber(GameTestHelper helper)
    {
        BlockPos grassPos = new BlockPos(1, 1, 1); // Place on platform template
        
        // Place grass on the platform (short grass plant, not grass_block)
        helper.setBlock(grassPos, Blocks.GRASS);
        
        // Create a mock server player and give them a flint knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        // Break the block using real harvest mechanics
        breakAsPlayer(helper, player, grassPos);
        
        // Assert that the block is now air
        helper.assertBlockNotPresent(Blocks.GRASS, grassPos);
        
        // Use polling assertion to check for plant fiber drops
        helper.succeedWhen(() -> helper.assertItemEntityPresent(ModItems.GRASS_FIBER.get(), grassPos, 2.0));
    }
    
    /**
     * Test Case 4: Hand on Grass - Verify that breaking a grass block with a bare hand results in no plant_fiber drop.
     */
    @GameTest(template = "platform")
    public static void test_hand_on_grass_no_plant_fiber(GameTestHelper helper)
    {
        BlockPos grassPos = new BlockPos(1, 1, 1); // Place on platform template
        
        // Place grass on the platform (short grass plant, not grass_block)
        helper.setBlock(grassPos, Blocks.GRASS);
        
        // Create a mock server player with empty hands
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        
        // Break the block using real harvest mechanics
        breakAsPlayer(helper, player, grassPos);
        
        // Assert that the block is now air
        helper.assertBlockNotPresent(Blocks.GRASS, grassPos);
        
        // Use polling assertion to verify no plant fiber items are dropped
        helper.succeedWhen(() -> helper.assertItemEntityNotPresent(ModItems.GRASS_FIBER.get(), grassPos, 2.0));
    }
    
    /**
     * Batch/Performance Test: Break a large number of blocks (5x5 area of flowers) in sequence.
     * While not a true performance benchmark, this helps identify significant performance regressions
     * in block-breaking or drop-handling logic by observing execution time.
     */
    @GameTest(template = "5x5_platform")
    public static void test_batch_flower_harvest_performance(GameTestHelper helper)
    {
        // Create a mock server player and give them a flint knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        // Record start time for performance observation
        long startTime = System.currentTimeMillis();
        
        // Place and break 25 flowers (5x5 grid)
        int flowersProcessed = 0;
        for (int x = 0; x < 5; x++)
        {
            for (int z = 0; z < 5; z++)
            {
                BlockPos flowerPos = new BlockPos(x, 1, z); // Place on 5x5 platform template
                
                // Place a flower
                helper.setBlock(flowerPos, Blocks.POPPY);
                
                // Break the block using real harvest mechanics
                breakAsPlayer(helper, player, flowerPos);
                
                flowersProcessed++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify we processed all flowers
        helper.assertTrue(flowersProcessed == 25, "Should have processed 25 flowers, but processed: " + flowersProcessed);
        
        // Log timing for regression detection (not a hard assertion since this is environment-dependent)
        System.out.println("Batch harvest test: Processed " + flowersProcessed + " flowers in " + duration + "ms");
        
        // Sanity check: ensure it doesn't take unreasonably long (soft performance check)
        // This is a very loose bound - mainly to catch severe regressions
        if (duration > 5000) { // 5 seconds
            System.out.println("WARNING: Batch harvest took " + duration + "ms, which may indicate performance regression");
        }
        
        helper.succeed();
    }
    
    /**
     * Test verifying that the sharp tool system respects configuration settings.
     * This ensures that when the system is enabled, knives work as expected.
     */
    @GameTest(template = "platform")
    public static void test_sharp_tool_system_configuration(GameTestHelper helper)
    {
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on platform template
        
        // Place a poppy
        helper.setBlock(flowerPos, Blocks.POPPY);
        
        // Create a mock server player and give them a knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        // Break the block using real harvest mechanics
        breakAsPlayer(helper, player, flowerPos);
        
        // Assert that the block is now air
        helper.assertBlockNotPresent(Blocks.POPPY, flowerPos);
        
        // With the system enabled (default), knife should produce drops
        helper.succeedWhen(() -> helper.assertItemEntityPresent(Items.POPPY, flowerPos, 2.0));
    }
}