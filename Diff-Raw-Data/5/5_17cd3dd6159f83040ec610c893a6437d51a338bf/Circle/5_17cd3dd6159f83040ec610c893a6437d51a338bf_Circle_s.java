 public class Circle implements Shape {
     private double radius;
 
     public Circle() {
         this(1.0);
     }
     public Circle(double radius) {
         this.radius = radius;
     }
	public double getPerimeter() {
         return getCircumference();
     }
	public double getCircumference() {
         return 2 * Math.PI * radius;
     }
 }
