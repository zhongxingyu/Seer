 package tsa2035.game.content.levels;
 
 import java.io.IOException;
 
 import tsa2035.game.engine.scene.AnimatedPlayer;
 import tsa2035.game.engine.texture.LoopedAnimatedTexture;
 
 public class MainCharacter extends AnimatedPlayer {
 
 	public MainCharacter(float x, float y) throws IOException {
		super(-0.8f, -0.5f, null, null, true, 0.5f, 0.01f);
 		LoopedAnimatedTexture left = new LoopedAnimatedTexture("/tsa2035/game/content/character/left", "character", 13, 30);
 		LoopedAnimatedTexture right = new LoopedAnimatedTexture("/tsa2035/game/content/character/right", "character", 13, 30);
 		setAnimations(left, right);
 		setScale(0.5f);
 	}
 	
 }
