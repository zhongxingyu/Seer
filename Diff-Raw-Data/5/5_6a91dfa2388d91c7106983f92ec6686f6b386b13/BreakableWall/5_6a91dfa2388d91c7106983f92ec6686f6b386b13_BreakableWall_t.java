 package entities;
 
 import game.Game;
 import graphics.Sprite;
 
 import java.util.Random;
 
 public class BreakableWall extends Wall {
 
 	/**
 	 * @param x
 	 * @param y
 	 */
 	public BreakableWall(int x, int y) {
 		super(x, y);
		int z = new Random().nextInt(12);
 		// int z = (int) (Math.random() * 10);
 		if (z < 4) {
 			this.images = Sprite.load("w1.png", 100, 100);
		} else if ((z >= 4) && (z < 8)) {
 			this.images = Sprite.load("w2.png", 100, 100);
 		} else if (z >= 8) {
 			this.images = Sprite.load("w3.png", 100, 100);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see entities.Entity#collide(entities.Entity)
 	 */
 	@Override
 	public void collide(Entity e) {
 		if (e instanceof BombAnimation) {
 			this.removed = true;
 			Game.staticBackground.add(new Background(this.x, this.y));
 		}
 	}
 }
