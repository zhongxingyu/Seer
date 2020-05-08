 package assets.levelup;
 
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockCrops;
 import net.minecraft.block.BlockGravel;
 import net.minecraft.block.BlockLog;
 import net.minecraft.block.BlockOre;
 import net.minecraft.block.BlockRedstoneOre;
 import net.minecraft.block.BlockStem;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.inventory.ContainerPlayer;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.world.World;
 import net.minecraftforge.common.DimensionManager;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class TickHandler implements ITickHandler {
 	public static Collection<BlockPosition> blockClicked = new HashSet();
 	public static Map<Integer, Integer> blockToCounter = new HashMap();
 	static {
 		blockToCounter.put(Block.oreCoal.blockID, 0);
 		blockToCounter.put(Block.oreLapis.blockID, 1);
 		blockToCounter.put(Block.oreRedstone.blockID, 2);
 		blockToCounter.put(Block.oreIron.blockID, 3);
 		blockToCounter.put(Block.oreGold.blockID, 4);
 		blockToCounter.put(Block.oreEmerald.blockID, 5);
 		blockToCounter.put(Block.oreDiamond.blockID, 6);
 		blockToCounter.put(Block.oreNetherQuartz.blockID, 7);
 	}
 	private static ItemStack digLoot[] = { new ItemStack(Item.clay, 8), new ItemStack(Item.bowlEmpty, 2), new ItemStack(Item.coal, 4), new ItemStack(Item.painting), new ItemStack(Item.stick, 4),
 			new ItemStack(Item.silk, 2) };
 	private static ItemStack digLoot1[] = { new ItemStack(Item.swordStone), new ItemStack(Item.shovelStone), new ItemStack(Item.pickaxeStone), new ItemStack(Item.axeStone) };
 	private static ItemStack digLoot2[] = { new ItemStack(Item.slimeBall, 2), new ItemStack(Item.redstone, 8), new ItemStack(Item.ingotIron), new ItemStack(Item.ingotGold) };
 	private static ItemStack digLoot3[] = { new ItemStack(Item.diamond) };
 
 	@Override
 	public String getLabel() {
 		return "LevelUpTick";
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 	}
 
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.SERVER);
 	}
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 		EntityPlayerMP player = null;
 		World world;
 		BlockPosition block;
 		boolean playerDestroys = false;
 		synchronized (blockClicked) {
 			Iterator<BlockPosition> itr = blockClicked.iterator();
 			while (itr.hasNext()) {
 				block = itr.next();
 				if (block != null) {
 					world = DimensionManager.getWorld(block.getData()[1]);
 					player = (EntityPlayerMP) world.getEntityByID(block.getData()[0]);
 					if (player != null) {
 						playerDestroys = player.theItemInWorldManager.isDestroyingBlock;
 						if (!playerDestroys) { //Changes to false right after block breaks
 							if (world.getBlockId(block.getData()[2], block.getData()[3], block.getData()[4]) != block.getData()[5] && player.isSwingInProgress) {
 								onBlockBreak(world, player, block);
 								//System.out.println("Broken"+block.getData()[2]+","+ block.getData()[3]+","+ block.getData()[4]);
 							}
 							itr.remove();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public static int getSkill(EntityPlayer player, int id) {
 		return PlayerExtendedProperties.getSkillFromIndex(player, id);
 	}
 
 	private static ItemStack getPlanks(EntityPlayerMP player, Block block, int meta) {
 		if (block.blockID != Block.wood.blockID) {
			InventoryCrafting craft = new ContainerPlayer(player.inventory, !player.worldObj.isRemote, player).craftMatrix;
			craft.setInventorySlotContents(1, new ItemStack(block, 1, meta));
 			ItemStack planks = CraftingManager.getInstance().findMatchingRecipe(craft, player.worldObj);
 			if (planks != null) {
 				planks.stackSize = 2;
 				return planks;
 			}
 		}
 		return new ItemStack(Block.planks, 2, meta & 3);
 	}
 
 	private static void onBlockBreak(World world, EntityPlayerMP player, BlockPosition info) {
 		Block block = Block.blocksList[info.getData()[5]];
 		int meta = info.getData()[6];
 		//System.out.println("Broken "+block.getUnlocalizedName());
 		int skill;
 		Random random = new Random();
 		if (block.blockMaterial == Material.ground) {
 			skill = getSkill(player, 11);
 			if (random.nextFloat() <= skill / 200F) {
 				ItemStack[] aitemstack4 = digLoot;
 				float f = random.nextFloat();
 				if (f <= 0.1F) {
 					aitemstack4 = digLoot2;
 				}
 				if (f <= 0.4F) {
 					aitemstack4 = digLoot1;
 				}
 				if (random.nextInt(500) == 0) {
 					aitemstack4 = digLoot3;
 				}
 				ItemStack itemstack = aitemstack4[random.nextInt(aitemstack4.length)];
 				ItemStack itemstack1 = itemstack.copy();
 				itemstack1.stackSize = 1;
 				if (aitemstack4 == digLoot1) {
 					itemstack1.setItemDamage(random.nextInt(80) + 20);
 				}
 				world.spawnEntityInWorld(new EntityItem(world, info.getData()[2], info.getData()[3], info.getData()[4], itemstack1));
 				for (int i1 = 0; i1 < itemstack.stackSize - 1; i1++) {
 					if (random.nextFloat() < 0.5F) {
 						world.spawnEntityInWorld(new EntityItem(world, info.getData()[2], info.getData()[3], info.getData()[4], itemstack1.copy()));
 					}
 				}
 			}
 		} else if (block instanceof BlockGravel) {
 			skill = getSkill(player, 11);
 			if (random.nextInt(10) < skill / 5) {
 				world.spawnEntityInWorld(new EntityItem(world, info.getData()[2], info.getData()[3], info.getData()[4], new ItemStack(Item.flint)));
 			}
 		} else if (block instanceof BlockLog) {
 			skill = getSkill(player, 3);
 			if (random.nextDouble() <= skill / 150D) {
 				world.spawnEntityInWorld(new EntityItem(world, info.getData()[2], info.getData()[3], info.getData()[4], new ItemStack(Item.stick, 2)));
 			}
 			if (random.nextDouble() <= skill / 150D) {
 				world.spawnEntityInWorld(new EntityItem(world, info.getData()[2], info.getData()[3], info.getData()[4], getPlanks(player, block, meta)));
 			}
 		} else if (block instanceof BlockOre || block instanceof BlockRedstoneOre) {
 			skill = getSkill(player, 0);
 			if (!blockToCounter.containsKey(block.blockID)) {
 				blockToCounter.put(block.blockID, blockToCounter.size());
 			}
 			LevelUp.incrementOreCounter(player, blockToCounter.get(block.blockID));
 			if (random.nextDouble() <= skill / 200D) {
 				world.spawnEntityInWorld(new EntityItem(world, info.getData()[2], info.getData()[3], info.getData()[4], new ItemStack(block.idDropped(meta, random, meta), block
 						.quantityDropped(random), 0)));
 			}
 		} else if (block instanceof BlockCrops || block instanceof BlockStem) {
 			skill = getSkill(player, 9);
 			if (random.nextInt(10) < skill / 5) {
 				int ID = block.idDropped(meta, null, 0);
 				world.spawnEntityInWorld(new EntityItem(world, info.getData()[2], info.getData()[3], info.getData()[4], new ItemStack(ID, 1, 0)));
 			}
 		}
 	}
 }
