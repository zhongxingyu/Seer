 package Model.Skills.Hunter;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skills.Skill;
 import Model.StatusEffects.StatusEffectStun;
 
 public class SkillArrow extends Skill{
 	
 	public SkillArrow(){
 		// name, cd, range, speed, aoe, cost, damageLvl1, damageLvl2, damageLvl3, damageLvl4, describe, affectSelf
		super("Arrow", 2000, 320, 2.2, 3, 0, 50, "Arrow: \nShoots a plain arrow.\n" +
 				"Level 1: 10 damage\n" +
 				"Level 2: 20 damage\n" +
 				"Level 3: 30 damage\n" +
 				"Level 4: 40 damage");
 		
 		Image[] attackImages = new Image[1];
 		Image[] skillBar = new Image[3];
 		
 		try {
 			attackImages[0] = new Image("res/animations/arrow/arrow.png");
 			
 			skillBar[0] = new Image("res/skillIcons/arrow.png");
 			skillBar[1] = new Image("res/skillIcons/arrow_active.png");
 			skillBar[2] = new Image("res/skillIcons/arrow_disabled.png");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		
 		
 		super.setImage(attackImages, 200);
 		super.setSkillBarImages(skillBar);
 	}
 	
 	@Override
 	public void upgradeSkill() {
 	}
 
 }
