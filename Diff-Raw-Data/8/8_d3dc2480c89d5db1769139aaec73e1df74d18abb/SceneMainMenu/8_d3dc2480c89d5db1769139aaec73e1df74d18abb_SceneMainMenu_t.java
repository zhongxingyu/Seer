 package com.me.mygdxgame.scene;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.me.mygdxgame.game.Game;
import com.me.mygdxgame.mgr.StageMgr;
import com.me.mygdxgame.ui.stage.StageMainMenu;
 
 public class SceneMainMenu extends SceneBase {
 
 	
 
 	public SceneMainMenu() {
		StageMgr.startStageLater(new StageMainMenu());
 	}
 
 	@Override
 	public void updateMain(){
 		Game.camera.update();
 		GL10 gl = Gdx.gl10;
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 	}
 }
