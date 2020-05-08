 public class Vector2
 {
 	public double x;
 	public double y;
 
 	/**Create a Vector2 object with coordinates 0, 0.**/
 	public Vector2()
 	{
 		this.set(0, 0);
 	}
 
 	/**Create a Vector2 object with coordinates x, y.**/
 	public Vector2(double x, double y)
 	{
 		this.set(x, y);
 	}
 
 	/**Create a Vector2 object with coordinates of Vector2 object vec.**/
 	public Vector2(Vector2 vec)
 	{
 		this(vec.x, vec.y);
 	}
 
 	/**Set the coordinates to x, y.**/
 	public void set(double x, double y)
 	{
 		this.x = x;
 		this.y = y;
 	}
 
 	/**Set the coordinates as Vector2 object vec.**/
 	public void set(Vector2 vec)
 	{
 		x = vec.x;
 		y = vec.y;
 	}
 
 	/**Override the String formatting.**/
 	public String toString()
 	{
 		return String.format("Vector2(x=%f, y=%f)", x, y);
 	}
 
 	/**Print the coordinates of a Vector2 object.**/
 	public void printSelf()
 	{
 		System.out.println(this);
 	}
 
 	/**Return a vector that is the sum of two vectors.**/
 	public Vector2 add(Vector2 rhs)
 	{
 		x += rhs.x;
 		y += rhs.y;
 		return new Vector2(this);
 	}
 
 	/**Return a vector that is the difference between two vectors.**/
 	public Vector2 subtract(Vector2 rhs)
 	{
 		x -= rhs.x;
 		y -= rhs.y;
 		return new Vector2(this);
 	}
 
 	/**Return a vector that has been scaled to 'scalar'.**/
 	public Vector2 scale(double scalar)
 	{
 		x *= scalar;
 		y *= scalar;
 		return new Vector2(x, y);
 	}
 
 	/**Return the dot product of coordinates for Vector2 object rhs.**/
 	public double dotProduct(Vector2 rhs)
 	{
 		return x * rhs.x + y * rhs.y;
 	}
 
 	/**Return the length of the vector.**/
 	public double getLength()
 	{
 		return Math.hypot(x, y);
 	}
 
	/**Return a Vector2 object that has been normalized to unit length.**/
	public Vector2 normalize()
 	{
 		double xx = x/Math.hypot(x, y);
 		double yy = y/Math.hypot(x, y);
 		this.x = xx;
 		this.y = yy;
		return new Vector2(x, y);
 	}
 
 	/**Return the distance between two vectors.**/
 	public double getDistance(Vector2 rhs)
 	{
 		return Math.hypot(rhs.x - this.x, rhs.y - this.y);
 	}
 
 	public Vector2 get()
 	{
 		return new Vector2(this);
 	}
 }
