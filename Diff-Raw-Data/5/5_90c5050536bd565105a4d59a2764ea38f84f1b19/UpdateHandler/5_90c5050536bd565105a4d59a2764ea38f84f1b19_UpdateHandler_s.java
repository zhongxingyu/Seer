 /*******************************************************************************
  * UpdateHandler.java
  * Copyright (c) 2013 WildBamaBoy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 
 package arrowsplus.core.util.object;
 
 import java.net.URL;
 import java.util.Scanner;
 
 import net.minecraft.command.ICommandSender;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.util.ChatMessageComponent;
 import arrowsplus.core.ArrowsPlus;
 import arrowsplus.core.io.ModPropertiesManager;
 import arrowsplus.core.util.Color;
 import arrowsplus.core.util.Format;
 
 /**
  * Checks for outdated versions and updates.
  */
 public class UpdateHandler implements Runnable
 {
 	/** The current version of Arrows Plus. */
	public static final String VERSION = "1.0.1";
 	
 	/** The update's compatible Minecraft version. */
 	public static String compatibleMinecraftVersion = "";
 	
 	/** The most recent version of ArrowsPlus. */
 	public static String mostRecentVersion = "";
 	
 	private NetHandler netHandler = null;
 	private ICommandSender commandSender = null;
 
 	/**
 	 * Constructor used when a player logs in.
 	 * 
 	 * @param 	netHandler	The NetHandler of the player that just logged in.
 	 */
 	public UpdateHandler(NetHandler netHandler)
 	{
 		this.netHandler = netHandler;
 	}
 
 	/**
 	 * Constructor used when a player issues the /mca.checkupdates on command.
 	 * 
 	 * @param 	commandSender	The player that sent the command.
 	 */
 	public UpdateHandler(ICommandSender commandSender)
 	{
 		this.commandSender = commandSender;
 	}
 
 	@Override
 	public void run()
 	{
 		try
 		{
 			if (!ArrowsPlus.instance.hasCheckedForUpdates && !ArrowsPlus.instance.isDedicatedServer && !ArrowsPlus.instance.isDedicatedClient)
 			{
 				ArrowsPlus.instance.hasCheckedForUpdates = true;
 				URL url = new URL("http://pastebin.com/raw.php?i=RwcDNJEr");
 				Scanner scanner = new Scanner(url.openStream());
 
 				compatibleMinecraftVersion = scanner.nextLine();
 				mostRecentVersion = scanner.nextLine();
 
 				ModPropertiesManager manager = ArrowsPlus.instance.modPropertiesManager;
 				
 				ArrowsPlus.instance.log(!mostRecentVersion.equals(VERSION) && (manager.modProperties.checkForUpdates || !manager.modProperties.lastFoundUpdate.equals(mostRecentVersion)));
 				ArrowsPlus.instance.log(!mostRecentVersion.equals(VERSION));
 				ArrowsPlus.instance.log(manager.modProperties.checkForUpdates);
 				ArrowsPlus.instance.log(!manager.modProperties.lastFoundUpdate.equals(mostRecentVersion));
 				
 				
 				if (!mostRecentVersion.equals(VERSION) && (manager.modProperties.checkForUpdates || !manager.modProperties.lastFoundUpdate.equals(mostRecentVersion)))
 				{
 					if (netHandler != null)
 					{
 						netHandler.getPlayer().sendChatToPlayer(new ChatMessageComponent().func_111072_b(
 								Color.YELLOW + 
 								"Arrows Plus version " + mostRecentVersion + " for Minecraft " + compatibleMinecraftVersion + " is available. " +
 								"See " + Color.BLUE + Format.ITALIC + "http://goo.gl/ZumsWE" +
 								Format.RESET + Color.YELLOW + " to download the update."
 								));
 						
 						netHandler.getPlayer().sendChatToPlayer(new ChatMessageComponent().func_111072_b(
 								Color.YELLOW + 
								"To turn off notifications about this update, type /arrowsplus.checkupdates off."
 								));
 					}
 
 					else if (commandSender != null)
 					{
 						commandSender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(
 								Color.YELLOW + 
 								"Arrows Plus version " + mostRecentVersion + " for Minecraft " + compatibleMinecraftVersion + " is available. " +
 								"See " + Color.BLUE + Format.ITALIC + "http://goo.gl/ZumsWE" +
 								Format.RESET + Color.YELLOW + " to download the update."
 								));
 						
 						commandSender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(
 								Color.YELLOW + 
 								"To turn off notifications about this update, type /ap.checkupdates off."
 								));
 					}
 				}
 
 				manager.modProperties.lastFoundUpdate = mostRecentVersion;
 				manager.saveModProperties();
 				scanner.close();
 			}
 		}
 
 		catch (Throwable e)
 		{
 			ArrowsPlus.instance.log(e);
 		}
 	}
 }
