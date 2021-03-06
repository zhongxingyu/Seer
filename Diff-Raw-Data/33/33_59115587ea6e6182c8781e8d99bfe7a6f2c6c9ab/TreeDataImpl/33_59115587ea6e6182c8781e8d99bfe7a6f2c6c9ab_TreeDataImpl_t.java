 package org.clueminer.hclust;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.clueminer.clustering.api.dendrogram.DendroTreeData;
 import org.clueminer.clustering.api.dendrogram.DendroNode;
 import org.clueminer.distance.api.DistanceMeasure;
import org.clueminer.utils.Dump;
 
 /**
  *
  * Stores tree structure
  *
  *                     says what is this level height and at which index are children
  * Example:                                         v
  *   idx  | 0 |  1 |  2  | 3 |  4 |  5 |  6 |  7 |  8 |  9 |
  * -------------------------------------------------------------------------------------
  * height: 0.0, 0.0, 0.0, 0.0, 0.0, 0.9, 1.5, 1.9, 7.4, 0.0
  * left:    -1,  -1,  -1,  -1,  -1,   1,   2,   0,   5,  -1
  * right:   -1,  -1,  -1,  -1,  -1,   3,   4,   6,   7,  -1
  * order: 5, 6, 7, 8, -1         ^
  *                 ^       node is a leaf
  *            tree top level
  *
  * @author Tomas Barton
  *
  */
 public class TreeDataImpl implements Serializable, DendroTreeData {
 
     private static final long serialVersionUID = -3984381476142130357L;
     private int[] left;
     private int[] right;
     private int[] order;
     private double[] height;
     private DistanceMeasure function;
     private double cutoff = -1;
     private int clusters[];
     private double min = Double.MAX_VALUE;
     private double max = Double.MIN_VALUE;
     private static final Logger logger = Logger.getLogger(TreeDataImpl.class.getName());
 
     /**
      * number of cluster starts from 1, if num eq 0, node wasn't visited by the
      * numbering algorithm
      */
     private int clusterNum = 0;
 
     public TreeDataImpl(DistanceMeasure function) {
         this.function = function;
     }
 
     /**
      * Array size is 2*height
      *
      * @param idx
      * @return index in height array
      */
     public int getLeft(int idx) {
         return this.left[idx];
     }
 
     /**
      * @param idx
      * @param left_children
      */
     public void setLeft(int idx, int left_children) {
         this.left[idx] = left_children;
     }
 
     public void setLeft(int[] left) {
         this.left = left;
     }
 
     /**
      * Array size is 2*height if height(right[idx]) == 0 => node is leaf
      *
      * @param idx
      * @return index in height array
      */
     public int getRight(int idx) {
         return right[idx];
     }
 
     /**
      * @param idx
      * @param right_children
      */
     public void setRight(int idx, int right_children) {
         this.right[idx] = right_children;
     }
 
     public void setRight(int[] right) {
         this.right = right;
     }
 
     public void setOrder(int[] order) {
         this.order = order;
     }
 
     /**
      * @param idx
      * @return node index of tree levels
      */
     public int getOrder(int idx) {
         return this.order[idx];
     }
 
     public int getOrderLength() {
         return order.length;
     }
 
     /**
      * @param idx
      * @return tree heights at @idx level
      */
     public double getHeight(int idx) {
         return this.height[idx];
     }
 
     /**
      * height[0] says on which level are children in
      *
      * @param height array of level's heights
      */
     public void setHeight(double[] height) {
         this.height = height;
 
         //some distance functions might produce negative values which doesn't
         //make much sense in context of tree's height, therefore we have to
         //move the range
         double nodeHeightOffset;
         if (function.useTreeHeight()) {
             nodeHeightOffset = getMinHeight() * function.getNodeOffset();
         } else {
             nodeHeightOffset = function.getNodeOffset();
         }
 
         if (nodeHeightOffset != 0.0) {
             for (int i = 0; i < height.length; i++) {
                 height[i] += nodeHeightOffset;
             }
         }
         //Dump.array(height, "tree height");
     }
 
     public void setFunction(DistanceMeasure function) {
         this.function = function;
     }
 
     public DistanceMeasure getFunction() {
         return function;
     }
 
     /**
      * Number includes leaves
      *
      * @return int
      */
     @Override
     public int treeLevels() {
         return order.length - 1;
     }
 
     /**
      * Number of terminal nodes
      *
      * @return
      */
     @Override
     public int numLeaves() {
         return height.length;
     }
 
     public boolean isLeaf(int idx) {
         if (getLeft(idx) == -1 && getRight(idx) == -1) {
             return true;
         }
         return false;
         //return (getHeight(idx) == 0.0); -too expensive and possible source of errors
     }
 
     /**
      * doesn't work with negative distances
     * @param zero_threshold
     * @return
      */
     public int getNumberOfTerminalNodes(double zero_threshold) {
         int n = 0;
         int index;
 
         for (int i = 0; i < order.length; i++) {
             index = order[i];
             if (index == -1 || height[index] < zero_threshold) {
                 continue;
             }
             n++;
         }
         return n + 1;
     }
 
     /**
      * Get idx of tree's root
      *
      * @return node's idx
      */
     public int getIntRoot() {
         return order[order.length - 2];
     }
 
     /**
      * From tree cutoff we can determine the number of clusters
      *
      * @return number between 0 and max tree height
      */
     public double getCutoff() {
         return this.cutoff;
     }
 
     public void setCutoff(double cutoff) {
         clusters = null; //clear result, if any
         clusterNum = 0;
         this.cutoff = cutoff;
     }
 
     public double treeCutByLevel(int level) {
         double lower = 0.0, upper, dist = 0.0;
         int idx;
         if (level < treeLevels()) {
             idx = getOrder(level);
             upper = getHeight(idx);
             if (level > 0) {
                 idx = getOrder(level - 1);
                 lower = getHeight(idx);
             }
             dist = upper - lower;
         }
         //half of distance between two levels
         return (dist / 2 + lower);
     }
 
     public void formClusters(int nodesNum) {
         synchronized (this) {
             if (nodesNum < 1) {
                 formClusters();
            }            
             ensureClusters(nodesNum);
             findClusters(getIntRoot(), -1);
         }
     }
 
     private void ensureClusters(int capacity) {
         logger.log(Level.INFO, "term nodes: ensuring tree clusters size to: {0}, root is {1}", new Object[]{capacity, getIntRoot()});
         synchronized (this) {
            // we can't have more clusters than data
            if (capacity > numLeaves()) {
                logger.log(Level.WARNING, "wooha mister won't go to {0}. maximum number of clusters set to: {1}", new Object[]{capacity, numLeaves()});
                capacity = numLeaves();
            }
             if (clusters == null || clusters.length == 0) {
                 clusters = new int[capacity];
             } else {
                 int[] aryCpy = new int[capacity];
                int minSize = clusters.length < capacity ? clusters.length : capacity;
                // if array will be smaller, copy only data that will fit
                System.arraycopy(clusters, 0, aryCpy, 0, minSize);
                 clusters = aryCpy;
                 logger.log(Level.INFO, "reallocated array: {0}", new Object[]{clusters.length});
             }
         }
     }
 
     public void formClusters() {
        synchronized (this) {
            int nodesNum = getNumberOfTerminalNodes(0.00001);
            logger.log(Level.INFO, "expected tree nodes number: {0}", new Object[]{nodesNum});
            ensureClusters(nodesNum);
            findClusters(getIntRoot(), -1);
            Dump.array(clusters, "result clusters");
        }
     }
 
     /**
      * Number specifies cluster assignment
      *
      * clusters [ 1 2 2 2 1 2 2 2 2 2 2 2 2 2 2 2 2 ]
      *
      * @param terminalsNum
      * @return array of node's assignments
      */
     public int[] getClusters(int terminalsNum) {
         if (clusters == null) {
             formClusters(terminalsNum);
         }
 
         int[] clust = new int[clusterNum];
         //copy just filled part
         System.arraycopy(clusters, 0, clust, 0, clusterNum);
         return clust;
     }
 
     /**
      * Number of cluster found by given cutoff
      *
      * @return
      */
     public int getNumberOfClusters() {
         if (clusterNum == 0) {
             formClusters();
         }
         return clusterNum;
     }
 
     /**
      * According to given cutoff, assign items to clusters numbered from 1 to k
      *
      * @param idx
      * @param parent
      */
     public void findClusters(int idx, int parent) {
         if (parent > -1) {
             if (getHeight(parent) > cutoff && getHeight(idx) < cutoff) {
                 //in between cutoff border -> create a cluster
                 clusterNum++;
                 //System.out.println("parent= "+getHeight(parent)+ ", node= "+getHeight(idx) + ", clustNum= "+clusterNum);
             }
         }
 
         if (isLeaf(idx)) {
             //Logger.getLogger(TreeDataImpl.class.getName()).log(Level.INFO, "getting {0} clusters size: {1}", new Object[]{idx, clusters.length});
             //assign cluster's id
             //logger.log(Level.INFO, "setting idx: {0} to cluster {1}", new Object[]{idx, clusterNum});
             if (idx >= clusters.length) {
                 int capacity = (int) (idx * 1.618);
                 ensureClusters(capacity);
             }
 
             clusters[idx] = clusterNum;
             return;
         }
 
         //left node
         findClusters(getLeft(idx), idx);
         //right node
         findClusters(getRight(idx), idx);
     }
 
     /**
      * Returns min height of the tree nodes.
      * @return
      */
     public double getMinHeight() {
         if (min == Double.MAX_VALUE) {
             for (int i = 0; i < order.length - 1; i++) {
                 min = Math.min(min, height[order[i]]);
             }
         }
         return min;
     }
 
     /**
      * Returns max height of the tree nodes.
      * @return
      */
     public double getMaxHeight() {
         if (max == Double.MIN_VALUE) {
             for (int i = 0; i < order.length - 1; i++) {
                 max = Math.max(max, height[order[i]]);
             }
         }
         return max;
     }
 
     /**
      * Returns true if tree is flat
      * @return
      */
     public boolean flatTreeCheck() {
         if (height.length == 1) {
             return false;
         }
 
         for (int i = 0; i < height.length - 1; i++) {
             if (height[i] != height[i + 1]) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Calculates tree node positions.
      * @return
      */
     public float[] getPositions() {
         float[] pos = new float[left.length];
         Arrays.fill(pos, -1);
         if (order.length < 2) {
             return pos;
         }
         fillPositions(pos, left, right, 0, left.length - 2);
         int node;
         for (int i = 0; i < order.length - 1; i++) {
             node = order[i];
             pos[node] = (pos[left[node]] + pos[right[node]]) / 2f;
         }
         return pos;
     }
 
     private int fillPositions(float[] positions, int[] child1, int[] child2, int pos, int index) {
         if (child1[index] != -1) {
             pos = fillPositions(positions, child1, child2, pos, child1[index]);
         }
         if (child2[index] != -1) {
             pos = fillPositions(positions, child1, child2, pos, child2[index]);
         } else {
             positions[index] = pos;
             pos++;
         }
         return pos;
     }
 
     public boolean isEmpty() {
         return this.height == null;
     }
 
     public int[] createTreeOrder() {
         return createTreeOrder(null);
     }
 
     public int[] createTreeOrder(int[] indices) {
         return getLeafOrder(indices);
     }
 
     private int[] getLeafOrder(int[] indices) {
         if (this.isEmpty() || this.getOrderLength() < 2) {
             return null;
         }
         return getLeafOrder(getOrderLength(), this.left, this.right, indices);
     }
 
     private int[] getLeafOrder(int nodeOrderLen, int[] left, int[] right, int[] indices) {
         int[] leafOrder = new int[nodeOrderLen];
         Arrays.fill(leafOrder, -1);
         fillLeafOrder(leafOrder, left, right, 0, left.length - 2, indices);
         return leafOrder;
     }
 
     /**
      * @TODO rewrite to iterative version
      *
      * @param leafOrder
      * @param child1
      * @param child2
      * @param pos
      * @param index
      * @param indices
      * @return
      */
     private int fillLeafOrder(int[] leafOrder, int[] child1, int[] child2, int pos, int index, int[] indices) {
         if (child1[index] != -1) {
             pos = fillLeafOrder(leafOrder, child1, child2, pos, child1[index], indices);
         }
         if (child2[index] != -1) {
             pos = fillLeafOrder(leafOrder, child1, child2, pos, child2[index], indices);
         } else {
             leafOrder[pos] = indices == null ? index : indices[index];
             pos++;
         }
         return pos;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("TreeData [");
         sb.append("function= ").append(function).append("\n");
         sb.append("height: ");
         for (int i = 0; i < height.length; i++) {
             sb.append(i).append(" = ").append(height[i]).append(", ");
         }
         sb.append("\n");
 
         sb.append("left: ");
         for (int i = 0; i < left.length; i++) {
             sb.append(i).append(" = ").append(left[i]).append(", ");
         }
         sb.append("\n");
 
         sb.append("right: ");
         for (int i = 0; i < right.length; i++) {
             sb.append(i).append(" = ").append(right[i]).append(", ");
         }
         sb.append("\n");
 
         sb.append("order: ");
         for (int i = 0; i < order.length; i++) {
             sb.append(i).append(" = ").append(order[i]).append(", ");
         }
         sb.append("\n");
 
         sb.append("]");
         return sb.toString();
     }
 
     @Override
     public int numNodes() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public DendroNode getRoot() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
