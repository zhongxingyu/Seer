 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 /** Test case for AVL tree
  * @author Jared Moore
  * @version Jan 31, 2013
  */
 public class AVLTest {
 
 	public AVL<Integer> buildTree() {
 
 		ArrayList<Integer> array = new ArrayList<Integer>();
 		array.add(50);
 		array.add(25);
 		array.add(75);
 		array.add(13);
 		array.add(87);
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.addAll(array); // note that my addAll will call add
 		return tree;
 	}
 
 	/**
 	 * Test method for {@link AVL#add(java.lang.Comparable)}.
 	 */
 	@Test
 	public void testRightHeavy() {
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(25);
 		tree.add(13);
 		tree.add(50);
 		tree.add(37);
 		tree.add(75);
 		tree.add(90);
 		assertEquals(50, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(37, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(90, (int) tree.getRoot().getRight().getRight().getData());
 	}
 	
 	@Test
 	public void testLeftHeavy() {
 		
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(75);
 		tree.add(90);
 		tree.add(50);
		tree.add(60);
 		tree.add(25);
 		tree.add(13);
 		assertEquals(50, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
		assertEquals(60, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(90, (int) tree.getRoot().getRight().getRight().getData());
 	}
 	
 	@Test
 	public void testLeftRightHeavy() {
 		
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(50);
 		tree.add(75);
 		tree.add(25);
 		tree.add(13);
 		tree.add(37);
 		tree.add(30);
 		assertEquals(37, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(30, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getRight().getData());
 	}
 	
 	@Test
 	public void testRightLeftHeavy() {
 		
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(25); 
 		tree.add(13);
 		tree.add(50);
 		tree.add(75);
 		tree.add(37);
 		tree.add(30);
 		assertEquals(37, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(30, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getRight().getData());
 	}
 
 	/**
 	 * Test method for {@link AVL#remove(java.lang.Comparable)}.
 	 */
 	@Test
 	public void testRemove() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link AVL#contains(java.lang.Comparable)}.
 	 */
 	@Test
 	public void testContains() {
 		AVL<Integer> tree = buildTree();
 		assertEquals(true, tree.contains(50));
 		assertEquals(false, tree.contains(100));
 		assertEquals(false, tree.contains(null));
 		tree.add(null);
 		assertEquals(true, tree.contains(null));
 		assertEquals(true, tree.contains(13));
 	}
 
 }
