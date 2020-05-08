 package com.ak.towerdefense.entity;
 
 import com.ak.towerdefense.map.MapPoint;
 
 /**
  * Entity is the absolute foundation of models that are
  * to be displayed on the screen. Things included in the
  * model category are (but not limited to): icons, sprites,
  * projectiles, magic effect (or effect in general).
  *
  * @author aedon
  * @date Jan 24, 2012 9:19:20 PM
  * @version
  * 
  * Possible Improvements:
  * <pre>
  * * TODO
  * </pre>
  */
 abstract public class Entity {
 	/* Our debugging tag */
 	public static final String TAG = "Entity";
 	/**
 	 * The number of entities registered to the game.
 	 * Consequently, this also may act as an id to an
 	 * Entity.
 	 */
 	private static int ENTITY_COUNT = 0; 
 	/**
 	 * This id of this Entity. The id is given at Entity
 	 * construction and is immutable to allow for long term
 	 * referencing.
 	 */
 	public final int ENTITY_ID;
 	/**
 	 * The location in the game grid that the Entity occupies.
 	 */
 	public MapPoint mMapLocation;
 	/**
 	 * The Entities absolute location in the "gl" world.
	 * TODO Create class PointF. The purpose of the class should
 	 * be to define and auto handle gl touch point and distance
 	 * conversions.
 	 */
	//public PointF mGLLocation;
 	
 	/**
 	 * Creates a new Entity and assigns it a new id. 
 	 */
 	public Entity() {
 		ENTITY_ID = ++ENTITY_COUNT;
 	}
 	
 	/**
 	 * Marks the Entity for disposal. All resources will
 	 * be released
 	 */
 	public void dispose() {
 		// TODO
 		onDispose();
 	}
 	
 	/**
 	 * Forwards propagates a nudge to dispose of all resources
 	 * to an Entity sub-class. <em>ALL</em> textures should be
 	 * released.
 	 */
	abstract protected void onDispose();
 }
