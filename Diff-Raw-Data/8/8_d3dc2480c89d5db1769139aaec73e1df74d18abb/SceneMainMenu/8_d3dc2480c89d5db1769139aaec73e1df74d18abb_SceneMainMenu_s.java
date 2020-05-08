 package com.me.mygdxgame.scene;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL10;
 import com.me.mygdxgame.game.Game;
import com.me.mygdxgame.mgr.WindowMgr;
 
 public class SceneMainMenu extends SceneBase {
 
 	
 
 	public SceneMainMenu() {
		
 	}
 
 	@Override
 	public void updateMain(){
 		Game.camera.update();
 		GL10 gl = Gdx.gl10;
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 	}
 }
