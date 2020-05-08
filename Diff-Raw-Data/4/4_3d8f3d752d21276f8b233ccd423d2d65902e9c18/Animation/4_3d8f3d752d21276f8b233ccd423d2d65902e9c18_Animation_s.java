 package com.musicgame.PumpAndJump.Animation;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.musicgame.PumpAndJump.game.gameStates.RunningGame;
 
 /**
  * A blank animation template that has animation methods for the players
  * Different animations will extends this class
  * @author gigemjt
  */
 public class Animation {
 
 	static Map< String, Animation > FileToAnimation = new TreeMap< String, Animation >();
 
 	public ArrayList< Keyframe > keyframes;
 	float lastValue;
 	public int dof = 1;
 	public int actor = 0;
 	public boolean isLooping;
 
 	public static void main( String[] args )
 	{
 		//test file inputs;'
 
 		/*Animation a = new Animation( "TestAnimationType0.txt" );
 		System.out.println( a.actor );
 		System.out.println( a.keyframes.size() );
 		Animation b = new Animation( "TestAnimationType1.txt" );
 		System.out.println( b.actor );
 		System.out.println( b.keyframes.size() );
 		Animation c = new Animation( "TestAnimationType2.txt" );
 		System.out.println( c.actor );
 		System.out.println( c.keyframes.size() );*/
 	}
 	public Animation()
 	{
 		keyframes = new ArrayList< Keyframe >();
 	}
 
 	public Animation( String fileName )
 	{
 		keyframes = new ArrayList< Keyframe >();
 		System.out.println( fileName );
 		loadAnimation( fileName );
 		if( actor == 1 )
 		{
 			scaleTimes( 1.0f / keyframes.get( keyframes.size() - 1 ).t );
 			scaleTimes( 1.0f / ( RunningGame.tempo/120.0f ) );
 		}
 	}
 
 	public Animation( Animation ani )
 	{
 		copy( ani );
 	}
 
 	void copy( Animation ani )
 	{
 		actor = ani.actor;
 		dof = ani.dof;
 		keyframes = new ArrayList< Keyframe >();
 		for( Keyframe kf: ani.keyframes )
 		{
 			keyframes.add( kf.copy() );
 		}
 	}
 
 	void loadAnimation( String fileName )
 	{
 		fileName = fileName.toLowerCase();
 		Animation ani = FileToAnimation.get( fileName );
 		if( ani != null )
 		{
 			copy( ani );
 		}
 		else
 		{
 			ReadAnimation( fileName );
 			FileToAnimation.put( fileName, new Animation( this ) );
 		}
 	}
 
 	void ReadAnimation( String fileName )
 	{
 		FileHandle dir =  Gdx.files.internal( fileName );
 		Scanner s = new Scanner( dir.reader() );
 
 		int type;
 		type = s.nextInt();
 
 		switch( type )
 		{
 			case -1: readType1( s ); break;
 			case -2: readType2( s ); break;
 			default: readType( s, type ); break;
 		}
 
 		normalize();
 	}
 
 	public void scaleTimes( float scalar )
 	{
 		for( Keyframe kf: keyframes )
 		{
 			kf.t *= scalar;
 		}
 	}
 
 	public void changeTime( int index, float newTime )
 	{
 		float timeDiff = newTime - keyframes.get( index ).t;
 		for( int i = index; i < keyframes.size(); i++ )
 		{
 			keyframes.get( i ).t += timeDiff;
 		}
 	}
 
 	public void changeTimeRange( int index, float[] times )
 	{
 		if( times.length > 0 )
 			return;
 		float timeDiff = 0.0f;
 		for( int i = index; i < keyframes.size(); i++ )
 		{
 			if( i-index < times.length )
 			{
 				timeDiff = times[ i-index ] - keyframes.get( i ).t;
 				keyframes.get( i ).t = times[i];
 			}
 			else
 			{
 				keyframes.get( i ).t += timeDiff;
 			}
 		}
 	}
 
 	private void normalize( )
 	{
 		for( int i = 0; i < dof; i++ )
 		{
 			int numberOfFullCircles = (int)(keyframes.get(0).pose[i] / 360);
 
 			for( int j = 0; j < keyframes.size(); j++ )
 			{
 				keyframes.get( j ).pose[ i ] -= ((float)numberOfFullCircles)*(360.0f);
 			}
 		}
 	}
 
 	private void readType1( Scanner s )
 	{
 		int size = s.nextInt();
 
 		float origY = s.nextFloat();
 
 		float t = 0.0f;
 
 		for( int i = 0; i < size; i++ )
 		{
 
 			float[] pose = new float[ dof ];
 
 			for( int j = 0; j < dof; j++ )
 			{
 				pose[j] = s.nextFloat();
 			}
 			pose[ 15 ] = (pose[15] - origY) / 5.0f;
 			t = s.nextFloat();
 			keyframes.add( new Keyframe( pose, t, i ) );
 		}
 
 		s.close();
 	}
 
 	private void readType2( Scanner s )
 	{
 		int size = s.nextInt();
 
 		float origY = s.nextFloat();
 
 		actor = s.nextInt();
 
 		dof = s.nextInt();
 
 		isLooping = s.nextBoolean();
 
 		float t = 0.0f;
 
 		for( int i = 0; i < size; i++ )
 		{
 			float[] pose = new float[ dof ];
 
 			for( int j = 0; j < dof; j++ )
 			{
 				pose[j] = s.nextFloat();
 			}
 			if( actor == 1 )
 				pose[ 15 ] = (pose[15] - origY) / 5.0f;
 			t = s.nextFloat();
 			keyframes.add( new Keyframe( pose, t, i ) );
 		}
 
 		if( actor == 0 )
 		{
 			lastValue = 0.0f;
 			for( Keyframe kf: keyframes )
 			{
 				float ydist = kf.pose[ 0 ] - lastValue;
 				float h = (float) Math.sqrt( 300.0f*300.0f +  ydist*ydist );
 				float o = ydist;
 
 				float angle = (float) Math.asin( o/h );
 
 				kf.pose[ 0 ] = 0;
 			}
 		}
 
 		s.close();
 	}
 
 	private void readType( Scanner s, int size )
 	{
 		float[] pose = new float[ dof ];
 
 		float t = 0.0f;
 
 		for( int i = 0; i < size; i++ )
 		{
 			for( int j = 0; j < dof; j++ )
 			{
 				if( j == 15 )
 					pose[j] = 0.0f;
 				else
 					pose[j] = s.nextFloat();
 			}
 			t = s.nextFloat();
 			keyframes.add( new Keyframe( pose, t, i ) );
 		}
 	}
 
 	public void addKeyFrame(float[] fs, double inputTimeReference) {
 		float ydist = (float)((fs[0] - lastValue)/Math.pow( 10, 8 ));
 		lastValue = fs[ 0 ];
		float h = (float) Math.sqrt( 1.0f +  ydist*ydist );
 		float o = ydist;
 
 		float angle = (float) Math.asin( o/h );
 		fs[0] = (float) (angle/Math.PI*180.0f);
		System.out.println( fs[0] );
 		keyframes.add( new Keyframe( fs, (float)inputTimeReference, keyframes.size() - 1 ) ); 
 	}
 
 }
