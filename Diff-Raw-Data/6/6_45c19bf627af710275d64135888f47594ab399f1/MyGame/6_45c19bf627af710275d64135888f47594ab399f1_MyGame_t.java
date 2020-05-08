 package com.celoron.test;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
 import com.badlogic.gdx.graphics.Texture;
 import com.celoron.engine.Entity;
 import com.celoron.engine.Game;
 
 public class MyGame extends Game {
 
 	@Override
 	public void onCreate() {
 		Entity e= new Entity("player");
 		e.AddComponent( new TextureRender("render", new Texture(Gdx.files.internal("data/badlogicsmall.jpg")) ) );
 		e.AddComponent( new WASD_ControllerComponent("wasd") );
 		
 		sceneManager.addEntity(e);
 	}
 
 	@Override
 	public void onUpdate() {
 		/*
 		 * There is nothing i haveto think. after all components doing all work :D
 		 * */
 	}
 	
    public static void main(String[] args) {
        new JoglApplication(new MyGame(), "My Game", 480, 320, false);
    }
	
 }
