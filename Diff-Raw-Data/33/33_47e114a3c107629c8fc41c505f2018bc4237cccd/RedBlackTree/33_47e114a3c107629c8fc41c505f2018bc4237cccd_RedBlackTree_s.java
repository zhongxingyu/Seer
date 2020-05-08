 package edu.puc.concurrentavl;
 
 import java.io.PrintStream;
 
 public class RedBlackTree implements ISearchTree, ITreeHolder {
 
 	protected Node root = null;
 	protected int height = 0;
 	protected boolean unbalanced = false;
 
 	@Override
 	public void delete(int delVal) {
         if(root != null)
 		    root.delete(delVal);
 	}	
 
 	@Override
 	public boolean find(int value) {
         if(root != null)
             return root.find(value);
         return false;
 	}
 	
 	protected Node findMin()
 	{
         if(root != null)
 		    return root.findMin();
         else
             return null;
 	}
 
 	@Override
 	public void insert(int newValue) {
         if(root != null)
 		    root.insert(newValue);
         else
             root = new Node(null, newValue, this);
 	}
 
     @Override
     public void updateRoot(Node newRoot) {
         this.root = newRoot;
         //this.root.color = Node.BLACK;
     }
 
 	public void print()
 	{
         if(root != null)
 		    root.print(System.out, "", "",  '-', Node.RIGHT, 0);
 	}
 
 	public void print(int extraWidth)
 	{
         if(root != null)
 		    root.print(System.out, "", "",  '-', Node.RIGHT, extraWidth);
 	}
 	
 	public void print(PrintStream ps)
 	{
         if(root != null)
             root.print(ps);
 
 	}
 
 	public void print(PrintStream ps, int extraWidth)
 	{
         if(root != null)
 		    root.print(ps, extraWidth);
 	}
 
 }
