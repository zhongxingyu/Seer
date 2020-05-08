 package net.loadingchunks.plugins.AuthenticItem.AuthenticItem;
 
 /*
     This file is part of AuthenticItem
 
     Foobar is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Foobar is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import org.bukkit.ChatColor;
 
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 public class AuthenticItemEventListener implements Listener {
 
 	private AuthenticItem plugin;
 
 	public AuthenticItemEventListener(AuthenticItem plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void playerChat(PlayerChatEvent event) // I hate myself for using that event type.
 	{
 		CraftItemStack cstack = (CraftItemStack)event.getPlayer().getItemInHand();
 		
 		if(cstack == null || cstack.getTypeId() == 0)
 			return;
 		
 		if(!event.getMessage().contains("{hand}"))
 			return;
 		
		AItem item = new AItem(cstack);
 		item.setPlugin(plugin);
 		
 		String displayName = item.getDisplayName();
 		String name = "";
 				
 		if(displayName == null || displayName.isEmpty())
 			name = cstack.getType().name();
 		else
 			name = displayName;
 
 		event.setMessage(event.getMessage().replace("{hand}", name));
 	}
 
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent event)
 	{
		AItem item = new AItem((CraftItemStack)event.getItem().getItemStack());
 		item.setPlugin(plugin);
 		
 		if(item.getAuthentic() == null)
 			return;
 		
 		String displayName = item.getDisplayName();
 		String name = "";
 
 		if(displayName.isEmpty() || displayName == null)
 			name = event.getItem().getItemStack().getType().name();
 		else
 			name = displayName;
 		
 		String aPrefix = "";
 		
 		if(name.toLowerCase().charAt(0) == 'a' || name.toLowerCase().charAt(0) == 'e' || name.toLowerCase().charAt(0) == 'i' || name.toLowerCase().charAt(0) == 'o' || name.toLowerCase().charAt(0) == 'u')
 			aPrefix = "n";
 
 		try {
 			AuthenticTypes.valueOf(item.getAuthentic().toUpperCase());
 			event.getPlayer().sendMessage("You have picked up a" + aPrefix + " " + name + ChatColor.RESET);
 		} catch (Exception e)
 		{
 			event.getPlayer().sendMessage("You have picked up an " + ChatColor.GRAY + "Unknown " + name);
 		}
 	}
 }
