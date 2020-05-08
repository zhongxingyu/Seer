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
 package ditl.graphs.viz;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 import ditl.*;
 import ditl.graphs.*;
 import ditl.viz.Scene;
 
 
 
 @SuppressWarnings("serial")
 public class GraphScene extends Scene implements 
 	MovementTrace.Handler, LinkTrace.Handler, EdgeTrace.Handler, GroupTrace.Handler {
 
 	protected Map<Integer,NodeElement> nodes = new HashMap<Integer,NodeElement>();
 	private Map<Link,LinkElement> links = new HashMap<Link,LinkElement>();
 	private Map<Link,EdgeElement> edges = new HashMap<Link,EdgeElement>();
 	private boolean showIds = false;
 	private Map<Integer,Color> group_color_map = null;
 
 	public GraphScene() {
 		super();
 		addMouseListener(new MouseAdapter(){
 			@Override
 			public void mouseClicked(MouseEvent e) { // extremely inefficient!
 				for ( NodeElement node : nodes.values() ){
 					if ( node.contains(e.getX(), e.getY())){
 						System.out.println(node.id());
 						break;
 					}
 				}
 			}
 		});
 	}
 	
 	public void setGroupColorMap(Map<Integer,Color> groupColorMap){
 		group_color_map = groupColorMap;
 	}
 	
 	@Override
 	public Listener<Movement> movementListener(){
 		return new StatefulListener<Movement>(){
 			@Override
 			public void handle(long time, Collection<Movement> events) {
 				for ( Movement m : events ){
 					NodeElement node = new NodeElement(m);
 					node.setShowId(showIds);
 					nodes.put(m.id(), node);
 					addScaleListener(node);
 				}
 			}
 
 			@Override
 			public void reset() {
 				nodes.clear();
 			}
 		};
 	}
 	
 	@Override
 	public Listener<MovementEvent> movementEventListener(){
 		return new Listener<MovementEvent>(){
 			@Override
 			public void handle(long time, Collection<MovementEvent> events) {
 				for ( MovementEvent mev : events ){
 					NodeElement node;
 					Integer id = mev.id(); 
 					switch (mev.type() ){
 					case MovementEvent.IN:
 						node = new NodeElement(mev.origMovement());
 						nodes.put(id, node);
 						addScaleListener(node);
 						break;
 						
 					case MovementEvent.OUT:
 						removeScaleListener(nodes.get(id));
 						nodes.remove(id);
 						break;
 						
 					case MovementEvent.NEW_DEST:
 						node = nodes.get(id);
 						node.updateMovement(time, mev); break;
 					}
 				}
 			}
 		};
 	}
 
 	@Override
 	public Listener<LinkEvent> linkEventListener(){
 		return new Listener<LinkEvent>() {
 			@Override
 			public void handle(long time, Collection<LinkEvent> events) {
 				for ( LinkEvent lev : events ){
 					Link l = lev.link();
 					if ( lev.isUp() ){
 						NodeElement n1 = nodes.get(lev.id1());
 						NodeElement n2 = nodes.get(lev.id2());
 						LinkElement link = new LinkElement(n1,n2);
 						links.put(l, link);
 					} else {
 						links.remove(l);
 					}
 				}
 			}
 		};
 	}
 	
 	@Override
 	public Listener<Link> linkListener(){
 		return new StatefulListener<Link>(){
 			@Override
 			public void handle(long time, Collection<Link> events){
 				for ( Link l : events ){
 					NodeElement n1 = nodes.get(l.id1());
 					NodeElement n2 = nodes.get(l.id2());
 					LinkElement link = new LinkElement(n1,n2);
 					links.put(l, link);
 				}
 			}
 
 			@Override
 			public void reset() {
 				links.clear();
 			}
 		};
 	}
 	
 	@Override
 	public Listener<EdgeEvent> edgeEventListener(){
 		return new Listener<EdgeEvent>() {
 			@Override
 			public void handle(long time, Collection<EdgeEvent> events) {
 				for ( EdgeEvent eev : events ){
 					Edge e = eev.edge();
 					Link l = e.link();
 					EdgeElement ee;
 					if ( eev.isUp() ){
 						if ( ! edges.containsKey(l) ){
 							NodeElement n1 = nodes.get(eev.from());
 							NodeElement n2 = nodes.get(eev.to());
 							ee = new EdgeElement(n1,n2);
 							edges.put(l, ee);
 						}
 						ee = edges.get(l);
 						ee.bringEdgeUp(e);
 					} else {
 						ee = edges.get(l);
 						ee.bringEdgeDown(e);
 						if ( ee.state == EdgeElement.DOWN ){
 							edges.remove(l);
 						}
 					}
 				}
 			}
 		};
 	}
 	
 	@Override
 	public Listener<Edge> edgeListener(){
 		return new StatefulListener<Edge>(){
 			@Override
 			public void handle(long time, Collection<Edge> events){
 				for ( Edge e : events ){
 					Link l = e.link();
 					EdgeElement ee;
 					if ( ! edges.containsKey(l) ){
 						NodeElement n1 = nodes.get(e.from());
 						NodeElement n2 = nodes.get(e.to());
 						ee = new EdgeElement(n1,n2);
 						edges.put(l, ee);
 					}
 					edges.get(l).bringEdgeUp(e);
 				}
 			}
 
 			@Override
 			public void reset() {
 				edges.clear();
 			}
 		};
 	}
 	
 	public void setShowIds(boolean show){
 		showIds = show;
 		for ( NodeElement node : nodes.values() )
 			node.setShowId(show);
 		repaint();
 	}
 	
 	public boolean getShowIds(){
 		return showIds; 
 	}
 	
 	@Override
 	public void paint2D(Graphics2D g2){
 		g2.setStroke(new BasicStroke(2));
 		for ( EdgeElement edge : edges.values() )
 			edge.paint(g2);
 		g2.setStroke(new BasicStroke());
 		g2.setColor(Color.BLACK);
 		for ( LinkElement link : links.values() )
 			link.paint(g2);
 		for ( NodeElement node : nodes.values() )
 			node.paint(g2);
 	}
 
 	
 	@Override
 	public void changeTime(long time){
 		for ( NodeElement node : nodes.values() ){
 			node.changeTime(time);
 			node.rescale(this);
 		}
 		super.changeTime(time);
 	}
 
 	@Override
 	public Listener<GroupEvent> groupEventListener() {
 		return new Listener<GroupEvent>(){
 			@Override
 			public void handle(long time, Collection<GroupEvent> events) {
 				for ( GroupEvent gev : events ){
 					switch (gev.type()){
 					case GroupEvent.LEAVE:
 						for ( Integer i : gev.members() )
 							nodes.get(i).setFillColor(GroupsPanel.noGroupColor);
 						break;
 					case GroupEvent.JOIN:
 						for ( Integer i : gev.members() )
 							nodes.get(i).setFillColor(group_color_map.get(gev.gid()));
 						break;
 					}
 				}
 			}
 		};
 	}
 
 	@Override
 	public Listener<Group> groupListener() {
 		return new StatefulListener<Group>(){
 
 			@Override
 			public void reset() {
 				for ( NodeElement node : nodes.values() )
					node.setFillColor(GroupsPanel.noGroupColor);
 			}
 
 			@Override
 			public void handle(long time, Collection<Group> events) {
 				for ( Group group : events ){
 					Color c =  group_color_map.get(group.gid());
 					for ( Integer i : group.members() ){
 						NodeElement node = nodes.get(i);
 						if ( node != null )
 							node.setFillColor(c);
 					}
 				}
 			}
 			
 		};
 	}
 }
