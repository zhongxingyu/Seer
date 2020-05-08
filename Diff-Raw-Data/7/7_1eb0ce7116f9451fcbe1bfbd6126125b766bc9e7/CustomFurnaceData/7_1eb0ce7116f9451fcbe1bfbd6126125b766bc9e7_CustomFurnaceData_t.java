 package org.dyndns.pamelloes.ExtraFurnaces.data;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import net.minecraft.server.FurnaceRecipes;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.dyndns.pamelloes.ExtraFurnaces.packet.ChangeDataServer;
 import org.dyndns.pamelloes.ExtraFurnaces.packet.ChangeInventoryServer;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.event.spout.ServerTickEvent;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 @SuppressWarnings("serial")
 public abstract class CustomFurnaceData implements Serializable, Listener {
 	private static final ItemStack empty = new ItemStack(0,0,(short) 0);
     protected transient ItemStack furnaceItemStacks[];
 	protected transient List<SpoutPlayer> players = new ArrayList<SpoutPlayer>();
 
     /** The number of ticks that the furnace will keep burning */
     public int furnaceBurnTime;
 
     /**
      * The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for
      */
     public int currentItemBurnTime;
 
     /** The number of ticks that the current item has been cooking for */
     public int furnaceCookTime;
     
     private int x,y,z;
     private UUID world;
 
     public CustomFurnaceData(int slots, UUID world, int x, int y, int z) {
         furnaceItemStacks = new ItemStack[slots];
         furnaceBurnTime = 0;
         currentItemBurnTime = 0;
         furnaceCookTime = 0;
         this.world = world;
         this.x = x;
         this.y = y;
         this.z = z;
         Bukkit.getPluginManager().getPlugin("ExtraFurnaces").getServer().getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("ExtraFurnaces"));
     }
 
     /**
      * Returns the number of slots in the inventory.
      */
     public int getSizeInventory()
     {
         return furnaceItemStacks.length;
     }
 
     /**
      * Returns the stack in slot i
      */
     public ItemStack getStackInSlot(int par1)
     {
         return furnaceItemStacks[par1];
     }
 
     /**
      * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
      */
     public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
     {
         furnaceItemStacks[par1] = par2ItemStack;
 
         if (par2ItemStack != null && par2ItemStack.getAmount() > 64) par2ItemStack.setAmount(64);
 
 		ChangeInventoryServer cis = new ChangeInventoryServer();
 		cis.setData(36 + par1, furnaceItemStacks[par1]==null?empty:furnaceItemStacks[par1]);
 		cis.send(players);
     }
 
     /**
      * Returns an integer between 0 and the passed value representing how close the current item is to being completely
      * cooked
      */
     public int getCookProgressScaled(int par1)
     {
         return (furnaceCookTime * par1) / 200;
     }
 
     /**
      * Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
      * item, where 0 means that the item is exhausted and the passed value means that the item is fresh
      */
     public int getBurnTimeRemainingScaled(int par1)
     {
         if (currentItemBurnTime == 0) {
             currentItemBurnTime = 200;
         }
 
         return (furnaceBurnTime * par1) / currentItemBurnTime;
     }
 
     /**
      * Returns true if the furnace is currently burning
      */
     public boolean isBurning()
     {
         return furnaceBurnTime > 0;
     }
     
     protected abstract int getFuelIndex();
     protected abstract int getBurnIndex();
     protected abstract int getResultIndex();
     protected int getItemSmeltTime() {
     	return 200;//1600 = coal so 1 coal = 8
     	//150 = iron furnace
     	//80 = gold furnace
     	//40 = diamond furnace
     }
     
     /**
      * Updates the furnace.
      * @return true if there has been a change in the inventory.
      */
     public boolean update() {
         boolean flag = furnaceBurnTime > 0;
         boolean flag1 = false;
 
         if (furnaceBurnTime > 0) {
             furnaceBurnTime--;
 		}
         
 		if (furnaceBurnTime == 0 && canSmelt()) {
 			currentItemBurnTime = furnaceBurnTime = getItemBurnTime(furnaceItemStacks[getFuelIndex()]);
 			if (furnaceBurnTime > 0) {
 				flag1 = true;
 				if (furnaceItemStacks[getFuelIndex()] != null) {
 					furnaceItemStacks[getFuelIndex()].setAmount(furnaceItemStacks[getFuelIndex()].getAmount()-1);
 
 					if (furnaceItemStacks[getFuelIndex()].getAmount() == 0) furnaceItemStacks[getFuelIndex()] = null;
 				}
 			}
 		}
 
 		if (isBurning() && canSmelt()) {
 			furnaceCookTime++;
 
 			if (furnaceCookTime == getItemSmeltTime()) {
 				furnaceCookTime = 0;
 				smeltItem();
 				flag1 = true;
 			}
 		} else {
 			furnaceCookTime = 0;
 		}
 
 		if (flag != (furnaceBurnTime > 0)) {
 			flag1 = true;
 			/*BlockFurnace.updateFurnaceBlockState(furnaceBurnTime > 0, worldObj,
 					xCoord, yCoord, zCoord);*/
 		}
 
         return flag1;
     }
 
     /**
      * Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, etc.
      */
     private boolean canSmelt() {
         if (furnaceItemStacks[getBurnIndex()] == null) {
             return false;
         } else {
             ItemStack itemstack = getResult(furnaceItemStacks[getBurnIndex()]);
             return itemstack == null ? false : (furnaceItemStacks[getResultIndex()] == null ? true : (!furnaceItemStacks[getResultIndex()].getType().equals(itemstack.getType()) ? false : (furnaceItemStacks[getResultIndex()].getAmount() + itemstack.getAmount() <= 64 && furnaceItemStacks[getResultIndex()].getAmount() < furnaceItemStacks[getResultIndex()].getMaxStackSize() ? true : furnaceItemStacks[getResultIndex()].getAmount() + itemstack.getAmount() <= itemstack.getMaxStackSize())));
         }
     }
 
     /**
      * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
      */
     public void smeltItem() {
         if (!canSmelt()) return;
 
         ItemStack result = getResult(furnaceItemStacks[getBurnIndex()]);
 
         if (furnaceItemStacks[getResultIndex()] == null) furnaceItemStacks[getResultIndex()] = result.clone();
         else if (furnaceItemStacks[getResultIndex()].getType().equals(result.getType())) furnaceItemStacks[getResultIndex()].setAmount(furnaceItemStacks[getResultIndex()].getAmount() + 1);
 
         furnaceItemStacks[getBurnIndex()].setAmount(furnaceItemStacks[getBurnIndex()].getAmount()-1);
 
         if (furnaceItemStacks[getBurnIndex()].getAmount() <= 0) furnaceItemStacks[getBurnIndex()] = null;
     }
 
 	/**
      * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't
      * fuel
      */
     protected int getItemBurnTime(ItemStack item) {
         if (item == null) return 0;
         int id = item.getTypeId();
         if (id == Material.WOOD.getId() || id == Material.LOG.getId()) return 300;
         if (id == Material.STICK.getId()) return 100;
         if (id == Material.COAL.getId()) return 1600;
         if (id == Material.LAVA_BUCKET.getId()) return 20000;
         if (id == Material.SAPLING.getId()) return 100;
         if (id == Material.BLAZE_ROD.getId()) return 2400;
         return 0;
     }
     
     protected ItemStack getResult(ItemStack in) {
     	Method m1 = null;
     	Method m2 = null;
     	try {
     		m1 = FurnaceRecipes.class.getDeclaredMethod("getResult",int.class);
     	} catch(Exception e) { }
     	try {
 			m2 = FurnaceRecipes.class.getDeclaredMethod("getResult",net.minecraft.server.ItemStack.class);
 		} catch (Exception e) { }
 		if(m1 != null) {
 			try {
				net.minecraft.server.ItemStack is = (net.minecraft.server.ItemStack) m1.invoke(FurnaceRecipes.getInstance(), in.getTypeId());
				return is == null ? null : new CraftItemStack(is);
 			} catch (Exception e) { }
 		}
 		if(m2 != null) {
 			try {
				net.minecraft.server.ItemStack is = (net.minecraft.server.ItemStack) m2.invoke(FurnaceRecipes.getInstance(), new CraftItemStack(in).getHandle());
				return is == null ? null : new CraftItemStack(is);
 			} catch (Exception e) { }
 		}
 		return null;
     }
     
     public void onPlayerOpenFurnace(SpoutPlayer player) {
     	players.add(player);
     	sendInventory();
     }
     
     public void onPlayerCloseFurnace(SpoutPlayer player) {
     	players.remove(player);
     }
     
     @EventHandler
     public abstract void onServerTick(ServerTickEvent e);
     
     private void writeObject(ObjectOutputStream out) throws IOException {
     	out.defaultWriteObject();
     	out.writeInt(furnaceItemStacks.length);
     	for(int i = 0; i < furnaceItemStacks.length; i++) {
     		if(furnaceItemStacks[i]!=null) {
 	    		out.writeInt(furnaceItemStacks[i].getTypeId());
 	    		out.writeShort(furnaceItemStacks[i].getDurability());
 	    		out.writeShort((short)furnaceItemStacks[i].getAmount());
     		} else {
 	    		out.writeInt(0);
 	    		out.writeShort(0);
 	    		out.writeShort(0);
     		}
     	}
     }
     
     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
     	in.defaultReadObject();
     	int amount = in.readInt();
     	furnaceItemStacks = new ItemStack[amount];
     	for(int i = 0; i < furnaceItemStacks.length; i++) {
     		furnaceItemStacks[i] = new ItemStack(Material.AIR);
     		furnaceItemStacks[i].setTypeId(in.readInt());
     		furnaceItemStacks[i].setDurability(in.readShort());
     		furnaceItemStacks[i].setAmount(in.readShort());
     		if(furnaceItemStacks[i].getTypeId()==0) furnaceItemStacks[i]=null;
     	}
     	players = new ArrayList<SpoutPlayer>();
         Bukkit.getPluginManager().getPlugin("Spout").getServer().getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("Spout"));
         Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Spout"), new Runnable() {
         	public void run() {
         		SpoutManager.getChunkDataManager().setBlockData("ExtraFurnaces", Bukkit.getWorld(world), x, y, z, CustomFurnaceData.this);
         	}
         }, 1L);
     }
 	
     protected void updateRange(int max, int min) {
     	updateRange(max,min, true);
     }
     
 	protected void updateRange(int max, int min, boolean fillLast) {
 		for(int t = 0; fillLast ? (t < max - min) && (furnaceItemStacks[max] == null) : t < 1; t++) {
 			for(int i = max; i > min; i--) {
 				if(furnaceItemStacks[i] == null && furnaceItemStacks[i-1] != null) {
 					furnaceItemStacks[i] = furnaceItemStacks[i-1];
 					furnaceItemStacks[i-1] = null;
 				}
 			}
 		}
 	}
 	
 	protected void sendInventory() {
 		ChangeInventoryServer cis = new ChangeInventoryServer();
 		for(int i = 0; i < furnaceItemStacks.length; i++) {
 			cis.setData(36 + i, furnaceItemStacks[i]==null?empty:furnaceItemStacks[i]);
 			cis.send(players);
 		}
 	}
 	
 	protected void sendData() {
 		ChangeDataServer cda = new ChangeDataServer();
 		cda.currentItemBurnTime=currentItemBurnTime;
 		cda.furnaceBurnTime=furnaceBurnTime;
 		cda.furnaceCookTime=furnaceCookTime;
 		cda.send(players);
 	}
 }
