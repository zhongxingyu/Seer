 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Queue;
 import java.util.Scanner;
 import java.util.Set;
 
 public class SegmentTree implements Runnable {
 
 	private static List<Segment> segments = new ArrayList<SegmentTree.Segment>();
 	private static Segment query;
 	private static Set<Segment> ans = new HashSet<Segment>();
 
 	public SegmentTree(List<Segment> s, Segment q) {
 		segments = s;
 		query = q;
 	}
 
 	public Set<Segment> getAnswer() {
 		return ans;
 	}
 
 	static class Interval {
 		int left, right;
 
 		public Interval(int left, int right) {
 			this.left = left;
 			this.right = right;
 		}
 
 		public Interval(Segment segment) {
 			this.left = segment.x1;
 			this.right = segment.x2;
 		}
 	}
 
 	public static class Segment {
 		public final int x1, y1, x2, y2;
 
 		public Segment(int x1, int y1, int x2, int y2) {
 			if (x1 > x2) {
 				this.x1 = x2;
 				this.y1 = y2;
 				this.x2 = x1;
 				this.y2 = y1;
 			} else {
 				this.x1 = x1;
 				this.y1 = y1;
 				this.x2 = x2;
 				this.y2 = y2;
 			}
 		}
 
 		@Override
 		public String toString() {
 			return Integer.valueOf(x1).toString() + " "
 					+ Integer.valueOf(y1).toString() + " "
 					+ Integer.valueOf(x2).toString() + " "
 					+ Integer.valueOf(y2).toString();
 		}
 	}
 
 	static void reading(List<Segment> segments, String fileName) {
 		try {
 			Scanner sc = new Scanner(new File(fileName));
 			while (sc.hasNext()) {
 				segments.add(new Segment(sc.nextInt(), sc.nextInt(), sc
 						.nextInt(), sc.nextInt()));
 			}
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	static class Node {
 		Node leftChild, rightChilrden;
 		Interval interval;
 		List<Segment> canonicalSubset;
 
 		public Node(Interval interval) {
 			this.interval = interval;
 			this.canonicalSubset = new ArrayList<Segment>();
 		}
 
 		public Node(Node left, Node right) {
 			this.interval = new Interval(left.interval.left,
 					right.interval.right);
 			this.leftChild = left;
 			this.rightChilrden = right;
 			this.canonicalSubset = new ArrayList<Segment>();
 		}
 
 		public void walk() {
 			System.out.println(this + " segment " + this.canonicalSubset);
 			if (leftChild != null)
 				leftChild.walk();
 			if (rightChilrden != null)
 				rightChilrden.walk();
 		}
 
 		@Override
 		public String toString() {
 			return interval.left + " " + interval.right;
 		}
 	}
 
 	static class Tree {
 		Node root;
 
 		public Tree(List<Node> leaves) {
 			int n = leaves.size();
 			int k = (Integer.highestOneBit(n) << 1) - n;
 			Queue<Node> queue = new ArrayDeque<Node>();
 			for (int i = 0; i < n - k; i += 2) {
 				queue.add(new Node(leaves.get(i), leaves.get(i + 1)));
 			}
 			for (int i = n - k; i < n; i++) {
 				queue.add(leaves.get(i));
 			}
 			while (queue.size() > 1) {
 				queue.add(new Node(queue.poll(), queue.poll()));
 			}
 			root = queue.poll();
 		}
 
 		private boolean overlaps(Interval int1, Interval int2) {
 			if ((int1.left <= int2.left) && (int1.right >= int2.left))
 				return true;
 			if ((int1.left <= int2.right) && (int1.right >= int2.right))
 				return true;
 			if ((int2.left <= int1.left) && (int2.right >= int1.left))
 				return true;
 			return false;
 		}
 
 		public void insert(Node node, Segment segment) {
 			if (node.interval.left >= segment.x1
 					&& node.interval.right <= segment.x2) {
 				node.canonicalSubset.add(segment);
 				return;
 			}
 			if (node.leftChild != null
 					&& overlaps(node.leftChild.interval, new Interval(segment))) {
 				insert(node.leftChild, segment);
 			}
 			if (node.rightChilrden != null
 					&& overlaps(node.rightChilrden.interval, new Interval(
 							segment))) {
 				insert(node.rightChilrden, segment);
 			}
 		}
 
 		public long vectorProd(long x1, long y1, long x2, long y2, long x3,
 				long y3) {
 			return (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);
 		}
 
 		public boolean intersection(Segment seg1, Segment seg2) {
			if (!overlaps(new Interval(seg1.y1, seg1.y2), new Interval(seg2.y1, seg2.y2)))
 				return false;
 			if (vectorProd(seg1.x1, seg1.y1, seg1.x2, seg1.y2, seg2.x1, seg2.y1)
 					* vectorProd(seg1.x1, seg1.y1, seg1.x2, seg1.y2, seg2.x2,
 							seg2.y2) > 0) {
 				return false;
 			}
 			if (vectorProd(seg2.x1, seg2.y1, seg2.x2, seg2.y2, seg1.x1, seg1.y1)
 					* vectorProd(seg2.x1, seg2.y1, seg2.x2, seg2.y2, seg1.x2,
 							seg1.y2) > 0) {
 				return false;
 			}
 			return true;
 		}
 
 		public void query(Node node, Segment q, Set<Segment> segments) {
 			for (Segment seg : node.canonicalSubset) {
 				if (intersection(seg, q)) {
 					segments.add(seg);
 				}
 			}
 			if (node.leftChild != null) {
 				if (q.x1 <= node.leftChild.interval.right) {
 					query(node.leftChild, q, segments);
 				}
 			}
 			if (node.rightChilrden != null) {
 				if (q.x1 >= node.rightChilrden.interval.left) {
 					query(node.rightChilrden, q, segments);
 				}
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		reading(segments, "input.txt");
 		List<Node> leaves = new ArrayList<Node>();
 		List<Integer> endpoints = new ArrayList<Integer>();
 		for (Segment seg : segments) {
 			endpoints.add(seg.x1);
 			endpoints.add(seg.x2);
 		}
 		int[] points = new int[endpoints.size()];
 		for (int i = 0; i < endpoints.size(); i++) {
 			points[i] = endpoints.get(i);
 		}
 		Arrays.sort(points);
 		leaves.add(new Node(new Interval(Integer.MIN_VALUE, points[0])));
 		leaves.add(new Node(new Interval(points[0], points[0])));
 		for (int i = 1; i < points.length; i++) {
 			leaves.add(new Node(new Interval(points[i - 1], points[i])));
 			leaves.add(new Node(new Interval(points[i], points[i])));
 		}
 		leaves.add(new Node(new Interval(points[points.length - 1],
 				Integer.MAX_VALUE)));
 		Tree tree = new Tree(leaves);
 		for (Segment seg : segments) {
 			tree.insert(tree.root, seg);
 		}
		query = new Segment(143, 40, 143, 162);
 		tree.query(tree.root, query, ans);
 		for (Segment seg : ans) {
 			System.err.println(seg);
 		}
 	}
 
 	@Override
 	public void run() {
 		// List<Segment> segments = new ArrayList<Segment>();
 		// reading(segments, "input.txt");
 		List<Node> leaves = new ArrayList<Node>();
 		List<Integer> endpoints = new ArrayList<Integer>();
 		for (Segment seg : segments) {
 			endpoints.add(seg.x1);
 			endpoints.add(seg.x2);
 		}
 		int[] points = new int[endpoints.size()];
 		for (int i = 0; i < endpoints.size(); i++) {
 			points[i] = endpoints.get(i);
 		}
 		Arrays.sort(points);
 		leaves.add(new Node(new Interval(Integer.MIN_VALUE, points[0])));
 		leaves.add(new Node(new Interval(points[0], points[0])));
 		for (int i = 1; i < points.length; i++) {
 			leaves.add(new Node(new Interval(points[i - 1], points[i])));
 			leaves.add(new Node(new Interval(points[i], points[i])));
 		}
 		leaves.add(new Node(new Interval(points[points.length - 1],
 				Integer.MAX_VALUE)));
 		Tree tree = new Tree(leaves);
 		for (Segment seg : segments) {
 			tree.insert(tree.root, seg);
 		}
 		// Segment query = new Segment(1, 2, 1, 6); // initialize!
 		// Set<Segment> ans = new HashSet<Segment>();
 		tree.query(tree.root, query, ans);
 		for (Segment seg : ans) {
 			System.err.println(seg);
 		}
 	}
 }
