 package de.philweb.bubblr;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 
 import de.philweb.bubblr.screens.AbstractScreen;
 import de.philweb.bubblr.screens.Screen;
 import de.philweb.bubblr.tools.IActivityRequestHandler;
 
 
 
 public abstract class Game implements ApplicationListener, InputProcessor {
 	
     
 	Screen screen;
 	AbstractScreen screenScene2D;
 	int renderID = 1;
 	
        
 	public float camWidth = 0.0f;		//the width of the cam
 	public float camHeight = 0.0f;		//the height of the cam
 	public float aspectRatio = 0.0f;	//the aspect ratio
 	public int screenWidth = 0;		//screen width in pixel size
 	public int screenHeight = 0;		//screen height in pixel size
 	protected OrthographicCamera cam;		//our camera
 	
     
     //=========== Handler f�r Ads etc ==================
     
     public IActivityRequestHandler myRequestHandler;
 
     public Game(IActivityRequestHandler handler) {
     	
         myRequestHandler = handler;
        
 	      
     }
     //==================================================
     
 	
 	public void setScreen (Screen helpScreen2) {
 		screen = helpScreen2;
 		renderID = 1;
 	}
 
 	public void setScene2DScreen (AbstractScreen scene2DScreen) {
 		screenScene2D = scene2DScreen;
 		renderID = 2;
 	}
 	
 	public void changeScene2DScreenToScreen (Screen helpScreen2) {
 		screen = helpScreen2;
 		renderID = 1;
 	}
 	
 	public void changeScreenToScene2DScreen (AbstractScreen scene2DScreen) {
 		screen.dispose();
 		screenScene2D = scene2DScreen;
 		renderID = 2;
 	}
 	
 	
 	
 	public abstract Screen getStartScreen ();
 
 	
 	@Override
 	public void create () {
 			
 		screen = getStartScreen();
 	}
 
 	  
 	
 	@Override
 	public void resume () {
 		screen.resume();
 	}
 
 	@Override
 	public void render () {
 		
 		if (renderID == 1) {
 			screen.update(Gdx.graphics.getDeltaTime());
 			screen.present(Gdx.graphics.getDeltaTime());
 		}
 		
 		if (renderID == 2) screenScene2D.render(Gdx.graphics.getDeltaTime());
 		
 	}
 
 	@Override
 	public void resize (int width, int height) {
 		
 //		WorldRenderer.setPixPerMeter();
 		
 		
 		screenWidth = width;
 		screenHeight = height;
 		aspectRatio = (float) screenWidth / (float) screenHeight;
 		camHeight = 480f;											//our camera will always be 1.0f height
 		camWidth = 480f * aspectRatio; 							//width depend on aspect ratio
 		
         cam = new OrthographicCamera(camWidth,camHeight);
         cam.position.set(camWidth / 2.0f, camHeight / 2.0f, 0.0f);
 	}
 
 	
 	
 	
 	@Override
 	public void pause () {
 		screen.pause();
 	}
 
 	@Override
 	public void dispose () {
 		
 //		m_batch.dispose();
 //		m_HUDBatch.dispose();
 		
 		screen.dispose();
 	}
 //--------------------------------
 
 	public OrthographicCamera getCamera() {
 		return cam;
 	}
 }
