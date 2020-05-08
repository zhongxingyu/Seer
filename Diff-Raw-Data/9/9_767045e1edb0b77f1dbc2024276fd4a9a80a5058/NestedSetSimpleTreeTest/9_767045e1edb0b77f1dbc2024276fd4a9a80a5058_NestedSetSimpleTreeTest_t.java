 package com.faraya.legioss.core.ns;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.FixMethodOrder;
 import org.junit.runners.MethodSorters;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.faraya.legioss.core.BasePersitenceTest;
 import com.faraya.legioss.core.dao.ns.INestedSetDAO;
 import com.faraya.legioss.core.entity.ns.Node;
 import java.util.List;
 
 /**
  * User: fabrizzio
  * Date: 6/8/13
  * Time: 10:48 PM
  */
 
 /**
  * This is considered a bad practice, but I found my self in a situation where I needed to guarantee the execution order of the test methods
  * Second methods relies on the commits done on the first, third method relies on the commits accomplished by the second and so on.
  *
  * When adding new methods to this test case, keep in mind stiking to the naming convention
  * if Last method was "thirdTestSomething"
  * The new one should start with "fourthSomethingDescriptive"
  *
  * @See http://howtodoinjava.com/2012/11/24/ordered-testcases-execution-in-junit-4/
  */
 @FixMethodOrder(MethodSorters.NAME_ASCENDING)
 public class NestedSetSimpleTreeTest extends BasePersitenceTest {
 
     Logger logger = LoggerFactory.getLogger(NestedSetSimpleTreeTest.class);
 
     @Autowired
     INestedSetDAO dao;
 
     /**
      * Commit won't be reflected until test method is executed without exceptions
      */
     @Test
     public void firstAddChildrenAndRootGrandChildTest() throws Exception{
 
         assertNotNull("null", dao);
         Node root = new Node("root");
         dao.add(root);
         assertEquals(root.countChildren(),0);
         printTree();
         assertOnlyHasRootState(root);
 
         Node child1 = new Node("child-1");
         dao.add(child1,root);
         assertEquals(root.countChildren(),1);
         printTree();
         assertRootHasOneChildState(root);
 
         Node child2 = new Node("child-2");
         dao.add(child2,root);
        assertEquals(root.countChildren(), 2);
         printTree();
         assertRootHasTwoChildrenState(root);
 
         Node child3 = new Node("child-3");
         dao.add(child3,child1); // Here we add the grand child
 
         printTree();
     }
 
     /**
      * Commit won't be reflected until test method is executed without exceptions
      */
 
     @Test
     public void secondSimpleDeleteTest(){
         //printTree();
         //System.out.println(dao.listAll());
         Node root = dao.findRoot();
         assertNotNull(" Root was not found! ",root);
         Node child1 = dao.findByName("child-1");
         assertNotNull(child1);
 
         Node child3 = dao.findByName("child-3");
         assertNotNull(child3);
         assertRootFirstGrandChildState(child1,child3);
 
         dao.delete(child3);
         printTree();
     }
 
     /**
      * Commit won't be reflected until test method is executed without exceptions
      */
     @Test
     public void thirdTestDeleteEffect(){
         //printTree();
         Node root = dao.findRoot();
         assertRootHasTwoChildrenState(root);
         printTree();
     }
 
     /**
      *
      * @param root
      */
     public void assertOnlyHasRootState(Node root){
         assertNotNull(root.getId());
         assertEquals("Root's left must always be 1 ",1,(int)root.getLeft());
         assertEquals("Root's right must be 2 when first added ",2,(int)root.getRight());
         assertNull("initially, root does not have right most node",dao.getRightMostNode(root));
     }
 
     /**
      *
      * @param root
      */
     public void assertRootHasOneChildState(Node root){
 
         Node rm1 = dao.getRightMostNode(root);
 
         assertNotNull("right most node must not be null this time",rm1);
         assertEquals("right most is the only child owned by root ",2L,(long)rm1.getId());
 
         List<Node> onlyChild = dao.getTree(root.getId());
         assertEquals("must have 1 child",1,onlyChild.size());
         Node c1 = onlyChild.get(0);
 
         assertNode(c1,2,3);
 
         //Expected is
         // [Node{ id=1, name='root', left=1, right=4},
         //    Node{ id=2, name='child-1', left=2, right=3}
         // ]
     }
 
     /**
      *
      * @param root
      */
     public void assertRootHasTwoChildrenState(Node root){
         Node rm2 = dao.getRightMostNode(root);
         assertNotNull("right most node must not be null this time",rm2);
         assertEquals("right most is the only child owned by root ",3L,(long)rm2.getId());
         List<Node> subTree1 = dao.getTree(root.getId());
         assertNotNull(subTree1);
         assertEquals("must have 2 children", 2, subTree1.size());
 
         Node c2 = subTree1.get(0);
         assertNode(c2,2,3);
 
         Node c3 = subTree1.get(1);
         assertNode(c3,4,5);
     }
 
     /**
      *
      * @param child1
      * @param grandChild
      */
     public void assertRootFirstGrandChildState(Node child1,Node grandChild){
         Node rm3 = dao.getRightMostNode(child1);
         assertNotNull(rm3);
         assertEquals("right most node is child-3", grandChild.getId(), rm3.getId());
         List<Node> subTree2 = dao.getTree(child1.getId());
         assertEquals("must have 1 child", 1, subTree2.size());
         Node c4 = subTree2.get(0);
         assertEquals("Must match.. ",rm3,c4);
         //Expected is
         // [Node{ id=1, name='root', left=1, right=8},
         //    Node{ id=2, name='child-1', left=2, right=5}, Node{ id=3, name='child-2', left=5, right=6}
         //      Node{ id=4, name='child-3 (grandChild)', left=3, right=4}
         // ]
 
     }
 
     private void assertNode(Node node,int left, int right){
         assertEquals("",left, (int)node.getLeft());
         assertEquals("",right,(int)node.getRight());
     }
 
     private void printTree(){
         Node root = dao.findRoot();
         assertNotNull("root was not expected to be null",root);
         List<Node> subTree = dao.getTree(root.getId());
         subTree.add(0,root);
         logger.info(subTree.toString());
     }
 
 }
