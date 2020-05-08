 package practice;
 
 import java.util.ArrayList;
 
 /**
  *  Practice code.  Sorting algorithms and trees and such.
  * @author scottl
  */
 public class Practice {
     private static final int arraySize = 3;
     
     private class MyVector<T> 
     {
      private Object[] items = null;
      
      public MyVector(T item)
      {
          items = new Object[1];
          items[0] = item;
      }
      
      public MyVector()
      {
          items = new Object[0];
      }
      
      public T getItem(int index) throws ArrayIndexOutOfBoundsException
      {
          if (index > items.length - 1 || index < 0)
              throw new ArrayIndexOutOfBoundsException(index);
          return (T)items[index];
      }
      
      public T removeItem(int index) throws ArrayIndexOutOfBoundsException
      {
          Object item = getItem(index);
          
          for (int ct = index + 1; ct < items.length; ct++)
              items[ct - 1] = items[ct];
          shrinkArray();
          
          return (T)item;
      }
      
      public T getTail() throws ArrayIndexOutOfBoundsException
      {
          return getItem(items.length - 1);
      }
      
      public T getHead() throws ArrayIndexOutOfBoundsException
      {
          return getItem(0);
      }
      
      public void putItem(T item)
      {
          extendArray();
          items[items.length - 1] = item; 
      }
      
      public void printArray()
      {
          for (int ct = 0; ct < items.length; ct++)
              System.out.println("[" + ct + "]" + items[ct]);
          println();
      }
      
      private void shrinkArray()
      {
          Object[] newArray = new Object[items.length - 1];
          for (int ct = 0; ct < newArray.length; ct++)
              newArray[ct] = items[ct];
          items = newArray;
      }
      
      private void extendArray()
      {
          Object[] newArray = new Object[items.length + 1];
          for (int ct = 0; ct < items.length; ct++)
              newArray[ct] = items[ct];
          items = newArray;
      }
     }
     
     
    public class Entry<K extends Comparable,V>
     {
         K key = null;
         final V val;
         
         public Entry(K key, V val)
         {
             this.key = key;
             this.val = val;
         }
     }
     
     private class HashTable
     {
        private class HashEntry<K extends Comparable,V> extends Entry
         {
             private HashEntry next = null;
             
             public HashEntry(K key, V val)
             {super(key,val);}
             
             public long getHash()
             {          
                 long outgoing = 0;
                 if (key == null)
                     return outgoing;
 
                 String strKey = key.toString();
                 for(char c : strKey.toCharArray())
                     outgoing = outgoing*127 + c;
 
                 return outgoing;
             }
 
             public V getVal()
             {return (V)val;}
 
             @Override
             public String toString()
             {return "key: " + key + "\tval: " + val + "\thash: " + getHash();}
         }
 
         
         private int arraySize = 100;
         private HashEntry[] entries = null;
         
         
         public HashTable(int size)
         {
             arraySize = size;
             entries = new HashEntry[arraySize];
         }
         
         public void insert(HashEntry entry)
         {
             int index = (int) (entry.getHash() % arraySize);
             
             // no contention for index
             if (entries[index] == null)
             {
                 entries[index] = entry;
                 return;
             }
             
             // contention for index, add entry to chain
             HashEntry last = entries[index];
             while(last.next != null)
                 last = last.next;
             last.next = entry;
         }
         
         
         private boolean resize(int size)
         {
             arraySize = size;
             HashEntry[] oldArray = entries;
             entries = new HashEntry[arraySize];
             
             HashEntry temp = null;
             for(HashEntry entry : oldArray)
             {
                 // skip empty buckets
                 if (entry == null)
                     continue;
                 insert(entry);
             }
             
             return true;
         }
         
         public String toString()
         {
             String outgoing = "number of buckets: " + arraySize + "\n";
             int ct = 0;
             
             for(HashEntry entry : entries)
             {
                 if (entry == null)
                 {
                     ct++;
                     continue;
                 }
                 
                 outgoing += "bucket: " + ct + "\t" + entry;
                 
                 // grab all chained entries
                 if (entry.next != null)
                     while((entry = entry.next) != null)
                         outgoing += "\t" + entry;
                 
                 outgoing += "\n";
                 ct++;
             }
             
             return outgoing;
         }
         
         
         public boolean expand()
         {
             resize(arraySize*2);
             return true;
         }
         
         public boolean shrink()
         {
             resize(arraySize/2);
             return true;
         }
     }
 
     
     // Trees
     private abstract class TreeNode<T extends Comparable>
     {
         protected T item;
         protected TreeNode<T> parent;
         
         
         public TreeNode()
         {
          item = null;
          parent = null;
         }
         
         public TreeNode(T item)
         {
             this();
             this.item = item;
         }
         
         public TreeNode(T item, TreeNode parent)
         {
             this(item);
             this.parent = parent;
         }
         
         
         public void setParent(TreeNode parentNode)
         {parent = parentNode;}
     }
     
     private abstract class Tree<T extends Comparable>
     {
         protected TreeNode root;
         
 //        public abstract TreeNode insertItem(T item, TreeNode parent);
     }
     
     
     private class BinaryTreeNode<T extends Comparable> extends TreeNode
     {
         protected BinaryTreeNode left = null;
         protected BinaryTreeNode right = null;
         protected BinaryTreeNode parent = null;
         
         
         public BinaryTreeNode(T item)
         {super(item);}
         
         
         public void setLeft(BinaryTreeNode leftChild)
         {left = leftChild;}
         
         public void setRight(BinaryTreeNode rightChild)
         {right = rightChild;}
     }
     
     private class BinarySearchTree<T extends Comparable> extends Tree
     {
         public BinarySearchTree(T item)
         {root = new BinaryTreeNode(item);}
         
         
         public void insertItem(T item)
         {
             insertItem(item, (BinaryTreeNode)root);
         }
         
         private BinaryTreeNode insertItem(T item, BinaryTreeNode localRoot)
         {
             if (localRoot == null)
                 localRoot = new BinaryTreeNode(item);
             
             else if (item.compareTo((T)localRoot.item) <= 0)
             {
                 localRoot.left = insertItem(item, localRoot.left);
                 localRoot.left.setParent(localRoot);
             }
             
             else if (item.compareTo((T)localRoot.item) > 0)
             {
                 localRoot.right = insertItem(item, localRoot.right);
                 localRoot.right.setParent(localRoot);
             }
             
             return localRoot;
         }
         
         public BinaryTreeNode getMin(BinaryTreeNode parent)
         {
             if (parent == null)
                 return null;
             
             while(parent.left != null)
                 parent = parent.left;
             return parent;
         }
         
         public BinaryTreeNode getMax(BinaryTreeNode parent)
         {
             if (parent == null)
                 return null;
             
             while(parent.right != null)
                 parent = parent.right;
             return parent;
         }
         
         public BinaryTreeNode find(T item)
         {return find(item, (BinaryTreeNode)root);}
         
         private BinaryTreeNode find(T item, BinaryTreeNode node)
         {
             if (node == null)
                 return null;
             
             else if (item.compareTo(node.item) == 0)
                 return node;
             
             else if (item.compareTo(node.item) < 0)
                 return find(item, node.left);
             
             else
                 return find(item, node.right);
         }
         
         public BinaryTreeNode deleteItem(T item)
         {
             BinaryTreeNode deleteMe = find(item);
             deleteNode(deleteMe);
             return deleteMe;
         }
         
         public void deleteNode(BinaryTreeNode node)
         {
             if (node == null)
                 return;
             
             BinaryTreeNode parent = node.parent;
             
             // no children
             if (node.left == null && node.right == null)
             {
                 // if this is the only node in the tree, it's the root
                 if (parent == null)
                     root = null;
                 
                 // delete this node from the parent node
                 else if (node.item.compareTo(parent.item) < 0)
                     parent.left = null;
                 else
                     parent.right = null;
                 
                 // we out
                 return;
             }
             
             // only one child, 
             if (node.left == null ^ node.right == null)
             {
                 // promote right child
                 if (node.left == null)
                 {
                     // link node's child to node's parent
                     node.right.parent = parent;
                     
                     // link parent to node's child
                     if (node.item.compareTo(parent.item) < 0)
                         parent.left = node.right;
                     else
                         parent.right = node.right;
                 }
                 
                 // promote left child
                 else
                 {
                     // link node's child to node's parent
                     node.left.parent = parent;
                     
                     // link parent to node's child
                     if(node.item.compareTo(node.item) < 0)
                         parent.left = node.left;
                     else
                         parent.right = node.left;
                 }
                 
                 // we out
                 return;
             }
             
             // two children
             // whether to use success or predecessor should be randomized to avoid unbalanced tree
             BinaryTreeNode successor = getMin(node.right);
             node.item = successor.item;
             deleteNode(successor);
             updateRootNode();
         }
         
         /**
          * Checks to see if the root node has been assigned a parent, if it has 
          * the root reference is updated to root.parent (since root cannot have 
          * a parent).  Call this after any call (other than insert) that modifies the tree.
          */
         private void updateRootNode()
         {
             if (root.parent != null)
                 root = root.parent;
         }
     }
     
     private class SplayTree<T extends Comparable> extends BinarySearchTree
     {
         public SplayTree(T item)
         {super(item);}
         
         
         // I hate you, type erasure. It should be of type T, but Java generics stink.
         @Override
         public void insertItem(Comparable item)
         {
             super.insertItem(item);
             splay((T)item);
         }
         
         /**
          * Delete an item from the SplayTree.
          * @param item Key to delete
          * @return Returns deleted item's BinaryTreeNode.
          */
         @Override
         public BinaryTreeNode deleteItem(Comparable item)
         {
             BinaryTreeNode deleteMe = find(item);
             BinaryTreeNode deleteMeParent = deleteMe.parent;
             super.deleteItem((Comparable)deleteMe);
             splay((T)deleteMeParent.item);
             
             return deleteMe;
         }
         
         private void splay(T item)
         {
             BinaryTreeNode node = find(item);
             BinaryTreeNode parent, grandparent;
             
             // if the search key is the root, we're done
             if (node == root)
                 return;
             
             while(node != root)
             {
                 parent = node.parent;
                 // single rotate to root position, zig once
                 if (parent == root)
                 {
                     if (parent.item.compareTo(node.item) > 0)
                         rotateRight(node);
                     else
                         rotateLeft(node);
                     return;
                 }
                 
                 grandparent = parent.parent;
                 // if both are right or left children, zig twice
                 if ((parent.left == node && grandparent.left == parent) ||
                     (parent.right == node && grandparent.right == parent))
                 {
                     // rotate right
                     if (parent.left == node)
                     {
                         rotateRight(parent);
                         rotateRight(node);
                     }
                     
                     // rotate left
                     else
                     {
                         rotateLeft(parent);
                         rotateLeft(node);
                     }
                 }
                 
                 // parent is left child and node is right child or vice versa, zig-zag
                 else
                 {
                     // rotate left, right
                     if (parent.right == node)
                     {
                         rotateLeft(node);
                         rotateRight(node);
                     }
                     
                     // rotate right, left
                     else
                     {
                         rotateRight(node);
                         rotateLeft(node);
                     }
                 }
             }
         }
         
         private void rotateRight(BinaryTreeNode node)
         {
             if (node == root)
                 return;
             
             BinaryTreeNode right = node.right;
             BinaryTreeNode parent = node.parent;
             BinaryTreeNode grandparent = parent.parent;
             
             // link node's right child to parent's left
             parent.left = right;
             if (right != null)
                 right.parent = parent;
             
             // move parent down
             node.right = parent;
             node.parent = grandparent;
             parent.parent = node;
             
             // link grandparent (now node's parent) to node
             if (grandparent != null)
             {
                 if (node.item.compareTo(grandparent.item) < 0)
                     grandparent.left = node;
                 else
                     grandparent.right = node;
             }
             
             // see if node has become the root from the rotation
             super.updateRootNode();
         }
         
         private void rotateLeft(BinaryTreeNode node)
         {
             if (node == root)
                 return;
             
             BinaryTreeNode left = node.left;
             BinaryTreeNode parent = node.parent;
             BinaryTreeNode grandparent = parent.parent;
             
             // link node's left child to parent's right
             parent.right = left;
             if (left != null)
                 left.parent = parent;
             
             // move parent down
             node.left = parent;
             node.parent = grandparent;
             parent.parent = node;
             
             // link grandparent (now node's parent) to node
             if (grandparent != null)
             {
                 if (node.item.compareTo(grandparent.item) < 0)
                     grandparent.left = node;
                 else
                     grandparent.right = node;
             }
             
             // see if node has become the root from the rotation
             super.updateRootNode();
         }
     }
     
     private class MyTree<T extends Comparable<T>>
     {   
         private MyNode<T> treeRoot;
         
         private class MyNode<T>
         {
             T item;
             MyNode<T> left;
             MyNode<T> right;
             
             public MyNode()
             {}
             
             public MyNode(T item)
             {this.item = item;}
         }
         
         public MyTree(T item)
         {
             treeRoot = new MyNode();
             treeRoot.item = item;
         }
         
         public MyTree()
         {treeRoot = null;}
         
         
         public void insertItem(T item)
         {insertItem(item, treeRoot);}
         
         public void printTree()
         {printTree(treeRoot);}
         
         public void printTree(MyNode node)
         {
             if (node == null)
                 return;
             
             System.out.println("Node: " + node.item);
             if (node.left != null)
             {
                 System.out.println("Left: ");
                 printTree(node.left);
             }
             if (node.right != null)
             {
                 System.out.println("Right: ");
                 printTree(node.right);
             }
         }
         
         public boolean contains(T item)
         {return contains(item, treeRoot);}
         
         private boolean contains(T item, MyNode node)
         {
             if (node == null)
                 return false;
             
             boolean found = false;
             if (node.item.equals(item))
                 found = true;
             
             else if (item.compareTo((T)node.item) <= 0)
                 found = contains(item, node.left);
             else if (item.compareTo((T)node.item) > 0)
                 found = contains(item, node.right);
             
             return found;
         }
         
         private MyNode<T> insertItem(T item, MyNode localRoot)
         {
             if (localRoot == null)
                 localRoot = new MyNode(item);
             
             else if (item.compareTo((T)localRoot.item) <= 0)
                 localRoot.left = insertItem(item, localRoot.left);
             
             else if (item.compareTo((T)localRoot.item) > 0)
                 localRoot.right = insertItem(item, localRoot.right);
             
             return localRoot;
         }
         
         
     }
     
     
     
     // sorting
     abstract class Sort
     {
         ArrayList<Entry> entries = new ArrayList();
         
         public void insert(Entry entry)
         {entries.add(entry);}
         
         public void remove(Entry entry)
         {entries.remove(entry);}
         
         public void printSort()
         {
             for(Entry entry : entries)
                 if(entry != null)
                     System.err.println("Key: " + entry.key + "\tVal: " + entry.val);
         }
         
         public abstract void sort();
     }
    
     
     
     
     
     public Practice()
     {
         Integer[] ints = {1,2,3,4,5,6,7,8,9,0};
         Entry[] entries = new Entry[ints.length];
         for (int i = 0; i<ints.length; i++)
             entries[i] = new Entry(ints[i], ints[i]);
             
 /*        
         MyTree<Integer> tree = new MyTree(10);
         tree.insertItem(5);
         tree.insertItem(15);
         tree.insertItem(14);
         tree.insertItem(16);
         tree.printTree();
         
         int find = 16;
         System.out.println("Contains " + find + " : " + tree.contains(find));
 */
         
 /*        
         MyVector<Integer> myVector = new MyVector();
         
         for (int ct = 0; ct < 5; ct++)
             myVector.putItem(ct);
         myVector.printArray();
         
         myVector.removeItem(4);
         myVector.printArray();
 */
         
 /*      
         int[] array = new int[arraySize];
         
         array[0] = 1;
         array[1] = 2;
         array[2] = 3;
         
         printArray(array);
         println();
         
         for (int ct = 0; ct < arraySize; ct++)
         {
             array = shrinkArray(array);
             printArray(array);
             println();
         }
 */
     }
 
     
     
     
     
     public static void printArray(int[] incoming)
     {
         for(int ct = 0; ct < incoming.length; ct++)
             System.out.println("[" + ct + "]" + incoming[ct]);
     }
     
     public static void println()
     {
         System.out.println("-------------------------------------------------");
     }
     
     
     
     
     
     
     public static void main(String[] args) 
     {
         Practice practice = new Practice();
         System.exit(0);
     }
 }
