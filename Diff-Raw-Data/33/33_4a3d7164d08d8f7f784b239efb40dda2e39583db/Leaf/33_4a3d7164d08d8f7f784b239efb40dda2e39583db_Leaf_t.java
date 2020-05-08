 package com.gmail.seizans.btree;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 final class Leaf<E extends Comparable<E>> extends AbsNode<E> {
 
 	Leaf(int deg) {
 		if (deg < 2) {
 			throw new IllegalArgumentException("Degree must be greater than or equal to 2.");
 		}
 		this.deg = deg;
 		this.keys = new ArrayList<E>(deg * 2 - 1);
 		this.children = null;
 	}
 
 	@Override
 	boolean search(E e) {
 		return keys.indexOf(e) != -1;
 	}
 
 	@Override
 	void insert(E e) {
 		if (keys.indexOf(e) != -1) {
 			throw new RuntimeException("The element already exists.");
 		}
 		int idx = childIndexOf(e);
 		keys.add(idx, e);
 	}
 
 	@Override
 	void delete(E e) {
 		int index = keys.indexOf(e);
 		if (index == -1) {
 			throw new IllegalArgumentException("The element does not exist.");
 		}
 		keys.remove(index);
 	}
 
 	@Override
 	Tuple2<E, AbsNode<E>> split() {
 		Leaf<E> newLeaf = new Leaf<E>(deg);
 		for (int i = 0; i < deg - 1; i++) {
 			newLeaf.keys.add(keys.get(deg + i));
 		}
 
 		for (int i = 0; i < deg - 1; i++) {
 			keys.remove(deg * 2 - 2 - i);
 		}
 
 		E center = keys.get(deg - 1);
 		keys.remove(deg - 1);
 		return new Tuple2<E, AbsNode<E>>(center, newLeaf);
 	}
 
 	@Override
 	E maxKey() {
 		return keys.get(keys.size() - 1);
 	}
 
 	@Override
 	E minKey() {
 		return keys.get(0);
 	}
 
 	@Override
 	void addRight(E key, AbsNode<E> right) {
 		keys.add(key);
 		keys.addAll(right.keys);
 	}
 
 	@Override
 	void pushRight(E key, AbsNode<E> right) {
 		keys.add(key);
 	}
 
 	@Override
 	void pushLeft(E key, AbsNode<E> left) {
 		keys.add(0, key);
 //		left.keys.add(key);
 //		left.keys.addAll(keys);
 //		keys.clear();
 //		keys.addAll(left.keys);
 	}
 
 	@Override
 	Tuple2<E, AbsNode<E>> popRight() {
 		E key = keys.get(keys.size() - 1);
 		keys.remove(keys.size() - 1);
 		return new Tuple2<E, AbsNode<E>>(key, null);
 	}
 
 	@Override
 	Tuple2<E, AbsNode<E>> popLeft() {
 		E key = keys.get(0);
 		keys.remove(0);
 		return new Tuple2<E, AbsNode<E>>(key, null);
 	}
 
	@Override
 	AbsNode<E> rootSplit() {
 		Node<E> root = new Node<E>(deg);
 		Leaf<E> lhs = new Leaf<E>(deg);
 		Leaf<E> rhs = new Leaf<E>(deg);
 		for (int i = 0; i < deg - 1; i++) {
 			lhs.keys.add(keys.get(i));
 			rhs.keys.add(keys.get(deg + i));
 		}
 		root.keys.add(keys.get(deg - 1));
 		root.children.add(lhs);
 		root.children.add(rhs);
 		return root;
 	}
 
 	@Override
 	void pushForIterator(Stack<Object> stack) {
 		for (int i = 0; i < keys.size(); i++) {
 			stack.push(keys.get(keys.size() - 1 - i));
 		}
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		for (E e: keys) {
 			sb.append("(" + e + ")");
 		}
 		return sb.toString();
 	}
 }
