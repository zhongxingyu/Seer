 package btwmod.itemlogger;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryCraftResult;
 import net.minecraft.src.InventoryPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.MathHelper;
 
 import btwmods.IMod;
 import btwmods.player.ContainerEvent;
 import btwmods.player.DropEvent;
 import btwmods.player.IContainerListener;
 import btwmods.player.IDropListener;
 import btwmods.player.IPlayerActionListener;
 import btwmods.player.IPlayerBlockListener;
 import btwmods.player.IPlayerInstanceListener;
 import btwmods.player.ISlotListener;
 import btwmods.player.PlayerActionEvent;
 import btwmods.player.PlayerBlockEvent;
 import btwmods.player.PlayerInstanceEvent;
 import btwmods.player.SlotEvent;
 import btwmods.server.ITickListener;
 import btwmods.server.TickEvent;
 
 public class PlayerListener implements ISlotListener, IDropListener, IContainerListener, IPlayerBlockListener, IPlayerActionListener, IPlayerInstanceListener, ITickListener {
 
 	private Map<String, BlockInfo> lastContainerOpened = new HashMap<String, BlockInfo>();
 	private mod_ItemLogger mod;
 	private ILogger logger;
 	
 	public PlayerListener(mod_ItemLogger mod, ILogger logger) {
 		this.mod = mod;
 		this.logger = logger;
 	}
 
 	@Override
 	public IMod getMod() {
 		return mod;
 	}
 
 	@Override
 	public void onContainerAction(ContainerEvent event) {
 		EntityPlayer player = event.getPlayer();
 		switch (event.getType()) {
 			case OPENED:
 				lastContainerOpened.put(event.getPlayer().username.toLowerCase(), new BlockInfo(event.getBlock(), event.getWorld().provider.dimensionId, event.getX(), event.getY(), event.getZ()));
 				logger.containerOpened(event, player, event.getBlock(), event.getWorld().provider.dimensionId, event.getX(), event.getY(), event.getZ());
 				break;
 				
 			case PLACED:
 				break;
 				
 			case REMOVED:
 				logger.containerRemoved(event, player, event.getBlock(), event.getWorld().provider.dimensionId, event.getX(), event.getY(), event.getZ());
 				break;
 				
 			case CLOSED:
 				lastContainerOpened.remove(event.getPlayer().username.toLowerCase());
 				logger.containerClosed(event, player);
 				break;
 		}
 	}
 
 	@Override
 	public void onPlayerItemDrop(DropEvent event) {
 		if (event.getType() == DropEvent.TYPE.ALL) {
 			logger.playerDropAll(event, event.getPlayer(),
 					event.getPlayer().dimension,
 					MathHelper.floor_double(event.getPlayer().posX), 
 					MathHelper.floor_double(event.getPlayer().posY),
 					MathHelper.floor_double(event.getPlayer().posZ),
 					event.getPlayer().inventory);
 		}
 		else if (event.getType() == DropEvent.TYPE.EJECT) {
 			logger.playerDropItem(event, event.getPlayer(),
 					event.getPlayer().dimension,
 					MathHelper.floor_double(event.getPlayer().posX), 
 					MathHelper.floor_double(event.getPlayer().posY),
 					MathHelper.floor_double(event.getPlayer().posZ),
 					event.getItems());
 		}
 		else if (event.getType() == DropEvent.TYPE.PICKUP) {
 			logger.playerPickupItem(event, event.getPlayer(),
 					event.getPlayer().dimension,
 					MathHelper.floor_double(event.getPlayer().posX), 
 					MathHelper.floor_double(event.getPlayer().posY),
 					MathHelper.floor_double(event.getPlayer().posZ),
 					event.getItems());
 		}
 	}
 
 	@Override
 	public void onSlotAction(SlotEvent event) {
 		ItemStack withdrawn = null;
 		int withdrawnQuantity = -1;
 		
 		ItemStack deposited = null;
 		int depositedQuantity = -1;
 		
 		switch (event.getType()) {
 			case ADD:
 				if (event.slotIsContainer()) {
 					deposited = event.getSlotItems();
 					depositedQuantity = event.getQuantity();
 				}
 				break;
 				
 			case REMOVE:
				if (event.slotIsContainer() || event.getSlot().inventory instanceof InventoryCraftResult) {
 					withdrawn = event.getHeldItems();
 					withdrawnQuantity = event.getQuantity();
 				}
 				break;
 				
 			case SWITCH:
 				if (event.slotIsContainer()) {
 					withdrawn = event.getHeldItems();
 					deposited = event.getSlotItems();
 				}
 				else {
 					withdrawn = event.getSlotItems();
 					deposited = event.getHeldItems();
 				}
 				
 				withdrawnQuantity = withdrawn.stackSize;
 				depositedQuantity = deposited.stackSize;
 				break;
 				
 			case TRANSFER:
 				if (event.getSlot().inventory instanceof InventoryPlayer) {
 					deposited = event.getOriginalItems();
 					depositedQuantity = event.getQuantity();
 				}
 				else {
 					withdrawn = event.getOriginalItems();
 					withdrawnQuantity = event.getQuantity();
 				}
 				break;
 		}
 		
 		BlockInfo lastContainer = lastContainerOpened.get(event.getPlayer().username.toLowerCase());
 		
 		if (withdrawn != null)
 			logger.withdrew(event, event.getPlayer(), withdrawn, withdrawnQuantity, event.getContainer(), event.getSlot().inventory, lastContainer);
 		
 		if (deposited != null)
 			logger.deposited(event, event.getPlayer(), deposited, depositedQuantity, event.getContainer(), event.getSlot().inventory, lastContainer);
 	}
 
 	@Override
 	public void onPlayerBlockAction(PlayerBlockEvent event) {
 		switch (event.getType()) {
 			case ACTIVATED:
 				break;
 			case ACTIVATION_ATTEMPT:
 				break;
 			case CHECK_PLAYEREDIT:
 				logger.playerEdit(event, event.getPlayer(), event.getDirection(), event.getPlayer().dimension, event.getX(), event.getY(), event.getZ(), event.getItemStack());
 				break;
 			case PLACE_ATTEMPT:
 				break;
 			case REMOVE_ATTEMPT:
 				logger.playerRemove(event, event.getPlayer(), event.getPlayer().dimension, event.getX(), event.getY(), event.getZ());
 				break;
 			case GET_ENDERCHEST_INVENTORY:
 				break;
 		}
 	}
 
 	@Override
 	public void onPlayerAction(PlayerActionEvent event) {
 		if (event.getType() == PlayerActionEvent.TYPE.USE_ITEM) {
 			logger.playerUseItem(event, event.getPlayer(),
 					event.getPlayer().dimension,
 					MathHelper.floor_double(event.getPlayer().posX), 
 					MathHelper.floor_double(event.getPlayer().posY),
 					MathHelper.floor_double(event.getPlayer().posZ),
 					event.getItemStack());
 		}
 		else if (event.getType() == PlayerActionEvent.TYPE.PLAYER_USE_ENTITY) {
 			logger.playerUseEntity(event, event.getPlayer(),
 					event.getPlayer().dimension,
 					MathHelper.floor_double(event.getPlayer().posX), 
 					MathHelper.floor_double(event.getPlayer().posY),
 					MathHelper.floor_double(event.getPlayer().posZ),
 					event.getEntity(),
 					MathHelper.floor_double(event.getEntity().posX), 
 					MathHelper.floor_double(event.getEntity().posY),
 					MathHelper.floor_double(event.getEntity().posZ),
 					event.isLeftClick());
 		}
 	}
 
 	@Override
 	public void onPlayerInstanceAction(PlayerInstanceEvent event) {
 		if (event.getType() == PlayerInstanceEvent.TYPE.LOGIN || event.getType() == PlayerInstanceEvent.TYPE.LOGOUT) {
 			lastContainerOpened.remove(event.getPlayerInstance().username.toLowerCase());
 			
 			logger.playerLogin(event, event.getPlayerInstance(),
 					event.getPlayerInstance().dimension,
 					MathHelper.floor_double(event.getPlayerInstance().posX), 
 					MathHelper.floor_double(event.getPlayerInstance().posY),
 					MathHelper.floor_double(event.getPlayerInstance().posZ),
 					event.getType() == PlayerInstanceEvent.TYPE.LOGOUT);
 		}
 		else if (event.getType() == PlayerInstanceEvent.TYPE.DEATH) {
 			logger.playerDeath(event, event.getPlayerInstance(),
 					event.getPlayerInstance().dimension,
 					MathHelper.floor_double(event.getPlayerInstance().posX), 
 					MathHelper.floor_double(event.getPlayerInstance().posY),
 					MathHelper.floor_double(event.getPlayerInstance().posZ),
 					event.getDeathMessage());
 		}
 	}
 
 	@Override
 	public void onTick(TickEvent event) {
 		if (event.getType() == TickEvent.TYPE.START) {
 			boolean doNormal = event.getTickCounter() % mod.locationTrackingFrequency == 0;
 			boolean doWatched = doNormal || event.getTickCounter() % mod.watchedLocationTrackingFrequency == 0;
 			
 			if (doNormal || doWatched) {
 				List<EntityPlayer> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
 				for (EntityPlayer player : players) {
 					if (doNormal || (doWatched && mod.isWatchedPlayer(player))) {
 						logger.playerPosition(player,
 								player.dimension,
 							MathHelper.floor_double(player.posX), 
 							MathHelper.floor_double(player.posY),
 							MathHelper.floor_double(player.posZ));
 					}
 				}
 			}
 		}
 	}
 }
