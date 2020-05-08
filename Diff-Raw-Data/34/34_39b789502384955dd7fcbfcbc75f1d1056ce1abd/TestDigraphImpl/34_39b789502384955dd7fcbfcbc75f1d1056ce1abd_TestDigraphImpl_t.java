 package org.javaan.graph;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.mock;
 
 import java.util.Set;
 
 import org.junit.Test;
 import org.mockito.InOrder;
 
 public class TestDigraphImpl {
 	
 	@Test
 	public void testAddNode() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addNode("a");
 		assertTrue(graph.containsNode("a"));
 		
 		graph.addEdge("b", "c");
 		// relation should not be overwritten
 		graph.addNode("b");
 		Set<String> childs = graph.getChilds("b");
 		assertNotNull(childs);
 		assertEquals(1, childs.size());
 		assertTrue(childs.contains("c"));
 	}
 	
 	@Test
 	public void testAddEdge() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		
 		assertTrue(graph.containsNode("a"));
 		assertTrue(graph.containsNode("x"));
 		Set<String> childs = graph.getChilds("a");
 		assertNotNull(childs);
 		assertEquals(0, childs.size());
 		childs = graph.getChilds("x");
 		assertNotNull(childs);
 		assertEquals(1, childs.size());
 		assertTrue(childs.contains("a"));
 	}
 	
 	@Test
 	public void testGetNodes() {
 		Graph<String> graph = new DigraphImpl<String>();
 		graph.addNode("a");
 		graph.addEdge("x", "b");
 		
 		Set<String> nodes = graph.getNodes();
 		assertNotNull(nodes);
 		assertEquals(3, nodes.size());
 		assertTrue(nodes.contains("a"));
 		assertTrue(nodes.contains("b"));
 		assertTrue(nodes.contains("x"));
 	}
 
 	@Test
 	public void testGetChilds() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addNode("b");
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		
 		Set<String> childs = graph.getChilds("b");
 		assertNotNull(childs);
 		assertEquals(0, childs.size());
 		
 		childs = graph.getChilds("a");
 		assertNotNull(childs);
 		assertEquals(0, childs.size());
 		
 		childs = graph.getChilds("x");
 		assertNotNull(childs);
 		assertEquals(3, childs.size());
 		assertTrue(childs.contains("a"));
 		assertTrue(childs.contains("b"));
 		assertTrue(childs.contains("c"));
 	}
 	
 	@Test
 	public void testGetParents() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("a", "x");
 		graph.addEdge("b", "x");
 		graph.addEdge("c", "x");
 		
 		Set<String> childs = graph.getParents("a");
 		assertNotNull(childs);
 		assertEquals(0, childs.size());
 		
 		childs = graph.getParents("x");
 		assertNotNull(childs);
 		assertEquals(3, childs.size());
 		assertTrue(childs.contains("a"));
 		assertTrue(childs.contains("b"));
 		assertTrue(childs.contains("c"));
 	}
 	
 	@Test
 	public void testGetSuccessors() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("b", "c");
 		
 		Set<String> successors = graph.getSuccessors("x");
 		assertNotNull(successors);
 		assertEquals(3, successors.size());
 		assertTrue(successors.contains("a"));
 		assertTrue(successors.contains("b"));
 		assertTrue(successors.contains("c"));
 	}
 	
 	@Test
 	public void testGetPredecessors() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("b", "c");
 		graph.addEdge("d", "c");
 		
 		Set<String> predecessors = graph.getPredecessors("x");
 		assertNotNull(predecessors);
 		assertEquals(0, predecessors.size());
 		
 		predecessors = graph.getPredecessors("a");
 		assertNotNull(predecessors);
 		assertEquals(1, predecessors.size());
 		assertTrue(predecessors.contains("x"));
 		
 		predecessors = graph.getPredecessors("b");
 		assertNotNull(predecessors);
 		assertEquals(1, predecessors.size());
 		assertTrue(predecessors.contains("x"));
 		
 		predecessors = graph.getPredecessors("c");
 		assertNotNull(predecessors);
 		assertEquals(3, predecessors.size());
 		assertTrue(predecessors.contains("b"));
 		assertTrue(predecessors.contains("d"));
 		assertTrue(predecessors.contains("x"));
 	}
 
 	@Test
 	public void testHasChilds() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		
 		assertTrue(graph.hasChilds("x"));
 		assertFalse(graph.hasChilds("a"));
 	}
 	
 	@Test
 	public void testContainsNode() {
 		Graph<String> graph = new DigraphImpl<String>();
 		graph.addNode("x");
 		
 		assertTrue(graph.containsNode("x"));
 	}
 	
 	@Test
	public void testGetLeafNodes() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		
		Set<String> leaveNodes = graph.getLeafNodes("x");
 		assertNotNull(leaveNodes);
 		assertEquals(4, leaveNodes.size());
 		assertTrue(leaveNodes.contains("a"));
 		assertTrue(leaveNodes.contains("b"));
 		assertTrue(leaveNodes.contains("d"));
 		assertTrue(leaveNodes.contains("f"));
		leaveNodes = graph.getLeafNodes("c");
 		assertNotNull(leaveNodes);
 		assertEquals(2, leaveNodes.size());
 		assertTrue(leaveNodes.contains("d"));
 		assertTrue(leaveNodes.contains("f"));
		leaveNodes = graph.getLeafNodes("a");
 		assertNotNull(leaveNodes);
 		assertEquals(0, leaveNodes.size());
		leaveNodes = graph.getLeafNodes("b");
 		assertNotNull(leaveNodes);
 		assertEquals(0, leaveNodes.size());
		leaveNodes = graph.getLeafNodes("e");
 		assertNotNull(leaveNodes);
 		assertEquals(1, leaveNodes.size());
 		assertTrue(leaveNodes.contains("f"));
 	}
 	
 	@Test
 	public void testGetLeaveNodesCycle() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("a", "b");
 		graph.addEdge("b", "x");
 		graph.addEdge("a", "c");
 		
		Set<String> leaveNodes = graph.getLeafNodes("x");
 		assertNotNull(leaveNodes);
 		assertEquals(1, leaveNodes.size());
 		assertTrue(leaveNodes.contains("c"));
 	}
 	
 	@Test
 	public void testTraverseSuccessorsDepthFirst() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		graph.addEdge("x", "g");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traverseSuccessorsDepthFirst("x", -1, visitor);
 		order.verify(visitor).visit("x", 0, true);
 		order.verify(visitor).visit("a", 1, false);
 		order.verify(visitor).visit("b", 1, false);
 		order.verify(visitor).visit("c", 1, true);
 		order.verify(visitor).visit("d", 2, false);
 		order.verify(visitor).visit("e", 2, true);
 		order.verify(visitor).visit("f", 3, false);
 		order.verify(visitor).visit("g", 1, false);
 		order.verifyNoMoreInteractions();
 	}
 
 	@Test
 	public void testTraverseSuccessorsDepthFirstWithDepth() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		graph.addEdge("x", "g");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traverseSuccessorsDepthFirst("x", 2, visitor);
 		order.verify(visitor).visit("x", 0, true);
 		order.verify(visitor).visit("a", 1, false);
 		order.verify(visitor).visit("b", 1, false);
 		order.verify(visitor).visit("c", 1, true);
 		order.verify(visitor).visit("d", 2, false);
 		order.verify(visitor).visit("e", 2, true);
 		order.verify(visitor).visit("g", 1, false);
 		order.verifyNoMoreInteractions();
 	}
 
 	@Test
 	public void testTraverseSuccessorsDepthFirstCycle() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("a", "b");
 		graph.addEdge("b", "x");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traverseSuccessorsDepthFirst("x", -1, visitor);
 		order.verify(visitor).visit("x", 0, true);
 		order.verify(visitor).visit("a", 1, true);
 		order.verify(visitor).visit("b", 2, true);
 		order.verifyNoMoreInteractions();
 	}
 
 	@Test
 	public void testTraverseSuccessorsBreadthFirst() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		graph.addEdge("x", "g");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traverseSuccessorsBreadthFirst("x", -1, visitor);
 		order.verify(visitor).visit("x", 0, true);
 		order.verify(visitor).visit("a", 1, false);
 		order.verify(visitor).visit("b", 1, false);
 		order.verify(visitor).visit("c", 1, true);
 		order.verify(visitor).visit("g", 1, false);
 		order.verify(visitor).visit("d", 2, false);
 		order.verify(visitor).visit("e", 2, true);
 		order.verify(visitor).visit("f", 3, false);
 		order.verifyNoMoreInteractions();
 	}
 
 
 	@Test
 	public void testTraverseSuccessorsBreadthFirstWithDepth() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traverseSuccessorsBreadthFirst("x", 2, visitor);
 		order.verify(visitor).visit("x", 0, true);
 		order.verify(visitor).visit("a", 1, false);
 		order.verify(visitor).visit("b", 1, false);
 		order.verify(visitor).visit("c", 1, true);
 		order.verify(visitor).visit("d", 2, false);
 		order.verify(visitor).visit("e", 2, true);
 		order.verifyNoMoreInteractions();
 	}
 
 	@Test
 	public void testTraverseSuccessorsBreadthFirstWithDepthZero() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traverseSuccessorsBreadthFirst("x", 0, visitor);
 		order.verify(visitor).visit("x", 0, true);
 		order.verifyNoMoreInteractions();
 	}
 	
 	@Test
 	public void testTraverseSuccessorsBreadthFirstCycle() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("a", "b");
 		graph.addEdge("b", "x");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traverseSuccessorsBreadthFirst("x", -1, visitor);
 		order.verify(visitor).visit("x", 0, true);
 		order.verify(visitor).visit("a", 1, true);
 		order.verify(visitor).visit("b", 2, true);
 		order.verifyNoMoreInteractions();
 	}
 	
 	@Test
 	public void testTraversePredecessorsBreadthFirst() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		graph.addEdge("y", "f");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traversePredecessorsBreadthFirst("f", -1, visitor);
 		order.verify(visitor).visit("f", 0, true);
 		order.verify(visitor).visit("e", 1, true);
 		order.verify(visitor).visit("y", 1, false);
 		order.verify(visitor).visit("c", 2, true);
 		order.verify(visitor).visit("x", 3, false);
 		order.verifyNoMoreInteractions();
 	}
 
 	@Test
 	public void testTraversePredecessorsDepthFirst() {
 		Digraph<String> graph = new DigraphImpl<String>();
 		graph.addEdge("x", "a");
 		graph.addEdge("x", "b");
 		graph.addEdge("x", "c");
 		graph.addEdge("c", "d");
 		graph.addEdge("c", "e");
 		graph.addEdge("e", "f");
 		graph.addEdge("y", "f");
 		Visitor<String> visitor = mock(Visitor.class);
 		InOrder order = inOrder(visitor);
 		
 		graph.traversePredecessorsDepthFirst("f", -1, visitor);
 		order.verify(visitor).visit("f", 0, true);
 		order.verify(visitor).visit("e", 1, true);
 		order.verify(visitor).visit("c", 2, true);
 		order.verify(visitor).visit("x", 3, false);
 		order.verify(visitor).visit("y", 1, false);
 		order.verifyNoMoreInteractions();
 	}
 }
