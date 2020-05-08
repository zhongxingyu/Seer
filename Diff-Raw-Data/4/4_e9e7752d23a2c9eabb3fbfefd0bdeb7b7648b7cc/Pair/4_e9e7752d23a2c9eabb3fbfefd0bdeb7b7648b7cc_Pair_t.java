 package com.group7.dragonwars.engine;
 
 public class Pair<L, R> {
 
     private L left;
     private R right;
 
     public Pair(L left, R right) {
         this.left = left;
         this.right = right;
     }
 
     public L getLeft() {
         return this.left;
     }
 
     public R getRight() {
         return this.right;
     }
 
     public String toString() {
         return String.format("(%s, %s)", left, right);
     }
 
     public int hashCode() {
    	int hashFirst = left != null ? left.hashCode() : 0;
    	int hashSecond = left != null ? right.hashCode() : 0;
 
     	return (hashFirst + hashSecond) * hashSecond + hashFirst;
     }
 
     public Boolean equals(Pair other) {
         if (other == null) {
             return false;
         }
 
         return left.equals(other.getLeft()) && right.equals(other.getRight());
     }
 }
