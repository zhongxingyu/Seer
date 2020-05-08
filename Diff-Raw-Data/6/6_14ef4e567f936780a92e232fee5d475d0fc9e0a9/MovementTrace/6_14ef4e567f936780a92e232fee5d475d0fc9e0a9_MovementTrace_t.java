 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.graphs;
 
 import java.io.IOException;
 import java.util.*;
 
 import ditl.*;
 
 public class MovementTrace extends StatefulTrace<MovementEvent, Movement> 
 	implements StatefulTrace.Filterable<MovementEvent, Movement>{
 
 	final public static String minXKey = "min X";
 	final public static String maxXKey = "max X";
 	final public static String minYKey = "min Y";
 	final public static String maxYKey = "max Y";
 	
 	public final static String defaultName = "movement";
 	public final static String type = "movement";
 	public final static int defaultPriority = 20;
 	
 	public double minX(){ 
 		return Double.parseDouble(getValue(minXKey));
 	}
 	
 	public double maxX(){
 		return Double.parseDouble(getValue(maxXKey));
 	}
 	
 	public double minY(){ 
 		return Double.parseDouble(getValue(minYKey));
 	}
 	
 	public double maxY(){ 
 		return Double.parseDouble(getValue(maxYKey));
 	}
 	
 	public interface Handler {
 		public Listener<Movement> movementListener();
 		public Listener<MovementEvent> movementEventListener();
 	}
 	
 	public MovementTrace(Store store, String name, PersistentMap info) throws IOException {
 		super(store, name, info, new MovementEvent.Factory(), new Movement.Factory(), 
 				new StateUpdaterFactory<MovementEvent,Movement>(){
 					@Override
 					public StateUpdater<MovementEvent, Movement> getNew() {
 						return new MovementTrace.Updater();
 					}
 		});
 		info.setIfUnset(Trace.defaultPriorityKey, defaultPriority);
 	}
 	
 	public final static class Updater implements StateUpdater<MovementEvent,Movement> {
 
 		private Map<Integer,Movement> movement_map = new HashMap<Integer,Movement>();
 		private Set<Movement> movements = new HashSet<Movement>();
 		
 		@Override
 		public void setState(Collection<Movement> states) {
 			movements.clear();
 			movement_map.clear();
 			for ( Movement m : states ){
 				Movement mv = m.clone();
 				movements.add(mv);
 				movement_map.put(mv.id(), mv);
 			}
 		}
 
 		@Override
 		public Set<Movement> states() {
 			return movements;
 		}
 
 		@Override
 		public void handleEvent(long time, MovementEvent event) {
 			Movement m;
 			switch(event.type){
 			case MovementEvent.IN: 
 				m = event.origMovement();
 				movements.add(m);
 				movement_map.put(m.id(), m);
 				break;
 				
 			case MovementEvent.OUT:
 				m = movement_map.get(event.id());
 				movements.remove(m);
 				movement_map.remove(m.id());
 				break;
 				
 			default:
 				m = movement_map.get(event.id());
 				m.handleEvent(time, event);
 			}
 		}
 	}
 	
 	@Override
 	public StatefulWriter<MovementEvent,Movement> getWriter(long snapInterval) throws IOException{
 		return new MovementWriter(_store, _name, snapInterval, _info);
 	}
 	
 	private static class MovementWriter extends StatefulWriter<MovementEvent,Movement> {
 		
 		private double minX = Double.MAX_VALUE;
 		private double maxX = Double.MIN_VALUE;
 		private double minY = Double.MAX_VALUE;
 		private double maxY = Double.MIN_VALUE;
 		
 		public MovementWriter(Store store, String name, long snapInterval, PersistentMap info) throws IOException {
 			super(store, name, snapInterval, new MovementTrace.Updater(), info);
 		}
 		
 		public void update(Point p){
			if ( p != null ){ // p can be null
				updateX(p.x);
				updateY(p.y);
			}
 		}
 		
 		public void updateX(double x){
 			if ( x < minX ) minX = x;
 			if ( x > maxX ) maxX = x;
 		}
 		
 		public void updateY(double y){
 			if ( y < minY ) minY = y;
 			if ( y > maxY ) maxY = y;
 		}
 		
 		@Override
 		public void write(long time, MovementEvent event) throws IOException {
 			super.write(time, event);
 			update( event.dest() );
 		}
 		
 		@Override
 		public void setInitState(long time, Collection<Movement> states) throws IOException{
 			super.setInitState(time, states);
 			for ( Movement mv : states )
 				update ( mv.from() );
 		}
 		
 		@Override
 		public void close() throws IOException {
 			setProperty(MovementTrace.minXKey, minX);
 			setProperty(MovementTrace.maxXKey, maxX);
 			setProperty(MovementTrace.minYKey, minY);
 			setProperty(MovementTrace.maxYKey, maxY);
 			super.close();
 		}
 		
 	}
 
 	@Override
 	public Matcher<Movement> stateMatcher(Set<Integer> group) {
 		return new Movement.GroupMatcher(group);
 	}
 
 	@Override
 	public Matcher<MovementEvent> eventMatcher(Set<Integer> group) {
 		return new MovementEvent.GroupMatcher(group);
 	}
 	
 }
