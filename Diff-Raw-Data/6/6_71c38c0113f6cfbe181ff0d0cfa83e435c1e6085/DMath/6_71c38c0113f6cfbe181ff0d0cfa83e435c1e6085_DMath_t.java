 package com.phyloa.dlib.util;
 
 import javax.vecmath.Point2f;
 import javax.vecmath.Tuple2f;
 import javax.vecmath.Vector2f;
 
 public class DMath
 {
 	public static final float PIF = (float)Math.PI;
 	public static final float PI2F = (float)(Math.PI*2);
 	public static final double PI2 = Math.PI*2;
 	/**
 	 * Returns a random float from 0 to 1. Casts Math.random() to a float.
 	 * 
 	 * @return the random float from 0 to 1
 	 */
 	public static float randomf()
 	{
 		return (float)Math.random();
 	}
 	
 	/**
 	 * Returns a random float from min to max
 	 * 
 	 * @param min
 	 *            the minimum possible random number
 	 * @param max
 	 *            the maximum possible random number
 	 * @return the random number.
 	 */
 	public static float randomf( float min, float max )
 	{
 		float r = (float)Math.random();
 		return r * (max - min) + min;
 	}
 	
 	/**
 	 * Returns the maximum int of all the values passed.
 	 * 
 	 * @param c
 	 *            the ints to find the max of
 	 * @return the maximum int
 	 */
 	public static int max( int... c )
 	{
 		int max = -Integer.MAX_VALUE;
 		for( int i = 0; i < c.length; i++ )
 		{
 			max = max > c[i] ? max : c[i];
 		}
 		return max;
 	}
 	
 	/**
 	 * Returns the maximum float of all the values passed.
 	 * 
 	 * @param c
 	 *            the floats to find the max of
 	 * @return the maximum float
 	 */
 	public static float maxf( float... c )
 	{
 		float max = -Float.MAX_VALUE;
 		for( int i = 0; i < c.length; i++ )
 		{
 			max = max > c[i] ? max : c[i];
 		}
 		return max;
 	}
 	
 	/**
 	 * Returns the minimum float of all the values passed.
 	 * 
 	 * @param c
 	 *            the floats to find the max of
 	 * @return the maximum float
 	 */
 	public static float minf( float... c )
 	{
 		float min = Float.MAX_VALUE;
 		for( int i = 0; i < c.length; i++ )
 		{
 			min = min < c[i] ? min : c[i];
 		}
 		return min;
 	}
 	
 	/**
 	 * Returns the maximum of a series of Comparables. If two are the same, the
 	 * one occurring first is returned.
 	 * 
 	 * @param c
 	 *            The comparables to compare
 	 * @return the maximum Comparable
 	 */
 	public static Comparable max( Comparable... c )
 	{
 		Comparable max = c[0];
 		for( int i = 1; i < c.length; i++ )
 		{
 			if( c[i].compareTo( max ) > 0 )
 				max = c[i];
 		}
 		return max;
 	}
 	
 	public static int mod( int n, int m )
 	{
 		int x = n % m;
 		if( x < 0 )
 			x += m;
 		return x;
 	}
 	
 	public static float bound( float v, float min, float max )
 	{
 		if( min > v ) return min;
 		if( max < v ) return max;
 		return v;
 	}
 	
 	public static int bound( int v, int min, int max )
 	{
 		if( min > v ) return min;
 		if( max < v ) return max;
 		return v;
 	}
 	
 	public static float lerp( float t, float a, float b) 
 	{ 
 		return a + t * (b - a); 
 	}
 	
 	public static float map( float v, float inmin, float inmax, float outmin, float outmax )
 	{
 		return lerp( (v - inmin) / inmax, outmin, outmax );
 	}
 	
 	public static float cosf( float a )
 	{
 		return (float)Math.cos( a );
 	}
 	
 	public static float sinf( float a )
 	{
 		return (float)Math.sin( a );
 	}
 	
 	public static float turnTowards( float heading, float desiredHeading )
 	{
 		while( heading < 0 )
 			heading += Math.PI*2;
 		while( desiredHeading < 0 )
 			desiredHeading += Math.PI*2;
 		while( heading > Math.PI*2 )
 			heading -= Math.PI*2;
 		while( desiredHeading > Math.PI*2 )
 			desiredHeading -= Math.PI*2;
 		double delta = heading - desiredHeading;
 		if( delta < -Math.PI )
 			delta += Math.PI*2;
 		if( delta > Math.PI )
 			delta -= Math.PI*2;
 		return (float)-delta;
 		
 	}
 	
 	public static Vector2f pointToLineSegment( Point2f p0, Vector2f dir, Point2f p ) 
 	{
 		p0 = new Point2f( p0 );
 		dir = new Vector2f( dir );
 		p = new Point2f( p );
 		Vector2f pMinusP0 = new Vector2f( p );
 		pMinusP0.sub( p0 );
 		float denom = dir.dot(dir);
 		float t = dir.dot( pMinusP0 ) / denom;
 
 		if( t < 0.f ) {
 			p.sub( p0 );
 			return new Vector2f( p );
 		}
 		else if( t > 1.f ) {
 			p0.add( dir );
 			p.sub( p0 );
 			return new Vector2f( p );
 		}
 
 		dir.scale( t );
 		dir.add( p0 );
 		p.sub( dir );
 
 		return new Vector2f( p );
 	}
 	
 	public static float posOnLineByPerpPoint( Point2f p0, Vector2f dir, Point2f p )
 	{
 		p0 = new Point2f( p0 );
 		dir = new Vector2f( dir );
 		p = new Point2f( p );
 		Vector2f pMinusP0 = new Vector2f( p );
 		pMinusP0.sub( p0 );
 		float denom = dir.dot(dir);
 		float t = dir.dot( pMinusP0 ) / denom;
 		return t;
 	}
 	
 	public static Point2f lineLineIntersection( Tuple2f a1, Tuple2f a2, Tuple2f b1, Tuple2f b2 )
 	{
 		float d = (a1.x-a2.x)*(b1.y-b2.y) - (a1.y-a2.y)*(b1.x-b2.x);
 		if( d == 0 )
 			return null;
 		
 		//SACRIFICING READABILITY...maybe find a slower, more readable version
 		//11/26/11...Why did I think it would be better to make it slower? if it works, it works!
 		//11/28/12 lol @ me
 		float x1 = ((b1.x-b2.x)*(a1.x*a2.y-a1.y*a2.x)-(a1.x-a2.x)*(b1.x*b2.y-b1.y*b2.x))/d;
 		float y1 = ((b1.y-b2.y)*(a1.x*a2.y-a1.y*a2.x)-(a1.y-a2.y)*(b1.x*b2.y-b1.y*b2.x))/d;
 		return new Point2f( x1, y1 );
 	}
 
 	public static int randomi( int min, int max )
 	{
 		return (int)DMath.randomf( min, max ); 
 	}
 	
 	public static String humanReadableNumber( long i )
 	{
 		if( i < 10000 ) return Long.toString( i );
		if( i < 1000000 ) return Math.round( (i*10.0 / 1000) ) / 10.0 + " Thousand";
		if( i < 1000000000 ) return Math.round( (i*10.0 / 1000000) ) / 10.0 + " Million";
		if( i < 1000000000000l ) return Math.round( (i*10.0 / 1000000000) ) / 10.0 + " Billion";
 		return Long.toString( i );
 	}
 }
