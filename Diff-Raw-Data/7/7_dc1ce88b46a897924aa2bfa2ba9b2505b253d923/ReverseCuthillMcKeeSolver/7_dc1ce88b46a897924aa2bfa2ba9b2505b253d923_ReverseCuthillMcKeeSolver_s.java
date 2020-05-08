 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.matrix;
 
 import gnu.trove.iterator.TIntIterator;
 import gnu.trove.list.array.TIntArrayList;
 import java.util.Arrays;
 import no.uib.cipr.matrix.BandMatrix;
 import no.uib.cipr.matrix.DenseMatrix;
 import no.uib.cipr.matrix.DenseVector;
 import no.uib.cipr.matrix.Matrix;
 import no.uib.cipr.matrix.MatrixEntry;
 import no.uib.cipr.matrix.UpperSPDBandMatrix;
 import no.uib.cipr.matrix.UpperSPDPackMatrix;
 import no.uib.cipr.matrix.UpperSymmBandMatrix;
 import no.uib.cipr.matrix.Vector;
 import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class ReverseCuthillMcKeeSolver {
 
     Matrix mat;
     Matrix graphMat;
     Matrix optMatrix;
     boolean upperSymmetric;
     int[] opt2ori;
     int[] ori2opt;
     int oriBandWidth;
     int optBandWidth;
 
    public  ReverseCuthillMcKeeSolver(Matrix mat, boolean upperSymmetric) {
         this.mat = mat;
         this.upperSymmetric = upperSymmetric;
         genGraphMat();
         reverseCuthillMcKee(0);
     }
 
     int pseudoPeripheralNode(int start) {
         TIntArrayList indes = new TIntArrayList(size());
         int[] distances = new int[size()];
         int maxDistance = 0;
         int node = start;
         do {
             broadFirstSearch(node, indes, distances);
             int distance = distances[indes.getQuick(indes.size() - 1)];
             if (distance <= maxDistance) {
                 break;
             } else {
                 maxDistance = distance;
                 int minDegree = Integer.MAX_VALUE;
                 for (int i = indes.size() - 1; i >= 0 && distances[indes.getQuick(i)] == distance; i--) {
                     int eccNode = indes.getQuick(i);
                     int deg = degree(eccNode);
                     if (deg < minDegree) {
                         minDegree = deg;
                         node = eccNode;
                     }
                 }
             }
         } while (true);
         return node;
     }
 
     private void reverseCuthillMcKee(int start) {
         TIntArrayList indes = new TIntArrayList(size());
         int[] distances = new int[size()];
         boolean[] visited = null;
         TIntArrayList opt2oriList = new TIntArrayList();
         do {
             int ppNode = pseudoPeripheralNode(start);
             broadFirstSearch(ppNode, indes, distances);
             opt2oriList.addAll(indes);
             if (opt2oriList.size() >= size()) {
                 break;
             } else {
                 if (null == visited) {
                     visited = new boolean[size()];
                 }
                 for (int i = 0; i < indes.size(); i++) {
                     visited[indes.getQuick(i)] = true;
                 }
                 for (int i = 0; i < visited.length; i++) {
                     if (!visited[i]) {
                         start = i;
                         break;
                     }
                 }
             }
         } while (true);
         opt2oriList.reverse();
         opt2ori = opt2oriList.toArray();
         genOri2Opt();
         genBandWidth();
         genOptMatrix();
     }
 
     void genBandWidth() {
         int oriB = 0;
         int optB = 0;
         for (int i = 0; i < size(); i++) {
             TIntIterator iter = neighborIterator(i);
             int optI = ori2opt[i];
             while (iter.hasNext()) {
                 int nb = iter.next();
                 int dst = Math.abs(nb - i);
                 if (dst > oriB) {
                     oriB = dst;
                 }
                 int optDst = Math.abs(ori2opt[nb] - optI);
                 if (optDst > optB) {
                     optB = optDst;
                 }
             }
         }
         oriBandWidth = oriB;
         optBandWidth = optB;
     }
 
     void genOri2Opt() {
         ori2opt = new int[size()];
         for (int i = 0; i < opt2ori.length; i++) {
             ori2opt[opt2ori[i]] = i;
         }
     }
 
     int degree(int node) {
         TIntIterator neighborIterator = neighborIterator(node);
         int degree = 0;
         while (neighborIterator.hasNext()) {
             degree++;
             neighborIterator.next();
         }
         return degree;
     }
 
     void broadFirstSearch(int start, TIntArrayList indes, int[] distances) {
         Arrays.fill(distances, -1);
         indes.resetQuick();
         indes.add(start);
         distances[start] = 0;
         for (int i = 0; i < indes.size(); i++) {
             int node = indes.getQuick(i);
             TIntIterator iter = neighborIterator(node);
             while (iter.hasNext()) {
                 int nb = iter.next();
                 if (distances[nb] < 0) {
                     indes.add(nb);
                     distances[nb] = distances[node] + 1;
                 }
             }
             if (indes.size() >= size()) {
                 break;
             }
         }
     }
 
     protected TIntIterator neighborIterator(int node) {
         if (graphMat instanceof FlexCompRowMatrix) {
             return new FlexCompRowNeiboursIterator((FlexCompRowMatrix) graphMat, node, upperSymmetric);
         } else {
             return new GeneralNeiboursIterator(graphMat, node, upperSymmetric);
         }
     }
 
     int size() {
         return mat.numRows();
     }
 
     private void genGraphMat() {
         if (mat instanceof FlexCompRowMatrix) {
             ((FlexCompRowMatrix) mat).compact();
         }
         if (upperSymmetric) {
             graphMat = mat;
         } else {
             graphMat = new DenseMatrix(size(), size());
             for (MatrixEntry me : mat) {
                 if (me.get() != 0) {
                     graphMat.set(me.row(), me.column(), 1);
                     graphMat.set(me.column(), me.row(), 1);
                 }
             }
         }
     }
 
     public int getOriginalBandWidth() {
         return oriBandWidth;
     }
 
     public int getOptimizedBandWidth() {
         return optBandWidth;
     }
 
     void genOptMatrix() {
 
         if (upperSymmetric) {
 
            if (mat instanceof UpperSPDBandMatrix || mat instanceof UpperSPDBandMatrix || mat instanceof UpperSPDPackMatrix) {
                 optMatrix = new UpperSPDBandMatrix(size(), optBandWidth);
             } else {
                 optMatrix = new UpperSymmBandMatrix(size(), optBandWidth);
             }
             for (MatrixEntry me : mat) {
                 if (me.get() != 0 && me.column() >= me.row()) {
                     int optRow = ori2opt[me.row()];
                     int optCol = ori2opt[me.column()];
                     if (optCol >= optRow) {
                         optMatrix.set(optRow, optCol, me.get());
                     } else {
                         optMatrix.set(optCol, optRow, me.get());
                     }
                 }
             }
         } else {
             optMatrix = new BandMatrix(size(), optBandWidth, optBandWidth);
             for (MatrixEntry me : mat) {
                 if (me.get() != 0) {
                     optMatrix.set(ori2opt[me.row()], ori2opt[me.column()], me.get());
                 }
             }
         }
     }
 
     public DenseVector solve(Vector b) {
 
         DenseVector optB = new DenseVector(b.size());
         for (int i = 0; i < optB.size(); i++) {
             optB.set(ori2opt[i], b.get(i));
         }
 
         Vector optVector = optMatrix.solve(optB, new DenseVector(size()));
         DenseVector result = new DenseVector(optVector.size());
         for (int i = 0; i < optVector.size(); i++) {
             result.set(opt2ori[i], optVector.get(i));
         }
         return result;
     }
 }
