 package graindcafe.tribu.Rollback;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.IInventory;
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.TileEntityChest;
 import net.minecraft.server.TileEntityDispenser;
 import net.minecraft.server.TileEntityFurnace;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.block.BlockState;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.inventory.InventoryHolder;
 
 public class EntryInventory extends EntryBlockState {
 	CraftItemStack[] items;
 
 	public EntryInventory(BlockState inventoryHolder) throws Exception {
 		super(inventoryHolder);
 
 		if (inventoryHolder instanceof InventoryHolder) {
 			InventoryHolder inventory = (InventoryHolder) inventoryHolder;

 			org.bukkit.inventory.ItemStack[] bItems = inventory.getInventory().getContents();
 			this.items = new CraftItemStack[bItems.length];
 			for (int i = 0; i < bItems.length; i++)
 				if (bItems[i] != null)
 					this.items[i] = new CraftItemStack(bItems[i]);
 		} else
 			throw new Exception("Not an inventory holder");
 	}
 	private void fillContainer(IInventory inventory)
 	{
 		int max;
 		if(inventory.getSize()<items.length)
 		{
 			ChunkMemory.debugMsg("Container is too short !");
 			max=inventory.getSize();
 		}else
 			max=items.length;
 		for (int i = 0; i < max ; i++)
 			if(items[i]!=null)
 				inventory.setItem(i, items[i].getHandle());
 	}
 	@Override
 	public void restore() throws WrongBlockException {
 		Validate.notNull(world, "World is null");
		Validate.notNull(items, "Items are empty");
 		IInventory inventory = ((IInventory) world.getTileEntity(x, y, z));
 		if (inventory == null) {
 			ChunkMemory.debugMsg("Null inventory tile entity");
 			int typeId = world.getTypeId(x, y, z);
 			if (typeId == Block.CHEST.id)
 				inventory = new TileEntityChest();
 			else if (typeId == Block.FURNACE.id)
 				inventory = new TileEntityFurnace();
 			else if (typeId == Block.DISPENSER.id)
 				inventory = new TileEntityDispenser();
 			else
 				throw new WrongBlockException(Block.CHEST.id,world.getTypeId(x,y,z),x,y,z,world.getWorld());
 			fillContainer(inventory);
 			world.setTileEntity(x, y, z, (TileEntity) inventory);
 		} else
 			fillContainer(inventory);
 		world.notify(x, y, z);
 	}
 
 }
