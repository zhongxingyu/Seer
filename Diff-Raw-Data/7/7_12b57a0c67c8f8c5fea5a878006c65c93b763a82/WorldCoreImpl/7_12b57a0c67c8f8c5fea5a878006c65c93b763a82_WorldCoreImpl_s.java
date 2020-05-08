 package com.github.joukojo.testgame.world.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.github.joukojo.testgame.Player;

 public class WorldCoreImpl implements WorldCore {
 
 	private final static Logger LOG = LoggerFactory
 			.getLogger(WorldCoreImpl.class);
 
 	private final Map<String, List<Drawable>> drawableObjects;
 	private final Map<String, List<Moveable>> moveableObjects;
 
 	public WorldCoreImpl() {
 		drawableObjects = new Hashtable<String, List<Drawable>>();
 		moveableObjects = new Hashtable<String, List<Moveable>>();
 	}
 
 	public Drawable getDrawableObject(final String objectName) {
 		final List<Drawable> drawables = getDrawableObjects(objectName);
 
 		if (drawables != null && !drawables.isEmpty()) {
 			return drawables.get(0);
 		} else {
 			return null;
 		}
 	}
 
 	public List<Drawable> getDrawableObjects(final String objectName) {
 		return drawableObjects.get(objectName);
 	}
 
 	public void addObject(final String objectName, final Drawable drawable) {
 		if (drawableObjects.containsKey(objectName)) {
 			drawableObjects.get(objectName).add(drawable);
 		} else {
 			List<Drawable> objectList = new ArrayList<Drawable>();
 			objectList.add(drawable);
 			drawableObjects.put(objectName, objectList);
 		}
 	}
 
 	@Override
 	public List<Moveable> getAllMoveables() {
 		Collection<List<Moveable>> allValues = moveableObjects.values();
 		List<Moveable> allMoveableObjects = new ArrayList<Moveable>();
 		for (List<Moveable> list : allValues) {
 			allMoveableObjects.addAll(list);
 		}
 
 		return allMoveableObjects;
 	}
 
 	@Override
 	public void cleanMoveables() {
 		Set<String> keys = moveableObjects.keySet();
 
 		for (String key : keys) {
 			List<Moveable> entries = moveableObjects.get(key);
 			List<Moveable> stillActiveEntries = new ArrayList<Moveable>();
 			for (int i = 0; i < entries.size(); i++) {
 				Moveable moveable = entries.get(i);
 
 				if (!moveable.isDestroyed()) {
 					stillActiveEntries.add(moveable);
 				}
 			}
 
 			moveableObjects.put(key, stillActiveEntries);
 
 		}
 
 	}
 
 	@Override
 	public List<Drawable> getAllDrawables() {
 		Collection<List<Drawable>> allValues = drawableObjects.values();
 
 		List<Drawable> allDrawableObjects = new ArrayList<Drawable>();
 		for (List<Drawable> list : allValues) {
 			allDrawableObjects.addAll(list);
 		}
 		return allDrawableObjects;
 	}
 
 	@Override
 	public void addMoveable(String objectName, Moveable moveable) {
 
 		boolean containsKey = moveableObjects.containsKey(objectName);
 
 		if (containsKey) {
 			List<Moveable> list = moveableObjects.get(objectName);
 			list.add(moveable);
 		} else {
 			List<Moveable> list = new ArrayList<Moveable>();
 			list.add(moveable);
 			moveableObjects.put(objectName, list);
 		}
 
 	}
 
 	@Override
 	public Moveable getMoveable(String objectName) {
 		List<Moveable> objects = getMoveableObjects(objectName);
 		Moveable moveable = null;
 		if (objects != null && !objects.isEmpty()) {
 			moveable = objects.get(0);
 		}
 		return moveable;
 	}
 
 	@Override
 	public List<Moveable> getMoveableObjects(String objectName) {
 		List<Moveable> allMoveableObjects = new ArrayList<Moveable>();
 		
 		List<Moveable> moveableObjectList = moveableObjects.get(objectName);
 		
 		if( moveableObjectList != null ) {
 			allMoveableObjects.addAll(moveableObjectList);
 		}
 		
 		return allMoveableObjects;
 
 	}
 
 	@Override
 	public void removeMoveable(String objectName, Moveable moveable) {
 		List<Moveable> objects = getMoveableObjects(objectName);
 
 		boolean isRemoved = objects.remove(moveable);
 
 		LOG.debug("isRemoved: {}", isRemoved);
		int i = 0;
 	}
 
 	@Override
 	public List<String> getMoveableObjectNames() {
 		
 		return new ArrayList<String>(moveableObjects.keySet());
 	}
 }
