 package edu.chl.codenameg.model;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.geom.Rectangle;
 
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 import edu.chl.codenameg.model.levels.Level;
 
 /*
  * World model object
  * 
  * Contains all entities and the camera for a level in the game.
  * 
  * On update: moves all entities according to its vectors and collide types.
  * 
  */
 public class World {
 
 	private boolean gameOver;
 	private boolean hasWonGame;
 	private Camera camera;
 	private List<Entity> entities;
 	private ArrayList<PlayerCharacter> players;
 	private int amountOfPlayers;
 	
 
 	public World() {
 		this(new ArrayList<Entity>()); // Defaults to empty world
 	}
 
 	public World(Level copy) {
		this(copy.getListOfEnteties());
 		
 	}
 	public World(World copy) {
 		this(copy.getEntities());
 		this.players = copy.getPlayers();
 	}
 	public World(List<Entity> list) {
 		this.entities = new ArrayList<Entity>();
 		this.players = new ArrayList<PlayerCharacter>();
 		for(Entity e : list) {
 			this.add(e);
 		}
 		this.camera=new Camera(this);
 		this.gameOver = false;
 		this.hasWonGame = false;
 	}
 
 	public void add(Entity e) {
 		if (e instanceof PlayerCharacter) {
 			entities.add(e);
 			players.add((PlayerCharacter) e);
 			this.amountOfPlayers++;
 		} else {
 			entities.add(e);
 		}
 	}
 
 	public void remove(Entity e) {
 		if (e instanceof PlayerCharacter) {
 			entities.remove(e);
 			players.remove(e);
 			this.amountOfPlayers--;
 		} else {
 			entities.remove(e);
 		}
 	}
 
 	public List<Entity> getEntities() {
 		return new ArrayList<Entity>(this.entities);
 	}
 	
 	public ArrayList<PlayerCharacter> getPlayers() {
 		return new ArrayList<PlayerCharacter>(this.players);
 	}
 
 	public Camera getCamera() {
 		return camera;
 	}
 
 	public void update(int elapsedTime) {
 		// System.out.println("World update()!!!!!!!!");
 		for (Entity e : this.getEntities()) {
 			if (e instanceof PlayerCharacter) {
 				PlayerCharacter pc = (PlayerCharacter) e;
 				if (pc.hasWonGame() && this.getAmountOfPlayers() < 2) {
 					this.hasWonGame = true;
 					this.gameOver();
 				} else if (!pc.isAlive() && this.getAmountOfPlayers() < 2) {
 					this.gameOver();
 				}
 				if (!pc.isAlive() && this.getAmountOfPlayers() ==2) {
 					this.remove(pc);
 				}
 				/*
 				 * else if(pc.hasWonGame() && this.getAmountOfPlayers() > 2){
 				 * entities.remove(pc); this.amountOfPlayers--; } else
 				 * if(!pc.isAlive() && this.getAmountOfPlayers() > 2){
 				 * entities.remove(pc); entities.add(new
 				 * PlayerCharacter(pc.getStartPosition())); }
 				 */
 			}
 			move(e);
 			e.update(elapsedTime);
 			
 		}
 		camera.update(elapsedTime);
 	}
 
 	private void move(Entity e) {
 		Vector2D vector = e.getVector2D();
 		stepPositionX(e, vector.getX());
 		stepPositionY(e, vector.getY());
 	}
 
 	public void gameOver() {
 		this.gameOver = true;
 	}
 
 	public boolean isGameOver() {
 		return this.gameOver;
 	}
 
 	public boolean isGameWon() {
 		return this.hasWonGame;
 	}
 
 	public int getAmountOfPlayers() {
 		return this.amountOfPlayers;
 	}
 
 	private boolean stepPositionX(Entity e, float movex) {
 		// TODO float->int conversion!!!
 		int preferredx = Math.round(movex);
 		List<Entity> collided = new ArrayList<Entity>();
 		// System.out.println("NEW MOTIONX");
 		int addx = Math.signum(preferredx) > 0 ? 1 : -1;
 		for (int x = 0; x < Math.abs(preferredx); x++) {
 			for (Entity colliding : this.getEntitiesAt(new Rectangle((e.getPosition().getX()
 							+ (x * Math.signum(preferredx)))
 					+ addx,(e.getPosition().getY()), e.getHitbox()
 					.getWidth(), e.getHitbox().getHeight()))) {
 				if (colliding != e) {
 
 					Direction d1 = Math.signum(preferredx) > 0 ? Direction.LEFT
 							: Direction.RIGHT;
 					Direction d2 = Math.signum(preferredx) > 0 ? Direction.RIGHT
 							: Direction.LEFT;
 
 					if (!collided.contains(colliding)) {
 						colliding.collide(new CollisionEvent(e, d1));
 						e.collide(new CollisionEvent(colliding, d2));
 						collided.add(colliding);
 					}
 
 					// If the entity is set to collide with this entity, check
 					// if the other has this in its list, in that case try to move that
 					// away and continue.
 					if (e.getCollideTypes().contains(colliding.getType())) {
 						if (colliding.getCollideTypes().contains(e.getType())) {
 							if (!stepPositionX(colliding, Math.signum(preferredx))) {
 								return false;
 							}
 						} else {
 							return false;
 						}
 					}
 
 					// Else if the collided entity has this entity in his list,
 					// move it out of the way
 					else if (colliding.getCollideTypes().contains(e.getType())) {
 						if (!stepPositionX(colliding, Math.signum(preferredx))) {
 							return false;
 						}
 					}
 
 					// Otherwise just pass through the entity
 				}
 			}
 			// System.out.println(Math.signum(preferredx));
 			e.setPosition(new Position(e.getPosition().getX()
 					+ Math.signum(preferredx), e.getPosition().getY()));
 			
 			// Send friction to objects on top
 			for (Entity colliding : this.getEntitiesAt(new Rectangle((e.getPosition().getX()), (e.getPosition()
 					.getY()) - 1, e.getHitbox().getWidth(), 1))) {
 				if (colliding != e) {
 					if (colliding.getCollideTypes().contains(e.getType())) {
 						this.stepPositionX(colliding, Math.signum(preferredx));
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean stepPositionY(Entity e, float movey) {
 		// TODO float->int conversion!!!
 		int preferredy = Math.round(movey);
 		List<Entity> collided = new ArrayList<Entity>();// TODO
 
 		int addy = Math.signum(preferredy) > 0 ? 1 : -1;
 		for (int y = 0; y < Math.abs(preferredy); y++) {
 			for (Entity colliding : this
 					.getEntitiesAt(new Rectangle((e.getPosition()
 							.getX()), (e.getPosition().getY()
 							+ (y * Math.signum(preferredy)))
 							+ addy, e.getHitbox().getWidth(), e.getHitbox()
 							.getHeight()))) {
 				if (colliding != e) {
 
 					Direction d1 = Math.signum(preferredy) > 0 ? Direction.TOP
 							: Direction.BOTTOM;
 					Direction d2 = Math.signum(preferredy) > 0 ? Direction.BOTTOM
 							: Direction.TOP;
 
 					if (!collided.contains(colliding)) {
 						colliding.collide(new CollisionEvent(e, d1));
 						e.collide(new CollisionEvent(colliding, d2));
 						collided.add(colliding);
 					}
 
 					if (e.getCollideTypes().contains(colliding.getType())) {
 						if (colliding.getCollideTypes().contains(e.getType())) {
 							if (!stepPositionY(colliding, Math.signum(preferredy))) {
 								return false;
 							}
 						} else {
 							return false;
 						}
 					} else if (colliding.getCollideTypes()
 							.contains(e.getType())) {
 						stepPositionY(colliding, Math.signum(preferredy));
 					}
 				}
 			}
 			e.setPosition(new Position(e.getPosition().getX(), e.getPosition()
 					.getY() + Math.signum(preferredy)));
 		}
 		return true;
 	}
 
 	public List<Entity> getEntitiesAt(Rectangle target) {
 		List<Entity> list = new ArrayList<Entity>();
 		for (Entity e : this.getEntities()) {
 			Rectangle rect = new Rectangle((e.getPosition().getX()),
 					(e.getPosition().getY()), e.getHitbox()
 							.getWidth(), e.getHitbox().getHeight());
 			if (target.intersects(rect) || target.contains(rect) || rect.contains(target)) {
 				list.add(e);
 			}
 		}
 		return list;
 	}
 
 
 }
