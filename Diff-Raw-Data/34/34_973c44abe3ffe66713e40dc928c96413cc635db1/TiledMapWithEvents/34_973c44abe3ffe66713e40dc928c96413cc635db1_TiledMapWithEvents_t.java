 /*
  * Copyright 2011 Alexander Baumgartner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ridiculousRPG.map.tiled;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.script.ScriptException;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledLayer;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
 import com.badlogic.gdx.math.Rectangle;
 import com.ridiculousRPG.DebugHelper;
 import com.ridiculousRPG.GameBase;
 import com.ridiculousRPG.event.EventFactory;
 import com.ridiculousRPG.event.EventObject;
 import com.ridiculousRPG.event.EventTrigger;
 import com.ridiculousRPG.event.EventTriggerAsync;
 import com.ridiculousRPG.event.PolygonObject;
 import com.ridiculousRPG.event.handler.EventHandler;
 import com.ridiculousRPG.map.MapLoader;
 import com.ridiculousRPG.map.MapRenderRegion;
 import com.ridiculousRPG.map.MapWithEvents;
 import com.ridiculousRPG.movement.auto.MovePolygonAdapter;
 import com.ridiculousRPG.util.ExecuteInMainThread;
 import com.ridiculousRPG.util.IntSet;
 import com.ridiculousRPG.util.ObjectState;
 
 /**
  * This class represents a tiled map with events on this map.<br>
  * The events may move around on the map.<br>
  * Don't forget to dispose the map and all events if you don't need it anymore.
  * 
  * @author Alexander Baumgartner
  */
 public class TiledMapWithEvents implements MapWithEvents<EventObject> {
 	private static final long serialVersionUID = 1L;
 
 	private static transient MapLoader<EventObject> mapLoader;
 	private transient TileAtlas atlas;
 	private transient int width, height;
 	private transient int tileWidth, tileHeight;
 	private String tmxPath;
 
 	private int localIdCount = 0;
 	private int globalIdCount = 1000000000;
 	// TODO: NEEDS REFACTORING
 	private IntSet intSet = new IntSet();
 
 	// tiles
 	private transient MapRenderRegion[] staticRegions;
 	private transient Map<String, PolygonObject> polyMap;
 	// events
 	private List<EventObject> dynamicRegions = new ArrayList<EventObject>(50);
 	// named events
 	private Map<String, EventObject> namedRegions = new HashMap<String, EventObject>(
 			30);
 
 	private static transient EventTrigger eventTrigger;
 
 	TiledMapWithEvents(String tmxPath) throws ScriptException {
 		TiledMap map = loadTileMap(tmxPath);
 
 		loadStaticTiles(map);
 		loadEvents(map);
 	}
 
 	private TiledMap loadTileMap(String tmxPath) {
 		this.tmxPath = tmxPath;
 		final FileHandle tmxFile = Gdx.files.internal(tmxPath);
 		final TiledMap map = TiledLoader.createMap(tmxFile);
 		tileWidth = map.tileWidth;
 		tileHeight = map.tileHeight;
 		width = map.width * tileWidth;
 		height = map.height * tileHeight;
 		if (GameBase.$().isGlContextThread()) {
 			atlas = new TileAtlas(map, tmxFile.parent());
 		} else {
 			new ExecuteInMainThread() {
 				@Override
 				public void exec() {
 					atlas = new TileAtlas(map, tmxFile.parent());
 				}
 			}.runWait();
 		}
 		return map;
 	}
 
 	private void loadStaticTiles(TiledMap map) {
 		int i, j, k;
 		int len_i, len_j, len_k;
 		int z;
 		ArrayList<MapRenderRegion> alTmp = new ArrayList<MapRenderRegion>(1000);
 		for (i = 0, len_i = map.layers.size(); i < len_i; i++) {
 			TiledLayer l = map.layers.get(i);
 			if (EventFactory.isSkip(l.properties))
 				continue;
 			int[][] layerTiles = l.tiles;
 			int layer_z = EventFactory.getZIndex(l.properties);
 			for (j = 0, len_j = layerTiles.length; j < len_j; j++) {
 				int[] row = layerTiles[j];
 				float rowY = (len_j - (j + 1)) * map.tileHeight;
 				for (k = 0, len_k = row.length; k < len_k; k++) {
 					int tile = row[k];
 					if (tile > 0) {
 						z = layer_z + EventFactory.getZIndex(map, tile);
 						AtlasRegion region = (AtlasRegion) atlas
 								.getRegion(tile);
 						if (region == null) {
 							System.out.println("TILE-REGION " + tile
 									+ " IS NULL");
 						} else {
 							alTmp.add(new MapRenderRegion(region, k
 									* map.tileWidth + region.offsetX, rowY
 									+ region.offsetY, z));
 						}
 					}
 				}
 			}
 		}
 		Collections.sort(alTmp);
 		staticRegions = alTmp.toArray(new MapRenderRegion[alTmp.size()]);
 	}
 
 	private void loadEvents(TiledMap map) throws ScriptException {
 		int i, j, len_i, len_j;
 		EventObject ev;
 		Map<Integer, ObjectState> eventsById = loadStateFromFS();
 		for (i = 0, len_i = map.objectGroups.size(); i < len_i; i++) {
 			TiledObjectGroup group = map.objectGroups.get(i);
 			if (EventFactory.isSkip(group.properties))
 				continue;
 			for (j = 0, len_j = group.objects.size(); j < len_j; j++) {
 				TiledObject object = group.objects.get(j);
 				if (EventFactory.isSkip(object.properties))
 					continue;
 				if (object.polygon != null) {
 					createPolyMove(map, group, object, object.polygon, true);
 					continue;
 				}
 				if (object.polyline != null) {
 					createPolyMove(map, group, object, object.polyline, false);
 					continue;
 				}
 				ev = new EventObject(object, group, atlas, map);
 				if (object.gid > 0) {
 					ev.z += EventFactory.getZIndex(map, object.gid);
 				}
 				EventFactory.parseProps(ev, group.properties);
 				EventFactory.parseProps(ev, object.properties);
 				put(object.name, ev);
 			}
 		}
 		Map<String, EventObject> globalEv = GameBase.$().getGlobalEventsClone();
 		// Initialize the events
 		for (i = 0, len_i = dynamicRegions.size(); i < len_i; i++) {
 			EventObject eventObj = dynamicRegions.get(i);
 			// Polygons are defined on the map and may not be available at event
 			// creation time, that's why we have to initialize them here.
 			if (eventObj.getMoveHandler() instanceof MovePolygonAdapter) {
 				((MovePolygonAdapter) eventObj.getMoveHandler())
 						.offerPolygons(polyMap);
 			}
 			if (eventObj.isGlobalEvent()) {
 				EventObject globalObj = globalEv.remove(eventObj.name);
 				if (globalObj == null) {
 					eventObj.init();
 					GameBase.$().getGlobalEvents().put(eventObj.name, eventObj);
 					if (EventObject.EVENT_TYPE_PLAYER
 							.equalsIgnoreCase(eventObj.type))
 						eventObj.consumeInput = true;
 				} else {
 					globalObj.clearCollision();
 					put(globalObj.name, globalObj).dispose();
 				}
 			} else {
 				if (eventObj.getEventHandler() != null
 						&& eventsById.containsKey(eventObj.id)) {
 					eventObj.getEventHandler().setState(
 							eventsById.get(eventObj.id));
 				}
 				eventObj.init();
 			}
 		}
 		for (EventObject globalObj : globalEv.values()) {
 			globalObj.clearCollision();
 			put(globalObj.name, globalObj);
 		}
 
 		// insert half-planes around the map
 		put(null, ev = new EventObject());
 		ev.setTouchBound(new Rectangle(-1000f, -1000f, width + 2000f, 1000f));
 		put(null, ev = new EventObject());
 		ev.setTouchBound(new Rectangle(-1000f, -1000f, 1000f, height + 2000f));
 		put(null, ev = new EventObject());
 		ev.setTouchBound(new Rectangle(-1000f, height, width + 2000f, 1000f));
 		put(null, ev = new EventObject());
 		ev.setTouchBound(new Rectangle(width, -1000f, 1000f, height + 2000f));
 	}
 
 	private void createPolyMove(TiledMap map, TiledObjectGroup group,
 			TiledObject object, String polygon, boolean loop) {
 		String name = object.name;
 		if (name == null || name.length() == 0)
 			return;
 		String[] sa = polygon.split(" ");
		int len = loop ? sa.length + 1 : sa.length;
		float[] verticesX = new float[len];
		float[] verticesY = new float[len];
 		float x = object.x;
 		float y = map.height * map.tileHeight - object.y;
 		for (int i = sa.length - 1; i > -1; i--) {
 			String point = sa[i];
 			// don't use regex split for performance reasons
 			int index = point.indexOf(',');
 			verticesX[i] = x + Integer.parseInt(point.substring(0, index));
 			verticesY[i] = y - Integer.parseInt(point.substring(index + 1));
 		}
		if (loop) {
			verticesX[len - 1] = verticesX[0];
			verticesY[len - 1] = verticesY[0];
		}
 		PolygonObject poly = new PolygonObject(name, verticesX, verticesY, loop);
 		EventFactory.parseProps(poly, group.properties);
 		EventFactory.parseProps(poly, object.properties);
 
 		if (polyMap == null)
 			polyMap = new HashMap<String, PolygonObject>();
 		polyMap.put(name, poly);
 	}
 
 	/**
 	 * Loads the current state from the file system.<br>
 	 * The path used for loading the state is determined by the method
 	 * {@link #getExternalSavePath()}.
 	 * 
 	 * @see #getExternalSavePath()
 	 * @see #saveStateToFS()
 	 * @return A map with {@link EventObject#id} as key and the state
 	 *         {@link ObjectState} as value
 	 */
 	@SuppressWarnings("unchecked")
 	protected Map<Integer, ObjectState> loadStateFromFS() {
 		FileHandle fh = getExternalSavePath();
 		if (fh.exists()) {
 			try {
 				ObjectInputStream stateIn = new ObjectInputStream(fh.read());
 				Map<Integer, ObjectState> stateMap = (Map<Integer, ObjectState>) stateIn
 						.readObject();
 				stateIn.close();
 				return stateMap;
 			} catch (Exception e) {
 				GameBase.$error("TiledMap.loadState",
 						"Could not load the map state from the file system", e);
 			}
 		}
 		return new HashMap<Integer, ObjectState>();
 	}
 
 	/**
 	 * Saves the current state to the file system.<br>
 	 * The path used for saving the state is determined by the method
 	 * {@link #getExternalSavePath()}.
 	 * 
 	 * @see #getExternalSavePath()
 	 * @see #loadStateFromFS()
 	 */
 	@Override
 	public void saveStateToFS() {
 		FileHandle fh = getExternalSavePath();
 		try {
 			HashMap<Integer, ObjectState> eventsById = new HashMap<Integer, ObjectState>(
 					100);
 			ObjectOutputStream oOut = new ObjectOutputStream(fh.write(false));
 			for (EventObject event : getAllEvents()) {
 				EventHandler handler = event.getEventHandler();
 				if (handler != null) {
 					eventsById.put(event.id, handler.getActualState());
 				}
 			}
 			oOut.writeObject(eventsById);
 			oOut.close();
 		} catch (IOException e) {
 			GameBase.$error("TiledMap.saveState",
 					"Could not save the map state onto the file system", e);
 		}
 	}
 
 	public EventObject put(String name, EventObject event) {
 		computeId(event);
 		EventObject old = null;
 		if (name != null && name.length() > 0) {
 			old = namedRegions.put(name, event);
 		}
 		if (old != null) {
 			dynamicRegions.set(dynamicRegions.indexOf(old), event);
 		} else {
 			dynamicRegions.add(event);
 		}
 		return old;
 	}
 
 	public void put(EventObject event) {
 		computeId(event);
 		dynamicRegions.add(event);
 	}
 
 	// TODO: NEEDS REFACTORING
 	private void computeId(EventObject event) {
 		int id = event.id;
 		if (id == -1) {
 			nextId(event);
 		} else if (id < (event.isGlobalEvent() ? globalIdCount : localIdCount)) {
 			for (int i = 0, len = dynamicRegions.size(); i < len; i++) {
 				if (dynamicRegions.get(i).id == id) {
 					nextId(dynamicRegions.get(i));
 					return;
 				}
 			}
 		} else {
 			intSet.put(id);
 		}
 	}
 
 	// TODO: NEEDS REFACTORING
 	private void nextId(EventObject eventToSet) {
 		if (eventToSet.isGlobalEvent()) {
 			while (intSet.containsKey(globalIdCount))
 				globalIdCount++;
 			eventToSet.id = globalIdCount;
 			globalIdCount++;
 		} else {
 			while (intSet.containsKey(localIdCount))
 				localIdCount++;
 			eventToSet.id = localIdCount;
 			localIdCount++;
 		}
 	}
 
 	public EventObject get(String name) {
 		return namedRegions.get(name);
 	}
 
 	public List<EventObject> getAllEvents() {
 		return dynamicRegions;
 	}
 
 	public EventObject remove(String name) {
 		EventObject old = namedRegions.remove(name);
 		if (old != null) {
 			dynamicRegions.remove(old);
 		}
 		return old;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Width of one tile
 	 * 
 	 * @return
 	 */
 	public int getTileWidth() {
 		return tileWidth;
 	}
 
 	/**
 	 * Height of one tile
 	 * 
 	 * @return
 	 */
 	public int getTileHeight() {
 		return tileHeight;
 	}
 
 	/**
 	 * Move all events on the map, animate events, compute reachable events,...
 	 * 
 	 * @param deltaTime
 	 */
 	public void compute(float deltaTime, boolean actionKeyDown) {
 		if (eventTrigger == null) {
 			// Uses a shared context to load textures in other thread
 			eventTrigger = new EventTriggerAsync();
 		}
 		eventTrigger.compute(deltaTime, actionKeyDown, dynamicRegions);
 	}
 
 	// TODO: NEEDS REFACTORING
 	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
 		List<EventObject> dynamicRegions = this.dynamicRegions;
 		Collections.sort(dynamicRegions);
 		// Load pointers into register
 		MapRenderRegion[] staticRegions = this.staticRegions;
 		MapRenderRegion region;
 		Rectangle drawBound;
 		// Load variables into register
 		float camX1 = camera.position.x;
 		float camX2 = camera.position.x + camera.viewportWidth;
 		float camY1 = camera.position.y;
 		float camY2 = camera.position.y + camera.viewportHeight;
 		float rX, rY;
 
 		int i = 0;
 		EventObject event = dynamicRegions.get(0); // is never empty
 		int dynSize = dynamicRegions.size();
 		// If there are performance problems:
 		// 1) Add only MapRenderRegions with z>0 to staticRegions
 		// 2) Build a new 3-dim array with [row][col][layer] for all
 		// MapRenderRegions with z==0
 		// 3) compute firstRow, lastRow, firstCol, lastCol
 		// 4) iterate over
 		// [firstRow<row<lastRow][firstCol<col<lastCol][allLayers]
 		// com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer is not usable
 		// (Tested and felt really bad)
 		for (int j = 0, statSize = staticRegions.length; j < statSize; j++) {
 			region = staticRegions[j];
 			rX = region.x;
 			rY = region.y;
 			if (rX < camX2 && rY < camY2 && rX + region.width > camX1
 					&& rY + region.height > camY1) {
 				while (dynSize > i && event.compareTo(region) == -1) {
 					if (event.visible) {
 						drawBound = event.drawBound;
 						if (drawBound.x < camX2 && drawBound.y < camY2
 								&& drawBound.x + drawBound.width > camX1
 								&& drawBound.y + drawBound.height > camY1)
 							event.draw(spriteBatch);
 					}
 					i++;
 					if (dynSize > i)
 						event = dynamicRegions.get(i);
 				}
 				region.draw(spriteBatch);
 			}
 		}
 		while (dynSize > i) {
 			event = dynamicRegions.get(i);
 			if (event.visible) {
 				drawBound = event.drawBound;
 				if (drawBound.x < camX2 && drawBound.y < camY2
 						&& drawBound.x + drawBound.width > camX1
 						&& drawBound.y + drawBound.height > camY1)
 					event.draw(spriteBatch);
 			}
 			i++;
 		}
 		if (debug) {
 			spriteBatch.end();
 			DebugHelper.debugEvents(dynamicRegions);
 			spriteBatch.begin();
 		}
 	}
 
 	public void dispose() {
 		dispose(false);
 	}
 
 	public void dispose(final boolean recycle) {
 		if (GameBase.$().isGlContextThread()) {
 			disposeInternal(recycle);
 		} else {
 			Gdx.app.postRunnable(new Runnable() {
 				@Override
 				public void run() {
 					disposeInternal(recycle);
 				}
 			});
 		}
 	}
 
 	private void disposeInternal(boolean recycle) {
 		for (int i = 0, size = dynamicRegions.size(); i < size; i++) {
 			EventObject event = dynamicRegions.get(i);
 			if (!event.isGlobalEvent()) {
 				event.dispose();
 			}
 		}
 		if (atlas != null)
 			atlas.dispose();
 		staticRegions = null;
 		dynamicRegions = null;
 		namedRegions = null;
 		if (!recycle) {
 			if (eventTrigger != null) {
 				eventTrigger.dispose();
 				eventTrigger = null;
 			}
 			if (mapLoader != null) {
 				mapLoader.dispose();
 				mapLoader = null;
 			}
 		}
 	}
 
 	@Override
 	public FileHandle getExternalSavePath() {
 		return GameBase.$tmpPath().child(
 				tmxPath.replaceFirst("(?i)\\.tmx$", "").replaceAll("\\W", "_")
 						+ ".sav");
 	}
 
 	/**
 	 * Obtain the map loader to load tiled maps
 	 */
 	public static MapLoader<EventObject> getMapLoader() {
 		if (mapLoader == null) {
 			// Uses a shared context to load textures in other thread
 			mapLoader = new TiledMapLoaderAsync();
 		}
 		return mapLoader;
 	}
 
 	private void writeObject(ObjectOutputStream out) throws IOException {
 		out.defaultWriteObject();
 	}
 
 	private void readObject(ObjectInputStream in) throws IOException,
 			ClassNotFoundException {
 		in.defaultReadObject();
 
 		TiledMap map = loadTileMap(tmxPath);
 		loadStaticTiles(map);
 		for (EventObject ev : dynamicRegions) {
 			if (ev.gid > 0)
 				ev.setImage((AtlasRegion) atlas.getRegion(ev.gid));
 		}
 	}
 
 }
