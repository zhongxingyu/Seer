 package aigilas.skills.impl;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.skills.AnimationType;
 import aigilas.skills.BaseSkill;
 import aigilas.skills.SkillId;
import aigilas.statuses.Status;
import aigilas.statuses.StatusFactory;
 
 public class VaporCloudSkill extends BaseSkill {
     public VaporCloudSkill() {
         super(SkillId.Vapor_Cloud, AnimationType.CLOUD);
     }
 
     @Override
     public void affect(BaseCreature target) {
        StatusFactory.apply(target, Status.SlowDown);
     }
 }
