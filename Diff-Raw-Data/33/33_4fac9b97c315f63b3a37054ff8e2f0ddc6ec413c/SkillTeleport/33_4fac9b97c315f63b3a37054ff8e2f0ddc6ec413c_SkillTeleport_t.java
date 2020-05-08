 package Model.Skills.Wizard;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skills.Skill;
 import Model.StatusEffects.*;
 
 public class SkillTeleport extends Skill {
 
 	public SkillTeleport() {
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
		super("Teleport", 20000, 500, 100, 8, 50, 0, "Teleport:\nTAFLEPoRT!");
 		
 		Image attackImage = null;
 		Image[] animation = new Image[5];
 		Image[] skillBar = new Image[3];
 		
 		super.setSelfAffectingStatusEffectShell(new StatusEffectTeleport(this));
 		
 		try {
 			attackImage = new Image("res/animations/Teleport/Teleport1.png");
 			
 			animation[0] = new Image("res/animations/Teleport/Teleport1.png");
 			animation[1] = new Image("res/animations/Teleport/Teleport2.png");
 			animation[2] = new Image("res/animations/Teleport/Teleport3.png");
 			animation[3] = new Image("res/animations/Teleport/Teleport4.png");
 			animation[4] = new Image("res/animations/Teleport/Teleport5.png");
 			
 			
 			
 			skillBar[0] = new Image("res/skillIcons/teleport.png");
 			skillBar[1] = new Image("res/skillIcons/teleport_active.png");
 			skillBar[2] = new Image("res/skillIcons/teleport_disabled.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		super.setImage(attackImage);
 		super.setEndState(animation, 200, 200);
 		super.setSkillBarImages(skillBar);
 	}
 
	private int lvl2 = 15000;
	private int lvl3 = 10000;
	private int lvl4 = 5000;
 	
 	@Override
 	public void upgradeSkill() {
 		if(super.getCurrentLvl() < 4){
 			super.incCurrentLvl();
 			
 			switch(super.getCurrentLvl()){
 			case 2:
				super.setCooldown(lvl2);
 				break;
 			case 3:
				super.setCooldown(lvl3);
 				break;
 			case 4:
				super.setCooldown(lvl4);
 				break;
 			}
 		}
 	}
 
 }
