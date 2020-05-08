 package toldea.romecraft.block;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import toldea.romecraft.client.renderer.RenderBlockRomanAnvil;
 import toldea.romecraft.managers.ItemManager;
 import toldea.romecraft.tileentity.TileEntityRomanAnvil;
 
 public class BlockRomanAnvil extends RomeCraftBlockContainer {
 	public BlockRomanAnvil(int blockId, Material material) {
 		super(blockId, material);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityRomanAnvil();
 	}
 
 	// This will tell minecraft not to render any side of our cube.
 	@Override
 	public boolean shouldSideBeRendered(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		return false;
 	}
 
 	// And this tell it that you can see through this block, and neighbor blocks should be rendered.
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
	
 	@Override
 	public int getRenderType() {
 		return RenderBlockRomanAnvil.renderID;
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
 		TileEntityRomanAnvil tileEntity = (TileEntityRomanAnvil) world.getBlockTileEntity(x, y, z);
 
 		if (tileEntity != null) {
 			ItemStack itemstack = player.getCurrentEquippedItem();
 			if (itemstack != null) {
 				int id = itemstack.itemID;
				
 				if (id == ItemManager.itemHammer.itemID) {
 					tileEntity.hammerIron(world);
 					return true;
 				} else {
 					int slot = -1;
	
 					if (id == ItemManager.itemIronBloom.itemID) {
 						slot = 0;
 					}
	
 					if (slot != -1 && tileEntity.isItemValidForSlot(slot, itemstack)) {
 						ItemStack anvilItemStack = tileEntity.getStackInSlot(slot);
 						if (anvilItemStack == null) {
 							anvilItemStack = itemstack.copy();
 							anvilItemStack.stackSize = 1;
 						} else {
 							if (anvilItemStack.stackSize < tileEntity.getInventoryStackLimit()) {
 								anvilItemStack.stackSize++;
 							} else {
 								return true;
 							}
 						}
	
 						if (!player.capabilities.isCreativeMode) {
 							--itemstack.stackSize;
 						}
 						tileEntity.setInventorySlotContents(slot, anvilItemStack);
 						if (itemstack.stackSize <= 0) {
 							player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack) null);
 						}
 						return true;
 					}
 				}
 			} else { // If itemstack is null aka we arent holding anything.
				int slot = 1; 
 				ItemStack anvilSlotStack = tileEntity.getStackInSlot(slot);
 				// If the 'finished items' slot is empty, try and withdraw from the 'raw items' slot.
 				if (anvilSlotStack == null) {
 					slot = 0;
 					anvilSlotStack = tileEntity.getStackInSlot(slot);
 				}
 				if (anvilSlotStack != null) {
 					itemstack = anvilSlotStack.copy();
 					player.inventory.setInventorySlotContents(player.inventory.currentItem, itemstack);
 					tileEntity.setInventorySlotContents(slot, null);
 					return true;
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
 		TileEntityRomanAnvil tileEntity = (TileEntityRomanAnvil) world.getBlockTileEntity(x, y, z);
 		if (tileEntity != null) {
 			// Get any items in the Bloomery block and drop them.
 			for (int i = 0; i < tileEntity.getSizeInventory(); i++) {
 				ItemStack stack = tileEntity.getStackInSlotOnClosing(i);
 				if (stack != null) {
 					float spawnX = x + world.rand.nextFloat();
 					float spawnY = y + world.rand.nextFloat();
 					float spawnZ = z + world.rand.nextFloat();
 
 					EntityItem droppedItem = new EntityItem(world, spawnX, spawnY, spawnZ, stack);
 
 					float mult = 0.05F;
 
 					droppedItem.motionX = (-0.5F + world.rand.nextFloat()) * mult;
 					droppedItem.motionY = (4 + world.rand.nextFloat()) * mult;
 					droppedItem.motionZ = (-0.5F + world.rand.nextFloat()) * mult;
 
 					world.spawnEntityInWorld(droppedItem);
 				}
 			}
 		}
 		super.breakBlock(world, x, y, z, par5, par6);
 	}
 }
