 package edu.chl.codenameg.model;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 
 public class World {
 
 	private List<Entity> entities;
 
 	public World() {
 		this.entities = new ArrayList<Entity>();
 	}
 
 	public World(World copy) {
 		this.entities = copy.getEntities();
 	}
 
 	public void add(Entity e) {
 		entities.add(e);
 	}
 
 	public List<Entity> getEntities() {
 		return new ArrayList<Entity>(this.entities);
 	}
 
 	public Camera getCamera() {
 		return new Camera();
 	}
 
 	public void update(int elapsedTime) {
 		for (Entity e : this.getEntities()) {
 			if(e instanceof PlayerCharacter) {
 				PlayerCharacter pc = (PlayerCharacter)e;
 				if(!pc.isAlive()) {
 					this.gameOver();
 				}
 			}
 			move(e);
 			e.update(elapsedTime);
 		}
 	}
 
 	private void move(Entity e) {
 		Vector2D vector = e.getVector2D();
 		motionx(e, vector.getX());
 		motiony(e, vector.getY());
 	}
 	
 	public void gameOver() {
 		//TODO implement
 	}
 
 	private boolean motionx(Entity e, float movex) {
 		// TODO float->int conversion!!!
 		int preferredx = Math.round(movex);
 		List<Entity> collided = new ArrayList<Entity>();
 
 		int addx = Math.signum(preferredx) > 0 ? 1 : -1;
 		for (int x = 0; x < Math.abs(preferredx); x++) {
 			for (Entity colliding : this.getEntitiesAt(new Rectangle(Math
 					.round(e.getPosition().getX()
 							+ (x * Math.signum(preferredx)))
 					+ addx, Math.round(e.getPosition().getY()), e.getHitbox()
 					.getWidth(), e.getHitbox().getHeight()))) {
 				if(colliding != e) {
 					if (!collided.contains(colliding)) {// Collide method calls is always sent
 						colliding.collide(e);
 						e.collide(colliding);
 					}
 					
 					if (e.getCollideTypes().contains(colliding.getType())) {// If the entity is set to collide with this entity, do not allow it to move
 						return false;
					} else if(colliding.getCollideTypes().contains(e.getType())) {// Else if the collided entity has this entity in his list, move it out of the way
						motionx(colliding, Math.signum(preferredx));
 					} 
 					//Otherwise just pass through the entity
 				}
 			}
 			e.setPosition(new Position(e.getPosition().getX()
 					+ Math.signum(preferredx), e.getPosition().getY()));
 		}
 		return true;
 	}
 
 	private boolean motiony(Entity e, float movey) {
 		// TODO float->int conversion!!!
 		int preferredy = Math.round(movey);
 		List<Entity> collided = new ArrayList<Entity>();
 
 		int addy = Math.signum(preferredy) > 0 ? 1 : -1;
 		for (int y = 0; y < Math.abs(preferredy); y++) {
 			for (Entity colliding : this
 					.getEntitiesAt(new Rectangle(Math.round(e.getPosition()
 							.getX()), Math.round(e.getPosition().getY()
 							+ (y * Math.signum(preferredy)))
 							+ addy, e.getHitbox().getWidth(), e.getHitbox()
 							.getHeight()))) {
 				if(colliding != e) {
 
 					if (!collided.contains(colliding)) { 
 						colliding.collide(e);
 						e.collide(colliding);
 					}
 					
 					if (e.getCollideTypes().contains(colliding.getType())) { 
 						return false;
 					} else if(colliding.getCollideTypes().contains(e.getType())) { 
 						motiony(colliding, Math.signum(preferredy));
 					} 
 				}
 			}
 			e.setPosition(new Position(e.getPosition().getX(), e.getPosition()
 					.getY() + Math.signum(preferredy)));
 		}
 		return true;
 	}
 
 	private List<Entity> getEntitiesAt(Position p) {
 		List<Entity> list = new ArrayList<Entity>();
 		for (Entity e : this.getEntities()) {
 			// TODO float->int :(((
 			Rectangle rect = new Rectangle(
 					Math.round(e.getPosition().getX()), Math.round(e
 							.getPosition().getY()), e.getHitbox()
 							.getWidth(), e.getHitbox().getHeight());
 			if (rect.contains(new Point(Math.round(p.getX()),
 					Math.round(p.getY())))) {
 				list.add(e);
 			}
 		}
 		return list;
 	}
 
 	private List<Entity> getEntitiesAt(Rectangle target) {
 		List<Entity> list = new ArrayList<Entity>();
 		for (Entity e : this.getEntities()) {
 			// TODO float->int :(((
 			Rectangle rect = new Rectangle(Math.round(e.getPosition().getX()),
 					Math.round(e.getPosition().getY()), e.getHitbox()
 							.getWidth(), e.getHitbox().getHeight());
 			if (target.intersects(rect)) {
 				list.add(e);
 			}
 		}
 		return list;
 	}
 
 	@Deprecated
 	private void checkCollision(Entity e) {
 		for (Entity target : this.getEntities()) {
 			if (target != e) {
 				boolean collision = false;
 
 				// TODO Collision detection without using rounded doubles!!
 
 				Rectangle thisRectangle = new Rectangle(new Point(Math.round(e
 						.getPosition().getX()), Math.round(e.getPosition()
 						.getY())), new Dimension(
 						e.getHitbox().getWidth(), e.getHitbox().getHeight()));
 
 				Rectangle targetRectangle = new Rectangle(new Point(
 						Math.round(target.getPosition().getX()),
 						Math.round(target.getPosition().getY())),
 						new Dimension(target.getHitbox().getWidth(), target
 								.getHitbox().getHeight()));
 
 				if (thisRectangle.intersects(targetRectangle)) {
 					collision = true;
 
 					if (e.getCollideTypes().contains(target.getType())) {
 						Rectangle intersection = thisRectangle
 								.intersection(targetRectangle);
 
 						// Collision on bottom
 						if (intersection.y == targetRectangle.y) {
 							e.setPosition(new Position(e.getPosition().getX(),
 									e.getPosition().getY()
 											- intersection.height));
 						}
 						// Collision on top
 						if (intersection.y + intersection.height == targetRectangle.y+targetRectangle.height) {
 							System.out.println(e.getType() + " collided on top");
 							e.setPosition(new Position(e.getPosition().getX(),
 									e.getPosition().getY()
 											+ intersection.height));
 						}
 						// Collision on right
 						if (intersection.x == targetRectangle.x) {
 							e.setPosition(new Position(e.getPosition().getX()
 									- intersection.width, e.getPosition()
 									.getY()));
 						}
 						// Collision on left
 						if (intersection.x + intersection.width == targetRectangle.x + targetRectangle.width) {
 							System.out.println(e.getType() + " collided on left");
 							e.setPosition(new Position(e.getPosition().getX()
 									+ intersection.width, e.getPosition()
 									.getY()));
 						}
 					}
 				}
 				if (collision) {
 					e.collide(target);
 					target.collide(e);
 				}
 			}
 
 		}
 	}
 
 }
