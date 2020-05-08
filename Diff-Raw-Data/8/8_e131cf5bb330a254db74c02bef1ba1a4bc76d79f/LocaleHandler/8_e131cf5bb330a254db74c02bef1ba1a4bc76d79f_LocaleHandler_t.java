 package net.invisioncraft.plugins.salesmania.configuration;
 
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 
 /**
  * Owner: Justin
  * Date: 5/22/12
  * Time: 5:51 AM
  */
 public class LocaleHandler {
     private static FileConfiguration config;
     private Salesmania plugin;
     HashMap<String, Locale> localeMap;
     public LocaleHandler(Salesmania plugin) {
         this.plugin = plugin;
         localeMap = new HashMap<String, Locale>();
        localeMap.put(plugin.getSettings().getDefaultLocale(), new Locale(plugin, plugin.getSettings().getDefaultLocale()));
         config = new Configuration(plugin, "playerLocale.yml").getConfig();
     }
 
     public Locale getPlayerLocale(Player player) {
         String localeName = config.getString(player.getName());
         if(localeName == null) localeName = plugin.getSettings().getDefaultLocale();
         return getLocale(localeName);
     }
 
     private Locale getLocale(String localeName) {
         Locale locale = localeMap.get(localeName);
         if(locale == null) {
             if(plugin.getSettings().getLocales().contains(localeName)) {
                 return localeMap.put(localeName, new Locale(plugin, localeName));
             }
            else return localeMap.get(plugin.getSettings().getDefaultLocale());
         }
        else return locale;
     }
 }
