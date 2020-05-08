 package white;
 
 import hypeerweb.Node;
 import junit.framework.TestCase;
 
 /**
  * 
  * @author Brian Davis
  *
  */
 
 public class NodeTest extends TestCase
 {   
     //Most tests are covered in BB. Justification given throughout code.
     
     //addNeighbor, addUpPointer, addDownPointer:  relational condition testing covered in BB
     
     public void testRemoveNeighbor()
     {
         //conditional testing
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         node0.removeNeighbor(null);
         assert node0.getNeighborsIds().isEmpty();
         node0.removeNeighbor(node1);
         assert node0.getNeighborsIds().isEmpty();
         
         node0.addNeighbor(node1);
         node0.removeNeighbor(null);
         assert node0.getNeighborsIds().size() == 1;
         node0.removeNeighbor(node1);
         assert node0.getNeighborsIds().size() == 0;
     }
     
     //findCapNode, findInsertionPoint: The exhaustive testing in BB covers all needed loop conditions
     
     //removeFromHyPeerWeb: The exhaustive testing in BB covers all needed logic conditions
     
     //findDeletionPoint: The exhaustive testing in BB covers all needed loop and logic conditions
     
     public void testGetLowestNeighborWithoutChild()
     {
         //relational condition testing
         
         //null case
         Node node0 = new Node(0);
         assert node0.getLowestNeighborWithoutChild() == node0;
         
         //temp less than this (0<1), temp height less than this height (0<1)
         Node node1 = new Node(1);
         node1.addNeighbor(node0);
         assert node1.getLowestNeighborWithoutChild() == node0;
         
         //temp greater than this (1>0), temp height less than this height (1<2)
         Node node8 = new Node(8);
         node0.addNeighbor(node1);
         node0.addNeighbor(node8);
        assert node0.getLowestNeighborWithoutChild() == node1;
         
         //temp less than this(0<1), temp height greater than this height (2>1)
         Node node11 = new Node(11);
         node0.removeNeighbor(node1);
         node0.addNeighbor(node11);
         assert node1.getLowestNeighborWithoutChild() == node1;
         
         //temp greater than this(1>0), temp height greater than this height (3>1)
         node1.addNeighbor(node8);
         node1.addNeighbor(node11);
        assert node0.getLowestNeighborWithoutChild() == node8;
         
         //temp less than this (0<8), temp height equal this height (1=1)
         Node node0b = new Node(0);
         Node node8b = new Node(8);
         node8b.addNeighbor(node0b);
         node0b.addNeighbor(node8b);
         assert node8b.getLowestNeighborWithoutChild() == node0b;
         
         //temp greater than this (8>0), temp height equal to this height (1<2)
        assert node0.getLowestNeighborWithoutChild() == node8;
         
         //temp equal to this is unreachable
     }
 }
