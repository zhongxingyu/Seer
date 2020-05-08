 package entities;
 
 import game.Game;
 import graphics.Image;
 import graphics.Sprite;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 import level.Box;
 
 public class Player extends Entity {
 	private float width;
 	private float height;
 
 	private Image[][] facing_down, facing_up, facing_left, facing_right,
 			facing_current;
 	private int speed;
 
 	public Player(int x, int y) {
 		super(x + 1, y + 1);
 		this.facing_left = Sprite.load("bomberman_l_small.png", 55, 90);
 		this.facing_right = Sprite.load("bomberman_r_small.png", 55, 90);
 		this.facing_down = Sprite.load("bomberman_v_small.png", 55, 90);
 		this.facing_up = Sprite.load("bomberman_h_small.png", 55, 90);
 		this.facing_current = this.facing_down;
 		this.width = (55 / 2);
 		this.height = (90 / 2);
 		this.box = new Box(this.x, this.y, (int) this.width, (int) this.height);
		this.speed = 10;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(Color.GREEN);
 		g.drawImage((this.facing_current[0][0]).image, this.x, this.y,
 				(int) this.width, (int) this.height, null);
 	}
 
 	@Override
 	public void action(double delta) {
 		boolean moved = false;
 		if (Game.keys.up.down) {
 			this.y = this.y - this.speed;
 			this.facing_current = this.facing_up;
 			moved = true;
 		}
 		// TODO: Bombs shouldn't be able to be planted with pressed space
 		// button.Need just one bomb per press.
 		if (Game.keys.bomb.down) {
 			Game.entities.add(new Bomb(this.x, this.y));
 
 		}
 
 		if (Game.keys.down.down) {
 			this.y = this.y + this.speed;
 			this.facing_current = this.facing_down;
 			moved = true;
 		}
 
 		if (Game.keys.left.down) {
 			this.x = this.x - this.speed;
 			this.facing_current = this.facing_left;
 			moved = true;
 		}
 
 		if (Game.keys.right.down) {
			this.x = this.x + 10;
 			this.facing_current = this.facing_right;
 			moved = true;
 		}
 
 		if (moved == false) {
 			this.facing_current = this.facing_down;
 		}
 
 		this.box.update(this.x, this.y);
 
 		List<Entity> es = Game.getEntities(this.box);
 		for (Entity e : es) {
 			if (e != this) {
 				e.collide(this);
 				this.collide(e);
 				this.box.update(this.x, this.y);
 			}
 		}
 
 	}
 
 	@Override
 	public void collide(Entity e) {
 		if (e instanceof Wall) {
 			ArrayList<Integer> dir = this.box.collideDirection(e.box);
 			if (dir.contains(Box.COLLIDE_LEFT)) {
 				this.x = this.x + this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_RIGHT)) {
 				this.x = this.x - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_DOWN)) {
 				this.y = this.y - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_UP)) {
 				this.y = this.y + this.speed;
 			}
 		}
 		// TODO: Player shouldn't be instantly kicked off the bomb
 		if (e instanceof Bomb) {
 			ArrayList<Integer> dir = this.box.collideDirection(e.box);
 			if (dir.contains(Box.COLLIDE_LEFT)) {
 				this.x = this.x + this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_RIGHT)) {
 				this.x = this.x - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_DOWN)) {
 				this.y = this.y - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_UP)) {
 				this.y = this.y + this.speed;
 			}
 
 		}
 	}
 }
