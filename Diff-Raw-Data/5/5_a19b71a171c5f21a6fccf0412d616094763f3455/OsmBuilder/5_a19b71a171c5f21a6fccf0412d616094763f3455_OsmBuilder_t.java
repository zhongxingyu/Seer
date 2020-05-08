 package com.oschrenk.gis.formats.osm.io;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.oschrenk.gis.formats.osm.Node;
 import com.oschrenk.gis.formats.osm.Osm;
 import com.oschrenk.gis.formats.osm.Way;
import com.oschrenk.gis.geometry.core.BoundingBox;
 
 /**
  * The Class OsmBuilder.
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class OsmBuilder {
 
 	/** The log. */
 	final Logger log = LoggerFactory.getLogger(OsmBuilder.class);
 
 	/** The factory. */
 	private XMLInputFactory factory = XMLInputFactory.newInstance();
 
 	/** The parser. */
 	private XMLStreamReader parser;
 
 	/** The osm file. */
 	private File osmFile;
 
 	/**
 	 * Instantiates a new osm builder.
 	 * 
 	 * @param osmFile
 	 *            the osm file
 	 */
 	public OsmBuilder(File osmFile) {
 		this.osmFile = osmFile;
 	}
 
 	/**
 	 * Builds the.
 	 * 
 	 * @return the osm
 	 */
 	public Osm build() {
 		final Map<Long, Node> nodeMap = new HashMap<Long, Node>();
 		final List<Way> ways = new LinkedList<Way>();
 
 		try {
 			parser = factory.createXMLStreamReader(new FileInputStream(osmFile));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 
 		try {
 
 			long currentNodeId = 0;
 			double latitude = 0;
 			double longitude = 0;
 
 			long currentWayId = 0;
 
 			Node currentNode = null;
 
 			Stack<Long> nodeIdStack = new Stack<Long>();
 			Map<String, String> tags = null;
 
 			while (parser.hasNext()) {
 
 				switch (parser.getEventType()) {
 				case XMLStreamConstants.END_DOCUMENT:
 					parser.close();
 					break;
 
 				case XMLStreamConstants.START_ELEMENT:
 
 					//	<node id='3596186' lat='53.4633699598014' lon='-2.22667910006381' timestamp='2007-06-21T17:10:58+01:00' version='2' changeset='2213'>
 					//		<tag k='amenity' v='hospital'/>
 					//		<tag k='name' v='Manchester Royal Infirmary'/>
 					//	</node>
 					if (parser.getLocalName() == "node") {
 						tags = null;
 						for (int i = 0; i < parser.getAttributeCount(); i++) {
 							if (parser.getAttributeLocalName(i) == "id")
 								currentNodeId = Long.valueOf(parser.getAttributeValue(i));
 							else if (parser.getAttributeLocalName(i) == "lat")
 								latitude = Double.valueOf(parser.getAttributeValue(i)).doubleValue();
 							else if (parser.getAttributeLocalName(i) == "lon")
 								longitude = Double.valueOf(parser.getAttributeValue(i)).doubleValue();
 						}
 						currentNode = new Node(currentNodeId, longitude, latitude);
 						nodeMap.put(currentNodeId, currentNode);
 
 						//	<way id='4958218' timestamp='2007-07-25T01:55:35+01:00' version='3' changeset='2211'>
 						//		<nd ref='218963'/>
 						// 		<nd ref='331193'/>
 						//		...
 						//		<tag k='landuse' v='residential'/>
 						//		<tag k='source' v='landsat'/>
 						//	</way>
 					} else if (parser.getLocalName() == "way") {
 						tags = null;
 						nodeIdStack = new Stack<Long>();
 						for (int i = 0; i < parser.getAttributeCount(); i++) {
 							if (parser.getAttributeLocalName(i) == "id")
 								currentWayId = Long.valueOf(parser.getAttributeValue(i));
 						}
 
 						// ...
 						// <nd ref="304994979"/>
 						// ...
 					} else if (parser.getLocalName() == "nd") {
 						for (int i = 0; i < parser.getAttributeCount(); i++) {
 							if (parser.getAttributeLocalName(i) == "ref") {
 								Long nodeId = Long.valueOf(parser.getAttributeValue(i)).longValue();
 								if (nodeMap.get(nodeId) != null) {
 									nodeIdStack.add(nodeId);
 								} else {
 									log.debug("Couldn't find a node with the id {}", nodeId);
 								}
 							}
 						}
 
 						// ...
 						// <tag k='landuse' v='residential'/>
 						// ...
 					} else if (parser.getLocalName() == "tag") {
 						if (tags == null) {
 							tags = new HashMap<String, String>();
 						}
 						String key = null;
 						String value = null;
 						for (int i = 0; i < parser.getAttributeCount(); i++) {
 							if (parser.getAttributeLocalName(i) == "k") {
 								key = parser.getAttributeValue(i);
 							} else if (parser.getAttributeLocalName(i) == "v") {
 								value = parser.getAttributeValue(i);
 							}
 						}
 						if (key != null && value != null) {
 							tags.put(key, value);
 							log.trace("Found tag with k={}, v={}", key, value);
 						}
 
 					}
 
 					break;
 
 				case XMLStreamConstants.END_ELEMENT:
 					if (parser.getLocalName() == "node") {
 						if (tags != null) {
 							currentNode.getTags().putAll(tags);
 							log.trace("Adding tags {} to node {}", tags, currentNode);
 						}
 					} else if (parser.getLocalName() == "way") {
 						if (nodeIdStack != null) {
 							Way way = new Way(currentWayId);
 							way.getTags().putAll(tags);
 							while (nodeIdStack.size() >= 1) {
 								way.addNode(nodeMap.get(nodeIdStack.pop()));
 							}
 							log.trace("Adding way {}", way);
 							ways.add(way);
 						}
 					}
 
 					break;
 
 				default:
 					break;
 				}
 				parser.next();
 			}
 
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 
		return new Osm(BoundingBox.from(nodeMap.values()), new LinkedList<Node>(nodeMap.values()), ways);
 	}
 
 }
