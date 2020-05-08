 package se.chalmers.datastrukt.lab2;
 
 import testSortCol.CollectionWithGet;
 import datastructures.BinarySearchTree;
 
 /**
  * A class containing a splay-tree. Whenever an entry is collected from 
  * the splay tree it is put on top of the splay tree. This is done by
  * so called splaying where the structure in sequences until the entry
  * is put at the root location, aka the top of the splay-tree.
  * @author Tomas Sellden, Anton Palmqvist group 36
  *
  * @param <E> The entrys of the splay tree
  */
 public class SplayTree<E extends Comparable<? super E>> extends
 BinarySearchTree<E> implements CollectionWithGet<E> {
 
 	/* Rotera 1 steg i hgervarv, dvs 
     x'                 y'
    / \                / \
   y'  C   -->        A   x'
  / \                    / \  
 A   B                  B   C
 	 */
 	
 	/**
 	 * If the sequence with the wanted entry is structured with a parent
 	 * having the entry as its left-wise child the entry is put at the 
 	 * top getting its former parent as its new rightwise-child. Also 
 	 * the entrys former rightwise-child-element is put as its 
 	 * former parent's leftwise-child- element.
 	 * @param x the wanted entry
 	 */
 	private void zag(Entry x) {
 		Entry y = x.left;
 		E temp = x.element;
 		
 		x.element = y.element;
 		y.element = temp;
 		x.left = y.left;
 		if (x.left != null) {
 			x.left.parent = x;
 		}
 		y.left = y.right;
 		y.right = x.right;
 		if (y.right != null) {
 			y.right.parent = y;
 		}
 		x.right = y;
 
 	} // rotateRight
 
 	/* Rotera 1 steg i vnstervarv, dvs 
     x'                 y'
    / \                / \
   A   y'  -->        x'  C
      / \            / \  
     B   C          A   B   
 	 */
 	
 	/**
 	 * If the sequence with the wanted entry is structured with a parent
 	 * having the entry as its right-wise child the entry is put at the 
 	 * top getting its former parent as its new leftwise-child. Also the
 	 * entry's former leftwise-child-element is put as its former parent's
 	 * rightwise-child-element.
 	 * @param x the wanted entry
 	 */
 	private void zig(Entry x) {
 		Entry y = x.right;
 		E temp = x.element;
		if(x==null)
			System.out.println("X IS NULL");
		if(y==null)
			System.out.println("Y IS NULL");
 		x.element = y.element;
 		y.element = temp;
 		x.right = y.right;
 		if (x.right != null) {
 			x.right.parent = x;
 		}
 		y.right = y.left;
 		y.left = x.left;
 		if (y.left != null) {
 			y.left.parent = y;
 		}
 		x.left = y;
 
 	} // rotateLeft
 
 	/* Rotera 2 steg i hgervarv, dvs 
     x'                  z'
    / \                /   \
   y'  D   -->        y'    x'
  / \                / \   / \
 A   z'             A   B C   D
    / \  
   B   C  
 	 */
 	
 	/**
 	 * If the sequence with the wanted entry is structured as having a 
 	 * leftwise parent who in turn has a rightwise parent the entry is 
 	 * put on top getting its former parent as its leftwise child and 
 	 * its former grandparent as its rightwise child. The child-elements 
 	 * are structured as shown in the picture above.
 	 * @param x the wanted entry
 	 */
 	private void zagZig(Entry x) {
 		Entry y = x.left, z = x.left.right;
 		E e = x.element;
 		x.element = z.element;
 		z.element = e;
 		y.right = z.left;
 		if (y.right != null) {
 			y.right.parent = y;
 		}
 		z.left = z.right;
 		z.right = x.right;
 		if (z.right != null) {
 			z.right.parent = z;
 		}
 		x.right = z;
 		z.parent = x;
 
 	} // doubleRotateRight
 
 	/* Rotera 2 steg i vnstervarv, dvs 
     x'                  z'
    / \                /   \
   A   y'   -->       x'    y'
      / \            / \   / \
     z   D          A   B C   D
    / \  
   B   C  
 	 */
 	/**
 	 * If the sequence with the wanted entry is structured as having a 
 	 * rightwise parent who in turn has a leftwise parent the entry is 
 	 * put on top getting its former parent as its rightwise child and 
 	 * its former grandparent as its leftwise child. The child-elements 
 	 * are structured as shown in the picture above.
 	 * @param x the wanted entry
 	 */
 	private void zigZag(Entry x) {
 		Entry y = x.right, z = x.right.left;
 		E e = x.element;
 		x.element = z.element;
 		z.element = e;
 		y.left = z.right;
 		if (y.left != null) {
 			y.left.parent = y;
 		}
 		z.right = z.left;
 		z.left = x.left;
 
 		if (z.left != null) {
 			z.left.parent = z;
 		}
 		x.left = z;
 		z.parent = x;
 
 	} // doubleRotateLeft
 
 
 	/* 
         x' 								z'
        / \							   / \
       a	  y'						  y'  d
          / \         --->			 / \ 
         b   z'						x'  c
            / \					   / \
           c   d					  a   b
 	 */
 	
 	/**
 	 * If the sequence with the wanted element is structured as having a 
 	 * leftwise-parent who in turn has a leftwise-parent, the entry is
 	 * put as having its former parent as a leftwise-child who in turn gets
 	 * its former parent as its leftwise-child. The child element of the 
 	 * three entrys are being structured as shown in the picture above
 	 * @param x the wanted entry
 	 */
 	private void zigZig( Entry x) {
 		Entry y = x.right, z = x.right.right;
 		E e = x.element;
 		x.element = z.element;
 		z.element = e;
 		x.right = z.right;
 		if (x.right != null) {
 			x.right.parent = x;
 		}
 		y.right = z.left;
 		if (y.right != null) {
 			y.right.parent = y;
 		}
 		z.right = y.left;
 		if (z.right != null) {
 			z.right.parent = z;
 		}
 		z.left = x.left;
 		if (z.left != null) {
 			z.left.parent = z;
 		}
 		x.left = y;
 		y.left = z;
 
 	}
 	/* 
         	x'				z'
            / \			   / \
           y'  d			  a   y'
          / \			     / \
         z'  c				b   x'
        / \					   / \
       a   b					  c   d
 
 	 */
 	
 	/**
 	 * If the sequence with the wanted element is structured as having a 
 	 * rightwise-parent who in turn has a rightwise-parent, the entry is
 	 * put as having its former parent as a rightwise-child who in turn gets
 	 * its former parent as its rightwise-child. The child element of the 
 	 * three entrys are being structured as shown in the picture above
 	 * @param x the wanted entry
 	 */
 	private void zagZag( Entry x) {
 		Entry y = x.left, z = x.left.left;
 		E tmp = x.element;
 		x.element = z.element;
 		z.element = tmp;
 		x.left = z.left;
 		if (x.left != null) {
 			x.left.parent = x;
 		}
 		y.left = z.right;
 		if (y.left != null) {
 			y.left.parent = y;
 		}
 		z.left = y.right;
 		if (z.left != null) {
 			z.left.parent = z;
 		}
 		z.right = x.right;
 		if (z.right != null) {
 			z.right.parent = z;
 		}
 		y.right = z;
 		x.right = y;
 
 	}
 
 	/**
 	 * Search for the element e from the entry root, if the method finds the
 	 * element e it will return the element if it doesnt find the element it
 	 * will invoke the method splay with the "parent" entry that has the 
 	 * closest element as e
 	 * 
 	 * @param e
 	 *            the element we search for in the SplayTree
 	 * @param entry
 	 *            will always be the root of the tree
 	 * @return the entry if it find it in the SplayTree else it will 
 	 * return null
 	 */
 	public Entry search(E e, Entry entry) {
 		Entry previous = null;
 		Entry current = entry;
 		int jfr = e.compareTo(current.element);
 		while (jfr != 0) {
 			if (jfr > 0) {
 				previous = current;
 				current = current.right;
 			} else {
 				previous = current;
 				current = current.left;
 			}
 
 			if (current == null ) {
 				splay(previous);
 				return null;
 			}
 			jfr = e.compareTo(current.element);
 		}
 		return current;
 	}
 
 	/**
 	 * Searching where in the splaytree the entry exists, depending on where in
 	 * the tree it will invoke 6 different methods to "splay" the tree so it
 	 * will be more effective (take less operations) to find an element next
 	 * time we search for another element. It will do this as long as the entry
 	 * isnt the the root of the Splay tree.
 	 * 
 	 * @param entry
 	 *            the entry we will splay to it reach the root
 	 * @return the same entry as we invoked the methods with
 	 */
 	public Entry splay(Entry entry) {
 		while (entry.parent != null) {
 			if (entry.parent.parent == null) {
 				Entry parent = entry.parent;
 				if (parent.left != null && parent.left.equals(entry)) {
 					zag(entry.parent);
 				} else  {
 					zig(entry.parent);
 				}
 				entry = entry.parent;
				System.out.println("splayTest2");
 				return entry;
 			} else {
 				Entry grandParent = entry.parent.parent;
 				if (grandParent.left != null && grandParent.left.left != null
 						&& grandParent.left.left == entry) {
 					zagZag(grandParent);
 					entry= grandParent;
 				} else if (grandParent.right != null
 						&& grandParent.right.left != null
 						&& grandParent.right.left == entry) {
 					zigZag(grandParent);
 					entry = entry.parent;
 				} else if (grandParent.left != null
 						&& grandParent.left.right != null
 						&& grandParent.left.right == entry) {
 					zagZig(grandParent);
 					entry = entry.parent;
 				} else if (grandParent.right != null
 						&& grandParent.right.right != null
 						&& grandParent.right.right == entry) {
 					zigZig(grandParent);
 					entry = grandParent;
 				}
 			}
 		}
 		return entry;
 	}
 	@Override
 	public E get(E e) {
 		if (e == null || root == null) {
 			return null;
 		}
 
 		if ( e == root.element) {
 			return e;
 		}
 
 		Entry entry = search(e, root);
 		if (entry == null) {
 			return null;
 		}
 		entry = splay(entry);
 
 		return entry.element;
 	}
 }
