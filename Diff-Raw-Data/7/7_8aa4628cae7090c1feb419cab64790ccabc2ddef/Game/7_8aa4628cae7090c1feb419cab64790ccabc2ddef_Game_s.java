 package game;
 
 import interaction.Input;
 import interaction.KeyBindings;
 
 import java.util.ArrayList;
 
 import main.Battlepath;
 
 import Entities.CollisionEntity;
 import Entities.Entity;
 import Entities.Projectile;
 import Entities.Unit;
 
 import collision.CollisionSystem;
 import collision.MovementSystem;
 
 import util.Vector2D;
 
 import engine.Field;
 import engine.GlobalInfo;
 import engine.Pathplanner;
 
 public class Game {
 	
 	public Field field;
 	public Pathplanner pathPlanner;
 	public CollisionSystem collisionSystem;
 	public MovementSystem movementSystem;
 	public Input input;
 	public Unit u;
 	//TODO: Merge these
 	public ArrayList<Entity> entities = new ArrayList<Entity>();
 	public ArrayList<Entity> addList = new ArrayList<Entity>();
 	public ArrayList<Entity> deleteList = new ArrayList<Entity>();
 	
 	public View view;
 	public GameMode mode;
 	
 	boolean[] lastMouseState = new boolean[3];
 	double lastShot = 0;
 	
 	public double dt;
 	
 	public Game(Vector2D startpos) {
 		movementSystem = new MovementSystem(this);
 		u = new Unit(startpos, this);
 		entities.add(u);
 	}
 	
 	public void step(double dt) {
 		this.dt = dt;
 		processInput(dt);
 		
 		for(Entity e : entities) {
 			e.process(dt);
 		}
 		
 		movementSystem.process(getCollisionEntities());
 		
 		entities.removeAll(deleteList);
 		deleteList.clear();
 		entities.addAll(addList);
 		addList.clear();
 		
 		view.process(dt);
 	}
 	
 	public void setMode(GameMode gm) {
 		mode = gm;
 		switch(mode) {
 		case ACTION:
 			view.follow(u);
 			break;
 		case STRATEGY:
 			view.unfollow();
 			break;
 		}
 	}
 	
 	public ArrayList<CollisionEntity> getCollisionEntities() {
 		ArrayList<CollisionEntity> es = new ArrayList<CollisionEntity>();
 		for(Entity e : entities) {
 			if(e instanceof CollisionEntity) {
 				es.add((CollisionEntity) e);
 			}
 		}
 		return es;
 	}
 	
 	public void particleExplosion(Vector2D pos, int n) {
 		for (int j = 0;j<=n;j++)
 			addList.add(new Particle(pos, Vector2D.fromAngle(j*1.8, 1), Math.random()/4+0.1, Math.random()*10, 20, this));
 	}
 	
 	public void toggleMode() {
 		if(mode == GameMode.ACTION) {
 			setMode(GameMode.STRATEGY);
 		}
 		else if(mode == GameMode.STRATEGY) {
 			setMode(GameMode.ACTION);
 		}
 	}
 	
 	public void processInput(double dt) {
 		//Mouse
 		
 		if(input.mouseButtonPressed[0] && !lastMouseState[0]) {
 			u.moveTo(input.getCursorPos());
 		}
 		
 		if((input.mouseButtonPressed[2] && GlobalInfo.time - lastShot > 0.3)) {
 			u.shoot(input.getCursorPos().subtract(u.pos).normalize());
 			lastShot = GlobalInfo.time;
 		}
 		
 		System.arraycopy(input.mouseButtonPressed, 0, lastMouseState, 0, input.mouseButtonPressed.length);
 		
 		//Keyboard part one (input.isPressed)
 		
 		if(input.isPressed(KeyBindings.MOVE_LEFT)) u.velocity.x = 1;
 		else if(input.isPressed(KeyBindings.MOVE_RIGHT)) u.velocity.x = -1;
 		else u.velocity.x = 0;
 		
 		if(input.isPressed(KeyBindings.MOVE_DOWN)) u.velocity.y = 1;
 		else if(input.isPressed(KeyBindings.MOVE_UP)) u.velocity.y = -1;
 		else u.velocity.y = 0;
 		
 		if(u.velocity.length() > 0)
 			u.velocity = u.velocity.normalize().scalar(10);
 		
 		if(input.isPressed(KeyBindings.SCROLL_LEFT)) view.velocity.x = 20;
 		else if(input.isPressed(KeyBindings.SCROLL_RIGHT)) view.velocity.x = -20;
 		else view.velocity.x = 0;
 		
 		if(input.isPressed(KeyBindings.SCROLL_UP)) view.velocity.y = 20;
 		else if(input.isPressed(KeyBindings.SCROLL_DOWN)) view.velocity.y = -20;
 		else view.velocity.y = 0;
 		
 		//Keyboard part two (input.getKeyBuffer)
 		
 		for(int key : input.getKeyBuffer()) {
 			switch(key) {
 				case KeyBindings.ZOOM_IN:
 					view.setZoom(view.zoom+0.1);
 					break;
 				case KeyBindings.ZOOM_OUT:
 					view.setZoom(view.zoom-0.1);
 					break;
 				case 't':
 					toggleMode();
 					break;
 				case 'r':
					u = new Unit(Battlepath.findStartPos(field), this);
					entities.add(u);
 					break;
 			}
 		}
 		
 	}
 	
 	public void emitShot(Vector2D start, Vector2D direction) {
 		Projectile p = new Projectile(start, direction, this);
 		addList.add(p);
 	}
 	
 }
