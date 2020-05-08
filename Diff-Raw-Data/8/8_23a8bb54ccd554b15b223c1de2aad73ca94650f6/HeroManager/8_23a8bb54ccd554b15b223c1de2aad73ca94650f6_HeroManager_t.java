 package com.herocraftonline.dev.heroes.hero;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroRegainManaEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.Expirable;
 import com.herocraftonline.dev.heroes.effects.ManagedCreatureEffect;
 import com.herocraftonline.dev.heroes.effects.ManagedEffect;
 import com.herocraftonline.dev.heroes.effects.ManagedHeroEffect;
 import com.herocraftonline.dev.heroes.effects.Periodic;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.party.PartyManager;
 import com.herocraftonline.dev.heroes.persistence.HeroStorage;
 import com.herocraftonline.dev.heroes.persistence.YMLHeroStorage;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.ui.MapAPI;
 import com.herocraftonline.dev.heroes.ui.MapInfo;
 import com.herocraftonline.dev.heroes.ui.TextRenderer;
 import com.herocraftonline.dev.heroes.ui.TextRenderer.CharacterSprite;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 /**
  * Player management
  * 
  * @author Herocraft's Plugin Team
  */
 public class HeroManager {
 
     private Heroes plugin;
     private Map<String, Hero> heroes;
     protected Map<Creature, Set<Effect>> creatureEffects;
     protected Set<ManagedEffect> managedEffects;
     private HeroStorage heroStorage;
     private final static int effectInterval = 2;
     private final static int manaInterval = 5;
     private final static int partyUpdateInterval = 5;
 
     public HeroManager(Heroes plugin) {
         this.plugin = plugin;
         this.heroes = new HashMap<String, Hero>();
         this.creatureEffects = new HashMap<Creature, Set<Effect>>();
         this.managedEffects = new HashSet<ManagedEffect>();
         // if (plugin.getConfigManager().getProperties().storageType.toLowerCase().equals("yml"))
         heroStorage = new YMLHeroStorage(plugin);
 
         Runnable effectTimer = new EffectUpdater(this);
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, effectTimer, 0, effectInterval);
 
         int regenAmount = plugin.getConfigManager().getProperties().manaRegenPercent;
         long regenInterval = plugin.getConfigManager().getProperties().manaRegenInterval * 1000L;
         Runnable manaTimer = new ManaUpdater(this, regenInterval, regenAmount);
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, manaTimer, 0, manaInterval);
 
         Runnable partyUpdater = new PartyUpdater(this, plugin, plugin.getPartyManager());
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, partyUpdater, 0, partyUpdateInterval);
     }
 
 
     protected void addManagedEffect(Hero hero, Effect effect) {
         managedEffects.add(new ManagedHeroEffect(hero, effect));
     }
     
     protected void addManagedEffect(Creature creature, Effect effect) {
         managedEffects.add(new ManagedCreatureEffect(creature, effect));
     }
 
     protected void removeManagedEffect(Hero hero, Effect effect) {
         managedEffects.remove(new ManagedHeroEffect(hero, effect));
     }
     
     protected void removeManagedEffect(Creature creature, Effect effect) {
         managedEffects.remove(new ManagedCreatureEffect(creature, effect));
     }
 
     /**
      * Adds a new effect to the specific creature
      * 
      * @param creature
      * @param effect
      */
     public void addCreatureEffect(Creature creature, Effect effect) {
         Set<Effect> cEffects = creatureEffects.get(creature);
         if (cEffects == null) {
             cEffects = new HashSet<Effect>();
             creatureEffects.put(creature, cEffects);
         }
         if (effect instanceof Periodic || effect instanceof Expirable) {
             managedEffects.add(new ManagedCreatureEffect(creature, effect));
         }
         cEffects.add(effect);
         effect.apply(creature);
     }
 
     public void addHero(Hero hero) {
         heroes.put(hero.getPlayer().getName().toLowerCase(), hero);
     }
 
     /**
      * Clears all effects from the creature
      * 
      * @param creature
      */
     public void clearCreatureEffects(Creature creature) {
         if (creatureEffects.containsKey(creature)) {
             Iterator<Effect> iter = creatureEffects.get(creature).iterator();
             while (iter.hasNext()) {
                 iter.next().remove(creature);
                 iter.remove();
             }
             creatureEffects.remove(creature);
         }
     }
 
     public boolean containsPlayer(Player player) {
         return getHero(player) != null;
     }
 
     /**
      * Checks if a creature has the effect
      * 
      * @param creature
      * @param effect
      * @return
      */
     public boolean creatureHasEffect(Creature creature, String name) {
         if (!creatureEffects.containsKey(creature))
             return false;
         for (Effect effect : creatureEffects.get(creature)) {
             if (effect.getName().equalsIgnoreCase(name))
                 return true;
         }
         return false;
     }
 
     public Effect getCreatureEffect(Creature creature, String name) {
         if (creatureEffects.get(creature) == null)
             return null;
 
         for (Effect effect : creatureEffects.get(creature)) {
             if (effect.getName().equals(name))
                 return effect;
         }
         return null;
     }
 
     /**
      * Gets a set view of all effects currently applied to the specified creature
      * 
      * @param creature
      * @return
      */
     public Set<Effect> getCreatureEffects(Creature creature) {
         return creatureEffects.get(creature) != null ? new HashSet<Effect>(creatureEffects.get(creature)) : null;
     }
 
     /**
      * Gets a hero Object from the hero mapping, if the hero does not exist then it loads in the Hero object for the
      * player
      * 
      * @param player
      * @return
      */
     public Hero getHero(Player player) {
         String key = player.getName().toLowerCase();
         Hero hero = heroes.get(key);
         if (hero != null) {
             if (hero.getPlayer().getEntityId() != player.getEntityId()) {
                 heroes.remove(key);
             } else {
                 return hero;
             }
         }
 
         // If it gets to this stage then clearly the HeroManager doesn't have it so we create it...
         hero = heroStorage.loadHero(player);
         addHero(hero);
         performSkillChecks(hero);
 
         return hero;
     }
 
     public Collection<Hero> getHeroes() {
         return Collections.unmodifiableCollection(heroes.values());
     }
 
     public void performSkillChecks(Hero hero) {
         HeroClass playerClass = hero.getHeroClass();
 
         for (String skillName : playerClass.getSkillNames()) {
             if (skillName.equals("*") || skillName.equals("ALL"))
                 continue;
             Skill skill = plugin.getSkillManager().getSkill(skillName);
             if (skill == null)
                 continue;
             if (skill instanceof OutsourcedSkill)
                 ((OutsourcedSkill) skill).tryLearningSkill(hero);
 
             if (skill instanceof PassiveSkill)
                 ((PassiveSkill) skill).tryApplying(hero);
         }
     }
 
     /**
      * Removes an effect from a creature 
      * 
      * @param creature
      * @param effect
      */
     public void removeCreatureEffect(Creature creature, Effect effect) {
         Set<Effect> cEffects = creatureEffects.get(creature);
         if (cEffects != null) {
             effect.remove(creature);
             cEffects.remove(effect);
             // If the creature has no effects left
             if (cEffects.isEmpty()) {
                 creatureEffects.remove(creature);
             }
             managedEffects.remove(new ManagedCreatureEffect(creature, effect));
         }
     }
     
     void safeRemoveCreatureEffect(Creature creature, Effect effect) {
         Set<Effect> cEffects = creatureEffects.get(creature);
         if (cEffects != null) {
             effect.remove(creature);
             cEffects.remove(effect);
             // If the creature has no effects left
             if (cEffects.isEmpty()) {
                 creatureEffects.remove(creature);
             }
         }
     }
 
     public void removeHero(Hero hero) {
         if (hero != null && hero.hasParty()) {
             HeroParty party = hero.getParty();
             party.removeMember(hero);
             if (party.getMembers().size() == 0) {
                 this.plugin.getPartyManager().removeParty(party);
             }
         }
 
         heroes.remove(hero.getPlayer().getName());
     }
 
     /**
      * Save the given Players Data to a file.
      * 
      * @param player
      */
     public void saveHero(Hero hero) {
         if (heroStorage.saveHero(hero)) {
             Heroes.log(Level.INFO, "Saved hero: " + hero.getPlayer().getName());
         }
     }
 
     public void saveHero(Player player) {
         saveHero(getHero(player));
     }
 
     public void stopTimers() {
         plugin.getServer().getScheduler().cancelTasks(plugin);
     }
 
 }
 
 class EffectUpdater implements Runnable {
 
     private final HeroManager heroManager;
 
     EffectUpdater(HeroManager heroManager) {
         this.heroManager = heroManager;
     }
 
     @Override
     public void run() {
         Iterator<ManagedEffect> iter = heroManager.managedEffects.iterator();
         while (iter.hasNext()) {
             ManagedEffect mEffect = iter.next();
             Effect effect = mEffect.getEffect();
             if (effect instanceof Expirable) {
                 if (((Expirable) effect).isExpired()) {
                     if (mEffect instanceof ManagedHeroEffect) {
                         Hero hero = ((ManagedHeroEffect) mEffect).hero;
                         hero.safeRemoveEffect(effect);
                         iter.remove();
                     } else {
                         heroManager.safeRemoveCreatureEffect(((ManagedCreatureEffect) mEffect).creature, effect);
                         iter.remove();
                     }
                 }
             } else if (effect instanceof Periodic) {
                 Periodic periodic = (Periodic) effect;
                 if (periodic.isReady()) {
                     if (mEffect instanceof ManagedHeroEffect) {
                         periodic.tick(((ManagedHeroEffect) mEffect).hero);
                     } else {
                         periodic.tick(((ManagedCreatureEffect) mEffect).creature);
                     }
                 }
             }
         }
     }
 }
 
 class ManaUpdater implements Runnable {
 
     private final HeroManager manager;
     private final long updateInterval;
     private final int manaPercent;
     private long lastUpdate = 0;
 
     ManaUpdater(HeroManager manager, long updateInterval, int manaPercent) {
         this.manager = manager;
         this.updateInterval = updateInterval;
         this.manaPercent = manaPercent;
     }
 
     @Override
     public void run() {
         long time = System.currentTimeMillis();
         if (time < lastUpdate + updateInterval)
             return;
         lastUpdate = time;
 
         Collection<Hero> heroes = manager.getHeroes();
         for (Hero hero : heroes) {
             if (hero == null) {
                 continue;
             }
 
             int mana = hero.getMana();
             if (mana == 100) {
                 continue;
             }
 
             HeroRegainManaEvent hrmEvent = new HeroRegainManaEvent(hero, manaPercent, null);
             Bukkit.getServer().getPluginManager().callEvent(hrmEvent);
             if (hrmEvent.isCancelled()) {
                 continue;
             }
 
             hero.setMana(mana + hrmEvent.getAmount());
             if (hero.isVerbose()) {
                 Messaging.send(hero.getPlayer(), ChatColor.BLUE + "MANA " + Messaging.createManaBar(hero.getMana()));
             }
         }
     }
 }
 
 class PartyUpdater implements Runnable {
 
     private final HeroManager manager;
     private final Heroes plugin;
     private final PartyManager partyManager;
 
     PartyUpdater(HeroManager manager, Heroes plugin, PartyManager partyManager) {
         this.manager = manager;
         this.plugin = plugin;
         this.partyManager = partyManager;
     }
 
     @Override
     public void run() {
         if (!this.plugin.getConfigManager().getProperties().mapUI)
             return;
 
         if (partyManager.getParties().size() == 0)
             return;
 
         for (HeroParty party : partyManager.getParties()) {
             if (party.updateMapDisplay()) {
                 party.setUpdateMapDisplay(false);
                 Player[] players = new Player[party.getMembers().size()];
                 int count = 0;
                 for (Hero heroes : party.getMembers()) {
                     players[count] = heroes.getPlayer();
                     count++;
                 }
                 updateMapView(players);
             }
         }
     }
 
     private void updateMapView(Player[] players) {
         MapAPI mapAPI = new MapAPI();
         short mapId = this.plugin.getConfigManager().getProperties().mapID;
 
         TextRenderer text = new TextRenderer(this.plugin);
         CharacterSprite sword = CharacterSprite.make("      XX", "     XXX", "    XXX ", "X  XXX  ", " XXXX   ", "  XX    ", " X X    ", "X   X   ");
         CharacterSprite crown = CharacterSprite.make("        ", "        ", "XX XX XX", "X XXXX X", "XX XX XX", " XXXXXX ", " XXXXXX ", "        ");
         CharacterSprite shield = CharacterSprite.make("   XX   ", "X  XX  X", "XXXXXXXX", "XXXXXXXX", "XXXXXXXX", " XXXXXX ", "  XXXX  ", "   XX   ");
         CharacterSprite heal = CharacterSprite.make("        ", "  XXX   ", "  XXX   ", "XXXXXXX ", "XXXXXXX ", "XXXXXXX ", "  XXX   ", "  XXX   ");
         CharacterSprite bow = CharacterSprite.make("XXXX   X", "X  XX X ", " X   X  ", "  X X X ", "   X  XX", "  X X  X", "XX   X X", " X    XX");
         text.setChar('\u0001', crown);
         text.setChar('\u0002', sword);
         text.setChar('\u0003', shield);
         text.setChar('\u0004', heal);
         text.setChar('\u0005', bow);
 
         MapInfo info = mapAPI.loadMap(Bukkit.getServer().getWorlds().get(0), mapId);
         mapAPI.getWorldMap(Bukkit.getServer().getWorlds().get(0), mapId).map = (byte) 9;
 
         info.setData(new byte[128 * 128]);
 
         String map = "§22;Party Members -\n";
 
         for (Player player : players) {
             Hero hero = this.manager.getHero(player);
             if (!hero.hasParty() || hero.getParty().getLeader() == null) {
                 Heroes.log(Level.SEVERE, "Error in party of player: " + player.getDisplayName());
                 continue;
             }
             if (hero.getParty().getLeader().equals(hero)) {
                 map += "§42;\u0001";
             } else {
                 map += "§27;\u0002";
             }
             boolean damage = plugin.getConfigManager().getProperties().damageSystem;
             double currentHP;
             double maxHP;
             if (damage) {
                 currentHP = hero.getHealth();
                 maxHP = hero.getMaxHealth();
             } else {
                 currentHP = hero.getPlayer().getHealth();
                 maxHP = 20;
             }
             map += " §12;" + player.getName() + "\n" + createHealthBar(currentHP, maxHP) + "\n";
         }
 
         text.fancyRender(info, 10, 3, map);
 
         for (Player player : players) {
             mapAPI.sendMap(player, mapId, info.getData(), this.plugin.getConfigManager().getProperties().mapPacketInterval);
         }
     }
 
     private static String createHealthBar(double health, double maxHealth) {
         String manaBar = com.herocraftonline.dev.heroes.ui.MapColor.DARK_RED + "[" + com.herocraftonline.dev.heroes.ui.MapColor.DARK_GREEN;
         int bars = 40;
         int progress = (int) (health / maxHealth * bars);
         for (int i = 0; i < progress; i++) {
             manaBar += "|";
         }
         manaBar += com.herocraftonline.dev.heroes.ui.MapColor.DARK_GRAY;
         for (int i = 0; i < bars - progress; i++) {
             manaBar += "|";
         }
         manaBar += com.herocraftonline.dev.heroes.ui.MapColor.RED + "]";
         double percent = health / maxHealth * 100;
         DecimalFormat df = new DecimalFormat("#.##");
         return manaBar + " - " + com.herocraftonline.dev.heroes.ui.MapColor.GREEN + df.format(percent) + "%";
     }
 }
