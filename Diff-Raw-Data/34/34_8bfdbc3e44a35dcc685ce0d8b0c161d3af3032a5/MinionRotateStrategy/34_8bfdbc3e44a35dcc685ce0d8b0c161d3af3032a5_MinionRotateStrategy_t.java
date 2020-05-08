 package aigilas.strategies.impl;
 
 import aigilas.creatures.BaseCreature;
import aigilas.creatures.StatType;
 import aigilas.strategies.BaseStrategy;
 import aigilas.strategies.Strategy;
import sps.core.Logger;
 import sps.core.Point2;
 
 public class MinionRotateStrategy extends BaseStrategy {
     public MinionRotateStrategy(BaseCreature parent) {
         super(parent, Strategy.MinionRotate);
         // TODO pass master into strategy to copy targets here.
         parent.setSkillVector(new Point2(1, 0));
     }
 
     @Override
     public void act() {
         if (_parent.isCooledDown()) {
             _parent.setSkillVector(_parent.getSkillVector().rotate());
             _parent.useActiveSkill();
            Logger.info(_parent.get(StatType.Health) + "");
            _parent.applyDamage(_parent.get(StatType.Defense) + (_parent.getMax(StatType.Health) / 6) + 1, null, false);
             _parent.resetWaitTime();
         }
     }
 }
