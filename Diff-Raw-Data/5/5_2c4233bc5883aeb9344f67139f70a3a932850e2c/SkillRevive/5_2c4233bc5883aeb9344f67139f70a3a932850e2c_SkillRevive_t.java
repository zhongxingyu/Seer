 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillRevive extends ActiveSkill {
 
     public Map<String, Location> deaths = new LinkedHashMap<String, Location>();
 
     @SuppressWarnings("serial")
     public SkillRevive(Heroes plugin) {
         super(plugin, "Revive");
         setDescription("Teleports the target to their place of death");
         setUsage("/skill revive [target]");
         setArgumentRange(1, 1);
         setIdentifiers(new String[]{"skill revive"});
 
         registerEvent(Type.ENTITY_DEATH, new SkillPlayerListener(), Priority.Normal);
 
         deaths = new LinkedHashMap<String, Location>() {
 
             private static final int MAX_ENTRIES = 50;
 
             @Override
             protected boolean removeEldestEntry(Map.Entry<String, Location> eldest) {
                 return size() > MAX_ENTRIES;
             }
         };
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("slime-amount", 1);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         Player target = getPlugin().getServer().getPlayer(args[0]);
 
         if (target == null) {
             player.sendMessage("Player not found.");
             return false;
         }
 
         Player targetPlayer = target;
         String targetName = targetPlayer.getName();
         if (!deaths.containsKey(targetName)) {
             Messaging.send(player, "$1 has not died recently.", targetName);
             return false;
         }
 
         Location deathLoc = deaths.get(targetName);
         Location playerLoc = player.getLocation();
         if (!playerLoc.getWorld().equals(deathLoc.getWorld()) || playerLoc.distance(deathLoc) > 50.0) {
             Messaging.send(player, "You are out of range.");
             return false;
         }
 
         if (targetPlayer.isDead()) {
             Messaging.send(player, "$1 is still dead.", targetName);
             return false;
         }
 
         int slimeballs = getSetting(hero.getHeroClass(), "slime-amount", 1);
         if (!player.getInventory().contains(Material.SLIME_BALL, slimeballs)) {
             Messaging.send(player, "You don't have enough slimeballs (" + slimeballs + ")");
             return false;
         }
        
        Hero targetHero = getPlugin().getHeroManager().getHero(targetPlayer);
        if (!hero.hasParty() || !hero.getParty().isPartyMember(targetHero)) {
            Messaging.send(player, "The person needs to be in your party to do that!"); 
        }
 
         targetPlayer.teleport(playerLoc);
 
         player.getInventory().remove(new ItemStack(Material.SLIME_BALL, slimeballs));
         broadcastExecuteText(hero);
         return true;
     }
 
     public class SkillPlayerListener extends EntityListener {
 
         @Override
         public void onEntityDeath(EntityDeathEvent event) {
             if (event.getEntity() instanceof Player) {
                 Player player = (Player) event.getEntity();
                 Location playerLoc = player.getLocation();
                 deaths.put(player.getName(), playerLoc);
             }
         }
 
     }
 }
