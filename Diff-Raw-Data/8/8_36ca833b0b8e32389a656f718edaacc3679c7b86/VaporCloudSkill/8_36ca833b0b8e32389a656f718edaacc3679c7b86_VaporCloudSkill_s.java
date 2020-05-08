 package aigilas.skills.impl;
 
 import aigilas.creatures.BaseCreature;
import aigilas.creatures.StatBuff;
import aigilas.creatures.StatType;
 import aigilas.skills.AnimationType;
 import aigilas.skills.BaseSkill;
 import aigilas.skills.SkillId;
 
 public class VaporCloudSkill extends BaseSkill {
     public VaporCloudSkill() {
         super(SkillId.Vapor_Cloud, AnimationType.CLOUD);
     }
 
     @Override
     public void affect(BaseCreature target) {
        target.addBuff(new StatBuff(StatType.Move_Cool_Down, -1000));
     }
 }
