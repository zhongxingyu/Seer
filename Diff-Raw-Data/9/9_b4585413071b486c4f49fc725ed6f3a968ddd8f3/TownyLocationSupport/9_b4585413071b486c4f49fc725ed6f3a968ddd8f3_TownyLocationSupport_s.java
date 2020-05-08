 package com.github.omwah.SDFEconomy.location;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 /**
  * Acts as a proxy adding support for Towny account names which
  * do not provide location information.
  * Towny accounts have a location separate from all other economy accounts.
  * 
  */
 public class TownyLocationSupport implements LocationTranslator {
     LocationTranslator translator;
     String locationName;
     
     public TownyLocationSupport(LocationTranslator translator, String locationName) {
         this.translator = translator;
         this.locationName = locationName.toLowerCase();
     }
 
     public String getLocationName(String playerName) {
         String proxied_name = translator.getLocationName(playerName);
        if (proxied_name == null && playerName.startsWith("town-")) {
             return this.locationName;
         } else {
             return proxied_name;
         }
     }
 
     public String getLocationName(Player player) {
         return translator.getLocationName(player);
     }
 
     public String getLocationName(Location location) {
         return translator.getLocationName(location);
     }
 
     public boolean validLocationName(String locationName) {
         if (locationName.equalsIgnoreCase(this.locationName)) {
             return true;
         } else {
             return translator.validLocationName(locationName);
         }
     }
     
 }
