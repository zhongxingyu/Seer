 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of AdminStuff.
  * 
  * AdminStuff is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * AdminStuff is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with AdminStuff.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.AdminStuff.manager;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class KitManager {
 
     private Map<String, List<ItemStack>> kits = new HashMap<String, List<ItemStack>>();
 
     public KitManager(File dataFolder) {
         loadKits(dataFolder);
     }
 
     private void loadKits(File dataFolder) {
         File kitFile = new File(dataFolder, "kits.txt");
         if (kitFile.exists()) {
             try {
                 BufferedReader bReader = new BufferedReader(new FileReader(kitFile));
                 String line = "";
 
                 // value and key for the map
                 List<ItemStack> kit = null;
                 String name = null;
 
                 // variables for parsing the strings
                 Pattern p1 = Pattern.compile(":");
                 Pattern p2 = Pattern.compile("x");
                 String[] split = null;
                 int id = 0;
                 short subId = 0;
                 int amount = 1;
                 while ((line = bReader.readLine()) != null) {
                     // new kit
                     if (line.startsWith("#")) {
                         // first kit isn't filled with items
                         if (kit != null)
                             kits.put(name, kit);
                         name = line.substring(1);
                         kit = new LinkedList<ItemStack>();
                     }
                     // add item to kit
                     else {
                         // split amount
                         split = p2.split(line);
                         // no amount given - standard is one
                         if (split.length == 1)
                             amount = 1;
                         else {
                             amount = Integer.valueOf(split[1]);
                             line = split[0];
                         }
 
                         // split sub id
                         split = p1.split(line);
                         // no sub id
                         if (split.length == 1)
                             subId = 0;
                         else
                             subId = Short.valueOf(split[1]);
                         id = Integer.valueOf(split[0]);
 
                         kit.add(new ItemStack(id, amount, subId));
                     }
                 }
                // add last kit to list
                if (name != null && kit != null && !kit.isEmpty())
                    kits.put(name, kit);

                ConsoleUtils.printInfo(Core.NAME, kits.size() + " kits loaded");
             } catch (Exception e) {
                 ConsoleUtils.printException(e, Core.NAME, "Error while reading " + kitFile + " !");
             }
         } else
             ConsoleUtils.printWarning(Core.NAME, kitFile + " doesn't exist! No kits loaded!");
     }
     public List<ItemStack> getKit(String name) {
         return kits.get(name);
     }
 
     public Iterable<String> getNames() {
         return kits.keySet();
     }
 }
