 public class Vector {
 	private float x;
 	private float y;
 	
 	public Vector() {
 		x = 0;
 		y = 0;
 	}
 	
 	public Vector(float x, float y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	// Copy constructor
 	public Vector(Vector position) {
 		x = position.x;
 		y = position.y;
 	}
 
 	public float x() {
 		return x;
 	}
 	
 	public float y() {
 		return y;
 	}
 	
 	public float setX(float x) {
		this.x = x;
 		return this.x;
 	}
 	
 	public float setY(float y) {
		this.y = y;
 		return this.y;
 	}	
 	
 	public float theta() {
 		return (float)Math.atan2(y, x);
 	}
 	
 	public float magnitude() {
 		return (float)Math.sqrt(x * x + y * y);
 	}
 	
 	/**
 	 * returns the distance between two vectors squared which is faster for comparisons
 	 * @param there
 	 * @return
 	 */
 	public float distance2(Vector there) {
 		float dx = this.x - there.x;
 		float dy = this.y - there.y;
 		return dx * dx + dy * dy;
 	}
 	
 	public float distance(Vector there) {
 		return (float)Math.sqrt(distance2(there));
 	}
 
 	public void incrementBy(Vector velocity) {
 		x += velocity.x;
 		y += velocity.y;
 	}
 
 	public void incrementXBy(float delta) {
 		x += delta;
 	}
 	
 	public void incrementYBy(float delta) {
 		y += delta;
 	}
 }
