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
 
 package com.ridiculousRPG.movement.auto;
 
 import java.util.Map;
 
 import javax.script.ScriptEngine;
 
 import com.badlogic.gdx.Gdx;
 import com.ridiculousRPG.GameBase;
 import com.ridiculousRPG.event.EventObject;
 import com.ridiculousRPG.event.PolygonObject;
 import com.ridiculousRPG.event.handler.EventHandler;
 import com.ridiculousRPG.movement.Movable;
 import com.ridiculousRPG.movement.MovementHandler;
 import com.ridiculousRPG.util.ObjectState;
 
 /**
  * This {@link MovementHandler} tries to move an event along the given polygon.
  * The move waits if a blocking event exists on it's way.<br>
  * After succeeding the switch finished is set to true.
  * 
  * @author Alexander Baumgartner
  */
 public class MovePolygonAdapter extends MovementHandler {
 	private static final long serialVersionUID = 1L;
 
 	private PolygonObject polygon;
 	private String execScript;
 	private String polygonName;
 	private boolean polygonChanged;
 	private boolean rewind;
 	private boolean crop;
 
 	private static String NODE_TEMPLATE;
 
 	public boolean isRewind() {
 		return rewind;
 	}
 
 	public void setRewind(boolean rewind) {
 		this.rewind = rewind;
 		if (finished)
 			polygonChanged = true;
 	}
 
 	public boolean isCrop() {
 		return crop;
 	}
 
 	public void setCrop(boolean crop) {
 		this.crop = crop;
 	}
 
 	public MovePolygonAdapter(PolygonObject polygon) {
 		this(polygon, false);
 	}
 
 	public MovePolygonAdapter(PolygonObject polygon, boolean rewind) {
 		this.rewind = rewind;
 		setPolygon(polygon);
 	}
 
 	public MovePolygonAdapter(String polyName) {
 		this(polyName, false);
 	}
 
 	public MovePolygonAdapter(String polyName, boolean rewind) {
 		this.rewind = rewind;
 		this.polygonName = polyName;
 	}
 
 	public PolygonObject getPolygon() {
 		return polygon;
 	}
 
 	public void setPolygon(PolygonObject polygon) {
 		this.polygon = polygon;
 		this.polygonName = polygon.getName();
 		polygonChanged = true;
 	}
 
 	public String getPolygonName() {
 		return polygonName;
 	}
 
 	public void setPolygonName(String polygonName) {
 		this.polygonName = polygonName;
 	}
 
 	public boolean offerPolygons(Map<String, PolygonObject> polyMap) {
 		if (polygonName == null)
 			return false;
 		PolygonObject newPoly = polyMap.get(polygonName);
 		if (newPoly == null)
 			return false;
 		this.polygon = newPoly;
 		polygonChanged = true;
 		return true;
 	}
 
 	@Override
 	public void tryMove(Movable event, float deltaTime) {
 		if (execScript != null) {
 			if (NODE_TEMPLATE == null)
 				NODE_TEMPLATE = Gdx.files.internal(
 						GameBase.$options().eventNodeTemplate).readString(
 						GameBase.$options().encoding);
 			try {
 				String script = GameBase.$scriptFactory()
 						.prepareScriptFunction(execScript, NODE_TEMPLATE);
 				GameBase.$().getSharedEngine().put(ScriptEngine.FILENAME,
 						"onNode-Event-" + polygonName);
 				ObjectState state = null;
 				if (event instanceof EventObject) {
 					EventHandler h = ((EventObject) event).getEventHandler();
 					if (h != null)
 						state = h.getActualState();
 				}
 				GameBase.$().invokeFunction(script, "onNode", event, state,
 						polygon, this);
 			} catch (Exception e) {
 				GameBase.$error("PolygonObject.onNode", "Could not execute "
 						+ "onNode script for " + event + " " + polygon, e);
 			}
 			execScript = null;
 		}
 		if (polygonChanged) {
 			reset();
 		} else if (finished) {
 			event.stop();
 		}
 		if (!finished && polygon != null) {
 			float distance = event.getMoveSpeed().computeStretch(deltaTime);
 			if (distance > 0) {
				execScript = polygon.moveAlong(distance, crop);
 				event.offerMove(polygon.getRelativeX(), polygon.getRelativeY());
 				if (event instanceof EventObject) {
 					((EventObject) event).animate(polygon.getRelativeX(),
 							polygon.getRelativeY(), deltaTime);
 				}
 				finished = polygon.isFinished();
 			}
 		}
 	}
 
 	@Override
 	public void moveBlocked(Movable event) {
 		if (polygon != null)
 			polygon.undoMove();
 		execScript = null;
 		event.stop();
 	}
 
 	@Override
 	public void reset() {
 		super.reset();
 		polygonChanged = false;
 		execScript = null;
 		if (polygon != null)
 			polygon.start(rewind);
 	}
 }
