 package com.WebSiteNameHere;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.XMLPackedSheet;
 
 public class Hero extends Entity{
 	public int lives;
 	public float vertVelocity;
 	public boolean rolling = false;
 
 	private Animation run;
 	private Animation ascend;
 	private Animation fall;
 	private Animation land;
 	private Animation roll;
 	
 	private Animation[] allAnimations = new Animation[5];
 	
 	private int animationFlag = 0; // 0=run; 1=ascend; 2=fall; 3=land; 4=roll; 5=death; 5 will be implemented later
 	private int prevAnimation;
 
 	private XMLPackedSheet sheet;
 	private float spd = 1.5f;  //speed of overall animation
 
 	public Hero(int x, int y)
 	{
 		this.x=x;
 		this.y=y;
 
 		vertVelocity = -10f; //Set to -10 for a smooth animation start
 		//This is her vertical velocity, and helps determine which animation
 		//needs to be used.
 
 		//setting up animations
 		setUpAnimations();
 		//run animation set to start at the beginning, other animations have to be stopped when setting them up.
 		run.start();
 		prevAnimation = animationFlag;
 		
 	}
 
 	public void update(){
 		//to check later if the flag has changed
 		prevAnimation = animationFlag;
 		
 		//logic to change the animation flag. Needs improvement!
		if(!rolling){
 			if(vertVelocity>0)
 				animationFlag = 1;
 			else if((vertVelocity<0)&&(vertVelocity>-0.3f))
 				animationFlag = 2;
 			else if((vertVelocity<-6)&&(vertVelocity>-7))
 				animationFlag = 3;
			else if(vertVelocity<-7 && roll.isStopped())
 				animationFlag = 0;
 		} else {
 			animationFlag = 4;
 			rolling = false;
 		}
 		
 		//if flag has changed after application of logic, start animation corresponding with the new flag
 		if(prevAnimation != animationFlag)
 			startAnimation(allAnimations[animationFlag]);
 	}
 	
 	private void startAnimation(Animation anim){
 		//stops all animations with exception of passed animation
 		for (int i=0; i<allAnimations.length; i++){
 			allAnimations[i].stop();
 		}
 		if(anim.isStopped())
 			anim.restart();
 	}
 
 	public void render(){ 
 		//rendering according to animation flag
 		allAnimations[animationFlag].draw(x, y);
 	}
 
 	private void setUpAnimations(){
 		//I am too tired atm, thus copy paste code
 
 
 		//getting SpriteSheet
 		try {
 			sheet = new XMLPackedSheet("res/sprites/player/RedSpriteSheet.png", "res/sprites/player/RedSpriteSheet.xml");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 		//setting up running animation according to sprite names
 		run = new Animation();
 		for (int i=1;i<=9;i++) {
 			run.addFrame(sheet.getSprite("run"+(i)+".png"), 150);
 		}
 		//these should be self-explanatory
 		run.setAutoUpdate(true);
 		run.setLooping(true);
 		run.setSpeed(spd);
 
 		//setting up jumping animation (3 parts of it) according to sprite names.
 		ascend = new Animation();
 		for (int i=2;i<=3;i++) {
 			ascend.addFrame(sheet.getSprite("jump"+(i)+".png"), 150);
 		}
 		ascend.setAutoUpdate(true);
 		ascend.setLooping(false);
 		ascend.setSpeed(spd - 0.8f);
 		ascend.stop();
 
 		fall = new Animation();
 		for (int i=4;i<=5;i++) {
 			fall.addFrame(sheet.getSprite("jump"+(i)+".png"), 150);
 		}
 		fall.setAutoUpdate(true);
 		fall.setLooping(false);
 		fall.setSpeed(spd -.5f);
 		fall.stop();
 
 		land = new Animation();
 		for (int i=6;i<=8;i++) {
 			land.addFrame(sheet.getSprite("jump"+(i)+".png"), 150);
 		}
 		land.setAutoUpdate(true);
 		land.setLooping(false);
 		land.setSpeed(spd - .5f);
 		land.stop();
 		
 		roll = new Animation();
 		for (int i=1;i<=14;i++) {
 			roll.addFrame(sheet.getSprite("roll"+(i)+".png"), 150);
 		}
 		roll.setAutoUpdate(true);
 		roll.setLooping(false);
 		roll.setSpeed(spd + 1f);
 		roll.stop();
 		
 		
 		//adding animations to the array
 		allAnimations[0] = run;
 		allAnimations[1] = ascend;
 		allAnimations[2] = fall;
 		allAnimations[3] = land;
 		allAnimations[4] = roll;
 //		allAnimations[5] = death;  //this is for later
 	}
 }
