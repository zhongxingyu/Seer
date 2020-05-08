 /*
  * Copyright (C) 2013 mewin<mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.mewin.killRewards.rewards;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class ItemReward extends Reward
 {
     private ArrayList<ItemStack> stacks;
     public ItemReward(int kills, ArrayList<HashMap> yaml, String pName)
     {
         super(kills, pName);
         stacks = new ArrayList<ItemStack>();
         for (HashMap map : yaml)
         {
             ItemStack stack;
             Object mat = map.get("type");
             if (mat instanceof String)
             {
                 stack = new ItemStack(Material.matchMaterial((String) mat));
             }
             else
             {
                 stack = new ItemStack((Integer) mat);
             }
             
             if (map.containsKey("data"))
             {
                 stack.setDurability(((Integer) map.get("data")).shortValue());
             }
             
             if (map.containsKey("amount"))
             {
                 stack.setAmount((Integer) map.get("amount"));
             }
             
             if (map.containsKey("name"))
             {
                 ItemMeta meta = stack.getItemMeta();
                 meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) map.get("name")));
                 stack.setItemMeta(meta);
             }
             
             if (map.containsKey("enchantments"))
             {
                 ArrayList<HashMap> list = (ArrayList) map.get("enchantments");
                 ItemMeta meta = stack.getItemMeta();
                 for (HashMap eMap : list)
                 {
                     Enchantment ench;
                     int level = -1;
                     Object name = eMap.get("type");
                     if (name instanceof String)
                     {
                         ench = Enchantment.getByName((String) eMap.get("type"));
                     }
                     else
                     {
                         ench = Enchantment.getById((Integer) eMap.get("type"));
                     }
                     
                     if (eMap.containsKey("level"))
                     {
                         level = (Integer) eMap.get("level");
                     }
                     
                     meta.addEnchant(ench, level > -1 ? level : ench.getStartLevel(), true);
                 }
                 stack.setItemMeta(meta);
             }
             
             if (map.containsKey("lore"))
             {
                 ItemMeta meta = stack.getItemMeta();
                meta.setLore((ArrayList) map.get("lore"));
                 stack.setItemMeta(meta);
             }
             
             stacks.add(stack);
         }
     }
     
     @Override
     public void give(Player player)
     {
         for (ItemStack stack : stacks)
         {
             player.getInventory().addItem(stack);
         }
     }
 }
