 package com.archmageinc.RandomEncounters;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 /**
  * Represents a Treasure configuration, an Item to be generated.
  * 
  * @author ArchmageInc
  */
 public class Treasure {
     
     /**
      * The material of the item.
      */
     protected Material material     =   Material.AIR;
     
     /**
      * The minimum number of items to generate.
      */
     protected Long min;
     
     /**
      * The maximum number of items to generate.
      */
     protected Long max;
     
     /**
      * The probability of additional items.
      */
     protected Double probability    =   0.0;
     
     /**
      * The set of TreasureEnchantments to place on the item.
      */
     protected Set<TreasureEnchantment> enchantments =   new HashSet();
     
     protected String tagName;
     
     
     /**
      * Constructor based on the JSON configuration.
      * 
      * @param jsonConfiguration 
      */
     public Treasure(JSONObject jsonConfiguration){
         try{
             material    =   Material.getMaterial((String) jsonConfiguration.get("material"));
             min         =   ((Number) jsonConfiguration.get("min")).longValue();
             max         =   ((Number) jsonConfiguration.get("max")).longValue();
             probability =   ((Number) jsonConfiguration.get("probability")).doubleValue();
             tagName     =   (String) jsonConfiguration.get("tagName");
             JSONArray jsonEnchantments  =   (JSONArray) jsonConfiguration.get("enchantments");
             if(jsonEnchantments!=null){
                 for(int i=0;i<jsonEnchantments.size();i++){
                     enchantments.add(new TreasureEnchantment((JSONObject) jsonEnchantments.get(i)));
                 }
             }
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid Treasure configuration: "+e.getMessage());
         }
         
     }
     
     /**
      * Set the Enchantments on the given item.
      * 
      * @param item The item to receive the enchantments
      */
     protected void setEnchantments(ItemStack item){
         if(enchantments.size()>0){
             for(TreasureEnchantment tEnchantment : enchantments){
                 Enchantment enchantment =   tEnchantment.get();
                 if(enchantment!=null){
                     try{
                         item.addUnsafeEnchantment(enchantment, tEnchantment.getLevel());
                     }catch(IllegalArgumentException e){
                         RandomEncounters.getInstance().logWarning("Invalid enchantment for Treasure item: "+e.getMessage());
                     }
                 }
             }
         }
     }
     
     /**
      * Get the list of items based on this configuration.
      * 
      * @return 
      */
     public List<ItemStack> get(){
         List<ItemStack> list    =   new ArrayList();
         ItemStack stack         =   new ItemStack(material,0);
         if(tagName!=null){
             stack.getItemMeta().setDisplayName(tagName);
         }
         for(int i=min.intValue();i<max;i++){
             if(Math.random()<probability){
                 if(stack.getAmount()<stack.getMaxStackSize()){
                     stack.setAmount(stack.getAmount()+1);
                 }else{
                     list.add(stack.clone());
                     stack   =   new ItemStack(material,1);
                     if(tagName!=null){
                        stack.getItemMeta().setDisplayName(tagName);
                     }
                 }
                 setEnchantments(stack);
             }
         }
         if(stack.getAmount()>0){
             list.add(stack);
         }
         return list;
     }
     
     /**
      * Get one item from the treasure.
      * 
      * @return Returns the item based on probability or null
      */
     public ItemStack getOne(){
         ItemStack item  =   null;
         if(Math.random()<probability){
             item    =   new ItemStack(material,1);
             setEnchantments(item);
         }
         return item;
     }
 }
