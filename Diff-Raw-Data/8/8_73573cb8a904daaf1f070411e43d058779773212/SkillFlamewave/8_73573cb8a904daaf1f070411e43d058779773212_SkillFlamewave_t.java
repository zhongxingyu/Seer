 package Model.Skills.Wizard;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skill;
 import Model.StatusEffects.StatusEffectBurning;
 
 public class SkillFlamewave extends Skill{
 
 	public SkillFlamewave() {
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
 		super("Flamewave", 11000, 400, 1.7, 3, 50, 3,"Flame wave:\nA majestic shot of flames that \ntakes the form of a phoenix \nwhile scorching your enemies.\n" +
 				"\nLevel 1: 15 damage\n" +
 				"Level 2: 25 damage\n" +
 				"Level 3: 35 damage\n" +
 				"Level 4: 45 damage");
 		
 		Image[] attackImages = new Image[2];
 		Image[] skillBar = new Image[3];
 
 		super.setPiercing(true);
 		super.setOffensiveStatusEffectShell(new StatusEffectBurning(this, 50, 1), false);
 		
 		try {
 			attackImages[0] = new Image("res/animations/Flamewave/Flamewave1.png");
 			attackImages[1] = new Image("res/animations/Flamewave/Flamewave2.png");
 			
 			skillBar[0] = new Image("res/skillIcons/flamewave.png");
 			skillBar[1] = new Image("res/skillIcons/flamewave_active.png");
 			skillBar[2] = new Image("res/skillIcons/flamewave_disabled.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		super.setImage(attackImages,200);
 		super.setSkillBarImages(skillBar);
 	}
 
 	private int lvl2 = 400;
 	private int lvl3 = 500;
 	private int lvl4 = 600;
 	
 	@Override
 	public void upgradeSkill() {
 		if(super.getCurrentLvl() < 4){
 			super.incCurrentLvl();
 			
 			switch(super.getCurrentLvl()){
 			case 2:
				super.setOffensiveStatusEffectShell(new StatusEffectBurning(this, 50, 2), false);
 				super.setDamage(lvl2);
 				break;
 			case 3:
				super.setOffensiveStatusEffectShell(new StatusEffectBurning(this, 50, 3), false);
 				super.setDamage(lvl3);
 				break;
 			case 4:
				super.setOffensiveStatusEffectShell(new StatusEffectBurning(this, 50, 4), false);
 				super.setDamage(lvl4);
 				break;
 			}
 		}
 	}
 }
