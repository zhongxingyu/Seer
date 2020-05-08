 package com.secondhand.scene;
 
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.entity.modifier.MoveXModifier;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.opengl.font.Font;
 
import com.secondhand.loader.FontLoader;
 import com.secondhand.twirl.GlobalResources;
 
import android.graphics.Color;
import android.graphics.Typeface;
 
 public class SplashScene extends Scene {
 	private Text title1;
 	private Text title2;
 	
 	public SplashScene(Camera camera){
 		
 		setBackground(new ColorBackground(0,0,0));
 		
 		Font font = GlobalResources.getInstance().menuFont;
 				
 		title1 = new Text(0, 0, font, "Second");
 		title2 = new Text(0, 0, font, "Hand");
 		
 		title1.setPosition(-title1.getWidth(), camera.getHeight() / 2);
 		title2.setPosition(camera.getWidth(),
 				camera.getHeight() / 2);
 		
 		attachChild(title1);
 		attachChild(title2);
 
 		title1.registerEntityModifier(new MoveXModifier(1, title1.getX(),
 				camera.getWidth() / 2 - title1.getWidth()));
 		title2.registerEntityModifier(new MoveXModifier(1, title2.getX(),
 				camera.getWidth() / 2));
 	}
 }
