 package trollhoehle.gamejam.magnets;
 
 import trollhoehle.gamejam.magnets.Player;
 
 import java.awt.Font;
 import java.util.ArrayList;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.geom.Circle;
 
 public class Management extends BasicGame {
 
     /**
      * All Entities but the Players and the ring
      */
     private ArrayList<Entity> entities;
     private Core core;
     private Ring ring;
     private ArrayList<Player> players;
     private float currentSpeed;
 
     public Management() {
 	super("Fucking magnets - How do they work?");
 	this.entities = new ArrayList<Entity>();
 	this.players = new ArrayList<Player>();
 	this.currentSpeed = 0.2f;
     }
 
     /**
      * @param args
      * @throws SlickException
      */
     public static void main(String[] args) throws SlickException {
 	AppGameContainer app = new AppGameContainer(new Management());
 
 	app.setDisplayMode(720, 720, false);
 	app.setTargetFrameRate(60);
 	app.start();
 
     }
 
     public void render(GameContainer gc, Graphics arg1) throws SlickException {
 	for (Entity e : this.entities) {
 	    e.getImg().draw(e.getX(), e.getY(),
 		    (float) ((float) e.getShape().getWidth() / (float) e.getImg().getWidth()));
 	}
 
 	ring.getImg().draw(0, 0, (float) ((float) gc.getHeight() / (float) ring.getImg().getHeight()));
 	this.ring.getImg().setCenterOfRotation(gc.getWidth() / 2, gc.getHeight() / 2);
 
 	core.getImg().draw(core.getX(), core.getY(),
 		(float) ((float) core.getShape().getWidth() / (float) core.getImg().getWidth()));
 
 	for (ObstacleSpawner os : this.core.getObstacleSpawner()) {
 	    os.getImg().draw(os.getX(), os.getY(),
 		    (float) ((float) os.getShape().getWidth() / (float) os.getImg().getWidth()));
 	}
 
 	for (Player p : this.players) {
 	    arg1.setColor(p.getColor());
 	    arg1.drawString("" + p.getHp(), p.getX(), p.getY() - 20);
 	    if (p.isInvincible()) {
 		p.getImg().draw(p.getX(), p.getY(), new Color(100, 100, 100));
 
 	    } else {
 		p.getImg().draw(p.getX(), p.getY());
 
 	    }
 	}
 
     }
 
     @Override
     public void init(GameContainer gc) throws SlickException {
 
 	this.ring = new Ring(gc.getWidth() / 2, gc.getHeight() / 2, gc.getWidth() / 2, 0.08f);
 	this.core = new Core(gc.getWidth(), gc.getHeight());
 
 	gc.getInput().addKeyListener(new MagnetKeyListener(this.players));
     }
 
     @Override
     public void update(GameContainer gc, int delta) throws SlickException {
 	Input input = gc.getInput();
 
 	Obstacle[] result;
 
 	// RING
 	//this.ring.setSpeedMultiplier(currentSpeed);
 	this.ring.getImg().rotate(-0.02f * delta - currentSpeed);
 
 	this.core.update(delta, gc.getWidth() / 2, gc.getHeight() / 2, 0.08f);
 
 	// OBSTACLE SPAWNER
 	for (ObstacleSpawner os : this.core.getObstacleSpawner()) {
 	    result = os.update(delta, gc.getWidth() / 2, gc.getHeight() / 2, 0.08f);
 
 	    // Change speed
 	    os.setSpeedMultiplier(currentSpeed);
 
 	    if (result != null) {
 		for (Entity er : result) {
 		    this.entities.add(er);
 		}
 
 	    }
 	}
 
 	// PLAYERS
 	for (int i = 0; i < this.players.size(); i++) {
 	    float attract = 0;
 
 	    Player p = this.players.get(i);
 
 	    // Change speed
 	    p.setSpeedMultiplier(currentSpeed);
 
 	    // KEY STROKES:
 	    if (input.isKeyDown(p.getButton())) {
 		attract = 0.3f;
 		p.setImg(new Image("res/images/magnet_active.png"));
 		// TODO: Particles!
 	    } else {
 		p.setImg(new Image("res/images/magnet_inactive.png"));
 		// TODO: Particles!
 	    }
 
 	    // NEW POSITION
 	    p.update(delta, gc.getWidth() / 2, gc.getHeight() / 2, attract);
 
 	    // COLLISION
 	    for (int j = i + 1; j < this.players.size(); j++) {
 		Player pc = this.players.get(j);
 		if (p.getShape().intersects(pc.getShape())) {
 		    p.collision(pc);
 		    pc.collision(p);
 		}
 
 	    }
 
 	    for (int j = 0; j < this.entities.size(); j++) {
 		Entity ec = this.entities.get(j);
 
 		// Change speed
 		ec.setSpeedMultiplier(currentSpeed);
 
 		if (p.getShape().intersects(ec.getShape())) {
 		    p.collision(ec);
 		    ec.collision(p);
 		}
 	    }
 
 	    if (!p.getShape().intersects(this.ring.getShape())) {
 		p.collision(this.ring);
 		this.ring.collision(p);
 	    }
 
 	    if (p.getShape().intersects(this.core.getShape())) {
 		p.collision(this.core);
 		this.core.collision(p);
 	    }
 	}
 
 	// OTHER ENTITES
 	for (int i = 0; i < this.entities.size(); i++) {
 	    Entity e = this.entities.get(i);
 
 	    // NEW POSITION
 	    result = e.update(delta, gc.getWidth() / 2, gc.getHeight() / 2, 0);
 
 	    if (result != null) {
 		for (Entity newE : result) {
 		    this.entities.add(newE);
 		}
 	    }
 
 	    // COLLISION
 	    for (int j = i + 1; j < this.entities.size(); j++) {
 		Entity ec = this.entities.get(j);
 
 		if (e.getShape().intersects(ec.getShape())) {
 		    e.collision(ec);
 		    ec.collision(e);
 		}
 	    }
 
 	    if (!e.getShape().intersects(this.ring.getShape())) {
 		e.collision(ring);
 		ring.collision(e);
 	    }
 	    if (e.getShape().intersects(this.core.getShape())) {
 		e.collision(core);
 		core.collision(e);
 	    }
 	}
 
 	// KILL DEAD PHYSICAL ENTITIES
 	for (int i = 0; i < this.players.size(); i++) {
	    if (this.players.get(i).getHp() == 0) {
 		this.players.remove(i);
 	    }
 	}
 	for (int i = 0; i < this.entities.size(); i++) {
 	    if (this.entities.get(i) instanceof PhysicalEntity) {
		if (((PhysicalEntity) this.entities.get(i)).getHp() == 0) {
 		    this.entities.remove(i);
 		}
 	    }
 	}
 	for (int i = 0; i < this.core.getObstacleSpawner().size(); i++) {
	    if (this.core.getObstacleSpawner().get(i).getHp() == 0) {
 		this.core.getObstacleSpawner().remove(i);
 	    }
 	}
 
 	// TODO: particles(?)
     }
 
 }
