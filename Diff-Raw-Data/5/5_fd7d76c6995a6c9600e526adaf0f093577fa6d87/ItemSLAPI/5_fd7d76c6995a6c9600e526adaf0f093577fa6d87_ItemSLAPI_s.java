 
 package net.arcanerealm.arcanelib;
 
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Kenny
  */
 public class ItemSLAPI 
 {
     /**
      * Saves an ItemStack to a String in a format that can be loaded
      * @param item ItemStack object
      * @return Save string for the ItemStack
      */
     public static String itemStackToSaveString(ItemStack item)
     {
         String output = "";
         output += item.getType().name();
         output += " ";
         output += item.getAmount();
         
         if (item.getData().getData() != 0)
         {
             output += " ";
             output += item.getData().getData();
         }
         
         
         if(!item.getEnchantments().isEmpty())
         {
             output += " ";
             String enchantment = "";
             int i = 1;
             for(Enchantment e : item.getEnchantments().keySet())
             {
                 enchantment += e.getName();
                 enchantment += ":";
                 enchantment += item.getEnchantmentLevel(e);
                 if(i != item.getEnchantments().keySet().size())
                     enchantment += ",";
                 i++;
             }
             output += enchantment;
         }
         
         return output;
     }
     
     /**
      * Gets an ItemStack from its save string
      * @param saveString ItemStack save string
      * @return ItemStack object
      */
     public static ItemStack getItemStackFromSave(String saveString) throws Exception
     {
         String[] split = saveString.split(" ");
         
         if(split[0].equalsIgnoreCase("0")) return null;
         
         Material mat = Material.matchMaterial(split[0]);
         int amount = Integer.parseInt(split[1]);
         byte data;
         
         ItemStack result;
         
         try
         {
             result = new ItemStack(mat, amount);
         }catch(Exception e)
         {
             Bukkit.getLogger().log(Level.WARNING, "Could not load ItemStack from save string: "+saveString);
             throw new Exception();
         }
         
         if(split.length == 3)
         {
             try
             {
                 data = Byte.parseByte(split[2]);
                 result = new ItemStack(mat, amount, mat.getMaxDurability(), data);
             }catch(Exception e){}
             
             try
             {
                 result = addEnchantmentFromSave(result, split[2]);
             }catch(Exception e){}
             return result;
         }
         if(split.length == 4)
         {
             try
             {
                 data = Byte.parseByte(split[2]);
                 result = new ItemStack(mat, amount, mat.getMaxDurability(), data);
                 result = addEnchantmentFromSave(result, split[3]);
             }catch(Exception e) {}
         }
         return result;
     }
     
     /**
      * Adds an enchantment to an item stack
      * @param item The item that it should add the enchantment to
      * @param enchantSave The save string from the enchantment
      * @return Bukkit ItemStack with an enchantment
      */
     public static ItemStack addEnchantmentFromSave(ItemStack item, String enchantSave)
     {
         ItemStack result = item;
         String[] splitEnchant = enchantSave.split(",");
         int i = 0;
         for(String s : splitEnchant)
         {
             String[] splitFinal = splitEnchant[i].split(":");
             String enchantName = splitFinal[0];
             int level = Integer.parseInt(splitFinal[1]);
             result.addEnchantment(Enchantment.getByName(enchantName), level);
             i++;
         }
         return result;
     }
 }
