 

 package com.gerardmeier.tree;
 
 
 public class Criterion {
     public float minimalSize;
     public float maximalSize;
     public int minimalEntities;
     public int maximalEntities;
     public boolean permitDiagonal;
 
     public Criterion() {
         // Default settings that are unlikely to overflow with too much recursion.
         this(50, 200, 50, 200, true);
     }
 
     public Criterion(float minSize, float maxSize, int minEntities, int maxEntities) {
         this(minSize, maxSize, minEntities, maxEntities, true);
     }
 
     public Criterion(float minSize, float maxSize, int minEntities, int maxEntities, boolean permitDiagonal) {
         minimalSize = minSize;
         maximalSize = maxSize;
         minimalEntities = minEntities;
         maximalEntities = maxEntities;
         this.permitDiagonal = permitDiagonal;
     }
 
     @Override
     public String toString() {
        return "[Heuristic: Size: " + minimalSize + "-" + maximalSize + ", entities: " + minimalEntities + "-" + maximalEntities + "]";
     }
 }
