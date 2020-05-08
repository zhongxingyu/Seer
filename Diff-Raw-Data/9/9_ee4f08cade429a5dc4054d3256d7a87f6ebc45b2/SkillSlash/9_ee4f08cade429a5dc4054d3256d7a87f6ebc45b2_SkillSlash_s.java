 package Model.Skills.Warrior;
 
 
 import Model.StatusEffect;
 import Model.Skills.Skill;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class SkillSlash extends Skill{
 	
 	public SkillSlash(){
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
		super("Slash", 1000, 0, 100, 5, 0, 150, "Slash \n" +
 				"Level 1: 15 damage\n" +
 				"Level 2: 25 damage\n" +
 				"Level 3: 35 damage\n" +
 				"Level 4: 45 damage");
 		
 		Image attackImage = null;
 		Image[] animation = new Image[4];
 		Image[] skillBar = new Image[3];
 		
 		try {
 			attackImage = new Image("res/animations/slash/slash1.png");
 			
 			animation[0] = new Image("res/animations/slash/slash1.png");
 			animation[1] = new Image("res/animations/slash/slash2.png");
 			animation[2] = new Image("res/animations/slash/slash3.png");
 			animation[3] = new Image("res/animations/slash/slash4.png");
 			
 			
 			skillBar[0] = new Image("res/skillIcons/slash.png");
 			skillBar[1] = new Image("res/skillIcons/slash_active.png");
 			skillBar[2] = new Image("res/skillIcons/slash_disabled.png");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		
 		super.setImage(attackImage);
 		super.setEndState(animation, 400, 400);
 		super.setSkillBarImages(skillBar);
 		
 	}
 	
 	@Override
 	public void upgradeSkill() {
 	}
 }
