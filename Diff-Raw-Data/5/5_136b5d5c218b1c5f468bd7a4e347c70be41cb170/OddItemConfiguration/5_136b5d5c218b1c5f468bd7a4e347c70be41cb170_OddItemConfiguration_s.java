 /* This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, "either" version 3 of the License, "or"
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, "see" <http://www.gnu.org/licenses/>.
  */
 package info.somethingodd.bukkit.OddItem;
 
 import info.somethingodd.bukkit.OddItem.configuration.OddItemAliases;
 import info.somethingodd.bukkit.OddItem.configuration.OddItemGroup;
 import info.somethingodd.bukkit.OddItem.configuration.OddItemGroups;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * @author Gordon Pettey (petteyg359@gmail.com)
  */
 public class OddItemConfiguration {
     private int version;
     private static String comparator = "r";
     private final OddItemBase oddItemBase;
 
     public OddItemConfiguration(OddItemBase oddItemBase) {
         this.oddItemBase = oddItemBase;
     }
 
     public static String getComparator() {
         return comparator;
     }
 
     public void configure() {
         String[] filenames = {"config.yml", "items.yml", "groups.yml"};
         try {
             initialConfig(filenames);
         } catch (Exception e) {
             oddItemBase.log.warning("Exception writing initial configuration files: " + e.getMessage());
             e.printStackTrace();
         }
 
         YamlConfiguration yamlConfiguration = (YamlConfiguration) oddItemBase.getConfig();
         comparator = yamlConfiguration.getString("comparator", "r");
         version = yamlConfiguration.getInt("version", 0);
 
         ConfigurationSerialization.registerClass(OddItemAliases.class);
 
         YamlConfiguration itemConfiguration = YamlConfiguration.loadConfiguration(new File(oddItemBase.getDataFolder(), "items.yml"));
         itemConfiguration.setDefaults(YamlConfiguration.loadConfiguration(oddItemBase.getResource("items.yml")));
         ConfigurationSerialization.registerClass(OddItemAliases.class);
        OddItem.items = (OddItemAliases) itemConfiguration.get("items", new OddItemAliases());
 
         ConfigurationSerialization.registerClass(OddItemGroup.class);
         ConfigurationSerialization.registerClass(OddItemGroups.class);
 
         YamlConfiguration groupConfiguration = YamlConfiguration.loadConfiguration(new File(oddItemBase.getDataFolder(), "groups.yml"));
         itemConfiguration.setDefaults(YamlConfiguration.loadConfiguration(oddItemBase.getResource("groups.yml")));
         ConfigurationSerialization.registerClass(OddItemAliases.class);
        OddItem.groups = (OddItemGroups) groupConfiguration.get("groups");
     }
 
     private void initialConfig(String[] filenames) throws IOException {
         for (String filename : filenames) {
             File file = new File(oddItemBase.getDataFolder(), filename);
             if (!file.exists()) {
                 BufferedReader src = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + filename)));
                 BufferedWriter dst = new BufferedWriter(new FileWriter(file));
                 try {
                     file.mkdirs();
                     file.createNewFile();
                     src = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + filename)));
                     dst = new BufferedWriter(new FileWriter(file));
                     String line = src.readLine();
                     while (line != null) {
                         dst.write(line + "\n");
                         line = src.readLine();
                     }
                     src.close();
                     dst.close();
                     oddItemBase.log.info(oddItemBase.logPrefix + "Wrote default " + filename);
                 } catch (IOException e) {
                     oddItemBase.log.warning(oddItemBase.logPrefix + "Error writing default " + filename);
                 } finally {
                     try {
                         src.close();
                         dst.close();
                     } catch (IOException e) {}
                 }
             }
         }
     }
 
     private int getVersion() {
         return version;
     }
 }
