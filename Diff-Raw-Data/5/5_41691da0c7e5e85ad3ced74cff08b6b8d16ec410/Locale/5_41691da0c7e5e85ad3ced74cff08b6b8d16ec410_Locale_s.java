 package net.invisioncraft.plugins.salesmania.configuration;
 
 import net.invisioncraft.plugins.salesmania.Salesmania;
 
 import java.util.List;
 
 /**
  * Owner: Byte 2 O Software LLC
  * Date: 5/16/12
  * Time: 7:29 PM
  */
 /*
 Copyright 2012 Byte 2 O Software LLC
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 public class Locale extends Configuration {
     private String localeName;
     public Locale(Salesmania plugin, String locale) {
         super(plugin, locale + ".yml");
         localeName = locale;
         plugin.getLogger().info(String.format("Loaded locale messages for %s", locale));
     }
 
     public String getMessage(String path) {
        if(getConfig().contains(path)) return getConfig().getString(path);
         else return "Locale message not found.";
     }
 
     public List<String> getMessageList(String path) {
         return getConfig().getStringList(path);
     }
 
     public String getName() {
         return localeName;
     }
 }
