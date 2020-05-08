 package Project.Game;
 
 import javax.xml.bind.annotation.XmlAttribute;
 
 public final class Vector2D {
 
     // fields
     @XmlAttribute(name = "x")
     public double x;
     @XmlAttribute(name = "y")
     public double y;
 
     // default is to make them immutable.
     private boolean mutable = false;
 
     // construct a zero vector
     public Vector2D() {
         x = 0;
         y = 0;
     }
 
     public Vector2D(boolean mutable) {
         x = 0;
         y = 0;
         this.mutable = mutable;
     }
 
     // construct a vector with given coordinates
     public Vector2D(double x, double y) {
         this.x = x;
         this.y = y;
     }
 
     public Vector2D(double x, double y, boolean mutable) {
         this(x, y);
         this.mutable = mutable;
     }
 
     // construct a vector that is a copy of the argument
     public Vector2D(Vector2D v) {
         this.x = v.getX();
         this.y = v.getY();
     }
 
     public Vector2D(Vector2D v, boolean mutable) {
         this(v);
         this.mutable = mutable;
     }
 
     // set coordinates
     public void set(double x, double y) {
         if (mutable) {
             this.x = x;
             this.y = y;
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
     // set coordinates to argument vector coordinates
     public void set(Vector2D v) {
         if (mutable) {
             set(v.getX(), v.getY());
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
     // compare for equality (needs to allow for Object type argument...)
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Vector2D vector2D = (Vector2D) o;
 
         if (Double.compare(vector2D.x, x) != 0) return false;
         if (Double.compare(vector2D.y, y) != 0) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result;
         long temp;
         temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L;
         result = (int) (temp ^ (temp >>> 32));
         temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L;
         result = 31 * result + (int) (temp ^ (temp >>> 32));
         return result;
     }
 
     //  magnitude (= "length") of this vector
     public double mag() {
         return (Math.sqrt(x * x + y * y));
     }
 
     // angle between vector and horizontal axis in radians
     public double theta() {
         return (Math.atan2(y, x));
     }
 
     // Returns the direction between the two vectors starting from this one
     public Vector2D getNormalDirectionBetween(Vector2D other) {
        Vector2D direction = new Vector2D(true);
         direction.x = other.x - x;
         direction.y = other.y - y;
         direction.normalise();
 //        System.out.println(direction);
         return direction;
     }
 
     // String for displaying vector as text
 
     @Override
     public String toString() {
         return "Vector2D{" +
                 "x=" + x +
                 ", y=" + y +
                 '}';
     }
 
     // add argument vector
     public void add(Vector2D v) {
         add(v.getX(), v.getY());
     }
 
     public static Vector2D add(final Vector2D first, final Vector2D second) {
         Vector2D third = new Vector2D(first, true);
         third.add(second);
         return third;
     }
 
     // add coordinate values
     public void add(double x, double y) {
         if (mutable) {
             this.x += x;
             this.y += y;
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
     public static Vector2D add(final Vector2D first, double x, double y) {
         Vector2D third = new Vector2D(first, true);
         third.add(x, y);
         return third;
     }
 
     // weighted add - frequently useful
     public void add(Vector2D v, double fac) {
         add(v.getX() * fac, v.getY() * fac);
     }
 
     public static Vector2D add(final Vector2D first, final Vector2D second, double fac) {
         Vector2D third = new Vector2D(first, true);
         third.add(second.x * fac, second.y * fac);
         return third;
     }
 
     // subtract argument vector
     public void subtract(Vector2D v) {
         subtract(v.getX(), v.getY());
     }
 
     public static Vector2D subtract(final Vector2D first, final Vector2D second) {
         Vector2D third = new Vector2D(first, true);
         third.subtract(second);
         return third;
     }
 
     // subtract coordinate values
     public void subtract(double x, double y) {
         if (mutable) {
             this.x -= x;
             this.y -= y;
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
     public static Vector2D subtract(final Vector2D first, double x, double y) {
         Vector2D third = new Vector2D(first, true);
         third.subtract(x, y);
         return third;
     }
 
     // multiply with factor
     public void multiply(double fac) {
         if (mutable) {
             this.x *= fac;
             this.y *= fac;
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
     public static Vector2D multiply(Vector2D first, double fac) {
         Vector2D second = new Vector2D(first, true);
         second.multiply(fac);
         return second;
     }
 
 // "wrap" vector with respect to given positive values w and h
 
     // method assumes that x >= -w and y >= -h
 
     public void wrap(double w, double h) {
         if (mutable) {
             if (x >= w) {
                 x = x % w;
             }
             if (y >= h) {
                 y = y % h;
             }
             if (x < 0) {
                 x = (x + w) % w;
             }
             if (y < 0) {
                 y = (y + h) % h;
             }
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
     // rotate by angle given in radians
     public void rotate(double theta) {
         set(x * Math.cos(theta) - y * Math.sin(theta), x * Math.sin(theta) + y * Math.cos(theta));
     }
 
     // scalar product with argument vector
     public double scalarProduct(Vector2D v) {
         return ((x * v.getX()) + (y * v.getY()));
     }
 
     // distance to argument vector
     public double dist(Vector2D v) {
         double tempx = (x - v.getX()) * (x - v.getX());
         double tempy = (y - v.getY()) * (y - v.getY());
 
         return (Math.sqrt(tempx + tempy));
     }
 
     public void clone(Vector2D target) {
         if (mutable) {
             this.x = target.x;
             this.y = target.y;
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
     public static void clone(Vector2D target, Vector2D source) {
         if (source.mutable) {
             source.x = target.x;
             source.y = target.y;
         } else {
             throw new IllegalAccessError("This Vector2D is immutable");
         }
     }
 
 // normalise vector so that mag becomes 1
 
     // direction is unchanged
 
     public void normalise() {
         if (mutable) {
             if (x != 0 || y != 0) {
                 multiply(1 / mag());
             }
         } else {
 
         }
     }
 
     public double getX() {
         return x;
     }
 
     public double getY() {
         return y;
     }
 }
