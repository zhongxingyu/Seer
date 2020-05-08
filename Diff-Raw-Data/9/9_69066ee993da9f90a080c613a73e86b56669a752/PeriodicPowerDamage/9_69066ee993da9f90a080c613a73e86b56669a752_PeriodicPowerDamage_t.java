 package roguelike.effects.effectTypes;
 
 import java.util.*;
 
 import roguelike.core.*;
 import roguelike.effects.*;
 
 public class PeriodicPowerDamage extends  EffectType {
 	int damagePerTick;
 	
 	public PeriodicPowerDamage(int damagePerTick) {
 		this.damagePerTick = damagePerTick;
 	}
 
 	public void onAdd(Effect e, GameObject target) {
 		eventHandler.createEvent(tickDelay, e);
 	}
 
 	public void onTick(Effect e, GameObject target) {
 		if( e.getPower() <= 0 ) {
 			target.removeEffect(e);
 			return;
 		}
 		long timeToNextTick = Math.min(10000, Math.max(250, 10000 - e.getPower()*e.getPower()));
 		e.setPower(e.getPower()-1);
 		eventHandler.createEvent(timeToNextTick, e);

		target.doDamage(damagePerTick);
 	}
 
 	public void stack(Effect e, List<Effect> effectList, GameObject target) {
 		//Find any effects with the same name as current, stack power
 		boolean targetFound = false;
 		for(Effect i : effectList) {
 			if( i.getTypeName() == e.getTypeName() ) {
 				i.setPower(i.getPower() + e.getPower());
 				targetFound = true;
 				break;
 			}
 		}
 		
 		if( !targetFound ) {
 			effectList.add(e);
 			onAdd(e,target);
 		}
 	}
 }
