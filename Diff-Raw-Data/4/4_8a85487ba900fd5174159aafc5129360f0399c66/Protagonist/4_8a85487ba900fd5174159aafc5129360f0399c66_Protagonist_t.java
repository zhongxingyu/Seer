 package com.example.pineapple;
 
 import java.util.*;
 
 import android.util.Log;
 
 public class Protagonist {
 
 	private static final String TAG = Protagonist.class.getSimpleName();
 	private final double slopeThreshold = 0.7; //How much slope it takes to move the protagonist
 	private double xPos;
 	private double yPos;
 	private double xVel;
 	private double yVel;
 	private double xAcc;
 	private double yAcc;
 	private double health;
 	private double angleAim;
 	private double jumpVel = -6.5;
 	private double jumpAcc = 0.4;
 	private double maxSpeed = 3;
 	private double slideCoefficient = 0.8;
 	private final int height = 15;
 	private final int width = (int)(height/1.5); //Change 1.42 to ratio of bitmap
 	private boolean touchingGround;
 	private GamePanel gp;
 	private int stepCount;
 	private final int numberOfSteps = 10;
 	private int breathCount = 0;
 	private final int breathMax = 20;
 	private boolean facingRight = true;
 	private boolean overPlatform = false;
 	private boolean invincible;
 	private int invincibilityCount;
 	private final int maxInvincibilityCount = 25;
 	private boolean readyToJump = false;
 	private boolean dashBonus = false; //The dash will do damage if true
 	private boolean dashing = false; //Means that the protagonist is dashing
 	private int platformNumber = -1;
 	private boolean dead = false;
 	private boolean sliding = false;
 	private double startHeight;
 	//------------------------------------------------------------------------------------------------//
 	// CONSTRUCTORS
 	public Protagonist(double i, double j, GamePanel gp) {
 		this.setXPos(i);
 		this.setYPos(j);
 		this.health = 1;
 		this.gp = gp;
 		this.stepCount = 0;
 		Log.d(TAG, "Me");
 
 	}
 
 	public Protagonist(double i, double j) {
 		this.setXPos(i);
 		this.setYPos(j);
 		this.stepCount = 0;
 		Log.d(TAG, "Me");
 
 	}
 	//------------------------------------------------------------------------------------------------//
 	//HOW TO MAKE PROTAGONIST MOVE
 	//Moving protagonist
 	public void move() {
 		this.setXPos(this.getXPos() + this.getXVel());
 		this.setYPos(this.getYPos() + this.getYVel());
 	}
 
 	//Accelerating protagonist
 	public void accelerate(double acc) { // acc = 0.2?
 		this.setXVel(this.getXVel() + acc);
 		if(Math.abs(this.getXVel()) > this.getMaxSpeed() && this.getXVel() > 0) {//Double code, also in checkSlope
 			this.setXVel(this.getMaxSpeed());
 		} else if (Math.abs(this.getXVel()) > this.getMaxSpeed() && this.getXVel() < 0) {
 			this.setXVel(-this.getMaxSpeed());
 		}
 	}
 
 	//Deaccelerate protagonist (if stick is not pointed)
 	public void slowDown() {
 		this.setXVel(this.getXVel()*slideCoefficient);
 	}
 
 	//Make action from stickAngle
 	public void handleLeftStick(double angle, double acc) {
 		if (angle <= 45 || angle >= 315) {
 			this.accelerate(acc);
 			step(1);
 		} else if (angle >= 135 && angle <= 225) {
 			this.accelerate(-acc);
 			step(1);
 		} else if (angle > 45 && angle < 135 && this.isTouchingGround()) {
 			if(readyToJump) //If the protagonist isn't standing in a steep slope
 				this.jump();
 		} else if (angle > 225 && angle < 315) {
 			if(!touchingGround){
 				this.down(gp.getGround(), gp.getPlatforms());
 			} else {
 				this.slowDown();
 				this.setStepCount(0);
 			}
 		}
 	}
 
 	//------------------------------------------------------------------------------------------------//
 	//ACTIONS
 	//Protagonist is aiming
 	public void aim(double angle) {
 		if(sliding){
 			if(getXVel() > 0){
 				if(angle > 90 && angle < 180){
 					angleAim = 90;
 				} else if(angle >= 180 && angle < 270){
 					angleAim = 270;
 				} else {
 					angleAim = angle;
 				}
 			} else {
 				if(angle < 90){
 					angleAim = 90;
 				} else if(angle > 270){
 					angleAim = 270;
 				} else {
 					angleAim = angle;
 				}
 			}
 		} else {
 			angleAim = angle;
 		}
 
 	}
 
 	//Protagonist lose health
 	public double reduceHealth(double damage) {
 		this.setHealth(this.getHealth()-damage);
 		return this.health;
 	}
 
 	//Protagonist jump
 	public void jump() {
 		if(readyToJump){
 			touchingGround = false;
 			this.setYVel(this.getYVel() + this.getJumpVel() + this.getJumpAcc());
 			Log.d(TAG, "Jump!!");
 			readyToJump = false;
 		}
 	}
 
 	//Method set dashingGround or dashingPlatform depending on position of protagonist 
 	//and calculate bonus or not
 	public void down(Ground g, ArrayList<Platform> platforms) {
 		if(!dashing){
 			dashing = true;
 			startHeight = this.getYPos();
 			this.checkOverPlatform(platforms); // Can perhaps be removed later
 			if(platformNumber >= 0){
 				Log.d(TAG, "Coming down 2 u!! #onPlatform");
 				//Check if protagonist get dash bonus (if he is high enough)
 				if(platforms.get(platformNumber).getUpperYFromX(this.getXPos()) - startHeight > 2*this.getHeight()) {
 					dashBonus = true;
 					invincible = true;
 					Log.d(TAG, "DASH!!");
 				} 
 			} else { //Over ground
 				Log.d(TAG, "Coming down 2 u!! #hitGround");
 				//Check if protagonist get dash bonus
 				if(g.getYFromX(this.getXPos()) - startHeight > 2*this.getHeight()) {
 					invincible = true;
 					dashBonus = true;
 					Log.d(TAG, "DASH!!");
 				}
 			}
 		}
 	}
 
 	//Method take protagonist to platform alt ground by acceleration (dashing)
 	public void dashing(Ground g, ArrayList<Platform> platforms){
 		if(dashing){
 			//If dashing above platform
 			if(platformNumber >= 0){
 				//Check if protagonist would pass platform in a frame, if yes set protagonist on platform
 				if(this.getYPos() + this.getHeight()/2 + this.getYVel() > platforms.get(platformNumber).getUpperYFromX(this.getXPos())){
 					this.setYAcc(0);
 					this.setYVel(0);
 					this.setYPos(platforms.get(platformNumber).getUpperYFromX(this.getXPos()) - this.getHeight()/2);
 					touchingGround = true;
 					if(dashBonus){
 						gp.sendEnemiesFlying();
 					}
 					dashing = false;
 					dashBonus = false;
 				} else { //Still above platform
 					this.setYAcc(15); //Set constant
 					this.setYVel(this.getYAcc() + this.getYVel());
 				}
 				//If dashing above ground
 			} else {
 				if(this.getYPos() + this.getHeight()/2 + this.getYVel() > g.getYFromX(this.getXPos())){
 					this.setYAcc(0);
 					this.setYVel(0);
 					this.setYPos(g.getYFromX(this.getXPos()) - this.getHeight()/2);
 					touchingGround = true;
 					if(dashBonus){
 						gp.sendEnemiesFlying();
 					}
 					dashing = false;
 					dashBonus = false;
 				} else {
 					this.setYAcc(15); //Set constant
 					this.setYVel(this.getYAcc() + this.getYVel());
 				}
 				//Let checkGround handle the y position
 			}
 		}
 	}		
 
 	//------------------------------------------------------------------------------------------------//
 	//OTHER PROPERTIES
 	//Let gravity work on protagonist
 	public void gravity(){
 		this.setYVel(this.getYVel()+this.getJumpAcc());
 	}
 
 	//Keeps track of the protagonist's invincibility when damaged or dashing
 	public void invincibility(){
 		if(invincible){
 			invincibilityCount++;
 			if(invincibilityCount >= maxInvincibilityCount ){
 				invincible = false;
 				invincibilityCount = 0;
 			}
 		}
 	}
 
 	public void inAir(ArrayList<Platform> platforms, Ground g) {
 		if(platformNumber == -1){
 			if(this.getYPos() < g.getYFromX(this.getXPos()) - this.getHeight()){
 				touchingGround = false;
 				readyToJump = false;
 			}
 		} else {
 			if(this.getYPos() < platforms.get(platformNumber).getUpperYFromX(this.getXPos()) - this.getHeight()){
 				touchingGround = false;
 				readyToJump = false;
 			}
 		}
 	}
 
 	//------------------------------------------------------------------------------------------------//
 	//CHECK-METHODS FOR PROTAGONIST AND HIS SURROUNDING
 	//Check slope under protagonist
 	public void checkSlope(Ground ground, ArrayList<Platform> platforms){
 		if(touchingGround){ 
 			readyToJump = true;
 			if(getYPos()+getHeight()/2 - ground.getYFromX(getXPos()) > -5){ //On ground
 				double slope = ground.getSlope(this.getXPos());
 				if(Math.abs(slope) > slopeThreshold){
 					setXVel(getXVel()+slope);
 					setYVel(slope*getXVel());
 					readyToJump = false;
 					faceDirection((int)(getXVel()));
 					sliding = true;
 				} else {
 					sliding = false;
 				}
 			} else { //On platform
 				sliding = false;
 				for(int i = 0; i < platforms.size(); i++){
 					if((platforms.get(i).getUpperX()[0] <= getXPos() && platforms.get(i).getUpperX()[platforms.get(i).getUpperLength()-1] >= getXPos())){
 						double slope = platforms.get(i).getSlope(this.getXPos());
 						if(Math.abs(slope) > slopeThreshold){
 							setXVel(getXVel()+slope);
 							setYVel(slope*getXVel());
 							readyToJump = false;
 							sliding = true;
 							break;
 						}
 					} 
 				}
 			}
 
 			//Check if the speed has to be reduced
 			//This doesn't look good in game
 			if(Math.abs(this.getXVel()) > this.getMaxSpeed() && this.getXVel() > 0) { //Double code, also in accelerate
 				this.setXVel(this.getMaxSpeed());
 			} else if (Math.abs(this.getXVel()) > this.getMaxSpeed() && this.getXVel() < 0) {
 				this.setXVel(-this.getMaxSpeed());
 			}
 		}
 	}
 
 	//Check if the protagonist is standing on the ground (make sure he is)
 	public void checkGround(Ground g){
 		if(this.yPos + height/2 > g.getYFromX(this.xPos)){
 			this.yPos = g.getYFromX(this.xPos)-height/2;
 			this.yVel = 0;
 			this.yAcc = 0;
 			touchingGround = true;
 			readyToJump = true;
 		}
 	}
 
 	//Check if protagonist hit platform
 	public void checkPlatform(ArrayList<Platform> platforms) {
 		checkOverPlatform(platforms);
 		if(platformNumber == -1){
 			//No platform around protagonist
 		} else {
 			if (this.getYVel() > 0 && this.getYPos() + this.getHeight()/2 > platforms.get(platformNumber).getUpperYFromX(this.getXPos()) && this.getYPos() + this.getHeight()/2 < platforms.get(platformNumber).getLowerYFromX(this.getXPos())){
 				this.setYPos(platforms.get(platformNumber).getUpperYFromX(this.getXPos()) - this.getHeight()/2);
 				this.setYAcc(0);
 				this.setYVel(0);
 				touchingGround = true;
 				readyToJump = true;
 			} 
 		}
 
 		//if making move towards edge of platform
 		for(int i = 0; i < platforms.size(); i++){
 			//if head is in platform
 			if (platforms.get(i).spans(getXPos()) && this.getYVel() < 0 && this.getYPos() - this.getHeight()/2 < platforms.get(i).getLowerYFromX(this.getXPos()) && this.getYPos() - this.getHeight()/2 > platforms.get(i).getUpperYFromX(this.getXPos())) {
 				this.setYVel(-this.getYVel());
 				Log.d(TAG, "Headache!!");
 				//if feet is in platform
 			} else if (platforms.get(i).checkSide(this, -1) && getXPos() < platforms.get(i).getUpperX()[0] && getXPos() + getWidth()/2 > platforms.get(i).getUpperX()[0] && getXVel() > 0) {
 				this.setXVel(0);
 				this.setXPos(platforms.get(i).getUpperX()[0] - getWidth()/2);
 			}
 			if(platforms.get(i).checkSide(this, 1) && getXPos() > platforms.get(i).getUpperX()[platforms.get(i).getUpperX().length-1] && getXPos() - getWidth()/2 < platforms.get(i).getUpperX()[platforms.get(i).getUpperX().length-1] && getXVel() < 0){
 				this.setXVel(0);
 				this.setXPos(platforms.get(i).getUpperX()[platforms.get(i).getUpperX().length-1] + getWidth()/2);
 			}
 		}
 	}
 
 	//Check if protagonist is over a platform (used in dashing)
 	public void checkOverPlatform(ArrayList<Platform> platforms) { //Add an if block to get the highest platform
 		for (int i = 0; i < platforms.size(); i++) {
 			if (platforms.get(i).spans(this.getXPos()) && platforms.get(i).getLowerYFromX(this.getXPos()) >= this.getYPos() + this.getHeight()/2){
 				platformNumber = i;
 				break;
 			} else {
 				platformNumber = -1;
 			}
 		}
 	}
 
 	//Check collision with enemy
 	public boolean collide(Enemy e){
 		if(getXPos() - getWidth()/2 < e.getXPos() + e.getWidth()/2 && getXPos() + getWidth()/2 > e.getXPos() - e.getWidth()/2 &&
 				getYPos() - getWidth()/2 < e.getYPos() + e.getHeight()/2 && getYPos() + getWidth()/2 > e.getYPos() - e.getHeight()/2){
 			return true;
 		} else 
 			return false;
 	}
 
 	//Check if protagonist is outside track and set back inside level
 	public void contain(int finishX){
 		if(getXPos() < 1){
 			setXPos(1);
 		}
 		if(getXPos() > finishX+20){
 			setXPos(finishX+20);
 		}
 	}
 
 	//Check if protagonist is dead
 	public boolean checkDead() {
 		if (health <= 0)
 			return dead = true;
 		else
 			return dead = false;
 	}
 
 	//------------------------------------------------------------------------------------------------//
 	//FOR RENDERING PROTAGONIST
 	//Keeps track of the protagonist's step (used for rendering)
 	public void step(int step){
 		stepCount += step;
 		if(stepCount >= numberOfSteps){
 			stepCount = -numberOfSteps;
 		} else if(stepCount <= -numberOfSteps){
 			stepCount = numberOfSteps;
 		}
 	}
 
 	//Which way the protagonist should be rendered
 	public void faceDirection(Stick left, Stick right){
 		if(!sliding){
 			if(right.isPointed()){
 				if(angleAim <= 90 || angleAim > 270){
 					facingRight = true;
 				} else {
 					facingRight = false;
 				}
 			} else {
 				if(left.getAngle() <= 90 || left.getAngle() > 270){
 					facingRight = true;
 					angleAim = 0;
 				} else {
 					facingRight = false;
 					angleAim = 180;
 				}
 			}
 		} else {
 			faceDirection((int)(getXVel()));
			if(!right.isPointed()){
				angleAim = 90 - 90*Math.signum(getXVel());
			}
 		}
 	}
 
 	//Alternative method (used in tutorial for the mentor)
 	public void faceDirection(int dir){
 		if(dir >= 0){
 			facingRight = true;
 		} else {
 			facingRight = false;
 		}
 	}
 
 	//Keeps track of the protagonist's breathing (used for rendering)
 	public void breathe(){
 		breathCount++;
 		if(breathCount >= breathMax){
 			breathCount = 0;
 		}
 	}
 	//------------------------------------------------------------------------------------------------//
 	//GET AND SET METHODS
 	//Methods for position, velocity, acceleration
 	public double getXPos() {
 		return xPos;
 	}
 
 	public void setXPos(double n) {
 		xPos = n;
 	}
 
 	public double getYPos() {
 		return yPos;
 	}
 
 	public void setYPos(double n) {
 		yPos = n;
 	}
 
 	public double getXVel() {
 		return xVel;
 	}
 
 	public void setXVel(double n) {
 		xVel = n;
 	}
 
 	public double getYVel() {
 		return yVel;
 	}
 
 	public void setYVel(double n) {
 		yVel = n;
 	}
 
 	public double getXAcc() {
 		return xAcc;
 	}
 
 	public void setXAcc(double n) {
 		xAcc = n;
 	}
 
 	public double getYAcc() {
 		return yAcc;
 	}
 
 	public void setYAcc(double n) {
 		yAcc = n;
 	}
 
 	//Methods for properties
 	public void setAim(int angle) {
 		angleAim = angle;
 	}
 
 	public double getAim() {
 		return angleAim;
 	}
 
 	public double getHealth() {
 		return health;
 	}
 
 	public void setHealth(double health) {
 		this.health = health;
 	}
 
 	private double getJumpVel() {
 		return jumpVel;
 	}
 
 	private void setJumpVel(double jumpVel) {
 		this.jumpVel = jumpVel;
 	}
 
 	private double getJumpAcc() {
 		return jumpAcc;
 	}
 
 	private void setJumpAcc(double jumpAcc) {
 		this.jumpAcc = jumpAcc;
 	}
 
 	private double getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	private void setMaxSpeed(double maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 
 	private double getSlideCoefficient() {
 		return slideCoefficient;
 	}
 
 	private void setSlideCoefficient(double slideCoefficient) {
 		this.slideCoefficient = slideCoefficient;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setStepCount(int step){
 		stepCount = step;
 	}
 
 	public int getStepCount(){
 		return stepCount;
 	}
 
 	public int getNumberOfSteps() {
 		return numberOfSteps;
 	}
 
 	public int getBreathCount() {
 		return breathCount;
 	}
 
 	public void setBreathCount(int breathCount) {
 		this.breathCount = breathCount;
 	}
 
 	public int getBreathMax() {
 		return breathMax;
 	}
 
 	public int getInvincibilityCount() {
 		return invincibilityCount;
 	}
 
 	//Booleans
 	public boolean isTouchingGround() {
 		return touchingGround;
 	}
 
 	public void setTouchingGround(boolean touchingGround) {
 		this.touchingGround = touchingGround;
 	}
 
 	public boolean isFacingRight() {
 		return facingRight;
 	}
 
 	public void setFacingRight(boolean facingRight) {
 		this.facingRight = facingRight;
 	}
 
 	public boolean isSliding() {
 		return sliding;
 	}
 
 	public boolean isInvincible() {
 		return invincible;
 	}
 
 	public void setInvincible(boolean invincible) {
 		this.invincible = invincible;
 	}
 
 	public boolean isDashBonus() {
 		return dashBonus;
 	}
 
 	public void setDashBonus(boolean dashBonus) {
 		this.dashBonus = dashBonus;
 	}
 
 	public boolean isReadyToJump(){
 		return readyToJump;
 	}
 
 	//Others
 	public void getPlatformNumber(ArrayList<Platform> platforms){
 		for(int i = 0; i < platforms.size(); i++){
 			if (platforms.get(i).spans(this.getXPos())){
 				this.platformNumber = i;
 				break;
 			} else {
 				this.platformNumber = -1;
 			}
 		}
 	}
 
 	public int getPlatformNumber(){
 		return platformNumber;
 	}
 	//------------------------------------------------------------------------------------------------//
 }
