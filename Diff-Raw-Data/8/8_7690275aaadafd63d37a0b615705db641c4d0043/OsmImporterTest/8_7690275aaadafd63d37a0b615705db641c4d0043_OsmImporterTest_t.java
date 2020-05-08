 package edu.uaskl.cpp.importer;
 
 
 
 import java.util.HashMap;
 
import static org.fest.assertions.api.Assertions.*;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import edu.uaskl.cpp.importer.OsmNode;
 import static edu.uaskl.cpp.importer.OsmImporter.*;
 import static org.junit.Assert.*;
 import edu.uaskl.cpp.model.graph.*;
 
 public class OsmImporterTest {
 
 	@Test
 	public void testGetDom() {
 		Document dom = getDomFromFile("fh_way_no_meta.osm");
 		NodeList testnodes = dom.getDocumentElement().getElementsByTagName("node");
		assertThat(dom).isNotNull();
		assertThat(testnodes.getLength()).isNotEqualTo(0);
		assertThat(dom.getElementsByTagName("node")).isNotNull();
 	}
 	
 	@Test
 	public void testNanoSec(){
 		long lat = get100NanoDegrees("49.2572968");
 		long lon = get100NanoDegrees("49.259868");
 		assertEquals("nanoseconds parser does not work",lat, 492572968l);
 		assertEquals("nanoseconds parser does not work",lon , 492598680l);
 	}
 	
 	@Test
 	public void testGetOsmNodes(){
 		Document dom = getDomFromFile("fh_way_no_meta.osm");
 		HashMap<String, OsmNode> map = getOsmNodes(dom);
 		assertFalse("Node map should not be empty",map.isEmpty());
 	}
 
 	@Test
 	public void testImportOsmUndirected(){
 		//GraphUndirected graph = testImporter.importOsmUndirected("zweibruecken_way_no_meta.osm");
 		GraphUndirected graph = importOsmUndirected("fh_way_no_meta.osm");
 		assertNotEquals("Graph should have nodes",graph.getNumberOfNodes(),0);
 		assertNotEquals("Graph should have edges",graph.getGetNumberOfEdges(),0);
 		//System.out.println(graph.toString());
 		//System.out.println(graph.getStatistics());
 	}
 }
