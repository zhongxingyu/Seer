 package edu.usf.eng.pie.avatars4change.wallpaper;
 
 import android.content.Context;
 import android.graphics.Canvas;
import android.os.Environment;
 import edu.usf.eng.pie.avatars4change.avatar.Animation;
 import edu.usf.eng.pie.avatars4change.avatar.Avatar;
 import edu.usf.eng.pie.avatars4change.avatar.Entity;
 import edu.usf.eng.pie.avatars4change.avatar.Location;
 import edu.usf.eng.pie.avatars4change.avatar.Scene;
 import edu.usf.eng.pie.avatars4change.avatar.Sprite;
 import edu.usf.eng.pie.avatars4change.userData.userData;
 
 public class Layer_UnitTestMain {
 	static Scene     testScene;
 	static Animation testAnimation;
 	static Entity    testEntity;
 	static Location  testEntLoc;
 	
 	public static void setup(Avatar daAvatar, Context context){
 		//setup Scene
         testScene = new Scene("testScene");
     	String baseFileDirectory = userData.getFileDir(context);		//file directory to use on sdcard
     	String spriteDir = baseFileDirectory + "/sprites";
     	String testFile = spriteDir + "/body/active/basketball/.";
     	int testSize =  150;
     	int testAngle= -45;
     	int testX    = -150;
     	int testY    =  300;
     	int testZ    =  3;
     	Location testLoc = new Location(testX, testY,testZ, testSize, testAngle);
     	Animation tAnimation = new Animation("tAnim", testFile, testLoc);
      	testScene.addAnimation(tAnimation);
      	Sprite tsprite = new Sprite("tSprite", testFile+"3.png", new Location(0,0,0,1000,0));
      	testScene.addSprite(tsprite);
         
         //setup test animation:
     	testSize =  100;
     	testAngle= -10;
     	testX    =  0;
     	testY    = -50;
     	testZ    =  1;
     	Location animTestLoc = new Location(testX, testY, testZ, testSize, testAngle);
     	testAnimation = new Animation("test", testFile, animTestLoc);
     	
     	//setup test entity
      	testSize =  200;
      	testAngle= -150;
      	testX    =  33;
      	testY    =  11;
      	testZ    =  0;
      	testEntLoc = new Location(testX, testY, testZ, testSize, testAngle);
      	testEntity   = new Entity("name",testEntLoc);  	
      	
        	Layer_DebugInfo.setup();
        	
        	Layer_UserStatus.setup();
 	}
 	public static void nextFrame(){
 		testScene.nextFrame();
 		Layer_DebugInfo.nextFrame();
 		//UserStatusLayer doesn't use nextFrame()
 	}
 
 	public static void draw(Canvas c, Context context){
 		c.save();
 		testScene.draw(c);
 		c.restore();
 		drawTestEntity(c);
 		drawTestSprite(c, context);
 		drawTestAnimation(c);
 		/*
 		c.save();
 		Layer_DebugInfo.drawFPS(c, avatarWallpaper.desiredFPS);
 		c.restore();
 		c.save();
 		Layer_UserStatus.draw(c);
 		c.restore();
 		*/
 	}
 	
     private static void drawTestEntity(Canvas c){
     	c.save();
         testEntity.nextFrame();
     	testEntity.draw(c);
     	c.restore();
     }
     private static void drawTestSprite(Canvas c, Context context){
     	c.save();
     	String baseFileDirectory = userData.getFileDir(context);		//file directory to use on sdcard
     	String spriteDir = baseFileDirectory + "/sprites";
     	String spriteFile = spriteDir + "/face/default/0.png";
     	int testSize = 50;
     	int testAngle= 45;
     	int testX    = 100;
     	int testY    = 200;
     	int testZ    = 2;
     	Location testLoc = new Location(testX, testY, testZ, testSize, testAngle);
     	Sprite testSprite = new Sprite("test",spriteFile, testLoc);
     	testSprite.draw(c);
     	c.restore();
     }
     private static void drawTestAnimation(Canvas c){
     	c.save();
         testAnimation.nextFrame();
     	testAnimation.draw(c);
     	c.restore();
     }
 }
