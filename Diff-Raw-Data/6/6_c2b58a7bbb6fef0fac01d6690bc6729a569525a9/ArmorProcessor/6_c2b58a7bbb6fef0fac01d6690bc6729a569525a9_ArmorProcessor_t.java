 package ch.andefgassm.adventuregame.game;
 
 import ch.andefgassm.adventuregame.combat.Combatant;
 import ch.andefgassm.adventuregame.combat.Effect;
 import ch.andefgassm.adventuregame.combat.IStatProcessor;
 
 public class ArmorProcessor implements IStatProcessor {
 
     @Override
     public int modify(Combatant caster, Combatant target, Effect effect, int baseDamage) {
         int effectiveDamage = baseDamage;
         if (effect.getDamageType() == DamageType.PHYSICAL && baseDamage < 0) {
            Integer armor = target.getCurrentStats().get(Stat.ARMOR);
            if (armor != null) {
                effectiveDamage = baseDamage + armor;
                 if (effectiveDamage > 0) {
                     effectiveDamage = 0;
                 }
             }
         }
         return effectiveDamage;
     }
 
 }
