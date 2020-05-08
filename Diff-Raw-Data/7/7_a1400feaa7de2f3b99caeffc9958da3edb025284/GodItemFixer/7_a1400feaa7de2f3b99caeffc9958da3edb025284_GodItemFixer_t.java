 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.bukkitdev.goditemfixer;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author daboross
  */
public class GodItemFixer extends JavaPlugin {
 
     @Override
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GodItemFixerListener(this), this);
     }
 
     @Override
     public void onDisable() {
     }
 }
