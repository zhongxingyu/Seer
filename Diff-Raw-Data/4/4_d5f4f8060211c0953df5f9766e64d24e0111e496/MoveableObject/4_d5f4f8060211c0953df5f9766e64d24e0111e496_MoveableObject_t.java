 package com.JAsteroids.LevelStuff;
 
 import com.JAsteroids.JAsteroidsUtil;
 import com.JAsteroids.Vector2f;
 
 /**
  * This class is the parent of all movable items in the game. Background textures are not included here.
  * It servers as a base for collision checking, e.t.c
  * 
  * 
  * 
  * @author Kolijn
  *
  */
 public class MoveableObject
 {
 	
 	/**
 	 * Known implementations:
 	 * 	Bullet
 	 * 	Enemy
 	 */
 	
 	public float X;
 	public float Y;
 	public float rotX = 1.0f;
 	public float rotY = 0.0f;
 	public float speedX = 0;
 	public float speedY = 0;
 	public float width;
 	public float height;
 	public float r;
 	public Vector2f[] collisionPoints; 
 	//Every object need a bounding box.
 	//Maybe introduce a Vector class?
 	
 	/**
 	 * Render the object
 	 * 
 	 * @param xpos
 	 * @param ypos
 	 */
 	public void render(int xpos, int ypos)
 	{
 		
 	}
 	
 	public int update()
 	{
 		return 0;
 	}
 	
 	public boolean checkCollision(MoveableObject a)
 	{		
 		float distance = JAsteroidsUtil.distance(this.X-a.X,this.Y-a.Y);
 		int collisions = 0;
 
 		//System.out.println(distance + "\t"+ a.r+ "\t" + this.r);
 		if (distance > a.r + this.r)
 		{
 			
 			return false;
 		}
 		else
 		{
 			System.out.println("\n");
 			
 			//Take the first vector of the first object
 			
 			Vector2f PointsThisObject[] = new Vector2f[this.collisionPoints.length];
 			Vector2f PointsOtherObject[] = new Vector2f[a.collisionPoints.length];
 			
 			
 			//Construct vectors for this object
 			for(int i =0;i<this.collisionPoints.length;i++)
 			{
 				//float temp1 = this.collisionPoints[i].x;
 				//float temp2 = this.collisionPoints[i].y;
 				
 				//Use a rotation matrix to translate:
 				//
 				//	| cos(t)  - sin(t) |
 				// | sin(t)    cos(t) |
 				
				PointsThisObject[i] = new Vector2f(this.X+(this.collisionPoints[i].x*this.rotX + this.collisionPoints[i].y*this.rotY),this.Y+(-this.collisionPoints[i].x*this.rotY + this.collisionPoints[i].y*this.rotX));
 				System.out.println(PointsThisObject[i]);
 			}
 			
 			//Construct vectors for the other object
 			for(int i =0;i<a.collisionPoints.length;i++)
 			{
 				PointsOtherObject[i] = new Vector2f(a.X+a.collisionPoints[i].x,a.Y+a.collisionPoints[i].y);
 				System.out.println("PointsOtherObject["+i+"]"+PointsOtherObject[i]);
 			}
 			
 			
 			//For each of the points on this object:
 			for (int i = 0; i<this.collisionPoints.length; i++)
 			{
 				System.out.println("Side +1");
 				Vector2f grad;
 				//Determine the gradient
 				if (i+1 == this.collisionPoints.length) 
 				{
 					//If it is the last i, use the first point again
 					grad = Vector2f.gradientNegative(PointsThisObject[i],PointsThisObject[0]);
 				}
 				else
 				{
 					//If it is the last i, use the first point again
 					grad = Vector2f.gradientNegative(PointsThisObject[i],PointsThisObject[i+1]);
 				}
 				System.out.println("\t grad "+grad);
 				
 				float myComponents[] = new float[this.collisionPoints.length];
 				float hisComponents[] = new float[a.collisionPoints.length];
 				
 				//Find the components of each of my vectors
 				for (int j =0; j< this.collisionPoints.length;j++)
 				{
 					myComponents[j] = Vector2f.component(PointsThisObject[j],grad);
 					System.out.println("\t MyComponent "+j+" "+myComponents[j] );
 				}
 				
 				for (int j =0; j< a.collisionPoints.length;j++)
 				{
 					hisComponents[j] = Vector2f.component(PointsOtherObject[j],grad);
 					System.out.println("\t HisComponent "+j+" "+hisComponents[j] );
 				}
 				
 				System.out.print("Comparing....");
 				
 				if ((JAsteroidsUtil.max(myComponents) < JAsteroidsUtil.min(hisComponents)) || (JAsteroidsUtil.min(myComponents) > JAsteroidsUtil.max(hisComponents)))
 				{
 					
 				}
 				else
 				{
 					collisions +=1;
					System.out.println("Collision Detected on this side");
 					
 				}
 				System.out.print("\n");
 			}
 			
 			if (collisions == this.collisionPoints.length)
 			{
 			return true;
 			}
 			else
 			{
 				return false;
 			}
 			
 		}
 	}
 	
 }
