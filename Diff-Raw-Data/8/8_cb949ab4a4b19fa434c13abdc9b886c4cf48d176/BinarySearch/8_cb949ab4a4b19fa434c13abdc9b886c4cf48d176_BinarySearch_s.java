 /**
  * Copyright (c) 2013, Matthew Erickson (Matt@MattErickson.ME)
  * All rights reserved.
  * 
  * Please see copyright.txt for full license details
  **/
 package me.matterickson.problems;
 
 import java.util.Random;
 
 import me.matterickson.model.TreeNode;
  
 /**
  * @author "Matt@MattErickson.ME"
  *
  */
 public class BinarySearch {
   private TreeNode m_node = new TreeNode();
   private int m_findCounter = 0;
   
   public BinarySearch() {
   }
   
   public void runSearch(int treeSize) {
     Random randomGenerator = new Random();
     m_node.setValue(randomGenerator.nextInt(50));
     System.out.println("Building our B-Search Tree with a root node of: [" + m_node.getValue() + "] and size [" + treeSize + "]");
     while (treeSize > 0) {
       int nextValue = randomGenerator.nextInt(50);
       setNewNode(m_node, nextValue);
       treeSize--;
     }
     System.out.println("The tree has been created, now lets find a value in O(log(n))! =P");
     find(m_node, randomGenerator.nextInt(50));
   }
   
   private void setNewNode(TreeNode node, int newValue) {
     if (node != null) {
       if (newValue < node.getValue()) {
         if (node.getLeft() != null) {
           setNewNode(node.getLeft(), newValue);
         } else {
          System.out.println("Interting value [" + newValue + "] to the left of [" + node.getValue() + "]");
           node.setLeft(new TreeNode(newValue));
         }
       } else if (newValue > node.getValue()) {
         if (node.getRight() != null) {
           setNewNode(node.getRight(), newValue);
         } else {
          System.out.println("Interting value [" + newValue + "] to the right of [" + node.getValue() + "]");
           node.setRight(new TreeNode(newValue));
         }
       }
     }
   }
   
   private void find(TreeNode node, int findThisNumber) {
     m_findCounter++;
    System.out.println("How deep down the rabit hole are we? [" + m_findCounter + "]");
     if (node == null) {
       return;
     } else {
       if (findThisNumber < node.getValue()) {
         find(node.getLeft(), findThisNumber);
       } else if (findThisNumber > node.getValue()) {
         find(node.getRight(), findThisNumber);
       } else if (findThisNumber == node.getValue()) {
         node.setMessage("FOUND IT!!!");
         System.out.println(node);
       }
     }
   }
 }
