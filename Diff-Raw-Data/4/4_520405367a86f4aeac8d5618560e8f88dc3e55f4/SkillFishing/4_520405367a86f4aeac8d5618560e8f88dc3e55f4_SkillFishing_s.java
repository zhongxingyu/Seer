 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.craftbukkit.entity.CraftItem;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerFishEvent.State;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 
 public class SkillFishing extends PassiveSkill {
     
     public SkillFishing(Heroes plugin) {
         super(plugin, "Fishing");
         setDescription("Double Drops for fishing!");
         setEffectTypes(EffectType.BENEFICIAL);
         setTypes(SkillType.KNOWLEDGE, SkillType.EARTH, SkillType.BUFF);
         registerEvent(Type.PLAYER_FISH, new SkillPlayerListener(this), Priority.Monitor);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set("chance-per-level", .001);
         node.set("special-item-level", 5);
         node.set("enable-special-item", false);
         return node;
     }
 
     public class SkillPlayerListener extends PlayerListener {
         
         private Skill skill;
 
         SkillPlayerListener(Skill skill) {
             this.skill = skill;
         }
         
         @Override
         public void onPlayerFish(PlayerFishEvent event){
             Heroes.debug.startTask("HeroesSkillListener");
             if (event.isCancelled() || event.getState() != State.CAUGHT_FISH || !(event.getCaught() instanceof CraftItem)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
                 CraftItem getCaught = (CraftItem) event.getCaught();
                 double chance = Util.rand.nextDouble();
                 Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
                 Player player = hero.getPlayer();
                 if (chance < SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL, .001, false) * hero.getSkillLevel(skill)){ //if the chance
                     
                        int special-itemlvl = SkillConfigManager.getUseSetting(hero, skill, "special-item-level", 5, true);
                        if (hero.getLevel() >= special-itemlvl && SkillConfigManager.getUseSetting(hero, skill, "enable-special-item", false)){ //if fishing leather is enabled and have the level
                             
                             if (getCaught != null){ //If not null
                                 switch(Util.rand.nextInt(6)){
                                     case 0: 
                                         getCaught.setItemStack(new ItemStack(Material.LEATHER_BOOTS, 1));
                                         Messaging.send(player, "You found leather boots!");
                                         getCaught.getItemStack().setDurability((short) (Math.random() * 40));
                                         break;
                                     case 1: 
                                         getCaught.setItemStack(new ItemStack(Material.LEATHER_LEGGINGS, 1));
                                         Messaging.send(player, "You found leather leggings!");
                                         getCaught.getItemStack().setDurability((short) (Math.random() * 46));
                                         break;
                                     case 2: 
                                         getCaught.setItemStack(new ItemStack(Material.LEATHER_HELMET, 1));
                                         Messaging.send(player, "You found a leather helmet!");
                                         getCaught.getItemStack().setDurability((short) (Math.random() * 34));
                                         break;
                                     case 3: 
                                         getCaught.setItemStack(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
                                         Messaging.send(player, "You found a leather chestplate!");
                                         getCaught.getItemStack().setDurability((short) (Math.random() * 49));
                                         break;
                                     case 4: 
                                         getCaught.setItemStack(new ItemStack(Material.GOLDEN_APPLE, 1));
                                         Messaging.send(player, "You found a golden apple, woo!");
                                         getCaught.getItemStack().setDurability((short) (Math.random() * 10));
                                         break;
                                     case 5: 
                                         getCaught.setItemStack(new ItemStack(Material.APPLE, 1));
                                         Messaging.send(player, "You found an apple!");
                                         getCaught.getItemStack().setDurability((short) (Math.random() * 29));
                                         break;
                                     case 6: 
                                         getCaught.setItemStack(new ItemStack(Material.RAW_FISH, 2));
                                         Messaging.send(player, "You found 2 Fishes!");
                                         break;
                                     case 7: 
                                         getCaught.setItemStack(new ItemStack(Material.RAW_FISH, 1));
                                         Messaging.send(player, "You found 1 Fish!");
                                         break;
                                 }
                             }
                         } else {
                                 switch(Util.rand.nextInt(2)){
                                     case 0: 
                                         getCaught.setItemStack(new ItemStack(Material.RAW_FISH, 2));
                                         Messaging.send(player, "You found 2 Fishes!");
                                         break;
                                     case 1: 
                                         getCaught.setItemStack(new ItemStack(Material.RAW_FISH, 1));
                                         Messaging.send(player, "You found 1 Fish!");
                                         break;
                                 }
                             }   
                         }           
                 Heroes.debug.stopTask("HeroesSkillListener");
             }
         }
     }
