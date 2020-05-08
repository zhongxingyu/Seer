 package View;
 
 //import java.awt.Image;
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import Control.GlobalClassSelector;
 import Model.Player;
 import Model.PlayerModel;
 import Model.Skills.Skill;
 import Model.Skills.Hunter.SkillArrow;
 import Model.Skills.Hunter.SkillArrowFlurry;
 import Model.Skills.Hunter.SkillBarrelRoll;
 import Model.Skills.Hunter.SkillCripplingShot;
 import Model.Skills.Hunter.SkillFlamingArrow;
 import Model.Skills.Hunter.SkillGuidedArrow;
 import Model.Skills.Hunter.SkillLifestealingArrows;
 import Model.Skills.Hunter.SkillPassiveDodge;
 import Model.Skills.Hunter.SkillSprint;
 import Model.Skills.Hunter.SkillStealth;
 import Model.Skills.Warrior.SkillAdrenaline;
 import Model.Skills.Warrior.SkillFirstAid;
 import Model.Skills.Warrior.SkillGrapplingHook;
 import Model.Skills.Warrior.SkillImprovedArmor;
 import Model.Skills.Warrior.SkillIncreasedMovement;
 import Model.Skills.Warrior.SkillLeapAttack;
 import Model.Skills.Warrior.SkillShieldStance;
 import Model.Skills.Warrior.SkillSlash;
 import Model.Skills.Warrior.SkillThrowingAxe;
 import Model.Skills.Warrior.SkillWarstomp;
 import Model.Skills.Wizard.SkillAbsorb;
 import Model.Skills.Wizard.SkillBlizzard;
 import Model.Skills.Wizard.SkillFireball;
 import Model.Skills.Wizard.SkillFirestorm;
 import Model.Skills.Wizard.SkillFlamewave;
 import Model.Skills.Wizard.SkillIceblock;
 import Model.Skills.Wizard.SkillIroncloak;
 import Model.Skills.Wizard.SkillTeleport;
 import Model.Skills.Wizard.SkillUnstablemagic;
 import Model.Skills.Wizard.SkillWandattack;
 
 
 
 public class ShoppingView extends BasicGameState {
 	
 	String classtype="Hunter";
 	
 	Image skillDescBg;
 
 	Image chosenSkill = null;
 	
 	Image playerGold;
 	String playerGoldText;
 	
 	private String mouse = "No input yet";
 	Image menuTab;
 	
 	Skill[] offSkills = new Skill[3];
 	Skill[] defSkills = new Skill[3];
 	Skill[] mobSkills = new Skill[3];
 	Skill[] chosenSkills = new Skill[5];
 	Skill basicSkill;
 	
 	Image playButton;
 	
 	Image classPortrait;
 	
 	//Wizard skillicons
 	Image firstOffSkill;
 	Image secondOffSkill;
 	Image thirdOffSkill; 
 	
 	Image firstDefSkill;
 	Image secondDefSkill;
 	Image thirdDefSkill;
 	
 	Image firstMobSkill;
 	Image secondMobSkill;
 	Image thirdMobSkill;
 	
 	Image wandAttackSkill;
 	String wandattackDesc;
 
 	String skillText;
 	
 	Image background;
 	Image skillsText;
 	Image shopText;
 
 	public ShoppingView (int state){
 		
 	}
 	
 	public int getID(){
 		return 4;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)throws SlickException {
 		
 		background = new Image("res/miscImages/ShoppingviewBackground.png");
 		skillsText = new Image("res/miscImages/skillsText.png");
 		shopText = new Image("res/miscImages/shopText.png");
 
		skillText = "Bitch please!";
 		playerGold = new Image("res/miscImages/PlayerGold.png");
 		playerGoldText = ""+GlobalClassSelector.getController().getPlayers()[GlobalClassSelector.getController().getActivePlayerIndex()].getGold();
 		
 
 		
 		skillDescBg = new Image("res/miscImages/initEmptyPic.png");
 
 		
 		playButton = new Image("res/buttons/playButtons.png");
 	}
 
 	public void enter(GameContainer container, StateBasedGame game)
 	         throws SlickException {
 	      // TODO Auto-generated method stub
 	      super.enter(container, game);
 	      chosenSkills = GlobalClassSelector.getController().getPlayers()[GlobalClassSelector.getController().getActivePlayerIndex()].getSkillList();
 	      
 	      switch(GlobalClassSelector.getController().getPlayers()[GlobalClassSelector.getController().getActivePlayerIndex()].getType()){
 			case "Wizard":
 				classPortrait = new Image("res/classImages/mage_portrait.png");
 				//Init wizard basic, offensive, defensive and mobility skillists
 				
 				basicSkill = new SkillWandattack();
 				
 				offSkills[0] = new SkillFireball();
 				offSkills[1] = new SkillFirestorm();
 				offSkills[2] = new SkillFlamewave();
 				
 				defSkills[0] = new SkillIroncloak();
 				defSkills[1] = new SkillAbsorb();
 				defSkills[2] = new SkillIceblock();
 				
 				mobSkills[0] = new SkillUnstablemagic();
 				mobSkills[1] = new SkillBlizzard();
 				mobSkills[2] = new SkillTeleport();
 			break;
 			case "Hunter":
 				classPortrait = new Image("res/classImages/hunter_portrait.png");
 				//Init Hunter basic, offensive, defensive and mobility skillists
 				
 				basicSkill = new SkillArrow();
 				
 				offSkills[0] = new SkillFlamingArrow();
 				offSkills[1] = new SkillGuidedArrow();
 				offSkills[2] = new SkillArrowFlurry();
 				
 				defSkills[0] = new SkillPassiveDodge();
 				defSkills[1] = new SkillLifestealingArrows();
 				defSkills[2] = new SkillStealth();
 				
 				mobSkills[0] = new SkillSprint();
 				mobSkills[1] = new SkillCripplingShot();
 				mobSkills[2] = new SkillBarrelRoll();
 			break;
 			case "Warrior":
 				classPortrait = new Image("res/classImages/warrior_portrait.png");
 				//Init warrior basic, offensive, defensive and mobility skillists
 				
 				basicSkill = new SkillSlash();
 				
 				offSkills[0] = new SkillThrowingAxe();
 				offSkills[1] = new SkillWarstomp();
 				offSkills[2] = new SkillAdrenaline();
 				
 				defSkills[0] = new SkillImprovedArmor();
 				defSkills[1] = new SkillShieldStance();
 				defSkills[2] = new SkillFirstAid();
 				
 				mobSkills[0] = new SkillIncreasedMovement();
 				mobSkills[1] = new SkillGrapplingHook();
 				mobSkills[2] = new SkillLeapAttack();
 			break;
 	      	}
 
 			//Init skillicons
 			//Offensive
 			firstOffSkill = offSkills[0].getSkillBarImage();
 			secondOffSkill = offSkills[1].getSkillBarImage();
 			thirdOffSkill = offSkills[2].getSkillBarImage();
 			//Defensive
 			firstDefSkill = defSkills[0].getSkillBarImage();
 			secondDefSkill = defSkills[1].getSkillBarImage();
 			thirdDefSkill = defSkills[2].getSkillBarImage();
 			//Mobility
 			firstMobSkill = mobSkills[0].getSkillBarImage();
 			secondMobSkill = mobSkills[1].getSkillBarImage();
 			thirdMobSkill = mobSkills[2].getSkillBarImage();
 			
 
 	   }
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)throws SlickException {
 		g.drawImage(background, 0, 0);
 		g.setColor(Color.black);
 		g.drawImage(skillDescBg, 485, 460);
 		g.setColor(Color.white);
 		g.drawImage(playButton, 1120, 670);
 		//g.drawImage(shopText, 599, 70);
 		g.drawString(skillText, 540, 475);
 		if(chosenSkill != null)
 			g.drawImage(chosenSkill, 470, 470);
 
 		g.drawString(mouse, 900, 10);
 		// TODO Auto-generated method stub
 		
 		g.drawImage(classPortrait, 70, 30);
 		g.drawString(playerGoldText, 140, 190);
 		g.drawImage(playerGold,70,185);
 		g.drawString(GlobalClassSelector.getController().getPlayers()[GlobalClassSelector.getController().getActivePlayerIndex()].getName() + 
 				"\nHP: "+GlobalClassSelector.getController().getPlayers()[GlobalClassSelector.getController().getActivePlayerIndex()].getHP() + 
 				"\nArmor: " + (int)(GlobalClassSelector.getController().getPlayers()[GlobalClassSelector.getController().getActivePlayerIndex()].getArmor()*100) 
 				+ "%\nKills: " + GlobalClassSelector.getController().getPlayers()[GlobalClassSelector.getController().getActivePlayerIndex()].getKills()
 				 , 80 + classPortrait.getWidth(), 20 + classPortrait.getHeight()/2);
 
 		g.drawString("1", 102, 250);
 		g.drawImage(chosenSkills[0].getSkillBarImage(), 70, 275);
 		g.drawString("2", 170, 250);
 		g.drawImage(chosenSkills[1].getSkillBarImage(), 139, 275);
 		g.drawString("3", 239, 250);
 		g.drawImage(chosenSkills[2].getSkillBarImage(), 208, 275);
 		g.drawString("4", 308, 252);
 		g.drawImage(chosenSkills[3].getSkillBarImage(), 277, 275);
 		g.drawString("5", 377, 250);
 		
 		//g.drawImage(skillsText, 200, 350);
 		
 		//Offensive skills
 		g.drawImage(firstOffSkill, 60, 440);
 		g.drawImage(secondOffSkill, 60, 515);
 		g.drawImage(thirdOffSkill, 60, 590);
 			
 		//Defensive skills
 		g.drawImage(firstDefSkill, 200, 440);
 		g.drawImage(secondDefSkill, 200, 515);
 		g.drawImage(thirdDefSkill, 200, 590);
 			
 		//Mobility skills
 		g.drawImage(firstMobSkill, 335, 440);
 		g.drawImage(secondMobSkill, 335, 515);
 		g.drawImage(thirdMobSkill, 335, 590);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)throws SlickException {
 		
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		Input input = gc.getInput();
 		
 		
 
 		if((1120<xPos && xPos<1240) && (670<yPos && yPos<715)){
 			playButton = new Image("res/buttons/ReadyOver.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				playButton = new Image("res/buttons/Ready.png");
 				sbg.enterState(1);
 			}
 		}else{
 			playButton = new Image("res/buttons/Ready.png");
 		}
 		
 		//Handling clicking on offensive skills
 		
 		if((60<xPos && xPos<124) && (440<yPos && yPos<504)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = firstOffSkill;
 				skillText = offSkills[0].getDescription() + "\n" +  offSkills[0].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}else if((60<xPos && xPos<124) && (515<yPos && yPos<579)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = secondOffSkill;
 				skillText = offSkills[1].getDescription() + "\n" +  offSkills[1].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}else if((60<xPos && xPos<124) && (590<yPos && yPos<654)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = thirdOffSkill;
 				skillText = offSkills[2].getDescription() + "\n" +  offSkills[2].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}
 		
 		//Handling clicking on defensive skills
 		
 		else if((200<xPos && xPos<264) && (440<yPos && yPos<504)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = firstDefSkill;
 				skillText = defSkills[0].getDescription() + "\n" + defSkills[0].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}else if((200<xPos && xPos<264) && (515<yPos && yPos<579)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = secondDefSkill;
 				skillText = defSkills[1].getDescription() + "\n" +  defSkills[1].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}else if((60<xPos && xPos<264) && (590<yPos && yPos<654)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = thirdDefSkill;
 				skillText = defSkills[2].getDescription() + "\n" +  defSkills[2].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}
 		
 		//Handling clicking on mobility skills
 		
 		else if((335<xPos && xPos<399) && (440<yPos && yPos<504)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = firstMobSkill;
 				skillText = mobSkills[0].getDescription() + "\n" +  mobSkills[0].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}else if((335<xPos && xPos<399) && (515<yPos && yPos<579)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = secondMobSkill;
 				skillText = mobSkills[1].getDescription() + "\n" +  mobSkills[1].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}else if((335<xPos && xPos<399) && (590<yPos && yPos<654)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				chosenSkill = thirdMobSkill;
 				skillText = mobSkills[2].getDescription() + "\n" +  mobSkills[2].getCost();
 				skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			}
 		}
 	}
 }
 
