 package util;
 
 import java.util.*;
 
 public class QuadTree<T> implements Iterable<Map.Entry<Position, T>> {
 	private Node root;
 
 	private static class Node {
 		final int x, y;
 		final Node[] children = new Node[4];
 		// Mmm .. I can't really see any good reason why Java doesn't allow generic arrays. Maybe I'm not thinking hard enough.
 		final Object val;
 
 		Node(int px, int py, Object v){
 			x = px; y = py; val = v;
 		}
 
 		public String toString(){
 			return "{p: (" + x + ", " + y + "), v: " + val + ", children: " + children + "}";
 		}
 	}
 
 	private class QuadTreeIterator implements Iterable<Map.Entry<Position, T>>, Iterator<Map.Entry<Position, T>>{
 		private final Stack<Node> stack = new Stack<Node>();
 		private Node offer;
 		private boolean offerReady;
 		private final boolean all;
 		private final int minx, miny, maxx, maxy;
 
 		// Not sure if I like this `final` system .. seems too restrictive
 		private QuadTreeIterator(int ix, int iy, int ax, int ay, boolean a){
 			minx = ix; miny = iy; maxx = ax; maxy = ay;
 			all = a;
 			if(root != null)
 				stack.push(root);
 		}
 
 		public QuadTreeIterator(){
 			this(0, 0, 0, 0, true);
 		}
 
 		public QuadTreeIterator(int ix, int iy, int ax, int ay){
 			this(ix, iy, ax, ay, false);
 		}
 
 		// Wonder why they don't make all iterators iterable, so you can use the `for(.. : ..)` loop on iterator objects.
 		public Iterator<Map.Entry<Position, T>> iterator(){
 			return this;
 		}
 
 		public boolean hasNext(){
 			if(!offerReady)
 				if(all)
 					readyNextAll();
 				else
 					readyNextRect();
 			return offer != null;
 		}
 
 		@SuppressWarnings("unchecked")
 		public Map.Entry<Position, T> next(){
 			if(!hasNext())
 				throw new NoSuchElementException();
 			Map.Entry<Position, T> r = new AbstractMap.SimpleEntry<Position, T>(new Position(offer.x, offer.y), (T)offer.val);
 			offerReady = false;
 			return r;
 		}
 
 		public void remove(){
 			throw new UnsupportedOperationException();
 		}
 
 		private void readyNextRect(){
 			offerReady = true;
 			do{
 				offer = null;
 				if(stack.empty())
 					break;
 				offer = stack.pop();
 				if(minx < offer.x){
 					if(miny < offer.y && offer.children[2] != null)
 						stack.push(offer.children[2]);
 					if(maxy >= offer.y && offer.children[0] != null)
 						stack.push(offer.children[0]);
 				}
 				if(maxx >= offer.x){
 					if(miny < offer.y && offer.children[3] != null)
 						stack.push(offer.children[3]);
 					if(maxy >= offer.y && offer.children[1] != null)
 						stack.push(offer.children[1]);
 				}
 			}while(!acceptable(offer.x, offer.y));
 		}
 
 		private boolean acceptable(int x, int y){
 			return x >= minx && y >= miny && x <= maxx && y <= maxy;
 		}
 
 		private void readyNextAll(){
 			offerReady = true;
 			if(stack.empty())
 				offer = null;
 			else{
 				offer = stack.pop();
 				for(Node n : offer.children)
 					if(n != null)
 						stack.push(n);
 			}
 		}
 	};
 
 	public void put(Position p, T v){
 		put(p.x(), p.y(), v);
 	}
 
 	public void put(int x, int y, T v){
 		put(x, y, null, -1, root, new Node(x, y, v));
 	}
 
 	private void put(int x, int y, Node from, int fromo, Node n, Node v){
 		if(n == null){
 			if(from == null)
 				root = v;
 			else
 				from.children[fromo] = v;
 		}else{
 			int i;
 			if(x < n.x)
 				i = y < n.y? 2 : 0;
 			else
 				i = y < n.y? 3 : 1;
 			put(x, y, n, i, n.children[i], v);
 		}
 	}
 
 	public boolean remove(Position p, T v){
 		return remove(p.x(), p.y(), v);
 	}
 
 	public boolean remove(int x, int y, T v){
 		return remove(x, y, null, -1, root, v);
 	}
 
 	private boolean remove(int x, int y, Node from, int fromo, Node n, T v){
 		if(n == null)
 			return false;
 		if(x == n.x && y == n.y && (v == null || v.equals(n.val))){
			if(from == null)
 				root = null;
 			else
 				from.children[fromo] = null;
 			tmpIterateAdd(n);
 			return true;
 		}
 		int i;
 		if(x < n.x)
 			i = y < n.y? 2 : 0;
 		else
 			i = y < n.y? 3 : 1;
 		return remove(x, y, n, i, n.children[i], v);
 	}
 
 	// until I can figure out proper deletion
 	@SuppressWarnings("unchecked")
 	private void tmpIterateAdd(Node n){
 		for(Node m : n.children)
 			if(m != null){
				put(n.x, n.y, (T)n.val);
 				tmpIterateAdd(m);
 			}
 	}
 
 	public Iterable<Map.Entry<Position, T>> portion(Position min, Position max){
 		return new QuadTreeIterator(min.x(), min.y(), max.x(), max.y());
 	}
 	
 	public Iterator<Map.Entry<Position, T>> iterator(){
 		return new QuadTreeIterator();
 	}
 
 	public String toString(){
 		return "QuadTree(" + root + ")";
 	}
 
 	public static void main(String[] argv){
 		QuadTree<Integer> qt = new QuadTree<Integer>();
 		Set<Map.Entry<Position, Integer>> test = new HashSet<Map.Entry<Position, Integer>>();
 		Set<Map.Entry<Position, Integer>> sq = new HashSet<Map.Entry<Position, Integer>>(), sm = new HashSet<Map.Entry<Position, Integer>>();
 		Set<Map.Entry<Position, Integer>> lf = new HashSet<Map.Entry<Position, Integer>>();
 		for(int x = 0; x < 10000; x++){
 			Position p = new Position((int)(Math.random()*100 - 50), (int)(Math.random()*100 - 50));
 			int r = (int)(Math.random()*50);
 			Map.Entry<Position, Integer> se = new AbstractMap.SimpleEntry<Position, Integer>(p, r);
 			test.add(se);
 			qt.put(p, r);
 			if(p.x() >= -3 && p.x() <= 17 && p.y() >= 5 && p.y() <= 20)
 				lf.add(se);
 		}
 		sm.addAll(test);
 		for(Map.Entry<Position, Integer> kv : qt)
 			sq.add(kv);
 		System.out.println("sm = " + sm);
 		System.out.println("sq = " + sq);
 		System.out.println("sm " + (sm.equals(sq)? "=" : "!") + "= sq");
 		/*
 		if(!sm.equals(sq)){
 			Set<Map.Entry<Position, Integer>> ss = new HashSet<Map.Entry<Position, Integer>>(sm);
 			ss.retainAll(sq);
 			sm.removeAll(ss);
 			sq.removeAll(ss);
 			System.out.println("sm\\sq = " + sm);
 			System.out.println("sq\\sm = " + sq);
 		}
 		*/
 		Set<Map.Entry<Position, Integer>> qf = new HashSet<Map.Entry<Position, Integer>>();
 		for(Map.Entry<Position, Integer> kv : qt.portion(new Position(-3, 5), new Position(17, 20)))
 			qf.add(kv);
 		System.out.println("qt.port " + (qf.equals(lf)? "=" : "!") + "= sq");
 		if(!qf.equals(lf)){
 			Set<Map.Entry<Position, Integer>> ss = new HashSet<Map.Entry<Position, Integer>>(lf);
 			ss.removeAll(qf);
 			System.out.println("port\\qt.port = " + ss);
 		}
 	}
 }
