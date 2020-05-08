 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.Random;
import java.util.Set;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroesEventListener;
 import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillCurse extends TargettedSkill {
 
     private String applyText;
     private String expireText;
     private String missText;
     private Random rand = new Random();
 
     public SkillCurse(Heroes plugin) {
         super(plugin, "Curse");
         setDescription("Curses your target causing their attacks to miss");
         setUsage("/skill curse <target>");
         setArgumentRange(0, 1);
         setIdentifiers("skill curse");
         setTypes(SkillType.DARK, SkillType.SILENCABLE, SkillType.HARMFUL);
 
         registerEvent(Type.CUSTOM_EVENT, new SkillEventListener(), Priority.Highest);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DURATION.node(), 5000); // in milliseconds
         node.setProperty("miss-chance", .50); // decimal representation of miss-chance
         node.setProperty("miss-text", "%target% misses an attack!");
         node.setProperty(Setting.APPLY_TEXT.node(), "%target% has been cursed!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "%target% has recovered from the curse!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         missText = getSetting(null, "miss-text", "%target% misses an attack!").replace("%target%", "$1");
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% has been cursed!").replace("%target%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target% has recovered from the curse!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         HeroClass heroClass = hero.getHeroClass();
         long duration = getSetting(heroClass, Setting.DURATION.node(), 5000);
         double missChance = getSetting(heroClass, "miss-chance", .50);
         CurseEffect cEffect = new CurseEffect(this, duration, missChance);
 
         if (target instanceof Player) {
             plugin.getHeroManager().getHero((Player) target).addEffect(cEffect);
             return true;
         } else if (target instanceof Creature) {
             plugin.getHeroManager().addCreatureEffect((Creature) target, cEffect);
             return true;
         }
 
         Messaging.send(player, "Invalid target!");
         return false;
     }
 
     public class CurseEffect extends ExpirableEffect {
 
         private final double missChance;
 
         public CurseEffect(Skill skill, long duration, double missChance) {
             super(skill, "Curse", duration);
             this.missChance = missChance;
             this.types.add(EffectType.HARMFUL);
             this.types.add(EffectType.DISPELLABLE);
         }
 
         @Override
         public void apply(Creature creature) {
             super.apply(creature);
             broadcast(creature.getLocation(), applyText, Messaging.getCreatureName(creature).toLowerCase());
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         public double getMissChance() {
             return missChance;
         }
 
         @Override
         public void remove(Creature creature) {
             super.remove(creature);
             broadcast(creature.getLocation(), expireText, Messaging.getCreatureName(creature).toLowerCase());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
 
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
     }
 
     public class SkillEventListener extends HeroesEventListener {
 
         @Override
         public void onWeaponDamage(WeaponDamageEvent event) {
             if (event.isCancelled() || event.getDamage() == 0)
                 return;
 
             Hero hero = null;
             Creature creature = null;
 
             if (event.getDamager() instanceof Player) {
                 hero = plugin.getHeroManager().getHero((Player) event.getDamager());
             } else if (event.getDamager() instanceof Creature) {
                 creature = (Creature) event.getDamager();
             } else if (event.getDamager() instanceof Projectile) {
                 LivingEntity shooter = ((Projectile) event.getDamager()).getShooter();
                 if (shooter == null)
                     return;
                 if (shooter instanceof Player) {
                     hero = plugin.getHeroManager().getHero((Player) shooter);
                 } else if (shooter instanceof Creature) {
                     creature = (Creature) shooter;
                 }
             }
 
             if (hero != null) {
                 if (hero.getEffect("Curse") != null) {
                     CurseEffect cEffect = (CurseEffect) hero.getEffect("Curse");
                     if (rand.nextDouble() < cEffect.missChance) {
                         event.setCancelled(true);
                         broadcast(hero.getPlayer().getLocation(), missText, hero.getPlayer().getDisplayName());
                     }
                 }
             } else if (creature != null) {
                Set<Effect> creatureEffects = plugin.getHeroManager().getCreatureEffects(creature);
                if (creatureEffects == null)
                     return;
                for (Effect effect : creatureEffects) {
                     if (effect instanceof CurseEffect) {
                         CurseEffect cEffect = (CurseEffect) effect;
                         if (rand.nextDouble() < cEffect.missChance) {
                             event.setCancelled(true);
                             broadcast(creature.getLocation(), missText, Messaging.getCreatureName(creature).toLowerCase());
                         }
                     }
                 }
             }
         }
     }
 }
