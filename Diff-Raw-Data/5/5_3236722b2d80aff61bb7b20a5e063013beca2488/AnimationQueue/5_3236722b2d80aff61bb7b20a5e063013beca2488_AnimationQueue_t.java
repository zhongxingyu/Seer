 package com.musicgame.PumpAndJump.Animation;
 
 import com.musicgame.PumpAndJump.Util.AnimationUtil;
 
 //any animation placed in this class must have at least 3 animations.
 public class AnimationQueue {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	Keyframe[] queue;
 	public boolean isLooping;
 	public boolean startOfLoop = false;
 	public boolean stop = false;
 	int lastKeyFrame;
 	float lastTime;
 	Animation ani;
 	
 	public AnimationQueue( Animation startAnimation, float[] intialPosition )
 	{
 		queue = new Keyframe[4];
 		
 		lastKeyFrame = 0;
 		
 		ani = startAnimation;
 		
 		isLooping = startAnimation.isLooping;
 		
 		lastTime = 0.0f;
 		
 		queue[0] = new Keyframe( intialPosition, -1.0f, 0 );
 		queue[1] = ani.keyframes.get( 0 ).copy();
 		queue[2] = ani.keyframes.get( 1 ).copy();
 		queue[3] = ani.keyframes.get( 2 ).copy();
 	}
 	
 	private void pushKeyFrame()
 	{
 		queue[0] = queue[1];
 		queue[1] = queue[2];
 		queue[2] = queue[3];
 		
 		lastKeyFrame++;
 		
 		if( lastKeyFrame < ani.keyframes.size() )
 		{
 			queue[3] = ani.keyframes.get( lastKeyFrame ).copy();
 		}
 		else
 		{
 			if( isLooping )
 			{
 				if( lastKeyFrame % ani.keyframes.size() == 0 )
 				{
 					queue[0].t -= lastTime;
					queue[0].normalize();
 					queue[1].t -= lastTime;
					queue[1].normalize();
 					queue[2].t -= lastTime; 
					queue[2].normalize();
 					lastTime = 0.0f;
 					lastKeyFrame = 1;	
 				}
 				queue[3] = ani.keyframes.get( 1 ).copy();
 			}
 			else
 			{
 				stop = true;
 			}	
 		}
 	}
 	
 	public void switchAnimation( Animation a, float[] pose )
 	{
 		ani = a;
 		lastKeyFrame = 0;
 		queue[ 0 ] = queue[ 1 ];
 		queue[ 0 ].normalize();
 		queue[ 1 ] = new Keyframe( pose, -.3f, 0);
 		queue[ 0 ].t = queue[1].t - (lastTime - queue[ 0 ].t);
 		queue[ 2 ] = a.keyframes.get( 0 ).copy();
 		queue[ 3 ] = a.keyframes.get( 1 ).copy();
 	}
 	
 	public float[] getPose( float changeInTime )
 	{
 		float highTime = queue[2].t;
 		float currentTime = lastTime + changeInTime;
 		
 		//for( int i = 0; i < )
 		
 		while( highTime <  currentTime && !stop )
 		{
 			pushKeyFrame();
 			//queue[2].print(); 
 			currentTime = lastTime + changeInTime;
 			highTime = queue[2].t;
 		}
 
 		lastTime += changeInTime;
 
 		float[] newpos = new float[ ani.dof];
 
 		if( !isLooping && lastTime > ani.keyframes.get( ani.keyframes.size() - 1 ).t )
 		{
 			Keyframe kf = ani.keyframes.get( ani.keyframes.size() - 1 );
 			for( int i = 0; i < ani.dof; i++ )
 			{
 				newpos[i] = kf.pose[ i ];
 			}
 		}
 		else 
 		{
 			for( int i = 0; i < ani.dof; i++ )
 			{
 				float newP;
 				if( Math.abs( queue[3].pose[i] - queue[0].pose[i] )*.05f > Math.abs(queue[2].pose[i] - queue[1].pose[i])  )
 				{
 					newP = AnimationUtil.lerp( (currentTime - queue[1].t)/( queue[2].t - queue[1].t ), queue[1].pose[i], queue[2].pose[i] );
 				}
 				else
 				{
 					newP = AnimationUtil.catmullrom( (currentTime - queue[1].t)/( queue[2].t - queue[1].t ), queue[0].pose[i], queue[1].pose[i], queue[2].pose[i], queue[3].pose[i] );
 				}
 				newpos[i] = ( newP%360.0f );
 				
 				//System.out.print( newpos[i]+"," );
 			}
 		}
 		//System.out.println();
 
 		return newpos;
 	}
 
 }
