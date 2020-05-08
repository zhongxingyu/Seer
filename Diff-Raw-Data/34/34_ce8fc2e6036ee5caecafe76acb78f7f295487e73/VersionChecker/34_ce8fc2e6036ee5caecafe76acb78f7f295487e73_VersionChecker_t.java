 package burptech.lib;
 
 import burptech.BurpTechCore;
 import cpw.mods.fml.common.*;
 import net.minecraft.entity.player.EntityPlayer;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 /**
  * Based on PRVersionChecker from Project Red - https://github.com/MrTJP/ProjectRed/blob/master/common/mrtjp/projectred/core/PRVersionChecker.java
  */
 public class VersionChecker extends Thread implements ITickHandler
 {
     public boolean isOutdated = false;
     public String newVersion;
     public boolean hasRan = false;
     public boolean hasDisplayed = false;
 
     public VersionChecker()
     {
         this.setName("BurpTech Version Checker");
         this.setDaemon(true);
         this.start();
     }
 
     @Override
     public void run()
     {
         if (hasRan)
             return;
 
         hasRan = true;
 
        String current = Constants.MOD_VERSION;

         try
         {
             if (current.contains("@"))
                 return;
 
             BurpTechCore.log.info("Checking for updates...");
 
             URL url = new URL("https://raw.github.com/RenEvo/BurpTech/master/src/resources/ChangeLog");
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
 
             String version;
             while ((version = reader.readLine()) != null)
             {
                 if (version.length() > 0 && version.startsWith("v"))
                 {
                     newVersion = version.substring(1);
                     break;
                 }
             }

            isOutdated = !current.equals(newVersion);
         }
         catch (Throwable e)
         {
            BurpTechCore.log.info("Version Check Failed With: " + e.getLocalizedMessage());
         }
 
         if (isOutdated)
             BurpTechCore.log.info("Found Updated Version " + newVersion);
     }
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData)
     {
         // NOOP
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData)
     {
         if (!isOutdated || hasDisplayed)
             return;
 
         hasDisplayed = true;
 
         EntityPlayer player = (EntityPlayer)tickData[0];
 
        player.addChatMessage("Version " + newVersion + " of §aBurpTech§r is available");
     }
 
     @Override
     public EnumSet<TickType> ticks()
     {
         return EnumSet.of(TickType.PLAYER);
     }
 
     @Override
     public String getLabel()
     {
         return super.getName();
     }
 }
