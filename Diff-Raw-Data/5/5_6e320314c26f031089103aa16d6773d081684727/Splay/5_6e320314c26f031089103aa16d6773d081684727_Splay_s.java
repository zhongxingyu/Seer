 package com.algorithms.splay;
 
 public class Splay {
 
 	private Node root;
 	
 	public Splay() {
 		root = new Node(0);
 	}
 	
 	public void insert(int value) {
 		root = insert(value, root);
 	}
 	
 	private Node insert(int value, Node node) {
 		if(node.getValue() == value) {
 			splay(node);
 			return node;
 		} else if (value < node.getValue()) {
 			if(node.getLeft() != null) {
				return find(value, node.getLeft());
 			}
 			else {
 				node.setLeft(new Node(value));
 				return node.getLeft();
 			}
 		} else {
 			if(node.getRight() != null) {
				return find(value, node.getRight());
 			}
 			else {
 				node.setRight(new Node(value));
 				return node.getRight();
 			}
 		}
 	}
 	
 	public Node find(int value) {
 		root = find(value, root);
 		return root;
 	}
 	
 	private Node find(int value, Node node) {
 		if(node.getValue() == value) {
 			splay(node);
 			return node;
 		} else if (value < node.getValue()) {
 			if(node.getLeft() != null) {
 				return find(value, node.getLeft());
 			}
 		} else if (value > node.getValue()) {
 			if(node.getRight() != null) {
 				return find(value, node.getRight());
 			}
 		}
 		return null;
 	}
 	
 	private void splay(Node node) {
 		while(!node.isRoot()) {
 			if(node.getParent().isRoot()) {
 				zig(node);
 			}
 			else if(node.getParent().isLeft() && node.isLeft()) {
 				zigZig(node);
 			}
 			else if(node.getParent().isRight() && node.isRight()) {
 				zigZig(node);
 			}
 			else {
 				zigZag(node);
 			}
 			
 		}
 	}
 	
 	private void zig(Node node) {
 		Node parent = node.getParent();
 		node.setParent(null);
 //		ROOT = node;
 		if(node.isLeft()) {
 			parent.setLeft(node.getRight());
 			node.setRight(parent);			
 		}
 		else {
 			parent.setRight(node.getLeft());
 			node.setLeft(parent);
 		}
 	}
 	
 	private void zigZig(Node node) {
 		Node parent = node.getParent();
 		Node grandparent = parent.getParent();
 		if(grandparent.isRoot()) {
 			node.setParent(null);
 //			ROOT = node;
 		}
 		if(parent.isLeft()) {
 			grandparent.setLeft(parent.getRight());
 			parent.setLeft(node.getRight());
 			parent.setRight(grandparent);
 			node.setRight(parent);
 		}
 		else {
 			grandparent.setRight(parent.getLeft());
 			parent.setRight(node.getLeft());
 			parent.setLeft(grandparent);
 			node.setLeft(parent);
 		}
 	}
 	
 	private void zigZag(Node node) {
 		Node parent = node.getParent();
 		Node grandparent = parent.getParent();
 		if(grandparent.isRoot()) {
 			node.setParent(null);
 //			ROOT = node;
 		}
 		if(parent.isLeft()) {
 			grandparent.setLeft(node.getRight());
 			parent.setRight(node.getLeft());
 			node.setRight(grandparent);
 			node.setLeft(parent);			
 		}
 		else {
 			grandparent.setRight(node.getLeft());
 			parent.setLeft(node.getRight());
 			node.setLeft(grandparent);
 			node.setRight(parent);
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Splay splay = new Splay();
 		for (int i = 1; i < 100; i++) {
 			splay.insert(i);
 		}
 		for (int i = 100; i >= 0; i--) {
 			Node node = splay.find(i);
 			System.out.println(node.getValue());
 		}
 		
 	}
 
 }
