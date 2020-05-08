 package com.derby.trainraid;
 
 import android.graphics.Point;
 
 
 public abstract class AttackEntity extends Entity{
 	public AttackEntity(int hitPoints, int fileLoc, int x, int y, int shotSpeed, Entity currentTarget) 
 	{
 		super(hitPoints, fileLoc, x, y);
 		this.shotSpeed = shotSpeed;
 		this.currentTarget = currentTarget;
 		// TODO Auto-generated constructor stub
 	}
 	String preferedTarget;
 	Entity currentTarget;
 	int damagePerShot;
 	int shotSpeed;
 	int timeSinceShot;
 	
 	public Point GetTargetMiddle()
 	{
 		Point result = new Point();
 		result.x = currentTarget.GetX() + (currentTarget.getWidth() / 2);
 		result.y = currentTarget.GetY() + (currentTarget.getHeight() / 2);
 		return result;
 	}
 	
 	void setPreferedTarget(String target)
 	{
 		preferedTarget = target;
 	}
 	void setCurrentTarget(Entity target)
 	{
 		currentTarget = target;
 	}
 	void setDamagePerShot(int damage)
 	{
 		damagePerShot = damage;
 	}
 	int getShotSpeed()
 	{
 		return shotSpeed;
 	}
 	void setShotSpeed(int speed)
 	{
 		shotSpeed = speed;
 	}
 	
 	String getPreferedTarget()
 	{
 		return preferedTarget;
 	}
 	Entity getCurrentTarget()
 	{
 		return currentTarget;
 	}
 	int getDamagePerShot()
 	{
 		return damagePerShot;
 	}
 	
 	void ChangeTarget(Entity newTarget)
 	{
 		currentTarget = newTarget;
 	}
 	
 	abstract Projectile Shoot(int bulletHeight, int bulletWidth);
 	
 	public int getTimeSinceShot()
 	{
 		return timeSinceShot;
 	}
 	
 	public void IncrementTimeSinceShot()
 	{
 		timeSinceShot++;
 	}
 	
 }
