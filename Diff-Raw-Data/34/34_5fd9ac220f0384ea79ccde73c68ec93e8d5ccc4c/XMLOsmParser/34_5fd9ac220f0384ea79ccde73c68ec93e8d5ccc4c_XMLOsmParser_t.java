 package cz.nuc.wheelgo;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.ws.Holder;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import cz.nuc.wheelgo.dijkstra.Edge;
 import cz.nuc.wheelgo.dijkstra.Vertex;
 
 public class XMLOsmParser {
 
 	public static void parseMap(String xml, NavigationParameters params,
 			Holder<List<Vertex>> retNodes, Holder<List<Edge>> retEdges)
 			throws XPathExpressionException, SAXException, IOException,
 			ParserConfigurationException {
 		Map<String, Vertex> vertices = new HashMap<String, Vertex>();
 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		Document doc = builder.parse(new ByteArrayInputStream(xml
 				.getBytes("UTF-8")));
 
 		HashMap<String, Vertex> allNodes = new HashMap<String, Vertex>();
 		NodeList nodes = doc.getElementsByTagName("node");
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Element node = (Element) nodes.item(i);
 			allNodes.put(
 					node.getAttribute("id"),
 					new Vertex(node.getAttribute("id"),
 							node.getAttribute("id"), Double.parseDouble(node
 									.getAttribute("lat")), Double
 									.parseDouble(node.getAttribute("lon"))));
 		}
 
 		ArrayList<Edge> edges = new ArrayList<Edge>();
 		NodeList ways = doc.getElementsByTagName("way");
 		for (int i = 0; i < ways.getLength(); i++) {
 			Element node = (Element) ways.item(i);
 
 			// ignore ways without highway tag
			/*boolean interesting = false;
 			NodeList tags = node.getElementsByTagName("tag");
 			for (int j = 0; j < tags.getLength(); j++) {
 				Element tag = (Element) tags.item(j);
 				if (tag.hasAttribute("k")
 						&& tag.getAttribute("k").equals("highway")) {
 					interesting = true;
 				}
 			}
 
 			if (interesting == false)
				continue;*/
 
 			String edgeId = node.getAttribute("id");
 			Vertex oldVertex = null;
 			Vertex vertex = null;
 			// go through its ref attribute to obtain vertexes
 			try {
 				NodeList references = node.getElementsByTagName("nd");
 				for (int j = 0; j < references.getLength(); j++) {
 					Element tag = (Element) references.item(j);
 					String id = tag.getAttribute("ref");
 					if (vertices.containsKey(id) == false) {
 						vertex = allNodes.get(id);
 						vertices.put(id, vertex);
 					} else
 						vertex = vertices.get(id);
 					if (oldVertex != null) {
 						float distance = Edge
 								.calculateWeight(vertex, oldVertex, node, params);
 						if (params != null && params.locationsToAvoid != null) {
 							for (Location avoid : params.locationsToAvoid) {
 								Vertex temp = new Vertex("", "");
 								temp.setLatitude(avoid.latitude);
 								temp.setLongitude(avoid.longitude);
 								if (NavigationTask.distToSegment(temp, vertex, oldVertex) < 0.0001) {
 									distance = Float.MAX_VALUE;
 								}
 							}
 						}
 
 						Edge e = new Edge(edgeId, oldVertex, vertex, distance);
 						edges.add(e);
 
 						e = new Edge(edgeId, vertex, oldVertex, distance);
 						edges.add(e);
 					}
 					oldVertex = vertex;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 
 		/*
 		 * retNodes = new Holder<List<Vertex>>((List<Vertex>) ret.values());
 		 * retEdges = new Holder<List<Edge>>(edges);
 		 */
 		retNodes.value = new ArrayList<Vertex>(vertices.values());
 		retEdges.value = edges;
 	}
 }
