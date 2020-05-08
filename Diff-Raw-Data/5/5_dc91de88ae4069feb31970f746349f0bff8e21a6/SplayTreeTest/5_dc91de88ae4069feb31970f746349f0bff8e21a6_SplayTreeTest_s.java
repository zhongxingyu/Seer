 import static org.junit.Assert.*;
 
 import org.junit.Test;
 

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
 
 	@Test
 	public void testIsEmpty() {
 		SplayTree<Integer> tree = new SplayTree<Integer>();
 		assertEquals(true, tree.isEmpty());
 		tree.add(1);
 		assertEquals(false, tree.isEmpty());
 		tree = new SplayTree<Integer>();
 		tree.add(null);
 		assertEquals(false, tree.isEmpty());
 	}
 
 }
