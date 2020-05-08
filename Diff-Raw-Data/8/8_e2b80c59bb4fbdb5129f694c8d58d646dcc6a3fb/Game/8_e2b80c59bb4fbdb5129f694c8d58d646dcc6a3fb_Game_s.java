 package game;
 
 import interaction.Input;
 import interaction.KeyBindings;
 
 import java.util.ArrayList;
 
 import collision.CollisionSystem;
 
 import util.Vector2D;
 
 import engine.Field;
 import engine.GlobalInfo;
 import engine.Pathplanner;
 
 
 public class Game {
 	
 	public Field field;
 	public Pathplanner pathPlanner;
 	public CollisionSystem collisionSystem;
 	public Input input;
 	public Unit u;
 	public ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
 	public ArrayList<Particle> particles = new ArrayList<Particle>();
 	
 	boolean[] lastMouseState = new boolean[2];
 	double lastShot = 0;
 	
 	public double dt;
 	
 	public Game(Vector2D startpos) {
 		u = new Unit(startpos, this);
 	}
 	
 
 	
 	public void step(double dt) {
 		this.dt = dt;
 		processInput();
 		
 		u.process(dt);
 		ArrayList<Projectile> delList = new ArrayList<Projectile>();
 		for (int i=0;i<projectiles.size();i++) {
 			
 			Projectile proj = projectiles.get(i);
 			proj.process(dt);
 
 			if(collisionSystem.collide(proj) != null) {
 				for (int j = 0;j<=200;j++)
 					particles.add(new Particle(proj.pos, Vector2D.fromAngle(j*1.8, 1), Math.random()/2+0.1, Math.random()*5, this));
 				delList.add(proj);
 			}
 		}
 		projectiles.removeAll(delList);
 		for (int i=0;i<particles.size();i++) {
 			Particle part = particles.get(i);
 			part.process(dt);
 			if(part.destroyed)
 				particles.remove(i);
 		}
 	}
 	
 	public void processInput() {
 		if(input.mouse1down && !lastMouseState[0]) u.moveTo(input.cursorPos);
		if((input.mouse2down && !lastMouseState[1]) || (input.mouse2down && GlobalInfo.time - lastShot > 0.5)) {
 			projectiles.add(
 					new Projectile(u.pos, input.cursorPos.subtract(u.pos),this));
 			lastShot = GlobalInfo.time;
 		}
 		
 		if(input.isPressed(KeyBindings.MOVE_LEFT)) u.velocity.x = 5;
 		else if(input.isPressed(KeyBindings.MOVE_RIGHT)) u.velocity.x = -5;
 		else u.velocity.x = 0;
 		
 		if(input.isPressed(KeyBindings.MOVE_DOWN)) u.velocity.y = 5;
 		else if(input.isPressed(KeyBindings.MOVE_UP)) u.velocity.y = -5;
 		else u.velocity.y = 0;
 		
 		lastMouseState[0] = input.mouse1down;
 		lastMouseState[1] = input.mouse2down;
 	}
 	
 }
