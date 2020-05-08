 //Ashley Barton
 //October 11th, 2013
 //Project 1 - BTNode class
 
 import java.util.*;
 
 
 public class BTNode<E> {
    private E data;
    private BTNode<E> left;
    private BTNode<E> right;
    private BTNode<E> root;
 
    public BTNode(E initialData, BTNode<E> initialLeft, BTNode<E> initialRight)
    {
       data = initialData;
       left = initialLeft;
       right = initialRight;
    }
 
    public E getData()
    {
       return data;
    }
 
    public BTNode<E> getLeft()
    {
       return left;
    }
 
    public BTNode<E> getRight()
    {
       return right;
    }
 
    public void setData(E newData)
    {
       data = newData;
    }
    public void setLeft(BTNode<E> newLeft)
    {
       left = newLeft;
    }
 
    public void setRight(BTNode<E> newRight)
    {
       right= newRight;
    }
 
 /*   public void remove(E removeValue, BTNode <E> node)
    {
       if(node == null)
       {
          System.out.println(removeValue + "does not exist");
       }
       else if(removeValue < node.getData())
       {
          left.remove(removeValue, left.node);
       }
       else if(removeValue > node.getData())
       {
          right.remove(removeValue, right.node);
       }
       else(removeValue == node.getData()
       {
          //remove node
          //find the largest of the smallest node
          //replace that value with the current node and set it equal to null
       }
 
    }
 */
 
  public void insert(E insertValue, BTNode<E> node)
    {
       if(node == null)
       {
          //need to set something up
          //new BTNode<Integer>(insertValue, null, null);
       }
       else if(insertValue < getData())
       {
          left.insert(insertValue, left.node);
       }
       else if(insertValue > getData())
       {
          right.insert(insertValue, right.node);
       }
       else if(insertValue == getData())
       {
          System.out.println(insertValue + " already exists, ignore.");
       }
    }
 
 
    public String inOrderTrav()
    {
       if(left !=null)
          left.inOrderTrav();
       System.out.print(data);
      if(right != null)
         right.inOrderTrav();
   }
 
    public String preOrderTrav()
    {
       System.out.println(data);
       if(left !=null)
          left.preOrderTrav();
       if(right != null)
          right.preOrderTrav();
    }
 
    public String postOrderTrav()
    {
       if(left !=null)
          left.postOrderTrav();
       if(right != null)
          right.postOrderTrav();
       System.out.println(data);
    }
 
 /*
    public BTNode<E> predecessor(BTNode<E> source)
    {
 
    }
 */
 
 /*
    public BTNode<E> successor(BTNode <E> source)
    {
       if(source.getData() == data)
 
       if(left !=null)
       {
          left.inOrderTrav();
       }
       if(right != null)
       {
          right.inOrderTrav();
       }
     }
 */
 }
