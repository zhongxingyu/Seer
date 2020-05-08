 package aigilas.skills;
 
 import aigilas.entities.Elements;
 import spx.bridge.ActorType;
 import spx.core.RNG;
 import spx.devtools.DevConsole;
 import sun.util.calendar.CalendarSystem;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class SkillLogic {
     private static HashMap<Elements, List<SkillId>> __elementMap = new HashMap<>();
     private static HashMap<SkillId, AnimationType> __skillAnimationMap = new HashMap<>();
     public static HashMap<ActorType, List<SkillId>> __actorToSkillMapping = new HashMap<>();
     private static List<SkillId> __invalidRandomSkills = new ArrayList<>();
     private static int skillPick = 0;
 
     private SkillLogic(){}
 
     static{
         if (__elementMap.size() == 0) {
             ISkill skill;
             for (SkillId skillId : SkillId.values()) {
                 if (skillId != SkillId.NO_SKILL) {
                     if(!skillId.Restrict){
                         __invalidRandomSkills.add(skillId);
                     }
                     skill = SkillFactory.Create(skillId);
                     for (Elements elem : skill.GetElements()) {
                         if (!__elementMap.containsKey(elem)) {
                             __elementMap.put(elem, new ArrayList<SkillId>());
                         }
                         __elementMap.get(elem).add(skillId);
                     }
                     __skillAnimationMap.put(skillId, skill.GetAnimationType());
                 }
             }
             __skillAnimationMap.put(SkillId.NO_SKILL, null);
         }
     }
 
     private static SkillId GetElementalSkill(Elements elementId) {
         while (__invalidRandomSkills.contains(SkillId.values()[skillPick])) {
             skillPick = RNG.Next(0, __elementMap.get(elementId).size());
         }
         DevConsole.Get().Add(skillPick + "");
         return __elementMap.get(elementId).get(skillPick);
     }
 
     public static List<SkillId> GetElementalSkills(ActorType actorType, List<Elements> _elementalComposition) {
         if (!__actorToSkillMapping.containsKey(actorType)) {
             __actorToSkillMapping.put(actorType, new ArrayList<SkillId>());
             for (Elements elem : _elementalComposition) {
                 __actorToSkillMapping.get(actorType).add(SkillLogic.GetElementalSkill(elem));
             }
         }
         return __actorToSkillMapping.get(actorType);
     }
 
     public static boolean IsSkill(SkillId skillId, AnimationType animationType) {
         return __skillAnimationMap.get(skillId) == animationType;
     }
 }
