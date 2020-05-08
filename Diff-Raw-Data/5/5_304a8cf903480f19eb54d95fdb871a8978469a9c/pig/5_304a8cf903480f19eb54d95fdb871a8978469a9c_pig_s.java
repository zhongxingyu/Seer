 package me.hammale.pig;
 
 import java.io.*;
import java.io.PrintWriter;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import net.citizensnpcs.api.CitizensManager;
 import net.citizensnpcs.resources.npclib.*;
 
 import me.desmin88.mobdisguise.api.*;
 
 public class pig extends JavaPlugin {
 	
 	Logger log = Logger.getLogger("Minecraft");
 	
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[NerdPig] Version: " + pdfFile.getVersion() + " Enabled!");
 	    PluginManager pm = getServer().getPluginManager();
 	    Plugin test = pm.getPlugin("Citizens");
 	    if (test != null) {
 	        System.out.println("[NerdPig] hooked into Citizens!");
 	    } else {
 	        System.out.println("[NerdPig] Citizens isn't loaded.");
 	        pm.disablePlugin(this);
 	    }
 	    Plugin test1 = pm.getPlugin("MobDisguise");
 	    if (test1 != null) {
 	        System.out.println("[NerdPig] hooked into MobDisguise!");
 	    } else {
 	        System.out.println("[NerdPig] MobDisguise isn't loaded.");
 	        pm.disablePlugin(this);
 	    }
 	    createFolders();
 	    fixNpc();
 	}
 
 	public void createFolders() {
 		File f = new File("plugins/NerdPig");
 		if(!f.exists()){
 			f.mkdir();
 		}
 		File f1 = new File("plugins/NerdPig/npcs");
 		if(!f1.exists()){
 			f1.mkdir();
 		}
 	}
 
 	public void fixNpc(){
 		for (World world : getServer().getWorlds()) {
 			for(Entity e : world.getEntities()){
 				if(e instanceof LivingEntity){
 					LivingEntity le = (LivingEntity) e;
 					if(le instanceof Player){
 						Player p = (Player) le;
 						if(CitizensManager.isNPC(e)){
 							readData(p.getName());
 						}
 					}
 				}
 			}
 		}	
 	}
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[NerdPig] Version: " + pdfFile.getVersion() + " Disabled!");	
 	}
 	
 	
 	  public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args){
 			if(cmd.getName().equalsIgnoreCase("npchide")){
 					if(args.length >= 1){
 						if(sender instanceof Player){
 							Player p = (Player) sender;
 							if(CitizensManager.validateSelected(p) == true){
 								int i = CitizensManager.getSelected(p);
 								HumanNPC npc = CitizensManager.getNPC(i);
 								MobDisguiseAPI.undisguisePlayer(npc.getPlayer());
 								MobDisguiseAPI.disguisePlayer(npc.getPlayer(), args[0]);
 								addData(npc.getPlayer().getName(), args[0]);
 								sender.sendMessage(ChatColor.GREEN + "Hiding " + npc.getPlayer().getName() + " as a " + args[0]);
 								return true;
 							}else{
 								sender.sendMessage(ChatColor.RED + "Please select an NPC before hiding!");
 							}
 						}
 					}
 			return false;
 			}
 			return false;
 	  }
 	  
 	  public void removeFile(String s) {
 		  File f = new File(s + ".dat");
 		  if(f.exists()){
 			  boolean success = f.delete();
 			  if (!success){
 				  throw new IllegalArgumentException("[NerdPig] Deletion failed!");
 			  }
 		  }
 	  }
 	  
 	  public void addData(String npc, String mob) {
 		  createFolders();
 //		  try{
 //			  File file = new File("plugins/NerdPigs/npcs/" + npc + ".dat");
 //		      String str = null;		    
 //		      //if (file.exists()) {
 //		    	//  removeFile(npc);
 //		      //}
 //		      str = (npc + "," + mob);	              
 //		      PrintWriter out = new PrintWriter(new FileWriter(file, true));	    
 //		      out.println(str);
 //		      out.close();
 //		  }catch (Exception e){
 //			  System.err.println("[NerdPig] Error1: " + e.getMessage());
 //		  }		  
 		  try{
 			  FileWriter fstream = new FileWriter(npc + ".dat");
 			  BufferedWriter out = new BufferedWriter(fstream);
 			  String str = (npc + "," + mob);	
 			  out.write(str);
 			  out.close();
 			  }catch (Exception e){
 			  System.err.println("Error1: " + e.getMessage());
 			  }
 	}
 	  
 	  public String readData(String s){
 
 		  try{
 			  File file = new File(s + ".dat");
 			  if(file.exists()){				  			  
 				  FileInputStream fstream = new FileInputStream(s + ".dat");
 				  DataInputStream in = new DataInputStream(fstream);
 				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
 				  String strLine;
 				  while ((strLine = br.readLine()) != null){
 					  return strLine;
 				  }
 				  in.close();
 			  }
 			  return null;
 		  }catch (Exception e){
 			  System.err.println("[NerdPig] Error2: " + e.getMessage());
 		  }
 		  return null;
 	  }
 }
