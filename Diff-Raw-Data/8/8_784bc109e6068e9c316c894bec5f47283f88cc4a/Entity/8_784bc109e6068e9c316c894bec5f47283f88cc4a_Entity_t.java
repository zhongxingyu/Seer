 package nl.kozie.twodee.entity;
 
 import nl.kozie.twodee.gfx.Sprite;
 
 public abstract  class Entity {
 
 	public int x;
 	public int y;
 	
 	public int width;
 	public int height;
 	
 	public boolean solid = true;
 	public Sprite sprite;
 	
 	public Entity() {
 		this(0, 0, 0, 0);
 	}
 	
 	public Entity(int w, int h) {
 		this(w, h, 0, 0);
 	}
 	
 	public Entity(int w, int h, int x, int y) {
 		this(w, h, x, y, null);
 	}
 	
 	public Entity(int w, int h, int x, int y, Sprite spr) {
 		
 		width = w;
 		height = h;
 		this.x = x;
 		this.y = y;
 		
 		sprite = spr;
		
		init();
 	}
 	
 	public abstract void init();
 	
 	public abstract void tick(int delta);
 	
 	public abstract void render();
 	
 	public int getX() {
 		return x;
 	}
 	
 	public void setX(int x) {
 		this.x = x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public void setWidth(int w) {
 		width = w;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 	
 	public void setHeight(int h) {
 		height = h;
 	}
 	
 	public Sprite getSprite() {
 		return sprite;
 	}
 	
 	public void setSprite(Sprite spr) {
 		sprite = spr;
 	}
 	
 	public boolean isSolid() {
 		return solid;
 	}
 	
 	public void setSolid() {
 		setSolid(true);
 	}
 	
 	public void setSolid(boolean state) {
 		solid = state;
 	}
 }
