 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillWeb extends TargettedSkill {
 
     private String applyText;
     private static Map<Hero, Map<Location, Material>> changedBlocks = new HashMap<Hero, Map<Location, Material>>();
     
     public SkillWeb(Heroes plugin) {
         super(plugin, "Web");
         setDescription("Catches your target in a web");
         setUsage("/skill web [target]");
         setArgumentRange(0, 1);
         setIdentifiers(new String[]{"skill web"});
         
         registerEvent(Type.BLOCK_BREAK, new WebBlockListener(), Priority.Highest);
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("range", 10);
         node.setProperty("duration", 5000); //in milliseconds
         node.setProperty("apply-text", "%hero% conjured a web at %target%'s feet!");
         return node;
     }
     
     @Override
     public void init() {
         super.init();
        applyText = getSetting(null, "apply-text", "%hero% conjured a web at %target%'s feet!");
     }
     
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         if (target.equals(player)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
         String name = "";
         
         if (target instanceof Player) {
             name = ((Player) target).getDisplayName();
         } else if (target instanceof Creature) {
             name = Messaging.getCreatureName((Creature) target).toLowerCase();
         }
         broadcast(player.getLocation(), applyText, new Object[] {player.getDisplayName(), name});
         int duration = getSetting(hero.getHeroClass(), "duration", 5000);
         WebEffect wEffect = new WebEffect(this, duration, target.getLocation().clone());
         hero.addEffect(wEffect);
         return true;
     }
 
     public class WebEffect extends ExpirableEffect {
 
         private Location loc;
         
         public WebEffect(Skill skill, long duration, Location location) {
             super(skill, "Web", duration);
             this.loc = location;
         }
         
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             changeBlock(loc, hero);
             for (BlockFace face : BlockFace.values()) {
                 if (face.toString().contains("_") || face == BlockFace.UP || face == BlockFace.DOWN) continue;
                 changeBlock(loc.getBlock().getRelative(face).getLocation(), hero);
             }
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             for(Entry<Location, Material> entry : changedBlocks.get(hero).entrySet()) {
                 entry.getKey().getBlock().setType(entry.getValue());
             }
             //CleanUp
             changedBlocks.get(hero).clear();
             changedBlocks.remove(hero);
         }
         
         public Location getLocation() {
             return this.loc;
         }
         
         private void changeBlock(Location location, Hero hero) {
             Map<Location, Material> heroChangedBlocks = changedBlocks.get(hero);
             if (heroChangedBlocks == null) {
                 changedBlocks.put(hero, new HashMap<Location, Material>());
             }
             if (location.getBlock().getType() != Material.WEB) {
                 changedBlocks.get(hero).put(location, location.getBlock().getType());
                 location.getBlock().setType(Material.WEB);
             }
         }
     }
     
     public class WebBlockListener extends BlockListener {
         
         @Override
         public void onBlockBreak(BlockBreakEvent event) {
             if (event.isCancelled()) return;
             
             //Check out mappings to see if this block was a changed block, if so lets deny breaking it.
             for(Map<Location, Material> blockMap : changedBlocks.values()) 
                 for (Location loc : blockMap.keySet()) 
                     if (event.getBlock().getLocation().equals(loc)) 
                         event.setCancelled(true);
         }
     }
 }
