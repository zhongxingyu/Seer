 package com.silvercode.droidz.model;
 
 import android.graphics.Canvas;
 import android.util.Log;
 
 public class Explosion
 {
     public static final String TAG = Explosion.class.getSimpleName();
     
     public static final int STATE_ALIVE = 1;
     public static final int STATE_DEAD = 0;
     
     private Particle[] particles;
     private int x, y;
     private int size;
     private int state;
     
     public Explosion(int particleNr, int x, int y)
     {
     	Log.d(TAG, "Explosion started at " + x + "," + y);
         
         this.x = x;
         this.y = y;
     	this.state = STATE_ALIVE;
     	this.particles = new Particle[particleNr];
         
     	for (int i = 0; i < this.particles.length; i++)
     	{
     		Particle p = new Particle(x, y);
     		this.particles[i] = p;
     	}
         
     	this.size = particleNr;
     }
     
     public void update()
     {
     	for (int i = 0; i < this.particles.length; i++)
     	{
            particles[i].update();
     	}
     }
     
     public void draw(Canvas canvas)
     {
     	for (int i = 0; i < this.particles.length; i++)
     	{
     		particles[i].draw(canvas);
     	}
     }
     
     public boolean isAlive() { return this.state == STATE_ALIVE; }
     public boolean isDead() { return this.state == STATE_DEAD; }
 }
