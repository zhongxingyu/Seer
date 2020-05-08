 package org.lttng.studio.tests.zgraph;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Test;
 import org.lttng.studio.model.zgraph.Dot;
 import org.lttng.studio.model.zgraph.Graph;
 import org.lttng.studio.model.zgraph.LinkType;
 import org.lttng.studio.model.zgraph.Node;
 import org.lttng.studio.model.zgraph.Operations;
 import org.lttng.studio.model.zgraph.Ops;
 
 public class TestGraph {
 
 	private static Object A = "A";
 	private static Object B = "B";
 
 	public static void writeString(Object writer, String fname, String content) {
 		String folder = writer.getClass().getName();
 		try {
 			File dir = new File("results", folder);
 			dir.mkdirs();
 			File fout = new File(dir, fname);
 			FileWriter fwriter = new FileWriter(fout);
 			fwriter.write(content);
 			fwriter.flush();
 			fwriter.close();
 			System.out.println("wrote " + fout);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testCreateGraph() {
 		Graph g = new Graph();
 		assertEquals(0, g.getNodesOf(A).size());
 	}
 
 	@Test
 	public void testPutNode() {
 		Graph g = new Graph();
 		g.replace(A, new Node(0));
 		g.replace(A, new Node(1));
 		assertEquals(1, g.getNodesOf(A).size());
 	}
 
 	@Test
 	public void testAppendNode() {
 		Graph g = new Graph();
 		int max = 10;
 		for(int i = 0; i < max; i++)
 			g.append(A, new Node(i));
 		List<Node> list = g.getNodesOf(A);
 		assertEquals(max, list.size());
 		checkLinkHorizontal(list);
 	}
 
 	private void checkLinkHorizontal(List<Node> list) {
 		if (list.isEmpty())
 			return;
 		for (int i = 0; i < list.size() - 1; i++) {
 			Node n0 = list.get(i);
 			Node n1 = list.get(i+1);
 			assertEquals(n0.right(), n1);
 			assertEquals(n1.left(), n0);
 			assertEquals(n0.links[Node.RIGHT].from, n0);
 			assertEquals(n1.links[Node.LEFT].to, n1);
 		}
 	}
 
 	@Test
 	public void testIllegalNode() {
 		Graph g = new Graph();
 		g.append(A, new Node(1));
 		Exception exception = null;
 		try {
 			g.append(A, new Node(0));
 		} catch (IllegalArgumentException e) {
 			exception = e;
 		}
 		assertNotNull(exception);
 	}
 
 	@Test
 	public void testDot() {
 		int max = 10;
 		Graph g = new Graph();
 		for (int i = 0; i < max; i++) {
 			g.append(A, new Node(i));
 			g.append(B, new Node(i));
 		}
 		List<Node> la = g.getNodesOf(A);
 		List<Node> lb = g.getNodesOf(B);
 		la.get(0).linkVertical(la.get(2));
 		la.get(1).linkVertical(lb.get(1));
 		lb.get(5).linkVertical(la.get(6));
 		String dot = Dot.todot(g);
 		writeString(this, "full.dot", dot);
 		List<Object> list = new LinkedList<Object>();
 		list.add(A);
 		dot = Dot.todot(g, list);
 		writeString(this, "partial.dot", dot);
 	}
 
 	@Test
 	public void testMakeGraphBasic() {
 		Node head = Ops.basic(10, LinkType.DEFAULT);
 		Graph g = Ops.toGraph(head);
 		String content = Dot.todot(g);
 		writeString(this, "basic.dot", content);
 		assertEquals(Ops.size(head), g.size());
 	}
 
 	@Test
 	public void testOffset() {
 		Node head = Ops.basic(10, LinkType.DEFAULT);
 		Ops.offset(head, 100);
 		Graph g = Ops.toGraph(head);
 		String content = Dot.todot(g);
 		writeString(this, "offset.dot", content);
 	}
 
 	@Test
 	public void testClone1() {
 		Node head = Ops.basic(10, LinkType.DEFAULT);
 		Node clone = Ops.clone(head);
 		assertEquals(Ops.size(head), Ops.size(clone));
 	}
 
 	@Test
 	public void testConcat() {
 		Node n1 = Ops.basic(1, LinkType.DEFAULT);
 		Node n2 = Ops.basic(1, LinkType.DEFAULT);
 		Node head = Ops.concat(n1, n2);
 		Graph g = Ops.toGraph(head);
 		String content = Dot.todot(g);
 		writeString(this, "concat.dot", content);
 		assertEquals(Ops.size(head), Ops.size(n1) + Ops.size(n2) + 2);
 	}
 
 	@Test
 	public void testIter() {
 		Node n = Ops.basic(10, LinkType.DEFAULT);
 		Node head = Ops.iter(n, 1);
		System.out.println(Ops.debug(head));
 		Graph g = Ops.toGraph(head);
 		String content = Dot.todot(g);
 		writeString(this, "iter.dot", content);
		assertEquals(Ops.size(head), 5);
 	}
 
 	@Test
 	public void testCriticalPath1() {
 		Graph g = new Graph();
 		for (int i = 0; i < 10; i++) {
 			g.append(A, new Node(i * 10));
 		}
 		Graph path = Operations.criticalPath(g, A, 25, 75);
 		String content = Dot.todot(g);
 		writeString(this, "task1_full.dot", content);
 		content = Dot.todot(path);
 		writeString(this, "task1_A.dot", content);
 		assertEquals(7, path.getNodesOf(A).size());
 	}
 
 }
