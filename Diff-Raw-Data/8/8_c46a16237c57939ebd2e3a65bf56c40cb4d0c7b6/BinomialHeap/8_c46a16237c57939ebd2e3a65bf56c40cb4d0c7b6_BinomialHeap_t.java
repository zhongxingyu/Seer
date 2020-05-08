 package awesomeBH;
 
 /**
  * BinomialHeap
  * 
  * An implementation of binomial heap over non-negative integers.
  * 
  * Algorithms are based on:
  * Cormen, Thomas H., Charles E. Leiserson, and
  * Robert L. Rivest. Introduction to Algorithms.
  * Cambridge, MA: MIT, 2001. Print.
  * 
  * Invariant: isValid()
  * 
  * @author Amir Moualem, amirmoua@mail.tau.ac.il, ID 300170800
  * @author Sagie Maoz, sagiemao@mail.tau.ac.il, ID 021526025
  */
 public class BinomialHeap {
 	/**
 	 * Size of an empty heap
 	 */
 	private static int EMPTY_HEAP_SIZE = 0;
 
 	/**
 	 * Minimal value of an empty heap
 	 */
 	private static int EMPTY_MIN_VALUE = -1;
 
 	/**
 	 * Pointer to tree with minimal root value
 	 */
 	private HeapNode min;
 
 	/**
 	 * Pointer to first tree in a linked list of binomial trees
 	 */
 	private HeapNode first;
 
 	/**
 	 * Pointer to last tree in a linked list of binomial trees
 	 */
 	private HeapNode last;
 
 	/**
 	 * Number of elements (non-negative integers) in the heap
 	 */
 	private int size;
 
 	/**
 	 * Number of binomial trees in the heap
 	 */
 	private int treesNum;
 
 	/**
 	 * Creates a new empty binomial heap.
 	 */
 	public BinomialHeap() {
 		this(null);
 	}
 
 	/**
 	 * Creates a new binomial heap with a given first binomial tree.
 	 * 
 	 * precondition: node == null || node.validate() != null
 	 * 
 	 * @param node
 	 *            Binomial tree
 	 */
 	private BinomialHeap(HeapNode node) {
 		this.min = node;
 		this.first = node;
 		this.last = node;
 
 		// set size and trees count
 		if (node != null) {
 			this.size = node.getSize();
 			treesNum = 1;
 		} else {
 			this.size = EMPTY_HEAP_SIZE;
 			treesNum = 0;
 		}
 	}
 
 	/**
 	 * Resets the data structure to contain no data.
 	 * 
 	 * postcondition: this.empty()
 	 */
 	private void reset() {
 		min = null;
 		first = null;
 		last = null;
 		size = EMPTY_HEAP_SIZE;
 		treesNum = 0;
 	}
 
 	/**
 	 * public boolean empty()
 	 * 
 	 * precondition: none
 	 * 
 	 * The method returns true if and only if the heap is empty.
 	 * 
 	 * Time complexity: O(1)
 	 */
 	public boolean empty() {
 		return this.size == EMPTY_HEAP_SIZE;
 	}
 
 	/**
 	 * public void insert(int value)
 	 * 
 	 * Insert value into the heap
 	 * 
 	 * Time complexity: O(logn) w.c., O(1) amortized
 	 */
 	public void insert(int value) {
 		BinomialHeap heapToMeld;
 		HeapNode newHeapNode = new HeapNode(value);
 
 		if (empty()) { // first element
 			this.first = newHeapNode;
 			this.last = newHeapNode;
 			this.min = newHeapNode;
 			this.size = 1;
 
 		} else { // use meld to add element to heap
 			heapToMeld = new BinomialHeap(newHeapNode);
 			this.meld(heapToMeld);
 		}
 	}
 
 	/**
 	 * public void deleteMin()
 	 * 
 	 * Delete the minimum value
 	 * 
 	 * Time complexity: O(logn)
 	 */
 	public void deleteMin() {
 		if (empty()) {
 			return; // nothing to do
 		}
 
 		// collect all min's children to new heap
 		BinomialHeap h = new BinomialHeap();
 		HeapNode p = this.min.getLeftmostChild();
 		if (p != null) {
 			while (p != null) {
 				h.addTree(p);
 				p = p.getNext();
 			}
 		}
 
 		// remove min from trees list
 		if (this.min.getPrev() != null) {
 			this.min.getPrev().setNext(this.min.getNext());
 		}
 		if (this.min.getNext() != null) {
 			this.min.getNext().setPrev(this.min.getPrev());
 		}
 		if (this.min == this.first) {
 			this.first = this.min.getNext();
 		}
 		if (this.min == this.last) {
 			this.last = this.min.getPrev();
 		}
 
 		this.meld(h);
 	}
 
 	/**
 	 * public int findMin()
 	 * 
 	 * Return the minimum value
 	 * 
 	 * Time complexity: O(1)
 	 */
 	public int findMin() {
 		if (empty()) {
 			return EMPTY_MIN_VALUE; // nothing to find
 		} else {
 			return this.min.getValue();
 		}
 	}
 
 	/**
 	 * public void meld (BinomialHeap heap2)
 	 * 
 	 * Meld the heap with heap2
 	 * 
 	 * Time complexity: O(logn)
 	 */
 	public void meld(BinomialHeap heap2) {
 		if (heap2 == null) {
 			return; // nothing to meld
 		}
 
 		// add heap2's trees to the trees list
 		BinomialHeap h = BinomialHeap.merge(this, heap2);
 		if (h.empty()) {
 			// this.empty(), so do nothing
 
 		} else {
 			HeapNode prevX = null;
 			HeapNode x = h.getFirst();
 			HeapNode nextX = x.getNext();
 
 			// successively link nodes of same ranks
 			while (nextX != null) {
 				// different ranks, no need to link
 				if (x.getRank() != nextX.getRank()
 						|| (nextX.getNext() != null && nextX.getNext()
 								.getRank() == x.getRank())) {
 					prevX = x;
 					x = nextX;
 				} else {
 					// find the larger node to be linked under the other node
 					if (x.getValue() <= nextX.getValue()) {
 						x.setNext(nextX.getNext());
 						if (nextX.getNext() != null) {
 							nextX.getNext().setPrev(x);
 						}
 						x.linkWith(nextX);
 					} else {
 						if (prevX == null) {
 							h.setFirst(nextX);
 						} else {
 							prevX.setNext(nextX);
 							if (nextX != null) {
 								nextX.setPrev(prevX);
 							}
 						}
 						nextX.linkWith(x);
 						x = nextX;
 					}
 				}
 
 				nextX = x.getNext();
 			}
 
 			// copy the created heap trees to the trees list
 			this.first = h.first;
 		}
 
 		// fix size, trees count, min node, last
 		this.fixProperties();
 	}
 
 	/**
 	 * Fixes the heap's properties: size, treesNum, min, last.
 	 * This method should be called after each call to meld(), to
 	 * maintain the class's invariant.
 	 * 
 	 * postcondition: this.isValid()
 	 */
 	private void fixProperties() {
 		HeapNode p = this.first;
 		HeapNode last = p;
 		this.size = 0;
 		this.treesNum = 0;
 		this.min = null;
 		
 		while (p != null) {
 			this.size += p.getSize();
 			this.treesNum++;
 			if (this.min == null) {
 				this.min = p;
 			} else if (p.getValue() < this.min.getValue()) {
 				this.min = p;
 			}
 			
 			last = p;
 			p = p.getNext();
 		}
 		
 		// fix last pointer
 		this.setLast(last);
 		
 		// fix list ends pointers
 		if (this.getLast() != null) {
 			this.getLast().setNext(null);
 		}
 		if (this.getFirst() != null) {
 			this.getFirst().setPrev(null);
 		}
 	}
 
 	/**
 	 * Merges the trees lists of two binomial heaps into a new heap.
 	 * 
 	 * NOTE: The created merged heap is not necessarily valid, because it may
 	 * contain trees of the same rank.
 	 * 
 	 * @param heap1
 	 *            First heap to merge
 	 * @param heap2
 	 *            Second heap to merge
 	 * @return Merged heap
 	 */
 	public static BinomialHeap merge(BinomialHeap heap1, BinomialHeap heap2) {
 		BinomialHeap h = new BinomialHeap();
 
 		HeapNode p1 = heap1.getFirst();
 		HeapNode p2 = heap2.getFirst();
 		
 		// merge-sort two linked lists of binomial trees
 		while (p1 != null && p2 != null) {
 			if (p1.getRank() <= p2.getRank()) {
 				h.addTree(p1);
 				p1 = p1.getNext();
 			} else {
 				h.addTree(p2);
 				p2 = p2.getNext();
 			}
 		}
 
 		// concatenate the remainders
 		while (p1 != null) {
 			h.addTree(p1);
 			p1 = p1.getNext();
 		}
 		while (p2 != null) {
 			h.addTree(p2);
 			p2 = p2.getNext();
 		}
 
 		return h;
 	}
 
 	/**
 	 * Adds a single binomial tree to the heap's trees list.
 	 * 
 	 * NOTE: The heap is not necessarily valid after calling addTree. see merge
 	 * for details.
 	 * 
 	 * precondition: tree != null
 	 * 
 	 * @param tree
 	 *            Tree to add
 	 */
 	private void addTree(HeapNode tree) {
 		if (empty()) {
 			this.first = tree;
 			this.last = tree;
 		} else {
 			this.last.setNext(tree);
 			tree.setPrev(this.last);
 			this.last = tree;
 		}
 
 		this.size += tree.getSize();
 	}
 
 	/**
 	 * Returns the pointer to the first binomial tree in the heap.
 	 * 
 	 * @return Pointer to the first binomial tree, null if heap is empty.
 	 */
 	public HeapNode getFirst() {
 		return first;
 	}
 
 	/**
 	 * Sets a binomial tree as the first in the heap.
 	 * 
 	 * @param first
 	 *            Node to set.
 	 */
 	public void setFirst(HeapNode first) {
 		this.first = first;
 	}
 
 	/**
 	 * Returns the pointer to the last binomial tree in the heap.
 	 * 
 	 * @return Pointer to the last binomial tree, null if heap is empty.
 	 */
 	public HeapNode getLast() {
 		return last;
 	}
 
 	/**
 	 * Sets a binomial tree as the last in the heap.
 	 * 
 	 * @param last
 	 *            Node to set.
 	 */
 	public void setLast(HeapNode last) {
 		this.last = last;
 	}
 
 	/**
 	 * public int size()
 	 * 
 	 * Return the number of elements in the heap
 	 * 
 	 * Time complexity: O(1)
 	 */
 	public int size() {
 		return this.size;
 	}
 
 	/**
 	 * public int[] treesSize()
 	 * 
 	 * Return an array containing the sizes of the trees that represent the heap
 	 * in ascending order.
 	 * 
 	 * Time complexity: O(logn)
 	 */
 	public int[] treesSize() {
 		// collect trees sizes
 		int[] treesSize = new int[this.treesNum];
 		HeapNode current = first;
 		int i = 0;
 		while (current != null) {
 			treesSize[i++] = current.getSize();
 			current = current.getNext();
 		}
 
 		return treesSize;
 	}
 
 	/**
 	 * public void arrayToHeap()
 	 * 
	 * Insert the array to the heap. Delete previous elements in the heap.
 	 * 
 	 * Time complexity: O(array.length)
 	 */
 	public void arrayToHeap(int[] array) {
 		this.reset();
 		for (int i : array) {
 			this.insert(i);
 		}
 	}
 
 	/**
 	 * public boolean isValid()
 	 * 
 	 * Returns true if and only if the heap is valid.
 	 * 
 	 * Time complexity: O(n)
 	 */
 	public boolean isValid() {
 		if (empty()) {
 			return true; // an empty tree is a valid tree
 		}
 
 		// validate size
 		int actualTreesCount = 0;
 		HeapNode p = this.first;
 		while (p != null) {
 			actualTreesCount++;
 			p = p.getNext();
 		}
 		
 		if (actualTreesCount != this.treesNum) {
 			return false; // invalid trees count
 		}
 
 		// validate binomial trees
 		int[] actualRanks = new int[actualTreesCount];
 		int actualMin = EMPTY_MIN_VALUE;
 		ValidatedInfo validInfo;
 		int actualSize = 0;
 		p = this.first;
 		int i = 0;
 
 		while (p != null) {
 			validInfo = p.validate();
 			if (validInfo == null) {
 				return false; // invalid binomial minimal tree
 			}
 
 			// collect ranks list, total size
 			actualRanks[i++] = validInfo.rank;
 			actualSize += validInfo.size;
 
 			// collect min value
 			if (actualMin == EMPTY_MIN_VALUE || p.getValue() < actualMin) {
 				actualMin = p.getValue();
 			}
 			p = p.getNext();
 		}
 
 		// validate heap size
 		if (actualSize != this.size) {
 			return false; // invalid 'size' value
 		}
 
 		// validate minimal value
 		if (actualMin != this.min.getValue()) {
 			return false; // invalid 'min' value
 		}
 
 		// validate uniqueness of ranks
 		int j;
 		for (i = 0; i < actualTreesCount; i++) {
 			for (j = i + 1; j < actualTreesCount; j++) {
 				if (actualRanks[i] == actualRanks[j]) {
 					return false; // ranks not unique
 				}
 			}
 		}
 
 		// all's good
 		return true;
 	}
 
 	/**
 	 * Returns a string representation of the heap.
 	 */
 	@Override
 	public String toString() {
 		StringBuffer str = new StringBuffer();
 
 		HeapNode tree = this.getFirst();
 		while (tree != null) {
 			if (tree == this.min) {
 				str.append('*'); // mark min tree
 			}
 			str.append(tree.toString());
 			str.append(", ");
 			tree = tree.getNext();
 		}
 
 		return String.format("{ %s }", str);
 	}
 
 	/**
 	 * public class HeapNode
 	 * 
 	 * If you wish to implement classes other than BinomialHeap (for example
 	 * HeapNode), do it in this file, not in another file
 	 * 
 	 */
 	public class HeapNode {
 
 		/**
 		 * Node's value
 		 */
 		private int value;
 
 		/**
 		 * Node's binomial rank
 		 */
 		private int rank;
 
 		/**
 		 * Number of elements in tree
 		 */
 		private int size;
 
 		/**
 		 * Pointer to following sibling node
 		 */
 		private HeapNode next;
 
 		/**
 		 * Pointer to previous sibling node
 		 */
 		private HeapNode prev;
 
 		/**
 		 * Pointer to leftmost child node
 		 */
 		private HeapNode leftmostChild;
 		
 		/**
 		 * Pointer to rightmost child node
 		 */
 		private HeapNode rightmostChild;
 
 		/**
 		 * Instantiates a new binomial node with a given value.
 		 * 
 		 * precondition: value >= 0
 		 * 
 		 * @param value
 		 *            Value to store in node
 		 */
 		public HeapNode(int value) {
 			this.value = value;
 			this.size = 1;
 			this.rank = 0;
 		}
 
 		/**
 		 * @return Pointer to following sibling node
 		 */
 		public HeapNode getNext() {
 			return next;
 		}
 
 		/**
 		 * Sets following sibling node
 		 * 
 		 * @param next
 		 *            Pointer to new following sibling node
 		 */
 		public void setNext(HeapNode next) {
 			this.next = next;
 		}
 
 		/**
 		 * @return Pointer to previous sibling node
 		 */
 		public HeapNode getPrev() {
 			return prev;
 		}
 
 		/**
 		 * Sets previous sibling node
 		 * 
		 * @param prev
 		 *            Pointer to new previous sibling node
 		 */
 		public void setPrev(HeapNode prev) {
 			this.prev = prev;
 		}
 
 		/**
 		 * @return Pointer to leftmost child node
 		 */
 		public HeapNode getLeftmostChild() {
 			return leftmostChild;
 		}
 
 		/**
 		 * Sets leftmost child node
 		 * 
 		 * @param leftmostChild
 		 *            Pointer to new leftmost child node
 		 */
 		public void setLeftmostChild(HeapNode leftmostChild) {
 			this.leftmostChild = leftmostChild;
 		}
 
 		/**
 		 * @return Pointer to rightmost child node
 		 */
 		public HeapNode getRightmostChild() {
 			return rightmostChild;
 		}
 
 		/**
 		 * Sets rightmost child node
 		 * 
 		 * @param rightmostChild
 		 *            Pointer to new rightmost child node
 		 */
 		public void setRightmostChild(HeapNode rightmostChild) {
 			this.rightmostChild = rightmostChild;
 		}
 
 		/**
 		 * @return Binomial rank of the tree
 		 */
 		public int getRank() {
 			return this.rank;
 		}
 
 		/**
 		 * @return Number of elements in the tree
 		 */
 		public int getSize() {
 			return this.size;
 		}
 
 		/**
 		 * @return Node's value
 		 */
 		public int getValue() {
 			return this.value;
 		}
 
 		/**
 		 * Sets the node's value.
 		 * 
 		 * precondition: value >= 0
 		 * 
 		 * @param value
 		 *            new value
 		 */
 		public void setValue(int value) {
 			this.value = value;
 		}
 
 		/**
 		 * Returns the actual size and rank of the node, independently of the
 		 * other method's validity.
 		 * 
 		 * May return null if the node was found invalid, i.e.: its root value
 		 * is not the minimal value, one of its children is an invalid heap
 		 * node, or its size isn't 2^rank.
 		 * 
 		 * @return An object containing the collected actual size and rank of
 		 *         the node, null if the node was found invalid.
 		 */
 		public ValidatedInfo validate() {
 			HeapNode child = this.getLeftmostChild();
 			ValidatedInfo childResult;
 			int size = 1;
 			int rank = 0;
 
 			// traversing child nodes, validating them
 			while (child != null) {
 				if (this.getValue() > child.getValue()) {
 					return null; // root's value isn't the minimal value
 									// (minimal rule)
 				}
 
 				childResult = child.validate();
 				if (childResult == null) {
 					return null; // child node is invalid
 				} else {
 					// collect size and rank
 					size += childResult.size;
 					rank++;
 				}
 
 				child = child.getNext();
 			}
 
 			// verify size and rank relation (size == 2^rank)
 			if (size != Math.pow(2, rank)) {
 				return null; // invalid size / rank
 			}
 
 			return new ValidatedInfo(size, rank);
 		}
 
 		/**
		 * Links the current node to another node.
 		 * 
 		 * precondition: other != null
 		 * precondition: other.getValue() > this.getValue()
 		 * precondition: other.getRank() == this.getRank()
 		 * postcondition: this.getRank() = 1 + $prev(this.getRank())
 		 * postcondition: this.getRightmostChild() == other
 		 */
 		public void linkWith(HeapNode other) {
 			other.setNext(null);
 			other.setPrev(this.rightmostChild);
 			if (this.rightmostChild != null) {
 				this.rightmostChild.setNext(other);
 			} else {
 				this.leftmostChild = other;
 			}
 			this.rightmostChild = other;
 			
 
 			this.rank++;
 			this.size += other.size;
 		}
 
 		/**
 		 * Returns a string representation of the binomial tree.
 		 */
 		@Override
 		public String toString() {
 			StringBuffer children = new StringBuffer();
 
 			HeapNode child = this.getLeftmostChild();
 			while (child != null) {
 				children.append(child.toString());
 				child = child.getNext();
 			}
 
 			return String.format("[ %d %s ]", this.getValue(), children);
 		}
 	}
 
 	/**
 	 * Simple data structure representing a tree's actual size and rank
 	 * properties.
 	 */
 	private class ValidatedInfo {
 		public int size;
 		public int rank;
 
 		public ValidatedInfo(int size, int rank) {
 			this.size = size;
 			this.rank = rank;
 		}
 	}
 
 }
