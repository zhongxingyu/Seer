 package uk.co.plogic.gwt.lib.map.overlay;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.Timer;
 import com.google.maps.gwt.client.GoogleMap;
 import com.google.maps.gwt.client.GoogleMap.BoundsChangedHandler;
 import com.google.maps.gwt.client.GoogleMap.CenterChangedHandler;
 import com.google.maps.gwt.client.GoogleMap.DragEndHandler;
 import com.google.maps.gwt.client.GoogleMap.ResizeHandler;
 import com.google.maps.gwt.client.GoogleMap.ZoomChangedHandler;
 import com.google.maps.gwt.client.LatLng;
 import com.google.maps.gwt.client.LatLngBounds;
 import com.google.maps.gwt.client.MarkerImage;
 import com.google.maps.gwt.client.Point;
 import com.google.maps.gwt.client.Size;
 
 import uk.co.plogic.gwt.lib.cluster.domain.Coord;
 import uk.co.plogic.gwt.lib.cluster.uncoil.Nest;
 import uk.co.plogic.gwt.lib.cluster.uncoil.Uncoil;
 import uk.co.plogic.gwt.lib.comms.DropBox;
 import uk.co.plogic.gwt.lib.comms.UxPostalService.LetterBox;
 import uk.co.plogic.gwt.lib.comms.envelope.ClusterPointsEnvelope;
 import uk.co.plogic.gwt.lib.map.BasicPoint;
 import uk.co.plogic.gwt.lib.map.IconMarker;
 import uk.co.plogic.gwt.lib.map.MarkerMoveAnimation;
 import uk.co.plogic.gwt.lib.map.AbstractMapMarker;
 
 public class ClusterPoints implements DropBox {
 
 	private LetterBox letterBox;
 	private GoogleMap gMap;
 	private ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
 	private int currentKeyFrame;
 	private HandlerManager eventBus;
 	private int requestedNoPoints = 35;
	final static int markerAnimationDuration = 500;
 	final String mapMarkersUrl; // the integer weight is added to the end of this
 	// weight -> markerIcon
 	HashMap<Integer, MarkerImage> markerIcons = new HashMap<Integer, MarkerImage>();
 	
 	class KeyFrame {
 		Uncoil uncoil;
 		HashMap<Integer, AbstractMapMarker> markers = new HashMap<Integer, AbstractMapMarker>();
 		public KeyFrame(Uncoil uncoil) {
 			this.uncoil = uncoil;
 		}
 	}
 	
 
 	public ClusterPoints(HandlerManager eventBus, final String mapMarkersUrl) {
 		this.eventBus = eventBus;
 		this.mapMarkersUrl = mapMarkersUrl;
 	}
 
 	@Override
 	public void onDelivery(String jsonEncodedPayload) {
 
 		ClusterPointsEnvelope envelope = new ClusterPointsEnvelope();
 		envelope.loadJson(jsonEncodedPayload);
 
 		ArrayList<BasicPoint> points = envelope.getPoints();
 		System.out.println("Got ["+points.size()+"] new points");
 		Uncoil u = new Uncoil();
 		for( BasicPoint point : points ) {
 			
 			String[] nodePosition = point.getId().split("\\.");
 			int left = Integer.parseInt(nodePosition[0]);
 			int right = Integer.parseInt(nodePosition[1]);
 			Coord c = new Coord(point.getLng(), point.getLat());
			Nest nst = new Nest(left, right, c, right-left);
 			u.addNest(nst);
 		}
 		keyFrames.add(new KeyFrame(u));
 		// old keyframes could be dropped here
 		currentKeyFrame = keyFrames.size()-1;
 		
 		refreshMapMarkers();
 
 	}
 
 	/**
 	 * get mapMarkers into sync with points (which have probably just been
 	 * delivered).
 	 * 
 	 * for now: clear old markers and add new ones
 	 */
 	public void refreshMapMarkers() {
 
 		KeyFrame newKeyFrame = keyFrames.get(currentKeyFrame);
 		KeyFrame oldKeyFrame = null;
 		if( currentKeyFrame > 0 ) {
 			// TODO - dangerous to rely on index
 			oldKeyFrame = keyFrames.get(currentKeyFrame-1);
 		}
 
 
 		while( newKeyFrame.uncoil.hasNext() ) {
 
 			Nest nst = newKeyFrame.uncoil.next();
 			Coord c = nst.getCoord(); 
 			LatLng endPosition = LatLng.create(c.getY(), c.getX());
 			
 			//MarkerOptions options = MarkerOptions.create();
 			//options.setMap(gMap);
 		
 			Nest relativeNst = null;
 			if( oldKeyFrame != null ) {
 				relativeNst = oldKeyFrame.uncoil.findRelative(nst.getLeftID(), nst.getRightID());
 			}
 
 			if( relativeNst == null ) {
 				// no known relatives ; use normal marker
 				//options.setPosition(endPosition);
 				//Marker mapMarker = Marker.create(options);
 				
 				IconMarker mapMarker = new IconMarker( eventBus, getMarkerIcon(nst.getWeight()),
 													   endPosition, gMap);
 				
 				newKeyFrame.markers.put(nst.getLeftID(), mapMarker);
 			} else {
 				// animate from/to a relative
 				
 				if( relativeNst.getLeftID() > nst.getLeftID() ) {
 					// relative is a child
 					// so move child to parent position and then make
 					// parent appear
 					final AbstractMapMarker childMarker = 
 											oldKeyFrame.markers.get(relativeNst.getLeftID());
 					
 					// marker might already have been used by another parent
 					if( childMarker != null ) {
 						MarkerMoveAnimation ma = new MarkerMoveAnimation(childMarker,
 																		 childMarker.getPosition(),
 																		 endPosition);
 						ma.run(markerAnimationDuration);
 						// make child disappear at end of duration
 						final Timer childTimer = new Timer() {  
 						    @Override
 						    public void run() {
 						    	childMarker.hideMarker();
 						    }
 						};
 						childTimer.schedule(markerAnimationDuration);
 					}
 
 					// parent to appear at end of duration
 					final IconMarker mapMarker = new IconMarker(eventBus,
 																getMarkerIcon(nst.getWeight()),
 																endPosition, gMap);
 					mapMarker.hideMarker();
 					
 					
 					newKeyFrame.markers.put(nst.getLeftID(), mapMarker);
 					final Timer parentTimer = new Timer() {  
 					    @Override
 					    public void run() {
 					    	mapMarker.showMarker();
 					    }
 					};
 					parentTimer.schedule(markerAnimationDuration);
 
 
 				} else {
 					// relative is the parent or relative is self (equal)
 
 					Coord cRel = relativeNst.getCoord();
 					LatLng startPosition = LatLng.create(cRel.getY(), cRel.getX());
 					
 					//options.setPosition(startPosition);
 					//Marker mapMarker = Marker.create(options);
 					
 					final IconMarker mapMarker = new IconMarker(eventBus,
 							getMarkerIcon(nst.getWeight()),
 							startPosition, gMap);
 					
 					
 					newKeyFrame.markers.put(nst.getLeftID(), mapMarker);
 					MarkerMoveAnimation ma = new MarkerMoveAnimation(mapMarker, startPosition,
 																	 endPosition);
 					ma.run(markerAnimationDuration);
 					
 					// remove parent marker
 					AbstractMapMarker parentMarker = oldKeyFrame.markers.get(relativeNst.getLeftID());
 					if( parentMarker != null )
 						parentMarker.hideMarker();
 					
 				}
 				// remove relative that we used. i.e. those markers remaining in
 				// oldKeyFrame need to be removed from the map
 				if( oldKeyFrame.markers.containsKey(relativeNst.getLeftID()) )
 					oldKeyFrame.markers.remove(relativeNst.getLeftID());
 				
 			}
 
 		}
 		
 		// dispose of markers from last key frame
 		if( oldKeyFrame != null ) {
 			
 			oldKeyFrame.uncoil.resetIterator();
 			while( oldKeyFrame.uncoil.hasNext() ) {
 
 				Nest nst = oldKeyFrame.uncoil.next();
 				int nstKeyID = nst.getLeftID();
 				if( ! oldKeyFrame.markers.containsKey(nstKeyID) ) {
 					//System.out.println("cant find: "+nstKeyID);
 					continue;
 				}
 				AbstractMapMarker mapMarker = oldKeyFrame.markers.get(nstKeyID);
 				//System.out.println("animating: "+nstKeyID);
 
 				Nest relativeNst = newKeyFrame.uncoil.findRelative(nstKeyID, nst.getRightID());
 				if( relativeNst == null )
 					continue;
 
 				if( relativeNst.getLeftID() < nstKeyID ) {
 					// has parent in newFrame so move from current
 					// position to that of the parent
 					
 					Coord cRel = relativeNst.getCoord();
 					LatLng endPosition = LatLng.create(cRel.getY(), cRel.getX());
 
 					MarkerMoveAnimation ma = new MarkerMoveAnimation(mapMarker,
 																	 mapMarker.getPosition(),
 																	 endPosition);
 					ma.run(markerAnimationDuration);
 				} else {
 					// has child in newFrame
 					// I don't think this will ever happen as this node
 					// would have been dealt with above
 					System.out.println("Found a parent in newKeyFrame??");
 				}
 				
 			}
 
 			// at the end of the animation, every marker left in oldKeyFrame needs to be
 			// removed from the map
 			final Collection<AbstractMapMarker> oldMarkers = oldKeyFrame.markers.values();
 			final Timer clearTimer = new Timer() {  
 			    @Override
 			    public void run() {
 					for( AbstractMapMarker oldMarker : oldMarkers ) {
 						oldMarker.hideMarker();
 					}
 			    }
 			};
 			clearTimer.schedule(markerAnimationDuration);
 		}
 		
 //		for( MapPointMarker m : mapMarkers ) {
 //			m.removeMarker();
 //		}
 //		mapMarkers.clear();
 //
 //		for( BasicPoint aPoint: points ) {
 //    		MapPointMarker m = new MapPointMarker(	eventBus,
 //    												"static/icons/marker.png",
 //    												"static/icons/marker_active.png",
 //    												aPoint, gMap);
 //    		System.out.println("Adding: "+aPoint.getId()+" "+aPoint.getLat()+","+aPoint.getLng());
 //    		mapMarkers.add(m);
 //    	}
 		
 	}
 	
 	private MarkerImage getMarkerIcon(int weight) {
 
 		if( markerIcons.containsKey(weight) ) {
 			return markerIcons.get(weight);
 		}
 		
  		int width = 32;
 		int height = 37;
 		int anchor_x = 16;
 		int anchor_y = 35;
 		MarkerImage markerIcon = MarkerImage.create(mapMarkersUrl+weight+"/",
 										  Size.create(width, height),
 										  Point.create(0, 0),
 										  Point.create(anchor_x, anchor_y));
 		markerIcons.put(weight, markerIcon);
 		return markerIcon;
 	}
 	
 	public void setLetterBox(LetterBox registeredLetterBox) {
 		this.letterBox = registeredLetterBox;
 	}
 
 	public void setMap(GoogleMap googleMap) {
 		
 		this.gMap = googleMap;
 		
 		
 		final Timer requestTimer = new Timer() {  
 		    @Override
 		    public void run() {
 		    	System.out.println("requesting now");
 		    	
 		    	LatLngBounds mapBounds = gMap.getBounds();
 		    	LatLng ll0 = mapBounds.getSouthWest();
 		    	LatLng ll1 = mapBounds.getNorthEast();
 		    	
 		    	ClusterPointsEnvelope envelope = new ClusterPointsEnvelope();
 		    	envelope.requestBounding(ll0.lng(), ll0.lat(), ll1.lng(), ll1.lat());
 		    	envelope.requestNoPoints(requestedNoPoints);
 		    	letterBox.send(envelope);
 		    }
 		};
 
 		gMap.addZoomChangedListener(new ZoomChangedHandler() {
 
 			@Override
 			public void handle() {
 				System.out.println("zoom changed");
 			}
 			
 		});
 		gMap.addBoundsChangedListener(new BoundsChangedHandler() {
 
 			@Override
 			public void handle() {
 				System.out.println("bounds changed");
 				requestTimer.cancel();
 				requestTimer.schedule(250);
 			}
 			
 		});
 		
 		gMap.addCenterChangedListener(new CenterChangedHandler() {
 
 			@Override
 			public void handle() {
 				System.out.println("center changed");
 				requestTimer.cancel();
 				requestTimer.schedule(250);
 			}
 			
 		});
 		
 		gMap.addDragEndListener(new DragEndHandler() {
 
 			@Override
 			public void handle() {
 				System.out.println("end drag");
 			}
 			
 		});
 		
 		gMap.addResizeListener(new ResizeHandler() {
 
 			@Override
 			public void handle() {
 				System.out.println("resized");
 			}
 			
 		});
 		
 	}
 
 	
 }
