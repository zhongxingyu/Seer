 //    SpoutMenu: Give your players a simple menu with Spout.
 //    Copyright (C) 2011 AVirusC
 //	  
 //This program is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 
 //This program is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 //GNU General Public License for more details.
 
 //You should have received a copy of the GNU General Public License
 //along with this program. If not, see <http://www.gnu.org/licenses/>.
 
 package me.avirusc.SpoutMenu;
 
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.event.input.InputListener;
 import org.getspout.spoutapi.event.input.KeyPressedEvent;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.event.screen.ScreenListener;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.keyboard.Keyboard;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class SpoutMenu extends JavaPlugin {
 	protected FileConfiguration config;
 
 	Logger log = Logger.getLogger("Minecraft");	
 	
 	public void onEnable(){
 		getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, new SMInputListener(), Priority.Normal, this);
 		getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, new SMScreenListener(), Priority.Normal, this);
 		
 		this.getConfig().options().copyDefaults(true);
 	        saveConfig();	      
 
 	    log.info("[SpoutMenu] Version 0.1 is enabled.");
 
 	}
 	
 	public void onDisable(){
 		log.info("[SpoutMenu] Version 0.1 is disabled.");
 	}
 	
 	public class SMInputListener extends InputListener{
 		
 		@Override
 		public void onKeyPressedEvent(KeyPressedEvent event) {
			if (event.getPlayer().getActiveScreen() != ScreenType.SIGN_SCREEN) {
 			if (event.getPlayer().getActiveScreen() != ScreenType.CHAT_SCREEN) {
 			if (event.getKey() == Keyboard.KEY_M) {
 	
 				GenericPopup spmenu = new GenericPopup();
 				
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name1")).setX(23).setY(10).setWidth(125).setHeight(21));	
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name2")).setX(23).setY(40).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name3")).setX(23).setY(70).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name4")).setX(23).setY(100).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name5")).setX(23).setY(130).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name6")).setX(23).setY(160).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name7")).setX(23).setY(190).setWidth(125).setHeight(21));
 				
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name8")).setX(153).setY(10).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name9")).setX(153).setY(40).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name10")).setX(153).setY(70).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name11")).setX(153).setY(100).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name12")).setX(153).setY(130).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name13")).setX(153).setY(160).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name14")).setX(153).setY(190).setWidth(125).setHeight(21));
 				
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name15")).setX(283).setY(10).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name16")).setX(283).setY(40).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name17")).setX(283).setY(70).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name18")).setX(283).setY(100).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name19")).setX(283).setY(130).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name20")).setX(283).setY(160).setWidth(125).setHeight(21));
 				spmenu.attachWidget(null, new GenericButton(getConfig().getString("name21")).setX(283).setY(190).setWidth(125).setHeight(21));
 				
 				spmenu.attachWidget(null, new GenericLabel("SpoutMenu by AVirusC").setAlign(WidgetAnchor.BOTTOM_CENTER).setAnchor(WidgetAnchor.BOTTOM_CENTER));
 				
 				//((SpoutPlayer) event.getPlayer()).getMainScreen().attachPopupScreen(spmenu);
 				((SpoutPlayer) event.getPlayer()).getMainScreen().attachPopupScreen(spmenu); 
 			}
			}
 		}
 	}
 	}
 	public class SMScreenListener extends ScreenListener {
 
 		@Override
 		public void onButtonClick(ButtonClickEvent event) {
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name1"))) {				
 				event.getPlayer().chat(getConfig().getString("command1"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name2"))) {				
 				event.getPlayer().chat(getConfig().getString("command2"));	
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name3"))) {				
 				event.getPlayer().chat(getConfig().getString("command3"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name4"))) {				
 				event.getPlayer().chat(getConfig().getString("command4"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name5"))) {				
 				event.getPlayer().chat(getConfig().getString("command5"));	
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name6"))) {				
 				event.getPlayer().chat(getConfig().getString("command6"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name7"))) {				
 				event.getPlayer().chat(getConfig().getString("command7"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name8"))) {				
 				event.getPlayer().chat(getConfig().getString("command8"));	
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name9"))) {				
 				event.getPlayer().chat(getConfig().getString("command9"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name10"))) {				
 				event.getPlayer().chat(getConfig().getString("command10"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name11"))) {				
 				event.getPlayer().chat(getConfig().getString("command11"));	
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name12"))) {				
 				event.getPlayer().chat(getConfig().getString("command12"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name13"))) {				
 				event.getPlayer().chat(getConfig().getString("command13"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name14"))) {				
 				event.getPlayer().chat(getConfig().getString("command14"));	
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name15"))) {				
 				event.getPlayer().chat(getConfig().getString("command15"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name16"))) {				
 				event.getPlayer().chat(getConfig().getString("command16"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name17"))) {				
 				event.getPlayer().chat(getConfig().getString("command17"));	
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name18"))) {				
 				event.getPlayer().chat(getConfig().getString("command18"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name19"))) {				
 				event.getPlayer().chat(getConfig().getString("command19"));
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name20"))) {				
 				event.getPlayer().chat(getConfig().getString("command20"));	
 			}
 			if (event.getButton() instanceof GenericButton && event.getButton().getText().equals(getConfig().getString("name21"))) {				
 				event.getPlayer().chat(getConfig().getString("command21"));
 			}
 		}	
 	}
 }
