 package com.buildcoolrobots.games.pgcgame.Screens;
 
 import me.pagekite.glen3b.gjlib.ExtendedSprite;
 import me.pagekite.glen3b.gjlib.SpriteManager;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.buildcoolrobots.games.pgcgame.CoreTypes.BGSprite;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.BaseScreen;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.GameImage;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.ScreenType;
 
 
 public class GameScreen extends BaseScreen {
 	ExtendedSprite dPad;
 	
 	public GameScreen(SpriteManager allSprites, SpriteBatch spriteBatch, ScreenType screenType) {
 		super(allSprites, spriteBatch, screenType);
 		
		BGSprite.scrollingBackground(allSprites);
		
 		dPad = new ExtendedSprite(GameImage.GAMEDPAD.ImageTexture());
 		allSprites.add(dPad);
 	}
 	
 	public void update(float deltaTime) {
 		super.update(deltaTime);
 	}
 }
