 package net.foxycorndog.p1xelandroid;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.opengles.GL11;
 
 import net.foxycorndog.jdoogl.GL;
 import net.foxycorndog.jdoogl.components.Frame;
 import net.foxycorndog.jdoogl.components.Frame.GameRenderer;
 import net.foxycorndog.jdoogl.image.imagemap.SpriteSheet;
 import net.foxycorndog.jdoogl.input.KeyboardInput;
 import net.foxycorndog.jdoogl.input.TouchInput;
 import net.foxycorndog.p1xelandroid.items.tiles.Tile;
 import android.app.Activity;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Window;
import net.foxycorndog.jdoogl.activity.*;
 
 public class MainActivity extends GameComponent
 {
 	private Activity        activity;
 	
 	private static P1xeland p;
 	
 	public void onCreate()
 	{
 		this.activity = this;
 				
 		p = new P1xeland();
 		
 		p.setActivity(this);
 		
 //		Frame.init(P1xeland.GAME_TITLE, getGameRenderer(), activity);
 //		
 //		p.init();
 	}
 	
 	public void render(GL10 gl)
 	{
 		p.render();
 	}
 	
 	public void loop()
 	{
 		p.loop();
 	}
 	
 	public void onSurfaceChanged(GL10 gl)
 	{
 		Frame.init(P1xeland.GAME_TITLE, getGameRenderer(), activity);
 		
 		Tile.init(activity.getResources());
 		
 		p.init();
 	}
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config)
 	{
 		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
 	}
}
