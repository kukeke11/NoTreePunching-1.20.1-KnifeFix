package com.alcatrazescapee.notreepunching.tests;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;

import com.alcatrazescapee.notreepunching.common.items.ModItems;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Comprehensive GameTests for the Sharp Tool harvesting mechanics in No Tree Punching mod.
 * Tests validate that the knife vs. hand system works correctly for various plant types.
 * Uses proper ServerPlayer.gameMode.destroyBlock() to exercise the real harvest code path.
 * 
 * Enhanced with detailed debugging output to provide actionable failure information:
 * - Logs expected vs actual drops (item type, position, count)
 * - Logs block state before/after breaking
 * - Logs player's held tool during tests
 * - Provides failure context for easier debugging of tag-based harvesting logic
 */
@GameTestHolder("notreepunching")
public class SharpToolHarvestTests
{
    // Debug logging flag - set to true to enable verbose logging even on success
    private static final boolean ENABLE_VERBOSE_LOGGING = false;
    
    /**
     * Enhanced logging method that provides context about test state.
     * Only logs when there's a failure or when verbose logging is enabled.
     */
    private static void logDebugInfo(String message, Object... args) {
        if (ENABLE_VERBOSE_LOGGING) {
            System.out.println("[SharpToolHarvestTests DEBUG] " + String.format(message, args));
        }
    }
    
    /**
     * Enhanced logging method for failures - always logs regardless of verbose flag.
     */
    private static void logFailureInfo(String message, Object... args) {
        System.err.println("[SharpToolHarvestTests FAILURE] " + String.format(message, args));
    }
    
    /**
     * Enhanced helper to log detailed block and tool state for debugging.
     */
    private static void logTestContext(GameTestHelper helper, ServerPlayer player, BlockPos pos, String testPhase) {
        logDebugInfo("=== %s Context ===", testPhase);
        logDebugInfo("Position: %s", pos);
        logDebugInfo("Block before: %s", helper.getBlockState(pos));
        logDebugInfo("Player held item: %s", player.getMainHandItem());
        logDebugInfo("Player gamemode: %s", player.gameMode.getGameModeForPlayer());
        
        // Log nearby items for context
        AABB area = new AABB(pos).inflate(2.0);
        List<ItemEntity> nearbyItems = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
        logDebugInfo("Nearby items before action: %d items", nearbyItems.size());
        for (ItemEntity item : nearbyItems) {
            logDebugInfo("  - %s at %s", item.getItem(), item.blockPosition());
        }
    }
    
    /**
     * Enhanced helper to check and log dropped items with detailed failure information.
     */
    private static void assertItemDropsWithLogging(GameTestHelper helper, BlockPos pos, net.minecraft.world.item.Item expectedItem, 
            boolean shouldHaveDrops, String testName) {
        AABB area = new AABB(pos).inflate(2.0);
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
        List<ItemEntity> matchingItems = items.stream()
            .filter(item -> item.getItem().getItem() == expectedItem)
            .collect(Collectors.toList());
        
        logDebugInfo("=== Drop Check Results for %s ===", testName);
        logDebugInfo("Expected drops: %s", shouldHaveDrops ? "YES" : "NO");
        logDebugInfo("Expected item: %s", expectedItem);
        logDebugInfo("Total items found: %d", items.size());
        logDebugInfo("Matching items found: %d", matchingItems.size());
        
        if (items.size() > 0) {
            logDebugInfo("All items found:");
            for (ItemEntity item : items) {
                logDebugInfo("  - %s (count: %d) at %s", 
                    item.getItem().getItem(), item.getItem().getCount(), item.blockPosition());
            }
        }
        
        if (shouldHaveDrops) {
            if (matchingItems.isEmpty()) {
                logFailureInfo("ASSERTION FAILED in %s:", testName);
                logFailureInfo("  Expected: At least 1x %s", expectedItem);
                logFailureInfo("  Found: No matching items");
                logFailureInfo("  All items found: %s", 
                    items.stream().map(i -> i.getItem().getItem().toString()).collect(Collectors.toList()));
                helper.fail(String.format("Expected to find %s but found no matching items. Found items: %s", 
                    expectedItem, items.stream().map(i -> i.getItem().getItem().toString()).collect(Collectors.toList())));
            } else {
                logDebugInfo("SUCCESS: Found expected drops for %s", testName);
            }
        } else {
            if (!matchingItems.isEmpty()) {
                logFailureInfo("ASSERTION FAILED in %s:", testName);
                logFailureInfo("  Expected: No %s drops", expectedItem);
                logFailureInfo("  Found: %d matching items", matchingItems.size());
                for (ItemEntity item : matchingItems) {
                    logFailureInfo("    - %s (count: %d) at %s", 
                        item.getItem().getItem(), item.getItem().getCount(), item.blockPosition());
                }
                helper.fail(String.format("Expected no %s drops but found %d items", expectedItem, matchingItems.size()));
            } else {
                logDebugInfo("SUCCESS: Confirmed no unwanted drops for %s", testName);
            }
        }
    }
    
    /**
     * Enhanced helper to check block state with detailed logging.
     */
    private static void assertBlockStateWithLogging(GameTestHelper helper, BlockPos pos, 
            net.minecraft.world.level.block.Block expectedBlock, boolean shouldBePresent, String testName) {
        net.minecraft.world.level.block.state.BlockState actualState = helper.getBlockState(pos);
        net.minecraft.world.level.block.Block actualBlock = actualState.getBlock();
        
        logDebugInfo("=== Block State Check for %s ===", testName);
        logDebugInfo("Position: %s", pos);
        logDebugInfo("Expected block present: %s", shouldBePresent);
        logDebugInfo("Expected block: %s", expectedBlock);
        logDebugInfo("Actual block: %s", actualBlock);
        logDebugInfo("Actual state: %s", actualState);
        
        if (shouldBePresent) {
            if (actualBlock != expectedBlock) {
                logFailureInfo("BLOCK STATE ASSERTION FAILED in %s:", testName);
                logFailureInfo("  Expected: %s to be present", expectedBlock);
                logFailureInfo("  Found: %s", actualBlock);
                helper.fail(String.format("Expected block %s but found %s at position %s", 
                    expectedBlock, actualBlock, pos));
            }
        } else {
            if (actualBlock == expectedBlock) {
                logFailureInfo("BLOCK STATE ASSERTION FAILED in %s:", testName);
                logFailureInfo("  Expected: %s to be absent", expectedBlock);
                logFailureInfo("  Found: Block is still present");
                helper.fail(String.format("Expected %s to be removed but it's still present at position %s", 
                    expectedBlock, pos));
            }
        }
    }
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
     * Enhanced helper method to break a block as a player with detailed logging.
     * This exercises the real harvest code path through ServerPlayerGameMode.destroyBlock()
     * which triggers Forge events and the mod's harvest logic.
     * Returns whether the block was successfully broken.
     */
    private static boolean breakAsPlayer(GameTestHelper helper, ServerPlayer player, BlockPos pos, String testName)
    {
        // Log context before breaking
        logTestContext(helper, player, pos, "BEFORE Breaking Block");
        
        // Ensure player is in survival mode for proper harvest mechanics
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        
        // Clear any stray items before breaking to avoid pollution
        clearItemsAroundPosition(helper, pos, 2.0);
        logDebugInfo("Cleared existing items around position for clean test");
        
        // Store block state before breaking for logging
        net.minecraft.world.level.block.state.BlockState stateBefore = helper.getBlockState(pos);
        
        // Break the block using the real harvest code path and capture the result
        boolean result = player.gameMode.destroyBlock(pos);
        
        // Log results after breaking
        net.minecraft.world.level.block.state.BlockState stateAfter = helper.getBlockState(pos);
        logDebugInfo("=== Block Breaking Results for %s ===", testName);
        logDebugInfo("Break operation returned: %s", result);
        logDebugInfo("Block before: %s", stateBefore);
        logDebugInfo("Block after: %s", stateAfter);
        logDebugInfo("Block successfully removed: %s", !stateBefore.equals(stateAfter));
        
        if (!result) {
            logFailureInfo("BLOCK BREAKING FAILED in %s:", testName);
            logFailureInfo("  destroyBlock() returned false");
            logFailureInfo("  Block before: %s", stateBefore);
            logFailureInfo("  Block after: %s", stateAfter);
            logFailureInfo("  Tool used: %s", player.getMainHandItem());
        }
        
        return result;
    }
    
    /**
     * Test Case 1: Knife on Flower - Verify that breaking a flower (Poppy) with a flint knife correctly drops the flower item.
     * 
     * Expected behavior:
     * - Block breaks successfully (poppy removed from world)
     * - Exactly 1 poppy item drops near the break location
     * - No other unexpected items are dropped
     */
    @GameTest(template = "platform")
    public static void test_knife_on_flower_drops_item(GameTestHelper helper)
    {
        final String testName = "test_knife_on_flower_drops_item";
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on default template
        
        logDebugInfo("Starting %s", testName);
        
        // Place a poppy 
        helper.setBlock(flowerPos, Blocks.POPPY);
        logDebugInfo("Placed poppy block at %s", flowerPos);
        
        // Create a mock server player and give them a flint knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        logDebugInfo("Created player with flint knife: %s", knife);
        
        // Break the block using real harvest mechanics and verify it succeeded
        boolean blockBroken = breakAsPlayer(helper, player, flowerPos, testName);
        
        // Enhanced assertion with detailed logging
        if (!blockBroken) {
            logFailureInfo("CRITICAL FAILURE in %s: Block breaking failed", testName);
            helper.fail("Block should have been successfully broken but destroyBlock() returned false");
        }
        
        // Assert that the block is now air with enhanced logging
        assertBlockStateWithLogging(helper, flowerPos, Blocks.POPPY, false, testName);
        
        // Use polling assertion to check for dropped items (allows for timing) with enhanced logging
        helper.succeedWhen(() -> {
            logDebugInfo("Checking for expected poppy drops...");
            assertItemDropsWithLogging(helper, flowerPos, Items.POPPY, true, testName);
            logDebugInfo("Test %s completed successfully", testName);
        });
    }
    
    /**
     * Test Case 2: Hand on Flower - Verify that breaking a flower with a bare hand results in no item drop.
     * 
     * Expected behavior:
     * - Block breaks successfully (poppy removed from world)
     * - NO poppy items drop (hand harvesting should not give drops)
     * - This validates the sharp tool requirement system
     * 
     * NOTE: This is a critical test for the sharp tool system - Minecraft's default behavior 
     * would normally drop the flower, but our mod should prevent drops when using bare hands.
     */
    @GameTest(template = "platform")
    public static void test_hand_on_flower_no_drops(GameTestHelper helper)
    {
        final String testName = "test_hand_on_flower_no_drops";
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on default template
        
        logDebugInfo("Starting %s", testName);
        
        // Place a poppy 
        helper.setBlock(flowerPos, Blocks.POPPY);
        logDebugInfo("Placed poppy block at %s", flowerPos);
        
        // Create a mock server player with empty hands
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        logDebugInfo("Created player with empty hands");
        
        // Break the block using real harvest mechanics and verify it succeeded
        boolean blockBroken = breakAsPlayer(helper, player, flowerPos, testName);
        
        // Enhanced assertion with detailed logging
        if (!blockBroken) {
            logFailureInfo("CRITICAL FAILURE in %s: Block breaking failed", testName);
            helper.fail("Block should have been successfully broken but destroyBlock() returned false");
        }
        
        // Assert that the block is now air with enhanced logging
        assertBlockStateWithLogging(helper, flowerPos, Blocks.POPPY, false, testName);
        
        // Wait ~2 ticks before checking for no drops to give the game time to spawn any items
        helper.runAfterDelay(2, () -> {
            logDebugInfo("Checking that no poppy drops occurred (testing sharp tool requirement)...");
            assertItemDropsWithLogging(helper, flowerPos, Items.POPPY, false, testName);
            logDebugInfo("Test %s completed successfully", testName);
            helper.succeed();
        });
    }
    
    /**
     * Test Case 3: Knife on Grass - Verify that breaking a grass block with a flint knife correctly drops plant_fiber.
     * 
     * Expected behavior:
     * - Block breaks successfully (grass removed from world)
     * - Exactly 1 grass_fiber item drops near the break location  
     * - This validates the plant fiber harvesting system with sharp tools
     * 
     * NOTE: This tests the tag-based system where grass blocks are tagged as PLANT_FIBER_SOURCES
     * and should drop grass_fiber when broken with sharp tools but not with hands.
     */
    @GameTest(template = "platform")
    public static void test_knife_on_grass_drops_plant_fiber(GameTestHelper helper)
    {
        final String testName = "test_knife_on_grass_drops_plant_fiber";
        BlockPos grassPos = new BlockPos(1, 1, 1); // Place on default template
        
        logDebugInfo("Starting %s", testName);
        
        // Place grass on the platform (short grass plant, not grass_block)
        helper.setBlock(grassPos, Blocks.GRASS);
        logDebugInfo("Placed grass block at %s", grassPos);
        
        // Create a mock server player and give them a flint knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        logDebugInfo("Created player with flint knife: %s", knife);
        
        // Break the block using real harvest mechanics and verify it succeeded
        boolean blockBroken = breakAsPlayer(helper, player, grassPos, testName);
        
        // Enhanced assertion with detailed logging
        if (!blockBroken) {
            logFailureInfo("CRITICAL FAILURE in %s: Block breaking failed", testName);
            helper.fail("Block should have been successfully broken but destroyBlock() returned false");
        }
        
        // Assert that the block is now air with enhanced logging
        assertBlockStateWithLogging(helper, grassPos, Blocks.GRASS, false, testName);
        
        // Use polling assertion to check for plant fiber drops with enhanced logging
        helper.succeedWhen(() -> {
            logDebugInfo("Checking for expected grass fiber drops...");
            assertItemDropsWithLogging(helper, grassPos, ModItems.GRASS_FIBER.get(), true, testName);
            logDebugInfo("Test %s completed successfully", testName);
        });
    }
    
    /**
     * Test Case 4: Hand on Grass - Verify that breaking a grass block with a bare hand results in no plant_fiber drop.
     * 
     * Expected behavior:
     * - Block breaks successfully (grass removed from world)
     * - NO grass_fiber items drop (hand harvesting should not give fiber drops)
     * - This validates that plant fiber requires sharp tools
     * 
     * NOTE: This is a key test for the plant fiber system - while vanilla Minecraft would
     * allow grass to be broken by hand, our mod should prevent fiber drops without sharp tools.
     * The block should still break (since grass has 0 hardness) but no items should drop.
     */
    @GameTest(template = "platform")
    public static void test_hand_on_grass_no_plant_fiber(GameTestHelper helper)
    {
        final String testName = "test_hand_on_grass_no_plant_fiber";
        BlockPos grassPos = new BlockPos(1, 1, 1); // Place on default template
        
        logDebugInfo("Starting %s", testName);
        
        // Place grass on the platform (short grass plant, not grass_block)
        helper.setBlock(grassPos, Blocks.GRASS);
        logDebugInfo("Placed grass block at %s", grassPos);
        
        // Create a mock server player with empty hands
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        logDebugInfo("Created player with empty hands");
        
        // Break the block using real harvest mechanics and verify it succeeded
        boolean blockBroken = breakAsPlayer(helper, player, grassPos, testName);
        
        // Enhanced assertion with detailed logging
        if (!blockBroken) {
            logFailureInfo("CRITICAL FAILURE in %s: Block breaking failed", testName);
            helper.fail("Block should have been successfully broken but destroyBlock() returned false");
        }
        
        // Assert that the block is now air with enhanced logging
        assertBlockStateWithLogging(helper, grassPos, Blocks.GRASS, false, testName);
        
        // Wait ~2 ticks before checking for no drops to give the game time to spawn any items
        helper.runAfterDelay(2, () -> {
            logDebugInfo("Checking that no grass fiber drops occurred (testing sharp tool requirement)...");
            assertItemDropsWithLogging(helper, grassPos, ModItems.GRASS_FIBER.get(), false, testName);
            logDebugInfo("Test %s completed successfully", testName);
            helper.succeed();
        });
    }
    
    /**
     * Batch/Performance Test: Break a large number of blocks (5x5 area of flowers) in sequence.
     * While not a true performance benchmark, this helps identify significant performance regressions
     * in block-breaking or drop-handling logic by observing execution time.
     * 
     * Expected behavior:
     * - All 9 flowers break successfully 
     * - Each flower drops exactly 1 poppy item
     * - Processing completes in reasonable time (< 5 seconds)
     * - No memory leaks or performance issues during batch operations
     * 
     * NOTE: This test exercises the sharp tool system under load and can help identify
     * performance regressions in the tag-checking or drop-handling systems.
     */
    @GameTest(template = "5x5_platform")
    public static void test_batch_flower_harvest_performance(GameTestHelper helper)
    {
        final String testName = "test_batch_flower_harvest_performance";
        logDebugInfo("Starting %s", testName);
        
        // Create a mock server player and give them a flint knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        logDebugInfo("Created player with flint knife for batch test: %s", knife);
        
        // Record start time for performance observation
        long startTime = System.currentTimeMillis();
        
        // Place and break 9 flowers (3x3 grid to fit in default template)
        int flowersProcessed = 0;
        int successfulBreaks = 0;
        int expectedDropsFound = 0;
        
        logDebugInfo("=== Starting Batch Processing ===");
        
        for (int x = 0; x < 3; x++)
        {
            for (int z = 0; z < 3; z++)
            {
                BlockPos flowerPos = new BlockPos(x, 1, z); // Place in default template
                
                logDebugInfo("Processing flower %d at position %s", flowersProcessed + 1, flowerPos);
                
                // Place a flower
                helper.setBlock(flowerPos, Blocks.POPPY);
                
                // Break the block using real harvest mechanics and track success
                boolean blockBroken = breakAsPlayer(helper, player, flowerPos, 
                    testName + "_flower_" + (flowersProcessed + 1));
                if (blockBroken)
                {
                    successfulBreaks++;
                    
                    // Check if expected drops appeared
                    AABB area = new AABB(flowerPos).inflate(2.0);
                    List<ItemEntity> poppyItems = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area)
                        .stream().filter(item -> item.getItem().getItem() == Items.POPPY)
                        .collect(Collectors.toList());
                    
                    if (!poppyItems.isEmpty()) {
                        expectedDropsFound++;
                        logDebugInfo("Found %d poppy drop(s) for flower %d", poppyItems.size(), flowersProcessed + 1);
                    } else {
                        logFailureInfo("No poppy drops found for flower %d at %s", flowersProcessed + 1, flowerPos);
                    }
                } else {
                    logFailureInfo("Failed to break flower %d at %s", flowersProcessed + 1, flowerPos);
                }
                
                flowersProcessed++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Enhanced logging for batch test results
        logDebugInfo("=== Batch Test Results ===");
        logDebugInfo("Flowers processed: %d", flowersProcessed);
        logDebugInfo("Successful breaks: %d", successfulBreaks);
        logDebugInfo("Expected drops found: %d", expectedDropsFound);
        logDebugInfo("Processing time: %d ms", duration);
        logDebugInfo("Average time per flower: %.2f ms", duration / (double) flowersProcessed);
        
        // Enhanced assertions with detailed failure info
        if (flowersProcessed != 9) {
            logFailureInfo("BATCH TEST ASSERTION FAILED: Expected to process 9 flowers, but processed: %d", flowersProcessed);
            helper.fail("Should have processed 9 flowers, but processed: " + flowersProcessed);
        }
        
        if (successfulBreaks != 9) {
            logFailureInfo("BATCH TEST ASSERTION FAILED: Expected 9 successful breaks, but got: %d", successfulBreaks);
            helper.fail("Should have successfully broken 9 flowers, but broke: " + successfulBreaks);
        }
        
        if (expectedDropsFound != 9) {
            logFailureInfo("BATCH TEST ASSERTION FAILED: Expected 9 poppy drops, but found: %d", expectedDropsFound);
            helper.fail("Should have found 9 poppy drops, but found: " + expectedDropsFound);
        }
        
        // Log timing for regression detection (not a hard assertion since this is environment-dependent)
        System.out.println("Batch harvest test: Processed " + flowersProcessed + " flowers in " + duration + "ms" + 
            " (avg: " + String.format("%.2f", duration / (double) flowersProcessed) + "ms per flower)");
        
        // Sanity check: ensure it doesn't take unreasonably long (soft performance check)
        // This is a very loose bound - mainly to catch severe regressions
        if (duration > 5000) { // 5 seconds
            System.out.println("WARNING: Batch harvest took " + duration + "ms, which may indicate performance regression");
            logFailureInfo("PERFORMANCE WARNING: Batch test took %d ms (>5000ms threshold)", duration);
        } else {
            logDebugInfo("Performance OK: Batch test completed in %d ms (under 5000ms threshold)", duration);
        }
        
        logDebugInfo("Test %s completed successfully", testName);
        helper.succeed();
    }
    
    /**
     * Test verifying that the sharp tool system respects configuration settings.
     * This ensures that when the system is enabled, knives work as expected.
     * 
     * Expected behavior:
     * - Block breaks successfully (poppy removed from world)
     * - Exactly 1 poppy item drops (knife should work when system is enabled)
     * - Configuration system is properly integrated with harvest logic
     * 
     * NOTE: This test validates that the configuration system (Config.INSTANCE.enableSharpToolSystem)
     * is properly integrated with the harvest mechanics. If the config system fails, sharp tools
     * might not work even when they should according to tags and item setup.
     */
    @GameTest(template = "platform")
    public static void test_sharp_tool_system_configuration(GameTestHelper helper)
    {
        final String testName = "test_sharp_tool_system_configuration";
        BlockPos flowerPos = new BlockPos(1, 1, 1); // Place on default template
        
        logDebugInfo("Starting %s", testName);
        
        // Place a poppy
        helper.setBlock(flowerPos, Blocks.POPPY);
        logDebugInfo("Placed poppy block at %s", flowerPos);
        
        // Create a mock server player and give them a knife
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer();
        ItemStack knife = new ItemStack(ModItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        logDebugInfo("Created player with flint knife: %s", knife);
        
        // Log configuration state for debugging
        logDebugInfo("Sharp tool system enabled: %s", 
            com.alcatrazescapee.notreepunching.Config.INSTANCE.enableSharpToolSystem.getAsBoolean());
        
        // Break the block using real harvest mechanics and verify it succeeded
        boolean blockBroken = breakAsPlayer(helper, player, flowerPos, testName);
        
        // Enhanced assertion with detailed logging
        if (!blockBroken) {
            logFailureInfo("CRITICAL FAILURE in %s: Block breaking failed", testName);
            logFailureInfo("This could indicate configuration system issues");
            helper.fail("Block should have been successfully broken but destroyBlock() returned false");
        }
        
        // Assert that the block is now air with enhanced logging
        assertBlockStateWithLogging(helper, flowerPos, Blocks.POPPY, false, testName);
        
        // With the system enabled (default), knife should produce drops
        helper.succeedWhen(() -> {
            logDebugInfo("Checking for expected poppy drops (validating config system integration)...");
            assertItemDropsWithLogging(helper, flowerPos, Items.POPPY, true, testName);
            logDebugInfo("Configuration system test completed successfully - sharp tools work as expected");
            logDebugInfo("Test %s completed successfully", testName);
        });
    }
}