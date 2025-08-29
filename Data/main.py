#  Part of the No Tree Punching mod by AlcatrazEscapee.
#  Work under copyright. See the project LICENSE.md for details.

from mcresources import ResourceManager, utils, loot_tables, advancements
from mcresources.type_definitions import ResourceIdentifier, Json
from typing import Optional, Sequence


def main():
    mod_id = 'notreepunching'
    root_dir = '../%s/src/main/resources'

    fabric = ResourceManager(mod_id, root_dir % 'Fabric')
    forge = ResourceManager(mod_id, root_dir % 'Forge')
    common = ResourceManager(mod_id, root_dir % 'Common')
    each = (fabric, forge, common)

    # Link lang, generate them to common
    fabric.lang_buffer = forge.lang_buffer = common.lang_buffer

    for rm in each:
        utils.clean_generated_resources('/'.join(rm.resource_dir))

    do_assets(common)
    do_advancements(common)
    do_tags(forge, fabric, common)
    do_recipes(common)
    do_loot_tables(common)
    do_world_gen(forge, common)

    common.flush()
    forge.flush()
    fabric.flush()


def do_assets(common: ResourceManager):
    # First
    common.lang({
        'itemGroup.notreepunching.items': 'No Tree Punching',
        'notreepunching.tooltip.small_vessel_more': '%d More...',
        'notreepunching.block_entity.large_vessel': 'Large Vessel',
        'notreepunching.subtitle.knapping': 'Rock smacks'
    })

    # Stone
    for stone in ('granite', 'andesite', 'diorite'):
        block = common.blockstate('%s_cobblestone' % stone)
        block.with_block_model()
        block.with_item_model()
        block.with_tag('minecraft:mineable/pickaxe')
        block.with_block_loot('notreepunching:%s_cobblestone' % stone)
        block.with_lang(lang('%s cobblestone', stone))
        block.make_stairs()
        block.make_slab()
        block.make_wall()
        for piece in ('stairs', 'slab', 'wall'):
            block = common.block('%s_cobblestone_%s' % (stone, piece))
            block.with_lang(lang('%s cobblestone %s', stone, piece))
            block.with_tag('minecraft:' + piece + ('s' if not piece.endswith('s') else ''))  # plural tag

    for stone in ('granite', 'andesite', 'diorite', 'stone', 'sandstone', 'red_sandstone'):
        block = common.blockstate('%s_loose_rock' % stone)
        block.with_block_model(textures='minecraft:block/%s' % stone, parent='notreepunching:block/loose_rock')
        block.with_block_loot('notreepunching:%s_loose_rock' % stone)
        block.with_lang(lang('%s loose rock', stone))
        block.with_tag('loose_rocks')
        block.with_tag('minecraft:mineable/pickaxe')

        # flat item model for the block item
        item = common.item_model('%s_loose_rock' % stone)
        item.with_tag('loose_rocks')  # item tag is needed for recipes

    # Pottery
    for pottery in ('worked', 'large_vessel', 'small_vessel', 'bucket', 'flower_pot'):
        block = common.blockstate('clay_%s' % pottery)
        block.with_block_model(textures='minecraft:block/clay', parent='notreepunching:block/pottery_%s' % pottery)
        block.with_item_model()
        block.with_block_loot('notreepunching:clay_%s' % pottery)
        block.with_tag('minecraft:mineable/pickaxe')
        if pottery == 'worked':
            block.with_lang(lang('worked clay'))
        else:
            block.with_lang(lang('clay %s', pottery))

    loot_tables.copy_block_entity_name(),
    loot_tables.copy_block_entity_nbt()
    block = common.blockstate('ceramic_large_vessel')
    block.with_block_model(textures='notreepunching:block/ceramic', parent='notreepunching:block/pottery_large_vessel')
    block.with_item_model()
    block.with_block_loot({
        'name': 'notreepunching:ceramic_large_vessel',
        'functions': [
            loot_tables.copy_block_entity_name(),
            loot_tables.copy_block_entity_nbt()
        ],
    })
    block.with_tag('minecraft:mineable/pickaxe')
    block.with_lang(lang('ceramic large vessel'))

    # Tools
    for tool in ('iron', 'gold', 'diamond', 'netherite'):
        item = common.item_model('%s_mattock' % tool, parent='item/handheld')
        item.with_lang(lang('%s mattock', tool))

        item = common.item_model('%s_saw' % tool, parent='item/handheld')
        item.with_lang(lang('%s saw', tool))

        item = common.item_model('%s_knife' % tool, parent='item/handheld')
        item.with_lang(lang('%s knife', tool))

    # Flint
    for tool in ('axe', 'pickaxe', 'shovel', 'hoe', 'knife'):
        item = common.item_model('flint_%s' % tool, parent='item/handheld')
        item.with_lang(lang('flint %s', tool))

    item = common.item_model('macuahuitl', parent='item/handheld')
    item.with_lang(lang('macuahuitl'))

    for item_name in ('flint_shard', 'plant_fiber', 'plant_string', 'clay_brick', 'ceramic_small_vessel', 'ceramic_bucket', 'ceramic_water_bucket', 'clay_tool', 'fire_starter'):
        item = common.item_model(item_name)
        item.with_lang(lang(item_name))


def do_advancements(common: ResourceManager):
    story = AdvancementBuilder(common, 'story', 'minecraft:textures/gui/advancements/backgrounds/stone.png')

    story.advancement('root', 'notreepunching:flint_pickaxe', 'No Tree Punching', 'I tried to punch tree. It didn\'t work and now my fingers are covered in splinters...', None, {
        'has_loose_rock': advancements.inventory_changed('#notreepunching:loose_rocks'),
        'has_gravel': advancements.inventory_changed('minecraft:gravel'),
        'has_sticks': advancements.inventory_changed('minecraft:stick'),
    }, requirements=[['has_loose_rock', 'has_gravel', 'has_sticks']], toast=False, chat=False)

    story.advancement('find_loose_rock', 'notreepunching:stone_loose_rock', 'Dull Rocks', 'Pick up a loose rock.', 'root', {'has_loose_rock': advancements.inventory_changed('#notreepunching:loose_rocks')})
    story.advancement('find_gravel', 'minecraft:gravel', 'Discount Cobblestone', 'Find some gravel, it may come in handy.', 'root', {
        'has_gravel': advancements.inventory_changed('minecraft:gravel'),
        'has_flint': advancements.inventory_changed('minecraft:flint')
    }, requirements=[['has_gravel', 'has_flint']])
    story.advancement('find_sticks', 'minecraft:stick', 'A Big Stick', 'Obtain sticks by breaking leaves.', 'root', {'has_stick': advancements.inventory_changed('minecraft:stick')})

    story.advancement('find_flint', 'minecraft:flint', 'Shiny Rocks!', 'Obtain some flint by digging through gravel.', 'find_gravel', {'has_flint': advancements.inventory_changed('minecraft:flint')})

    story.advancement('knapping', 'notreepunching:flint_shard', 'Knapit!', 'Use a piece of flint on some exposed stone, to break it into small flint shards.', 'find_flint', {'has_flint_shard': advancements.inventory_changed('notreepunching:flint_shard')})

    story.advancement('plant_fiber', 'notreepunching:plant_fiber', 'Plant Based Tool Bindings', 'With a primitive flint knife, obtain plant fiber by cutting down tall grasses.', 'knapping', {'has_plant_fiber': advancements.inventory_changed('notreepunching:plant_fiber')})

    story.advancement('flint_axe', 'notreepunching:flint_axe', 'And My Axe!', 'Build your first tool capable of harvesting wood!', 'plant_fiber', {'has_flint_axe': advancements.inventory_changed('notreepunching:flint_axe')})

    story.advancement('macuahuitl', 'notreepunching:macuahuitl', 'Macaroniwhatnow?', 'Craft a macuahuitl', 'flint_axe', {'has_macuahuitl': advancements.inventory_changed('notreepunching:macuahuitl')})
    story.advancement('flint_pickaxe', 'notreepunching:flint_pickaxe', 'My First Pickaxe', 'Craft your first pickaxe from flint, plant fiber, and sticks!', 'flint_axe', {'has_flint_pickaxe': advancements.inventory_changed('notreepunching:flint_pickaxe')})

    story.advancement('use_clay_tool', 'notreepunching:clay_large_vessel', 'You\'re a Potter, Harry', 'Use a clay tool on a block of clay to create pottery of various kinds.', 'find_sticks', {'damage_clay_tool': use_item_on_block('notreepunching:clay_tool', 'notreepunching:pottery')})
    story.advancement('fire_pottery', 'notreepunching:ceramic_large_vessel', 'Ceramics', 'Fire some pottery into useful devices!', 'use_clay_tool', {'has_ceramics': advancements.inventory_changed('#notreepunching:ceramics')})

    story.advancement('mattock', 'notreepunching:iron_mattock', 'Getting a Better Upgrade', 'Craft a mattock, a hoe-axe-shovel-all-in-one multitool!', 'flint_pickaxe', {'has_mattock': advancements.inventory_changed('#notreepunching:mattocks')})


def do_tags(forge: ResourceManager, fabric: ResourceManager, common: ResourceManager):
    # Tags use the following structure
    # ntp:tag ⊃ {
    #   forge:tag?,
    #   c:tag?,
    #   minecraft:tag?,
    #   minecraft:item...,
    #   ntp:h/tag ⊃ {
    #     ntp:item...
    #   }
    # }
    # Referencing tags in recipes always uses ntp:tag
    # Referencing tags in loader specific tags always uses ntp:h/tag

    # /h/tags/
    common.item_tag('h/string', 'notreepunching:plant_string')
    block_item_tag(common, 'h/cobblestone', 'notreepunching:andesite_cobblestone', 'notreepunching:granite_cobblestone', 'notreepunching:diorite_cobblestone')

    for tool in ('iron', 'gold', 'diamond', 'netherite'):
        common.item_tag('h/mattocks', 'notreepunching:%s_mattock' % tool)
        common.item_tag('h/knives', 'notreepunching:%s_knife' % tool)
        common.item_tag('h/saws', 'notreepunching:%s_saw' % tool)

    common.item_tag('h/weak_saws', 'notreepunching:flint_axe')
    common.item_tag('h/knives', 'notreepunching:flint_knife')

    # /tags/
    common.item_tag('sticks', 'minecraft:stick', '#c:wood_sticks?', '#c:wooden_rods?', '#forge:rods/wooden?')
    common.item_tag('string', 'minecraft:string', '#notreepunching:h/string', '#c:string?', '#forge:string?')

    common.item_tag('mattocks', '#notreepunching:h/mattocks', '#c:mattocks?', '#forge:tools/mattocks?')
    common.item_tag('knives', '#notreepunching:h/knives', '#c:knives?', '#forge:tools/knives?')
    common.item_tag('saws', '#notreepunching:h/saws', '#c:saws?', '#forge:tools/saws?')
    common.item_tag('weak_saws', '#notreepunching:h/weak_saws')

    common.item_tag('iron_ingots', 'minecraft:iron_ingot', '#c:iron_ingots?', '#forge:ingots/iron?')
    common.item_tag('gold_ingots', 'minecraft:gold_ingot', '#c:gold_ingots?', '#forge:ingots/gold?')
    common.item_tag('diamond_ingots', 'minecraft:diamond', '#c:diamonds?', '#forge:ingots/diamond?')

    common.item_tag('shears', 'minecraft:shears', '#c:shears?', '#forge:shears?')

    # c:tags -> /h/tags/
    fabric.item_tag('c:string', '#notreepunching:h/string')
    block_item_tag(fabric, 'c:cobblestone', '#notreepunching:h/cobblestone')
    block_item_tag(fabric, 'c:cobblestones', '#notreepunching:h/cobblestone')

    # forge:tags -> /h/tags/
    forge.item_tag('forge:string', '#notreepunching:h/string')
    block_item_tag(forge, 'forge:cobblestone', '#notreepunching:h/cobblestone')

    # minecraft:tags -> /h/tags/
    common.item_tag('minecraft:stone_tool_materials', '#notreepunching:h/cobblestone')
    common.item_tag('minecraft:stone_crafting_materials', '#notreepunching:h/cobblestone')

    # Misc Tags
    common.block_tag('needs_with_flint_tool')
    common.block_tag('mineable_with_mattock', '#minecraft:mineable/shovel', '#minecraft:mineable/hoe', '#minecraft:mineable/axe')
    common.block_tag('mineable', '#notreepunching:mineable_with_mattock', '#minecraft:mineable/pickaxe', '#minecraft:mineable/shovel', '#minecraft:mineable/hoe', '#minecraft:mineable/axe', '#minecraft:sword_efficient')
    common.item_tag('pickaxe_tools')
    common.item_tag('axe_tools', '#notreepunching:mattocks')
    common.item_tag('shovel_tools', '#notreepunching:mattocks')
    common.item_tag('hoe_tools', '#notreepunching:mattocks')
    common.item_tag('sharp_tools', '#notreepunching:knives')

    # Add plant fiber sources block tag for future extensibility
    common.block_tag('plant_fiber_sources', 'minecraft:grass', 'minecraft:tall_grass')

    common.block('minecraft:gravel').with_tag('always_breakable').with_tag('always_drops')

    common.item_tag('weak_saws', 'minecraft:iron_axe', 'minecraft:golden_axe', 'minecraft:diamond_axe', 'minecraft:netherite_axe')

    common.block_tag('always_breakable', '#minecraft:leaves', 'minecraft:gravel', '#minecraft:dirt', 'minecraft:grass', 'minecraft:podzol', 'minecraft:coarse_dirt', '#minecraft:sand', '#notreepunching:loose_rocks', '#minecraft:snow')
    common.block_tag('always_drops', '#minecraft:leaves', 'minecraft:gravel', '#minecraft:dirt', 'minecraft:grass', 'minecraft:podzol', 'minecraft:coarse_dirt', '#minecraft:sand', '#notreepunching:loose_rocks')

    common.item_tag('fire_starter_logs', '#minecraft:logs', '#minecraft:planks')
    common.item_tag('fire_starter_kindling', '#notreepunching:sticks', '#minecraft:saplings', '#minecraft:leaves', 'minecraft:string', '#notreepunching:string', 'notreepunching:plant_fiber')
    common.item_tag('fire_starter_soul_fire_catalyst', 'minecraft:soul_sand', 'minecraft:soul_soil')

    ceramics = ['notreepunching:ceramic_large_vessel', 'notreepunching:ceramic_small_vessel', 'notreepunching:ceramic_bucket', 'minecraft:flower_pot']
    pottery = ['minecraft:clay', 'notreepunching:clay_worked', 'notreepunching:clay_large_vessel', 'notreepunching:clay_small_vessel', 'notreepunching:clay_bucket', 'notreepunching:clay_flower_pot']

    common.item_tag('ceramics', *ceramics)
    common.item_tag('pottery', *pottery)

    common.block_tag('pottery', *pottery)
    common.block_tag('minecraft:mineable/shovel', *pottery)

    common.item('ceramic_small_vessel').with_tag('large_vessel_blacklist').with_tag('small_vessel_blacklist')
    common.item('ceramic_large_vessel').with_tag('large_vessel_blacklist').with_tag('small_vessel_blacklist')
    common.item('minecraft:shulker_box').with_tag('large_vessel_blacklist').with_tag('small_vessel_blacklist')
    for color in ('white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black'):
        common.item('minecraft:%s_shulker_box' % color).with_tag('large_vessel_blacklist').with_tag('small_vessel_blacklist')

    # Vanilla Tags
    common.item('minecraft:flint').with_tag('notreepunching:flint_knappable')
    for block in ('grass_block', 'dirt', 'coarse_dirt', 'gravel', 'sand', 'red_sand', 'terracotta', 'stone', 'andesite', 'diorite', 'granite', 'sandstone', 'red_sandstone', 'podzol'):
        common.block('minecraft:%s' % block).with_tag('notreepunching:loose_rock_placeable_on')


def block_item_tag(rm: ResourceManager, name_parts: ResourceIdentifier, *values: ResourceIdentifier):
    rm.item_tag(name_parts, *values)
    rm.block_tag(name_parts, *values)


def do_recipes(common: ResourceManager):
    # Loose rocks
    common.crafting_shaped('cobblestone_from_rocks', ('XX', 'XX'), {'X': 'notreepunching:stone_loose_rock'}, 'minecraft:cobblestone').with_advancement('notreepunching:stone_loose_rock')
    for stone in ('andesite', 'diorite', 'granite'):
        common.crafting_shaped('%s_cobblestone_from_rocks' % stone, ('XX', 'XX'), {'X': 'notreepunching:%s_loose_rock' % stone}, 'notreepunching:%s_cobblestone' % stone).with_advancement('notreepunching:%s_loose_rock' % stone)
    for stone in ('sandstone', 'red_sandstone'):
        common.crafting_shaped('%s_from_rocks' % stone, ('XX', 'XX'), {'X': 'notreepunching:%s_loose_rock' % stone}, 'minecraft:%s' % stone).with_advancement('notreepunching:%s_loose_rock' % stone)

    # Stairs, Slabs, Walls
    for stone in ('andesite', 'diorite', 'granite'):
        cobble = 'notreepunching:%s_cobblestone' % stone

        # crafting recipes
        common.crafting_shaped('%s_cobblestone_stairs' % stone, ('X  ', 'XX ', 'XXX'), {'X': cobble}, (4, cobble + '_stairs')).with_advancement(cobble)
        common.crafting_shaped('%s_cobblestone_slab' % stone, ('XXX',), {'X': cobble}, (6, cobble + '_slab')).with_advancement(cobble)
        common.crafting_shaped('%s_cobblestone_wall' % stone, ('XXX', 'XXX'), {'X': cobble}, (6, cobble + '_wall')).with_advancement(cobble)

        # stone cutting
        common.recipe(('stonecutting', '%s_cobblestone_stairs' % stone), 'minecraft:stonecutting', {
            'ingredient': utils.ingredient(cobble),
            'result': cobble + '_stairs',
            'count': 1
        }).with_advancement(cobble)
        common.recipe(('stonecutting', '%s_cobblestone_slab' % stone), 'minecraft:stonecutting', {
            'ingredient': utils.ingredient(cobble),
            'result': cobble + '_slab',
            'count': 2
        })
        common.recipe(('stonecutting', '%s_cobblestone_wall' % stone), 'minecraft:stonecutting', {
            'ingredient': utils.ingredient(cobble),
            'result': cobble + '_wall',
            'count': 1
        })

        # smelting
        common.recipe(('smelting', '%s_from_cobblestone' % stone), 'minecraft:smelting', {
            'ingredient': utils.ingredient(cobble),
            'result': 'minecraft:%s' % stone,
            'experience': 0.1,
            'cookingtime': 200
        })

    # Clay tool
    tool_damaging_shapeless(common, 'clay_brick_from_balls', ('notreepunching:clay_tool', 'minecraft:clay_ball'), 'notreepunching:clay_brick').with_advancement('minecraft:clay_ball')
    tool_damaging_shapeless(common, 'clay_brick_from_blocks', ('notreepunching:clay_tool', 'minecraft:clay'), (4, 'notreepunching:clay_brick')).with_advancement('minecraft:clay')

    # Misc
    common.crafting_shapeless('plant_string', ['notreepunching:plant_fiber'] * 3, 'notreepunching:plant_string').with_advancement('notreepunching:plant_fiber')
    common.recipe(('smelting', 'string_from_plant_string'), 'minecraft:smelting', {
        'ingredient': utils.ingredient('notreepunching:plant_string'),
        'result': 'minecraft:string',
        'experience': 0.1,
        'cookingtime': 200
    })
    common.crafting_shapeless('flint_from_gravel', ['minecraft:gravel'] * 3, (2, 'minecraft:flint')).with_advancement('minecraft:gravel')

    # Sticks
    tool_damaging_shaped(common, 'sticks_from_logs_with_saw', ('SW',), {'S': '#notreepunching:saws', 'W': '#minecraft:logs'}, (8, 'minecraft:stick')).with_advancement('#minecraft:logs')
    tool_damaging_shaped(common, 'sticks_from_planks_with_saw', ('SW',), {'S': '#notreepunching:saws', 'W': '#minecraft:planks'}, (2, 'minecraft:stick')).with_advancement('#minecraft:planks')

    tool_damaging_shaped(common, 'sticks_from_logs_with_flint_axe', ('SW',), {'S': '#notreepunching:weak_saws', 'W': '#minecraft:logs'}, (6, 'minecraft:stick')).with_advancement('#minecraft:logs')
    tool_damaging_shaped(common, 'sticks_from_planks_with_flint_axe', ('SW',), {'S': '#notreepunching:weak_saws', 'W': '#minecraft:planks'}, (1, 'minecraft:stick')).with_advancement('#minecraft:planks')

    # Tools
    for tool in ('iron', 'gold', 'diamond'):
        ingot = '#notreepunching:%s_ingots' % tool
        common.crafting_shaped('%s_knife' % tool, ('I', 'S'), {'S': '#notreepunching:sticks', 'I': ingot}, 'notreepunching:%s_knife' % tool).with_advancement(ingot)
        common.crafting_shaped('%s_mattock' % tool, ('III', ' SI', ' S '), {'S': '#notreepunching:sticks', 'I': ingot}, 'notreepunching:%s_mattock' % tool).with_advancement(ingot)
        common.crafting_shaped('%s_saw' % tool, ('  S', ' SI', 'SI '), {'S': '#notreepunching:sticks', 'I': ingot}, 'notreepunching:%s_saw' % tool).with_advancement(ingot)

    for tool in ('knife', 'mattock', 'saw'):
        common.recipe('netherite_%s' % tool, 'minecraft:smithing_transform', {
            'base': utils.item_stack('notreepunching:diamond_%s' % tool),
            'addition': utils.item_stack('minecraft:netherite_ingot'),
            'result': utils.item_stack('notreepunching:netherite_%s' % tool),
            'template': {'item': 'minecraft:netherite_upgrade_smithing_template'}
        }).with_advancement('minecraft:netherite_ingot')

    # Flint Tools
    common.crafting_shaped('flint_axe', ('PI', 'S '), {'S': '#notreepunching:sticks', 'I': 'notreepunching:flint_shard', 'P': '#notreepunching:string'}, 'notreepunching:flint_axe').with_advancement('notreepunching:flint_shard')
    common.crafting_shaped('flint_hoe', ('PII', 'S  ', 'S  '), {'S': '#notreepunching:sticks', 'I': 'notreepunching:flint_shard', 'P': '#notreepunching:string'}, 'notreepunching:flint_hoe').with_advancement('notreepunching:flint_shard')
    common.crafting_shaped('flint_knife', ('I', 'S'), {'S': '#notreepunching:sticks', 'I': 'notreepunching:flint_shard'}, 'notreepunching:flint_knife').with_advancement('notreepunching:flint_shard')
    common.crafting_shaped('flint_pickaxe', ('IPI', 'ISI', ' S '), {'S': '#notreepunching:sticks', 'I': 'notreepunching:flint_shard', 'P': '#notreepunching:string'}, 'notreepunching:flint_pickaxe').with_advancement('notreepunching:flint_shard')
    common.crafting_shaped('flint_shovel', (' II', ' PI', 'S  '), {'S': '#notreepunching:sticks', 'I': 'notreepunching:flint_shard', 'P': '#notreepunching:string'}, 'notreepunching:flint_shovel').with_advancement('notreepunching:flint_shard')
    common.crafting_shaped('macuahuitl', (' IS', 'ISI', 'SI '), {'S': '#notreepunching:sticks', 'I': 'notreepunching:flint_shard'}, 'notreepunching:macuahuitl').with_advancement('notreepunching:flint_shard')

    # Misc Tools
    common.crafting_shaped('clay_tool', ('  I', ' II', 'I  '), {'I': '#notreepunching:sticks'}, 'notreepunching:clay_tool').with_advancement('#notreepunching:sticks')
    common.crafting_shaped('fire_starter', ('SP', 'FS'), {'S': '#notreepunching:sticks', 'P': '#notreepunching:string', 'F': 'notreepunching:flint_shard'}, 'notreepunching:fire_starter').with_advancement('notreepunching:flint_shard')

    # Pottery firing
    for pottery in ('large_vessel', 'small_vessel', 'bucket', 'flower_pot', 'brick'):
        clay = 'notreepunching:clay_' + pottery
        if pottery == 'flower_pot':
            fired = 'minecraft:flower_pot'
        elif pottery == 'brick':
            fired = 'minecraft:brick'
        else:
            fired = 'notreepunching:ceramic_' + pottery
        common.recipe(('smelting', pottery), 'minecraft:smelting', {
            'ingredient': utils.ingredient(clay),
            'result': fired,
            'experience': 0.1,
            'cookingtime': 200
        })
        common.recipe(('campfire', pottery), 'minecraft:campfire_cooking', {
            'ingredient': utils.ingredient(clay),
            'result': fired,
            'experience': 0.1,
            'cookingtime': 600
        })

    # Knife crafting
    knife = '#notreepunching:knives'
    tool_damaging_shapeless(common, 'string_from_wool_with_knife', ('#minecraft:wool', knife), (4, 'minecraft:string')).with_advancement('#minecraft:wool')
    tool_damaging_shapeless(common, 'string_from_web_with_knife', ('minecraft:cobweb', knife), (8, 'minecraft:string')).with_advancement('minecraft:cobweb')
    tool_damaging_shapeless(common, 'plant_fiber_from_sugarcane_with_knife', ('minecraft:sugar_cane', knife), (3, 'notreepunching:plant_fiber')).with_advancement('minecraft:sugar_cane')
    tool_damaging_shapeless(common, 'plant_fiber_from_wheat_with_knife', ('minecraft:wheat', knife), (2, 'notreepunching:plant_fiber')).with_advancement('minecraft:wheat')
    tool_damaging_shapeless(common, 'plant_fiber_from_vines_with_knife', ('minecraft:vine', knife), (5, 'notreepunching:plant_fiber')).with_advancement('minecraft:vine')
    tool_damaging_shapeless(common, 'plant_fiber_from_cactus_with_knife', ('minecraft:cactus', knife), (3, 'notreepunching:plant_fiber')).with_advancement('minecraft:cactus')
    tool_damaging_shapeless(common, 'plant_fiber_from_leaves_with_knife', ('#minecraft:leaves', knife), 'notreepunching:plant_fiber').with_advancement('#minecraft:leaves')
    tool_damaging_shapeless(common, 'plant_fiber_from_saplings_with_knife', ('#minecraft:saplings', knife), (2, 'notreepunching:plant_fiber')).with_advancement('#minecraft:saplings')
    tool_damaging_shapeless(common, 'plant_fiber_from_small_flowers_with_knife', ('#minecraft:small_flowers', knife), 'notreepunching:plant_fiber').with_advancement('#minecraft:small_flowers')
    tool_damaging_shapeless(common, 'plant_fiber_from_tall_flowers_with_knife', ('#minecraft:tall_flowers', knife), (2, 'notreepunching:plant_fiber')).with_advancement('#minecraft:tall_flowers')

    tool_damaging_shapeless(common, 'leather_from_boots_with_knife', ('minecraft:leather_boots', knife), (3, 'minecraft:leather'), tool='#notreepunching:knives').with_advancement('minecraft:leather_boots')
    tool_damaging_shapeless(common, 'leather_from_leggings_with_knife', ('minecraft:leather_leggings', knife), (6, 'minecraft:leather'), tool='#notreepunching:knives').with_advancement('minecraft:leather_leggings')
    tool_damaging_shapeless(common, 'leather_from_chestplate_with_knife', ('minecraft:leather_chestplate', knife), (7, 'minecraft:leather'), tool='#notreepunching:knives').with_advancement('minecraft:leather_chestplate')
    tool_damaging_shapeless(common, 'leather_from_helmet_with_knife', ('minecraft:leather_helmet', knife), (4, 'minecraft:leather'), tool='#notreepunching:knives').with_advancement('minecraft:leather_helmet')

    tool_damaging_shapeless(common, 'melon_slices_with_knife', ('minecraft:melon', knife), (9, 'minecraft:melon_slice')).with_advancement('minecraft:melon')

    remove_recipe(common, 'minecraft:stick')

    # Remove wood and stone tools
    for tool in ('pickaxe', 'shovel', 'hoe', 'sword', 'axe'):
        remove_recipe(common, 'minecraft:wooden_%s' % tool)
        remove_recipe(common, 'minecraft:stone_%s' % tool)

    remove_recipe(common, 'minecraft:campfire')
    remove_recipe(common, 'minecraft:soul_campfire')
    remove_recipe(common, 'minecraft:flower_pot')
    remove_recipe(common, 'minecraft:brick')


def do_loot_tables(common: ResourceManager):
    # Add optional plant fiber to loot tables
    common.block_loot('minecraft:grass', ({
        'name': 'minecraft:grass',
        'conditions': loot_tables.match_tag('notreepunching:shears')
    }, {
        'name': 'notreepunching:plant_fiber',
        'conditions': [
            loot_tables.match_tag('notreepunching:sharp_tools'),
            loot_tables.random_chance(0.25)
        ]
    }, {
        'name': 'minecraft:wheat_seeds',
        'conditions': [
            loot_tables.match_tag('notreepunching:sharp_tools'),
            loot_tables.random_chance(0.125)
        ],
        'functions': [
            loot_tables.fortune_bonus(2),
            'minecraft:explosion_decay'
        ]
    }))
    common.block_loot('minecraft:tall_grass', ({
         'name': 'minecraft:grass',
         'conditions': loot_tables.match_tag('notreepunching:shears')
     }, {
         'name': 'notreepunching:plant_fiber',
         'conditions': [
             loot_tables.match_tag('notreepunching:sharp_tools'),
             loot_tables.random_chance(0.25)
         ]
     }, {
         'name': 'minecraft:wheat_seeds',
         'conditions': [
             loot_tables.match_tag('notreepunching:sharp_tools'),
             loot_tables.survives_explosion(),
             loot_tables.block_state_property('minecraft:tall_grass[half=lower]'),
             loot_tables.random_chance(0.125)
         ]
     }))


def do_world_gen(forge: ResourceManager, common: ResourceManager):
    common.configured_feature('loose_rocks', 'notreepunching:loose_rocks')
    common.placed_feature('loose_rocks', 'notreepunching:loose_rocks', ('count', {'count': 5}), 'minecraft:in_square', ('minecraft:heightmap', {'heightmap': 'WORLD_SURFACE'}), 'minecraft:biome')

    forge.data(('forge', 'biome_modifier', 'add_loose_rocks'), {
        'type': 'forge:add_features',
        'biomes': '#minecraft:is_overworld',
        'features': 'notreepunching:loose_rocks',
        'step': 'top_layer_modification'
    })


def forge_mod_loaded(mod_id: str):
    return {
        'type': 'forge:mod_loaded',
        'modid': mod_id
    }


def tool_damaging_shaped(rm: ResourceManager, name_parts: ResourceIdentifier, pattern: Sequence[str], ingredients: Json, result: utils.Json, group: str = None, conditions: Optional[Json] = None, tool: Optional[Json] = None):
    return rm.recipe(name_parts, 'notreepunching:tool_damaging_shaped', {
        'recipe': {
            'type': 'minecraft:crafting_shaped',
            'group': group,
            'pattern': pattern,
            'key': utils.item_stack_dict(ingredients, ''.join(pattern)[0]),
            'result': utils.item_stack(result),
        },
        'tool': tool if tool is None else utils.ingredient(tool)
    }, conditions=conditions)


def tool_damaging_shapeless(rm: ResourceManager, name_parts: ResourceIdentifier, ingredients: Json, result: Json, group: str = None, conditions: Optional[Json] = None, tool: Optional[Json] = None):
    return rm.recipe(name_parts, 'notreepunching:tool_damaging_shapeless', {
        'recipe': {
            'type': 'minecraft:crafting_shapeless',
            'group': group,
            'ingredients': utils.item_stack_list(ingredients),
            'result': utils.item_stack(result),
        },
        'tool': tool if tool is None else utils.ingredient(tool)
    }, conditions=conditions)


def remove_recipe(rm: ResourceManager, name_parts: utils.ResourceIdentifier):
    rm.recipe(name_parts, 'notreepunching:empty', {})


def use_item_on_block(item: str, block: str):
    return {
        'trigger': 'minecraft:item_used_on_block',
        'conditions': {
            'location': [
                {
                    'condition': 'minecraft:location_check',
                    'predicate': {
                        'block': {'tag': block}
                    }
                },
                {
                    'condition': 'minecraft:match_tool',
                    'predicate': {
                        'items': [item]
                    }
                }
            ],
        }
    }


def lang(key: str, *args) -> str:
    return ((key % args) if len(args) > 0 else key).replace('_', ' ').replace('/', ' ').title()


class AdvancementBuilder:

    def __init__(self, rm: ResourceManager, category: str, background: str):
        self.rm = rm
        self.category = category
        self.background = background

    def advancement(self, name: str, icon: utils.Json, title: str, description: str, parent: Optional[str], criteria: utils.Json, requirements: utils.Json = None, frame: str = 'task', toast: bool = True, chat: bool = True, hidden: bool = False):
        key = '%s.advancements.%s.%s' % (self.rm.domain, self.category, name)

        if parent is not None:
            parent = utils.resource_location(self.rm.domain, self.category + '/' + parent).join()

        self.rm.advancement((self.category, name), {
            'icon': utils.item_stack(icon),
            'title': {'translate': key + '.title'},
            'description': {'translate': key + '.description'},
            'frame': frame,
            'show_toast': toast,
            'announce_to_chat': chat,
            'hidden': hidden,
            'background': self.background
        }, parent, criteria, requirements)
        self.rm.lang(key + '.title', title)
        self.rm.lang(key + '.description', description)


if __name__ == '__main__':
    main()
