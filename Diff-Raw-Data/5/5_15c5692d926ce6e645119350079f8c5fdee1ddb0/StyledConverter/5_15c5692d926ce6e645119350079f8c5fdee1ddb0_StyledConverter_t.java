 /*
  * Copyright (C) 2007 Steve Ratcliffe
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 2 as
  *  published by the Free Software Foundation.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  * 
  * Author: Steve Ratcliffe
  * Create date: Feb 17, 2008
  */
 package uk.me.parabola.mkgmap.osmstyle;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import uk.me.parabola.imgfmt.app.Area;
 import uk.me.parabola.imgfmt.app.Coord;
 import uk.me.parabola.imgfmt.app.CoordNode;
 import uk.me.parabola.log.Logger;
 import uk.me.parabola.mkgmap.general.AreaClipper;
 import uk.me.parabola.mkgmap.general.Clipper;
 import uk.me.parabola.mkgmap.general.Exit;
 import uk.me.parabola.mkgmap.general.LineAdder;
 import uk.me.parabola.mkgmap.general.LineClipper;
 import uk.me.parabola.mkgmap.general.MapCollector;
 import uk.me.parabola.mkgmap.general.MapElement;
 import uk.me.parabola.mkgmap.general.MapExitPoint;
 import uk.me.parabola.mkgmap.general.MapLine;
 import uk.me.parabola.mkgmap.general.MapPoint;
 import uk.me.parabola.mkgmap.general.MapRoad;
 import uk.me.parabola.mkgmap.general.MapShape;
 import uk.me.parabola.mkgmap.general.RoadNetwork;
 import uk.me.parabola.mkgmap.reader.osm.Element;
 import uk.me.parabola.mkgmap.reader.osm.GType;
 import uk.me.parabola.mkgmap.reader.osm.Node;
 import uk.me.parabola.mkgmap.reader.osm.OsmConverter;
 import uk.me.parabola.mkgmap.reader.osm.Relation;
 import uk.me.parabola.mkgmap.reader.osm.RestrictionRelation;
 import uk.me.parabola.mkgmap.reader.osm.Rule;
 import uk.me.parabola.mkgmap.reader.osm.Style;
 import uk.me.parabola.mkgmap.reader.osm.Way;
 
 /**
  * Convert from OSM to the mkgmap intermediate format using a style.
  * A style is a collection of files that describe the mappings to be used
  * when converting.
  *
  * @author Steve Ratcliffe
  */
 public class StyledConverter implements OsmConverter {
 	private static final Logger log = Logger.getLogger(StyledConverter.class);
 
 	private final String[] nameTagList;
 
 	private final MapCollector collector;
 
 	private Clipper clipper = Clipper.NULL_CLIPPER;
 	private Area bbox;
 	private Set<Coord> boundaryCoords = new HashSet<Coord>();
 
 	// restrictions associates lists of turn restrictions with the
 	// Coord corresponding to the restrictions' 'via' node
 	private final Map<Coord, List<RestrictionRelation>> restrictions = new HashMap<Coord, List<RestrictionRelation>>();
 
 	// originalWay associates Ways that have been created due to
 	// splitting or clipping with the Ways that they were derived
 	// from
 	private final Map<Way, Way> originalWay = new HashMap<Way, Way>();
 
 	private int roadId;
 
 	private final int MAX_POINTS_IN_WAY = 200;
 
 	private final int MAX_NODES_IN_WAY = 16;
 
 	private final double MIN_DISTANCE_BETWEEN_NODES = 5.5;
 
 	// nodeIdMap maps a Coord into a nodeId
 	private final Map<Coord, Integer> nodeIdMap = new HashMap<Coord, Integer>();
 	private int nextNodeId = 1;
 	
 	private final Rule wayRules;
 	private final Rule nodeRules;
 	private final Rule relationRules;
 
 
 	class AccessMapping {
 		private final String type;
 		private final int index;
 		AccessMapping(String type, int index) {
 			this.type = type;
 			this.index = index;
 		}
 	}
 
 	private final AccessMapping[] accessMap = {
 		new AccessMapping("access",     RoadNetwork.NO_MAX), // must be first in list
 		new AccessMapping("bicycle",    RoadNetwork.NO_BIKE),
 		new AccessMapping("foot",       RoadNetwork.NO_FOOT),
 		new AccessMapping("hgv",        RoadNetwork.NO_TRUCK),
 		new AccessMapping("motorcar",   RoadNetwork.NO_CAR),
 		new AccessMapping("motorcycle", RoadNetwork.NO_CAR),
 		new AccessMapping("psv",        RoadNetwork.NO_BUS),
 		new AccessMapping("taxi",       RoadNetwork.NO_TAXI),
 		new AccessMapping("emergency",  RoadNetwork.NO_EMERGENCY),
 		new AccessMapping("delivery",   RoadNetwork.NO_DELIVERY),
 		new AccessMapping("goods",      RoadNetwork.NO_DELIVERY),
 	};
 
 	private LineAdder lineAdder = new LineAdder() {
 		public void add(MapLine element) {
 			if (element instanceof MapRoad)
 				collector.addRoad((MapRoad) element);
 			else
 				collector.addLine(element);
 		}
 	};
 
 	public StyledConverter(Style style, MapCollector collector) {
 		this.collector = collector;
 
 		nameTagList = style.getNameTagList();
 		wayRules = style.getWayRules();
 		nodeRules = style.getNodeRules();
 		relationRules = style.getRelationRules();
 
 		LineAdder overlayAdder = style.getOverlays(lineAdder);
 		if (overlayAdder != null)
 			lineAdder = overlayAdder;
 	}
 
 	/**
 	 * This takes the way and works out what kind of map feature it is and makes
 	 * the relevant call to the mapper callback.
 	 * <p>
 	 * As a few examples we might want to check for the 'highway' tag, work out
 	 * if it is an area of a park etc.
 	 *
 	 * @param way The OSM way.
 	 */
 	public void convertWay(Way way) {
 		if (way.getPoints().size() < 2)
 			return;
 
 		preConvertRules(way);
 
 		GType foundType = wayRules.resolveType(way);
 		if (foundType == null)
 			return;
 
 		postConvertRules(way, foundType);
 
 		if (foundType.getFeatureKind() == GType.POLYLINE) {
 		    if(foundType.isRoad())
				addRoad(way, foundType);
 		    else
				addLine(way, foundType);
 		}
 		else
 			addShape(way, foundType);
 	}
 
 	/**
 	 * Takes a node (that has its own identity) and converts it from the OSM
 	 * type to the Garmin map type.
 	 *
 	 * @param node The node to convert.
 	 */
 	public void convertNode(Node node) {
 		preConvertRules(node);
 
 		GType foundType = nodeRules.resolveType(node);
 		if (foundType == null)
 			return;
 
 		postConvertRules(node, foundType);
 
 		addPoint(node, foundType);
 	}
 
 	/**
 	 * Rules to run before converting the element.
 	 */
 	private void preConvertRules(Element el) {
 		if (nameTagList == null)
 			return;
 
 		for (String t : nameTagList) {
 			String val = el.getTag(t);
 			if (val != null) {
 				el.addTag("name", val);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Built in rules to run after converting the element.
 	 */
 	private void postConvertRules(Element el, GType type) {
 		// Set the name from the 'name' tag or failing that from
 		// the default_name.
 		el.setName(el.getTag("name"));
 		if (el.getName() == null)
 			el.setName(type.getDefaultName());
 	}
 
 	/**
 	 * Set the bounding box for this map.  This should be set before any other
 	 * elements are converted if you want to use it. All elements that are added
 	 * are clipped to this box, new points are added as needed at the boundry.
 	 *
 	 * If a node or a way falls completely outside the boundry then it would be
 	 * ommited.  This would not normally happen in the way this option is typically
 	 * used however.
 	 *
 	 * @param bbox The bounding area.
 	 */
 	public void setBoundingBox(Area bbox) {
 		this.clipper = new AreaClipper(bbox);
 		this.bbox = bbox;
 	}
 
 	/**
 	 * Run the rules for this relation.  As this is not an end object, then
 	 * the only useful rules are action rules that set tags on the contained
 	 * ways or nodes.  Every rule should probably start with 'type=".."'.
 	 *
 	 * @param relation The relation to convert.
 	 */
 	public void convertRelation(Relation relation) {
 		// Relations never resolve to a GType and so we ignore the return
 		// value.
 		relationRules.resolveType(relation);
 
 		if(relation instanceof RestrictionRelation) {
 			RestrictionRelation rr = (RestrictionRelation)relation;
 			if(rr.isValid()) {
 				List<RestrictionRelation> lrr = restrictions.get(rr.getViaCoord());
 				if(lrr == null) {
 					lrr = new ArrayList<RestrictionRelation>();
 					restrictions.put(rr.getViaCoord(), lrr);
 				}
 				lrr.add(rr);
 			}
 		}
 	}
 
 	private void addLine(Way way, GType gt) {
 		MapLine line = new MapLine();
 		elementSetup(line, gt, way);
 		line.setPoints(way.getPoints());
 
 		if (way.isBoolTag("oneway"))
 			line.setDirection(true);
 
 		clipper.clipLine(line, lineAdder);
 	}
 
 	private void addShape(Way way, GType gt) {
 		MapShape shape = new MapShape();
 		elementSetup(shape, gt, way);
 		shape.setPoints(way.getPoints());
 
 		clipper.clipShape(shape, collector);
 		
 		GType pointType = nodeRules.resolveType(way);
 		
 		if(pointType != null)
 			shape.setPoiType(pointType.getType());
 	}
 
 	private void addPoint(Node node, GType gt) {
 		if (!clipper.contains(node.getLocation()))
 			return;
 
 		// to handle exit points we use a subclass of MapPoint
 		// to carry some extra info (a reference to the
 		// motorway associated with the exit)
 		MapPoint mp;
 		int type = gt.getType();
 		if(type >= 0x2000 && type < 0x2800) {
 			String ref = node.getTag(Exit.TAG_ROAD_REF);
 			String id = node.getTag("osm:id");
 			if(ref != null) {
 				String to = node.getTag(Exit.TAG_TO);
 				MapExitPoint mep = new MapExitPoint(ref, to);
 				String fd = node.getTag(Exit.TAG_FACILITY);
 				if(fd != null)
 					mep.setFacilityDescription(fd);
 				if(id != null)
 					mep.setOSMId(id);
 				mp = mep;
 			}
 			else {
 				mp = new MapPoint();
 				log.warn("Motorway exit " + node.getName() + " (OSM id " + id + ") located at " + node.getLocation().toDegreeString() + " has no motorway! (either make the exit share a node with the motorway or specify the motorway ref with a " + Exit.TAG_ROAD_REF + " tag)");
 			}
 		}
 		else {
 			mp = new MapPoint();
 		}
 		elementSetup(mp, gt, node);
 		mp.setLocation(node.getLocation());
 
 		collector.addPoint(mp);
 	}
 
 	private String combineRefs(Element element) {
 		String ref = element.getTag("ref");
 		String int_ref = element.getTag("int_ref");
 		if(int_ref != null) {
 			if(ref == null)
 				ref = int_ref;
 			else
 				ref += ";" + int_ref;
 		}
 		String nat_ref = element.getTag("nat_ref");
 		if(nat_ref != null) {
 			if(ref == null)
 				ref = nat_ref;
 			else
 				ref += ";" + nat_ref;
 		}
 		String reg_ref = element.getTag("reg_ref");
 		if(reg_ref != null) {
 			if(ref == null)
 				ref = reg_ref;
 			else
 				ref += ";" + reg_ref;
 		}
 
 		return ref;
 	}
 
 	private void elementSetup(MapElement ms, GType gt, Element element) {
 		String name = element.getName();
 		String refs = combineRefs(element);
 		if(name == null && refs != null) {
 			// use first ref as name
 			name = refs.split(";")[0].trim();
 		}
 		if(name != null)
 			ms.setName(name);
 		if(refs != null)
 			ms.setRef(refs);
 		ms.setType(gt.getType());
 		ms.setMinResolution(gt.getMinResolution());
 		ms.setMaxResolution(gt.getMaxResolution());
 		
 		// Now try to get some address info for POIs
 		
 		String city         = element.getTag("addr:city");
 		String zip          = element.getTag("addr:postcode");
 		String street 	    = element.getTag("addr:street");
 		String houseNumber  = element.getTag("addr:housenumber");
 		String phone        = element.getTag("phone");
 		String isIn         = element.getTag("is_in");
 		String country      = element.getTag("is_in:country");
 		String region       = element.getTag("is_in:county");
 		
 		if(country != null)
 			country = element.getTag("addr:country");
 
 		if(zip == null)
 		  zip = element.getTag("openGeoDB:postal_codes");
 		
 		if(city == null)
 		  city = element.getTag("openGeoDB:sort_name");
 		
 		if(city != null)
 		  ms.setCity(city);
 		  
 		if(zip != null)
 		  ms.setZip(zip);
 		  
 		if(street != null)
 		  ms.setStreet(street);		  
 
 		if(houseNumber != null)
 		  ms.setHouseNumber(houseNumber);
 		  
 		if(isIn != null)
 		  ms.setIsIn(isIn);		  
 			
 		if(phone != null)
 		  ms.setPhone(phone);	
 
 		if(country != null)
 		  ms.setCountry(country);	
 
 		if(region != null)
 		  ms.setRegion(region);			
 	}
 
 	void addRoad(Way way, GType gt) {
 
 		if("roundabout".equals(way.getTag("junction"))) {
 			String frigFactorTag = way.getTag("mkgmap:frig_roundabout");
 			if(frigFactorTag != null) {
 				// do special roundabout frigging to make gps
 				// routing prompt use the correct exit number
 				double frigFactor = 0.25; // default
 				try {
 					frigFactor = Double.parseDouble(frigFactorTag);
 				}
 				catch (NumberFormatException nfe) {
 					// relax, tag was probably not a number anyway
 				}
 				frigRoundabout(way, frigFactor);
 			}
 		}
 
 		// if there is a bounding box, clip the way with it
 
 		List<Way> clippedWays = null;
 
 		if(bbox != null) {
 			List<List<Coord>> lineSegs = LineClipper.clip(bbox, way.getPoints());
 			boundaryCoords = new HashSet<Coord>();
 
 			if (lineSegs != null) {
 
 				clippedWays = new ArrayList<Way>();
 
 				for (List<Coord> lco : lineSegs) {
 					Way nWay = new Way();
 					nWay.setName(way.getName());
 					nWay.copyTags(way);
 					for(Coord co : lco) {
 						nWay.addPoint(co);
 						if(co.getHighwayCount() == 0) {
 							boundaryCoords.add(co);
 							co.incHighwayCount();
 						}
 					}
 					clippedWays.add(nWay);
 					// associate the original Way
 					// to the new Way
 					Way origWay = originalWay.get(way);
 					if(origWay == null)
 						origWay = way;
 					originalWay.put(nWay, origWay);
 				}
 			}
 		}
 
 		if(clippedWays != null) {
 			for(Way cw : clippedWays) {
 				while(cw.getPoints().size() > MAX_POINTS_IN_WAY) {
 					Way tail = splitWayAt(cw, MAX_POINTS_IN_WAY - 1);
 					addRoadAfterSplittingLoops(cw, gt);
 					cw = tail;
 				}
 				addRoadAfterSplittingLoops(cw, gt);
 			}
 		}
 		else {
 			// no bounding box or way was not clipped
 			while(way.getPoints().size() > MAX_POINTS_IN_WAY) {
 				Way tail = splitWayAt(way, MAX_POINTS_IN_WAY - 1);
 				addRoadAfterSplittingLoops(way, gt);
 				way = tail;
 			}
 			addRoadAfterSplittingLoops(way, gt);
 		}
 	}
 
 	void addRoadAfterSplittingLoops(Way way, GType gt) {
 
 		// check if the way is a loop or intersects with itself
 
 		boolean wayWasSplit = true; // aka rescan required
 
 		while(wayWasSplit) {
 			List<Coord> wayPoints = way.getPoints();
 			int numPointsInWay = wayPoints.size();
 
 			wayWasSplit = false; // assume way won't be split
 
 			// check each point in the way to see if it is
 			// the same point as a following point in the way
 			for(int p1I = 0; !wayWasSplit && p1I < (numPointsInWay - 1); p1I++) {
 				Coord p1 = wayPoints.get(p1I);
 				for(int p2I = p1I + 1; !wayWasSplit && p2I < numPointsInWay; p2I++) {
 					if(p1 == wayPoints.get(p2I)) {
 						// way is a loop or intersects itself
 						int splitI = p2I - 1; // split before second point
 						if(splitI == p1I) {
 							log.info("Way has zero length segment - " + wayPoints.get(splitI).toOSMURL());
 							wayPoints.remove(p2I);
 							// next point to inspect has same index
 							--p2I;
 							// but number of points has reduced
 							--numPointsInWay;
 						}
 						else {
 							// split the way before the second point
 							log.info("Split way at " + wayPoints.get(splitI).toDegreeString() + " - it has " + (numPointsInWay - splitI - 1 ) + " following segments.");
 							Way loopTail = splitWayAt(way, splitI);
 							// way before split has now been verified
 							addRoadWithoutLoops(way, gt);
 							// now repeat for the tail of the way
 							way = loopTail;
 							wayWasSplit = true;
 						}
 					}
 				}
 			}
 
 			if(!wayWasSplit) {
 				// no split required so make road from way
 				addRoadWithoutLoops(way, gt);
 			}
 		}
 	}
 
 	void addRoadWithoutLoops(Way way, GType gt) {
 		List<Integer> nodeIndices = new ArrayList<Integer>();
 		List<Coord> points = way.getPoints();
 		Way trailingWay = null;
 		String wayName = way.getName();
 
 		// make sure the way has nodes at each end
 		points.get(0).incHighwayCount();
 		points.get(points.size() - 1).incHighwayCount();
 
 		// collect the Way's nodes
 		for(int i = 0; i < points.size(); ++i) {
 			Coord p = points.get(i);
 			int highwayCount = p.getHighwayCount();
 			if(highwayCount > 1) {
 				// this point is a node connecting highways
 				Integer nodeId = nodeIdMap.get(p);
 				if(nodeId == null) {
 					// assign a node id
 					nodeIdMap.put(p, nextNodeId++);
 				}
 				nodeIndices.add(i);
 
 				if((i + 1) < points.size() &&
 				   nodeIndices.size() == MAX_NODES_IN_WAY) {
 					// this isn't the last point in the way so split
 					// it here to avoid exceeding the max nodes in way
 					// limit
 					trailingWay = splitWayAt(way, i);
 					// this will have truncated the current Way's
 					// points so the loop will now terminate
 					log.info("Splitting way " + wayName + " at " + points.get(i).toDegreeString() + " as it has at least " + MAX_NODES_IN_WAY + " nodes");
 				}
 			}
 		}
 
 		MapLine line = new MapLine();
 		elementSetup(line, gt, way);
 		line.setPoints(points);
 
 		MapRoad road = new MapRoad(roadId++, line);
 
 		// set road parameters.
 		road.setRoadClass(gt.getRoadClass());
 		if (way.isBoolTag("oneway")) {
 			road.setDirection(true);
 			road.setOneway();
 		}
 
 		// maxspeed attribute overrides default for road type
 		
 		String maxSpeed = way.getTag("maxspeed");
 		int speedIdx = -1;
 		
 		if(maxSpeed != null)
 			speedIdx = getSpeedIdx(maxSpeed);
 
 		road.setSpeed(speedIdx >= 0? speedIdx : gt.getRoadSpeed());
 
 		boolean[] noAccess = new boolean[RoadNetwork.NO_MAX];
 		String highwayType = way.getTag("highway");
 		if(highwayType == null) {
 			// it's a routable way but not a highway (e.g. a ferry)
 			// use the value of the route tag as the highwayType for
 			// the purpose of testing for access restrictions
 			highwayType = way.getTag("route");
 		}
 
 		for (AccessMapping anAccessMap : accessMap) {
 			int index = anAccessMap.index;
 			String type = anAccessMap.type;
 			String accessTagValue = way.getTag(type);
 			if (accessTagValue == null)
 				continue;
 			if (accessExplicitlyDenied(accessTagValue)) {
 				if (index == RoadNetwork.NO_MAX) {
 					// everything is denied access
 					for (int j = 1; j < accessMap.length; ++j)
 						noAccess[accessMap[j].index] = true;
 				} else {
 					// just the specific vehicle class is denied
 					// access
 					noAccess[index] = true;
 				}
 				log.info(type + " is not allowed in " + highwayType + " " + wayName);
 			} else if (accessExplicitlyAllowed(accessTagValue)) {
 				if (index == RoadNetwork.NO_MAX) {
 					// everything is allowed access
 					for (int j = 1; j < accessMap.length; ++j)
 						noAccess[accessMap[j].index] = false;
 				} else {
 					// just the specific vehicle class is allowed
 					// access
 					noAccess[index] = false;
 				}
 				log.info(type + " is allowed in " + highwayType + " " + wayName);
 			}
 			else if (accessTagValue.equalsIgnoreCase("destination")) {
 				if (type.equals("motorcar") ||
 				    type.equals("motorcycle")) {
 					road.setNoThroughRouting();
 				} else if (type.equals("access")) {
 					log.warn("access=destination only affects routing for cars in " + highwayType + " " + wayName);
 					road.setNoThroughRouting();
 				} else {
 					log.warn(type + "=destination ignored in " + highwayType + " " + wayName);
 				}
 			} else if (accessTagValue.equalsIgnoreCase("unknown")) {
 				// implicitly allow access
 			} else {
 				log.warn("Ignoring unsupported access tag value " + type + "=" + accessTagValue + " in " + highwayType + " " + wayName);
 			}
 		}
 
 		road.setAccess(noAccess);
 
 		if(way.isBoolTag("toll"))
 			road.setToll();
 
 		// if the way is a motorway and has a ref tag, we may be
 		// generating a Garmin "highway" record for it so save the ref
 		// tag for later
 		String ref = combineRefs(way);
 		if(ref != null) {
 			road.setRef(ref);
 		}
 
 		Way origWay = originalWay.get(way);
 		if(origWay == null)
 			origWay = way;
 
 		int numNodes = nodeIndices.size();
 		road.setNumNodes(numNodes);
 
 		if(numNodes > 0) {
 			// replace Coords that are nodes with CoordNodes
 			boolean hasInternalNodes = false;
 			CoordNode lastCoordNode = null;
 			List<RestrictionRelation> lastRestrictions = null;
 			for(int i = 0; i < numNodes; ++i) {
 				int n = nodeIndices.get(i);
 				if(n > 0 && n < points.size() - 1)
 					hasInternalNodes = true;
 				Coord coord = points.get(n);
 				Integer nodeId = nodeIdMap.get(coord);
 				boolean boundary = boundaryCoords.contains(coord);
 				if(boundary) {
 					log.info("Way " + wayName + "'s point #" + n + " at " + points.get(n).toDegreeString() + " is a boundary node");
 				}
 
 				CoordNode thisCoordNode = new CoordNode(coord.getLatitude(), coord.getLongitude(), nodeId, boundary);
 				points.set(n, thisCoordNode);
 
 				// see if this node plays a role in any turn
 				// restrictions
 
 				if(lastRestrictions != null) {
 					// the previous node was the location of one or
 					// more restrictions
 					for(RestrictionRelation rr : lastRestrictions) {
 						if(rr.getToWay().equals(origWay)) {
 							rr.setToNode(thisCoordNode);
 						}
 						else if(rr.getFromWay().equals(origWay)) {
 							rr.setFromNode(thisCoordNode);
 						}
 						else {
 							rr.addOtherNode(thisCoordNode);
 						}
 					}
 				}
 
 				List<RestrictionRelation> theseRestrictions = restrictions.get(coord);
 				if(theseRestrictions != null) {
 					// this node is the location of one or more
 					// restrictions
 					for(RestrictionRelation rr : theseRestrictions) {
 						rr.setViaNode(thisCoordNode);
 						if(rr.getToWay().equals(origWay)) {
 							if(lastCoordNode != null)
 								rr.setToNode(lastCoordNode);
 						}
 						else if(rr.getFromWay().equals(origWay)) {
 							if(lastCoordNode != null)
 								rr.setFromNode(lastCoordNode);
 						}
 						else if(lastCoordNode != null) {
 							rr.addOtherNode(lastCoordNode);
 						}
 					}
 				}
 
 				lastRestrictions = theseRestrictions;
 				lastCoordNode = thisCoordNode;
 			}
 
 			road.setStartsWithNode(nodeIndices.get(0) == 0);
 			road.setInternalNodes(hasInternalNodes);
 		}
 
 		lineAdder.add(road);
 
 		if(trailingWay != null)
 			addRoadWithoutLoops(trailingWay, gt);
 	}
 
 	// split a Way at the specified point and return the new Way (the
 	// original Way is truncated)
 
 	Way splitWayAt(Way way, int index) {
 		Way trailingWay = new Way();
 		List<Coord> wayPoints = way.getPoints();
 		int numPointsInWay = wayPoints.size();
 
 		for(int i = index; i < numPointsInWay; ++i)
 			trailingWay.addPoint(wayPoints.get(i));
 
 		// ensure split point becomes a node
 		wayPoints.get(index).incHighwayCount();
 
 		// copy the way's name and tags to the new way
 		trailingWay.setName(way.getName());
 		trailingWay.copyTags(way);
 
 		// remove the points after the split from the original way
 		// it's probably more efficient to remove from the end first
 		for(int i = numPointsInWay - 1; i > index; --i)
 			wayPoints.remove(i);
 
 		// associate the original Way to the new Way
 		Way origWay = originalWay.get(way);
 		if(origWay == null)
 			origWay = way;
 		originalWay.put(trailingWay, origWay);
 
 		return trailingWay;
 	}
 
 	// function to add points between adjacent nodes in a roundabout
 	// to make gps use correct exit number in routing instructions
 	void frigRoundabout(Way way, double frigFactor) {
 		List<Coord> wayPoints = way.getPoints();
 		int origNumPoints = wayPoints.size();
 
 		if(origNumPoints < 3) {
 			// forget it!
 			return;
 		}
 
 		int[] highWayCounts = new int[origNumPoints];
 		int middleLat = 0;
 		int middleLon = 0;
 		highWayCounts[0] = wayPoints.get(0).getHighwayCount();
 		for(int i = 1; i < origNumPoints; ++i) {
 			Coord p = wayPoints.get(i);
 			middleLat += p.getLatitude();
 			middleLon += p.getLongitude();
 			highWayCounts[i] = p.getHighwayCount();
 		}
 		middleLat /= origNumPoints - 1;
 		middleLon /= origNumPoints - 1;
 		Coord middleCoord = new Coord(middleLat, middleLon);
 
 		// account for fact that roundabout joins itself
 		--highWayCounts[0];
 		--highWayCounts[origNumPoints - 1];
 
 		for(int i = origNumPoints - 2; i >= 0; --i) {
 			Coord p1 = wayPoints.get(i);
 			Coord p2 = wayPoints.get(i + 1);
 			if(highWayCounts[i] > 1 && highWayCounts[i + 1] > 1) {
 				// both points will be nodes so insert a new point
 				// between them that (approximately) falls on the
 				// roundabout's perimeter
 				int newLat = (p1.getLatitude() + p2.getLatitude()) / 2;
 				int newLon = (p1.getLongitude() + p2.getLongitude()) / 2;
 				// new point has to be "outside" of existing line
 				// joining p1 and p2 - how far outside is determined
 				// by the ratio of the distance between p1 and p2
 				// compared to the distance of p1 from the "middle" of
 				// the roundabout (aka, the approx radius of the
 				// roundabout) - the higher the value of frigFactor,
 				// the further out the point will be
 				double scale = 1 + frigFactor * p1.distance(p2) / p1.distance(middleCoord);
 				newLat = (int)((newLat - middleLat) * scale) + middleLat;
 				newLon = (int)((newLon - middleLon) * scale) + middleLon;
 				Coord newPoint = new Coord(newLat, newLon);
 				double d1 = p1.distance(newPoint);
 				double d2 = p2.distance(newPoint);
 				double maxDistance = 100;
 				if(d1 >= MIN_DISTANCE_BETWEEN_NODES && d1 <= maxDistance &&
 				   d2 >= MIN_DISTANCE_BETWEEN_NODES && d2 <= maxDistance) {
 				    newPoint.incHighwayCount();
 				    wayPoints.add(i + 1, newPoint);
 				}
 			}
 		}
 	}
 
 	int getSpeedIdx(String tag)
 	{
 		double kmh;
 		double factor = 1.0;
 		
 		String speedTag = tag.toLowerCase().trim();
 		
 		if(speedTag.matches(".*mph")) // Check if it is a limit in mph
 		{
 			speedTag = speedTag.replaceFirst("mph", "");
 			factor = 1.61;
 		}
 		else
 			speedTag = speedTag.replaceFirst("kmh", "");  // get rid of kmh just in case
 		
 		try {
 			kmh = Integer.parseInt(speedTag) * factor;
 		}
 		catch (Exception e)
 		{
 			return -1;
 		}
 		
 		if(kmh > 110)
 			return 7;
 		if(kmh > 90)
 			return 6;
 		if(kmh > 80)
 			return 5;
 		if(kmh > 60)
 			return 4;
 		if(kmh > 40)
 			return 3;
 		if(kmh > 20)
 			return 2;
 		if(kmh > 10)
 			return 1;
 		else
 			return 0;	    
 
 	}
 
 	protected boolean accessExplicitlyAllowed(String val) {
 		if (val == null)
 			return false;
 
 		return (val.equalsIgnoreCase("yes") ||
 			val.equalsIgnoreCase("designated") ||
 			val.equalsIgnoreCase("permissive"));
 	}
 
 	protected boolean accessExplicitlyDenied(String val) {
 		if (val == null)
 			return false;
 
 		return (val.equalsIgnoreCase("no") ||
 			val.equalsIgnoreCase("private"));
 	}
 }
