 package com.asylumsw.bukkit.kits;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author jonathan
  */
 public class Kits extends JavaPlugin {
 	private PackageList packages;
 	
 	@Override
 	public void onEnable() {
 		packages = new PackageList();
 		packages.load();
 
 		// EXAMPLE: Custom code, here we just output some info so we can check all is well
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		System.out.println("Kits Disabled.");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if( !(sender instanceof Player) ) return false;
 
 		if( cmd.getName().equalsIgnoreCase("kit") ) {
 			if( 1 > args.length ) return false;
 			if( args[0].equalsIgnoreCase("list") ) {
 				packages.listPackages((Player)sender);
 			}
			else {
				packages.givePlayerPackage((Player)sender, args[0]);
			}
 			return true;
 		}
 		
 		return false;
 	}
 
 	public static void main(String[] args) {}
 }
