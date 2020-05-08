 package com.musicgame.PumpAndJump.objects;
 
 import com.musicgame.PumpAndJump.game.gameStates.RunningGame;
 
 public class DuckObstacle extends Obstacle {
 
 	boolean triggered = false;
 	public DuckObstacle( float Start, float End)
 	{
 		super( Start, End, 70.0f, 60.0f );
 		image.setColor( 0.0f, 1.0f, 1.0f, 1.0f );
 	}
 
 	public void Impacted( float tempo )
 	{
 		if( !triggered )
 		{
		//	triggered = true;
 			RunningGame.score--;
 		}
 	}
 }
