 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.listeners;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.inventory.BrewEvent;
 import org.bukkit.event.inventory.FurnaceExtractEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.comphenix.xp.Action;
 import com.comphenix.xp.ActionTypes;
 import com.comphenix.xp.Configuration;
 import com.comphenix.xp.Debugger;
 import com.comphenix.xp.PlayerScheduler;
 import com.comphenix.xp.Presets;
 import com.comphenix.xp.expressions.NamedParameter;
 import com.comphenix.xp.expressions.ParameterProviderSet;
 import com.comphenix.xp.extra.Permissions;
 import com.comphenix.xp.lookup.ItemQuery;
 import com.comphenix.xp.lookup.ItemTree;
 import com.comphenix.xp.lookup.PlayerRewards;
 import com.comphenix.xp.lookup.PotionTree;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.messages.MessagePlayerQueue;
 import com.comphenix.xp.mods.BlockResponse;
 import com.comphenix.xp.mods.CustomBlockProviders;
 import com.comphenix.xp.rewards.ResourceHolder;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.google.common.base.Objects;
 
 public class ExperienceItemListener extends AbstractExperienceListener {
 
 	private Debugger debugger;
 	private PlayerScheduler scheduler;
 	private CustomBlockProviders blockProvider;
 	
 	// Task IDs
 	private static final String TASK_TAG = "item";
 	
 	// Random source
 	private Random random = new Random();
 	
 	// Error reporting
 	private ErrorReporting report = ErrorReporting.DEFAULT;
 	
 	public ExperienceItemListener(Debugger debugger, PlayerScheduler scheduler,
 							      CustomBlockProviders blockProvider, Presets presets) {
 		
 		this.scheduler = scheduler;
 		this.debugger = debugger;
 		this.blockProvider = blockProvider;
 		setPresets(presets);
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onFurnaceExtract(FurnaceExtractEvent event) {
 		// Would this yield a reward for the current player?
 		Configuration config = getConfiguration(event.getPlayer());
 		
 		if (config != null) {
 			ItemTree tree = config.getActionReward(ActionTypes.SMELTING);
 			Action action = getAction(new ItemStack(event.getItemType()), tree);
 
 			// See if we are overriding CraftBukkit
 			if (action != null) {
 				event.setExpToDrop(0);
 			}
 		}	
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerFishEvent(PlayerFishEvent event) {
 
 		try {
 			Player player = event.getPlayer();
 	
 			if (player != null && Permissions.hasRewardFishing(player)) {
 				handlePlayerFishing(event, player);
 			}
 			
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event);
 		}
 	}
 	
 	private void handlePlayerFishing(PlayerFishEvent event, Player player) {
 		
 		String message = null;
 		Action action = null;
 		Configuration config = getConfiguration(player);
 		
 		// No configuration or default configuration found
 		if (config == null) {
 			if (debugger != null)
 				debugger.printDebug(this, "Cannot find config for player %s in fishing.", player.getName());
 			return;
 		}
 			
 		PlayerRewards playerReward = config.getPlayerRewards();
 		ChannelProvider channels = config.getChannelProvider();
 		
 		// Reward type
 		switch (event.getState()) {
 		case CAUGHT_FISH:
 			action = playerReward.getFishingSuccess();
 			message = "Fish caught by %s: Spawned %d xp.";
 			break;
 
 		case FAILED_ATTEMPT:
 			action = playerReward.getFishingFailure();
 			message = "Fishing failed for %s: Spawned %d xp.";
 			break;
 			
 		default:
 			return;
 		}
 		
 		// Has an action been set?
 		if (action != null && !action.hasNothing(channels)) {
 			RewardProvider rewards = config.getRewardProvider();
 			Collection<NamedParameter> params = config.getParameterProviders().getParameters(action, player);
 			
 			List<ResourceHolder> generated = action.generateRewards(params, rewards, random);
 			
 			// Check and see if the player is broke
 			if (!action.canRewardPlayer(rewards, player, generated)) {
 				if (hasDebugger())
 					debugger.printDebug(this, "Unable to penalize fishing for %s. Not enough funds.", player.getName());
 				
 				// Don't catch the fish
 				if (!Permissions.hasUntouchable(player))
 					event.setCancelled(true);
 				return;
 			}
 			
 			Collection<ResourceHolder> result = action.rewardPlayer(rewards, player, generated);
 			config.getMessageQueue().enqueue(player, action, channels.getFormatter(player, result, generated));
 			
 			if (hasDebugger())
 				debugger.printDebug(this, message, player.getName(), result);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onBrewEvent(BrewEvent event) {
 				
 		try {
 			// Reset the potion markers
 			if (event != null && event.getContents() != null) {
 				handleBrewing(event);
 			}
 			
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event);
 		}
 	}
 	
 	private void handleBrewing(BrewEvent event) {
 	
 		for (ItemStack stack : event.getContents().getContents()) {
 			
 			// Find all potions in the brewing stand
 			if (ItemQuery.hasItems(stack) && stack.getType() == Material.POTION) {
 				
 				PotionMarker marker = new PotionMarker(stack.getDurability());
 				
 				// Reset potion markers
 				marker.reset();
 				stack.setDurability(marker.toDurability());
 			}
 		}
 		
 		if (hasDebugger())
 			debugger.printDebug(this, "Reset potion markers in brewing stand %s", getLocationString(event.getBlock()) );
 	}
 	
 	// Convert a block to a more readable format
 	private String getLocationString(Block block) {
 		return String.format("x=%d, y=%d, z=%d", block.getX(), block.getY(), block.getZ());
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onInventoryClickEvent(InventoryClickEvent event) {
 
 		// Should be included in the debug report
 		ItemQuery lastBlock = null;
 		BlockResponse response = null;
 		
 		try {
 			// Clicked item and player
 			Player player = (Player) event.getWhoClicked();
 
 			// Make sure we have a player, inventory and item
 			if (player != null && event.getInventory() != null) {
 				
 				lastBlock = blockProvider.getLastInteraction().getLastRightClick(player, null);
 				response = blockProvider.processInventoryClick(event, lastBlock);
 				
 				// See if we should handle the inventory click ourself
 				if (BlockResponse.isSuccessful(response) && response.hasDefaultBehavior()) {
 					processInventory(event, response);
 				}
 			}
 			
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event, lastBlock, response);
 		}
 	}
 	
 	/**
 	 * Handles the given inventory event using the default behavior for the given inventory type.
 	 * @param event - inventory click event.
 	 * @param response - block response detailing how to process the inventory.
 	 */
 	public void processInventory(InventoryClickEvent event, BlockResponse response) {
 		
 		if (!BlockResponse.isSuccessful(response))
 			throw new IllegalArgumentException("Block response must be successful.");
 		if (!response.hasDefaultBehavior())
 			throw new IllegalArgumentException("Block response must have a default behavior.");
 		
 		// Clicked item and player
 		Player player = (Player) event.getWhoClicked();
 		ItemStack toCraft = response.getOverridableCurrentItem(event);
 		InventoryType type = response.getDefaultBehavior();
 		
 		// Do not proceed if the user isn't permitted
 		if (!player.hasPermission(response.getPermission())) {
 			if (hasDebugger())
 				debugger.printDebug(this, "%s doesn't have permission to be awarded for %s.", 
 					response.getPermission(), response.getActionType());
 			return;
 		}
 		
 		Configuration config = getConfiguration(player);
 		
 		// Special case for brewing here too
 		if (type == InventoryType.BREWING) {
 			
 			// Guard again
 			if (config == null) {
 				if (hasDebugger())
 					debugger.printDebug(this, "No config found for %s with brewing %s.", player.getName(), toCraft);
 				return;
 			}
 			
 			// Prepare rewards and actions
 			RewardableAction potionFuture = potionItemReward(config);
 			ItemTree simpleTree = config.getActionReward(response.getActionType());
 			
 			// Yes, this feels a bit like a hack to me too. Blame faulty design. Anyways, the point
 			// is that we get to check more complex potion matching rules, like "match all splash potions"
 			// or "match all level 2 regen potions (splash or not)".
 			PotionTree complexTree = config.getComplexReward(response.getActionType());
 			
 			// Rewards for items alone
 			if (simpleTree != null)
 				handleInventory(event, response, simpleTree, potionFuture, true);
 			else if (hasDebugger())
 				debugger.printDebug(this, "Could not find simple reward for action %s.", response.getActionType());
 			
 			// Rewards for special potions
 			if (complexTree != null)
 				handleInventory(event, response, complexTree.getItemQueryAdaptor(), potionFuture, true);
 			else if (hasDebugger())
 				debugger.printDebug(this, "Could not find complex reward for action %s.", response.getActionType());
 		
 			
 		// Handle every other inventory type	
 		} else {
 			
 			// Furnaces allow partial results
 			boolean partial = (type == InventoryType.FURNACE);
 			
 			if (config != null) {
 				RewardableAction itemFuture = genericItemReward(config);
 				ItemTree craftingTree = config.getActionReward(response.getActionType());
 				
 				handleInventory(event, response, craftingTree, itemFuture, partial);
 				
 			} else if (hasDebugger()) {
 				debugger.printDebug(this, "No config found for %s with crafting/smelting %s.", player.getName(), toCraft);
 			}
 		}
 	}
 	
 	/**
 	 * Retrieves the most relevant action, or NULL if no action can be found.
 	 * @param toCraft Item to query after.
 	 * @param rewards Tree of rewards and actions.
 	 * @return The most relevant action, or NULL.
 	 */
 	private Action getAction(ItemStack toCraft, ItemTree rewards) {
 		
 		ItemQuery retrieveKey = ItemQuery.fromExact(toCraft);
 		
 		if (hasExperienceReward(rewards, retrieveKey)) {
 			return rewards.get(retrieveKey);
 		} else {
 			return null;
 		}
 	}
 	
 	private void handleInventory(InventoryClickEvent event, BlockResponse response, 
 								 ItemTree tree, RewardableAction rewardAction, 
 								 boolean partialResults) {
 		
 		final Player player = (Player) event.getWhoClicked();
 		final ItemStack toStore = getStackCopy(event.getCursor());
 		final ItemStack toCraft = getStackCopy(response.getOverridableCurrentItem(event));
 				
 		if (event.isShiftClick() || response.isForceHack()) {
 			
 			// Don't waste resources if we're already waiting
 			if (scheduler.getTasks(player, TASK_TAG).size() > 0) {
 				if (hasDebugger())
 					debugger.printDebug(this, "Duplicated scheduled task aborted.");
 				return;
 				
 			} else if (hasDebugger()) {
 				debugger.printDebug(this, "Spawned scheduled task for %s.", player);
 			}
 			
 			// Store this in case we have to cancel the event manually
 			final Inventory blockInventory = event.getInventory();
 			final Inventory playerInventory = player.getInventory();
 			final ItemStack[] originalPlayerInventory = getInventoryCopy(playerInventory);
 			final ItemStack[] originalBlockInventory = getInventoryCopy(blockInventory);
 			
 			// Hack ahoy. So ugly!
 			schedulePostCraftingReward(player, tree, rewardAction, toCraft, 
 					new Runnable() {
 						
 						// Revert crafting. Attempt to, at least.
 						public void run() {
 							
 							// Don't touch the inventory of the untouchables
 							if (!Permissions.hasUntouchable(player)) {
 								
 								if (originalBlockInventory != null)
 									blockInventory.setContents(originalBlockInventory);
 								
 								playerInventory.setContents(originalPlayerInventory);
 								player.setItemOnCursor(toStore);
 							}
 						}
 					});
 			
 		} else {
 			
 			// Use the force Luke!
 			if (!ItemQuery.hasItems(toCraft)) {
 				throw new IllegalArgumentException("Must specify current item unless using force hack.");
 			}
 			
 			// The items are stored in the cursor. Make sure there's enough space.
 			int count = getStorageCount(toStore, toCraft, partialResults);
 
 			if (count > 0) {
 				
 				Action action = getAction(toCraft, tree);
 				ItemStack current = response.getOverridableCurrentItem(event);
 				
 				// Some cruft here - the stack is only divided when the user has no cursor items
 				if (partialResults && event.isRightClick() && !ItemQuery.hasItems(toStore)) {
 					count = count / 2 + count % 2;
 				}
 				
 				// Nothing do do
 				if (action == null) {
 					debugger.printDebug(this, "Action not found: %s", toCraft);
 					return;
 				} else
 					action.setDebugger(debugger);
 				
 				// Simple enough
 				if (rewardAction.canPerform(player, current, action, count)) {
 					rewardAction.performAction(player, current, action, count);
 					
 				} else {
 					// Events will not be cancelled for untouchables
 					if (!Permissions.hasUntouchable(player))
 						event.setCancelled(true);
 				}
 			}
 		}
 	}
 	
 	private RewardableAction potionItemReward(Configuration config) {
 		
 		final RewardableAction fundamental = genericItemReward(config);
 		
 		// Next, mark all potions as "consumed" or "rewarded"
 		return new RewardableAction() {
 			
 			@Override
 			public void performAction(Player player, ItemStack stack, Action action, int count) {
 				
 				PotionMarker marker = new PotionMarker(stack.getDurability());
 				
 				// Only reward potions once
 				if (!marker.hasBeenRewarded()) {
 					fundamental.performAction(player, stack, action, count);
 				
 					marker.setBeenRewarded(true);
 					stack.setDurability(marker.toDurability());
 					
 					if (hasDebugger())
 						debugger.printDebug(this, "Added potion marker. New durability: %s", marker.toDurability());
 					
 				} else {
 					if (hasDebugger())
 						debugger.printDebug(this, "Cannot reward potion %s twice.", stack);
 				}
 			}
 			
 			@Override
 			public boolean canPerform(Player player, ItemStack stack, Action action, int count) {
 				return fundamental.canPerform(player, stack, action, count);
 			}
 		};	
 	}
 	
 	private RewardableAction genericItemReward(Configuration config) {
 		
 		final RewardProvider rewardsProvider = config.getRewardProvider();
 		final ChannelProvider channelsProvider = config.getChannelProvider();
 		final MessagePlayerQueue messageQueue = config.getMessageQueue();
 		final ParameterProviderSet parameterProviders = config.getParameterProviders();
 		
 		// Create a rewardable future action handler
 		return new RewardableAction() {
 			private List<ResourceHolder> generated;
 			
 			public void generateRewards(ItemStack stack, Action action, int count) {
 				Collection<NamedParameter> params = parameterProviders.getParameters(action, stack);
 				generated = action.generateRewards(params, rewardsProvider, random, count);
 			}
 			
 			@Override
 			public void performAction(Player player, ItemStack stack, Action action, int count) {
 
 				// Just in case
 				if (generated == null)
 					generateRewards(stack, action, count);
 				
 				// Give the experience straight to the user
 				Collection<ResourceHolder> result = action.rewardPlayer(rewardsProvider, player, generated);
 				messageQueue.enqueue(player, action, channelsProvider.getFormatter(player, result, generated, count));
 				
 				// Reset
 				generated = null;
 				
 				// Like above
 				if (hasDebugger())
 					debugger.printDebug(this, "User %s - spawned %s for item %s.", 
 						player.getName(), StringUtils.join(result, ", "), stack.getType());
 			}
 
 			@Override
 			public boolean canPerform(Player player, ItemStack stack, Action action, int count) {
 				if (generated == null)
 					generateRewards(stack, action, count);
 				return action.canRewardPlayer(rewardsProvider, player, generated);
 			}
 		};
 	}
 	
 	// HACK! The API doesn't allow us to easily determine the resulting number of
 	// crafted items, so we're forced to compare the inventory before and after.
 	private void schedulePostCraftingReward(final Player player, final ItemTree tree,
 											final RewardableAction rewardAction, 
 											final ItemStack compareItem,
 											final Runnable cancel) {
 											
 		final ItemStack[] preInv = getInventoryCopy(player.getInventory());
 		final ItemStack preCursor = getStackCopy(player.getItemOnCursor());
 
 		// Await future data
 		scheduler.scheduleSync(player, TASK_TAG, new Runnable() {
 			@Override
 			public void run() {
 				try {
 					compareState();
 				} catch (Exception e) {
 					report.reportError(debugger, this, e, player, tree, rewardAction, compareItem);
 				}
 			}
 			
 			private void compareState() {
 				final ItemStack[] postInv = player.getInventory().getContents();
 				int newItemsCount = 0;
 				
 				// The most relevant item stack to return
 				ItemStack last = compareItem;
 				
 				// Previous and current
 				ItemStack pre = null;
 				ItemStack post = null;
 				
 				for (int i = 0; i <= preInv.length; i++) {
 					
 					// Compare cursor item too
 					if (i == preInv.length) {
 						pre = preCursor;
 						post = player.getItemOnCursor();
 					} else {
 						pre = preInv[i];
 						post = postInv[i];
 					}
 					
 					// Increase of item count
 					int delta = (post != null ? post.getAmount() : 0) - 
 							    (pre != null ? pre.getAmount() : 0);
 					
 					// We're only interested in filled slots that are different
 					if ((hasSameItem(post, pre) || !ItemQuery.hasItems(pre)) &&
 					    (hasSameItem(last, post) || !ItemQuery.hasItems(last))) {
 						
 						if (delta > 0) {
 							newItemsCount += delta;
 							last = post;
 						}
 					}
 				}
 
 				// See if we actually got anything
 				if (newItemsCount > 0) {
 					
 					Action action = getAction(last, tree);
 					
 					// Make sure we got a action
 					if (action == null)
 						return;
 					else
 						action.setDebugger(debugger);
 					
 					// See if the event must be cancelled
 					if (!rewardAction.canPerform(player, last, action, newItemsCount)) {
 						// A big stinky hack in a hack
 						cancel.run();
 					} else {
 						rewardAction.performAction(player, last, action, newItemsCount);
 					}
 					
 				} else {
 					if (hasDebugger())
 						debugger.printDebug(this, "No changes detected.");
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Retrieves a copy of the content of a given inventory.
 	 * @param inventory - inventory to copy.
 	 * @return A copy of the content.
 	 */
 	private ItemStack[] getInventoryCopy(Inventory inventory) {
 	
 		try {
 			ItemStack[] copy = inventory.getContents();
 			
 			// Clone the array. The content may (was for me) mutable.
 			for (int i = 0; i < copy.length; i++) {
 				copy[i] = copy[i] != null ? copy[i].clone() : null;
 			}
 			
 			return copy;
 
 		} catch (NullPointerException e) {
 			// Lazy mod authors!
 			if (hasDebugger())
 				debugger.printDebug(this, "Unable to get inventory content. Cannot fully cancel reward.");
 
 			return null;
 		}
 	}
 	
 	// Makes a copy of a item stack
 	private ItemStack getStackCopy(ItemStack stack) {
 		if (stack != null)
 			return stack.clone();
 		else
 			return null;
 	}
 	
 	private boolean hasSameItem(ItemStack a, ItemStack b) {
 		if (a == null)
 			return b == null;
 		else if (b == null)
 			return a == null;
 		
 		return a.getTypeId() == b.getTypeId() &&
 			   a.getDurability() == b.getDurability() && 
 			   Objects.equal(a.getEnchantments(), b.getEnchantments());
 	}
 	
 	private boolean hasExperienceReward(ItemTree rewards, ItemQuery key) {
 		// Make sure there is any experience
 		return rewards.containsKey(key) && !rewards.get(key).equals(Action.Default);
 	}
 	
 	// Determine if a debugger is attached and is listening
 	private boolean hasDebugger() {
 		return debugger != null && debugger.isDebugEnabled();
 	}
 	
 	// Recipes are cancelled if there's isn't exactly enough space. 
 	public int getStorageCount(ItemStack storage, ItemStack addition, boolean allowPartial) {
 		
		boolean isAir = storage == null || storage.getType() == Material.AIR;
		
 		if (addition == null)
 			return 0;
 		else if (storage == null)
 			// All storage slots have the same limits
 			return addition.getAmount(); 
 		// Yes, storage might be air blocks ... weird.
		else if (!isAir && !hasSameItem(storage, addition)) 
 			// Items MUST be the same
 			return 0;
 		
 		int sum = storage.getAmount() + addition.getAmount();
		int max = isAir ? addition.getMaxStackSize() : storage.getMaxStackSize();
 
 		// Now determine the number of additional items in the storage stack
 		if (sum > max) {
 			return allowPartial ? max - storage.getAmount() : 0;
 		} else {
 			return addition.getAmount();
 		}
 	}
 }
