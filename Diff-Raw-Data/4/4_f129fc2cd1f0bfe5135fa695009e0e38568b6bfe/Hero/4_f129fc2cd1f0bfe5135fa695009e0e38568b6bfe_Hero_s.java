 package com.herocraftonline.dev.heroes.persistence;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ExperienceGainEvent;
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class Hero {
 
     private static final DecimalFormat decFormat = new DecimalFormat("#0.##");
 
     protected final Heroes plugin;
     protected Player player;
     protected HeroClass heroClass;
     protected int mana = 0;
     protected HeroParty party = null;
     protected boolean verbose = true;
     protected Set<Effect> effects = new HashSet<Effect>();
     protected Map<String, Double> experience = new HashMap<String, Double>();
     protected Map<String, Long> cooldowns = new HashMap<String, Long>();
     protected Map<Entity, CreatureType> summons = new HashMap<Entity, CreatureType>();
     protected Map<Material, String[]> binds = new HashMap<Material, String[]>();
     protected List<ItemStack> itemRecovery = new ArrayList<ItemStack>();
     protected Set<String> suppressedSkills = new HashSet<String>();
     protected double health;
 
     public Hero(Heroes plugin, Player player, HeroClass heroClass) {
         this.plugin = plugin;
         this.player = player;
         this.heroClass = heroClass;
     }
 
     public void syncHealth() {
         getPlayer().setHealth((int) (health / getMaxHealth() * 20));
     }
 
     public void addEffect(Effect effect) {
         effects.add(effect);
         effect.apply(this);
     }
 
     public void addRecoveryItem(ItemStack item) {
         this.itemRecovery.add(item);
     }
 
     public void bind(Material material, String[] skillName) {
         binds.put(material, skillName);
     }
 
     public void changeHeroClass(HeroClass heroClass) {
         setHeroClass(heroClass);
         binds.clear();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Hero other = (Hero) obj;
         if (player == null) {
             if (other.player != null)
                 return false;
         } else if (!player.getName().equals(other.player.getName()))
             return false;
         return true;
     }
 
     public void gainExp(double expGain, ExperienceType source) {
         gainExp(expGain, source, true);
     }
 
     public void gainExp(double expGain, ExperienceType source, boolean distributeToParty) {
         Properties prop = plugin.getConfigManager().getProperties();
 
         if (distributeToParty && party != null && party.getExp()) {
             Location location = getPlayer().getLocation();
 
             Set<Hero> partyMembers = party.getMembers();
             Set<Hero> inRangeMembers = new HashSet<Hero>();
             for (Hero partyMember : partyMembers) {
                 if (location.distance(partyMember.getPlayer().getLocation()) <= 50) {
                     inRangeMembers.add(partyMember);
                 }
             }
 
             int partySize = inRangeMembers.size();
             double sharedExpGain = expGain / partySize * ((partySize - 1) * prop.partyBonus + 1.0);
 
             for (Hero partyMember : inRangeMembers) {
                 partyMember.gainExp(sharedExpGain, source, false);
             }
 
             return;
         }
 
         double exp = getExperience();
 
         // adjust exp using the class modifier
         expGain *= heroClass.getExpModifier();
 
         int currentLevel = prop.getLevel(exp);
         int newLevel = prop.getLevel(exp + expGain);
         if (currentLevel >= prop.maxLevel) {
             expGain = 0;
         }
 
         // add the experience
         exp += expGain;
 
         // call event
         ExperienceGainEvent expEvent;
         if (newLevel == currentLevel) {
             expEvent = new ExperienceGainEvent(this, expGain, source);
         } else {
             expEvent = new LevelEvent(this, expGain, currentLevel, newLevel, source);
         }
         plugin.getServer().getPluginManager().callEvent(expEvent);
         if (expEvent.isCancelled()) {
             // undo the experience gain
             exp -= expGain;
             return;
         }
 
         // undo the previous gain to make sure we use the updated value
         exp -= expGain;
         expGain = expEvent.getExpGain();
 
         // add the updated experience
         exp += expGain;
 
         // notify the user
         if (expGain != 0) {
             if (verbose) {
                 Messaging.send(player, "$1: Gained $2 Exp", heroClass.getName(), decFormat.format(expGain));
             }
             if (newLevel != currentLevel) {
                player.setHealth(20);
                // setHealth(getMaxHealth());
                 Messaging.send(player, "You leveled up! (Lvl $1 $2)", String.valueOf(newLevel), heroClass.getName());
                 if (newLevel >= prop.maxLevel) {
                     exp = prop.getExperience(prop.maxLevel);
                     Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), heroClass.getName());
                     plugin.getHeroManager().saveHero(player);
                 }
             }
         }
 
         setExperience(exp);
     }
 
     public Map<Material, String[]> getBinds() {
         return binds;
     }
 
     public Map<String, Long> getCooldowns() {
         return cooldowns;
     }
 
     public Effect getEffect(String name) {
         for (Effect effect : effects) {
             if (effect.getName().equalsIgnoreCase(name))
                 return effect;
         }
         return null;
     }
 
     public Set<Effect> getEffects() {
         return new HashSet<Effect>(effects);
     }
 
     public double getExperience() {
         return getExperience(heroClass);
     }
 
     public double getExperience(HeroClass heroClass) {
         Double exp = experience.get(heroClass.getName());
         return exp == null ? 0 : exp;
     }
 
     public HeroClass getHeroClass() {
         return heroClass;
     }
 
     public int getLevel() {
         return plugin.getConfigManager().getProperties().getLevel(getExperience());
     }
 
     public double getHealth() {
         return health;
     }
 
     public double getMaxHealth() {
         int level = plugin.getConfigManager().getProperties().getLevel(getExperience());
         return heroClass.getBaseMaxHealth() + (level - 1) * heroClass.getMaxHealthPerLevel();
     }
 
     public int getMana() {
         return mana;
     }
 
     public HeroParty getParty() {
         return party;
     }
 
     public Player getPlayer() {
         Player servPlayer = plugin.getServer().getPlayer(player.getName());
         if (servPlayer != null && player != servPlayer) {
             player = servPlayer;
         }
         return player;
     }
 
     public List<ItemStack> getRecoveryItems() {
         return this.itemRecovery;
     }
 
     public Map<Entity, CreatureType> getSummons() {
         return summons;
     }
 
     public Set<String> getSuppressedSkills() {
         return new HashSet<String>(suppressedSkills);
     }
 
     public boolean hasEffect(String name) {
         for (Effect effect : effects) {
             if (effect.getName().equalsIgnoreCase(name))
                 return true;
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return player == null ? 0 : player.getName().hashCode();
     }
 
     public boolean hasParty() {
         return party != null;
     }
 
     public boolean isMaster() {
         return isMaster(heroClass);
     }
 
     public boolean isMaster(HeroClass heroClass) {
         int maxExp = plugin.getConfigManager().getProperties().maxExp;
         return getExperience(heroClass) >= maxExp || getExperience(heroClass) - maxExp > 0;
     }
 
     public boolean isSuppressing(Skill skill) {
         return suppressedSkills.contains(skill.getName());
     }
 
     public boolean isVerbose() {
         return verbose;
     }
 
     public void removeEffect(Effect effect) {
         effects.remove(effect);
         if (effect != null) {
             effect.remove(this);
         }
     }
 
     public void setExperience(double experience) {
         setExperience(heroClass, experience);
     }
 
     public void setExperience(HeroClass heroClass, double experience) {
         this.experience.put(heroClass.getName(), experience);
     }
 
     public void setHeroClass(HeroClass heroClass) {
         double currentMaxHP = getMaxHealth();
         this.heroClass = heroClass;
         double newMaxHP = getMaxHealth();
         health *= newMaxHP / currentMaxHP;
         if (health > newMaxHP) {
             health = newMaxHP;
         }
 
         // Check the Players inventory now that they have changed class.
         this.plugin.getInventoryChecker().checkInventory(getPlayer());
     }
 
     public void setMana(int mana) {
         if (mana > 100) {
             mana = 100;
         } else if (mana < 0) {
             mana = 0;
         }
         this.mana = mana;
     }
 
     public void setParty(HeroParty party) {
         this.party = party;
     }
 
     public void setRecoveryItems(List<ItemStack> items) {
         this.itemRecovery = items;
     }
 
     public void setSuppressed(Skill skill, boolean suppressed) {
         if (suppressed) {
             suppressedSkills.add(skill.getName());
         } else {
             suppressedSkills.remove(skill.getName());
         }
     }
 
     public void setVerbose(boolean verbose) {
         this.verbose = verbose;
     }
 
     public void unbind(Material material) {
         binds.remove(material);
     }
 
     public void setHealth(Double health) {
         double maxHealth = getMaxHealth();
         if (health > maxHealth) {
             this.health = maxHealth;
         } else if (health < 0) {
             this.health = 0;
         } else {
             this.health = health;
         }
     }
 
 }
