 package model_example;
 
 import edu.berkeley.path.model_mock.*;
 import edu.berkeley.path.model_elements.*;
 
 import java.util.*;
 
 import org.apache.avro.Schema;
 import org.apache.avro.generic.GenericData;
 
 public class NetworkSource extends MockDataSource {
   /**
    * Construct a network source with an example network ready to
    * be consumed by the example.
    */
   public NetworkSource() {
     super();
     
     Network nw = new Network();
     
     nw.setName("example network");
     
     Schema schema = nw.getSchema();
 
    nw.setId(42L);
     nw.setNodes(new ArrayList<edu.berkeley.path.model_elements_base.Node>());
     nw.setLinks(new ArrayList<edu.berkeley.path.model_elements_base.Link>());
     
     Node nd1;
     Node nd2;
     Link ln;
 
     nd1 = new Node();
    nd1.setId(1L);
     nd1.setName("one");
     nd1.setType("hwy");
     nw.nodes.add(nd1);
 
     nd2 = new Node();
    nd2.setId(2L);
     nd2.setName("two");
     nd2.setType("hwy");
     nw.nodes.add(nd2);
 
     ln = new Link();
    ln.setId(3L);
     ln.setName("two");
     ln.setType("hwy");
     ln.setLaneCount(4.0);
     ln.setLength(1000.0);
     
     ln.setBegin(nd1);
     ln.setEnd(nd2);
     
     nw.links.add(ln);
 
     insert(nw);
   }
   
   public Network getNetwork() {
     return (Network)get().x();
   }
 }
