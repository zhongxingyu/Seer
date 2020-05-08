 package no.runsafe.framework.minecraft;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.minecraft.IEnchant;
 import no.runsafe.framework.api.minecraft.IEnchantable;
 import no.runsafe.framework.api.minecraft.RunsafeEntityType;
 import no.runsafe.framework.internal.LegacyMaterial;
 import no.runsafe.framework.minecraft.entity.RunsafeItem;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.minecraft.material.RunsafeMaterialData;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.material.MaterialData;
 
 import javax.annotation.Nullable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Item implements IEnchantable
 {
 	public static final byte AnyData = -1;
 
 	@Nullable
 	public static Item get(RunsafeMeta itemStack)
 	{
 		return itemStack == null ? null : itemStack.getItemType();
 	}
 
 	@Nullable
 	public static Item get(IBlock block)
 	{
 		return block == null ? null : block.getMaterial();
 	}
 
 	@Nullable
 	public static Item get(MaterialData material)
 	{
 		return material == null ? null : getItem(material.getItemType(), material.getData());
 	}
 
 	@Nullable
 	public static Item get(Material material, byte damage)
 	{
 		if (material == null)
 			return null;
 		return getItem(material, damage);
 	}
 
 	@Nullable
 	public static Item get(String type)
 	{
 		Matcher id = materialId.matcher(type);
 		if (id.matches())
			return getItem(LegacyMaterial.getById(Integer.valueOf(id.group(1))), Byte.valueOf(id.group(2)));
 		Material material = Material.getMaterial(type);
 		if (material == null)
 			for (Material candidate : Material.values())
 				if (candidate.name().replace("_", "").equalsIgnoreCase(type))
 					return getItem(candidate, (byte) 0);
 
 		if (material == null)
 			return null;
 
 		return getItem(material, (byte) 0);
 	}
 
 	@Nullable
 	public static Item get(String type, byte damage)
 	{
 		Material material = Material.getMaterial(type);
 		if (material == null)
 			return null;
 		return getItem(material, damage);
 	}
 
 	@SuppressWarnings("StaticVariableOfConcreteClass")
 	public static final class BuildingBlock
 	{
 		public static final Item Stone = new Item(Material.STONE, true);
 		public static final Item Grass = new Item(Material.GRASS, true);
 		public static final Item Dirt = new Item(Material.DIRT, true);
 		public static final Item Podzol = new Item(Material.DIRT, true, (byte) 2);
 		public static final Item Cobblestone = new Item(Material.COBBLESTONE, true);
 		public static final Item Bedrock = new Item(Material.BEDROCK, true);
 		public static final Item Sand = new Item(Material.SAND, true);
 		public static final Item Gravel = new Item(Material.GRAVEL, true);
 		public static final Item Sponge = new Item(Material.SPONGE, true);
 		public static final Item Glass = new Item(Material.GLASS, true);
 		public static final Item LapisLazuli = new Item(Material.LAPIS_BLOCK, true);
 		public static final Item GoldBlock = new Item(Material.GOLD_BLOCK, true);
 		public static final Item IronBlock = new Item(Material.IRON_BLOCK, true);
 		public static final Item Brick = new Item(Material.BRICK, true);
 		public static final Item Bookshelf = new Item(Material.BOOKSHELF, true);
 		public static final Item MossyCobblestone = new Item(Material.MOSSY_COBBLESTONE, true);
 		public static final Item Obsidian = new Item(Material.OBSIDIAN, true);
 		public static final Item Diamond = new Item(Material.DIAMOND_BLOCK, true);
 		public static final Item Ice = new Item(Material.ICE, true);
 		public static final Item PackedIce = new Item(Material.PACKED_ICE, true);
 		public static final Item Snow = new Item(Material.SNOW_BLOCK, true);
 		public static final Item Clay = new Item(Material.CLAY, true);
 		public static final Item Pumpkin = new Item(Material.PUMPKIN, true);
 		public static final Item Netherrack = new Item(Material.NETHERRACK, true);
 		public static final Item Soulsand = new Item(Material.SOUL_SAND, true);
 		public static final Item Glowstone = new Item(Material.GLOWSTONE, true);
 		public static final Item JackOLantern = new Item(Material.JACK_O_LANTERN, true);
 		public static final Item Melon = new Item(Material.MELON_BLOCK, true);
 		public static final Item Mycelium = new Item(Material.MYCEL, true);
 		public static final Item NetherBrick = new Item(Material.NETHER_BRICK, true);
 		public static final Item EndStone = new Item(Material.ENDER_STONE, true);
 		public static final Item Emerald = new Item(Material.EMERALD_BLOCK, true);
 		public static final Item CoalBlock = new Item(Material.COAL_BLOCK, true);
 
 		private BuildingBlock()
 		{
 		}
 
 		public static final class StainedGlass
 		{
 			public static final Item Any = new Item(Material.STAINED_GLASS, true, AnyData);
 			public static final Item White = new Item(Material.STAINED_GLASS, true, (byte) 0);
 			public static final Item Orange = new Item(Material.STAINED_GLASS, true, (byte) 1);
 			public static final Item Magenta = new Item(Material.STAINED_GLASS, true, (byte) 2);
 			public static final Item LightBlue = new Item(Material.STAINED_GLASS, true, (byte) 3);
 			public static final Item Yellow = new Item(Material.STAINED_GLASS, true, (byte) 4);
 			public static final Item Lime = new Item(Material.STAINED_GLASS, true, (byte) 5);
 			public static final Item Pink = new Item(Material.STAINED_GLASS, true, (byte) 6);
 			public static final Item Gray = new Item(Material.STAINED_GLASS, true, (byte) 7);
 			public static final Item LightGray = new Item(Material.STAINED_GLASS, true, (byte) 8);
 			public static final Item Cyan = new Item(Material.STAINED_GLASS, true, (byte) 9);
 			public static final Item Purple = new Item(Material.STAINED_GLASS, true, (byte) 10);
 			public static final Item Blue = new Item(Material.STAINED_GLASS, true, (byte) 11);
 			public static final Item Brown = new Item(Material.STAINED_GLASS, true, (byte) 12);
 			public static final Item Green = new Item(Material.STAINED_GLASS, true, (byte) 13);
 			public static final Item Red = new Item(Material.STAINED_GLASS, true, (byte) 14);
 			public static final Item Black = new Item(Material.STAINED_GLASS, true, (byte) 15);
 
 			private StainedGlass()
 			{
 			}
 		}
 
 		public static final class StainedGlassPane
 		{
 			public static final Item Any = new Item(Material.STAINED_GLASS_PANE, true, AnyData);
 			public static final Item White = new Item(Material.STAINED_GLASS_PANE, true, (byte) 0);
 			public static final Item Orange = new Item(Material.STAINED_GLASS_PANE, true, (byte) 1);
 			public static final Item Magenta = new Item(Material.STAINED_GLASS_PANE, true, (byte) 2);
 			public static final Item LightBlue = new Item(Material.STAINED_GLASS_PANE, true, (byte) 3);
 			public static final Item Yellow = new Item(Material.STAINED_GLASS_PANE, true, (byte) 4);
 			public static final Item Lime = new Item(Material.STAINED_GLASS_PANE, true, (byte) 5);
 			public static final Item Pink = new Item(Material.STAINED_GLASS_PANE, true, (byte) 6);
 			public static final Item Gray = new Item(Material.STAINED_GLASS_PANE, true, (byte) 7);
 			public static final Item LightGray = new Item(Material.STAINED_GLASS_PANE, true, (byte) 8);
 			public static final Item Cyan = new Item(Material.STAINED_GLASS_PANE, true, (byte) 9);
 			public static final Item Purple = new Item(Material.STAINED_GLASS_PANE, true, (byte) 10);
 			public static final Item Blue = new Item(Material.STAINED_GLASS_PANE, true, (byte) 11);
 			public static final Item Brown = new Item(Material.STAINED_GLASS_PANE, true, (byte) 12);
 			public static final Item Green = new Item(Material.STAINED_GLASS_PANE, true, (byte) 13);
 			public static final Item Red = new Item(Material.STAINED_GLASS_PANE, true, (byte) 14);
 			public static final Item Black = new Item(Material.STAINED_GLASS_PANE, true, (byte) 15);
 
 			private StainedGlassPane()
 			{
 			}
 		}
 
 		public static final class StainedClay
 		{
 			public static final Item Any = new Item(Material.STAINED_CLAY, true, AnyData);
 			public static final Item White = new Item(Material.STAINED_CLAY, true, (byte) 0);
 			public static final Item Orange = new Item(Material.STAINED_CLAY, true, (byte) 1);
 			public static final Item Magenta = new Item(Material.STAINED_CLAY, true, (byte) 2);
 			public static final Item LightBlue = new Item(Material.STAINED_CLAY, true, (byte) 3);
 			public static final Item Yellow = new Item(Material.STAINED_CLAY, true, (byte) 4);
 			public static final Item Line = new Item(Material.STAINED_CLAY, true, (byte) 5);
 			public static final Item Pink = new Item(Material.STAINED_CLAY, true, (byte) 6);
 			public static final Item Gray = new Item(Material.STAINED_CLAY, true, (byte) 7);
 			public static final Item LightGray = new Item(Material.STAINED_CLAY, true, (byte) 8);
 			public static final Item Cyan = new Item(Material.STAINED_CLAY, true, (byte) 9);
 			public static final Item Purple = new Item(Material.STAINED_CLAY, true, (byte) 10);
 			public static final Item Blue = new Item(Material.STAINED_CLAY, true, (byte) 11);
 			public static final Item Brown = new Item(Material.STAINED_CLAY, true, (byte) 12);
 			public static final Item Green = new Item(Material.STAINED_CLAY, true, (byte) 13);
 			public static final Item Red = new Item(Material.STAINED_CLAY, true, (byte) 14);
 			public static final Item Black = new Item(Material.STAINED_CLAY, true, (byte) 15);
 
 			private StainedClay()
 			{
 			}
 		}
 
 		public static final class Wood
 		{
 			public static final Item Any = new Item(Material.LOG, true, AnyData);
 			public static final Item Oak = new Item(Material.LOG, true, (byte) 0);
 			public static final Item Spruce = new Item(Material.LOG, true, (byte) 1);
 			public static final Item Birch = new Item(Material.LOG, true, (byte) 2);
 			public static final Item Jungle = new Item(Material.LOG, true, (byte) 3);
 			public static final Item Acacia = new Item(Material.LOG, true, (byte) 4);
 			public static final Item DarkOak = new Item(Material.LOG, true, (byte) 5);
 
 			private Wood()
 			{
 			}
 
 			@SuppressWarnings("InnerClassFieldHidesOuterClassField")
 			public static final class Plank
 			{
 				public static final Item Any = new Item(Material.WOOD, true, AnyData);
 				public static final Item Oak = new Item(Material.WOOD, true, (byte) 0);
 				public static final Item Spruce = new Item(Material.WOOD, true, (byte) 1);
 				public static final Item Birch = new Item(Material.WOOD, true, (byte) 2);
 				public static final Item Jungle = new Item(Material.WOOD, true, (byte) 3);
 				public static final Item Acacia = new Item(Material.WOOD, true, (byte) 4);
 				public static final Item DarkOak = new Item(Material.WOOD, true, (byte) 5);
 
 				private Plank()
 				{
 				}
 			}
 		}
 
 		public static final class Sandstone
 		{
 			public static final Item Any = new Item(Material.SANDSTONE, true, AnyData);
 			public static final Item Normal = new Item(Material.SANDSTONE, true, (byte) 0);
 			public static final Item Chiseled = new Item(Material.SANDSTONE, true, (byte) 1);
 			public static final Item Smooth = new Item(Material.SANDSTONE, true, (byte) 2);
 
 			private Sandstone()
 			{
 			}
 		}
 
 		public static final class Wool
 		{
 			public static final Item Any = new Item(Material.WOOL, true, AnyData);
 			public static final Item White = new Item(Material.WOOL, true, (byte) 0);
 			public static final Item Orange = new Item(Material.WOOL, true, (byte) 1);
 			public static final Item Magenta = new Item(Material.WOOL, true, (byte) 2);
 			public static final Item LightBlue = new Item(Material.WOOL, true, (byte) 3);
 			public static final Item Yellow = new Item(Material.WOOL, true, (byte) 4);
 			public static final Item Lime = new Item(Material.WOOL, true, (byte) 5);
 			public static final Item Pink = new Item(Material.WOOL, true, (byte) 6);
 			public static final Item Gray = new Item(Material.WOOL, true, (byte) 7);
 			public static final Item LightGray = new Item(Material.WOOL, true, (byte) 8);
 			public static final Item Cyan = new Item(Material.WOOL, true, (byte) 9);
 			public static final Item Purple = new Item(Material.WOOL, true, (byte) 10);
 			public static final Item Blue = new Item(Material.WOOL, true, (byte) 11);
 			public static final Item Brown = new Item(Material.WOOL, true, (byte) 12);
 			public static final Item Green = new Item(Material.WOOL, true, (byte) 13);
 			public static final Item Red = new Item(Material.WOOL, true, (byte) 14);
 			public static final Item Black = new Item(Material.WOOL, true, (byte) 15);
 
 			private Wool()
 			{
 			}
 		}
 
 		@SuppressWarnings("InnerClassFieldHidesOuterClassField")
 		public static final class Slab
 		{
 			public static final Item AnyWood = new Item(Material.WOOD_STEP, true, AnyData);
 			public static final Item Oak = new Item(Material.WOOD_STEP, true, (byte) 0);
 			public static final Item Spruce = new Item(Material.WOOD_STEP, true, (byte) 1);
 			public static final Item Birch = new Item(Material.WOOD_STEP, true, (byte) 2);
 			public static final Item Jungle = new Item(Material.WOOD_STEP, true, (byte) 3);
 			public static final Item Acacia = new Item(Material.WOOD_STEP, true, (byte) 4);
 			public static final Item DarkOak = new Item(Material.WOOD_STEP, true, (byte) 5);
 			public static final Item AnyStone = new Item(Material.STEP, true, AnyData);
 			public static final Item Stone = new Item(Material.STEP, true, (byte) 0);
 			public static final Item Sandstone = new Item(Material.STEP, true, (byte) 1);
 			public static final Item Cobblestone = new Item(Material.STEP, true, (byte) 2);
 			public static final Item Brick = new Item(Material.STEP, true, (byte) 3);
 			public static final Item StoneBrick = new Item(Material.STEP, true, (byte) 4);
 			public static final Item NetherRack = new Item(Material.STEP, true, (byte) 5);
 			public static final Item Quartz = new Item(Material.STEP, true, (byte) 6);
 
 			private Slab()
 			{
 			}
 		}
 
 		@SuppressWarnings("InnerClassFieldHidesOuterClassField")
 		public static final class Stairs
 		{
 			public static final Item Oak = new Item(Material.WOOD_STAIRS, true);
 			public static final Item Spruce = new Item(Material.SPRUCE_WOOD_STAIRS, true);
 			public static final Item Birch = new Item(Material.BIRCH_WOOD_STAIRS, true);
 			public static final Item Jungle = new Item(Material.JUNGLE_WOOD_STAIRS, true);
 			public static final Item Acacia = new Item(Material.ACACIA_STAIRS, true);
 			public static final Item DarkOak = new Item(Material.DARK_OAK_STAIRS, true);
 			public static final Item Cobblestone = new Item(Material.COBBLESTONE_STAIRS, true);
 			public static final Item Brick = new Item(Material.BRICK_STAIRS, true);
 			public static final Item StoneBrick = new Item(Material.SMOOTH_STAIRS, true);
 			public static final Item NetherBrick = new Item(Material.NETHER_BRICK_STAIRS, true);
 			public static final Item Quartz = new Item(Material.QUARTZ_STAIRS, true);
 			public static final Item Sandstone = new Item(Material.SANDSTONE_STAIRS, true);
 
 			private Stairs()
 			{
 			}
 		}
 
 		public static final class StoneBrick
 		{
 			public static final Item Any = new Item(Material.SMOOTH_BRICK, true, AnyData);
 			public static final Item Normal = new Item(Material.SMOOTH_BRICK, true, (byte) 0);
 			public static final Item Mossy = new Item(Material.SMOOTH_BRICK, true, (byte) 1);
 			public static final Item Cracked = new Item(Material.SMOOTH_BRICK, true, (byte) 2);
 			public static final Item Chiseled = new Item(Material.SMOOTH_BRICK, true, (byte) 3);
 
 			private StoneBrick()
 			{
 			}
 		}
 
 		public static final class CobbleWall
 		{
 			public static final Item Any = new Item(Material.COBBLE_WALL, true, AnyData);
 			public static final Item Normal = new Item(Material.COBBLE_WALL, true, (byte) 0);
 			public static final Item Mossy = new Item(Material.COBBLE_WALL, true, (byte) 1);
 
 			private CobbleWall()
 			{
 			}
 		}
 
 		public static final class Quartz
 		{
 			public static final Item Any = new Item(Material.QUARTZ_BLOCK, true, AnyData);
 			public static final Item Normal = new Item(Material.QUARTZ_BLOCK, true, (byte) 0);
 			public static final Item Chiseled = new Item(Material.QUARTZ_BLOCK, true, (byte) 1);
 			public static final Item Pillar = new Item(Material.QUARTZ_BLOCK, true, (byte) 2);
 
 			private Quartz()
 			{
 			}
 		}
 	}
 
 	public static final class Ore
 	{
 		public static final Item Gold = new Item(Material.GOLD_ORE, true);
 		public static final Item Iron = new Item(Material.IRON_ORE, true);
 		public static final Item Coal = new Item(Material.COAL_ORE, true);
 		public static final Item LapisLazuli = new Item(Material.LAPIS_ORE, true);
 		public static final Item Diamond = new Item(Material.DIAMOND_ORE, true);
 		public static final Item Emerald = new Item(Material.EMERALD_ORE, true);
 		public static final Item Quartz = new Item(Material.QUARTZ_ORE, true);
 		public static final Item Redstone = new Item(Material.REDSTONE_ORE, true);
 
 		private Ore()
 		{
 		}
 	}
 
 	public static final class Decoration
 	{
 		public static final Item Web = new Item(Material.WEB, true);
 		public static final Item LongGrass = new Item(Material.LONG_GRASS, true);
 		public static final Item DeadBush = new Item(Material.DEAD_BUSH, true);
 		public static final Item Dandelion = new Item(Material.YELLOW_FLOWER, true);
 		public static final Item Torch = new Item(Material.TORCH, true);
 		public static final Item Chest = new Item(Material.CHEST, true);
 		public static final Item Workbench = new Item(Material.WORKBENCH, true);
 		public static final Item Furnace = new Item(Material.FURNACE, true);
 		public static final Item Ladder = new Item(Material.LADDER, true);
 		public static final Item Snow = new Item(Material.SNOW, true);
 		public static final Item Cactus = new Item(Material.CACTUS, true);
 		public static final Item Jukebox = new Item(Material.JUKEBOX, true);
 		public static final Item Fence = new Item(Material.FENCE, true);
 		public static final Item IronBars = new Item(Material.IRON_FENCE, true);
 		public static final Item GlassPane = new Item(Material.THIN_GLASS, true);
 		public static final Item Vine = new Item(Material.VINE, true);
 		public static final Item LilyPad = new Item(Material.WATER_LILY, true);
 		public static final Item NetherFence = new Item(Material.NETHER_FENCE, true);
 		public static final Item EnchantmentTable = new Item(Material.ENCHANTMENT_TABLE, true);
 		public static final Item EnderPortalFrame = new Item(Material.ENDER_PORTAL_FRAME, true);
 		public static final Item EnderChest = new Item(Material.ENDER_CHEST, true);
 		public static final Item TrappedChest = new Item(Material.TRAPPED_CHEST, true);
 		public static final Item Painting = new Item(Material.PAINTING, true);
 		public static final Item Sign = new Item(Material.SIGN, true);
 		public static final Item Bed = new Item(Material.BED, true);
 		public static final Item ItemFrame = new Item(Material.ITEM_FRAME, true);
 		public static final Item FlowerPot = new Item(Material.FLOWER_POT_ITEM, true);
 
 		private Decoration()
 		{
 		}
 
 		public static final class Flower
 		{
 			public static final Item Any = new Item(Material.RED_ROSE, true, AnyData);
 			public static final Item Poppy = new Item(Material.RED_ROSE, true, (byte) 0);
 			public static final Item BlueOrchid = new Item(Material.RED_ROSE, true, (byte) 1);
 			public static final Item Allium = new Item(Material.RED_ROSE, true, (byte) 2);
 			public static final Item RedTulip = new Item(Material.RED_ROSE, true, (byte) 4);
 			public static final Item OrangeTulip = new Item(Material.RED_ROSE, true, (byte) 5);
 			public static final Item WhiteTulip = new Item(Material.RED_ROSE, true, (byte) 6);
 			public static final Item PinkTulip = new Item(Material.RED_ROSE, true, (byte) 7);
 			public static final Item OxeyeDaisy = new Item(Material.RED_ROSE, true, (byte) 8);
 
 			private Flower()
 			{
 			}
 		}
 
 		public static final class DoublePlant
 		{
 			public static final Item Any = new Item(Material.DOUBLE_PLANT, true, AnyData);
 			public static final Item Sunflower = new Item(Material.DOUBLE_PLANT, true, (byte) 0);
 			public static final Item Lilac = new Item(Material.DOUBLE_PLANT, true, (byte) 1);
 			public static final Item TallGrass = new Item(Material.DOUBLE_PLANT, true, (byte) 2);
 			public static final Item Fern = new Item(Material.DOUBLE_PLANT, true, (byte) 3);
 			public static final Item RoseBush = new Item(Material.DOUBLE_PLANT, true, (byte) 4);
 			public static final Item Peony = new Item(Material.DOUBLE_PLANT, true, (byte) 5);
 
 			private DoublePlant()
 			{
 			}
 		}
 
 		public static final class Sapling
 		{
 			public static final Item Any = new Item(Material.SAPLING, true, AnyData);
 			public static final Item Oak = new Item(Material.SAPLING, true, (byte) 0);
 			public static final Item Spruce = new Item(Material.SAPLING, true, (byte) 1);
 			public static final Item Birch = new Item(Material.SAPLING, true, (byte) 2);
 			public static final Item Jungle = new Item(Material.SAPLING, true, (byte) 3);
 			public static final Item Acacia = new Item(Material.SAPLING, true, (byte) 4);
 			public static final Item DarkOak = new Item(Material.SAPLING, true, (byte) 5);
 
 			private Sapling()
 			{
 			}
 		}
 
 		public static final class Leaves
 		{
 			public static final Item Oak = new Item(Material.LEAVES, true);
 			public static final Item Spruce = new Item(Material.LEAVES, true);
 			public static final Item Birch = new Item(Material.LEAVES, true);
 			public static final Item Jungle = new Item(Material.LEAVES, true);
 
 			private Leaves()
 			{
 			}
 		}
 
 		public static final class Mushroom
 		{
 			public static final Item Brown = new Item(Material.BROWN_MUSHROOM, true);
 			public static final Item Red = new Item(Material.RED_MUSHROOM, true);
 			public static final Item Huge1 = new Item(Material.HUGE_MUSHROOM_1, true);
 			public static final Item Huge2 = new Item(Material.HUGE_MUSHROOM_2, true);
 
 			private Mushroom()
 			{
 			}
 		}
 
 		public static final class MonsterEgg
 		{
 			public static final Item Any = new Item(Material.MONSTER_EGGS, true, AnyData);
 			public static final Item Stone = new Item(Material.MONSTER_EGGS, true, (byte) 0);
 			public static final Item Cobblestone = new Item(Material.MONSTER_EGGS, true, (byte) 1);
 			public static final Item StoneBrick = new Item(Material.MONSTER_EGGS, true, (byte) 2);
 
 			private MonsterEgg()
 			{
 			}
 		}
 
 		public static final class Anvil
 		{
 			public static final Item Any = new Item(Material.ANVIL, true, AnyData);
 			public static final Item Normal = new Item(Material.ANVIL, true, (byte) 0);
 			public static final Item SlightlyDamaged = new Item(Material.ANVIL, true, (byte) 1);
 			public static final Item VeryDamaged = new Item(Material.ANVIL, true, (byte) 2);
 
 			private Anvil()
 			{
 			}
 		}
 
 		public static final class Head
 		{
 			public static final Item Any = new Item(Material.SKULL_ITEM, true, AnyData);
 			public static final Item Skeleton = new Item(Material.SKULL_ITEM, true, (byte) 0);
 			public static final Item WitherSkeleton = new Item(Material.SKULL_ITEM, true, (byte) 1);
 			public static final Item Zombie = new Item(Material.SKULL_ITEM, true, (byte) 2);
 			public static final Item Human = new Item(Material.SKULL_ITEM, true, (byte) 3);
 			public static final Item Creeper = new Item(Material.SKULL_ITEM, true, (byte) 4);
 
 			private Head()
 			{
 			}
 		}
 	}
 
 	public static final class Redstone
 	{
 		public static final Item Block = new Item(Material.REDSTONE_BLOCK, true);
 		public static final Item Dust = new Item(Material.REDSTONE, true);
 		public static final Item Torch = new Item(Material.REDSTONE_TORCH_ON, true);
 		public static final Item Diode = new Item(Material.DIODE, true);
 		public static final Item Comparator = new Item(Material.REDSTONE_COMPARATOR, true);
 		public static final Item TripwireHook = new Item(Material.TRIPWIRE_HOOK, true);
 		public static final Item DaylightDetector = new Item(Material.DAYLIGHT_DETECTOR, true);
 		public static final Item Lever = new Item(Material.LEVER, true);
 		public static final Item TNT = new Item(Material.TNT, true);
 
 		private Redstone()
 		{
 		}
 
 		public static final class Lamp
 		{
 			public static final Item Off = new Item(Material.REDSTONE_LAMP_OFF, true);
 			public static final Item On = new Item(Material.REDSTONE_LAMP_ON, true);
 
 			private Lamp()
 			{
 			}
 		}
 
 		public static final class Device
 		{
 			public static final Item Dispenser = new Item(Material.DISPENSER, true);
 			public static final Item NoteBlock = new Item(Material.NOTE_BLOCK, true);
 			public static final Item Hopper = new Item(Material.HOPPER, true);
 			public static final Item Dropper = new Item(Material.DROPPER, true);
 
 			private Device()
 			{
 			}
 		}
 
 		public static final class Piston
 		{
 			public static final Item Sticky = new Item(Material.PISTON_STICKY_BASE, true);
 			public static final Item Normal = new Item(Material.PISTON_BASE, true);
 
 			private Piston()
 			{
 			}
 		}
 
 		public static final class Button
 		{
 			public static final Item Stone = new Item(Material.STONE_BUTTON, true);
 			public static final Item Wood = new Item(Material.WOOD_BUTTON, true);
 
 			private Button()
 			{
 			}
 		}
 
 		public static final class PressurePlate
 		{
 			public static final Item Stone = new Item(Material.STONE_PLATE, true);
 			public static final Item Wood = new Item(Material.WOOD_PLATE, true);
 			public static final Item Gold = new Item(Material.GOLD_PLATE, true);
 			public static final Item Iron = new Item(Material.IRON_PLATE, true);
 
 			private PressurePlate()
 			{
 			}
 		}
 
 		public static final class Door
 		{
 			public static final Item Trap = new Item(Material.TRAP_DOOR, true);
 			public static final Item Wood = new Item(Material.WOOD_DOOR, true);
 			public static final Item Iron = new Item(Material.IRON_DOOR, true);
 			public static final Item Gate = new Item(Material.FENCE_GATE, true);
 
 			private Door()
 			{
 			}
 		}
 	}
 
 	public static final class Transportation
 	{
 		public static final Item Saddle = new Item(Material.SADDLE, true);
 		public static final Item Boat = new Item(Material.BOAT, true);
 		public static final Item CarrotOnAStick = new Item(Material.CARROT_STICK, true);
 
 		private Transportation()
 		{
 		}
 
 		public static final class Rail
 		{
 			public static final Item Normal = new Item(Material.RAILS, true);
 			public static final Item Powered = new Item(Material.POWERED_RAIL, true);
 			public static final Item Detector = new Item(Material.DETECTOR_RAIL, true);
 			public static final Item Activator = new Item(Material.ACTIVATOR_RAIL, true);
 
 			private Rail()
 			{
 			}
 		}
 
 		public static final class Minecart
 		{
 			public static final Item Normal = new Item(Material.MINECART, true);
 			public static final Item Storage = new Item(Material.STORAGE_MINECART, true);
 			public static final Item Powered = new Item(Material.POWERED_MINECART, true);
 			public static final Item Explosive = new Item(Material.EXPLOSIVE_MINECART, true);
 			public static final Item Hopper = new Item(Material.HOPPER_MINECART, true);
 
 			private Minecart()
 			{
 			}
 		}
 	}
 
 	public static final class Miscellaneous
 	{
 		public static final Item Beacon = new Item(Material.BEACON, true);
 		public static final Item Snowball = new Item(Material.SNOW_BALL, true);
 		public static final Item Paper = new Item(Material.PAPER, true);
 		public static final Item Book = new Item(Material.BOOK, true);
 		public static final Item Slimeball = new Item(Material.SLIME_BALL, true);
 		public static final Item Bone = new Item(Material.BONE, true);
 		public static final Item EnderPearl = new Item(Material.ENDER_PEARL, true);
 		public static final Item EyeOfEnder = new Item(Material.EYE_OF_ENDER, true);
 		public static final Item ExperienceBottle = new Item(Material.EXP_BOTTLE, true);
 		public static final Item FireCharge = new Item(Material.FIREBALL, true);
 		public static final Item BookAndQuill = new Item(Material.BOOK_AND_QUILL, true);
 		public static final Item Map = new Item(Material.EMPTY_MAP, true);
 		public static final Item FireworkCharge = new Item(Material.FIREWORK_CHARGE, true);
 
 		private Miscellaneous()
 		{
 		}
 
 		public static final class Bucket
 		{
 			public static final Item Normal = new Item(Material.BUCKET, true);
 			public static final Item Water = new Item(Material.WATER_BUCKET, true);
 			public static final Item Lava = new Item(Material.LAVA_BUCKET, true);
 			public static final Item Milk = new Item(Material.MILK_BUCKET, true);
 
 			private Bucket()
 			{
 			}
 		}
 
 		@SuppressWarnings("NumericCastThatLosesPrecision")
 		public static final class MonsterEgg
 		{
 			private MonsterEgg()
 			{
 			}
 
 			public static Item Get(RunsafeEntityType type)
 			{
 				return new Item(Material.MONSTER_EGG, false, (byte) type.getId());
 			}
 
 			public static final Item Any = new Item(Material.MONSTER_EGG, true, AnyData);
 			public static final Item Creeper = new Item(Material.MONSTER_EGG, true, (byte) EntityType.CREEPER.getTypeId());
 			public static final Item Skeleton = new Item(Material.MONSTER_EGG, true, (byte) EntityType.SKELETON.getTypeId());
 			public static final Item Spider = new Item(Material.MONSTER_EGG, true, (byte) EntityType.SPIDER.getTypeId());
 			public static final Item Zombie = new Item(Material.MONSTER_EGG, true, (byte) EntityType.ZOMBIE.getTypeId());
 			public static final Item Slime = new Item(Material.MONSTER_EGG, true, (byte) EntityType.SLIME.getTypeId());
 			public static final Item Ghast = new Item(Material.MONSTER_EGG, true, (byte) EntityType.GHAST.getTypeId());
 			public static final Item ZombiePigman = new Item(Material.MONSTER_EGG, true, (byte) EntityType.PIG_ZOMBIE.getTypeId());
 			public static final Item Enderman = new Item(Material.MONSTER_EGG, true, (byte) EntityType.ENDERMAN.getTypeId());
 			public static final Item CaveSpider = new Item(Material.MONSTER_EGG, true, (byte) EntityType.CAVE_SPIDER.getTypeId());
 			public static final Item Silverfish = new Item(Material.MONSTER_EGG, true, (byte) EntityType.SILVERFISH.getTypeId());
 			public static final Item Blaze = new Item(Material.MONSTER_EGG, true, (byte) EntityType.BLAZE.getTypeId());
 			public static final Item MagmaCube = new Item(Material.MONSTER_EGG, true, (byte) EntityType.MAGMA_CUBE.getTypeId());
 			public static final Item Bat = new Item(Material.MONSTER_EGG, true, (byte) EntityType.BAT.getTypeId());
 			public static final Item Witch = new Item(Material.MONSTER_EGG, true, (byte) EntityType.WITCH.getTypeId());
 			public static final Item Pig = new Item(Material.MONSTER_EGG, true, (byte) EntityType.PIG.getTypeId());
 			public static final Item Sheep = new Item(Material.MONSTER_EGG, true, (byte) EntityType.SHEEP.getTypeId());
 			public static final Item Cow = new Item(Material.MONSTER_EGG, true, (byte) EntityType.COW.getTypeId());
 			public static final Item Chicken = new Item(Material.MONSTER_EGG, true, (byte) EntityType.CHICKEN.getTypeId());
 			public static final Item Squid = new Item(Material.MONSTER_EGG, true, (byte) EntityType.SQUID.getTypeId());
 			public static final Item Wolf = new Item(Material.MONSTER_EGG, true, (byte) EntityType.WOLF.getTypeId());
 			public static final Item Mooshroom = new Item(Material.MONSTER_EGG, true, (byte) EntityType.MUSHROOM_COW.getTypeId());
 			public static final Item Ocelot = new Item(Material.MONSTER_EGG, true, (byte) EntityType.OCELOT.getTypeId());
 			public static final Item Villager = new Item(Material.MONSTER_EGG, true, (byte) EntityType.VILLAGER.getTypeId());
 		}
 
 		public static final class Record
 		{
 			public static final Item C418_13 = new Item(Material.GOLD_RECORD, true);
 			public static final Item C418_Cat = new Item(Material.GREEN_RECORD, true);
 			public static final Item C418_Blocks = new Item(Material.RECORD_3, true);
 			public static final Item C418_Chirp = new Item(Material.RECORD_4, true);
 			public static final Item C418_Far = new Item(Material.RECORD_5, true);
 			public static final Item C418_Mall = new Item(Material.RECORD_6, true);
 			public static final Item C418_Mellohi = new Item(Material.RECORD_7, true);
 			public static final Item C418_Stal = new Item(Material.RECORD_8, true);
 			public static final Item C418_Strad = new Item(Material.RECORD_9, true);
 			public static final Item C418_Ward = new Item(Material.RECORD_10, true);
 			public static final Item C418_11 = new Item(Material.RECORD_11, true);
 			public static final Item C418_Wait = new Item(Material.RECORD_12, true);
 
 			private Record()
 			{
 			}
 		}
 	}
 
 	public static class Food
 	{
 		public static final class Plant
 		{
 			public static final Item Apple = new Item(Material.APPLE, true);
 			public static final Item Melon = new Item(Material.MELON, true);
 			public static final Item Carrot = new Item(Material.CARROT_ITEM, true);
 			public static final Item Potato = new Item(Material.POTATO_ITEM, true);
 
 			private Plant()
 			{
 			}
 		}
 
 		public static final class Meat
 		{
 			public static final Item Pork = new Item(Material.PORK, true);
 			public static final Item Beef = new Item(Material.RAW_BEEF, true);
 			public static final Item Chicken = new Item(Material.RAW_CHICKEN, true);
 
 			private Meat()
 			{
 			}
 
 			public static final class Fish
 			{
 				public static final Item Any = new Item(Material.RAW_FISH, true, AnyData);
 				public static final Item Normal = new Item(Material.RAW_FISH, true, (byte) 0);
 				public static final Item Salmon = new Item(Material.RAW_FISH, true, (byte) 1);
 				public static final Item Clownfish = new Item(Material.RAW_FISH, true, (byte) 2);
 				public static final Item Pufferfish = new Item(Material.RAW_FISH, true, (byte) 3);
 
 				private Fish()
 				{
 				}
 			}
 		}
 
 		public static final class Cooked
 		{
 			public static final Item MushroomSoup = new Item(Material.MUSHROOM_SOUP, true);
 			public static final Item Bread = new Item(Material.BREAD, true);
 			public static final Item Pork = new Item(Material.GRILLED_PORK, true);
 			public static final Item Cake = new Item(Material.CAKE, true);
 			public static final Item Cookie = new Item(Material.COOKIE, true);
 			public static final Item Beef = new Item(Material.COOKED_BEEF, true);
 			public static final Item Chicken = new Item(Material.COOKED_CHICKEN, true);
 			public static final Item Potato = new Item(Material.BAKED_POTATO, true);
 			public static final Item PumpkinPie = new Item(Material.PUMPKIN_PIE, true);
 
 			private Cooked()
 			{
 			}
 
 			public static final class Fish
 			{
 				public static final Item Any = new Item(Material.COOKED_FISH, true, AnyData);
 				public static final Item Normal = new Item(Material.COOKED_FISH, true, (byte) 0);
 				public static final Item Salmon = new Item(Material.COOKED_FISH, true, (byte) 1);
 				public static final Item Clownfish = new Item(Material.COOKED_FISH, true, (byte) 2);
 				public static final Item Pufferfish = new Item(Material.COOKED_FISH, true, (byte) 3);
 
 				private Fish()
 				{
 				}
 			}
 		}
 
 		public static final class Golden
 		{
 			public static final Item Apple = new Item(Material.GOLDEN_APPLE, true);
 			public static final Item Carrot = new Item(Material.GOLDEN_CARROT, true);
 
 			private Golden()
 			{
 			}
 		}
 
 		public static final class Unsavory
 		{
 			public static final Item PoisonousPotato = new Item(Material.POISONOUS_POTATO, true);
 			public static final Item RottenFlesh = new Item(Material.ROTTEN_FLESH, true);
 			public static final Item SpiderEye = new Item(Material.SPIDER_EYE, true);
 
 			private Unsavory()
 			{
 			}
 		}
 	}
 
 	public static final class Tool
 	{
 		public static final Item FlintAndSteel = new Item(Material.FLINT_AND_STEEL, true);
 		public static final Item Compass = new Item(Material.COMPASS, true);
 		public static final Item FishingRod = new Item(Material.FISHING_ROD, true);
 		public static final Item Watch = new Item(Material.WATCH, true);
 		public static final Item Shears = new Item(Material.SHEARS, true);
 
 		private Tool()
 		{
 		}
 
 		public static final class Shovel
 		{
 			public static final Item Iron = new Item(Material.IRON_SPADE, true);
 			public static final Item Wood = new Item(Material.WOOD_SPADE, true);
 			public static final Item Stone = new Item(Material.STONE_SPADE, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_SPADE, true);
 			public static final Item Gold = new Item(Material.GOLD_SPADE, true);
 
 			private Shovel()
 			{
 			}
 		}
 
 		public static final class Pickaxe
 		{
 			public static final Item Iron = new Item(Material.IRON_PICKAXE, true);
 			public static final Item Wood = new Item(Material.WOOD_PICKAXE, true);
 			public static final Item Stone = new Item(Material.STONE_PICKAXE, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_PICKAXE, true);
 			public static final Item Gold = new Item(Material.GOLD_PICKAXE, true);
 
 			private Pickaxe()
 			{
 			}
 		}
 
 		public static final class Axe
 		{
 			public static final Item Iron = new Item(Material.IRON_AXE, true);
 			public static final Item Wood = new Item(Material.WOOD_AXE, true);
 			public static final Item Stone = new Item(Material.STONE_AXE, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_AXE, true);
 			public static final Item Gold = new Item(Material.GOLD_AXE, true);
 
 			private Axe()
 			{
 			}
 		}
 
 		public static final class Hoe
 		{
 			public static final Item Wood = new Item(Material.WOOD_HOE, true);
 			public static final Item Stone = new Item(Material.STONE_HOE, true);
 			public static final Item Iron = new Item(Material.IRON_HOE, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_HOE, true);
 			public static final Item Gold = new Item(Material.GOLD_HOE, true);
 
 			private Hoe()
 			{
 			}
 		}
 	}
 
 	public static final class Combat
 	{
 		public static final Item Bow = new Item(Material.BOW, true);
 		public static final Item Arrow = new Item(Material.ARROW, true);
 
 		private Combat()
 		{
 		}
 
 		public static final class Sword
 		{
 			public static final Item Wood = new Item(Material.WOOD_SWORD, true);
 			public static final Item Iron = new Item(Material.IRON_SWORD, true);
 			public static final Item Stone = new Item(Material.STONE_SWORD, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_SWORD, true);
 			public static final Item Gold = new Item(Material.GOLD_SWORD, true);
 
 			private Sword()
 			{
 			}
 		}
 
 		public static final class Helmet
 		{
 			public static final Item Leather = new Item(Material.LEATHER_HELMET, true);
 			public static final Item Iron = new Item(Material.IRON_HELMET, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_HELMET, true);
 			public static final Item Gold = new Item(Material.GOLD_HELMET, true);
 			public static final Item Chainmail = new Item(Material.CHAINMAIL_HELMET, true);
 
 			private Helmet()
 			{
 			}
 		}
 
 		public static final class Chestplate
 		{
 			public static final Item Leather = new Item(Material.LEATHER_CHESTPLATE, true);
 			public static final Item Iron = new Item(Material.IRON_CHESTPLATE, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_CHESTPLATE, true);
 			public static final Item Gold = new Item(Material.GOLD_CHESTPLATE, true);
 			public static final Item Chainmail = new Item(Material.CHAINMAIL_CHESTPLATE, true);
 
 			private Chestplate()
 			{
 			}
 		}
 
 		public static final class Leggings
 		{
 			public static final Item Leather = new Item(Material.LEATHER_LEGGINGS, true);
 			public static final Item Iron = new Item(Material.IRON_LEGGINGS, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_LEGGINGS, true);
 			public static final Item Gold = new Item(Material.GOLD_LEGGINGS, true);
 			public static final Item Chainmail = new Item(Material.CHAINMAIL_LEGGINGS, true);
 
 			private Leggings()
 			{
 			}
 		}
 
 		public static final class Boots
 		{
 			public static final Item Leather = new Item(Material.LEATHER_BOOTS, true);
 			public static final Item Iron = new Item(Material.IRON_BOOTS, true);
 			public static final Item Diamond = new Item(Material.DIAMOND_BOOTS, true);
 			public static final Item Gold = new Item(Material.GOLD_BOOTS, true);
 			public static final Item Chainmail = new Item(Material.CHAINMAIL_BOOTS, true);
 
 			private Boots()
 			{
 			}
 		}
 	}
 
 	public static final class Brewing
 	{
 		public static final Item GhastTear = new Item(Material.GHAST_TEAR, true);
 		public static final Item Potion = new Item(Material.POTION, true);
 		public static final Item GlassBottle = new Item(Material.GLASS_BOTTLE, true);
 		public static final Item FermentedSpiderEye = new Item(Material.FERMENTED_SPIDER_EYE, true);
 		public static final Item BlazePowder = new Item(Material.BLAZE_POWDER, true);
 		public static final Item MagmaCream = new Item(Material.MAGMA_CREAM, true);
 		public static final Item BrewingStand = new Item(Material.BREWING_STAND_ITEM, true);
 		public static final Item GlisteringMelon = new Item(Material.SPECKLED_MELON, true);
 		public static final Item Cauldron = new Item(Material.CAULDRON_ITEM, true);
 		public static final Item NetherWart = new Item(Material.NETHER_STALK, true);
 
 		private Brewing()
 		{
 		}
 	}
 
 	public static final class Materials
 	{
 		public static final Item Coal = new Item(Material.COAL, true, (byte) 0);
 		public static final Item CharCoal = new Item(Material.COAL, true, (byte) 1);
 		public static final Item Diamond = new Item(Material.DIAMOND, true);
 		public static final Item IronIngot = new Item(Material.IRON_INGOT, true);
 		public static final Item GoldIngot = new Item(Material.GOLD_INGOT, true);
 		public static final Item Stick = new Item(Material.STICK, true);
 		public static final Item Bowl = new Item(Material.BOWL, true);
 		public static final Item String = new Item(Material.STRING, true);
 		public static final Item Feather = new Item(Material.FEATHER, true);
 		public static final Item Gunpowder = new Item(Material.SULPHUR, true);
 		public static final Item Wheat = new Item(Material.WHEAT, true);
 		public static final Item Flint = new Item(Material.FLINT, true);
 		public static final Item Leather = new Item(Material.LEATHER, true);
 		public static final Item ClayBrick = new Item(Material.CLAY_BRICK, true);
 		public static final Item Clay = new Item(Material.CLAY_BALL, true);
 		public static final Item Sugarcane = new Item(Material.SUGAR_CANE, true);
 		public static final Item Egg = new Item(Material.EGG, true);
 		public static final Item GlowstoneDust = new Item(Material.GLOWSTONE_DUST, true);
 		public static final Item InkSack = new Item(Material.INK_SACK, true, (byte) 0);
 		public static final Item Cocoa = new Item(Material.INK_SACK, true, (byte) 3);
 		public static final Item Sugar = new Item(Material.SUGAR, true);
 		public static final Item BlazeRod = new Item(Material.BLAZE_ROD, true);
 		public static final Item GoldNugget = new Item(Material.GOLD_NUGGET, true);
 		public static final Item Emerald = new Item(Material.EMERALD, true);
 		public static final Item NetherStar = new Item(Material.NETHER_STAR, true);
 		public static final Item NetherBrick = new Item(Material.NETHER_BRICK_ITEM, true);
 		public static final Item Quartz = new Item(Material.QUARTZ, true);
 		public static final Item BoneMeal = new Item(Material.INK_SACK, true, (byte) 15);
 
 		private Materials()
 		{
 		}
 
 		public static final class Dye
 		{
 			public static final Item Any = new Item(Material.INK_SACK, true, AnyData);
 			public static final Item Black = InkSack;
 			public static final Item Red = new Item(Material.INK_SACK, true, (byte) 1);
 			public static final Item Green = new Item(Material.INK_SACK, true, (byte) 2);
 			public static final Item Brown = Cocoa;
 			public static final Item Blue = new Item(Material.INK_SACK, true, (byte) 4);
 			public static final Item Purple = new Item(Material.INK_SACK, true, (byte) 5);
 			public static final Item Cyan = new Item(Material.INK_SACK, true, (byte) 6);
 			public static final Item LightGray = new Item(Material.INK_SACK, true, (byte) 7);
 			public static final Item Gray = new Item(Material.INK_SACK, true, (byte) 8);
 			public static final Item Pink = new Item(Material.INK_SACK, true, (byte) 9);
 			public static final Item Lime = new Item(Material.INK_SACK, true, (byte) 10);
 			public static final Item Yellow = new Item(Material.INK_SACK, true, (byte) 11);
 			public static final Item LightBlue = new Item(Material.INK_SACK, true, (byte) 12);
 			public static final Item Magenta = new Item(Material.INK_SACK, true, (byte) 13);
 			public static final Item Orange = new Item(Material.INK_SACK, true, (byte) 14);
 			public static final Item White = BoneMeal;
 
 			private Dye()
 			{
 			}
 		}
 
 		@SuppressWarnings("InnerClassFieldHidesOuterClassField")
 		public static final class Seed
 		{
 			public static final Item Wheat = new Item(Material.SEEDS, true);
 			public static final Item Pumpkin = new Item(Material.PUMPKIN_SEEDS, true);
 			public static final Item Melon = new Item(Material.MELON_SEEDS, true);
 
 			private Seed()
 			{
 			}
 		}
 	}
 
 	public static final class Unavailable
 	{
 		public static final Item Air = new Item(Material.AIR, true);
 		public static final Item Water = new Item(Material.WATER, true);
 		public static final Item StationaryWater = new Item(Material.STATIONARY_WATER, true);
 		public static final Item Lava = new Item(Material.LAVA, true);
 		public static final Item StationaryLava = new Item(Material.STATIONARY_LAVA, true);
 		public static final Item BedBlock = new Item(Material.BED_BLOCK, true);
 		public static final Item PistonArm = new Item(Material.PISTON_EXTENSION, true);
 		public static final Item MovingPistonArm = new Item(Material.PISTON_MOVING_PIECE, true);
 		public static final Item Fire = new Item(Material.FIRE, true);
 		public static final Item MobSpawner = new Item(Material.MOB_SPAWNER, true);
 		public static final Item RedstoneWire = new Item(Material.REDSTONE_WIRE, true);
 		public static final Item Crops = new Item(Material.CROPS, true);
 		public static final Item TilledSoil = new Item(Material.SOIL, true);
 		public static final Item BurningFurnace = new Item(Material.BURNING_FURNACE, true);
 		public static final Item Signpost = new Item(Material.SIGN_POST, true);
 		public static final Item WallSign = new Item(Material.WALL_SIGN, true);
 		public static final Item WoodenDoor = new Item(Material.WOODEN_DOOR, true);
 		public static final Item IronDoorBlock = new Item(Material.IRON_DOOR_BLOCK, true);
 		public static final Item GlowingRedstoneOre = new Item(Material.GLOWING_REDSTONE_ORE, true);
 		public static final Item SugarCaneBlock = new Item(Material.SUGAR_CANE_BLOCK, true);
 		public static final Item Portal = new Item(Material.PORTAL, true);
 		public static final Item CakeBlock = new Item(Material.CAKE_BLOCK, true);
 		public static final Item LockedChest = new Item(Material.LOCKED_CHEST, true);
 		public static final Item PumpkinStem = new Item(Material.PUMPKIN_STEM, true);
 		public static final Item MelonStem = new Item(Material.MELON_STEM, true);
 		public static final Item NetherWart = new Item(Material.NETHER_WARTS, true);
 		public static final Item BrewingStand = new Item(Material.BREWING_STAND, true);
 		public static final Item Cauldron = new Item(Material.CAULDRON, true);
 		public static final Item EnderPortal = new Item(Material.ENDER_PORTAL, true);
 		public static final Item Cocoa = new Item(Material.COCOA, true);
 		public static final Item Tripwire = new Item(Material.TRIPWIRE, true);
 		public static final Item CommandBlock = new Item(Material.COMMAND, true);
 		public static final Item FlowerPot = new Item(Material.FLOWER_POT, true);
 		public static final Item Skull = new Item(Material.SKULL, true);
 		public static final Item Carrot = new Item(Material.CARROT, true);
 		public static final Item Potato = new Item(Material.POTATO, true);
 
 		private Unavailable()
 		{
 		}
 
 		public static final class Redstone
 		{
 			public static final Item TorchOff = new Item(Material.REDSTONE_TORCH_OFF, true);
 			public static final Item DiodeOn = new Item(Material.DIODE_BLOCK_ON, true);
 			public static final Item DiodeOff = new Item(Material.DIODE_BLOCK_OFF, true);
 			public static final Item ComparatorOn = new Item(Material.REDSTONE_COMPARATOR_ON, true);
 			public static final Item ComparatorOff = new Item(Material.REDSTONE_COMPARATOR_OFF, true);
 
 			private Redstone()
 			{
 			}
 		}
 
 		public static final class DoubleSlab
 		{
 			public static final Item AnyWood = new Item(Material.WOOD_DOUBLE_STEP, true, (byte) -1);
 			public static final Item Oak = new Item(Material.WOOD_DOUBLE_STEP, true, (byte) 0);
 			public static final Item Spruce = new Item(Material.WOOD_DOUBLE_STEP, true, (byte) 1);
 			public static final Item Birch = new Item(Material.WOOD_DOUBLE_STEP, true, (byte) 2);
 			public static final Item Jungle = new Item(Material.WOOD_DOUBLE_STEP, true, (byte) 3);
 			public static final Item AnyStone = new Item(Material.DOUBLE_STEP, true, (byte) -1);
 			public static final Item Stone = new Item(Material.DOUBLE_STEP, true, (byte) 0);
 			public static final Item Sandstone = new Item(Material.DOUBLE_STEP, true, (byte) 1);
 			public static final Item Plank = new Item(Material.DOUBLE_STEP, true, (byte) 2);
 			public static final Item Cobblestone = new Item(Material.DOUBLE_STEP, true, (byte) 3);
 			public static final Item Brick = new Item(Material.DOUBLE_STEP, true, (byte) 4);
 			public static final Item StoneBrick = new Item(Material.DOUBLE_STEP, true, (byte) 5);
 			public static final Item NetherBrick = new Item(Material.DOUBLE_STEP, true, (byte) 6);
 			public static final Item Quartz = new Item(Material.DOUBLE_STEP, true, (byte) 7);
 
 			private DoubleSlab()
 			{
 			}
 		}
 	}
 
 	public static final class Special
 	{
 		public static final Item DragonEgg = new Item(Material.DRAGON_EGG, true);
 
 		private Special()
 		{
 		}
 
 		public static final class Crafted
 		{
 			public static final Item Map = new Item(Material.MAP, true);
 			public static final Item WrittenBook = new Item(Material.WRITTEN_BOOK, true);
 			public static final Item Firework = new Item(Material.FIREWORK, true);
 			public static final Item EnchantedBook = new Item(Material.ENCHANTED_BOOK, true);
 
 			private Crafted()
 			{
 			}
 		}
 	}
 
 	public int getStackSize()
 	{
 		return material.getMaxStackSize();
 	}
 
 	public Material getType()
 	{
 		return material;
 	}
 
 	public String getName()
 	{
 		return material.name();
 	}
 
 	public byte getData()
 	{
 		return data;
 	}
 
 	public RunsafeItem Drop(ILocation location)
 	{
 		return Drop(location, getStackSize());
 	}
 
 	@SuppressWarnings("LocalVariableOfConcreteClass")
 	public RunsafeItem Drop(ILocation location, int amount)
 	{
 		RunsafeMeta stack = getItem();
 		stack.setAmount(amount);
 		return location.getWorld().dropItem(location, stack);
 	}
 
 	public IBlock Place(ILocation location)
 	{
 		location.getBlock().set(this);
 		return location.getBlock();
 	}
 
 	@Override
 	public boolean enchanted()
 	{
 		return !(root || material.isBlock() || item == null) && item.enchanted();
 	}
 
 	@Override
 	public boolean enchanted(IEnchant enchant)
 	{
 		return !(root || material.isBlock() || item == null) && item.enchanted(enchant);
 	}
 
 	@Override
 	public IEnchantable enchant(IEnchant enchant)
 	{
 		if (material.isBlock())
 			return this;
 
 		if (root || item == null)
 			return convertToItem().enchant(enchant);
 
 		item.enchant(enchant);
 		return this;
 	}
 
 	@Override
 	public IEnchantable enchant(Iterable<IEnchant> enchants)
 	{
 		if (material.isBlock())
 			return this;
 
 		if (root || item == null)
 			return convertToItem().enchant(enchants);
 
 		item.enchant(enchants);
 		return this;
 	}
 
 	@Override
 	public IEnchantable disenchant()
 	{
 		if (material.isBlock())
 			return this;
 
 		if (root || item == null)
 			return convertToItem().disenchant();
 
 		item.disenchant();
 		return this;
 	}
 
 	@Override
 	public IEnchantable disenchant(IEnchant enchant)
 	{
 		if (material.isBlock())
 			return this;
 
 		if (root || item == null)
 			return convertToItem().disenchant(enchant);
 
 		item.disenchant(enchant);
 		return this;
 	}
 
 	@Override
 	public RunsafeMeta getItem()
 	{
 		return item == null ? convertToItem().getItem() : item;
 	}
 
 	public int getItemID()
 	{
 		return getItem().getItemId();
 	}
 
 	Item(Material material, boolean root)
 	{
 		this(material, root, AnyData);
 	}
 
 	Item(Material material, boolean root, byte dataByte)
 	{
 		this.material = material;
 		this.root = root;
 		data = dataByte;
 		item = root ? null : new RunsafeMaterialData(material.getId(), dataByte).toItemStack(1);
 
 		if (root)
 			//noinspection ThisEscapedInObjectConstruction
 			addItem(this);
 	}
 
 	private Item convertToItem()
 	{
 		return new Item(material, false, data);
 	}
 
 	private static Item getItem(Material material, byte dataByte)
 	{
 		String key = String.format("%s_%d", material, dataByte);
 
 		if (!items.containsKey(key))
 			return new Item(material, true, dataByte);
 		return items.get(key);
 	}
 
 	private static void addItem(Item item)
 	{
 		String key = String.format("%s_%d", item.material.name(), item.data);
 		if (!items.containsKey(key))
 			items.put(key, item);
 	}
 
 	private static final Map<String, Item> items = new HashMap<String, Item>(Material.values().length);
	private static final Pattern materialId = Pattern.compile("^-?(\\d+)(\\.\\d+)?$");
 	private final Material material;
 	private final boolean root;
 	@Nullable
 	private final RunsafeMeta item;
 	private final byte data;
 }
