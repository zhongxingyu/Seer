 package com.subfty.krkjam2013;
 
 import java.util.Random;
 
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.subfty.krkjam2013.game.GameScreen;
 import com.subfty.krkjam2013.menu.MenuScreen;
 import com.subfty.krkjam2013.util.Art;
 import com.subfty.krkjam2013.util.Screen;
 
 public class Krakjam implements ApplicationListener {
 	
     //SCREEN STUFF
 	public static float SCREEN_WIDTH,
 						SCREEN_HEIGHT,
 						SCALE;
 	public final static float STAGE_W=800f,
 							  STAGE_H=600f,
 							  ASPECT_RATIO = STAGE_W/STAGE_H;
 	
 	public static Stage stage;
 	public static TweenManager tM;
 	public static Art art;
 	public static Random rand;
 	
 	public static Vector2 playerPos = new Vector2(400, 100);
 	
     //SCREEN UPDATE
 	private final long TARGET_FPS = 1000/60;
 	private long delta,
 				 prevTime;
 	
     //SCREENS
 	public static int S_GAME = 0,
 					  S_MENU = 1;
 	public Screen screens[];
 	
 	static public GameScreen gameScreen;
 	
 	static public Vector2 getPlayerPos() {
 		return playerPos;
 	}
 	
 	@Override
 	public void create() {		
 		stage = new Stage(STAGE_W, STAGE_H, false);
 		art = new Art();
 		tM = new TweenManager();
 		rand = new Random(System.currentTimeMillis());
 		Gdx.input.setInputProcessor(stage);
 	
 	    //INITING SCREENS
 		screens = new Screen[2];
 		gameScreen = new GameScreen(stage);
 		screens[S_MENU] = new MenuScreen(stage);
 		
 		screens[S_GAME] = gameScreen;
 		
 		for(int i=0; i<screens.length; i++)
 			screens[i].visible = false;
 		
 		delta = 0;
 		prevTime = -1;
 		
 		showScreen(S_GAME);
 	}
 
 	@Override
 	public void dispose() {
 	
 	}
 
 	@Override
 	public void render() {		
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		if(prevTime == -1)
 			prevTime = System.currentTimeMillis();
 		delta += System.currentTimeMillis() - prevTime;
 		
 		while(delta > TARGET_FPS){
 		    delta -= TARGET_FPS;
 			
 		    tM.update(delta/1000.0f);
		    stage.act(delta/1000.0f);
 		}
		 
 		stage.draw();
 	}
 	
 	@Override
 	public void resize(int w, int h) {
 		float ratio = (float)w/(float)h;
 		if(ratio > STAGE_W/STAGE_H)
 			SCALE = STAGE_H/((float)h);
 		else
 			SCALE = STAGE_W/((float)w);
 		
 		Camera c = stage.getCamera(); 
 		c.viewportWidth = SCREEN_WIDTH= w*SCALE;
 		c.viewportHeight = SCREEN_HEIGHT = h*SCALE;
 		c.position.set(STAGE_W/2,STAGE_H/2,0);
 	}
 
 	@Override
 	public void pause() {}
 
 	@Override
 	public void resume() {}
 	
 	//SCREEN MANAGEMENT
 	public void showScreen(int id){
 		screens[id].visible = true;
 	}
 	public void hideScreen(int id){
 		screens[id].visible = false;
 	}
 }
