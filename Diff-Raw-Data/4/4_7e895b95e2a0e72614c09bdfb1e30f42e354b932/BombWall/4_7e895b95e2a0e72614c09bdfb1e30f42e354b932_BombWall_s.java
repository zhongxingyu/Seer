 import org.newdawn.slick.opengl.Texture;
 
 
 public class BombWall extends Entity {
 	
 	protected Texture bombWall;
 
 	public BombWall(Game ingame,int hp) {
 		game = ingame;
		bombWall = loadTexture("brick.jpg");
		Shot = loadTexture("brickShot.jpg");
 		width = game.map.TILE_SIZE;
         height = game.map.TILE_SIZE;
 		halfSize = width/2;
 		HP = hp;
 		maxHP = HP;
 	}
 	
 	public void draw() {
 		super.draw(bombWall);
 		if(shoted){
 			super.draw(Shot);
 		}
 		if(showHP){
 			drawHP();
 		}
 	}
 
 	@Override
 	public void collidedWith(Entity other) {
 		if(other instanceof Bullet){
 			shoted = true;
 		}
 	}
 }
