 package aigilas.strategies.impl;
 
 import aigilas.creatures.BaseCreature;
import aigilas.creatures.StatType;
 import aigilas.strategies.BaseStrategy;
 import aigilas.strategies.Strategy;
 
 public class MinionFireStrategy extends BaseStrategy {
    public MinionFireStrategy(BaseCreature parent) {
         super(parent, Strategy.MinionFire);
         parent.setSkillVector(parent.getSkillVector());
     }
 
     @Override
     public void act() {
         if (_parent.isCooledDown()) {
             _parent.useActiveSkill();
            _parent.applyDamage(_parent.get(StatType.Defense) + (_parent.getMax(StatType.Health) / 6) + 1, null, false);
             _parent.resetWaitTime();
         }
     }
 }
