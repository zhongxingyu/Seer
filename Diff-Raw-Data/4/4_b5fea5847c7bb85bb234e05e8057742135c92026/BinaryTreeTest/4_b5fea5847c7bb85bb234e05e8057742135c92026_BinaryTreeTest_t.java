 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 
 public class BinaryTreeTest {
 
 	
 
 	@Test
 	public void testGetValue() {
 		
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		assertEquals(5, tree.getValue().intValue());
 	}
 
 	@Test
 	public void testAddValueT() {
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		tree.addValue(4);
 		tree.addValue(3);
 		assertTrue(tree.contains(4));
 
 		assertTrue(tree.contains(3));
 		
 		assertTrue(tree.contains(5));;
 	}
 
 	@Test
 	public void testAddValueListOfT() {
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		List<Integer> values = Arrays.asList(1,2,3,6,7,8,9);
 		tree.addValue(values);
 		for(int value:values)
 			assertTrue(tree.contains(value));
 	}
 
 	@Test
 	public void testRemove() {
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		List<Integer> values = Arrays.asList(1,2,3,6,7,8,9,3,6,12,3,5,1);
 		tree.addValue(values);
 		assertTrue(tree.contains(7));
 		System.out.println(tree);
 		tree.remove(7);
 		assertTrue(!tree.contains(7));
 		System.out.println(tree);
 	}
 
 	@Test
 	public void testGetDepth() {
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		assertEquals(1, tree.getDepth());
 		tree.addValue(4);
 		assertEquals(2, tree.getDepth());
 		tree.addValue(6);
 		assertEquals(2, tree.getDepth());
 		tree.addValue(9);
 		assertEquals(3, tree.getDepth());
 		
 		
 		
 	}
 
 	@Test
 	public void testGetSize() {
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		assertEquals(1, tree.getSize());
 		tree.addValue(123);
 		assertEquals(2, tree.getSize());
 		List<Integer> values = Arrays.asList(1,2,3,6,7,8,9);
 		tree.addValue(values);
 		assertEquals(9, tree.getSize());
 	}
 
 	@Test
 	public void testGetMinTree() {
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		
 		assertEquals(tree, tree.getMinTree());
 		
 		tree.addValue(6);
 		assertEquals(6, tree.getMinTree().getValue().intValue());
 		tree.addValue(4);
 		assertEquals(6, tree.getMinTree().getValue().intValue());
		tree.addValue(7);
		assertEquals(7, tree.getMinTree().getValue().intValue());
 	}
 
 	@Test
 	public void testContains() {
 		BinaryTree<Integer> tree = new BinaryTree<Integer>(5);
 		tree.addValue(4);
 		tree.addValue(3);
 		assertTrue(tree.contains(4));
 
 		assertTrue(tree.contains(3));
 		
 		assertTrue(tree.contains(5));;
 		assertTrue(!tree.contains(1));
 	}
 
 }
