 package MMC.neocraft.recipe;
 
 import MMC.neocraft.block.NCblock;
 import MMC.neocraft.item.NCitem;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.ICraftingHandler;
 
 public class NCcrafter implements ICraftingHandler
 {
 	@Override
 	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
 	{
 		if(item.itemID == NCitem.pulpOrange.itemID)
 		{
 			if(!player.inventory.addItemStackToInventory(new ItemStack(NCitem.rindOrange, 1)) && !player.worldObj.isRemote)
 			{
 				EntityItem drop = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, new ItemStack(NCitem.rindOrange));
 				player.worldObj.spawnEntityInWorld(drop);
 			}
 			int orange = find(NCitem.fruitOrange.itemID, craftMatrix);
 			int amount = craftMatrix.getStackInSlot(orange).stackSize;
 			for(int i = 0; i < amount; i++)
 			{
 				if(player.worldObj.rand.nextInt(20) == 0 && !player.worldObj.isRemote)
 				{
 					float deg = player.rotationYaw;
 					float rad = (float)((deg * Math.PI / 180) + (Math.PI / 2));
					EntityItem sin = new EntityItem(player.worldObj, player.posX + Math.cos(rad) * 2, player.getEyeHeight(), player.posZ + Math.sin(rad) * 2, new ItemStack(NCitem.elementSinensium));
 					player.worldObj.spawnEntityInWorld(sin);
 				}
 			}
 			this.findAndDamage(NCitem.knifePruning.itemID, 1, craftMatrix);
 		}
 		else if(item.itemID == NCblock.saplingOrange.blockID) { this.findAndDamage(NCitem.staveSinensium.itemID, 1, craftMatrix); }
 		else if(item.itemID == NCitem.yeast.itemID) { this.findAndDamage(NCitem.knifePruning.itemID, 1, craftMatrix); }
 		else if(item.itemID == NCitem.flour.itemID) { this.findAndDamage(NCitem.knifePruning.itemID, 1, craftMatrix); }
 		else if(item.itemID == NCitem.dough.itemID) { this.returnBucket(player, 1); }
 	}
 	@Override
 	public void onSmelting(EntityPlayer player, ItemStack item)
 	{
 		
 	}
 	private int find(int item, IInventory inv)
 	{
 		for(int i = 0; i < inv.getSizeInventory(); i++)
 		{
 			if(inv.getStackInSlot(i) == null) { continue; }
 			if(inv.getStackInSlot(i).itemID == item) { return i; }
 		}
 		return 0;
 	}
 	private void findAndDamage(int item, int dam, IInventory inv)
 	{
 		int i, id = 0;
 		for(i = 0; i < inv.getSizeInventory(); i++)
 		{
 			if(inv.getStackInSlot(i) == null) { continue; }
 			id = inv.getStackInSlot(i).itemID;
 			if(id == item) { break; }
 		}
 		if(Item.itemsList[id] == null) { return; }
 		ItemStack damage = new ItemStack(Item.itemsList[id], 2, inv.getStackInSlot(i).getItemDamage() + dam);
 		if(damage.getItemDamage() >= damage.getMaxDamage()) { damage.stackSize--; }
 		inv.setInventorySlotContents(i, damage);
 	}
 	private void returnBucket(EntityPlayer player, int amount)
 	{
 		ItemStack ret = new ItemStack(Item.bucketEmpty, amount);
 		if(!player.inventory.addItemStackToInventory(ret) && !player.worldObj.isRemote)
 		{
 			EntityItem drop = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, ret);
 			player.worldObj.spawnEntityInWorld(drop);
 		}
 	}
 }
