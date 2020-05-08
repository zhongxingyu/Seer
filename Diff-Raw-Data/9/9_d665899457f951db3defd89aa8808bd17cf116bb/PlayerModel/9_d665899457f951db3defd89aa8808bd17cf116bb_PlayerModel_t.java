 package Model;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import Control.GameEngine;
 import Model.Obstacles.*;
 import Model.Timers.SkillCheckingTimer;
 
 public class PlayerModel {
 	
 	Player player;
 	Skill[] playerSkills;
 	Skill currentActiveSkill;
 	
 	private long globalAttackCDStart = 0;
 	private long globalAttackCDElapsed = 0;
 	private long globalWalkCDStart = 0;
 	private long globalWalkCDElapsed = 0;
 	
 	public PlayerModel(Player player){
 		
 		this.player = player;
 		
 		playerSkills = player.getSkillList();
 		currentActiveSkill = playerSkills[0];
 	}
 	
 	// Getters
 	public Player getPlayer(){
 		return player;
 	}
 	public Skill getCurrentActiveSkill(){
 		return currentActiveSkill;
 	}
 	private SkillCheckingTimer getRelevantSCT(Skill skill){
 		ArrayList<SkillCheckingTimer> SCTArray = skill.getSCTArray();
 		SkillCheckingTimer SCT = null;
 		int index = -1;
 		for(int j=0; j<SCTArray.size(); j++){
 			// Changed == to .equals
 			if(SCTArray.get(j) != null && SCTArray.get(j).getPlayerName().equals(player.getName())){
 				index = j;
 				SCT = SCTArray.get(j);
 			}
 		}
 		if(index == -1){
 			SCT = skill.addNewSkillCheckingTimer(player);
 		}
 		
 		return SCT;
 	}
 	private SkillCheckingTimer getRelevantSCT(Obstacle obstacle, Skill skill){
 		ArrayList<SkillCheckingTimer> SCTArray = skill.getSCTArray();
 		SkillCheckingTimer SCT = null;
 		int index = -1;
 		for(int j=0; j<SCTArray.size(); j++){
 			if(SCTArray.get(j) != null && SCTArray.get(j).getObstacle() == obstacle){
 				index = j;
 				SCT = SCTArray.get(j);
 			}
 		}
 		if(index == -1){
 			SCT = skill.addNewSkillCheckingTimer(obstacle);
 		}
 		return SCT;
 	}
 	
 	// Setters
 	public void setCurrentActiveSkill(int index){
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null){
 				if(i == index){
 					currentActiveSkill = playerSkills[i];
 					playerSkills[i].setChosenState(true);
 					player.setCurrentActiveSkillIndex(i);
 					//Using attack instantly if the range is 0
 					if(playerSkills[i].getRange() == 0){
 						attack((int)player.getX(), (int)player.getY());
 					}
 				}else{
 					playerSkills[i].setChosenState(false);
 				}
 			}
 		}
 	}
 
 	// Misc methods
 	public void resetGlobalAttackTimer(){
 		globalAttackCDStart = System.currentTimeMillis();
 	}
 	public long checkGlobalAttackCooldown(){
 		globalAttackCDElapsed = System.currentTimeMillis() - globalAttackCDStart;
 		if(globalAttackCDElapsed >= 1000){
 			return 0;
 			
 		}
 		return (1000-globalAttackCDElapsed);
 	}
 	
 	public void resetGlobalWalkTimer(){
 		globalWalkCDStart = System.currentTimeMillis();
 	}
 	
 	public long checkGlobalWalkCooldown(){
 		globalWalkCDElapsed = System.currentTimeMillis() - globalWalkCDStart;
 		if(globalWalkCDElapsed >= 100){
 			player.setCanWalkState(true);
 			return 0;
 			
 		}
 		return (100-globalWalkCDElapsed);
 	}
 	
 	public void killPlayer(){
 		player.setAliveState(false);
 		player.setX(1000);
 		player.setY(-1000);
 		player.setRunningState(false);
 		player.resetStatusEffects();
 	}
 	
 	public void ressurectPlayer() throws SlickException{
 		player.setAliveState(true);
 		player.setStunState(false);
 		player.setPushState(false);
 		player.resetStatusEffects();
 		player.setX(player.getStartX());
 		player.setY(player.getStartY());
 		checkPlayerObstacleCollision(0, 0);
 		player.resetHP();
 		resetGlobalAttackTimer();
 		resetGlobalWalkTimer();
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null){
 				playerSkills[i].resetCooldown();
 				playerSkills[i].collidedShot();
 			}
 		}
 	}
 	public void fixSpawn(){
 		player.setX(player.getStartX());
 		player.setY(player.getStartY());
 		move((int)player.getX()+1,(int)player.getY()+1);
 	}
 	
 	public void isRunning() throws SlickException{
 		checkPlayerObstacleCollision((float)(player.getXDirMove()*player.getMovementSpeed()), (float)(player.getYDirMove()*player.getMovementSpeed()));
 		
 		if(player.getAliveState() && !player.getPushState() && !player.getStunState() && player.getMovementSpeed() > 0/* && !collided*/){
 			player.addX((float)(player.getXDirMove()*player.getMovementSpeed()));
 			player.addY((float)(player.getYDirMove()*player.getMovementSpeed()));
 			player.incMoveCounter();
 			if(player.getMoveCounter()*player.getMovementSpeed() >= player.getGenDirMove())
 				player.setRunningState(false);
 			
 		}else if(player.getAliveState() && player.getPushState() && player.getMovementSpeed() > 0/* && !collided*/){
 			
 			double tempSpeed = player.getPushSpeed();
 			double calculateDecision = tempSpeed*player.getMoveCounter();
 			if(calculateDecision>player.getGenDirMove()*0.5){
 				tempSpeed *= 0.7;
 				if(calculateDecision>player.getGenDirMove()*0.7){
 					tempSpeed *= 0.7;
 					if(calculateDecision>player.getGenDirMove()*0.9){
 						tempSpeed *= 0.7;
 					}
 				}
 				
 			}else if(calculateDecision<player.getGenDirMove()*0.3){
 				tempSpeed *= 1.5;
 			}
 			
 			
 			player.addX((float)(player.getXDirMove()*tempSpeed));
 			player.addY((float)(player.getYDirMove()*tempSpeed));
 			
 			player.incMoveCounter();
 			if(player.getMoveCounter()*player.getPushSpeed() >= player.getGenDirMove()){
 				player.setRunningState(false);
 				player.setPushState(false);
 			}
 		}else{
 			player.setRunningState(false);
 		}
 	}
 	
 	//Determines action depending on what state the skill is in
 	public void isAttacking(Skill attackingSkill) throws SlickException{
 		if(attackingSkill != null){
 			boolean collided = checkSkillObstacleCollision(attackingSkill, (float)(attackingSkill.getXDirAtt()*attackingSkill.getAttSpeed()), (float)(attackingSkill.getYDirAtt()*attackingSkill.getAttSpeed()));
 			
 			if(!attackingSkill.getEndState() && attackingSkill.isProjectile()){
 				
 				//Calculates the new direction if the skill is guided
 				if(attackingSkill.getGuided()){
 					if(attackingSkill.getGuidedTarget() != null){
 						if(attackingSkill.getGuidedTarget().getStealthState() || !stillAbleToMakeAction(attackingSkill.getGuidedTarget())){
 							attackingSkill.setGuidedTarget(null);
 						}
 						
 					}
 					if(attackingSkill.getGuidedTarget() != null){
 						float xDir = attackingSkill.getGuidedTarget().getX()-attackingSkill.getAttX();
 						float yDir = attackingSkill.getGuidedTarget().getY()-attackingSkill.getAttY();
 						float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 						
 						attackingSkill.setXDirAtt(xDir/genDir);
 						attackingSkill.setYDirAtt(yDir/genDir);
 						double rotation = Math.toDegrees(Math.atan2((yDir),(xDir)));
 						attackingSkill.setRotation(90 + (float)rotation);
 					}
 					
 				}
 				//determines which x and y the skill will have in the next render session
 				attackingSkill.addAttX((float)(attackingSkill.getXDirAtt()*attackingSkill.getAttSpeed()));
 				attackingSkill.addAttY((float)(attackingSkill.getYDirAtt()*attackingSkill.getAttSpeed()));
 				
 				attackingSkill.incAttCounter();
 				
 				if(attackingSkill.getProjectileAnimationTimer() != null){
 					Image projectileImage = attackingSkill.getProjectileAnimationTimer().getCurrentAnimationImage();
 				
 					if(projectileImage != null)
 						attackingSkill.setImage(projectileImage);
 					
 					attackingSkill.setRotation(attackingSkill.getRotation());
 				}
 				
 				if(attackingSkill.getAttCounter()*attackingSkill.getAttSpeed() >= attackingSkill.getGenDirAtt() || collided){
 					if(attackingSkill.getAffectSelfOnHit() && !attackingSkill.getSelfAffectingOnHitStatusEffect().hasBeenGivenTo(player.getName())){
 						player.addStatusEffect(attackingSkill.getSelfAffectingOnHitStatusEffect().createStatusEffectTo(player, null));
 					}
 					if(!attackingSkill.getHasEndState()){
 						attackingSkill.setAttackingState(false);
 						if(attackingSkill.getPiercing()){
 							attackingSkill.resetTargetsHit();
 						}
 					}else{
 						attackingSkill.activateEndState();
 					}
 				}
 				
 			}else if(!attackingSkill.getEndState() && !attackingSkill.isProjectile()){
 				attackingSkill.activateEndState();			
 			}else if(attackingSkill.getEndState()){
 				if(!player.getChannel() && attackingSkill.getSelfAffectingStatusEffect() != null 
 						&& attackingSkill.getSelfAffectingStatusEffect().getChannel()){
 					attackingSkill.setAttackingState(false);
 				}
 				if(attackingSkill.getAnimationTimer() != null){
 					Image animationImage = attackingSkill.getAnimationTimer().getCurrentAnimationImage();
					//Moved this down to the if below
					//animationImage.setRotation(attackingSkill.getRotation());
 				
					if(animationImage != null) {
						animationImage.setRotation(attackingSkill.getRotation());
 						attackingSkill.setEndStateImage(animationImage);
					}
 				}
 			}
 		}
 	}
 	
 	public boolean isColliding(Skill skill) throws SlickException{
 
 		if(skill.getAttX() <= player.getX() && skill.getAttX()+skill.getCurrentWidth() >= player.getX()){
 			if(skill.getAttY() >= player.getY() && skill.getAttY() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY()+skill.getCurrentHeight() >= player.getY() && skill.getAttY()+skill.getCurrentHeight() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY() <= player.getY() && skill.getAttY()+skill.getCurrentHeight() >= player.getY()+player.getImage().getHeight() ){
 				return true;
 			}
 		}else if(skill.getAttY() <= player.getY() && skill.getAttY()+skill.getCurrentHeight() >= player.getY()){
 			if(skill.getAttX() >= player.getX() && skill.getAttX() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX()+skill.getCurrentWidth() >= player.getX() && skill.getAttX()+skill.getCurrentWidth() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX() <= player.getX() && skill.getAttX()+skill.getCurrentWidth() >= player.getX()+player.getImage().getWidth() ){
 				return true;
 			}
 		}else if(skill.getAttX() <= player.getX()+player.getImage().getWidth()  && skill.getAttX()+skill.getCurrentWidth() >= player.getX()+player.getImage().getWidth() ){
 			if(skill.getAttY() >= player.getY() && skill.getAttY() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY()+skill.getCurrentHeight() >= player.getY() && skill.getAttY()+skill.getCurrentHeight() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY() <= player.getY() && skill.getAttY()+skill.getCurrentHeight() >= player.getY()+player.getImage().getHeight() ){
 				return true;
 			}
 		}else if(skill.getAttY() <= player.getY()+player.getImage().getHeight()  && skill.getAttY()+skill.getCurrentHeight() >= player.getY()+player.getImage().getHeight() ){
 			if(skill.getAttX() >= player.getX() && skill.getAttX() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX()+skill.getCurrentWidth() >= player.getX() && skill.getAttX()+skill.getCurrentWidth() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX() <= player.getX() && skill.getAttX()+skill.getCurrentWidth() >= player.getX()+player.getImage().getWidth() ){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	
 	
 	public void move(int x, int y){
 		if(checkGlobalWalkCooldown() == 0 && stillAbleToMakeAction(player)){
 			checkAndCorrectChannel();
 			if(!player.getPushState()){
 				
 				rotate(x, y);
 				
 				float xDir = x - player.getX();
 				float yDir = y - player.getY();
 				float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 				
 				Double findNaN = (double)genDir;
 				if(!findNaN.isNaN() && !findNaN.isInfinite()){
 
 					player.setGenDirMove(genDir);
 					player.setXDirMove(xDir/player.getGenDirMove());
 					player.setYDirMove(yDir/player.getGenDirMove());
 					
 					player.resetMoveCounter();
 					player.setRunningState(true);
 				}else{
 					System.out.println("NaN BUG");
 					fixSpawn();
 				}
 			}
 			resetGlobalWalkTimer();
 		}
 	}
 	
 	public void attack(int x, int y){
 		currentActiveSkill = player.getSkillList()[player.getCurrentActiveSkillIndex()];
 		if(currentActiveSkill != null && checkGlobalAttackCooldown() == 0 && stillAbleToMakeAction(player) && 
 				!currentActiveSkill.getPassive() && currentActiveSkill.checkCooldown() == currentActiveSkill.getCoolDown()){
 			checkAndCorrectChannel();
 			checkAndCorrectStealth();
 			if(currentActiveSkill.getGuided()){
 				findAndSetGuidedTarget(currentActiveSkill);
 			}
 
 			if(currentActiveSkill.getGrapplingHook()){
 				
 			}
 			//Rotating to where the middle of the mouse click is
 			if(currentActiveSkill.getRange() != 0)
 				rotate(x, y);
 			//Setting x and y to make the center of the skill appear in the center of the mouse click
 			x -= currentActiveSkill.getCurrentWidth()/2;
 			y -= currentActiveSkill.getCurrentHeight()/2;
 		
 			currentActiveSkill.activateSkill();
 			currentActiveSkill.resetShot(player);
 			
 			float xDir = x - currentActiveSkill.getAttX();
 			float yDir = y - currentActiveSkill.getAttY();
 			float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 			
 			
 			Double findNaN = (double)genDir;
 			if(!findNaN.isNaN() && !findNaN.isInfinite()){
 			
 				currentActiveSkill.setRotation(player.getRotation());
 				
 				currentActiveSkill.setGenDirAtt(genDir);
 				currentActiveSkill.setXDirAtt(xDir/currentActiveSkill.getGenDirAtt());
 				currentActiveSkill.setYDirAtt(yDir/currentActiveSkill.getGenDirAtt());
 				
 				if(currentActiveSkill.getGenDirAtt() > currentActiveSkill.getAttackRange()){
 					currentActiveSkill.setGenDirAtt(currentActiveSkill.getAttackRange());
 				}
 				currentActiveSkill.resetAttCounter();
 				if(!currentActiveSkill.isProjectile()){
 					currentActiveSkill.setNonProjectileShot();
 				}
 				currentActiveSkill.setAttackingState(true);
 				
 				currentActiveSkill.setMouseXPos(player.getX()+currentActiveSkill.getXDirAtt()*currentActiveSkill.getGenDirAtt());
 				currentActiveSkill.setMouseYPos(player.getY()+currentActiveSkill.getYDirAtt()*currentActiveSkill.getGenDirAtt());
 				
 			}
 				
 			if(currentActiveSkill.getAffectSelf()){
 				if(currentActiveSkill.getSelfAffectingStatusEffect() != null/* && !currentActiveSkill.getSelfAffectingStatusEffect().hasBeenGivenTo(player.getName())*/){
 					player.addStatusEffect(currentActiveSkill.getSelfAffectingStatusEffect().createStatusEffectTo(player, null));
 				}
 			}
 			resetGlobalAttackTimer();
 		}
 	}
 
 	public void rotate(int x, int y){
 		if(!player.getStunState()){
 			player.setMouseXPosMove(x);
 			player.setMouseYPosMove(y);
 			double rotation = Math.toDegrees(Math.atan2((player.getMouseYPosMove()-player.getY()),(player.getMouseXPosMove()-player.getX())));
 			player.setRotation(90 + (float)rotation);
 		}
 	}
 
 	public void checkCollision(Player attackingPlayer, Skill[] playerSkills) throws SlickException{
 		if(player.getAliveState()){
 			for(int i=0; i<playerSkills.length; i++){
 				Skill skill = playerSkills[i];
 				if(skill != null && isColliding(skill)){
 					int evasion = player.getEvasion();
 					//Calculates new evasion to check if player will evade the attack in this state
 					if(evasion>=0){
 						Random generator = new Random();
 						evasion = generator.nextInt(100) - evasion;
 					}
 					//Checks if collided skill has a statusEffect and adds it to the player it hit
 					if(skill.getOffensiveStatusEffect() != null && !skill.getOffensiveStatusEffect().hasBeenGivenTo(player.getName()) 
 							&& playerSkills[i].getAffectOthers() && evasion > 0){
 						player.addStatusEffect(skill.getOffensiveStatusEffect().createStatusEffectTo(player, attackingPlayer));
 					}
 					
 					if(!skill.getEndState()){
 						if(evasion>=0 && !skill.getPiercing()){
 							pushPlayer(skill.getXDirAtt(), skill.getYDirAtt(), skill.getPushDistance());
 						}
 						if(!skill.getHasEndState()){
 							//Piercing skills can not have end state
 							boolean canHitTarget = true;
 							if(!skill.getPiercing()){
 								skill.setAttackingState(false);
 								skill.collidedShot();
 							}else{
 								canHitTarget = false;
 								if(!skill.getTargetsHit(player.getPlayerListIndex())){
 									canHitTarget = true;
 									skill.addTargetHit(player.getPlayerListIndex());
 								}
 							}
 							if(evasion>=0 && canHitTarget){
 							//	player.dealDamage(skill.getDamage());
 							//	attackingPlayer.incRoundDamageDone((int)(skill.getDamage()*(1-player.getArmor())));
 								playerHit(attackingPlayer, skill);
 							}
 						}else{
 							if(evasion>=0){
 						//		player.dealDamage(skill.getDamage());
 						//		attackingPlayer.incRoundDamageDone((int)(skill.getDamage()*(1-player.getArmor())));
 								playerHit(attackingPlayer, skill);
 							}
 							skill.activateCollisionEndState();
 						}
 					}else{
 						
 						SkillCheckingTimer SCT = getRelevantSCT(skill);
 						if(SCT != null && SCT.checkESColTimer() == skill.getESColInterval()){
 							if(evasion>=0){
 								//player.dealDamage(skill.getDamage());
 								//attackingPlayer.incRoundDamageDone((int)(skill.getDamage()*(1-player.getArmor())));
 								playerHit(attackingPlayer, skill);
 								skill.resetOffensiveStatusGivenTo();
 							}
 							SCT.resetESColTimer();
 						}
 					}
 				}
 			}
 		}
 		if(player.getHP() <= 0){
 			attackingPlayer.incKillsThisRound();
 			killPlayer();
 			player.incDeaths();
 		}
 	}
 	
 	private void playerHit(Player attackingPlayer, Skill attackingSkill){
 		player.dealDamage(attackingSkill.getDamage());
 		attackingPlayer.incRoundDamageDone((int)(attackingSkill.getDamage()*(1-player.getArmor())));
 		if(attackingSkill.getAffectSelfOnHit())
 			System.out.println(attackingSkill.getSelfAffectingOnHitStatusEffect().hasBeenGivenTo(player.getName()));
 		if(attackingSkill.getAffectSelfOnHit() && !attackingSkill.getSelfAffectingOnHitStatusEffect().hasBeenGivenTo(player.getName())){
 			player.addStatusEffect(attackingSkill.getSelfAffectingOnHitStatusEffect().createStatusEffectTo(attackingPlayer, player));
 		}
 	}
 	
 	public boolean checkPlayerObstacleCollision(float x, float y) throws SlickException{
 		for(int i=0; i<MainHub.getController().getMapSelected().getObstacles().length; i++){
 			Obstacle currentObstacleCheck = MainHub.getController().getMapSelected().getObstacles()[i];
 			if(currentObstacleCheck != null && isCollidingWithObstacle(currentObstacleCheck, player.getX()+x, player.getY()+y, player.getImage().getWidth(), player.getImage().getHeight())){
 				
 				if(currentObstacleCheck.isSolid()){
 					player.setPushState(false);
 					if(currentObstacleCheck.getDamage() > 0){
 						player.dealDamage(currentObstacleCheck.getDamage());
 						pushPlayer(player.getXDirMove(), player.getYDirMove(), -20);
 						if(player.getHP() <= 0){
 							killPlayer();
 							player.incDeaths();
 						}
 					}
 					fixSpawnCollision(currentObstacleCheck);
 				}				
 				return true;
 
 			}
 		}
 		return false;
 	}
 	public boolean checkSkillObstacleCollision(Skill skill, float x, float y) throws SlickException{
 		boolean collision = false;
 		for(int i=0; i<MainHub.getController().getMapSelected().getObstacles().length; i++){
 			Obstacle currentObstacleCheck = MainHub.getController().getMapSelected().getObstacles()[i];
 			if(currentObstacleCheck != null && currentObstacleCheck.isSolid() && isCollidingWithObstacle(currentObstacleCheck, skill.getAttX()+x, skill.getAttY()+y, skill.getCurrentWidth(), skill.getCurrentHeight())){
 				if(!skill.getEndState()){
 					currentObstacleCheck.takeDamage(skill.getDamage());
 				}else{
 					SkillCheckingTimer SCT = getRelevantSCT(currentObstacleCheck, skill);
 					if(SCT != null && SCT.checkESColTimer() == skill.getESColInterval()){
 						currentObstacleCheck.takeDamage(skill.getDamage());
 						
 						SCT.resetESColTimer();
 					}
 				}
 				if(currentObstacleCheck.getHealth()<=0){
 					if(currentObstacleCheck.getType() == "Explosive Barrel"){
 						MainHub.getController().getMapSelected().addObstacle(new ObstacleCrater("Crater", 0, 50000, currentObstacleCheck.getX() - currentObstacleCheck.getCurrentWidth(), currentObstacleCheck.getY() - currentObstacleCheck.getCurrentHeight(), false));
 					}
 					currentObstacleCheck = null;
 					MainHub.getController().getMapSelected().removeObstacle(i);
 					//obstacles[i] = null;
 				}
 				collision = true;
 			}
 		}
 		return collision;
 	}
 	
 	public boolean isCollidingWithObstacle(Obstacle obstacle, float x, float y, int width, int height) throws SlickException{
 
 		if(obstacle.getX() <= x && obstacle.getX()+obstacle.getCurrentWidth() >= x){
 			if(obstacle.getY() >= y && obstacle.getY() <= y+height 
 					|| obstacle.getY()+obstacle.getCurrentHeight() >= y && obstacle.getY()+obstacle.getCurrentHeight() <= y+height 
 					|| obstacle.getY() <= y && obstacle.getY()+obstacle.getCurrentHeight() >= y+height ){
 				return true;
 			}
 		}else if(obstacle.getY() <= y && obstacle.getY()+obstacle.getCurrentHeight() >= y){
 			if(obstacle.getX() >= x && obstacle.getX() <= x+width 
 					|| obstacle.getX()+obstacle.getCurrentWidth() >= x && obstacle.getX()+obstacle.getCurrentWidth() <= x+width 
 					|| obstacle.getX() <= x && obstacle.getX()+obstacle.getCurrentWidth() >= x+width ){
 				return true;
 			}
 		}else if(obstacle.getX() <= x+width  && obstacle.getX()+obstacle.getCurrentWidth() >= x+width ){
 			if(obstacle.getY() >= y && obstacle.getY() <= y+height 
 					|| obstacle.getY()+obstacle.getCurrentHeight() >= y && obstacle.getY()+obstacle.getCurrentHeight() <= y+height 
 					|| obstacle.getY() <= y && obstacle.getY()+obstacle.getCurrentHeight() >= y+height ){
 				return true;
 			}
 		}else if(obstacle.getY() <= y+height  && obstacle.getY()+obstacle.getCurrentHeight() >= y+height ){
 			if(obstacle.getX() >= x && obstacle.getX() <= x+width 
 					|| obstacle.getX()+obstacle.getCurrentWidth() >= x && obstacle.getX()+obstacle.getCurrentWidth() <= x+width 
 					|| obstacle.getX() <= x && obstacle.getX()+obstacle.getCurrentWidth() >= x+width ){
 				return true;
 			}
 		}else if(x <= obstacle.getX() && x+width >= obstacle.getX()+obstacle.getCurrentWidth()
 				&& y <= obstacle.getY() && y+width >= obstacle.getY()+obstacle.getCurrentHeight()){
 			return true;
 		}
 		return false;
 	}
 	
 	public void fixSpawnCollision(Obstacle obstacle) throws SlickException{
 		int N=0,S=0,W=0,E=0;
 		if(MainHub.getController().getMapSelected().getObstacles() != null){
 			while(isCollidingWithObstacle(obstacle, player.getX()+E, player.getY(), player.getImage().getWidth(), player.getImage().getHeight())){
 				E++;
 			}
 			while(isCollidingWithObstacle(obstacle, player.getX()-W, player.getY(), player.getImage().getWidth(), player.getImage().getHeight())){
 				W++;
 			}
 			while(isCollidingWithObstacle(obstacle, player.getX(), player.getY()+S, player.getImage().getWidth(), player.getImage().getHeight())){
 				S++;
 			}
 			while(isCollidingWithObstacle(obstacle, player.getX(), player.getY()-N, player.getImage().getWidth(), player.getImage().getHeight())){
 				N++;
 			}
 			
 			if(N<E && N<S && N<W){
 				player.addY(-N);
 			}else if(S<E && S<N && S<W){
 				player.addY(S);
 			}else if(E<N && E<S && E<W){
 				player.addX(E);
 			}else if(W<E && W<S && W<N){
 				player.addX(-W);
 			}
 		}
 	}
 	
 	
 	int changeRate = 10;
 	public void checkUserImageChange(){
 		player.changeUserImage();
 		changeRate = 20;
 		changeRate--;
 	}
 	
 	
 	public void checkStatusEffects(){
 		
 		//Goes through all the current StatusEffects player has
 		for(int i=0; i<player.getStatusEffects().size(); i++){
 			StatusEffect currentStatusEffect = player.getStatusEffects().get(i);
 			
 			//Checks and makes the effect if timer has not run out. If it has it will return false and remove the statusEffect from player
 			if(!currentStatusEffect.checkStatusEffect()){
 				player.removeStatusEffect(currentStatusEffect);
 				if(currentStatusEffect.hasStun()){
 					boolean stunDecision = false;
 					for(int j=0; j<player.getStatusEffects().size(); j++){
 						StatusEffect stunCheckSE = player.getStatusEffects().get(j);
 						if(stunCheckSE.hasStun()){
 							stunDecision = true;
 						}
 						player.setStunState(stunDecision);
 					}
 				}
 			}
 		}
 	}
 
 	public void pushPlayer(float xDir, float yDir, int pushRange){
 		//Aborts channel if player is channeling
 		checkAndCorrectChannel();
 		
 		float x = player.getX()+xDir*pushRange;
 		float y = player.getY()+yDir*pushRange;
 		
 		player.setMouseXPosMove(x);
 		player.setMouseYPosMove(y);
 		
 		xDir = x - player.getX();
 		yDir = y - player.getY();
 		
 		float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 		
 		Double findNaN = (double)genDir;
 		if(!findNaN.isNaN() && !findNaN.isInfinite()){
 			player.setXDirMove(xDir);
 			player.setYDirMove(yDir);
 			player.setGenDirMove(genDir);
 			player.setXDirMove(player.getXDirMove()/player.getGenDirMove());
 			player.setYDirMove(player.getYDirMove()/player.getGenDirMove());
 			
 			player.resetMoveCounter();
 			player.setRunningState(true);
 			player.setPushState(true);
 		}
 	}
 	
 	
 	/**
 	 * Finds the enemy closest enemy to where the player used the attack and sets him as the target for the guided attack
 	 * @param skill which is the skill that will have a target assigned to it
 	 */
 	public void findAndSetGuidedTarget(Skill skill){
 		Player[] guidedPlayers = MainHub.getController().getPlayers();
 		int targetPlayer = player.getPlayerListIndex() != 0 ? 0 : 1;
 		float targetPlayerXDir = guidedPlayers[targetPlayer].getX() - player.getX();
 		float targetPlayerYDir = guidedPlayers[targetPlayer].getY() - player.getY();
 		float targetPlayerGenDir = (float)Math.sqrt(targetPlayerXDir*targetPlayerXDir+targetPlayerYDir*targetPlayerYDir);
 		
 		for(int i=targetPlayer+1; i<guidedPlayers.length; i++){
 			if(guidedPlayers[i] != null && player.getPlayerListIndex() != i){
 				float compXDir = guidedPlayers[i].getX() - player.getX();
 				float compYDir = guidedPlayers[i].getY() - player.getY();
 				float compGenDir = (float)Math.sqrt(compXDir*compXDir+compYDir*compYDir);
 				
 				if(compGenDir<targetPlayerGenDir && !guidedPlayers[i].getStealthState()){
 					targetPlayer = i;
 					targetPlayerXDir = compXDir;
 					targetPlayerYDir = compYDir;
 					targetPlayerGenDir = compGenDir;
 				}
 			}
 		}
 		skill.setGuidedTarget(guidedPlayers[targetPlayer]);
 	}
 	
 	private boolean stillAbleToMakeAction(Player player){
 		if(player != null)
 			if(player.getX() < 0-MainHub.noActionLimit || player.getY() < 0-MainHub.noActionLimit || 
 					player.getX() > GameEngine.screenWidth+MainHub.noActionLimit || player.getY() > GameEngine.screenHeight+MainHub.noActionLimit ||
 					!player.getAliveState() || player.getStunState()){
 				return false;
 			}else{
 				return true;
 		}
 		return false;
 	}
 	private void checkAndCorrectChannel(){
 		if(player.getChannel()){
 			for(int i=0; i<player.getStatusEffects().size();i++){
 				if(player.getStatusEffects().get(i).getChanneling()){
 					player.getStatusEffects().get(i).setResetOfStatusEffect();
 					player.removeStatusEffect(player.getStatusEffects().get(i));
 				}
 			}
 		}
 	}
 	private void checkAndCorrectStealth(){
 		if(player.getStealthState()){
 			for(int i=0; i<player.getStatusEffects().size();i++){
 				if(player.getStatusEffects().get(i).hasStealth()){
 					player.getStatusEffects().get(i).setResetOfStatusEffect();
 					player.removeStatusEffect(player.getStatusEffects().get(i));
 				}
 			}
 		}
 	}
 }
