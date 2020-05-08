 /*
  * Copyright 2002-2005 the original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jcf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcf.graphicMessage.Create;
 import org.jcf.graphicMessage.Delete;
 import org.jcf.graphicMessage.Event;
 import org.jcf.graphicMessage.GraphicMessage;
 import org.jcf.graphicMessage.GraphicMessageFactory;
 import org.jcf.graphicMessage.GraphicObject;
 import org.jcf.graphicMessage.GraphicObjectFactory;
 import org.jcf.graphicMessage.GraphicObjectProperty;
 import org.jcf.graphicMessage.Id;
 import org.jcf.graphicMessage.Location;
 import org.springframework.util.Assert;
 
 
 /**
  * class to handle the current graphical objects, GraphicalMessage and to create new instances
  * of GraphicalMessages
  * @author FaKod
  *
  */
 public class GraphicObjectHandlerImpl implements GraphicObjectHandler {
 	
 	/**
 	 * thread safe local variable to store the instances of GraphicalMessage
 	 */
 	ThreadLocal<GraphicMessage> threadLocalGraphicalMessage;
 	
 	/**
 	 * store for all graphical objects
 	 */
 	private Map<Id, GraphicObject> objects;
 	
 	/**
 	 * store for the listener
 	 */
 	private List<GraphicObjectEventListener> listener;
 	
 	private String nikName;
 	private String room;
 	
 	/**
 	 * do not use this
 	 */
 	@SuppressWarnings("unused")
 	private GraphicObjectHandlerImpl(){}
 	
 	/**
 	 * ctor to use for a given room and nikname
 	 * @param nikName room unique nikname 
 	 * @param room room name
 	 */
 	GraphicObjectHandlerImpl(String nikName, String room) {
 		Assert.notNull(nikName);
 		Assert.notNull(room);
 		
 		this.room = room;
 		this.nikName = nikName;
 		objects = Collections.synchronizedMap(new HashMap<Id, GraphicObject>());
 		listener = Collections.synchronizedList(new ArrayList<GraphicObjectEventListener>());
 		threadLocalGraphicalMessage = new ThreadLocal<GraphicMessage>();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#addListener(org.jcf.GraphicObjectEventListener)
 	 */
 	public void addListener(GraphicObjectEventListener l) {
 		Assert.notNull(l);
 		listener.add(l);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#removeListener(org.jcf.GraphicObjectEventListener)
 	 */
 	public void removeListener(GraphicObjectEventListener l) {
 		Assert.notNull(l);
 		listener.remove(l);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#getObject(org.jcf.graphicMessage.Id)
 	 */
 	public GraphicObject getObject(Id id) {
 		Assert.notNull(id);
 		return objects.get(id);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#getIds()
 	 */
 	public Set<Id> getIds() {
 		return objects.keySet();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#processGraphicMessage(org.jcf.graphicMessage.GraphicMessage)
 	 */
 	public void processGraphicMessage(GraphicMessage gm) {
 		Assert.notNull(gm);
 		
 		List<Event> events = gm.getEvents();
 		if(events==null)
 			return;
 		
 		for(Event e : events) {
 			if(e instanceof Delete) {
 				Id id = ((Delete)e).getId();
 				Assert.notNull(id);
 				if(objects.remove(id) != null)
 					fireDeleteEvent(id);
 			}
 		}
		
 		for(Event e : events) {
 			if(e instanceof Create) {
 				GraphicObject g = ((Create)e).getGraphicObject();
 				
 				Assert.notNull(g);
 				Assert.notNull(g.getId());
 				
				objects.put(g.getId(), g);
 				fireCreateEvent(g.getId());
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#createNewGraphicMessage()
 	 */
 	public GraphicMessage createNewGraphicMessage() {
 		threadLocalGraphicalMessage.set(GraphicMessageFactory.createGraphicMessage());
 		return threadLocalGraphicalMessage.get();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#getGraphicMessage()
 	 */
 	public GraphicMessage getGraphicMessage() {
 		return threadLocalGraphicalMessage.get();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#moveGraphicObject(java.util.List, org.jcf.graphicMessage.Id)
 	 */
 	public GraphicObjectHandler moveGraphicObject(List<Location> newLocations,  Id graphicObjectId) {
 		Assert.notNull(newLocations);
 		Assert.notNull(graphicObjectId);
 		
 		GraphicObject oldGo = objects.get(graphicObjectId);
 		if(oldGo==null)
 			throw new JCFException("old Graphic Object not found");
 		
 		GraphicMessage graphicMessage = threadLocalGraphicalMessage.get();
 		if(graphicMessage==null)
 			throw new JCFException("GraphicMessage available. call createNewGraphicMessage first");
 		
 		graphicMessage.addDeleteEvent(graphicObjectId);
 		objects.remove(graphicObjectId);
 		
 		GraphicObject newGo = GraphicObjectFactory.create(oldGo, nikName, room, newLocations);
 		newGo.setGraphicObjectProperty(oldGo.getGraphicObjectProperty());
 		graphicMessage.addCreateEvent(newGo);
 		objects.put(newGo.getId(), newGo);
 		
 		return this;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#deleteGraphicObject(org.jcf.graphicMessage.Id)
 	 */
 	public GraphicObjectHandler deleteGraphicObject(Id graphicObjectId) {
 		Assert.notNull(graphicObjectId);
 		
 		GraphicMessage graphicMessage = threadLocalGraphicalMessage.get();
 		if(graphicMessage==null)
 			throw new JCFException("GraphicMessage available. call createNewGraphicMessage first");
 		
 		graphicMessage.addDeleteEvent(graphicObjectId);
 		objects.remove(graphicObjectId);
 		//fireDeleteEvent(graphicObjectId);
 		return this;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#createPointObject(org.jcf.graphicMessage.Location)
 	 */
 	public GraphicObjectHandler createPointObject(Location loc) {
 		Assert.notNull(loc);
 		createAndReturnPointObject(loc);
 		return this;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.jcf.GraphicObjectHandler#createPointObject(org.jcf.graphicMessage.Location, org.jcf.graphicMessage.GraphicObjectProperty)
 	 */
 	public GraphicObjectHandler createPointObject(Location loc,
 			GraphicObjectProperty graphicObjectProperty) {
 		Assert.notNull(loc);
 		GraphicObject g = createAndReturnPointObject(loc);
 		g.setGraphicObjectProperty(graphicObjectProperty);
 		return this;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#createLineObject(java.util.List)
 	 */
 	public GraphicObjectHandler createLineObject(List<Location> locs) {
 		Assert.notNull(locs);
 		createAndReturnLineObject(locs);
 		return this;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.jcf.GraphicObjectHandler#createLineObject(java.util.List, org.jcf.graphicMessage.GraphicObjectProperty)
 	 */
 	public GraphicObjectHandler createLineObject(List<Location> locs,
 			GraphicObjectProperty graphicObjectProperty) {
 		Assert.notNull(locs);
 		GraphicObject g = createAndReturnLineObject(locs);
 		g.setGraphicObjectProperty(graphicObjectProperty);
 		return this;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.IGraphicObjectHandler#createPolygonObject(java.util.List)
 	 */
 	public GraphicObjectHandler createPolygonObject(List<Location> locs) {
 		Assert.notNull(locs);
 		createAndReturnPolygonObject(locs);
 		return this;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.jcf.GraphicObjectHandler#createPolygonObject(java.util.List, org.jcf.graphicMessage.GraphicObjectProperty)
 	 */
 	public GraphicObjectHandler createPolygonObject(List<Location> locs,
 			GraphicObjectProperty graphicObjectProperty) {
 		Assert.notNull(locs);
 		GraphicObject g = createAndReturnPolygonObject(locs);
 		g.setGraphicObjectProperty(graphicObjectProperty);
 		return this;
 	}
 	
 	/**
 	 * fires the necessary delete events
 	 * @param id
 	 */
 	private void fireDeleteEvent(Id id) {
 		Assert.notNull(id);
 		
 		for(GraphicObjectEventListener l :listener ) {
 			l.deleteEvent(id);
 		}
 	}
 	
 	/**
 	 * fires the necessary create events
 	 * @param id
 	 */
 	private void fireCreateEvent(Id id) {
 		Assert.notNull(id);
 		
 		for(GraphicObjectEventListener l :listener ) {
 			l.createEvent(id);
 		}
 	}
 	
 	/**
 	 * creates and returns new Point Object
 	 * @param locs location for creation
 	 */
 	private GraphicObject createAndReturnPointObject(Location loc) {
 		GraphicMessage graphicMessage = threadLocalGraphicalMessage.get();
 		if(graphicMessage==null)
 			throw new JCFException("GraphicMessage available. call createNewGraphicMessage first");
 		
 		GraphicObject newGo = GraphicObjectFactory.createPoint(nikName, room, loc);
 		graphicMessage.addCreateEvent(newGo);
 		objects.put(newGo.getId(), newGo);
 		return newGo;
 	}
 	
 	/**
 	 * creates and returns new Line Object
 	 * @param locs location for creation
 	 */
 	private GraphicObject createAndReturnLineObject(List<Location> locs) {
 		GraphicMessage graphicMessage = threadLocalGraphicalMessage.get();
 		if(graphicMessage==null)
 			throw new JCFException("GraphicMessage available. call createNewGraphicMessage first");
 		
 		GraphicObject newGo = GraphicObjectFactory.createLine(nikName, room, locs);
 		graphicMessage.addCreateEvent(newGo);
 		objects.put(newGo.getId(), newGo);
 		return newGo;
 	}
 	
 	/**
 	 * creates and returns new Polygon Object
 	 * @param locs location for creation
 	 */
 	private GraphicObject createAndReturnPolygonObject(List<Location> locs) {
 		GraphicMessage graphicMessage = threadLocalGraphicalMessage.get();
 		if(graphicMessage==null)
 			throw new JCFException("GraphicMessage available. call createNewGraphicMessage first");
 		
 		GraphicObject newGo = GraphicObjectFactory.createPolygon(nikName, room, locs);
 		graphicMessage.addCreateEvent(newGo);
 		objects.put(newGo.getId(), newGo);
 		return newGo;
 	}
 
 }
