 package entities;
 
 import game.Game;
 import graphics.Sprite;
 
public class WallWithFinishingPoint extends Wall {
 
 	public WallWithFinishingPoint(int x, int y) {
 		super(x, y);
 		this.images = Sprite.load("brick_break_small.png", 100, 100);
 	}
 
 	@Override
 	public void collide(Entity e) {
 		if (e instanceof BombAnimation) {
 			this.removed = true;
 			Game.staticBackground.add(new Background(this.x, this.y));
			Game.staticBackground.add(new Finishpoint(this.x, this.y));
 		}
 	}
 }
