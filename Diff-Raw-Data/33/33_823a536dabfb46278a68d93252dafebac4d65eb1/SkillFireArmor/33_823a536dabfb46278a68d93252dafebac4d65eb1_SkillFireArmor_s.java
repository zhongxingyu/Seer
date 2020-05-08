 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillFireArmor extends ActiveSkill {
     
     private String applyText;
     private String expireText;
     private String igniteText;
 
     private Random random = new Random();
     private List<String> defaultArmors = new ArrayList<String>();
     
     public SkillFireArmor(Heroes plugin) {
         super(plugin, "FireArmor");
         setDescription("Gives your armor a chance to ignite your attackers!");
         setUsage("/skill firearmor");
         setArgumentRange(0, 0);
         setIdentifiers(new String[] { "skill firearmor", "skill farmor" });
         
         defaultArmors.add(Material.GOLD_CHESTPLATE.name());
         
         registerEvent(Type.ENTITY_DAMAGE, new SkillDamageListener(), Priority.Monitor);
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("armors", defaultArmors);
        node.setProperty(Setting.APPLY_TEXT.node(), "%hero%'s weapon is sheathed in flame!");
        node.setProperty(Setting.EXPIRE_TEXT.node(), "%hero%'s weapon is no longer aflame!");
         node.setProperty(Setting.DURATION.node(), 20000);
         node.setProperty("ignite-chance", 0.20);
         node.setProperty("ignite-duration", 5000);
         node.setProperty("ignite-text", "%hero% ignited %target% with firearmor!");
         return node;
     }
     
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%hero%'s armor is enveloped in flame!").replace("%hero%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%hero%'s armor is no longer aflame!").replace("%hero%", "$1");
         igniteText = getSetting(null, "ignite-text", "%hero% ignited %target% with firearmor!").replace("%hero%", "$1").replace("%target%", "$2");
     }
     
     @Override
     public boolean use(Hero hero, String[] args) {
         int duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 20000);
         hero.addEffect(new FireArmorEffect(this, duration));
         return true;
     }
 
     public class FireArmorEffect extends ExpirableEffect {
 
         public FireArmorEffect(Skill skill, long duration) {
             super(skill, "FireArmor", duration);
             this.types.add(EffectType.BENEFICIAL);
             this.types.add(EffectType.DISPELLABLE);
             this.types.add(EffectType.FIRE);
         }
         
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
     }
     
     public class SkillDamageListener extends EntityListener {
         
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent) ) 
                 return;
             
             Player player = (Player) event.getEntity();
             Hero hero = plugin.getHeroManager().getHero(player);
             
             if (!hero.hasEffect("FireArmor") || !getSetting(hero.getHeroClass(), "armors", defaultArmors).contains(player.getInventory().getChestplate().getType().name()))
                 return;
             
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             //Dont set Projectiles on fire
             if (!(subEvent.getDamager() instanceof LivingEntity))
                 return;
             
             //Check our ignite chance
             double chance = getSetting(hero.getHeroClass(), "ignite-chance", .2);
             if (random.nextDouble() >= chance)
                 return;
             
             //Set the damager on fire if it was successful
             int fireTicks = getSetting(hero.getHeroClass(), "ignite-duration", 5000) / 200;
             subEvent.getDamager().setFireTicks(fireTicks);
             
             String name = null;
             if (subEvent.getDamager() instanceof Player) {
                 name = ((Player) subEvent.getDamager()).getName();
             } else if (subEvent.getDamager() instanceof Creature) {
                 Messaging.getCreatureName((Creature) subEvent.getDamager());
             }
             
             broadcast(player.getLocation(), igniteText, new Object[] {player.getDisplayName(), name});
         }
     }
 }
