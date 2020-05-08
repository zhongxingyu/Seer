 package ch.zhaw.jroute.routedata;
 
 import gov.nasa.worldwind.geom.Angle;
 import gov.nasa.worldwind.geom.Position;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 
 import ch.zhaw.jroute.model.Way;
 import ch.zhaw.jroute.model.WayStatusEnum;
 import ch.zhaw.jroute.model.Waypoint;
 
 import com.ximpleware.AutoPilot;
 import com.ximpleware.EOFException;
 import com.ximpleware.EncodingException;
 import com.ximpleware.EntityException;
 import com.ximpleware.NavException;
 import com.ximpleware.ParseException;
 import com.ximpleware.PilotException;
 import com.ximpleware.VTDGen;
 import com.ximpleware.VTDNav;
 
 /**
  * BoxHandler Class for API Calls to openstreetmap.org needed for getting all
  * ways and waypoints with all values
  * 
  * defined through Interface IBoxHandler
  * 
  * @author pascal
  */
 public class BoxHandler implements IBoxHandler {
 	private static Logger logger = Logger.getLogger("org.apache.log4j");
 	// private final String openStreetMapBoxURL =
 	// "http://open.mapquestapi.com/xapi/api/0.6/way[bbox=";
 	private final String openStreetMapBoxURL = "http://www.overpass-api.de/api/xapi?way[bbox=";
 	private IAPIConnector apiConnector;
 	private List<String> streetFilterList;
 	private List<Way> effectiveWayList;
 	private HashMap<Long, Waypoint> allWaypoints;
 	private HashMap<Long, Way> allWays;
 
 	public BoxHandler(IAPIConnector connector) {
 		this.apiConnector = connector;
 	}
 
 	/**
 	 * defined through Interface IBoxHandler
 	 * 
 	 * @throws IOException
 	 */
 	@Override
 	public List<Way> getAllWays(double left, double bottom, double right,
 			double top) throws IOException {
 
 		double powerSek = Math.pow(10.0, 9.0);
 		
 		// initialize the worker lists
 		streetFilterList = new ArrayList<String>();
 		allWaypoints = new HashMap<Long, Waypoint>();
 		allWays = new HashMap<Long, Way>();
 
 		// start method time measuring
 		long startMethodTime = System.nanoTime();
 
 		// check for correct coordinates
 		checkLatitudeCoordinates(bottom, top);
 		checkLongitudeCoordinates(left, right);
 
 		// set street filter (manually)
 		// addNewStreetFilter("motorway");
 		// addNewStreetFilter("tertiary");
 		// addNewStreetFilter("residential");
 
 		// use filter
 		String filterForURL = createFilterForURL();
 
 		// create url
 		URL boxURL;
 		try {
 			boxURL = new URL(openStreetMapBoxURL + left + "," + bottom + ","
 					+ right + "," + top + "][highway=" + filterForURL + "]");
 		} catch (MalformedURLException ex) {
 			logger.fatal(ex);
 			throw new RuntimeException(ex);
 		}
 
 		// start timer for connection
 		long startApiCallTime = System.nanoTime();
 
 		// get document via connection
 		Document document = apiConnector.getDocumentOverNewConnection(boxURL);
 		
 		TransformerFactory transformerFactory = TransformerFactory
 				.newInstance();
 		Transformer transformer = null;
 		try {
 			transformer = transformerFactory.newTransformer();
 		} catch (TransformerConfigurationException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		}
 
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
 		DOMSource source = new DOMSource(document);
 		StreamResult result = new StreamResult(bos);
 
 		try {
 			transformer.transform(source, result);
 		} catch (TransformerException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		}
 
 		byte[] array = bos.toByteArray();
 		
 		// stop timer for connection
 		long endApiCallTime = System.nanoTime();
 		long apiCallTime = endApiCallTime - startApiCallTime;
 		
 		long setWaypointsForWaysStartTime = System.nanoTime();
 		HashMap<Long, Waypoint> allWaypoints = this
 				.getAllWaypointFromDocument(array);
 		long setWaypointsForWaysEndTime = System.nanoTime();
 		
 		List<Way> matchingWayList = null;
 		
 		// get all ways, which match with the filter
 		long setSelectedWaysStartTime = System.nanoTime();
 		matchingWayList = getSelectedWays(array, allWaypoints);
 		long setSelectedWaysEndTime = System.nanoTime();
 		logger.debug("time setWaypointsForWaysTime : "
 				+ ((setWaypointsForWaysEndTime - setWaypointsForWaysStartTime)/powerSek)
 				+ " s");
 		logger.debug("time setSelectedWaysTime : "
 				+ ((setSelectedWaysEndTime - setSelectedWaysStartTime)/powerSek) + " s");
 
 		long setHandleCrossRoadsStartTime = System.nanoTime();
 		// handler for crossroads
 		handleCrossRoads(matchingWayList);
 		long setHandleCrossRoadsEndTime = System.nanoTime();
 		
 		logger.debug("time setHandleCrossRoadsTime : "
 				+ ((setHandleCrossRoadsEndTime-setHandleCrossRoadsStartTime)/powerSek) + " s");
 		
 		// counting ways and waypoints for debugging
 		int matchedWaySize = matchingWayList.size();
 		int effecticeWaySize = effectiveWayList.size();
 		int filterSize = streetFilterList.size();
 
 		// stop timer for connection
 		long endMethodTime = System.nanoTime();
 		long methodTime = endMethodTime - startMethodTime;
 
 		// sysout for debugging
 		logger.debug("get all data from openstreetmap took: " + apiCallTime/powerSek
 				+ " s");
 		logger.debug("running whole method took: " + methodTime/powerSek + " s");
 		logger.debug("time for document processing : "
 				+ (methodTime - apiCallTime)/powerSek + " s");
 		logger.debug("total ways matched: " + matchedWaySize);
 		logger.debug("total effective ways: " + effecticeWaySize);
 		logger.debug("total waypoints matched: " + allWaypoints.size());
 		logger.debug("filter size: " + filterSize);
 
 		List<Way> resultWayList = new ArrayList<Way>();
 		resultWayList.addAll(effectiveWayList);
 
 		// cleanup
 		cleanUpForGui();
 		matchingWayList.clear();
 
 		// return wayList;
 		return resultWayList;
 	}
 
 	private HashMap<Long, Waypoint> getAllWaypointFromDocument(byte[] array) {
 
 		allWaypoints = new HashMap<Long, Waypoint>();
 
 		VTDGen vg = new VTDGen();
 		vg.setDoc(array);
 		try {
 			vg.parse(false);
 		} catch (EncodingException e) {
 			logger.fatal(e);
 		} catch (EOFException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (EntityException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (ParseException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		}
 
 		VTDNav vn = vg.getNav();
 
 		AutoPilot ap = new AutoPilot(vn);
 
 		ap.selectElement("node");
 
 		try {
 			while (ap.iterate()) {
 
 				int latPos = vn.getAttrVal("lat");
 				int lonPos = vn.getAttrVal("lon");
 				int idPos = vn.getAttrVal("id");
 
 				double lat = Double.parseDouble(vn.toString(latPos));
 
 				double lon = Double.parseDouble(vn.toString(lonPos));
 
 				long id = Long.parseLong(vn.toString(idPos));
 
 				Waypoint waypoint = new Waypoint();
 
 				waypoint.setWaypointID(id);
 
 				// set latitude and longitude for waypoint
 				waypoint.setLat(lat);
 				waypoint.setLon(lon);
 
 				// set position (lon and lat in one value)
 				Angle latAngle = Angle.fromDegreesLatitude(lat);
 				Angle lonAngle = Angle.fromDegreesLatitude(lon);
 				Position pos = new Position(latAngle, lonAngle, 0);
 				waypoint.setCenter(pos);
 
 				allWaypoints.put(id, waypoint);
 			}
 		} catch (PilotException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (NavException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		}
 
 		return allWaypoints;
 	}
 
 	/**
 	 * clean up the lists for the gui for more queries
 	 */
 	private void cleanUpForGui() {
 		streetFilterList.clear();
 		effectiveWayList.clear();
 		allWaypoints.clear();
 		allWays.clear();
 	}
 
 	/**
 	 * create the url for the api-call with the correct filterlist
 	 * 
 	 * @return String
 	 */
 	private String createFilterForURL() {
 		String filterURL = null;
 		for (String filter : streetFilterList) {
 			if (filterURL == null) {
 				filterURL = filter;
 			} else {
 				filterURL = filterURL + "|" + filter;
 			}
 		}
 		if (filterURL == null) {
 			filterURL = "*";
 		}
 		return filterURL;
 	}
 
 	/**
 	 * adding an additional filter-value for the streets
 	 * 
 	 * @param filter
 	 */
 	private void addNewStreetFilter(String filter) {
 		streetFilterList.add(filter);
 	}
 
 	/**
 	 * handle the crossroads...
 	 * 
 	 * @param wayList
 	 */
 	private void handleCrossRoads(List<Way> wayList) {
 
 		List<Way> splittedWay = new ArrayList<Way>();
 
 		for (Waypoint waypoint : allWaypoints.values()) {
 			if (waypoint.getAddedWays().size() > 1) {
 				for (Way way : waypoint.getAddedWays()) {
 					if (way.getStart() == waypoint || way.getEnd() == waypoint) {
 					} else {
 						way.addSplitPoint(waypoint);
 						if (!splittedWay.contains(way)) {
 							splittedWay.add(way);
 						}
 					}
 				}
 			}
 		}
 
 		for (Way way : splittedWay) {
 
 			effectiveWayList.remove(way);
 
 			int start = 0;
 			int end = 0;
 			long generatedKey = 0;
 
 			List<Waypoint> splitWaypointList = sortWaypoints(
 					way.getWaypointList(), way.getSplitPointList());
 
 			for (Waypoint waypoint : splitWaypointList) {
 				end = way.getWaypointList().indexOf(waypoint);
 
 				if (end < start) {
 					int temp = end;
 					end = start;
 					start = temp;
 				}
 
 				List<Waypoint> newWaypoints = getWaypointSubset(start, end,
 						way.getWaypointList());
 
 				Way newWay = new Way();
 				newWay.setStatus(WayStatusEnum.undefined);
 
 				generatedKey = way.getWayID() + 1;
 
 				while (allWays.containsKey(generatedKey)) {
 					generatedKey++;
 				}
 
 				newWay.setWayID(generatedKey);
 				newWay.setName(String.valueOf(way.getWayID()) + "Part" + start
 						+ "-" + end);
 				Waypoint startPoint = newWaypoints.get(0);
 				Waypoint endPoint = waypoint;
 
 				newWay.setStart(startPoint);
 				newWay.setEnd(endPoint);
 				newWay.setWaypointList(newWaypoints);
 				effectiveWayList.add(newWay);
 				allWays.put(newWay.getWayID(), newWay);
 
 				start = end;
 			}
 
 			if (end != way.getWaypointList().size() - 1) {
 				end = way.getWaypointList().size() - 1;
 				List<Waypoint> newWaypoints = getWaypointSubset(start, end,
 						way.getWaypointList());
 				Way newWay = new Way();
 				newWay.setStatus(WayStatusEnum.undefined);
 				newWay.setName(String.valueOf(way.getWayID()) + "Part" + start
 						+ "-" + end);
 
 				while (allWays.containsKey(generatedKey)) {
 					generatedKey++;
 				}
 
 				newWay.setWayID(generatedKey);
 				start = 0;
 				end = newWaypoints.size() - 1;
 				Waypoint startPoint = newWaypoints.get(start);
 				Waypoint endPoint = newWaypoints.get(end);
 
 				newWay.setStart(startPoint);
 				newWay.setEnd(endPoint);
 				newWay.setWaypointList(newWaypoints);
 				effectiveWayList.add(newWay);
 				allWays.put(newWay.getWayID(), newWay);
 			}
 		}
 	}
 
 	/**
 	 * returns the waypoint subset
 	 * 
 	 * @param from
 	 * @param to
 	 * @param targetList
 	 * @return List<Waypoint>
 	 */
 	private List<Waypoint> getWaypointSubset(int from, int to,
 			List<Waypoint> targetList) {
 
 		List<Waypoint> resultList = new ArrayList<Waypoint>();
 
 		for (int i = from; i <= to; i++) {
 			resultList.add(targetList.get(i));
 		}
 
 		return resultList;
 
 	}
 
 	/**
 	 * set the correct order for the waypoints
 	 * 
 	 * @param allWaypoint
 	 * @param splitWaypoint
 	 * @return List<Waypoint> (sorted)
 	 */
 	private List<Waypoint> sortWaypoints(List<Waypoint> allWaypoint,
 			List<Waypoint> splitWaypoint) {
 		TreeMap<Integer, Waypoint> tm = new TreeMap<Integer, Waypoint>();
 		for (Waypoint waypoint : splitWaypoint) {
 			int index = allWaypoint.indexOf(waypoint);
 			tm.put(index, waypoint);
 		}
 		List<Waypoint> returnList = new ArrayList<Waypoint>(tm.values());
 		return returnList;
 
 	}
 
 	/**
 	 * get all ways
 	 * 
 	 * @param allWaypoints
 	 * 
 	 * @param document
 	 * @param xpath
 	 * @return
 	 * @throws Exception
 	 */
 	private List<Way> getSelectedWays(byte[] array,
 			HashMap<Long, Waypoint> allWaypoints) throws IOException {
 		List<Way> matchingWayList = new ArrayList<Way>();
 		effectiveWayList = new ArrayList<Way>();
 
 		VTDGen vg = new VTDGen();
 		vg.setDoc(array);
 		try {
 			vg.parse(false);
 		} catch (EncodingException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (EOFException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (EntityException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (ParseException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		}
 
 		VTDNav vn = vg.getNav();
 
 		AutoPilot ap = new AutoPilot(vn);
 		ap.selectElement("way");
 
 		try {
 			while (ap.iterate()) {
 				int idPos = vn.getAttrVal("id");
 
 				long wayId = Long.parseLong(vn.toString(idPos));
 
 				Way way = new Way(wayId);
 				way.setStatus(WayStatusEnum.undefined);
 
 				vn.toElement(VTDNav.FIRST_CHILD, "nd");
 				do {
					int wpIdPos = vn.getAttrVal("ref");
					long wpId = Long.parseLong(vn.toString(wpIdPos));
 					way.addWaypoint(allWaypoints.get(wpId));
 					allWaypoints.get(wpId).addWay(way);
 				} while (vn.toElement(VTDNav.NEXT_SIBLING, "nd"));
 
 				// get start and end waypoint
 				Waypoint tempStartWaypoint = way.getWaypointList().get(0);
 				int lastTempWaypoint = way.getWaypointList().size() - 1;
 				Waypoint tempEndWaypoint = way.getWaypointList().get(
 						lastTempWaypoint);
 
 				// set start and end for the way
 				way.setStart(tempStartWaypoint);
 				way.setEnd(tempEndWaypoint);
 
 				allWays.put(way.getWayID(), way);
 				matchingWayList.add(way);
 			}
 		} catch (PilotException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (NumberFormatException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		} catch (NavException e) {
 			logger.fatal(e);
 			throw new RuntimeException(e);
 		}
 
 		effectiveWayList.addAll(matchingWayList);
 
 		return matchingWayList;
 
 	}
 
 	/**
 	 * check, if the longitude coordinates are correct
 	 * 
 	 * @param left
 	 * @param right
 	 * @throws IllegalArgumentException
 	 */
 	private void checkLongitudeCoordinates(double left, double right) {
 		if (left > 90 || left < -90) {
 			throw new IllegalArgumentException(
 					"left longitude must be between -90 and 90");
 		}
 		if (right > 90 || right < -90) {
 			throw new IllegalArgumentException(
 					"right longitude must be between -90 and 90");
 		}
 		if (left > right) {
 			throw new IllegalArgumentException(
 					"first parameter can't be bigger than second!!");
 		}
 
 	}
 
 	/**
 	 * check, if the latitude coordinates are correct
 	 * 
 	 * @param bottom
 	 * @param top
 	 * @throws IllegalArgumentException
 	 */
 	private void checkLatitudeCoordinates(double bottom, double top) {
 		if (top > 180 || top < -180) {
 			throw new IllegalArgumentException(
 					"top latitude must be between -180 and 180");
 		}
 		if (bottom > 180 || bottom < -180) {
 			throw new IllegalArgumentException(
 					"bottom latitude must be between -180 and 180");
 		}
 		if (bottom > top) {
 			throw new IllegalArgumentException(
 					"first parameter can't be bigger than second!!");
 		}
 
 	}
 
 }
