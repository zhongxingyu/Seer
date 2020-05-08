 package rgbdslam;
 
 import kinect.*;
 
 import april.jmat.*;
 import april.jmat.geom.*;
 
 import java.lang.Double;
 import java.util.ArrayList;
 import rgbdslam.KdTree.Entry;
 
 /* ICP does no internal downsampling so it assumes that it has been given a
  * down sampled point cloud, using all points takes about 1 second for initialization
  * and 17 seconds to match, using decimation of 10, takes 0.002 seconds to construct
  * and 0.04 seconds to match
  */
 public class ICP {
     // XXX No clue if these are the right values
 
     final static int MAX_ITERATIONS = 50; // maximum number of iteration for ICP
    final static double THRESHOLD = 0.0005; // threshold for convergence change in normalized error
    final static double DISCARD_D = .5; // threshold for outlier rejection
     final static double ALPHA = 0.75; // relative weighting between initial estimate and new rbt estimate
     private KdTree.SqrEuclid<double[]> kdtree; // kdtree for storing points in B
 
     // Constructs ICP iterator which can be called to allign ColorPointClouds with this one
     public ICP(ColorPointCloud cpcB) {
 
         ArrayList<double[]> PB = cpcB.points;
 
         if (PB.size() > 0) {
             kdtree = new KdTree.SqrEuclid<double[]>(3, PB.size()); // points are in 3 space
             // add points
             for (double[] P : PB) {
                 kdtree.addPoint(P, P); // since our points are simply points in 3D space
                 // we are sorting based on their location in 3D space and the point is what we want back
             }
         } else {
             kdtree = new KdTree.SqrEuclid<double[]>(3, 0); // max size of 0
         }
     }
 
     // XXX alternate constructor that uses our Voxel represenation for B ?
     // refines initial estimate of an RBT that alligns points in A with points in B
     // in frame B
     public double[][] match(ColorPointCloud cpcA, double[][] Irbt) {
         int cntr = 0;
         double curerror = Double.MAX_VALUE / 2; // these values will represent an average error
         double preverror = Double.MAX_VALUE;
 
         double[][] rbt = Irbt; // rbt is our best guess for the rbt so far
 
         // get points stored in colored point clouds
         ArrayList<double[]> PA = cpcA.points;
 
         // compute an inital for the given rbt
         // apply rbt to points in A to get into frame B
         ArrayList<double[]> PAinB = LinAlg.transform(Irbt, PA);
         // good correspondance points
         ArrayList<double[]> GoodA = new ArrayList<double[]>();
         ArrayList<double[]> GoodB = new ArrayList<double[]>();
         double totalError = 0; // for accumulating error
         // for each transformed point
         for (int index = 0; index < PAinB.size(); index++) {
             // find nearest point in B for each in A using K-D tree
             // wierd K-D tree object
             Entry<double[]> BE = kdtree.nearestNeighbor(PAinB.get(index), 1, false).get(0);
             double[] B = BE.value;
             // compute distance between A and B
             //double d = LinAlg.distance(A,B);
             double d = BE.distance;
             // if distance is small enough, then not an outlier
             // need this to handle parts that will never overlap  
             if (d < DISCARD_D) {
                 // add both to our list of good points
                 GoodA.add(PA.get(index));
                 GoodB.add(B);
                 // add distance to our error estimate
                 totalError = totalError + d;
             }
         }
         curerror = totalError / GoodA.size(); // how good was the initial guess
 
         // itterate until change in error becomes small or reach max iterations
         while (((preverror - curerror) > THRESHOLD) && (cntr < MAX_ITERATIONS)) {
             // use these lists to compute updated RBT
             // http://www.cs.duke.edu/courses/spring07/cps296.2/scribe_notes/lecture24.pdf
             // this seems to do what I need
             double[][] Erbt = AlignPoints3D.align(GoodA, GoodB);
             //double[][] Erbt = weightedSum(AlignPoints3D.align(GoodB, GoodA), rbt, ALPHA);
 
             // apply rbt to points in A to get into frame B
             PAinB = LinAlg.transform(Erbt, PA);
 
             // good correspondance points
             GoodA = new ArrayList<double[]>();
             GoodB = new ArrayList<double[]>();
 
             totalError = 0; // for accumulating error
 
             // for each transformed point
             for (int index = 0; index < PAinB.size(); index++) {
 
                 // find nearest point in B for each in A using K-D tree
                 // wierd K-D tree object
                 Entry<double[]> BE = kdtree.nearestNeighbor(PAinB.get(index), 1, false).get(0);
                 double[] B = BE.value;
 
                 // compute distance between A and B
                 //double d = LinAlg.distance(A,B);
                 double d = BE.distance;
 
                 // if distance is small enough, then not an outlier
                 // need this to handle parts that will never overlap
 
                 if (d < DISCARD_D) {
                     // add both to our list of good points
                     GoodA.add(PA.get(index));
                     GoodB.add(B);
                     // add distance to our error estimate
                     totalError = totalError + d;
                 }
 
             }
 
             // reassign errors
             preverror = curerror;
             //System.out.println("Preverror = " + preverror);
             // XXX this error is actually the error for the previous RBT, since we are looping it might be ok, but for
             // small numbers of iterrations this might be a problem
             curerror = totalError / GoodA.size(); // maintaining an average error
             //System.out.println("Curerror = " + curerror);
 
             // use this new estimate if error has improved
             if (curerror < preverror) {
             rbt = Erbt; // new best guess if we got better
             }
 
             cntr++;
         }
         //System.out.println("Performed " + cntr + " Iterations.");
         //System.out.println("Normalized Error Change: " + (preverror - curerror));
         return rbt;
     }
 
     // weight A by alpha and B by 1-alpha
     // if alpha == 1 then returns A
     private double[][] weightedSum(double[][] A, double[][] B, double alpha) {
 
         assert ((A.length == B.length) && (A[0].length == B[0].length) && (alpha <= 1) && (alpha >= 0));
 
         double[][] C = new double[A.length][A[0].length];
         for (int i = 0; i < A.length; i++) {
             for (int j = 0; j < A[0].length; j++) {
                 C[i][j] = alpha * A[i][j] + (1 - alpha) * B[i][j];
             }
         }
 
         return C;
     }
 }
