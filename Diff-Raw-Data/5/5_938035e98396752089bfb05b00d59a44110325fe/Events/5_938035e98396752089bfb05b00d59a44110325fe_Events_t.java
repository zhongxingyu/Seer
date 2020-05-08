 package digi.recipeManager;
 
 import java.util.*;
 
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.inventory.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.world.*;
 import org.bukkit.inventory.*;
 import org.bukkit.inventory.Recipe;
 
 import digi.recipeManager.customEvents.*;
 import digi.recipeManager.data.*;
 
 public class Events implements Listener
 {
 	private RecipeManager			plugin;
 	private Recipes					recipes;
 	private Settings				settings;
 	private HashSet<String>			furnaceNotified	= new HashSet<String>();
 	private HashSet<String>			furnaceStop		= new HashSet<String>();
 	private HashMap<String, int[]>	workbench;
 	
 	protected Events()
 	{
 		plugin = RecipeManager.getPlugin();
 		recipes = RecipeManager.getRecipes();
 		settings = RecipeManager.getSettings();
 	}
 	
 	protected void registerEvents()
 	{
 		HandlerList.unregisterAll(this);
 		Bukkit.getPluginManager().registerEvents(this, plugin);
 		
 		if(!recipes.hasExplosive)
 			PlayerInteractEvent.getHandlerList().unregister(this);
 		else if(workbench == null)
 			workbench = new HashMap<String, int[]>();
 		
 		if(settings.COMPATIBILITY_CHUNKEVENTS)
 		{
 			if(recipes.furnaceSmelting == null)
 			{
 				recipes.furnaceSmelting = new HashMap<String, Double>();
 				
 				for(World world : Bukkit.getServer().getWorlds())
 				{
 					worldLoad(world);
 				}
 			}
 		}
 		else
 		{
 			if(recipes.furnaceSmelting != null)
 			{
 				recipes.furnaceSmelting = null;
 				
 				ChunkLoadEvent.getHandlerList().unregister(this);
 				ChunkUnloadEvent.getHandlerList().unregister(this);
 				WorldLoadEvent.getHandlerList().unregister(this);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void eventPlayerQuit(PlayerQuitEvent event)
 	{
 		String playerName = event.getPlayer().getName();
 		
 		plugin.playerPage.remove(playerName);
 		furnaceNotified.remove(playerName);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void eventPlayerInteract(PlayerInteractEvent event)
 	{
 		String playerName = event.getPlayer().getName();
 		Action action = event.getAction();
 		
 		if(action == Action.RIGHT_CLICK_BLOCK)
 		{
 			Block block = event.getClickedBlock();
 			
 			if(block.getType() == Material.WORKBENCH)
 			{
 				Location loc = block.getLocation();
 				
 				workbench.put(playerName, new int[]
 				{
 					loc.getBlockX(),
 					loc.getBlockY(),
 					loc.getBlockZ(),
 				});
 				
 				return;
 			}
 		}
 		
 		if(action != Action.PHYSICAL)
 			workbench.remove(playerName); // remove when interacting with something else
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void eventInventoryClick(InventoryClickEvent event)
 	{
 		try
 		{
 			Inventory inv = event.getInventory();
 			
 			if(inv == null || !(inv instanceof FurnaceInventory))
 				return;
 			
 			InventoryHolder holder = inv.getHolder();
 			
 			if(holder == null || !(holder instanceof Furnace))
 				return;
 			
 			Furnace furnace = (Furnace)holder;
 			Player player = (Player)event.getWhoClicked();
 			
 			switch(event.getRawSlot())
 			{
 				case 0: // INGREDIENT slot
 				{
 					furnacePlaceIngredient(event, furnace, player, event.getCursor());
 					return;
 				}
 				
 				case 1: // FUEL slot
 				{
 					furnacePlaceFuel(event, furnace, player, event.getCursor());
 					return;
 				}
 				
 				case 2:
 					return; // Result slot
 					
 				default: // player inventory - Shift+Click handling in player inventory while having furnace UI opened
 				{
 					if(!event.isShiftClick())
 						return;
 					
 					ItemStack clicked = event.getCurrentItem();
 					
 					if(clicked == null || clicked.getTypeId() == 0) // clicked empty slot
 						return;
 					
 					int slot = ((settings.FURNACE_SHIFT_CLICK == 'f' ? recipes.getFuelRecipe(clicked) != null : event.isRightClick()) ? 1 : 0);
 					ItemStack item = inv.getItem(slot);
 					boolean itemsAlike = (item != null && item.getTypeId() == clicked.getTypeId() && item.getDurability() == clicked.getDurability());
 					
 					if(settings.FURNACE_SHIFT_CLICK == 'f' && slot == 1 && item != null && !itemsAlike)
 					{
 						slot = 0;
 						item = inv.getItem(slot);
 						itemsAlike = (item != null && item.getTypeId() == clicked.getTypeId() && item.getDurability() == clicked.getDurability());
 					}
 					
 					if(item == null || item.getTypeId() == 0) // nothing in slot, place entire clicked stack
 					{
 						if(slot == 1 ? furnacePlaceFuel(event, furnace, player, clicked) : furnacePlaceIngredient(event, furnace, player, clicked))
 						{
 							inv.setItem(slot, clicked);
 							event.setCurrentItem(null);
 							event.setCancelled(true);
 						}
 					}
 					else
 					{
 						int itemStack = item.getType().getMaxStackSize();
 						int itemAmount = item.getAmount();
 						
 						if(itemsAlike && itemAmount < itemStack) // ingredient has room for more in the stack and it's the same type and data
 						{
 							int amount = itemAmount + clicked.getAmount();
 							int diff = amount - itemStack;
 							
 							item.setAmount(Math.min(amount, itemStack));
 							
 							if(diff > 0)
 								clicked.setAmount(diff);
 							else
 								event.setCurrentItem(null);
 							
 							event.setCancelled(true);
 						}
 					}
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			event.setCancelled(true);
 			Messages.log(ChatColor.RED + event.getEventName() + " cancelled due to error:");
 			e.printStackTrace();
 			
 			if(event.getWhoClicked() instanceof Player)
 				((Player)event.getWhoClicked()).sendMessage(ChatColor.RED + event.getEventName() + " cancelled due to error!");
 		}
 	}
 	
 	private boolean furnacePlaceIngredient(InventoryClickEvent event, Furnace furnace, Player player, ItemStack item)
 	{
 		if(!plugin.allowedToCraft(player)) // player not allowed to craft
 		{
 			event.setCancelled(true);
 			event.setResult(Result.DENY);
 			return false;
 		}
 		
 		Smelt recipe = recipes.getSmeltRecipe(item);
 		
 		if(recipe != null && !recipe.isUsableBy(player, true))
 		{
 			event.setCancelled(true);
 			event.setResult(Result.DENY);
 			return false;
 		}
 		
 		furnaceNotified.remove(player.getName());
 		furnaceStop.remove(Recipes.locationToString(furnace.getLocation()));
 		
 		if(recipe != null)
 		{
 			if(recipe.getProximity() != null)
 			{
 				if(recipe.getProximity().getValue() > 0)
 				{
 					Messages.CRAFT_WARNDISTANCE.print(player, recipe.getProximity().getSuccessMessage(), new String[][]
 					{
 						{
 							"{distance}",
 							"" + recipe.getProximity().getValue()
 						}
 					});
 				}
 				else
 					Messages.CRAFT_WARNONLINE.print(player, recipe.getProximity().getSuccessMessage());
 			}
 			
 			recipes.getFurnaceData(furnace.getLocation(), true).setSmelter(player.getName()).setSmeltItem(recipe.getIngredient());
 		}
 		
 		return true; // custom recipe or not, no reason to restrict
 	}
 	
 	private boolean furnacePlaceFuel(InventoryClickEvent event, Furnace furnace, Player player, ItemStack item)
 	{
 		if(!plugin.allowedToCraft(player)) // player not allowed to craft
 		{
 			event.setCancelled(true);
 			event.setResult(Result.DENY);
 			return false;
 		}
 		
 		Fuel recipe = recipes.getFuelRecipe(item);
 		
 		if(recipe != null && !recipe.isUsableBy(player, true))
 		{
 			event.setCancelled(true);
 			event.setResult(Result.DENY);
 			return false;
 		}
 		
 		furnaceNotified.remove(player.getName());
 		furnaceStop.remove(Recipes.locationToString(furnace.getLocation()));
 		
 		if(recipe != null)
 		{
 			if(recipe.getProximity() != null)
 			{
 				if(recipe.getProximity().getValue() > 0)
 				{
 					Messages.CRAFT_WARNDISTANCE.print(player, recipe.getProximity().getSuccessMessage(), new String[][]
 					{
 						{
 							"{distance}",
 							"" + recipe.getProximity().getValue()
 						}
 					});
 				}
 				else
 					Messages.CRAFT_WARNONLINE.print(player, recipe.getProximity().getSuccessMessage());
 			}
 			
 			recipes.getFurnaceData(furnace.getLocation(), true).setFueler(player.getName()).setFuelItem(recipe.getFuel());
 		}
 		
 		return true; // custom recipe or not, no reason to restrict
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void eventPreCraft(PrepareItemCraftEvent event)
 	{
 		try
 		{
 			Player player = (event.getView() == null ? null : (Player)event.getView().getPlayer());
 			
 			CraftingInventory inventory = event.getInventory();
 			
 			if(player != null && !plugin.allowedToCraft(player))
 			{
 				inventory.setResult(null);
 				return;
 			}
 			
 			if(event.isRepair())
 			{
 				if(!settings.REPAIR_RECIPES)
 				{
 					inventory.setResult(null);
 					
 					if(player != null)
 						Messages.CRAFT_NOREPAIR.print(player);
 					
 					return;
 				}
 				
 				ItemStack result = event.getRecipe().getResult();
 				
 				if(settings.REPAIR_ENCHANTED)
 				{
 					ItemStack[] matrix = inventory.getMatrix();
 					ItemStack[] repaired = new ItemStack[2];
 					int repair[] = new int[2];
 					int repairIndex = 0;
 					
 					for(int i = 1; i < matrix.length; i++)
 					{
 						if(matrix[i] != null && matrix[i].getTypeId() != 0)
 						{
 							repair[repairIndex] = i;
 							repaired[repairIndex] = matrix[i];
 							
 							if(++repairIndex > 1)
 								break;
 						}
 					}
 					
 					if(repaired[0] == null || repaired[1] == null)
 						return;
 					
 					Map<Enchantment, Integer> enchantments = repaired[0].getEnchantments();
 					
 					if(enchantments.size() == 0)
 						enchantments = repaired[1].getEnchantments();
 					
 					if(enchantments.size() > 0)
 						inventory.getResult().addUnsafeEnchantments(enchantments);
 					
 					result = inventory.getResult();
 				}
 				
 				RecipeManagerPreCraftEvent callEvent = new RecipeManagerPreCraftEvent(null, result, player);
 				Bukkit.getPluginManager().callEvent(callEvent);
 				
 				inventory.setResult(callEvent.getDisplayResult());
 				
 				return;
 			}
 			
 			Recipe craftedRecipe = event.getRecipe();
 			
 			if(craftedRecipe == null)
 				return;
 			
 			ItemStack craftedResult = craftedRecipe.getResult();
 			
 			if(!recipes.isCustomRecipe(craftedResult))
 				return;
 			
 			if(craftedRecipe instanceof ShapedRecipe)
 			{
 				Craft recipe = recipes.getCraftRecipes().get(craftedResult.getAmount());
 				
 				if(recipe == null)
 					return;
 				
 				ItemStack result = null;
 				
 				if(recipe.isUsableBy(player, true))
 					result = recipe.getResults().get(0).getItemStack(); // Display the first result item
 					
 				RecipeManagerPreCraftEvent callEvent = new RecipeManagerPreCraftEvent(recipe, result, player);
 				Bukkit.getPluginManager().callEvent(callEvent);
 				
 				result = callEvent.getDisplayResult();
 				
 				inventory.setResult(result);
 			}
 			else if(craftedRecipe instanceof ShapelessRecipe)
 			{
 				Combine recipe = recipes.getCombineRecipes().get(craftedResult.getAmount());
 				
 				if(recipe == null)
 					return;
 				
 				ItemStack result = null;
 				
 				if(recipe.isUsableBy(player, true))
 					result = recipe.getResults().get(0).getItemStack(); // Display the first result item
 					
 				RecipeManagerPreCraftEvent callEvent = new RecipeManagerPreCraftEvent(recipe, result, player);
 				Bukkit.getPluginManager().callEvent(callEvent);
 				
 				result = callEvent.getDisplayResult();
 				
 				inventory.setResult(result);
 			}
 		}
 		catch(Exception e)
 		{
 			event.getInventory().setResult(null);
 			Messages.log(ChatColor.RED + event.getEventName() + " cancelled due to error:");
 			e.printStackTrace();
 			
 			if(event.getView() != null && event.getView().getPlayer() instanceof Player)
 				((Player)event.getView().getPlayer()).sendMessage(ChatColor.RED + event.getEventName() + " cancelled due to error!");
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void eventCraft(CraftItemEvent event)
 	{
 		try
 		{
 			if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
 				return;
 			
 			Recipe craftRecipe = event.getRecipe();
 			
 			if(craftRecipe == null)
 				return;
 			
 			ItemStack craftResult = craftRecipe.getResult();
 			
 			if(!recipes.isCustomRecipe(craftResult))
 				return;
 			
 			final Player player = (event.getWhoClicked() == null ? null : (Player)event.getWhoClicked());
 			
 			if(craftRecipe instanceof ShapedRecipe)
 			{
 				Craft recipe = recipes.getCraftRecipes().get(craftResult.getAmount());
 				
 				if(recipe == null)
 					return;
 				
 				if(player != null && event.isShiftClick() && recipe.isRewarding())
 				{
 					Messages.NOSHIFTCLICK_REWARDS.print(player);
 					event.setCancelled(true);
 					return;
 				}
 				
 				Item result = processResult(event, recipe, (recipe.getResults().size() > 1), recipe.getResult(), player);
 				
 				if(result == null)
 					return;
 				
 				boolean failed = (result.getType() == 0);
 				
				if(recipes.hasExplosive && player != null)
 				{
 					if(event.getInventory().getType() == InventoryType.CRAFTING)
 						recipe.explode(player, player.getLocation(), !failed);
 					
 					else
 					{
 						int[] vec = workbench.get(player.getName());
 						
 						if(vec != null)
 							recipe.explode(player, player.getWorld(), vec[0], vec[1], vec[2], !failed);
 					}
 				}
 				
 				recipe.sendLog((player == null ? null : player.getName()), result);
 				
 				if(failed)
 					return;
 				
 				recipe.affectExp(player);
 				recipe.affectLevel(player);
 				recipe.affectMoney(player);
 				recipe.sendMessages(player, null, result);
 				recipe.sendCommands(player, null, result);
 			}
 			else if(craftRecipe instanceof ShapelessRecipe)
 			{
 				Combine recipe = recipes.getCombineRecipes().get(craftResult.getAmount());
 				
 				if(recipe == null)
 					return;
 				
 				if(player != null && event.isShiftClick() && recipe.isRewarding())
 				{
 					Messages.NOSHIFTCLICK_REWARDS.print(player);
 					event.setCancelled(true);
 					return;
 				}
 				
 				Item result = processResult(event, recipe, (recipe.getResults().size() > 1), recipe.getResult(), player);
 				
 				if(result == null)
 					return;
 				
 				boolean failed = (result.getType() == 0);
 				
				if(recipes.hasExplosive && player != null)
 				{
 					if(event.getInventory().getType() == InventoryType.CRAFTING)
 						recipe.explode(player, player.getLocation(), !failed);
 					
 					else
 					{
 						int[] vec = workbench.get(player.getName());
 						
 						if(vec != null)
 							recipe.explode(player, player.getWorld(), vec[0], vec[1], vec[2], !failed);
 					}
 				}
 				
 				recipe.sendLog((player == null ? null : player.getName()), result);
 				
 				if(failed)
 					return;
 				
 				recipe.affectExp(player);
 				recipe.affectLevel(player);
 				recipe.affectMoney(player);
 				recipe.sendMessages(player, null, result);
 				recipe.sendCommands(player, null, result);
 			}
 			else
 				return;
 			
 			if(player != null)
 			{
 				// temp bugfix for Bukkit with shift clicking and returned items
 				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						player.updateInventory();
 					}
 				});
 			}
 		}
 		catch(Exception e)
 		{
 			event.setCurrentItem(null);
 			event.setCancelled(true);
 			Messages.log(ChatColor.RED + event.getEventName() + " cancelled due to error:");
 			e.printStackTrace();
 			
 			if(event.getView() != null && event.getView().getPlayer() instanceof Player)
 				((Player)event.getView().getPlayer()).sendMessage(ChatColor.RED + event.getEventName() + " cancelled due to error!");
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	private Item processResult(CraftItemEvent event, digi.recipeManager.data.Recipe recipe, boolean multiResult, Item result, Player player)
 	{
 		ItemStack cursor = event.getCursor();
 		
 		if(!multiResult)
 		{
 			if(player != null && !recipes.isResultTakeable(player, result, cursor, event.isShiftClick()))
 				return null;
 			
 			RecipeManagerCraftEvent callEvent = new RecipeManagerCraftEvent(recipe, result, player, cursor, event.isShiftClick(), event.isRightClick());
 			Bukkit.getPluginManager().callEvent(callEvent);
 			
 			if(callEvent.isCancelled())
 			{
 				event.setCancelled(true);
 				return null;
 			}
 			
 			result = callEvent.getResult();
 			event.setCurrentItem(result.getItemStack());
 		}
 		else
 		{
 			if(player != null && event.isShiftClick())
 			{
 				Messages.NOSHIFTCLICK_MULTIPLERESULTS.print(player);
 				event.setCancelled(true);
 				return null;
 			}
 			
 			if(player != null && (result.getType() == 0 || (cursor != null && cursor.getTypeId() != 0 && !result.compareItemStack(cursor))))
 			{
 				event.setResult(Result.DENY);
 				event.setCancelled(true);
 				
 				RecipeManagerCraftEvent callEvent = new RecipeManagerCraftEvent(recipe, result, player, cursor, false, event.isRightClick());
 				Bukkit.getPluginManager().callEvent(callEvent);
 				
 				if(callEvent.isCancelled())
 					return null;
 				
 				result = callEvent.getResult();
 				
 				CraftingInventory inv = event.getInventory();
 				ItemStack item;
 				int amt;
 				
 				for(int i = 1; i < 10; i++)
 				{
 					item = inv.getItem(i);
 					
 					if(item != null)
 					{
 						if((amt = (item.getAmount() - 1)) > 0)
 							item.setAmount(amt);
 						else
 							inv.clear(i);
 					}
 				}
 				
 				if(result.getType() == 0)
 				{
 					Messages.CRAFT_FAILURE.print(player, recipe.getFailMessage(), new String[][]
 					{
 						{
 							"{chance}",
 							result.getChance() + "%"
 						}
 					});
 				}
 				else
 				{
 					HashMap<Integer, ItemStack> extra = player.getInventory().addItem(cursor);
 					player.updateInventory();
 					
 					if(extra != null && extra.size() > 0)
 					{
 						Messages.CRAFT_DROPPED.print(player);
 						
 						for(ItemStack i : extra.values())
 						{
 							player.getWorld().dropItem(player.getLocation(), i);
 						}
 					}
 					
 					event.setCursor(result.getItemStack());
 				}
 			}
 			else
 			{
 				RecipeManagerCraftEvent callEvent = new RecipeManagerCraftEvent(recipe, result, player, cursor, false, event.isRightClick());
 				Bukkit.getPluginManager().callEvent(callEvent);
 				
 				if(callEvent.isCancelled())
 				{
 					event.setCancelled(true);
 					return null;
 				}
 				
 				result = callEvent.getResult();
 				event.setCurrentItem(result.getItemStack());
 			}
 		}
 		
 		return result;
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void eventFurnaceSmelt(FurnaceSmeltEvent event)
 	{
 		try
 		{
 			Smelt recipe = recipes.getSmeltRecipe(event.getSource());
 			
 			if(recipe == null)
 				return;
 			
 			Block block = event.getBlock();
 			BlockState blockState = event.getBlock().getState();
 			
 			if(!(blockState instanceof Furnace))
 				return;
 			
 			Furnace furnace = (Furnace)blockState;
 			Location location = block.getLocation();
 			FurnaceData furnaceData = recipes.getFurnaceData(location, true);
 			String smelterName = furnaceData.getSmelter();
 			
 			if(!recipe.isUsableProximity(smelterName, location, !furnaceNotified.contains(smelterName)))
 			{
 				furnaceNotified.add(smelterName);
 				furnaceStop.add(Recipes.locationToString(location));
 				furnace.setBurnTime((short)0);
 				furnace.setCookTime((short)0);
 				return;
 			}
 			
 			Player player = (smelterName == null ? null : Bukkit.getPlayer(smelterName));
 			
 			if(player != null && (!recipe.isUsablePermissions(player, false) || !recipe.isUsableGroups(player, false) || !recipe.isUsableExp(player, false) || !recipe.isUsableLevel(player, false) || !recipe.isUsableMoney(player, false)))
 			{
 				event.setCancelled(true);
 				furnaceStop.add(Recipes.locationToString(location));
 				furnace.setBurnTime((short)0);
 				furnace.setCookTime((short)0);
 				
 				if(!furnaceNotified.contains(smelterName))
 				{
 					Messages.CRAFT_NOSMELT.print(player, null, new String[][]
 					{
 						{
 							"{location}",
 							location.getWorld().getName() + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")"
 						}
 					});
 					
 					furnaceNotified.add(smelterName);
 				}
 				
 				return;
 			}
 			
 			furnaceStop.remove(Recipes.locationToString(location));
 			furnaceNotified.remove(smelterName);
 			Item result = (recipe.getChanceResult() ? recipe.getResult() : null);
 			
 			RecipeManagerSmeltEvent callEvent = new RecipeManagerSmeltEvent(recipe, recipes.getFuelRecipe(furnaceData.getFuelItem()), result, block, smelterName, furnaceData.getFueler());
 			Bukkit.getPluginManager().callEvent(callEvent);
 			
 			if(callEvent.isCancelled())
 			{
 				event.setCancelled(true);
 				return;
 			}
 			
 			result = callEvent.getResult();
 			boolean failed = (result == null);
 			
 			if(failed || result.getType() == 0) // special handle for AIR results
 			{
 				event.setCancelled(true); // setting the result to null will make the client timeout and the server spit errors
 				
 				ItemStack item = furnace.getInventory().getSmelting();
 				int amount = item.getAmount() - 1;
 				
 				if(amount <= 0)
 					furnace.getInventory().clear(0);
 				else
 					item.setAmount(amount);
 			}
 			
 			ItemData ingredient = new ItemData(event.getSource());
 			
 			if(failed)
 			{
 				Messages.CRAFT_FAILURE.print(player, recipe.getFailMessage(), new String[][]
 				{
 					{
 						"{chance}",
 						(100 - recipe.getResult().getChance()) + "%"
 					}
 				});
 			}
 			else
 			{
 				recipe.affectExp(player);
 				recipe.affectLevel(player);
 				recipe.affectMoney(player);
 				recipe.sendCommands(player, ingredient, result);
 				recipe.sendMessages(player, ingredient, result);
 			}
 			
 			recipe.explode(player, location, !failed);
 			recipe.sendLog(smelterName, ingredient, result);
 		}
 		catch(Exception e)
 		{
 			event.setCancelled(true);
 			Messages.log(ChatColor.RED + event.getEventName() + " cancelled due to error:");
 			e.printStackTrace();
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void eventFurnaceBurn(FurnaceBurnEvent event)
 	{
 		try
 		{
 			final Block block = event.getBlock();
 			Location location = block.getLocation();
 			String locStr = Recipes.locationToString(location);
 			
 			if(furnaceStop.contains(locStr))
 			{
 				event.setBurning(false);
 				return;
 			}
 			
 			if(settings.COMPATIBILITY_CHUNKEVENTS && recipes.furnaceSmelting != null && !recipes.furnaceSmelting.containsKey(locStr))
 				recipes.furnaceSmelting.put(locStr, (double)0);
 			
 			ItemStack ingredient = event.getFuel();
 			Fuel recipe = recipes.getFuelRecipe(ingredient);
 			
 			if(recipe != null)
 			{
 				String fuelerName = recipes.getFurnaceData(location).getFueler();
 				
 				if(!recipe.isUsableProximity(fuelerName, location, !furnaceNotified.contains(fuelerName)))
 				{
 					furnaceNotified.add(fuelerName);
 					event.setBurning(false);
 					return;
 				}
 				
 				Player player = (fuelerName == null ? null : Bukkit.getPlayer(fuelerName));
 				
 				if(player != null && (!recipe.isUsablePermissions(player, false) || !recipe.isUsableGroups(player, false) || !recipe.isUsableExp(player, false) || !recipe.isUsableLevel(player, false) || !recipe.isUsableMoney(player, false)))
 				{
 					event.setBurning(false);
 					
 					if(!furnaceNotified.contains(fuelerName))
 					{
 						Messages.CRAFT_NOFUEL.print(player, null, new String[][]
 						{
 							{
 								"{location}",
 								location.getWorld().getName() + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")"
 							}
 						});
 						
 						furnaceNotified.add(fuelerName);
 					}
 					
 					return;
 				}
 				
 				furnaceNotified.remove(fuelerName);
 				RecipeManagerFuelBurnEvent callEvent = new RecipeManagerFuelBurnEvent(recipe, (20 * recipe.getTime()), block, fuelerName);
 				Bukkit.getPluginManager().callEvent(callEvent);
 				
 				if(callEvent.isCancelled())
 				{
 					event.setBurning(false);
 					return;
 				}
 				
 				event.setBurnTime(callEvent.getBurnTicks());
 				event.setBurning(true);
 				
 				recipe.affectExp(player);
 				recipe.affectLevel(player);
 				recipe.affectMoney(player);
 				recipe.sendCommands(player, recipe.getFuel(), null);
 				recipe.sendMessages(player, recipe.getFuel(), null);
 				recipe.explode(player, location, true);
 				recipe.sendLog(fuelerName, recipe.getFuel());
 			}
 			
 			if(settings.FUEL_RETURN_BUCKETS)
 			{
 				switch(ingredient.getType())
 				{
 					case WATER_BUCKET:
 					case LAVA_BUCKET:
 					case MILK_BUCKET:
 					{
 						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								BlockState blockState = block.getState();
 								
 								if(blockState instanceof Furnace)
 								{
 									FurnaceInventory inventory = ((Furnace)blockState).getInventory();
 									
 									if(inventory.getFuel() == null || inventory.getFuel().getTypeId() == 0)
 										inventory.setFuel(new ItemStack(Material.BUCKET, 1));
 								}
 							}
 						});
 						
 						break;
 					}
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			event.setCancelled(true);
 			Messages.log(ChatColor.RED + event.getEventName() + " cancelled due to error:");
 			e.printStackTrace();
 		}
 	}
 	
 	// Monitor furnaces
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void eventBlockBreak(BlockBreakEvent event)
 	{
 		Block block = event.getBlock();
 		
 		switch(block.getType())
 		{
 			case BURNING_FURNACE:
 			case FURNACE:
 			{
 				String locString = Recipes.locationToString(block.getLocation());
 				
 				furnaceStop.remove(locString);
 				recipes.furnaceData.remove(locString);
 				
 				if(settings.COMPATIBILITY_CHUNKEVENTS && recipes.furnaceSmelting != null)
 					recipes.furnaceSmelting.remove(locString);
 				
 				break;
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void eventWorldLoad(WorldLoadEvent event)
 	{
 		worldLoad(event.getWorld());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void eventChunkLoad(ChunkLoadEvent event)
 	{
 		if(!event.isNewChunk())
 			furnaceChunk(event.getChunk(), true);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void eventChunkUnload(ChunkUnloadEvent event)
 	{
 		furnaceChunk(event.getChunk(), false);
 	}
 	
 	protected void worldLoad(World world)
 	{
 		Chunk chunks[] = world.getLoadedChunks();
 		
 		for(Chunk chunk : chunks)
 		{
 			furnaceChunk(chunk, true);
 		}
 	}
 	
 	private void furnaceChunk(Chunk chunk, boolean add)
 	{
 		if(chunk == null || recipes.furnaceSmelting == null)
 			return;
 		
 		BlockState tileEnts[] = chunk.getTileEntities();
 		
 		if(tileEnts.length <= 0)
 			return;
 		
 		for(BlockState state : tileEnts)
 		{
 			if(state != null && state instanceof Furnace)
 			{
 				if(add)
 				{
 					if(state.getType() == Material.BURNING_FURNACE)
 						recipes.furnaceSmelting.put(Recipes.locationToString(state.getLocation()), (double)0);
 				}
 				else
 					recipes.furnaceSmelting.remove(Recipes.locationToString(state.getLocation()));
 			}
 		}
 	}
 }
