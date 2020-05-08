 /*
  * DvRLib - Container
  * Duncan van Roermund, 2010-2011
  * WeightedTreeIterator.java
  */
 
 package dvrlib.container;
 
 import java.util.Iterator;
 
 public class WeightedTreeIterator<E> implements Iterator<E> {
    protected final WeightedTree<E> tree;
    protected WeightedTreeNode<E> node;
    protected Iterator<E> nodeIterator = null;
 
    public WeightedTreeIterator(WeightedTree<E> tree) {
       this.tree = tree;
       node = tree.getMax();
       if(node != null)
          nodeIterator = node.values.iterator();
    }
 
    @Override
    public boolean hasNext() {
       return (nodeIterator != null);
    }
 
    @Override
    public E next() {
      assert hasNext()      : "!hasNext()";
      assert (node != null) : "node == null";
 
       E next = nodeIterator.next();
       if(!nodeIterator.hasNext()) {
          if(node.left != null)
            node = node.left.getMin();
          else {
             while(node.parent != null && node.parent.left == node) {
                node = node.parent;
             }
             node = node.parent;
          }
 
          if(node != null)
             nodeIterator = node.values.iterator();
          else
             nodeIterator = null;
       }
       return next;
    }
 
    @Override
    public void remove() {
       throw new UnsupportedOperationException(this.getClass().getName() + ".remove() is not supported");
    }
 }
