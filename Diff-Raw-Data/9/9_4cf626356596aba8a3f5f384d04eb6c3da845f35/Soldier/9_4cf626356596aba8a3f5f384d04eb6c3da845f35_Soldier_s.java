 package com.kvantnysning.castle.objects;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 
 public class Soldier implements Graphics {
 
 	Bitmap bitmap;
 	
 	protected static final double damageMultiplyer = 1;
 	protected static final double g = 10;
 	protected static final double radius = 40;
 	
 	protected double walkSpeed;
 	
 	protected double px, py, vx, vy;
 	
 	protected double health = 100;
 	
 	protected boolean onFloor;
 	
 	protected double FloorY = 500;
 	
 	public Soldier(Bitmap bitmap, double x, double y, double speed){
 		
 		this.bitmap = bitmap;
 		
 		px = x;
 		py = FloorY;
 		vx = speed;
 		walkSpeed = speed;
 		
 		onFloor = false;
 		
 	}
 	
 	/*
 	 *  Return true if the soldier is dead.
 	 */
 	public boolean update(double Ts){
 		
 		px += Ts*vx;
 		
 		if(!onFloor){
			py += Ts*vy;
			vy += g;
 		}
 		
 		if( py>FloorY && vy>0 ){
 			
 			health -= damageMultiplyer*vy;
 			py = FloorY;
 			
 			vx = walkSpeed;
 			vy = 0;
 			onFloor = true;
 			
 			if(health<0){
 				return true;
 			}
 		}
 		
 		
 		return false;
 		
 	}
 
 	private static final int offsetX=0, offsetY=0;
 	
 	@Override
 	public void draw(Canvas canvas) {
 
 		canvas.drawBitmap(bitmap, (float) px+offsetX, (float) py+offsetY, null);
 		
 	}
 	
 	public void setPosition(double x, double y){
 		px = x;
 		py = y;
 	}
 	
 	public void setThrow(double x, double y, double vx, double vy){
 		
 		onFloor = false;
 		
 		px = x;
 		py = y;
 		this.vx = vx;
 		this.vy = vy;
 		
 	}
 	
 	public boolean isClicked(double x, double y){
 		return Math.sqrt( (px-x)*(px-x) + (py-y)*(py-y)) > radius;
 	}
 	
 }
