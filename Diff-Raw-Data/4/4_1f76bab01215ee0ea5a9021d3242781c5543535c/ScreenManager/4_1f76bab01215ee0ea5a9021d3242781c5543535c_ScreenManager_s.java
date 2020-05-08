 package com.mangecailloux.screens;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.math.MathUtils;
 import com.mangecailloux.debug.Debuggable;
 
 public abstract class ScreenManager extends Debuggable implements ApplicationListener {
     private Screen  	 screen;
     private Screen  	 nextScreen;
     private boolean		 changeScreenPending;
     private boolean 	 manageScreenDisposal;
     private float 		 updateTimer;
     private AssetManager assetManager;
     private boolean		 appCreated;
     
     public ScreenManager()
     {
     	super();
     	updateTimer = 0.0f;
     	manageScreenDisposal = true;
     	appCreated = false;
     	assetManager = new AssetManager();
    	Texture.setAssetManager(assetManager);
     }
     
     public AssetManager getAssetManager()
     {
     	return assetManager;
     }
     
     public void manageScreenDisposal(boolean _Manage)
     {
     	manageScreenDisposal = _Manage;
     }
     
     @Override
     protected 	void onDebug (boolean _debug) 
     {
     }
 
     @Override
     public void dispose () {
     		applyNextScreen(null);
             assetManager.dispose();
     }
     
     @Override
     public void create ()
     {
     	appCreated = true;
     	setScreen(getInitialScreen());
     }
     
     protected abstract Screen getInitialScreen();
 
     @Override
     public void pause () {
             if (screen != null) 
             	screen.pause(true);
     }
 
     @Override
     public void resume () {
             if (screen != null) 
             	screen.pause(false);
     }
 
     @Override
     public void render () {
 
     		if(changeScreenPending)
     		{
     			applyNextScreen(nextScreen);
     			changeScreenPending = false;
     		}
     	
             if (screen != null)
             {
             	float fDt = Gdx.graphics.getDeltaTime();
             	
             	float fUpdateDt = screen.getUpdateDt();
             	
             	if(fUpdateDt <= 0.0f)
             	{
             		float maxedDt = MathUtils.clamp(fDt, 0.0f, screen.getMaxUpdateDt());
             		screen.onUpdate(maxedDt);
             	}
             	else
             	{
             		updateTimer -= fDt;
                 	if(updateTimer <= 0.0f)
                 	{
                 		updateTimer = screen.getUpdateDt();
                 		screen.onUpdate(updateTimer);	
                 	}
             	}
             		
             	screen.onRender(fDt);
             }
     }
 
     @Override
     public void resize (int _width, int _height) {
             if (screen != null) 
             	screen.resize(_width, _height);
     }
 
     public void setScreen (Screen _screen) {
     	changeScreenPending = true;
     	nextScreen = _screen;
     }
 
     private void applyNextScreen (Screen _screen) {
     	if(appCreated && _screen != screen)
         {   
             if(_screen != null && !_screen.isLoaded())
             	_screen.load();
             
     		if (screen != null) 
             {
             	screen.activate(false);
             	screen.unload();
             	if(manageScreenDisposal)
             		screen.dispose();
             }
             
             screen = _screen;
             
             if(screen != null)
             {
 	            Gdx.input.setInputProcessor(screen.getInputMultiplexer());
 		        screen.activate(true);
 		        screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
             }
         }
     }
 
     public Screen getScreen () {
             return screen;
     }
 }
