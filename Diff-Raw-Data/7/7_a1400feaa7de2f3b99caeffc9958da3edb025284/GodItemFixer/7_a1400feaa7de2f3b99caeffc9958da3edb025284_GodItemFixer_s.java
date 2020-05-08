 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.bukkitdev.goditemfixer;
 
import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author daboross
  */
public class GodItemFixer extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
     }
 }
