 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.Iterator;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroesEventListener;
 import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillWolf extends ActiveSkill {
 
     public boolean skillTaming = true;
 
     public SkillWolf(Heroes plugin) {
         super(plugin, "Wolf");
         setDescription("Summons and tames a wolf to your side");
         setUsage("/skill wolf <release|summon>");
         setArgumentRange(0, 1);
         setIdentifiers(new String[] { "skill wolf" });
 
         SkillEntityListener seListener = new SkillEntityListener(this);
         SkillPlayerListener spListener = new SkillPlayerListener(this);
 
         registerEvent(Type.ENTITY_TAME, seListener, Priority.Highest);
         registerEvent(Type.ENTITY_DEATH, seListener, Priority.Monitor);
         registerEvent(Type.PLAYER_JOIN, spListener, Priority.Monitor);
        registerEvent(Type.PLAYER_QUIT, spListener, Priority.Lowest);
         registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Highest);
     }
 
     @Override
     public void init() {
         super.init();
         skillTaming = getSetting(null, "tame-requires-skill", true);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("max-wolves", 3);
         node.setProperty(Setting.HEALTH.node(), 30);
         node.setProperty("health-per-level", .25);
         node.setProperty(Setting.DAMAGE.node(), 3);
         node.setProperty("damage-per-level", .1);
         node.setProperty("tame-requires-skill", true);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
 
         if (args.length == 0) {
             int maxWolves = getSetting(hero.getHeroClass(), "max-wolves", 3);
             if (hero.getSummons().size() >= maxWolves) {
                 Messaging.send(player, "You already have the maximum number of wolves");
 
                 return false;
             }
             Wolf wolf = (Wolf) player.getWorld().spawnCreature(player.getLocation(), CreatureType.WOLF);
             setWolfSettings(hero, wolf);
             updateWolves(hero);
             broadcastExecuteText(hero);
             return true;
         } else if (args[0].equals("summon")) {
             boolean summoned = false;
             for (Creature creature : hero.getSummons()) {
                 if (creature instanceof Wolf) {
                     summoned = true;
                     creature.teleport(player);
                 }
             }
             if (!summoned) {
                 Messaging.send(player, "You have no wolves to summon.");
             } else {
                 broadcast(player.getLocation(), "$1 has summoned wolves to their side!", player.getDisplayName());
                 return true;
             }
         } else if (args[0].equals("release")) {
             Iterator<Creature> iter = hero.getSummons().iterator();
             while (iter.hasNext()) {
                 Creature creature = iter.next();
                 if (creature instanceof Wolf) {
                     iter.remove();
                     creature.remove();
                 }
             }
             
             hero.setSkillSetting(this, "wolves", 0);
             broadcast(player.getLocation(), "$1 has released their wolves into the wild", player.getDisplayName());
         }
         
         return false;
     }
 
     private void updateWolves(Hero hero) {
         int wolves = 0;
         for (Creature creature : hero.getSummons()) {
             if (creature instanceof Wolf)
                 wolves++;
         }
         hero.setSkillSetting(this, "wolves", wolves);
     }
 
     private void setWolfSettings(Hero hero, Wolf wolf) {
         Player player = hero.getPlayer();
         int health = getSetting(hero.getHeroClass(), Setting.HEALTH.node(), 30);
         health = (int) (health + (getSetting(hero.getHeroClass(), "health-per-level", .25) * hero.getLevel()));
         wolf.setOwner(player);
         wolf.setTamed(true);
         wolf.setHealth(health);
         hero.getSummons().add(wolf);
     }
 
     public class SkillPlayerListener extends PlayerListener {
 
         private final Skill skill;
 
         public SkillPlayerListener(Skill skill) {
             this.skill = skill;
         }
 
         @Override
         public void onPlayerJoin(PlayerJoinEvent event) {
             Hero hero = getPlugin().getHeroManager().getHero(event.getPlayer());
             
             if (!hero.hasSkill("Wolf"))
                 return;
 
             if (hero.getSkillSettings(skill).containsKey("wolves")) {
                 int wolves = Integer.parseInt(hero.getSkillSettings(skill).get("wolves"));
                 for (int i = 0; i < wolves; i++) {
                     Wolf wolf = (Wolf) event.getPlayer().getWorld().spawnCreature(event.getPlayer().getLocation(), CreatureType.WOLF);
                     setWolfSettings(hero, wolf);
                 }
             }
         }
 
         @Override
         public void onPlayerQuit(PlayerQuitEvent event) {
             Hero hero = getPlugin().getHeroManager().getHero(event.getPlayer());
             if (!hero.hasSkill("Wolf"))
                 return;
 
             Iterator<Creature> iter = hero.getSummons().iterator();
             while (iter.hasNext()) {
                 Creature creature = iter.next();
                 if (creature instanceof Wolf) {
                     creature.remove();
                     iter.remove();
                 }
             }
         }
     }
 
     public class SkillEntityListener extends EntityListener {
 
         private final SkillWolf skill;
 
         SkillEntityListener(SkillWolf skill) {
             this.skill = skill;
         }
 
         @Override
         public void onEntityTame(EntityTameEvent event) {
             if (event.isCancelled() || !(event.getEntity() instanceof Wolf) || !(event.getOwner() instanceof Player))
                 return;
             
             Player player = (Player) event.getOwner();
             Hero hero = getPlugin().getHeroManager().getHero((Player) event.getOwner());
             if (skill.skillTaming && !hero.hasSkill(skill.getName())) {
                 event.setCancelled(true);
             } else {
                 Wolf wolf = (Wolf) player.getWorld().spawnCreature(player.getLocation(), CreatureType.WOLF);
                 skill.setWolfSettings(hero, wolf);
                 Messaging.send(player, "You have tamed a wolf!");
             }
         }
 
         @Override
         public void onEntityDeath(EntityDeathEvent event) {
             if (!(event.getEntity() instanceof Wolf))
                 return;
 
             Wolf wolf = (Wolf) event.getEntity();
             if (!wolf.isTamed() || wolf.getOwner() == null || !(wolf.getOwner() instanceof Player))
                 return;
 
             Hero hero = getPlugin().getHeroManager().getHero((Player) wolf.getOwner());
             if (!hero.getSummons().contains(wolf))
                 return;
 
             hero.getSummons().remove(wolf);
             int wolves = Integer.parseInt(hero.getSkillSettings(skill).get("wolves"));
             hero.setSkillSetting(skill, "wolves", wolves - 1);
         }
 
     }
 
     public class SkillHeroListener extends HeroesEventListener {
 
         @Override
         public void onWeaponDamage(WeaponDamageEvent event) {
             if (!(event.getDamager() instanceof Wolf) || event.isCancelled())
                 return;
 
             Wolf wolf = (Wolf) event.getDamager();
             if (!wolf.isTamed() || wolf.getOwner() == null || !(wolf.getOwner() instanceof Player))
                 return;
 
             Hero hero = getPlugin().getHeroManager().getHero((Player) wolf.getOwner());
             if (!hero.getSummons().contains(wolf))
                 return;
 
             Skill skill = getPlugin().getSkillMap().get("Wolf");
             double damagePerLevel = skill.getSetting(hero.getHeroClass(), "damage-per-level", .1);
             int damage = skill.getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 3) + (int) (hero.getLevel() * damagePerLevel);
             event.setDamage(damage);
         }
     }
 }
