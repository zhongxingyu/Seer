 package edu.uaskl.cpp.exporter;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import static edu.uaskl.cpp.importer.OsmImporter.*;
 import static edu.uaskl.cpp.model.exporter.Exporter.*;
 import edu.uaskl.cpp.model.edge.EdgeCppOSM;
 import edu.uaskl.cpp.model.graph.GraphUndirected;
 import edu.uaskl.cpp.model.node.NodeCppOSM;
 import edu.uaskl.cpp.model.path.PathExtended;
 
 public class ExportTest {
     @Rule
     public TemporaryFolder folder = new TemporaryFolder();
 
     @Test
     public void test() {
         final GraphUndirected<NodeCppOSM, EdgeCppOSM> graph = importOsmUndirected(getClass().getResource("../fh_way_no_meta.osm").toString());
         final List<NodeCppOSM> nodes = new LinkedList<>();
        nodes.add(graph.getNode(280959081l));
        nodes.add(graph.getNode(267970528l));
        nodes.add(graph.getNode(267969715l));
        nodes.add(graph.getNode(267969713l));
        nodes.add(graph.getNode(280959081l));
 
         final PathExtended<NodeCppOSM> path = new PathExtended<>(nodes);
         exportPathToHTML(path, folder.getRoot()); //
         assertTrue(true); // TODO well, not that helpful. you could check if the file exists and some expected content is there -tbach
     }
 
 }
