 package simulation;
 
 import java.util.Random;
 import linear.MaxTotalWeightLP;
 import linear.BrendansAlg;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 /**
  *
  * @author Kairat Zhubayev
  */
 public class Driver {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Random generator = new java.util.Random();
         Vertex[] optimalGraph = null;
         MaxTotalWeightLP optimal = null;
 
         CmdLineOptions options = new CmdLineOptions();
         CmdLineParser parser = new CmdLineParser(options);
         parser.setUsageWidth(80);
 
         try {
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             System.out.println(e.getMessage());
             parser.printUsage(System.err);
             System.exit(1);
         }
 
         if (options.optimum) {
             optimal = new MaxTotalWeightLP(options.nodeNumber,
                     options.seed, options.squareSide, options.beams);
             optimal.run();
             optimalGraph = optimal.getGraph();
         }
 
        if (options.randomSeed) {
            options.seed = generator.nextInt(65536)+1;
        }

         BrendansAlg brendan = new BrendansAlg(options.nodeNumber, options.seed,
                 options.squareSide, options.beams);
         brendan.run();
         Vertex[] brendanGraph = brendan.getGraph();
 
         MSTAlgorithm mst = new MSTAlgorithm(options.nodeNumber, options.seed,
                 options.squareSide, options.beams);
         mst.buildMinimumSpanningTree();
         mst.constructNewGraph();
         Vertex[] mstGraph = mst.getNewGraph();
 
         int neighborsNumber = 3;
         KNearestNeighborsAlgorithm knn =
                 new KNearestNeighborsAlgorithm(options.nodeNumber, options.seed,
                 options.squareSide, options.neighborsNumber, options.beams);
         knn.findKNearestNeighbors();
         knn.constructNewGraph();
         Vertex[] knnGraph = knn.getNewGraph();
 
 //        PrimsBasedAlgorithm prims = new PrimsBasedAlgorithm(options.nodeNumber,
 //                options.seed, options.squareSide, options.beams);
 //        prims.run();
 //        Vertex[] primsGraph = prims.getGraph();
 
         if (options.graphs) {
             if (options.optimum) {
                 DrawRegion draw = new DrawRegion(optimalGraph,
                         options.squareSide, "The optimal graph topology",
                         optimal.getTotalWeight());
             }
             DrawRegion draw1 = new DrawRegion(brendanGraph,
                     options.squareSide, "The Brendan graph topology",
                     brendan.getTotalWeight());
             DrawRegion draw2 = new DrawRegion(mstGraph,
                     options.squareSide, "The MST-based graph topology",
                     mst.getTotalWeight());
             DrawRegion draw3 = new DrawRegion(knnGraph,
                     options.squareSide, "The k nearest neighbors-based graph topology",
                     knn.getTotalWeight());
 //            DrawRegion draw4 = new DrawRegion(primsGraph,
 //                    options.squareSide, "The Prim's-based graph topology",
 //                    prims.getTotalWeight());
         }
 
 	// Output in one line
 	// Headers first
 	String headers = "n,side,seed,sectors,neighbors,";
 	headers += "Optimal Total,Optimal Connected,Optimal Fairness (in),Optimal Fairness (out),";
 	headers += "Brendan Total,Brendan Connected,Brendan Fairness (in),Brendan Fairness (out),";
 	headers += "MinSpanTree Total,MinSpanTree Connected,MinSpanTree Fairness (in),MinSpanTree Fairness (out),";
 	headers += "K-nearest Total,K-nearest Connected,K-nearest Fairness (in),K-nearest Fairness (out),";
 	//headers += "Prim's Total,Prim's Connected,Prim's Fairness (in),Prim's Fairness (out),";
 	System.out.println(headers);
         if (options.optimum) {
             System.out.println(options.nodeNumber + ","
                     + options.squareSide + ","
                     + options.seed + ","
                     + options.beams + ","
                     + options.neighborsNumber + ","
                     + optimal.getTotalWeight() + ","
                     + Utilities.checkForConnectivity(optimalGraph) + ","
                     + Utilities.inFairness(optimalGraph) + ","
                     + Utilities.outFairness(optimalGraph) + ","
                     + brendan.getTotalWeight() + ","
                     + Utilities.checkForConnectivity(brendanGraph) + ","
                     + Utilities.inFairness(brendanGraph) + ","
                     + Utilities.outFairness(brendanGraph) + ","
                     + mst.getTotalWeight() + ","
                     + Utilities.checkForConnectivity(mstGraph) + ","
                     + Utilities.inFairness(mstGraph) + ","
                     + Utilities.outFairness(mstGraph) + ","
                     + knn.getTotalWeight() + ","
                     + Utilities.checkForConnectivity(knnGraph) + ","
                     + Utilities.inFairness(knnGraph) + ","
                     + Utilities.outFairness(knnGraph));
 //                    + prims.getTotalWeight() + ","
 //                    + Utilities.checkForConnectivity(primsGraph) + ","
 //                    + Utilities.inFairness(primsGraph) + ","
 //                    + Utilities.outFairness(primsGraph));
         } else {
             System.out.println(options.nodeNumber + ","
                     + options.squareSide + ","
                     + options.seed + ","
                     + options.beams + ","
                     + options.neighborsNumber + ","
                     + "Not Run" + ","
                     + "Not Run" + ","
                     + "Not Run" + ","
                     + "Not Run" + ","
                     + brendan.getTotalWeight() + ","
                     + Utilities.checkForConnectivity(brendanGraph) + ","
                     + Utilities.inFairness(brendanGraph) + ","
                     + Utilities.outFairness(brendanGraph) + ","
                     + mst.getTotalWeight() + ","
                     + Utilities.checkForConnectivity(mstGraph) + ","
                     + Utilities.inFairness(mstGraph) + ","
                     + Utilities.outFairness(mstGraph) + ","
                     + knn.getTotalWeight() + ","
                     + Utilities.checkForConnectivity(knnGraph) + ","
                     + Utilities.inFairness(knnGraph) + ","
                     + Utilities.outFairness(knnGraph));
 //                    + prims.getTotalWeight() + ","
 //                    + Utilities.checkForConnectivity(primsGraph) + ","
 //                    + Utilities.inFairness(primsGraph) + ","
 //                    + Utilities.outFairness(primsGraph));
         }
     }
 }
