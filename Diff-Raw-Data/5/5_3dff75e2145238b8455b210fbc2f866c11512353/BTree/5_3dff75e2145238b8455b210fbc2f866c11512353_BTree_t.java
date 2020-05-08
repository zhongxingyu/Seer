 import java.io.*;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 
 public class BTree<T extends Comparable<T>> {
 	
 	private int degree;
 	private BTreeNode<T> root;
 
 	public BTree(int degree) {
 		
 		this.degree = degree;
 		
 		if (degree < 1) {
 			
 			System.err.println("Invalid degree. Must be a positive integer.");
 			System.exit(3);
 		}	
 		
 		this.root = new BTreeNode<T>();
 	}
 	
 	public BTree(String bTreeFile) {
 		
 		try {
 			
 			FileInputStream fis = new FileInputStream(bTreeFile);
 			DataInputStream dis = new DataInputStream(fis);
 			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
 			
 			//@TODO Do something
 			/*
 			
 			*/
 			
 			dis.close();
 		} catch (FileNotFoundException e) {
 			
 		} catch (IOException e) {
 			
 			
 		}
 	}
 	
 	public void insert(T key){
 		BTreeNode<T> r = root;
 		if (r.isFull()) {
 			BTreeNode<T> s = new BTreeNode<T>();
 			this.root = s;
 			
 			s.setChild(0, r);
 			s.splitChild(0);
 			s.insert(key);
 		} else {
 			r.insert(key);
 		}
 	}
 	
 	private class BTreeNode<T extends Comparable<T>> {
 		private boolean leaf;
 		private long key;
 		/**
 		 * Number of keys.
 		 */
 		private int n;
 		private ArrayList<TreeObject<T>> keys;
 		private ArrayList<BTreeNode<T>> children; 
 		
 		@SuppressWarnings("unchecked")
 		public BTreeNode(){
 			this.leaf = true;
 			
 			this.n = 0;
 			
 			this.keys = new ArrayList<TreeObject<T>>(degree * 2 - 1);
 			this.children = new ArrayList<BTreeNode<T>>(degree * 2);
 			
 			this.key = 0;
 		}
 		
 		public void setChild(int index, BTreeNode<T> node){
 			children.set(index, node);
 		}
 		
 		public BTreeNode<T> getChild(int index){
 			return children.get(index);
 		}
 		
 		public BTreeNode<T> removeChild(int index){
 			BTreeNode<T> c = children.get(index);
 			children.remove(index);
 			return c;
 		}
 		
 		public void splitChild(int index){
 			BTreeNode<T> z = new BTreeNode<T>();
 			BTreeNode<T> y = getChild(index);
 			
 			z.isLeaf(y.isLeaf());
 			z.n(y.n());
 			
 			for (int j = 0; j < degree - 1; j++) {
 				z.setKey(j, y.removeKey(j+degree));
 			}
 			
 			if (!y.isLeaf()) {
 				for (int j = 0; j < degree; j++) {
 					z.setChild(j, y.removeChild(j+degree));
 				}
 			}
 			y.n(degree - 1);
 			
 			for (int j = this.n() + 1; j > index + 1; j--) {
 				this.setChild(j+1, this.removeChild(j));
 			}
 			this.setChild(index+1, z);
 			
 			for (int j = this.n(); j > index; j--){
 				this.setKey(j+1,this.removeKey(j));
 			}
 			this.setKey(index, y.removeKey(degree));
 			this.n = this.n + 1;
 			
 			y.save();
 			z.save();
 			this.save();
 		}
 
 		public void setKey(int index, TreeObject<T> obj){
 			keys.set(index, obj);
 		}
 		
 		public TreeObject<T> getKey(int index){
 			return keys.get(index);
 		}
 		
 		public TreeObject<T> removeKey(int index){
 			TreeObject<T> k = keys.get(index);
 			keys.remove(index);
 			return k;
 		}
 		
 		/**
 		 * Inserted key assuming node is not full.
 		 * @param key key to insert
 		 */
 		public void insert(T key){
 			int i = this.n - 1;
 			if (this.isLeaf()) {
 				while (i >= 0 && key.compareTo(this.getKey(i).key()) < 0 ) {
 					this.setKey(i+1, this.removeKey(i));
 					i--;
 				}
 				
 				// If key already exists in tree, increment frequency
 				if (key.compareTo(this.getKey(i).key()) == 0) {
 					this.getKey(i).incrementFrequency();
 				} else {
 					this.setKey(i+1,new TreeObject<T>(key));
 					this.n = this.n + 1;
 				}
 				
 				this.save();
 			} else {
				while(i >= 0 && key.compareTo(this.getKey(i).key()) < 0 ) {
 					i--;
 				}
				i++;
 				this.getChild(i).load();
 				if (this.getChild(i).isFull()) {
 					this.splitChild(i);
 					if (key.compareTo(this.getKey(i).key()) > 0) {
 						i++;
 					}
 				}
 				this.getChild(i).insert(key);
 			}
 		}
 		
 		/**
 		 * Loads node from disk.
 		 */
 		private void load() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		/**
 		 * Saves node to disk.
 		 */
 		private void save() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		/**
 		 * Returns number of contains objects
 		 * @return # of objects
 		 */
 		public int n(){
 			return n;
 		}
 		
 		public void n(int n) {
 			this.n = n;
 		}
 		
 		/**
 		 * Returns true if node is full, false otherwise.
 		 * @return 
 		 */
 		public boolean isFull(){
 			return n() == (2 * degree) - 1;
 		}
 		
 		public boolean isLeaf(){
 			return this.leaf;
 		}
 		
 		public void isLeaf(boolean leaf){
 			this.leaf = leaf;
 		}
 	}
 	
 	private class TreeObject<T extends Comparable<T>>{
 		private int frequency;
 		private T key;
 		
 		public TreeObject(T key){
 			this.key = key;
 		}
 		
 		public T key(){
 			return this.key;
 		}
 		
 		public int frequency(){
 			return this.frequency;
 		}
 		
 		public void incrementFrequency(){
 			this.frequency++;
 		}
 	}
 }
