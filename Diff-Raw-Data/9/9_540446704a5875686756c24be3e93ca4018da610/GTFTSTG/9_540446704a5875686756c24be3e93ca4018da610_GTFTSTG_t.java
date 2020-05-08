 package com.aquasheep.GTFTSTG;
 
 import com.aquasheep.GTFTSTG.Items.ActionFigure;
 import com.aquasheep.GTFTSTG.Items.Bottle;
 import com.aquasheep.GTFTSTG.Items.Camera;
 import com.aquasheep.GTFTSTG.Items.Clown;
 import com.aquasheep.GTFTSTG.Items.Diaper;
 import com.aquasheep.GTFTSTG.Items.Item;
 import com.aquasheep.GTFTSTG.Items.Keys;
 import com.aquasheep.GTFTSTG.Items.Mirror;
 import com.aquasheep.GTFTSTG.Items.Pacifier;
 import com.aquasheep.GTFTSTG.Items.Phone;
 import com.aquasheep.GTFTSTG.Items.Rattle;
 import com.aquasheep.GTFTSTG.Screens.SplashScreen;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.audio.Sound;
 
 public class GTFTSTG extends Game {
 	
 	public static final String LOG = "GTFTSTG Log";
 	public final boolean DEBUG = false;
 	public Item[] items;
 	private Baby baby;
 	private float screenWidth = 1, screenHeight = 1;
 	private float widthOld,heightOld;
 	public Sound backgroundSound;
 	
 	@Override
 	public void create() {
 		baby = new Baby();
 		//Add all items to screen	
 		items = new Item[10];
 		setScreen(new SplashScreen(this));
 		//TODO Soft-code image positions based on screen size
 			//Perhaps possible by having a set of multipliers and applying them to screen size whenever setSize is called
 			//Another possibility is to bind items to background as one texture and access relative coords
 		//Must substract from screenHeight to convert topleft to bottomleft and then subtract image height
		items[0] = new Bottle(this,(int)(0.3065*screenWidth),(int)(0.2859*screenHeight));
 		items[1] = new Clown(this,(int)(0.5098*screenWidth),(int)(0.3311*screenHeight));
 		items[2] = new ActionFigure(this,(int)(0.5499*screenWidth),(int)(0.1619*screenHeight));
 		items[3] = new Mirror(this,(int)(0.9256*screenWidth),(int)(0.3514*screenHeight));
 		items[4] = new Diaper(this,(int)(0.9256*screenWidth),(int)(0.2700*screenHeight));
 		items[5] = new Phone(this,(int)(0.3425*screenWidth),(int)(0.3689*screenHeight));
 		items[6] = new Camera(this,(int)(0.5915*screenWidth),(int)(0.6421*screenHeight));
 		items[7] = new Keys(this,(int)(0.7960*screenWidth),(int)(0.0095*screenHeight));
 		items[8] = new Rattle(this,(int)(0.3425*screenWidth),(int)(0.0176*screenHeight));
 		items[9] = new Pacifier(this,(int)(0.3303*screenWidth),(int)(0.2700*screenHeight));
 	}
 
 	public Baby getBaby() {
 		return baby;
 	}
 
 	public void setSize(int width, int height) {
 		widthOld = screenWidth;
 		heightOld = screenHeight;
 		this.screenWidth = width;
 		this.screenHeight = height;
 		//Forces program to wait for items to load
 		if (items[9]!=null)
 			for (int i = 0; i < items.length; ++i) {
 				items[i].setPos(items[i].getPos().x/widthOld*width, items[i].getPos().y/heightOld*height);
 			}
 	}
 	
 	public float getWidth() {
 		return screenWidth;
 	}
 	
 	public float getHeight() {
 		return screenHeight;
 	}
 
 }
