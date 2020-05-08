 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /** Splay Tree Test
  * @author Jared Moore
  * @version Feb 11, 2013
  */
 public class SplayTreeTest {
 
 	// Simple case tests, make sure these work first
 	@Test
 	public void testZigRightSimple() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50);
 		tree.add(25);
 
 		assertEquals(25, (int) tree.getRoot().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 	}
 
 
 	@Test
 	public void testZigLeftSimple() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(25);
 		tree.add(50);
 
 		assertEquals(50, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 	}
 
 	@Test
 	public void testZigZigRightSimple() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50);
 		tree.add(75);
 		tree.add(25);
 
 		assertEquals(25, (int) tree.getRoot().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getRight().getData());
 	}
 
 	@Test
 	public void testZigZigLeftSimple() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50);
 		tree.add(25);
 		tree.add(75);
 
 		assertEquals(75, (int) tree.getRoot().getData());
 		assertEquals(50, (int) tree.getRoot().getLeft().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getLeft().getData());
 	}
 
 	@Test
 	public void testZigZagRightSimple() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50);
 		tree.add(75);
 		tree.add(60);
 
 		assertEquals(60, (int) tree.getRoot().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getData());
 		assertEquals(50, (int) tree.getRoot().getLeft().getData());
 	}
 
 	@Test
 	public void testZigZagLeftSimple() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50);
 		tree.add(25);
 		tree.add(37);
 
 		assertEquals(37, (int) tree.getRoot().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 	}
 
 	// More complicated tests, make sure all of the above tests work before looking at these
 	@Test
 	public void testZigRightWithChildren() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50); // build a pretty tree
 		tree.add(25);
 		tree.add(37);
 		tree.add(30);
 		tree.add(40);
 		assertEquals(30, (int) tree.get(30)); // make the zig call on the item to the left of the root
 
 		assertEquals(30, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(40, (int) tree.getRoot().getRight().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getRight().getData());
 		assertEquals(37, (int) tree.getRoot().getRight().getLeft().getData());
 	}
 
 	@Test
 	public void testZigLeftWithChildren() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50); // build a pretty tree
 		tree.add(25);
 		tree.add(37);
 		tree.add(40);
 		tree.add(30);
 		tree.get(40); // make the zig call on the item to the right of the root
 
 		assertEquals(40, (int) tree.getRoot().getData());
 		assertEquals(30, (int) tree.getRoot().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(37, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getLeft().getData());
 	}
 
 	@Test
 	public void testZigZigRightWithChildren() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50); // build a pretty tree
 		tree.add(25);
 		tree.add(37);
 		tree.add(30);
 		tree.add(40);
 		tree.add(45);
 		assertEquals(30, (int) tree.get(30)); // make the zig-zig call on the left side
 
 		assertEquals(30, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(40, (int) tree.getRoot().getRight().getData());
 		assertEquals(45, (int) tree.getRoot().getRight().getRight().getData());
 		assertEquals(37, (int) tree.getRoot().getRight().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getRight().getRight().getData());
 	}
 
 	@Test
 	public void testZigZigLeftWithChildren() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 
 		tree.add(50); // build a pretty tree
 		tree.add(25);
 		tree.add(37);
 		tree.add(40);
 		tree.add(30);
 		tree.add(27);
 		tree.get(40); // make the zig-zig call on the right side
 
 		assertEquals(40, (int) tree.getRoot().getData());
 		assertEquals(30, (int) tree.getRoot().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(37, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(27, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getLeft().getLeft().getData());
 	}
 
 	/** Begin tests made by @author Adam Yost
 	 */
 	@Test
 	public void testIsEmpty() {
 		SplayTree<String> tree = new SplayTree<String>();
 		assertEquals(true, tree.isEmpty());
 		tree.add("1");
 		assertEquals(false, tree.isEmpty());
 		tree.add(null);
 		assertEquals(false, tree.isEmpty());
 		tree.remove(null);
 		tree.remove("1");
 		assertTrue(tree.isEmpty());
 	}
 
 	@Test (timeout=500)
 	public void TestRemove() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 		tree.add(50); // build a pretty tree
 		tree.add(25);
 		tree.add(37);
 		tree.add(30);
 		tree.add(40);
 		tree.remove(37);
 		assertEquals(30, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(40, (int) tree.getRoot().getRight().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getRight().getData());
 		tree.add(10);
 		tree.remove(40);
 		assertEquals(30, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(10, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 	}
 	@Test (timeout = 1000)
 	public void TestNulls(){
 		SplayTree<String> tree = new SplayTree<String>();
 		tree.add("5");
 		tree.add("2");
 		tree.add("9");
 		tree.add(null);
 		assertEquals(null, (String) tree.getRoot().getData());
 		assertTrue(tree.contains(null));
 		assertEquals(null, tree.get(null));
 		tree.remove(null);
 		assertEquals("9", (String) tree.getRoot().getData());
 	}
 
 	/** End tests by @author Adam Yost
 	 * Begin tests by @author Steven Han
 	 */
 	@Test (timeout = 1000)
 	public void StevenTest1(){
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 		for (int i = 0; i < 50; i++){
 			tree.add(i);
 			assertEquals((Integer) i, tree.getRoot().getData());
 		}
 	}
 
 	@Test (timeout = 1000)
 	public void StevenTest2(){
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 		for (int i = 0; i < 50; i++)
 			tree.add(i);
 
 		for (int i = 0; i < 50; i++){
 			tree.remove(i);
 			assertEquals((Integer)(49 - i ), (Integer)tree.size());
 		}
 	}
 
 	@Test (timeout = 1000)
 	public void StevenTest3(){
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 		for (int i = 0; i < 50; i++)
 			tree.add(i);
 
 		for (int i = 0; i < 50; i++){
 			assertEquals((Integer)tree.get(i), (Integer)i);
 			assertEquals((Integer)tree.getRoot().getData(), (Integer)i);
 		}
 	}
 
 	@Test (timeout = 1000)
 	public void StevenTest4(){
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 		for (int i = 0; i < 50; i++)
 			tree.add(i);
 
 		for (int i = -50; i < 100; i++){
 
 			if (i >= 0 && i < 50){
 				assertEquals((Integer)tree.get(i), (Integer)i);
 				assertEquals((Integer)tree.getRoot().getData(), (Integer)i);
 			} else {
 				assertEquals((Integer)tree.get(i), (Integer)null);
 			}
 		}
 	}
 
 	@Test (timeout = 1000)
 	public void StevenTest5(){
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 		tree.add(1);
 		tree.add(2);
 		tree.add(-10);
 		tree.add(33);
 		tree.add(2345);
 		tree.add(-123);
 		tree.add(0);
 		tree.add(null);
 		tree.add(3049);
 		tree.add(129736591);
 		tree.add(-23483124);
 		tree.add(23511235);
 		tree.add(12351235);
 		tree.add(123513);
 		tree.add(44);
 		tree.add(-1);
 		tree.add(-2);
 
 		assertEquals(tree.contains(null), true);
 		assertEquals(tree.contains(314), false);
 		assertEquals(tree.contains(-1235), false);
 		assertEquals(tree.contains(34), false);
 		assertEquals(tree.contains(355), false);
 		assertEquals(tree.contains(44), true);
 		assertEquals(tree.contains(0), true);
 		assertEquals(tree.contains(-2), true);
 		assertEquals(tree.contains(-1), true);
 	}
 }
