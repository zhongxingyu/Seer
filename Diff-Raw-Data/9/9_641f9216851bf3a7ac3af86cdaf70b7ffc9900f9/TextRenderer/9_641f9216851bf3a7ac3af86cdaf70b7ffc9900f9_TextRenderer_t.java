 package com.m0pt0pmatt.menuservice.renderers;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.m0pt0pmatt.menuservice.MenuServicePlugin;
 import com.m0pt0pmatt.menuservice.api.AbstractRenderer;
 import com.m0pt0pmatt.menuservice.api.Action;
 import com.m0pt0pmatt.menuservice.api.ActionEvent;
 import com.m0pt0pmatt.menuservice.api.ActionListener;
 import com.m0pt0pmatt.menuservice.api.Component;
 import com.m0pt0pmatt.menuservice.api.Menu;
 import com.m0pt0pmatt.menuservice.api.MenuInstance;
 import com.m0pt0pmatt.menuservice.api.MenuService;
 import com.m0pt0pmatt.menuservice.api.attributes.ContainerAttribute;
 
 public class TextRenderer extends AbstractRenderer implements Listener{
 
 	public TextRenderer(MenuService menuService, MenuServicePlugin plugin) {
 		super(menuService);
 		Bukkit.getPluginManager().registerEvents(this, plugin);
 	}
 
 	@Override
 	public void renderPlayer(MenuInstance menuInstance, String playerName) {
 		if (menuInstance == null){
 			return;
 		} else if (playerName == null){
 			return;
 		}
 		
 		Player player = Bukkit.getPlayer(playerName);
 		if (player == null){
 			return;
 		}
 		
 		Menu menu = menuInstance.getMenu();
 		player.sendMessage("MENU: " + menu.getAttribute("title"));
 		for (Component component: menu.getComponents().values()) {
 			player.sendMessage(component.getAttribute("text") + ": " + component.getAttribute("lore"));
 		}
 		
 	}
 
 	@Override
 	public void renderAllPlayers(MenuInstance menuInstance) {
 		if (menuInstance == null){
 			return;
 		}
 		
 		for (String playerName: menuInstance.getPlayers()){
 			this.renderPlayer(menuInstance, playerName);
 		}
 		
 	}
 
 	@Override
 	public String getName() {
 		return "text";
 	}
 
 	@Override
 	public void closeMenu(String playerName) {
 		MenuInstance instance = this.getPlayers().get(playerName);
 		
 		//remove the player from the Renderer
 		getPlayers().remove(playerName);
 		
 		//remove the player from the instance
 		instance.removePlayer(playerName);
 	}
 	
 	/**
 	 * Catch when a player executes a command if interaction should occur
 	 * @param event
 	 */
 	@EventHandler
     public void onCommand(PlayerCommandPreprocessEvent event) {
         Player player = event.getPlayer();
         String command = event.getMessage();
                 
         MenuInstance instance = this.getPlayers().get(player.getName());
         if (instance == null){
         	return;
         }
         
         Menu menu = instance.getMenu();
         Component component = menu.getComponents().get(command);
         if (component == null){
         	return;
         }
         
         //get the actions
  		ContainerAttribute actions = component.getContainerAttribute("actions");
   		if (actions != null){
 
   			//run each action
   			for (Object action: actions.getAttributes().values()){
   				if (action instanceof ContainerAttribute){
   					
   					executeAction((ContainerAttribute) action, event, instance, component);
   				}
   			}
   		}
     }
 	
 	/**
 	 * Executes an Action on a given Component
 	 * @param action the ContainerAttrubute that specifies the Action
 	 * @param event the InventoryClickEvent
 	 * @param instance the MenuInstance of the Action
 	 * @param component the Component
 	 */
 	@SuppressWarnings("unchecked")
 	private void executeAction(ContainerAttribute action, PlayerCommandPreprocessEvent event, MenuInstance instance, Component component){
 		Player player = event.getPlayer();
 		
 		//check if the player has permission to interact with the component
 		if (!hasPermissions(player, component)){
 			player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
 			return;
 		}
 		
 		//check if the player has permission to activate the action
 		if (!hasPermissions(player, action)){
 			player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
 			return;
 		}
 		
 		//get the list of action tags
 		List<Integer> actionTags = null;
 		try{
 			actionTags = ((List<Integer>)action.getAttribute("tags"));
 		} catch (ClassCastException e){
 			return;
 		}
 		if (actionTags != null){
 			
 			//execute each tag for each ActionListener tied to the instance
 			for (Integer tag: actionTags){
 			
 				for (ActionListener listener: instance.getActionListeners().values()){
 					String plugin = instance.getMenu().getPlugin();
 					listener.handleAction(new ActionEvent(new Action(plugin, tag, player.getName(), instance, action.getName())));
 				}			
 			}
 		}
 		
 		//get the list of commands
 		List<String> commands = null;
 		try {
 			commands = ((List<String>)action.getAttribute("commands"));
 		} catch (ClassCastException e){
 			return;
 		}
 		
 		if (commands != null){
 			
 			//get the command sender
 			String sender = (String) action.getAttribute("sender");
 			CommandSender cSender = this.getCommandSender(player, sender);
 			if (cSender == null){
 				return;
 			}
 			
 			//execute the string of commands
 			for (String command: commands){
 				
 				//replace placeholders
 				command = command.replaceAll("<player>", player.getName());
 				command = command.replaceAll("<sender>", cSender.getName());
 
 				//execute the command
 				Bukkit.dispatchCommand(cSender, command);	
 			}
 		}
 	}
 
 }
