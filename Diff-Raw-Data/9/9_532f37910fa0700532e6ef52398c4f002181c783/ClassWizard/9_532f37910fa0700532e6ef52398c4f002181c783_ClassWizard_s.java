 package Model.Classes;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Player;
 import Model.Skills.Skill;
 import Model.Skills.Hunter.SkillArrow;
 import Model.Skills.Warrior.SkillSlash;
 import Model.Skills.Wizard.*;
 
 public class ClassWizard extends Player {
 
 	Image playerImage;
 	Image firstStep;
 	Image secondStep;
 	static Skill[] chosenSkills = new Skill[5];
 	Image[] changedModelWalkImages = new Image[12];
 	Image[] changedModelStandImages = new Image[12];
 
 	public ClassWizard(String name, float x, float y, int index) {
 		super(name, "Wizard", x, y, 900, 0.5, 0.2, index);
 		try {
 			playerImage = new Image("res/animations/mage_stand.png");
 			firstStep = new Image("res/animations/mage_walk1.png");
 			secondStep = new Image("res/animations/mage_walk2.png");
 			
 			changedModelWalkImages[0] = new Image("res/animations/UnstableMagic/UMmage_walk1_1.png");
 			changedModelWalkImages[1] = new Image("res/animations/UnstableMagic/UMmage_walk2_2.png");
 			changedModelWalkImages[2] = new Image("res/animations/UnstableMagic/UMmage_walk1_3.png");
 			changedModelWalkImages[3] = new Image("res/animations/UnstableMagic/UMmage_walk2_4.png");
 			changedModelWalkImages[4] = new Image("res/animations/UnstableMagic/UMmage_walk1_5.png");
 			changedModelWalkImages[5] = new Image("res/animations/UnstableMagic/UMmage_walk2_6.png");
 			changedModelWalkImages[6] = new Image("res/animations/UnstableMagic/UMmage_walk1_7.png");
 			changedModelWalkImages[7] = new Image("res/animations/UnstableMagic/UMmage_walk2_8.png");
 			changedModelWalkImages[8] = new Image("res/animations/UnstableMagic/UMmage_walk1_9.png");
 			changedModelWalkImages[9] = new Image("res/animations/UnstableMagic/UMmage_walk2_10.png");
 			changedModelWalkImages[10] = new Image("res/animations/UnstableMagic/UMmage_walk1_11.png");
 			changedModelWalkImages[11] = new Image("res/animations/UnstableMagic/UMmage_walk2_12.png");
 					
 			changedModelStandImages[0] = new Image("res/animations/UnstableMagic/UMmage_stand1.png");
 			changedModelStandImages[1] = new Image("res/animations/UnstableMagic/UMmage_stand2.png");
 			changedModelStandImages[2] = new Image("res/animations/UnstableMagic/UMmage_stand3.png");
 			changedModelStandImages[3] = new Image("res/animations/UnstableMagic/UMmage_stand4.png");
 			changedModelStandImages[4] = new Image("res/animations/UnstableMagic/UMmage_stand5.png");
 			changedModelStandImages[5] = new Image("res/animations/UnstableMagic/UMmage_stand6.png");
 			changedModelStandImages[6] = new Image("res/animations/UnstableMagic/UMmage_stand7.png");
 			changedModelStandImages[7] = new Image("res/animations/UnstableMagic/UMmage_stand8.png");
 			changedModelStandImages[8] = new Image("res/animations/UnstableMagic/UMmage_stand9.png");
 			changedModelStandImages[9] = new Image("res/animations/UnstableMagic/UMmage_stand10.png");
 			changedModelStandImages[10] = new Image("res/animations/UnstableMagic/UMmage_stand11.png");
 			changedModelStandImages[11] = new Image("res/animations/UnstableMagic/UMmage_stand12.png");
 			
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		chosenSkills[0] = new SkillFireball();
 		chosenSkills[1] = new SkillFirestorm();
		chosenSkills[2] = new SkillIceNeedle();
 		chosenSkills[3] = new SkillUnstableMagic();
 		chosenSkills[4] = new SkillTeleport();
 		
 		super.setImages(playerImage, firstStep, secondStep);
 		super.setChangedModelImages(changedModelWalkImages, changedModelStandImages);
 		super.setSkillList(chosenSkills);
 	}
 }
