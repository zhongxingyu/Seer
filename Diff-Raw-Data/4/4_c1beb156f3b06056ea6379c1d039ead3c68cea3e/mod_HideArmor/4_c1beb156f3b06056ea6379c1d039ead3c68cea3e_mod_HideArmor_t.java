 package btwmod.hidearmor;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.FCItemRefinedArmor;
 import net.minecraft.src.FCItemSpecialArmor;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Packet5PlayerInventory;
 import net.minecraft.src.WorldServer;
 import net.minecraft.src.WrongUsageException;
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.NetworkAPI;
 import btwmods.PlayerAPI;
 import btwmods.WorldAPI;
 import btwmods.io.Settings;
 import btwmods.network.IPacketHandlerListener;
 import btwmods.network.PacketHandlerEvent;
 import btwmods.player.IPlayerInstanceListener;
 import btwmods.player.PlayerInstanceEvent;
 import btwmods.player.PlayerInstanceEvent.METADATA;
 import btwmods.player.PlayerInstanceEvent.TYPE;
 
 public class mod_HideArmor extends CommandBase implements IMod, IPacketHandlerListener, IPlayerInstanceListener {
 	
 	private boolean plateAlwaysVisible = true;
 	private boolean specialAlwaysVisible = true;
 	
 	private Map<String, HideSettings> playerSettings = new HashMap<String, HideSettings>();
 	
 	private MinecraftServer server;
 	
 	@Override
 	public String getName() {
 		return "Hide Armor";
 	}
 
 	@Override
 	public void init(Settings settings) throws Exception {
 		if (settings.isBoolean("platealwaysvisible")) {
 			plateAlwaysVisible = settings.getBoolean("platealwaysvisible");
 		}
 		if (settings.isBoolean("specialalwaysvisible")) {
 			specialAlwaysVisible = settings.getBoolean("specialalwaysvisible");
 		}
 		
 		CommandsAPI.registerCommand(this, this);
 		NetworkAPI.addListener(this);
 		PlayerAPI.addListener(this);
 		server = MinecraftServer.getServer();
 	}
 
 	@Override
 	public void unload() throws Exception {
 		CommandsAPI.unregisterCommand(this);
 		NetworkAPI.removeListener(this);
 		PlayerAPI.removeListener(this);
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 
 	@Override
 	public void onHandlePacket(PacketHandlerEvent event) {
 		if (event.getPacket() instanceof Packet5PlayerInventory) {
 			Packet5PlayerInventory packet = (Packet5PlayerInventory)event.getPacket();
 			
 			// Get the entity using the ID.
 			WorldServer world = server.worldServerForDimension(event.getPlayer().dimension);
 			Entity entity = world.getEntityByID(packet.entityID);
 			
 			// Only handle if the player referenced in the packet is different than the player receiving it.
 			if (!event.getPlayer().equals(entity) && entity instanceof EntityPlayerMP) {
 				EntityPlayerMP referencedPlayer = (EntityPlayerMP)entity;
 				
 				// Armor is always visible in PvP
 				if (!PlayerAPI.onCheckPvPEnabled(referencedPlayer)) {
 					
 					// Get the referenced player's hide settings.
 					HideSettings settings = playerSettings.get(referencedPlayer.username);
 					
 					// Only handle armor slots.
 					if (settings != null && packet.slot > 0 && settings.isHiddenForSlot(packet.slot) && !isAlwaysVisible(referencedPlayer.getEquipmentInSlot(packet.slot))) {
 						event.replaceWithPacket(new Packet5PlayerInventory(packet.entityID, packet.slot, null));
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Determine if an item will always be shown as equipment.
 	 * 
 	 * @param stack The item stack to check.
 	 * @return true if it is always shown; false otherwise.
 	 */
 	public boolean isAlwaysVisible(ItemStack stack) {
 		if (stack != null &&
			((plateAlwaysVisible && Item.itemsList[stack.itemID] instanceof FCItemRefinedArmor)
			|| (specialAlwaysVisible && Item.itemsList[stack.itemID] instanceof FCItemSpecialArmor)))
 			return true;
 		
 		return false;
 	}
 
 	@Override
 	public void onPlayerInstanceAction(PlayerInstanceEvent event) {
 		if (event.getType() == TYPE.METADATA_CHANGED && event.getMetadata() == METADATA.IS_PVP) {
 			WorldAPI.sendEntityEquipmentUpdate(event.getPlayerInstance());
 		}
 		else if (event.getType() == TYPE.READ_NBT) {
 			HideSettings settings = new HideSettings();
 			settings.helm = event.getNBTTagCompound().getBoolean("helm");
 			settings.chest = event.getNBTTagCompound().getBoolean("chest");
 			settings.legs = event.getNBTTagCompound().getBoolean("legs");
 			settings.boots = event.getNBTTagCompound().getBoolean("boots");
 			playerSettings.put(event.getPlayerInstance().username, settings);
 		}
 		else if (event.getType() == TYPE.WRITE_NBT) {
 			HideSettings settings = playerSettings.get(event.getPlayerInstance().username);
 			if (settings != null && settings.isHidingArmor()) {
 				event.getNBTTagCompound().setBoolean("helm", settings.helm);
 				event.getNBTTagCompound().setBoolean("chest", settings.chest);
 				event.getNBTTagCompound().setBoolean("legs", settings.legs);
 				event.getNBTTagCompound().setBoolean("boots", settings.boots);
 			}
 		}
 	}
 
 	@Override
 	public String getCommandName() {
 		return "armor";
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender sender) {
 		return "/" + getCommandName() + " <status|hide|show> [<helm|chest|legs|boots> ...]";
 	}
 
 	@Override
 	public boolean canCommandSenderUseCommand(ICommandSender sender) {
 		return !server.isPVPEnabled() && sender instanceof EntityPlayer;
 	}
 
 	@Override
 	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
 		if (sender instanceof EntityPlayer) {
 			if (args.length == 1) {
 				return getListOfStringsMatchingLastWord(args, new String[] { "hide", "show" });
 			}
 			else {
 				return getListOfStringsMatchingLastWord(args, new String[] { "helm", "chest", "legs", "boots" });
 			}
 		}
 		
 		return super.addTabCompletionOptions(sender, args);
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if (sender instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer)sender;
 			if (args.length >= 1 && args[0].equalsIgnoreCase("status")) {
 				HideSettings settings = playerSettings.get(player.username);
 				String list = "";
 				
 				if (settings != null && (list = settings.toString()).length() != 0) {
 					sender.sendChatToPlayer("You are hiding: " + list);
 				}
 				else {
 					sender.sendChatToPlayer("You are not hiding armor.");
 				}
 			}
 			else if (args.length >= 1 && args[0].matches("^(hide|show)$")) {
 				
 				// Get the settings, creating them if necessary.
 				HideSettings settings = playerSettings.get(player.username);
 				if (settings == null) {
 					playerSettings.put(player.username, settings = new HideSettings());
 				}
 				
 				boolean oldIsHidingArmor = settings.isHidingArmor();
 				boolean state = args[0].equalsIgnoreCase("hide");
 				
 				if (args.length == 1) {
 					settings.setAll(state);
 				}
 				else {
 					for (int i = 1; i < args.length; i++) {
 						if (args[i].equalsIgnoreCase("helm"))
 							settings.helm = state;
 						
 						else if (args[i].equalsIgnoreCase("chest"))
 							settings.chest = state;
 						
 						else if (args[i].equalsIgnoreCase("legs"))
 							settings.legs = state;
 						
 						else if (args[i].equalsIgnoreCase("boots"))
 							settings.boots = state;
 						
 						else
 							throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
 					}
 				}
 				
 				if (settings.isHidingArmor())
 					sender.sendChatToPlayer("The following is now hidden: " + settings.toString());
 				else if (!oldIsHidingArmor)
 					sender.sendChatToPlayer("You are not hiding any armor.");
 				else
 					sender.sendChatToPlayer("You are no longer hiding any armor.");
 				
 				WorldAPI.sendEntityEquipmentUpdate(player);
 			}
 			else {
 				throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
 			}
 		}
 	}
 	
 	private class HideSettings {
 		public boolean helm = false;
 		public boolean chest = false;
 		public boolean legs = false;
 		public boolean boots = false;
 		
 		public boolean isHiddenForSlot(int slot) {
 			switch (slot) {
 				case 4: return helm;
 				case 3: return chest;
 				case 2: return legs;
 				case 1: return boots;
 				default: return false;
 			}
 		}
 		
 		public void setAll(boolean value) {
 			helm = chest = legs = boots = value;
 		}
 		
 		public boolean isHidingArmor() {
 			return helm || chest || legs || boots;
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder sb = new StringBuilder();
 			if (helm) { if (sb.length() > 0) sb.append(", "); sb.append("helm"); }
 			if (chest) { if (sb.length() > 0) sb.append(", "); sb.append("chest"); }
 			if (legs) { if (sb.length() > 0) sb.append(", "); sb.append("legs"); }
 			if (boots) { if (sb.length() > 0) sb.append(", "); sb.append("boots"); }
 			return sb.toString();
 		}
 	}
 
 }
