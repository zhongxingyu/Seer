 package aigilas.strategies.impl;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.strategies.BaseStrategy;
 import aigilas.strategies.Strategy;
 
 public class MinionCloudStrategy extends BaseStrategy {
    public MinionCloudStrategy(BaseCreature parent)

    {
         super(parent, Strategy.MinionCloud);

         parent.setSkillVector(parent.getSkillVector());
     }
 
     @Override
     public void act() {
         _parent.useActiveSkill();
         _parent.setInactive();
     }
 }
