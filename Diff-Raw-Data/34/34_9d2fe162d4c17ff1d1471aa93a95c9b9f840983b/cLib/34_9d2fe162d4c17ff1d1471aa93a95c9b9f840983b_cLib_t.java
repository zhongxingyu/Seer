 package net.catharos.lib;
 
 import net.catharos.lib.command.CommandManager;
 import net.catharos.lib.plugin.Plugin;
 
 
 public class cLib extends Plugin {
	private static cLib instance;
 	protected CommandManager cmdManager;
 	
 	
 	@Override
 	public void onLoad() {
		cLib.instance = this;
 		this.cmdManager = new CommandManager();
 	}
 	
	
	/**
	 * Returns the plugin instance.
	 * 
	 * @return The current plugin instance
	 */
 	public static cLib getInstance() {
		return cLib.instance;
 	}
 	
 	public CommandManager getCommandManager() {
 		return this.cmdManager;
 	}
 	
 }
