 package server;
 
 import server.GsonExclusionStrategy.noGson;
 
 public abstract class Entity {
 	protected int id;
 	protected float x;
 	protected float y;
 	protected float height = 20f;
 	protected float width = 20f;
 	@noGson
 	protected float speed = 5;
 	// XXX: Does nothing?
 	@noGson
 	protected boolean collisionResolving = false;
 	@noGson
 	private boolean collisionState;
 	protected String type = "entity";
 	
 	public Entity(int id, float x, float y) {
 		this.id = id;
 
 		this.x = x;
 		this.y = y;
 	}
 	
 	public Entity(float x, float y){
 		this.id = IdFactory.getNextId();
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * 
 	 * @param game
 	 * @param dirX
 	 * @param dirY
 	 * @return whether or not you hit a wall
 	 */
	public boolean moveOnMap(Game game, float dirX, float dirY) {
		float deltax = this.speed * dirX;
		float deltay = this.speed * dirY;
		if (deltax != 0 && deltay != 0) {
			deltax /= Math.sqrt(2);
			deltay /= Math.sqrt(2);
		}
 		boolean hit = Util.moveOnMap(game, this, deltax, deltay);
 		if(this.collisionResolving)
 			this.resolveCollisions(game);
 		return hit;
 	}
 	
 	protected void resolveCollisions(Game game){
 		for(Entity other : Util.getEntitiesOverlapping(game.getPlayersList(), this)){
 			if(other.isCollisionResolving())
 				Util.resolveCollision(game, this, other);
 		}
 	}
 
 	public int getId() {
 		return this.id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public float getWidth() {
 		return width;
 	}
 
 	public float getHeight() {
 		return height;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 	public void setCollisionState(boolean state) {
 		this.collisionState = state;
 	}
 
 	public boolean collisionState() {
 		return this.collisionState;
 	}
 	
 	public abstract void brain(Game game);
 	
 	@Override
 	public boolean equals(Object o1){
 		return o1 instanceof Entity && ((Entity) o1).getId() == this.getId();
 	}
 	
 	/**
 	 * we override the hashCode, as entities with the same id are equal.
 	 */
 	@Override
 	public int hashCode() {
 		return this.getId();
 	}
 	
 	public double getRadius(){
 		return Math.min(this.getWidth()/2, this.getHeight()/2);
 	}
 
 	public boolean isCollisionResolving() {
 		return this.collisionResolving;
 	}
 
 	public void setSpeed(float speed){
 		this.speed = speed;
 	}
 	
 	public void hitByBullet(){
 		//Empty
 	}
 	
 	public String getType() {
 		return this.type;
 	}
 }
