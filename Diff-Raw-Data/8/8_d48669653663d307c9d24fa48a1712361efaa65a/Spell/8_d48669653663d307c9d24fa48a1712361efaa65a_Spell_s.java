 // A generic spell class.
 
 package aor.SimplePlugin;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.painting.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.weather.*;
 import org.bukkit.event.vehicle.*;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import java.util.ArrayList;
 
 /**
  * This is the main spell class that all spells must extend.
  */
 public class Spell{
 	
 	public static SimplePlugin plugin;
 	
 	public Spell() { } // Empty constructor.
 	public String spellName="";
 	/**
 	 * gets the name of the spell
 	 * @return the spell's name
 	 */
 	public String getName() { return spellName; }
 	public String spellDescription="";
 	/**
 	 * gets the spell's description
 	 * @return the spell's description
 	 */
 	public String getDescription() { return spellDescription; }
 	
 	public String shortName="";
 	/**
 	 * gets the spell's short name
 	 * @return the spell's short name
 	 */
 	public String getShortName() { return shortName; }
 
 	public ArrayList<ItemStack> requiredItems = new ArrayList<ItemStack>(); // The required items arraylist of arrays.
 	/**
 	 * adds an arraylist of item stacks to the arraylist of itemstacks that are required for the spell to be used
 	 * @param items-the arraylist of the item stacks
 	 */
 	public void addRequiredItems(ArrayList<ItemStack> items)
 	{
 		for (int i = 0; i < items.size(); i++) {
 			requiredItems.add(items.get(i));
 		}
 	}
 	/**
 	 * adds an item stack or itemstack array to the arraylist of item stacks that are required for the spell to be used
 	 * @param items-the item stack or array of item stacks
 	 */
 	public void addRequiredItems(ItemStack... items){
 		for (int i = 0; i < items.length; i++) {
 			requiredItems.add(items[i]);
 		}
 	}
 	/**
 	 * sets the arraylist of itemstacks that are required for the spell to be used to an arraylist
 	 * @param items-the arraylist of the item stacks
 	 */
 	public void setRequiredItems(ArrayList<ItemStack> items)
 	{
 		requiredItems.clear();
 		requiredItems.trimToSize();
 		for (int i = 0; i < items.size(); i++) {
 			requiredItems.add(items.get(i));
 		}
 	}
 	/**
 	 * sets the arraylist of itemstacks that are required for the spell to be used to an itemstack or an itemstack array
 	 * @param items-the the item stack or itemstack array
 	 */
 	public void setRequiredItems(ItemStack... items){
 		requiredItems.clear();
 		requiredItems.trimToSize();
 		for (int i = 0; i < items.length; i++) {
 			requiredItems.add(items[i]);
 		}
 	}
 	/**
 	 * Is run when the delayedRun function is used. It is here so that no errors are produced when delayedRun is used and a run function has not been defined in the spell.
 	 * Separate runnables just complicate things and might be able to produce memory leaks, so they are now deprecated. Instead, add the arguments to an arraylist, one
 	 * for each argument. then use the run function in the spell and use the arguments and remove the index 0, so that the next run uses the next set of arguments. If you
 	 * have multiple things you want to run, put a switch in the run function that differentiates between which function is being called and in each case call a function.
 	 * You can have separate array lists for the other set of arguments. Its really not that complicated. See the example spell for something easier to understand. The
 	 * template spell shows the suggested switch format
 	 */
 	public void run(int arg){
 		
 	}
 	/**
 	 * Runs the given run function after the given delay
 	 * @param timeDelay-the amount of time before the run function is run in server ticks (20 server ticks = 1 second)
 	 * @param runNumber-the number of the run function to run.
 	 */
 	public void delayedRun(int timeDelay,int argument){
 		while(plugin.runner.main.size()<=timeDelay){
 			plugin.runner.main.add(new ArrayList<int[]>());
 		}
 		plugin.runner.main.get(timeDelay).add(new int[2]);
 		plugin.runner.main.get(timeDelay).get(plugin.runner.main.get(timeDelay).size()-1)[0]=plugin.spellList.indexOf(this);
 		plugin.runner.main.get(timeDelay).get(plugin.runner.main.get(timeDelay).size()-1)[1]=argument;
 	}
 	/**
 	 * checks if the given inventory has all of the requirements for the spell to be cast.
 	 * @param inventory-the inventory being checked
 	 * @return false-if it doesn't meet the requirements
 	 * @return true-if it meets the requirements
 	 */
 	public boolean checkInventoryRequirements(PlayerInventory inventory) // Checks if player has the proper requirements for the spell.
 	{
 		for (int i = 0; i < requiredItems.size(); i++) // The loop for the requiredItems arraylist.
 		{
 			if (!inventory.contains(requiredItems.get(i).getType(), requiredItems.get(i).getAmount())) {
 				return false;
 			}  // If it doesn't return false.
 		}
 		return true; // If all conditions were met.
 	}
 	/**
 	 * removes the required item in the given index of the array from the given inventory
 	 * @param inventory-the inventory to remove the items from
 	 * @param index-the index of the item in the required items arraylist to remove from the array
 	 * @return false-if it was unsuccessful
 	 * @return true-if it was successful
 	 */
 	public boolean removeRequiredItemFromInventory(PlayerInventory inventory,int index){
 		if(requiredItems.size()>index){
 				return removeFromInventory(inventory,requiredItems.get(index))!=-1;
 		}
 		return false;
 	}
 	/**
 	 * removes all of the spell's required items from the given inventory
 	 * @param inventory-the inventory to remove the items from
 	 * @return false-if the player did not have the neccesary items
 	 * @return true-if the items were removed from the player's inventory
 	 */
 	public boolean removeRequiredItemsFromInventory(PlayerInventory inventory)
 	{
 		if(checkInventoryRequirements(inventory)){
 			for (int i = 0; i < requiredItems.size(); i++) // The loop for the requiredItems arraylist.
 			{
 				removeFromInventory(inventory,requiredItems.get(i));
 			}
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * The default castspell function that only runs if the spell hasn't set one.
 	 * @param player-the player that cast the spell
 	 */
 	public void castSpell(Player player)
 	{
 		player.sendMessage("You're trying to cast a spell that's not set!");
 	}
 	/**
 	 * damages an item by a certain amount from the given inventory and removes it if it has been used up.
 	 * @param material-the iem to damage
 	 * @param amount-the amount to damage it
 	 * @param inventory-the player's inventory in which to damage it
 	 */
 	public void damageItem(Material material, int amount, PlayerInventory inventory) // Damages an item in the inventory. Removes it if it's all used up.
 	{ 
 		ItemStack item = inventory.getItem(inventory.first(material));
 		item.setDurability((short)(item.getDurability() + amount)); // Set durability + amount.
 
 		if (item.getDurability() >= material.getMaxDurability())
 		{ inventory.removeItem(item); } // It's used up.
 	}
 	/**
 	 * replaces an item in a given player's inventory with another item.
 	 * @param inventory-the player's inventory where the item is replaced
 	 * @param itemToRemove-the item that is to be replaced
 	 * @param itemToReplaceWith-the item to replace it with
 	 */
 	public void replaceInInventory(PlayerInventory inventory, ItemStack itemToRemove, ItemStack itemToReplaceWith){
 		int index=removeFromInventory(inventory,itemToRemove);
 		if(inventory.getItem(index)==null){
 			addItemToIndex(inventory,itemToReplaceWith,index);
 		}
 		else{
 			addItemSafely(inventory,itemToReplaceWith);
 		}
 	}
 	/**
 	 * Adds an item to a given inventory if possible.
 	 * There is a function that already does this in the inventory api: inventory.addItem(ItemStack... itemstack);
 	 * but it doesn't return whether it was possible. Instead, it returns a hashmap of the index and itemstack.
 	 * @param inventory-the inventory to add it to
 	 * @param item-the itemstack to add.
 	 * @return if it was possible
 	 */
 	public boolean addItemSafely(PlayerInventory inventory, ItemStack item){
 		if(inventory.firstEmpty()!=-1){
 			addItemToIndex(inventory,item,inventory.firstEmpty());
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * Adds an itemstack to a given index in the inventory.
 	 * @param inventory-the inventory to add it to
 	 * @param item-the item to add
 	 * @param index-the index of the inventory to use
 	 * @deprecated This function is rather pointless. You can just use the line the function uses.
 	 */
 	public void addItemToIndex(PlayerInventory inventory, ItemStack item,int index){
 		inventory.setItem(index, item);
 	}
 	/**
 	 * removes an item from a given inventory. It also takes into account the amount of the item and removes only that much, even if there is less than the amount in two different stacks.
 	 * @param inventory-the inventory to remove it from
 	 * @param item-the itemstack to remove from the inventory
 	 * @return the last index the item was removed from
 	 */
 	public int removeFromInventory(PlayerInventory inventory, ItemStack item) { // Removes an itemstack from the inventory. Use this for quantities of items.
 		int amountLeft=item.getAmount();
 		if(inventory.contains(item.getType(), item.getAmount())){
 			while(amountLeft>0){
 				int firstFound=inventory.first(item.getType());
				if(inventory.getItem(firstFound).getAmount()>=amountLeft){
 					inventory.getItem(firstFound).setAmount(inventory.getItem(firstFound).getAmount()-amountLeft);
					amountLeft=0;
 					return firstFound;
 				}
 				else{
 					amountLeft-=inventory.getItem(inventory.first(item.getType())).getAmount();
 					inventory.clear(inventory.first(item.getType()));
 				}
 			}
 		}
 		return -1;
 	}
 	//It is hard to go over what each of these functions does, but in summary they are each run when a certain event occurs, such as when a block breaks. However, they only
 	//run when the variable before them with the same name is set to true. If you want to use one of these in your plugin, you must set the variable to true in the
 	//constructor. Changing it later has no effect whatsoever. If you want to disable it later, just put an if statement with your own variable and set it to false later.
 	public boolean onDisable=false;
 	public void onDisable(){}
 	public boolean onBlockBreak=false;
 	public void onBlockBreak(BlockBreakEvent event){}
 	public boolean onBlockBurn=false;
 	public void onBlockBurn(BlockBurnEvent event) {}
 	public boolean onBlockCanBuild=false;
 	public void onBlockCanBuild(BlockCanBuildEvent event) {}
 	public boolean onBlockDamage=false;
 	public void onBlockDamage(BlockDamageEvent event) {}
 	public boolean onBlockDispence=false;
 	public void onBlockDispense(BlockDispenseEvent event) {}
 	public boolean onBlockFromTo=false;
 	public void onBlockFromTo(BlockFromToEvent event) {}
 	public boolean onBlockIgnite=false;
 	public void onBlockIgnite(BlockIgniteEvent event) {}
 	public boolean onBlockPhysics=false;
 	public void onBlockPhysics(BlockPhysicsEvent event) {}
 	public boolean onBlockPlace=false;
 	public void onBlockPlace(BlockPlaceEvent event) {}
 	public boolean onBlockRedstoneChange=false;
 	public void onBlockRedstoneChange(BlockRedstoneEvent event) {}
 	public boolean onLeavesDecay=false;
 	public void onLeavesDecay(LeavesDecayEvent event) {}
 	public boolean onSignChange=false;
 	public void onSignChange(SignChangeEvent event) {}
 	public boolean onSnowForm=false;
 	public void onSnowForm(SnowFormEvent event) {}
 	public boolean onInventoryOpen=false;
 	public void onInventoryOpen(PlayerInventoryEvent event){}
 	public boolean onItemHeldChange=false;
 	public void onItemHeldChange(PlayerItemHeldEvent event){}
 	public boolean onPlayerAnimation=false;
 	public void onPlayerAnimation(PlayerAnimationEvent event){}
 	public boolean onPlayerBedEnter=false;
 	public void onPlayerBedEnter(PlayerBedEnterEvent event){}
 	public boolean onPlayerBedLeave=false;
 	public void onPlayerBedLeave(PlayerBedLeaveEvent event){}
 	public boolean onPlayerBucketEmpty=false;
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){}
 	public boolean onPlayerBucketFill=false;
 	public void onPlayerBucketFill(PlayerBucketFillEvent event){}
 	public boolean onPlayerChat=false;
 	public void onPlayerChat(PlayerChatEvent event){}
 	public boolean onPlayerDropItem=false;
 	public void onPlayerDropItem(PlayerDropItemEvent event){}
 	public boolean onPlayerEggThrow=false;
 	public void onPlayerEggThrow(PlayerEggThrowEvent event){}
 	public boolean onPlayerInteract=false;
 	public void onPlayerInteract(PlayerInteractEvent event){}
 	public boolean onPlayerInteractEntity=false;
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event){}
 	public boolean onPlayerJoin=false;
 	public void onPlayerJoin(PlayerJoinEvent event){}
 	public boolean onPlayerKick=false;
 	public void onPlayerKick(PlayerKickEvent event){}
 	public boolean onPlayerLogin=false;
 	public void onPlayerLogin(PlayerLoginEvent event){}
 	public boolean onPlayerMove=false;
 	public void onPlayerMove(PlayerMoveEvent event){}
 	public boolean onPlayerPickupItem=false;
 	public void onPlayerPickupItem(PlayerPickupItemEvent event){}
 	public boolean onPlayerPortal=false;
 	public void onPlayerPortal(PlayerPortalEvent event){}
 	public boolean onPlayerPreLogin=false;
 	public void onPlayerPreLogin(PlayerPreLoginEvent event){}
 	public boolean onPlayerQuit=false;
 	public void onPlayerQuit(PlayerQuitEvent event){}
 	public boolean onPlayerRespawn=false;
 	public void onPlayerRespawn(PlayerRespawnEvent event){}
 	public boolean onPlayerTeleport=false;
 	public void onPlayerTeleport(PlayerTeleportEvent event){}
 	public boolean onPlayerToggleSneak=false;
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event){}
 	public boolean onLightningStrike=false;
 	public void onLightningStrike(LightningStrikeEvent event){}
 	public boolean onThunderChange=false;
 	public void onThunderChange(ThunderChangeEvent event){}
 	public boolean onWeatherChange=false;
 	public void onWeatherChange(WeatherChangeEvent event){}
 	public boolean onVehicleBlockCollision=false;
 	public void onVehicleBlockCollision(VehicleBlockCollisionEvent event){}
 	public boolean onVehicleCreate=false;
 	public void onVehicleCreate(VehicleCreateEvent event){}
 	public boolean onVehicleDamage=false;
 	public void onVehicleDamage(VehicleDamageEvent event){}
 	public boolean onVehicleDestroy=false;
 	public void onVehicleDestroy(VehicleDestroyEvent event){}
 	public boolean onVehicleEnter=false;
 	public void onVehicleEnter(VehicleEnterEvent event){}
 	public boolean onVehicleEntityCollision=false;
 	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event){}
 	public boolean onVehicleExit=false;
 	public void onVehicleExit(VehicleExitEvent event){}
 	public boolean onVehicleMove=false;
 	public void onVehicleMove(VehicleMoveEvent event){}
 	public boolean onCreatureSpawn=false;
 	public void onCreatureSpawn(CreatureSpawnEvent event){}
 	public boolean onCreeperPower=false;
 	public void onCreeperPower(CreeperPowerEvent event){}
 	public boolean onEntityCombust=false;
 	public void onEntityCombust(EntityCombustEvent event){}
 	public boolean onEntityDamage=false;
 	public void onEntityDamage(EntityDamageEvent event){}
 	public boolean onEntityDeath=false;
 	public void onEntityDeath(EntityDeathEvent event){}
 	public boolean onEntityExplode=false;
 	public void onEntityExplode(EntityExplodeEvent event){}
 	public boolean onEntityInteract=false;
 	public void onEntityInteract(EntityInteractEvent event){}
 	public boolean onEntityPortalEnter=false;
 	public void onEntityPortalEnter(EntityPortalEnterEvent event){}
 	public boolean onEntityTame=false;
 	public void onEntityTame(EntityTameEvent event){}
 	public boolean onEntityTarget=false;
 	public void onEntityTarget(EntityTargetEvent event){}
 	public boolean onExplosionPrime=false;
 	public void onExplosionPrime(ExplosionPrimeEvent event){}
 	public boolean onPaintingBreak=false;
 	public void onPaintingBreak(PaintingBreakEvent event){}
 	public boolean onPaintingPlace=false;
 	public void onPaintingPlace(PaintingPlaceEvent event){}
 	public boolean onPigZap=false;
 	public void onPigZap(PigZapEvent event){}
 }
