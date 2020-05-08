 package com.volumetricpixels.mcsquared;
 
 import java.util.logging.Level;
 import org.spout.api.plugin.Plugin;
 
 public class MCSquared extends Plugin {
 
     @Override
     public void onEnable() {
        getLogger().log(Level.INFO, "MCSquared version: {0} has been enabled.", getDescription().getVersion());
     }
 
     @Override
     public void onDisable() {
        getLogger().log(Level.INFO, "MCSquared version: {0} has been disabled.", getDescription().getVersion());
     }
}
