 package org.Giraffe;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.media.AudioManager;
 import android.util.Log;
 
 public class GameModel {
 	/**list of enenmies and images +giraffe+background*/
 	static LinkedList<Effects>levelObjects=new LinkedList<Effects>();
 	LinkedList<Effects>objectsToDraw=new LinkedList<Effects>();
 	
 	private ArrayList<Backgrounds2> backgrounds=new ArrayList<Backgrounds2>();
 	
 	static Random r = new Random();
 	
 	//creates level
 	LevelMaker level;
 	Context context;
 	Backgrounds background;
 	/**this checks when enemies or obstacles appear and disapear.*/
 	long timeIn;
 	
 	GiraffeEntity ourGiraffe;
 	
 	int bLocation1;
 	int bLocation2;
 	float backgroundSpeed;
 	
 	float width;
 	float height;
 	
 	long timeFrozen;
 	
 	private boolean levelOver=false;
 	private boolean levelLose=false;
 	private boolean laugh=false;
 	private boolean mPlaying = false;
 	
 	public GameModel(Context context){
 		this.context=context;
         timeFrozen=System.currentTimeMillis()+0;
         SoundManager.initSounds(context);
 		SoundManager.addSound(1,R.raw.boing);  
 		SoundManager.addSound(2,R.raw.laugh2); 
 		SoundManager.addSound(3,R.raw.ow);  
 		SoundManager.addSound(4,R.raw.psh); 
         /*LOADS LEVEL 1 AS DEFUALT*/
 		
 		//this gets called twice and needs to be fixed
         loadLevel(1);
         
 	}
 	public void setSize(float width, float height){
 		this.width=width;
 		this.height=height;
 		bLocation1=0;
 		bLocation2=(int)width;
 		loadLevel(1);
 		
 	}
 	
 	public void loadLevel(int act){
 		LevelBuilder levels=new LevelBuilder(act);
 		background= new Backgrounds(levels.getNumForBackground(),context.getResources());
 		backgroundSpeed=8*(width/800);
 		
 		level= new LevelMaker(levels.getLevel(), context, height, width);
 		backgrounds=level.getBacks();
         
 		levelObjects=level.getLevelObjects();
 		ourGiraffe=(GiraffeEntity)levelObjects.get(0);
 		ourGiraffe.setHealth(3);
 		//Changed
 		if(act == 1 && mPlaying == false)
 		{
 				Music.create((Activity)context, R.raw.newcentralpark);
 				Music.start((Activity)context);
 				Music.setLooping((Activity)context, R.raw.newcentralpark);
 				mPlaying = true;
 		}
         
 	}
 	public ArrayList<Backgrounds2> getBkround(){
 		return backgrounds;
 	}
 	public void gameOver(long t){
 		if(levelLose){
 			levelOver=false;
 			((Activity) context).finish();
 			Intent gameOverScreen = new Intent(context, GameOver.class);
 			gameOverScreen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 			context.startActivity(gameOverScreen);
 			//updateLevel();
 		}else{
 			//background=new Backgrounds(3,context.getResources());
 			//updateLevel();
 			levelOver=false;
 			((Activity) context).finish();
 			Intent winScreen = new Intent(context, WinScreen.class);
 			winScreen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 			context.startActivity(winScreen);
 			}
 		}
 	public void updateLevel(){
 		
 		this.searchForCollision();
 		this.alternateBackground();
 		
 		if(ourGiraffe.getJump()){ourGiraffe.jump();}
 		
 		if(ourGiraffe.getHealth()==0){
 			//Changed
 			if(laugh==false){
 				SoundManager.playSound(2);  
 				laugh=true;
 			}
 			levelLose=true;
 			levelOver=true;
 		}
 		objectsToDraw.clear();
 		ourGiraffe.setPic();
 		timeIn=System.currentTimeMillis();
 
 		if(levelObjects.size()<2){
 			//levelWin=true;//this is winning the level
 			levelOver=true;
 			//Changed
 			mPlaying = false;
 			
 		}
 		for(int f=0; f<levelObjects.size(); f++){
 			if(levelObjects.get(f).X2()<-10 || levelObjects.get(f).Y2()>500){
 				levelObjects.remove(f);
 			}
 		}
 		for(Effects e:levelObjects){
 				if(e.getTime()<timeIn){
 					objectsToDraw.add(e);
 
 					
 				}
 			}
 	}
 	/**
 	 * This returns the objects of effects only to be drawn. This list gets cleared every update.
 	 * @return effects to be drawn in real time. 
 	 */
 	public LinkedList<Effects> getObjects(){
 		return objectsToDraw;
 	}
 	/**
 	 * This returns all the effects in the level, and not the ones just to be drawn.
 	 * @return all the objects created for the level as 'effects'
 	 */
 	public LinkedList<Effects> allEffects(){
 		return levelObjects;
 	}
 	
 	public void searchForCollision() {
 	    for (Effects fx : levelObjects) {
 			
 	    	if(fx.canCollide()&&ourGiraffe.canCollide()&&!fx.toString().equals("giraffe")){
 				
 	    		fx.collidesWithGiraffe((Enemy)fx,ourGiraffe);
 				//Log.d("COLLISION", ""+entity.toString()+" collided with "+otherEntity.toString());
 					}
 			}
 	  
 	}
 	public void alternateBackground(){
 		
 		if(bLocation2<=0){
 			bLocation1=0;
 			bLocation2=(int)width;
 		}
 		
 		//Background changes based on time;
 		if(System.currentTimeMillis()-timeFrozen>2){
 			bLocation1-=backgroundSpeed;
 			bLocation2-=backgroundSpeed;
 			timeFrozen=System.currentTimeMillis();
 		}
 	}
 
 	
 	public boolean levelOver(){
 		return levelOver;
 	}
 	public GiraffeEntity getOurGiraffe()
 	{
 		return ourGiraffe;
 	}
 	
 	
 }
