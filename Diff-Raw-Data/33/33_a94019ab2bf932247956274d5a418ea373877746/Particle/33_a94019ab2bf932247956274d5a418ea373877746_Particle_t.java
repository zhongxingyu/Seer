 package com.silvercode.droidz.model;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 
 public class Particle
 {
     public static final int STATE_ALIVE = 0;
     public static final int STATE_DEAD = 1;
     
     public static final int DEFAULT_LIFETIME = 200;
     public static final int MAX_DIMENSION = 5;
    public static final int MAX_SPEED = 1;
     
     private int state;
     private float width;
     private float height;
     private float x,y;
     private double xv, yv;
     private int age;
     private int lifetime;
     private int color;
     private Paint paint;
     
     public Particle(int x, int y)
     {
     	this.x = x;
     	this.y = y;
         
     	this.state = Particle.STATE_ALIVE;
     	this.width = rndInt(1, MAX_DIMENSION);
         this.height = this.width;
         this.lifetime = DEFAULT_LIFETIME;
         this.age = 0;
         this.xv = (rndDbl(0, MAX_SPEED * 2) - MAX_SPEED);
         this.yv = (rndDbl(0, MAX_SPEED * 2) - MAX_SPEED);
         
         // Smoothing out the diagonal speed
         if (xv * xv + yv * yv > MAX_SPEED * MAX_SPEED)
         {
         	xv *= 0.7;
         	yv *= 0.7;
         }
         
         this.color = Color.argb(255, rndInt(0, 255), rndInt(0, 255), rndInt(0, 255));
         this.paint = new Paint(this.color);
     }
     
     public void update()
     {
         if (this.state != STATE_DEAD)
         {
         	this.x += this.xv;
         	this.y += this.yv;
         	
             // Extract the alpha
         	int a = this.color >>> 24;
             a -= 2; // fade by 2
             
             if (a <= 0)
             {
             	this.state = STATE_DEAD;
             }
             else
             {
                 this.color = (this.color & 0x00ffffff) + (a << 24);
                 this.age++;
             }
             
             if (this.age >= this.lifetime)
             {
             	this.state = STATE_DEAD;
             }
         }
     }
     
     public void draw(Canvas canvas)
     {
     	paint.setColor(color);
     	canvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, paint);
     }
     
     static int rndInt(int min, int max)
     {
 		return (int) (min + Math.random() * (max - min + 1));
 	}
 
 	static double rndDbl(double min, double max)
 	{
 		return min + (max - min) * Math.random();
 	}
    
	public boolean isAlive() { return this.state == STATE_ALIVE; }
	public boolean isDead() { return this.state == STATE_DEAD; }
 }
