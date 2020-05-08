 package game;
 
 import java.awt.Point;
 import java.util.Iterator;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class PlayingState extends BasicGameState {
 	
 	public static final int ID = 10;
 	
 	private Level _level;
 	
 	public Level getLevel() {
 		return _level;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		_level = new Level(container.getWidth() / Game.BLOCK_WIDTH, 
 				container.getHeight() / Game.BLOCK_HEIGHT, 
 				Game.BLOCK_WIDTH, Game.BLOCK_HEIGHT);
 	}
 	
 	@Override
 	public void enter(GameContainer container, StateBasedGame game) {
 		Game.player.setPosition(_level.getPlayerStart().x, _level.getPlayerStart().y);
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		int blockWidth = container.getWidth() / Game.BLOCK_WIDTH;
 		int blockHeight = container.getHeight() / Game.BLOCK_HEIGHT;
 		
 		Game.spriteSheet.startUse();
 		
 		for (int x = 0; x < blockWidth; ++x) {
 			for (int y = 0; y < blockHeight; ++y) {
 				Block block = _level.blockAt(x, y);
 				Game.spriteSheet.renderInUse(x * Game.BLOCK_WIDTH, y * Game.BLOCK_WIDTH, 
 						block.getSpritePosition().x, block.getSpritePosition().y);
 			}
 		}
 
 		Game.spriteSheet.renderInUse(Game.player.getPosition().x * Game.BLOCK_WIDTH, 
 				Game.player.getPosition().y * Game.BLOCK_HEIGHT, 0, 3);
 		
 		Game.spriteSheet.endUse();
 		
 		String scoreDisplay = String.format("Score: %d", Game.player.getPoints());
 		int width = g.getFont().getWidth(scoreDisplay);
 		g.drawString(scoreDisplay, container.getWidth() - width - 10, 10);
 		
 		Iterator<Block> cityIter = _level.getCities().iterator();
 		while (cityIter.hasNext()) {
 			Block block = cityIter.next();
			if (block.getType() != Block.BLOCK_TYPE_CITY) {
				cityIter.remove();
				continue;
			}
			
 			int blockX = block.getBounds().x / Game.BLOCK_WIDTH;
 			int blockY = block.getBounds().y / Game.BLOCK_HEIGHT;
 			
 			if (_level.blockAt(blockX, blockY + 1).getType() == Block.BLOCK_TYPE_NONE) {
 				Explosion explosion = new Explosion(Game.spriteSheet);
 				explosion.addPosition(new Point(blockX, blockY));
 				_level.getExplosions().add(explosion);
 				cityIter.remove();
 			}
 		}
 		
 		Iterator<Explosion> explosionIter = _level.getExplosions().iterator();
 		while (explosionIter.hasNext()) {
 			Explosion e = explosionIter.next();
 			if (e.getAnimation().isStopped()) {
 				Iterator<Point> posIter = e.getPositions().iterator();
 				while (posIter.hasNext()) {
					new Sound("data/explosion.wav").play(0.75f, 0.3f);
 					Point position = posIter.next();
 					Block block = _level.blockAt(position.x, position.y);
 					Game.player.applyPoints(block.getPoints());
 					block.setType(Block.BLOCK_TYPE_NONE);
 				}
 				
 				explosionIter.remove();
 			} else {
 				e.draw();
 			}
 		}
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		Point playerPosition = Game.player.getPosition();
 		if (playerPosition.y < container.getHeight() / Game.BLOCK_HEIGHT - 1 &&_level.blockAt(playerPosition.x, playerPosition.y + 1).getType() == Block.BLOCK_TYPE_NONE) {
 			Game.player.setPosition(playerPosition.x, playerPosition.y + 1);
 		}
 		
 		if (playerPosition.y == (container.getHeight() / Game.BLOCK_HEIGHT) - 1) {
 			game.enterState(LevelInterstitialState.ID);
 		}
 	}
 	
 	@Override
 	public void keyPressed(int key, char c) {
 		if (key == Input.KEY_LEFT) {
 			Game.player.move(-1, 0, _level);
 		} else if (key == Input.KEY_RIGHT) {
 			Game.player.move(1, 0, _level);
 		} else if (key == Input.KEY_DOWN) {
 			Game.player.move(0, 1, _level);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }
