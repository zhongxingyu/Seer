 package tsa2035.game.content.levels.level1;
 
 import java.io.IOException;
 
 import tsa2035.game.content.levels.MainCharacter;
 import tsa2035.game.engine.scene.Scene;
 import tsa2035.game.engine.scene.Sprite;
 import tsa2035.game.engine.scene.background.SpriteBackground;
 import tsa2035.game.engine.texture.TextureManager;
 
 public class Level1 extends Scene {
 	
 	public Level1()
 	{
 		try {
 			setBackground(new SpriteBackground(TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/wallpanels.png")));
 			addToScene("character", new MainCharacter(0,0));
			addToScene("floor", new Sprite(0f, -0.5f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/floor.png"))).setSolid(true);
 		} catch (IOException e) {
 			System.out.println("Texture loading failed!");
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void sceneLogic() {
 		// This function is called every render loop
 		// Note: callbacks are the prefered way to do collision/interaction checking, not polling
 	}
 
 }
