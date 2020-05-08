 package black;
 
 import hypeerweb.Node;
 import hypeerweb.SimplifiedNodeDomain;
 import hypeerweb.Node.State;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import testing.ExpectedResult;
 
 /* Black Box test for Node class
  * 
  * Written by Brian Davis
  * 
  */
 
 public class NodeBlackTest extends TestCase {
 
     Node node0a;
     Node node0b;
     Node node1b;
     Node node0c;
     Node node1c;
     Node node2c;
     Node node0d;
     Node node1d;
     Node node2d;
     Node node3d;
     Node node0e;
     Node node1e;
     Node node2e;
     Node node3e;
     Node node4e;
     
     @Before
     public void setUp() throws Exception 
     {
         node0a = new Node(0);
         node0b = new Node(0);
         node1b = new Node(1);
         node0c = new Node(0);
         node1c = new Node(1);
         node2c = new Node(2);
         node0d = new Node(0);
         node1d = new Node(1);
         node2d = new Node(2);
         node3d = new Node(3);
         node0e = new Node(0);
         node1e = new Node(1);
         node2e = new Node(2);
         node3e = new Node(3);
         node4e = new Node(4);
         
         //size 1
         node0a.setState(State.CAP);
         node0a.setFold(node0a);
         
         //size 2
         node1b.setState(State.CAP);
         node0b.setState(State.STANDARD);
         node0b.addNeighbor(node1b);
         node1b.addNeighbor(node0b);
         node0b.setFold(node1b);
         node1b.setFold(node0b);
         
         //size 3
         node2c.setState(State.DOWN);
         node1c.setState(State.CAP);
         node0c.setState(State.STANDARD);
         node0c.addNeighbor(node1c);
         node1c.addNeighbor(node0c);
         node0c.addNeighbor(node2c);
         node2c.addNeighbor(node0c);
         node0c.setFold(Node.NULL_NODE);
         node1c.setFold(node2c);
         node0c.setSurrogateFold(node1c);
         node1c.setInverseSurrogateFold(node0c);
         node2c.setFold(node1c);
         node2c.addDownPointer(node1c);
         node1c.addUpPointer(node2c);
         
         //size 4
         node0d.setState(State.STANDARD);
         node1d.setState(State.STANDARD);
         node2d.setState(State.DOWN);
         node3d.setState(State.CAP);
         node3d.setFold(node0d);
         node0d.setFold(node3d);
         node0d.addNeighbor(node1d);
         node1d.addNeighbor(node0d);
         node0d.addNeighbor(node2d);
         node2d.addNeighbor(node0d);
         node1d.addNeighbor(node3d);
         node2d.addNeighbor(node3d);
         node3d.addNeighbor(node2d);
         node3d.addNeighbor(node1d);
         node1d.setFold(node2d);
         node2d.setFold(node1d);
         
         //size 5
         node0e.setState(State.STANDARD);
         node1e.setState(State.STANDARD);
         node2e.setState(State.DOWN);
         node3e.setState(State.CAP);
         node4e.setState(State.DOWN);
         node0e.addNeighbor(node1e);
         node1e.addNeighbor(node0e);
         node0e.addNeighbor(node2e);
         node2e.addNeighbor(node0e);
         node1e.addNeighbor(node3e);
         node2e.addNeighbor(node3e);
         node3e.addNeighbor(node2e);
         node3e.addNeighbor(node1e);
         node0e.addNeighbor(node4e);
         node0e.setFold(Node.NULL_NODE);
         node0e.setSurrogateFold(node3e);
         node3e.setInverseSurrogateFold(node0e);
         node3e.setFold(node4e);
         node4e.addNeighbor(node0e);
         node4e.setFold(node3e);
         node1e.setFold(node2e);
         node2e.setFold(node1e);
         node4e.addDownPointer(node1e);
         node4e.addDownPointer(node2e);
         node1e.addUpPointer(node4e);
         node2e.addUpPointer(node4e);
     }
     
     public void testCompareTo() {
         assertTrue(node0e.compareTo(node1e) < 0);
         assertTrue(node1e.compareTo(node0e) > 0);
     }
     
     public void testGetNeighborsIds()
     {
         Node node0 = new Node(0);
         int sum = 0;
         for (int i = 1; i < 500; i++)
         {
             node0.addNeighbor(new Node(i));
             sum += i;
         }
         HashSet<Integer> ids = node0.getNeighborsIds();
         for (Integer id : ids)
         {
             sum = sum - id;
         }
         assertTrue(sum == 0);
     }
     public void testGetSurNeighborsIds()
     {
         Node node0 = new Node(0);
         int sum = 0;
         for (int i = 1; i < 500; i++)
         {
             node0.addUpPointer(new Node(i));
             sum += i;
         }
         HashSet<Integer> ids = node0.getInvSurNeighborsIds();
         for (Integer id : ids)
         {
             sum = sum - id;
         }
         assertTrue(sum == 0);
     }
     public void testGetInvSurNeighborsIds()
     {
         Node node0 = new Node(0);
         int sum = 0;
         for (int i = 1; i < 500; i++)
         {
             node0.addDownPointer(new Node(i));
             sum += i;
         }
         HashSet<Integer> ids = node0.getSurNeighborsIds();
         for (Integer id : ids)
         {
             sum = sum - id;
         }
         assertTrue(sum == 0);
     }
     
     public void testAddNeighbor()
     {
         Node node0 = new Node(0);
         node0.addNeighbor(null);
         assertTrue(node0.getNeighborsIds().isEmpty());
         node0.addNeighbor(node0);
         assertTrue(node0.getNeighborsIds().isEmpty());
         
         Node node1 = new Node(1);
         node0.addNeighbor(node1);
         assertTrue(node0.getNeighborsIds().size() == 1);
         
         //Try a large number
         for (int i = 2; i < 500; i++)
         {
             node0.addNeighbor(new Node(i));
             assertTrue(node0.getNeighborsIds().size() == i);
         }
     }
     
     public void testRemoveNeighbor()
     {
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         node0.removeNeighbor(null);
         assertTrue(node0.getNeighborsIds().isEmpty());
         node0.removeNeighbor(node1);
         assertTrue(node0.getNeighborsIds().isEmpty());
         node0.addNeighbor(node1);
         node0.removeNeighbor(node1);
         assertTrue(node0.getNeighborsIds().isEmpty());
         
         
         //Try a large number
         for (int i = 1; i < 500; i++)
         {
             node0.addNeighbor(new Node(i));
         }
         for (int i = 499; i < 0; i++)
         {
             node0.removeNeighbor(new Node(i));
             assertTrue(node0.getNeighborsIds().size() == i-1);
         }
         
      }
 
     public void testAddUpPointer()
     {
         Node node0 = new Node(0);
         node0.addUpPointer(null);
         assertTrue(node0.getInvSurNeighborsIds().isEmpty());
         node0.addUpPointer(node0);
         assertTrue(node0.getInvSurNeighborsIds().isEmpty());
         
         Node node1 = new Node(1);
         node0.addUpPointer(node1);
         assertTrue(node0.getInvSurNeighborsIds().size() == 1);
         
         //Try a large number
         for (int i = 2; i < 500; i++)
         {
             node0.addUpPointer(new Node(i));
             assertTrue(node0.getInvSurNeighborsIds().size() == i);
         }
     }
     
     public void testRemoveUpPointer()
     {
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         node0.removeUpPointer(null);
         assertTrue(node0.getInvSurNeighborsIds().isEmpty());
         node0.removeUpPointer(node1);
         assertTrue(node0.getInvSurNeighborsIds().isEmpty());
         node0.addUpPointer(node1);
         node0.removeUpPointer(node1);
         assertTrue(node0.getInvSurNeighborsIds().isEmpty());
         
         
         //Try a large number
         for (int i = 1; i < 500; i++)
         {
             node0.addUpPointer(new Node(i));
         }
         for (int i = 499; i < 0; i++)
         {
             node0.removeUpPointer(new Node(i));
             assertTrue(node0.getInvSurNeighborsIds().size() == i-1);
         }
      }
     
     public void testAddDownPointer() 
     {
         Node node0 = new Node(0);
         node0.addDownPointer(null);
         assertTrue(node0.getSurNeighborsIds().isEmpty());
         node0.addDownPointer(node0);
         assertTrue(node0.getSurNeighborsIds().isEmpty());
         
         Node node1 = new Node(1);
         node0.addDownPointer(node1);
         assertTrue(node0.getSurNeighborsIds().size() == 1);
         
         //Try a large number
         for (int i = 2; i < 500; i++)
         {
             node0.addDownPointer(new Node(i));
             assertTrue(node0.getSurNeighborsIds().size() == i);
         }
      }
      
      public void testRemoveDownPointer()
      {
          Node node0 = new Node(0);
          Node node1 = new Node(1);
          node0.removeDownPointer(null);
          assertTrue(node0.getSurNeighborsIds().isEmpty());
          node0.removeDownPointer(node1);
          assertTrue(node0.getSurNeighborsIds().isEmpty());
          node0.addDownPointer(node1);
          node0.removeDownPointer(node1);
          assertTrue(node0.getSurNeighborsIds().isEmpty());
          
          
          //Try a large number
          for (int i = 1; i < 500; i++)
          {
              node0.addDownPointer(new Node(i));
          }
          for (int i = 499; i < 0; i++)
          {
              node0.removeDownPointer(new Node(i));
              assertTrue(node0.getSurNeighborsIds().size() == i-1);
          }
       }
     
     
     public void  testConstructSimplifiedNodeDomain() 
     {
         SimplifiedNodeDomain snd = node0a.constructSimplifiedNodeDomain();
         ExpectedResult expectedResult = new ExpectedResult(1, 0);
         assertTrue(snd.equals(expectedResult));
         
         snd = node0b.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(2, 0);
         assertTrue(snd.equals(expectedResult));
         snd = node1b.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(2, 1);
         assertTrue(snd.equals(expectedResult));
         
         snd = node0c.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(3,0);
         assertTrue(snd.equals(expectedResult));
         snd = node1c.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(3,1);
         assertTrue(snd.equals(expectedResult));
         snd = node2c.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(3,2);
         assertTrue(snd.equals(expectedResult));
         
         snd = node0d.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(4,0);
         assertTrue(snd.equals(expectedResult));
         snd = node2d.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(4,2);
         assertTrue(snd.equals(expectedResult));
         snd = node3d.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(4,3);
         assertTrue(snd.equals(expectedResult));
         
         snd = node0e.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(5,0);
         assertTrue(snd.equals(expectedResult));
         snd = node2e.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(5,2);
         assertTrue(snd.equals(expectedResult));
         snd = node3e.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(5,3);
         assertTrue(snd.equals(expectedResult));
         snd = node4e.constructSimplifiedNodeDomain();
         expectedResult = new ExpectedResult(5,4);
         assertTrue(snd.equals(expectedResult));
     }
     
     
     
     public void testInsertSelf()
     {
         for (int size = 1; size <= 32; size++)
         {
             for (int startNodeId = 0; startNodeId < size - 1; startNodeId++)
             {
                 Map<Integer, Node> nodeById = new HashMap<Integer, Node>();
                 Node node0 = new Node(0);
                 node0.setFold(node0);
                 node0.setState(State.CAP);
                 createHyPeerWebWith(node0, nodeById, size - 1);
 
                 Node node = new Node(0);
                 Node startNode = nodeById.get(startNodeId);
                 
                 node.insertSelf(startNode);
                 nodeById.put(size-1, node);
                 
                 for (int i = 0; i < size; i++)
                 {
                     Node nodei = nodeById.get(i);
                     SimplifiedNodeDomain simplifiedNodeDomain = nodei.constructSimplifiedNodeDomain();
                     ExpectedResult expectedResult = new ExpectedResult(size, i);
 
                     assertTrue(simplifiedNodeDomain.equals(expectedResult));
                 }
             }
         }
     } 
     
     public void testFindCapNode()
     {
         /*
          * 
          * A hypeerweb is boundless, and so has no upper bound.
          * It's lower bound is one node and this is the lowest we can test.
          * The constriuction of a very large hypeerweb is infeasible, so we simply look to cover the bounds of the equivalence classes by going to size 5
          * These being:
          * -A complete hypeerweb
          * -One node short of a complete hypeerweb
          * -One node over a complete hypeerweb
          * 
          * There are four equivalence classes for the starting locations
          * -The start point is node 0
          * -The start point is the cap node
          * -The start point is inbetween the node 0 and cap node
          * -(for incomplete) The start node is above the cap node (on the edge)
          */
         
         //size 1
         assertTrue(node0a.findCapNode(node0a).getWebId() == 0);
         
         //size 2
         
         assertTrue(node0b.findCapNode(node0b).getWebId() == 1);
         assertTrue(node0b.findCapNode(node1b).getWebId() == 1);
         
         //size 3
         
         assertTrue(node0c.findCapNode(node0c).getWebId() == 1);
         assertTrue(node0c.findCapNode(node1c).getWebId() == 1);
         assertTrue(node0c.findCapNode(node2c).getWebId() == 1);
         
         //size 4
         
         assertTrue(node0d.findCapNode(node0d).getWebId() == 3);
         assertTrue(node0d.findCapNode(node3d).getWebId() == 3);
         assertTrue(node0d.findCapNode(node2d).getWebId() == 3);
         
         //size 5
         
         assertTrue(node0e.findCapNode(node0e).getWebId() == 3);
         assertTrue(node0e.findCapNode(node3e).getWebId() == 3);
         assertTrue(node0e.findCapNode(node2e).getWebId() == 3);
         assertTrue(node0e.findCapNode(node4e).getWebId() == 3);
     }
     
    
     public void testRemoveFromHyPeerWeb()
     {
         //mostly copied from Exp.java
         for (int size = 1; size <= 32; size++)
         {
             
             for (int removeNodeId = 0; removeNodeId < size; removeNodeId++)
             {
                 Map<Integer, Node> nodeById = new HashMap<Integer, Node>();
                 Node node0 = new Node(0);
                 node0.setFold(node0);
                 node0.setState(State.CAP);
                 
                 createHyPeerWebWith(node0, nodeById, size);
 
                 Node removeNode = nodeById.get(removeNodeId);
                 removeNode.removeFromHyPeerWeb();
 
                 for (int i = 0; i < size-1; i++)
                 {
                     Node nodei =nodeById.get(i);
                     SimplifiedNodeDomain simplifiedNodeDomain = nodei.constructSimplifiedNodeDomain();
                     ExpectedResult expectedResult = new ExpectedResult(size-1, i);
 
                     assertTrue(simplifiedNodeDomain.equals(expectedResult));
                 }
             }
         }
         
     }
      
     
     public void testGetLowestNeighborWithoutChild()
     {
         /*
          * I assume these bounds for the number of neighbors
          * Low: 1
          * High: none
          * 
          * I will use three equivelence classes
          * -No children.
          * -All neighbors have a child.
          * -Mix of neighbors having children.
          * 
          * I cannot test size 0.
          * A hypeerweb will never have duplicate nodes, so we need not test that.
          */
       /* 
         //No children
       //number of neighbors 1 (boundary)
         assertTrue(node0a.getLowestNeighborWithoutChild() == node0a);
         
         //number of neighbors 2 (boundary + 1)
         assertTrue(node1b.getLowestNeighborWithoutChild() == node0b);
         
         //number of neighbors 3 (beyond boundary)
        assertTrue(node0e.getLowestNeighborWithoutChild() == node0e);
         
         //extra test
         assertTrue(node3e.getLowestNeighborWithoutChild() == node1e); 
         
         //All with child
         //num of neighbors 1 doesn't apply to this eq class
         
         //number of neighbors 2 (boundary + 1)
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         Node node2 = new Node(2);
         Node node3 = new Node(3);
         Node node5 = new Node(5);
         node0.addNeighbor(node1);
         node0.addNeighbor(node2);
         node1.addNeighbor(node3);
         node2.addNeighbor(node5);
        assertTrue(node0.getLowestNeighborWithoutChild() == node0);
         
         //number of neighbors 3 (beyond boundary)
         Node node4 = new Node(4);
         Node node12 = new Node(12);
         node0.addNeighbor(node4);
         node4.addNeighbor(node12);
        assertTrue(node0.getLowestNeighborWithoutChild() == node0);
         
         //Mixed (real hypeerweb)
         
         //number of neighbors 1 (boundary)
         assertTrue(node0a.getLowestNeighborWithoutChild() == node0a);
         
         //number of neighbors 2 (boundary + 1)
        //assertTrue(node1b.getLowestNeighborWithoutChild() == node0b;
        assertTrue(node0b.getLowestNeighborWithoutChild() == node1b);
         
         //number of neighbors 3 (beyond boundary)
         assertTrue(node1e.getLowestNeighborWithoutChild() == node1e); 
         
         //I will now test a very large case
         Node nodeX = new Node(501);
         for (int i = 1; i < 500; i ++)
         {
         	nodeX.addNeighbor(new Node(i));
         }
         assertTrue(nodeX.getLowestNeighborWithoutChild().getWebId()==1);
        */
     }
     
     //mostly copied from Exp.java
     static private void createHyPeerWebWith(Node node0, Map<Integer, Node> reference, int numberOfNodes) {
         reference.put(0, node0);
         for (int i = 1; i < numberOfNodes; i++) {
             Node node = new Node(0);
             node.insertSelf(node0);
             reference.put(i, node);
         }
     }
 
 }
