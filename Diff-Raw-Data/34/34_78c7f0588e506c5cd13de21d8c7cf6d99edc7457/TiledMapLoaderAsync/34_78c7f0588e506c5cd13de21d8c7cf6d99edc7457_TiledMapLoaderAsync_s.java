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
 
 package com.madthrax.ridiculousRPG.map.tiled;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 
 import javax.script.ScriptException;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.madthrax.ridiculousRPG.GameBase;
 import com.madthrax.ridiculousRPG.ObjectState;
 import com.madthrax.ridiculousRPG.event.EventObject;
 import com.madthrax.ridiculousRPG.event.handler.EventHandler;
 import com.madthrax.ridiculousRPG.map.MapLoader;
 import com.madthrax.ridiculousRPG.map.MapWithEvents;
 
 /**
  * This asynchronous map loader allows loading the new map while blending out
  * the old one. It also allows to store the old maps state asynchronously while
  * blending in the new map.
  * 
  * @author Alexander Baumgartner
  */
 public class TiledMapLoaderAsync extends Thread implements
 		MapLoader<EventObject> {
 
 	private boolean disposed = false;
 	private boolean done = true;
 	private boolean disposeMap = false;
 	private String filePath;
 	private MapWithEvents<EventObject> map;
 	private ScriptException loadException;
 
 	TiledMapLoaderAsync() {
 		start();
 	}
 
 	@Override
 	public synchronized void run() {
 		GameBase.$().registerGlContextThread();
 		while (true) {
 			while (done) {
 				if (disposed) {
 					notifyAll();
 					return;
 				}
 				try {
 					wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			if (map != null && filePath != null) {
 				// store map
 				FileHandle fh = Gdx.files.external(map.getExternalSavePath());
 				try {
 					HashMap<Integer, ObjectState> eventsById = new HashMap<Integer, ObjectState>(
 							100);
 					ObjectOutputStream oOut = new ObjectOutputStream(
 							fh.write(false));
 					for (EventObject event : map.getAllEvents()) {
 						EventHandler handler = event.getEventHandler();
 						if (handler != null) {
 							eventsById.put(event.id, handler.getActualState());
 						}
 					}
 					oOut.writeObject(eventsById);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				if (disposeMap)
 					map.dispose(true);
 				map = null;
 			} else if (filePath != null) {
 				// load map
 				try {
 					map = loadTiledMap();
 				} catch (ScriptException e) {
 					loadException = e;
 				}
 			}
 			done = true;
 			notify();
 		}
 	}
 
 	private MapWithEvents<EventObject> loadTiledMap() throws ScriptException {
 		return new TiledMapWithEvents(filePath);
 	}
 
 	public synchronized void startLoadMap(String tmxPath) {
 		// Wait until outstanding operation has completed.
 		while (!done) {
 			if (disposed) {
 				notifyAll();
 				return;
 			}
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		this.filePath = tmxPath;
 		done = false;
 		if (GameBase.$().isGlAsyncLoadable())
 			notify();
 	}
 
 	public synchronized MapWithEvents<EventObject> endLoadMap()
 			throws ScriptException {
 		try {
 			if (GameBase.$().isGlAsyncLoadable()) {
 				// Wait until the map has been loaded by the thread.
 				while (!done) {
 					if (disposed) {
 						notifyAll();
 						return null;
 					}
 					try {
 						wait();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				if (loadException != null)
 					throw loadException;
 			} else {
 				try {
 					map = loadTiledMap();
 				} finally {
 					done = true;
 					notify();
 				}
 			}
 			return map;
 		} finally {
 			map = null;
 			loadException = null;
 		}
 	}
 
 	public synchronized void storeMapState(MapWithEvents<EventObject> map,
 			boolean disposeMap) {
 		// Wait until outstanding operation has completed.
 		while (!done) {
 			if (disposed) {
 				notifyAll();
 				return;
 			}
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		this.disposeMap = disposeMap;
 		this.map = map;
 		this.filePath = map.getExternalSavePath();
 		this.done = false;
 		notify();
 	}
 
 	@Override
 	public void dispose() {
 		disposed = true;
 		synchronized (this) {
 			notifyAll();
 		}
 	}
 }
