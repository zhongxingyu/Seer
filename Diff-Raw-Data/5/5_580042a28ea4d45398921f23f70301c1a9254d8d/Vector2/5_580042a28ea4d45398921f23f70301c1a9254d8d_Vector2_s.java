 package de.rockschenn.android.games.watershipdown.objects;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 public class Vector2 {
 	public static final Vector2 ZERO = new Vector2(0,0);
 	public static final Vector2 XAXIS = new Vector2(1,0);
 	public static final Vector2 YAXIS = new Vector2(0,1);
 	public double x;
 	public double y;
 	
 	//Constructs from Coordinates
 	public Vector2(double x, double y){
 		this.x = x;
 		this.y = y;
 	}
 	//Copy-Constructor
 	public Vector2(Vector2 pos){
 		this.x = pos.x;
 		this.y = pos.y;
 	}
 	//Creates Direction-Vector from Point a to Point b
 	public Vector2(Vector2 a, Vector2 b){
 		this.x = b.x-a.x;
 		this.y = b.y-a.y;
 	}
 	
 	//Returns the Scalar-Product from a and b
 	public static double getDotProduct(Vector2 a, Vector2 b){
 		return ((a.x*b.x)+(a.y*b.y));
 	}
 	
 	//Returns the length of this Vector
 	public double length(){
 		return Math.sqrt(this.x*this.x + this.y*this.y);
 	}
 	//Returns the length Vector a
 	public static double length(Vector2 a){
 		return Math.sqrt(a.x*a.x + a.y*a.y);
 	}
 	
 	//Normalizes this Vector - !!! Overrides existing values !!!
 	public void normalize(){
 		double l = this.length();
 		x = x/l;
 		y = y/l;
 	}
 	//Normalizes Vector a
 	public static Vector2 normalize(Vector2 a){
		double nx = a.x/a.length();
		double ny = a.y/a.length();
 		return new Vector2(nx,ny);
 	}
 	
 	//Returns radian angle between this Vector and x-Axis
 	public double getAngleToXAxis(){
 		return this.getAngle(Vector2.XAXIS);
 	}
 	//Returns radian angle between Vector a and x-Axis
 	public static double getAngleToXAxis(Vector2 a){
 		return a.getAngle(Vector2.XAXIS);
 	}
 	
 	//Returns radian angle between this Vector and Vector v
 	public double getAngle(Vector2 v){
 		return Math.acos(getDotProduct(this, v)/(length(this)*length(v)));
 	}
 	//Returns radian angle between Vector a and Vector b
 	public static double getAngle(Vector2 a, Vector2 b){
 		return Math.acos(getDotProduct(a, b)/(length(a)*length(b)));
 	}
 	
 	public void rotate(double angle){
 		double x = this.x;
 		double y = this.y;
 		this.x = (Math.cos(angle)*x)+(-Math.sin(angle)*y);
 		this.y = (Math.sin(angle)*x)+(Math.cos(angle)*y);
 	}
 	
 	public void drawVector(Canvas c, Vector2 startPos, Paint p){
 		c.drawLine((float)startPos.x, (float)startPos.y, (float)(startPos.x+(100*x)), (float)(startPos.y+(100*y)), p);
 	}
 	
 	public boolean equals(Vector2 v, int tolerance) {
 		if((this.x >= v.x-tolerance && this.x <= v.x+tolerance) && (this.y <= v.y+tolerance && this.y >= v.y-tolerance)){
 			return true;
 		}
 		return false;
 	}
 	
 }
