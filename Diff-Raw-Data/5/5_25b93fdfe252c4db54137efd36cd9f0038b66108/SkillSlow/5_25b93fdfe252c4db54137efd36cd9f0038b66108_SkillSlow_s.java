 package com.herocraftonline.dev.heroes.skill.skills;
 
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.Packet41MobEffect;
 import net.minecraft.server.Packet42RemoveMobEffect;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillSlow extends TargettedSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillSlow(Heroes plugin) {
         super(plugin, "Slow");
         setDescription("Slows the target's movement speed");
         setUsage("/skill slow");
         setArgumentRange(0, 1);
         setIdentifiers("skill slow");
         setTypes(SkillType.DEBUFF, SkillType.MOVEMENT, SkillType.SILENCABLE);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("speed-multiplier", 2);
         node.setProperty(Setting.DURATION.node(), 15000);
        node.setProperty("apply-text", "%hero% gained a burst of speed!");
         node.setProperty("expire-text", "%hero% returned to normal speed!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
        applyText = getSetting(null, "apply-text", "%hero% gained a burst of speed!").replace("%hero%", "$1");
         expireText = getSetting(null, "expire-text", "%hero% returned to normal speed!").replace("%hero%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         HeroClass heroClass = hero.getHeroClass();
         int duration = getSetting(heroClass, Setting.DURATION.node(), 15000);
         int multiplier = getSetting(heroClass, "speed-multiplier", 2);
         if (multiplier > 20) {
             multiplier = 20;
         }
         SlowEffect effect = new SlowEffect(this, duration, multiplier);
 
         if (target instanceof Player) {
             plugin.getHeroManager().getHero((Player) target).addEffect(effect);
             return true;
         } else if (target instanceof Creature) {
             plugin.getHeroManager().addCreatureEffect((Creature) target, effect);
             return true;
         }
 
         broadcastExecuteText(hero, target);
 
         return true;
     }
 
     public class SlowEffect extends ExpirableEffect {
 
         private int amplifier = 0;
         private int duration = 0;
 
         private MobEffect mobEffect = new MobEffect(2, 0, 0);
 
         public SlowEffect(Skill skill, long duration, int amplifier) {
             super(skill, "Slow", duration);
             this.types.add(EffectType.DISPELLABLE);
             this.types.add(EffectType.HARMFUL);
             this.amplifier = amplifier;
             this.duration = (int) (duration / 1000) * 20;
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
 
             this.mobEffect = new MobEffect(2, this.duration, this.amplifier);
 
             Player player = hero.getPlayer();
             EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
             entityPlayer.netServerHandler.sendPacket(new Packet41MobEffect(entityPlayer.id, this.mobEffect));
 
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             Player player = hero.getPlayer();
             EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
             entityPlayer.netServerHandler.sendPacket(new Packet42RemoveMobEffect(entityPlayer.id, this.mobEffect));
 
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
 
     }
 }
