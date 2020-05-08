 package squared.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a node of the Tree<T> class. The TreeNode<T> is also a container, and
  * can be thought of as instrumentation to determine the location of the type T
  * in the Tree<T>.
  */
 public class TreeNode<T> {
  
	public TreeNode<T> parent = null;
     public T data;
     public List<TreeNode<T>> children;
  
     /**
      * Default ctor.
      */
     public TreeNode() {
         super();
     }
  
     /**
      * Convenience ctor to create a TreeNode<T> with an instance of T.
      * @param data an instance of T.
      */
     public TreeNode(T data) {
         this();
         setData(data);
     }
      
     /**
      * Return the children of TreeNode<T>. The Tree<T> is represented by a single
      * root TreeNode<T> whose children are represented by a List<TreeNode<T>>. Each of
      * these TreeNode<T> elements in the List can have children. The getChildren()
      * method will return the children of a TreeNode<T>.
      * @return the children of TreeNode<T>
      */
     public List<TreeNode<T>> getChildren() {
         if (this.children == null) {
             return new ArrayList<TreeNode<T>>();
         }
         return this.children;
     }
  
     /**
      * Sets the children of a TreeNode<T> object. See docs for getChildren() for
      * more information.
      * @param children the List<TreeNode<T>> to set.
      */
     public void setChildren(List<TreeNode<T>> children) {
         this.children = children;
     }
  
     /**
      * Returns the number of immediate children of this TreeNode<T>.
      * @return the number of immediate children.
      */
     public int getNumberOfChildren() {
         if (children == null) {
             return 0;
         }
         return children.size();
     }
      
     /**
      * Adds a child to the list of children for this TreeNode<T>. The addition of
      * the first child will create a new List<TreeNode<T>>.
      * @param child a TreeNode<T> object to set.
      */
     public void addChild(TreeNode<T> child) {
         if (children == null) {
             children = new ArrayList<TreeNode<T>>();
         }
         children.add(child);
     }
      
     /**
      * Inserts a TreeNode<T> at the specified position in the child list. Will     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
      * @param index the position to insert at.
      * @param child the TreeNode<T> object to insert.
      * @throws IndexOutOfBoundsException if thrown.
      */
     public void insertChildAt(int index, TreeNode<T> child) throws IndexOutOfBoundsException {
         if (index == getNumberOfChildren()) {
             // this is really an append
             addChild(child);
             return;
         } else {
             children.get(index); //just to throw the exception, and stop here
             children.add(index, child);
         }
     }
      
     /**
      * Remove the TreeNode<T> element at index index of the List<TreeNode<T>>.
      * @param index the index of the element to delete.
      * @throws IndexOutOfBoundsException if thrown.
      */
     public void removeChildAt(int index) throws IndexOutOfBoundsException {
         children.remove(index);
     }
  
     public T getData() {
         return this.data;
     }
  
     public void setData(T data) {
         this.data = data;
     }
      
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("{").append(getData().toString()).append(",[");
         int i = 0;
         for (TreeNode<T> e : getChildren()) {
             if (i > 0) {
                 sb.append(",");
             }
             sb.append(e.getData().toString());
             i++;
         }
         sb.append("]").append("}");
         return sb.toString();
     }
    
    public void setParent(TreeNode<T> par) {
    	parent = par;
    }
 }
