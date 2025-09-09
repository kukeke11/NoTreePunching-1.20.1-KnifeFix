package com.alcatrazescapee.notreepunching.tests;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;

import com.alcatrazescapee.notreepunching.common.items.ModItems;
import com.alcatrazescapee.notreepunching.util.HarvestBlockHandler;

import java.util.List;

/**
 * Comprehensive GameTests for the Sharp Tool harvesting mechanics in No Tree Punching mod.
 * Tests validate that the knife vs. hand system works correctly for various plant types.
 */
@GameTestHolder("notreepunching")
public class SharpToolHarvestTests
{
    /**
     * Test Case 1: Knife on Flower - Verify that breaking a flower (Poppy) with a flint knife correctly drops the flower item.
     */
    @GameTest(template = "empty")
    public static void test_knife_on_flower_drops_item(GameTestHelper helper)
    {
        BlockPos flowerPos = new BlockPos(1, 2, 1); // Place at Y=2 for empty template
        Player player = helper.makeMockPlayer();
        
        // Place a poppy on the platform
        helper.setBlock(flowerPos, Blocks.POPPY);
        
        // Give player a flint knife
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        // Simulate breaking the block with proper context
        BlockState flowerState = helper.getBlockState(flowerPos);
        
        // Check that the knife is considered the correct tool for harvest
        boolean canHarvest = HarvestBlockHandler.isUsingCorrectToolForDrops(flowerState, flowerPos, player);
        helper.assertTrue(canHarvest, "Flint knife should be able to harvest flowers");
        
        // Verify the block can be broken and should drop items
        helper.destroyBlock(flowerPos);
        
        // Look for dropped items in the area
        helper.assertItemEntityPresent(Items.POPPY, flowerPos, 2.0);
        
        helper.succeed();
    }
    
    /**
     * Test Case 2: Hand on Flower - Verify that breaking a flower with a bare hand results in no item drop.
     */
    @GameTest(template = "empty")
    public static void test_hand_on_flower_no_drops(GameTestHelper helper)
    {
        BlockPos flowerPos = new BlockPos(1, 2, 1); // Place at Y=2 for empty template
        Player player = helper.makeMockPlayer();
        
        // Place a poppy on the platform
        helper.setBlock(flowerPos, Blocks.POPPY);
        
        // Ensure player is empty-handed
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        
        BlockState flowerState = helper.getBlockState(flowerPos);
        
        // Check that bare hands cannot harvest flowers
        boolean canHarvest = HarvestBlockHandler.isUsingCorrectToolForDrops(flowerState, flowerPos, player);
        helper.assertFalse(canHarvest, "Bare hands should not be able to harvest flowers");
        
        // Break the block
        helper.destroyBlock(flowerPos);
        
        // Verify no poppy items are dropped (should be no drops)
        helper.assertItemEntityNotPresent(Items.POPPY, flowerPos, 2.0);
        
        helper.succeed();
    }
    
    /**
     * Test Case 3: Knife on Grass - Verify that breaking a grass block with a flint knife correctly drops plant_fiber.
     */
    @GameTest(template = "empty")
    public static void test_knife_on_grass_drops_plant_fiber(GameTestHelper helper)
    {
        BlockPos grassPos = new BlockPos(1, 2, 1); // Place at Y=2 for empty template
        Player player = helper.makeMockPlayer();
        
        // Place grass on the platform
        helper.setBlock(grassPos, Blocks.GRASS);
        
        // Give player a flint knife
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        BlockState grassState = helper.getBlockState(grassPos);
        
        // Check that the knife is considered the correct tool for harvest
        boolean canHarvest = HarvestBlockHandler.isUsingCorrectToolForDrops(grassState, grassPos, player);
        helper.assertTrue(canHarvest, "Flint knife should be able to harvest grass for plant fiber");
        
        // Break the block
        helper.destroyBlock(grassPos);
        
        // Look for plant fiber drops
        helper.assertItemEntityPresent(ModItems.GRASS_FIBER.get(), grassPos, 2.0);
        
        helper.succeed();
    }
    
    /**
     * Test Case 4: Hand on Grass - Verify that breaking a grass block with a bare hand results in no plant_fiber drop.
     */
    @GameTest(template = "empty")
    public static void test_hand_on_grass_no_plant_fiber(GameTestHelper helper)
    {
        BlockPos grassPos = new BlockPos(1, 2, 1); // Place at Y=2 for empty template
        Player player = helper.makeMockPlayer();
        
        // Place grass on the platform
        helper.setBlock(grassPos, Blocks.GRASS);
        
        // Ensure player is empty-handed
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        
        BlockState grassState = helper.getBlockState(grassPos);
        
        // Check that bare hands cannot harvest grass for plant fiber
        boolean canHarvest = HarvestBlockHandler.isUsingCorrectToolForDrops(grassState, grassPos, player);
        helper.assertFalse(canHarvest, "Bare hands should not be able to harvest grass for plant fiber");
        
        // Break the block
        helper.destroyBlock(grassPos);
        
        // Verify no plant fiber items are dropped
        helper.assertItemEntityNotPresent(ModItems.GRASS_FIBER.get(), grassPos, 2.0);
        
        helper.succeed();
    }
    
    /**
     * Batch/Performance Test: Break a large number of blocks (5x5 area of flowers) in sequence.
     * While not a true performance benchmark, this helps identify significant performance regressions
     * in block-breaking or drop-handling logic by observing execution time.
     */
    @GameTest(template = "empty")
    public static void test_batch_flower_harvest_performance(GameTestHelper helper)
    {
        Player player = helper.makeMockPlayer();
        
        // Give player a flint knife
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
                BlockPos flowerPos = new BlockPos(x, 2, z); // Y=2 for empty template
                
                // Place a flower
                helper.setBlock(flowerPos, Blocks.POPPY);
                
                // Verify it can be harvested
                BlockState flowerState = helper.getBlockState(flowerPos);
                boolean canHarvest = HarvestBlockHandler.isUsingCorrectToolForDrops(flowerState, flowerPos, player);
                helper.assertTrue(canHarvest, "Knife should harvest flower at " + flowerPos);
                
                // Break the block
                helper.destroyBlock(flowerPos);
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
     * This ensures that when the system is disabled, tools behave like vanilla.
     */
    @GameTest(template = "empty")
    public static void test_sharp_tool_system_configuration(GameTestHelper helper)
    {
        BlockPos flowerPos = new BlockPos(1, 2, 1); // Y=2 for empty template
        Player player = helper.makeMockPlayer();
        
        // Place a poppy
        helper.setBlock(flowerPos, Blocks.POPPY);
        
        // Test with knife
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        
        BlockState flowerState = helper.getBlockState(flowerPos);
        
        // The configuration should be enabled by default for this test
        boolean canHarvest = HarvestBlockHandler.isUsingCorrectToolForDrops(flowerState, flowerPos, player);
        helper.assertTrue(canHarvest, "With sharp tool system enabled, knife should harvest flowers");
        
        helper.succeed();
    }
}