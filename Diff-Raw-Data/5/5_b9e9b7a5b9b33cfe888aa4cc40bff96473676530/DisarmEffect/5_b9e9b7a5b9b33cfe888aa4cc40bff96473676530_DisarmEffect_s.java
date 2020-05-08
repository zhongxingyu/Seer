 package com.herocraftonline.dev.heroes.effects.common;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class DisarmEffect extends ExpirableEffect {
 
     private final String applyText;
     private final String expireText;
     private HashMap<Hero, ItemStack[]> disarms = new HashMap<Hero, ItemStack[]>();
     
     public DisarmEffect(Skill skill, long duration, String applyText, String expireText) {
         super(skill, "Disarm", duration);
         this.types.add(EffectType.HARMFUL);
         this.types.add(EffectType.DISARM);
         this.applyText = applyText;
         this.expireText = expireText;
     }
 
     @Override
     public void apply(Hero hero) {
         super.apply(hero);
         Player player = hero.getPlayer();
         ItemStack[] inv = player.getInventory().getContents();
         for (int i = 0; i < 9; i++) {
             ItemStack is = inv[i];
             if (is != null && Util.isWeapon(is.getType())) {
                 if (!disarms.containsKey(hero)) {
                    ItemStack[] items = disarms.put(hero, new ItemStack[9]);
                    items[i] = is;
                     player.getInventory().clear(i);
                 } else {
                     ItemStack[] items = disarms.get(hero);
                     items[i] = is;
                     player.getInventory().clear(i);
                 }
             }
         }
         Util.syncInventory(player, plugin);
         broadcast(player.getLocation(), applyText, player.getDisplayName());
     }
 
     @Override
     public void remove(Hero hero) {
         super.remove(hero);
         Player player = hero.getPlayer();
         
         if (disarms.containsKey(hero)) {
             PlayerInventory inv = player.getInventory();
             ItemStack[] contents = inv.getContents();
             ItemStack[] oldInv = disarms.get(hero);
             for (int i = 0; i < 9; i++) {
                 if (oldInv[i] != null) {
                     if (contents[i] != null) {
                         Util.moveItem(hero, i, contents[i]);
                     }
                     inv.setItem(i, oldInv[i]);
                 }
             }
             disarms.remove(hero);
             Util.syncInventory(player, plugin);
         }
         broadcast(player.getLocation(), expireText, player.getDisplayName());
     }
 }
