 package spx.graphics;
 
 import spx.core.Settings;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 public class ContentManager {
 	static public void Load() {
 	}
 
 	public String RootDirectory;
 	private Texture _spriteSheet;
 	private BitmapFont _font;
 
 	public Texture LoadTexture(String resourceName) {
 		return AssetManager.Get().GetImage(resourceName);
 	}
 
 	public Sprite LoadSprite(int verticalIndex) {
 		if (_spriteSheet == null) {
 			_spriteSheet = AssetManager.Get().GetImage("GameplaySheet.png");
 		}
		int x = 0;
		int y = verticalIndex * Settings.Get().spriteHeight + verticalIndex;
		int width = Settings.Get().spriteWidth;
		int height = Settings.Get().spriteHeight;
 		return new Sprite(_spriteSheet, x, y, width, height);
 	}
 
 	public BitmapFont LoadFont(String resourceName) {
 		if (_font == null) {
 			_font = AssetManager.Get().GetFont("Main.font");
 		}
 		return _font;
 	}
 }
