 //Ashley Barton
 //October 11th, 2013
 //Project 1 - BTNode class
 
 import java.util.*;
 
 
 public class BTNode<E extends Integer> {
    private E data;
    private BTNode<E> left;
    private BTNode<E> right;
 
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
 
 /*
    public void remove(E removeValue, BTNode <E> node)
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
 
    public void insert(E insertValue)
    {
       if(getData() == null)
       {
          setData(insertValue);
       }
       else if(insertValue < getData())
       {
          if(left == null) {
             left = new BTNode<E>(null, null, null);
          }
          left.insert(insertValue);
       }
       else if(insertValue > getData())
       {
          if(right == null) {
             right = new BTNode<E>(null, null, null);
          }
          right.insert(insertValue);
       }
       else if(insertValue == getData())
       {
          System.out.println(insertValue + " already exists, ignore.");
       }
    }
 
 
    public String inOrderTrav()
    {
       String r = "";
 
       if(left !=null)
       {
         r = r + left.inOrderTrav();
       }
 
       if(getData() != null)
       {
          r = r + getData();
       }
 
       if(right != null)
       {
         r = r + right.inOrderTrav();
       }
 
       return r;
    }
 
    public String preOrderTrav()
    {
       String r = "";
 
       if(getData() != null)
       {
          r = r + getData();
       }
 
       if(left !=null)
       {
          r = r + left.preOrderTrav();
       }
 
       if(right != null)
       {
          r = r + right.preOrderTrav();
       }
 
       return r;
    }
 
    public String postOrderTrav()
    {
       String r = "";
 
       if(left != null)
       {
          r = r + left.postOrderTrav();
       }
 
       if(right != null)
       {
          r = r + right.postOrderTrav();
       }
 
       if(getData() != null)
       {
          r = r + getData();
       }
 
       return r;
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
 // vim: set expandtab sts=3 ts=3 sw=3:
