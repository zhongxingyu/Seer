 package linear;
 
 import ilog.concert.*;
 import ilog.cplex.*;
 import ilog.cplex.IloCplex.UnknownObjectException;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import simulation.*;
 
 /**
  *
  * @author Brendan Mumey
  */
 public class BrendansAlg {
 
     private int nodeNumber;
     private double squareSide;
     private int beams;
     private int seed;
     private Throughput throughput;
     private Vertex vertices[];        // The array of vertices
     private double totalWeight;
     private double cplexTotal;
     static double threshold = Math.pow(10, 6);
     double c[][][];
     int sector[][];
     static int sourceVertex = 0;
     IloCplex cplex;
     IloNumVar[][] s;
     IloNumVar[][][] x;
     private int sectorUsed[][]; // to store the correct sector used info
     private int[] component;
 
     // Constructor
     public BrendansAlg(int nodeNumber, int seed, double squareSide, int beams) {
         this.nodeNumber = nodeNumber;
         this.squareSide = squareSide;
         this.beams = beams;
         this.seed = seed;
         this.throughput = new Throughput(beams);
         loadGraph();
     }
 
     /**
      * Randomly distribute points in the square.
      */
     public void loadGraph() {
         // initialize an array
         vertices = Utilities.buildRandomGraph(nodeNumber, squareSide, seed);
     }
 
     private void setUpModel() {
         // c[u][v][j] = transmission rates from u to v, assuming u uses exactly j antenna sectors
         //System.out.println("C values");
         c = new double[nodeNumber][nodeNumber][beams + 1];
         for (int i = 0; i < nodeNumber; i++) {
             for (int j = 0; j < nodeNumber; j++) {
                 if (i == j) // the same node
                 {
                     continue;
                 }
 
                 double distance = vertices[i].point.distance(vertices[j].point);
                 for (int k = 1; k < beams + 1; k++) {
                     c[i][j][k] = throughput.calculateThroughput(k, distance);
                     //System.out.println((i+1)+" "+(j+1)+" "+k+" "+c[i][j][k]);
                 }
             }
         }
 
         // sector[u][v]: the sector of u that node v falls in
         //System.out.println("S values");
         sector = new int[nodeNumber][nodeNumber];
         for (int i = 0; i < nodeNumber; i++) {
             for (int j = 0; j < nodeNumber; j++) {
                 if (i == j) {
                     continue;
                 }
                 sector[i][j] = vertices[i].point.beamIndex(beams, vertices[j].point);
                 //System.out.println((i + 1) + " " + (j + 1) + " " + sector[i][j]);
             }
         }
     }
 
     /**
      * The ILP formulation
      */
     public void runModel() {
         // create an ILP
         try {
            if (cplex == null) {
                cplex = new IloCplex();
            }

            cplex.clearModel();
 
 
 
             // x[u][v][j]: u transits v and uses exactly j antenna sectors
             x = new IloNumVar[nodeNumber][nodeNumber][beams + 1];
             for (int u = 0; u < nodeNumber; u++) {
                 for (int v = 0; v < nodeNumber; v++) {
                     for (int j = 1; j < beams + 1; j++) {
                         // I. 0 <= x[u][v][j] <= 1
                         if (u != v) {
                             x[u][v][j] = cplex.numVar(0, 1, "x(" + u + ")(" + v + ")(" + j + ")");
                         }
 
                         // if c[u][v][j] = 0, set x[u][v][j] to zero
                         if (c[u][v][j] < threshold) {
                             cplex.addEq(0, x[u][v][j]);
                         }
                     }
                 }
             }
 
             // s[u][k]: u uses antenna sector k
             s = new IloNumVar[nodeNumber][beams + 1];
             for (int u = 0; u < nodeNumber; u++) {
                 for (int k = 1; k < beams + 1; k++) {
                     // II. 0 <= s[u][k] <= 1
                     s[u][k] = cplex.numVar(0, 1, "s(" + u + ")(" + k + ")");
                 }
             }
 
             // add sector used constraints:
             for (int v = 0; v < nodeNumber; v++) {
                 for (int k = 1; k < beams + 1; k++) {
                     if (sectorUsed[v][k] == 0) {
                         cplex.addEq(0, s[v][k]);
                     } else if (sectorUsed[v][k] > 0) {
                         cplex.addEq(1, s[v][k]);
                     }
                 }
             }
 
 
 
             // f[u][v]: the amount of flow on the edge (u, v)
             IloNumVar[][] f = new IloNumVar[nodeNumber][nodeNumber];
             for (int u = 0; u < nodeNumber; u++) {
                 for (int v = 0; v < nodeNumber; v++) {
                     if (u != v) {
                         f[u][v] = cplex.numVar(0, 1, "f(" + u + ")(" + v + ")");
                     }
                 }
             }
 
             // constraints III, IV, and VI
             for (int u = 0; u < nodeNumber; u++) {
                 for (int v = 0; v < nodeNumber; v++) {
                     if (u == v || c[u][v][1] < threshold) {
                         continue;
                     }
 
                     // expr = sum x[u][v] over j
                     IloLinearNumExpr expr = cplex.linearNumExpr();
                     for (int j = 1; j < beams + 1; j++) {
                         // IV. x[u][v][j] <= s[u][sector(u, v)]
                         int sectorUtoV = sector[u][v];
                         cplex.addLe(x[u][v][j], s[u][sectorUtoV]);
 
                         expr.addTerm(1, x[u][v][j]);
                     }
 
                     // III. sum x[u][v] over j <= 1
                     cplex.addLe(expr, 1);
 
                     // VI. f[u][v] <= sum x[u][v] over j
                     IloLinearNumExpr expr2 = cplex.linearNumExpr();
                     expr2.addTerm(1.0 / (nodeNumber), f[u][v]);
                     cplex.addLe(expr2, expr);
                 }
             }
 
             // Brendan added new approach to constraint 5:
             IloNumVar[][] z = new IloNumVar[nodeNumber][beams + 1];
             for (int v = 0; v < nodeNumber; v++) {
                 for (int j = 1; j < beams + 1; j++) {
                     z[v][j] = cplex.numVar(0, Double.MAX_VALUE, "z(" + v + ")(" + j + ")");
                 }
             }
             //int xx = 1;
             for (int i = 0; i < 256; i++) {
                 int j = i;
                 int k = 1;
                 int n = 0;
                 int[] element = new int[8];
                 while (j > 0) {
                     if ((j % 2) == 1) {
                         element[n++] = k;
                     }
                     j /= 2;
                     k++;
                 }
                 if (n > 1) {
                     for (int v = 0; v < nodeNumber; v++) {
                         IloLinearNumExpr rhs = cplex.linearNumExpr(n);
                         for (int l = 0; l < n; l++) {
                             rhs.addTerm(-1, s[v][element[l]]);
                         }
                         cplex.addLe(z[v][n - 1], rhs);
                     }
                 }
             }
             for (int u = 0; u < nodeNumber; u++) {
                 for (int v = 0; v < nodeNumber; v++) {
                     if (u == v || c[u][v][1] < threshold) {
                         continue;
                     }
                     for (int j = 1; j < beams + 1; j++) {
                         cplex.addLe(x[u][v][j], z[u][j]);
                     }
                 }
             }
 
             // VII. vF[v] <= 1/n, n = |V|
             double vFAmount = 1.0 / nodeNumber;
 
             // VIII. sum f[s][v] over v - sum f[v][s] over v = 1 (Brendan revised)
             IloLinearNumExpr sourceFlow = cplex.linearNumExpr(vFAmount);
             for (int v = 0; v < nodeNumber; v++) {
                 if (v == sourceVertex) {
                     continue;
                 }
 
                 if (c[sourceVertex][v][1] >= threshold) {
                     sourceFlow.addTerm(1, f[sourceVertex][v]);
                 }
 
                 if (c[v][sourceVertex][1] >= threshold) {
                     sourceFlow.addTerm(-1, f[v][sourceVertex]);
                 }
             }
             cplex.addEq(1, sourceFlow);
 
             // sum f[u][v] over u = sum f[v][w] over w
             for (int v = 0; v < nodeNumber; v++) {
                 if (v == sourceVertex) {
                     continue;
                 }
 
                 IloLinearNumExpr incFlow = cplex.linearNumExpr();
                 for (int u = 0; u < nodeNumber; u++) {
                     if (u != v && c[u][v][1] >= threshold) {
                         incFlow.addTerm(1, f[u][v]);
                     }
                 }
 
                 IloLinearNumExpr outFlow = cplex.linearNumExpr(vFAmount);
                 for (int w = 0; w < nodeNumber; w++) {
                     if (w != v && c[v][w][1] >= threshold) {
                         outFlow.addTerm(1, f[v][w]);
                     }
                 }
                 //outFlow.addTerm(1, vF[v]);
 
                 cplex.addEq(incFlow, outFlow);
             }
 
             // new constaint added on the meeting: no unidirectional links
             for (int u = 0; u < nodeNumber - 1; u++) {
                 for (int v = u + 1; v < nodeNumber; v++) {
                     IloLinearNumExpr sumUtoV = cplex.linearNumExpr();
                     IloLinearNumExpr sumVtoU = cplex.linearNumExpr();
                     for (int j = 1; j < beams + 1; j++) {
                         sumUtoV.addTerm(1, x[u][v][j]);
                         sumVtoU.addTerm(1, x[v][u][j]);
                     }
 
                     // sum x[u][v] over j = sum x[v][u] over j
                     cplex.addEq(sumUtoV, sumVtoU);
                 }
             }
 
 
             // maximize the following expression: sum c[u][v][j] * x[u][v][j]
             IloLinearNumExpr maximizeExpr = cplex.linearNumExpr();
             for (int u = 0; u < nodeNumber; u++) {
                 for (int v = 0; v < nodeNumber; v++) {
                     if (u == v) {
                         continue;
                     }
                     for (int j = 1; j < beams + 1; j++) {
                         //if(c[u][v][1] >= threshold)
                         maximizeExpr.addTerm(c[u][v][j], x[u][v][j]);
                     }
                 }
             }
 
             // solve the problem
             IloObjective obj = cplex.maximize(maximizeExpr);
             cplex.add(obj);
             cplex.setOut(null);
             //cplex.exportModel("Brendan.lp");
             cplex.solve();
             cplexTotal = cplex.getObjValue();
             //System.out.println("Max total weight LP = " + cplexTotal);
             //System.out.println("Solution status = " + cplex.getStatus());
         } catch (IloException ex) {
             ex.printStackTrace();
         }
         Runtime r = Runtime.getRuntime();
         r.gc();
     }
 
     private void selectNextEdge() {
         int bestU = 0, bestV = 0;
         double bestWeight = -1.0;
         double[][] weight = new double[nodeNumber][nodeNumber];
         for (int u = 0; u < nodeNumber; u++) {
             for (int v = 0; v < nodeNumber; v++) {
                 if (u == v) {
                     continue;
                 }
                 weight[u][v] = 0.0;
                 for (int k = 1; k < beams + 1; k++) {
                     try {
                         double xval = cplex.getValue(x[u][v][k]);
                         weight[u][v] += xval * c[u][v][k];
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
                 if (weight[u][v] > bestWeight && component[u] != component[v]) {
                     bestWeight = weight[u][v];
                     bestU = u;
                     bestV = v;
                 }
             }
         }
         // merge components by selecting (u,v):
         if (component[bestU] < component[bestV]) {
             int old = component[bestV];
             for (int i = 0; i < nodeNumber; i++) {
                 if (component[i] == old) {
                     component[i] = component[bestU];
                 }
             }
         } else {
             int old = component[bestU];
             for (int i = 0; i < nodeNumber; i++) {
                 if (component[i] == old) {
                     component[i] = component[bestV];
                 }
             }
         }
         sectorUsed[bestU][sector[bestU][bestV]] = 1;
         sectorUsed[bestV][sector[bestV][bestU]] = 1;
     }
 
     public void run() {
         setUpModel();
 
         sectorUsed = new int[nodeNumber][beams + 1];
         for (int i = 0; i < nodeNumber; i++) {
             for (int j = 0; j < beams + 1; j++) {
                 sectorUsed[i][j] = -1; // default: free during phase 1
             }
         }
 
         // PHASE 1
         component = new int[nodeNumber];
         for (int i = 0; i < nodeNumber; i++) {
             component[i] = i;
         }
         for (int e = 0; e < nodeNumber - 1; e++) {
             runModel();
             selectNextEdge();
         }
 
         // find base soln for PHASE 2:
         for (int i = 0; i < nodeNumber; i++) {
             for (int j = 0; j < beams + 1; j++) {
                 if (sectorUsed[i][j] == -1) {
                     sectorUsed[i][j] = 0;
                 }
             }
         }
         runModel();
         double curObj = cplexTotal;
 
         // PHASE 2
 
         boolean improvement = true;
         while (improvement) {
             improvement = false;
             for (int u = 0; u < nodeNumber; u++) {
                 for (int v = 0; v < nodeNumber; v++) {
                     if (u == v) {
                         continue;
                     }
                     int usec = sector[u][v];
                     int vsec = sector[v][u];
                     if (sectorUsed[u][usec] == 0 || sectorUsed[v][vsec] == 0) {
                         sectorUsed[u][usec]++;
                         sectorUsed[v][vsec]++;
 //                        System.out.println("testing pair (" + u + "," + usec + "), (" + v + "," + vsec + ")..");
                         runModel();
                         if (cplexTotal > curObj) {
                             curObj = cplexTotal;
                             improvement = true;
                         } else {
                             sectorUsed[u][usec]--;
                             sectorUsed[v][vsec]--;
                         }
                     }
                 }
             }
         }
         runModel(); // restore values
 
         // figure out the antenna pattern assignment
         for (int u = 0; u < nodeNumber; u++) {
             vertices[u].activeBeams = new boolean[beams + 1];
             vertices[u].beamsUsedNumber = 0;
 
             for (int k = 1; k < beams + 1; k++) {
                 if (sectorUsed[u][k] > 0) {
                     vertices[u].activeBeams[k] = true;
                     vertices[u].beamsUsedNumber++;
                 }
             }
         }
 
         // build the topology
         totalWeight = 0;
         for (int i = 0; i < nodeNumber - 1; i++) {
             for (int j = i + 1; j < nodeNumber; j++) {
                 //double weightItoJ = throughput.calculateThroughput(vertices[i].beamsUsedNumber, dist);
                 try {
                     //int index1 = vertices[i].point.beamIndex(beams, vertices[j].point);
                     int index1 = sector[i][j];
                     if (!vertices[i].activeBeams[index1]) {
                         continue;
                     } //int index2 = vertices[j].point.beamIndex(beams, vertices[i].point);
                     int index2 = sector[j][i];
                     if (!vertices[j].activeBeams[index2]) {
                         continue;
                     } //double dist = vertices[i].point.distance(vertices[j].point);
                     //double weightItoJ = throughput.calculateThroughput(vertices[i].beamsUsedNumber, dist);
                     double weightItoJ = c[i][j][vertices[i].beamsUsedNumber];
                     if (weightItoJ < threshold) {
                         continue;
                     } //double weightJtoI = throughput.calculateThroughput(vertices[j].beamsUsedNumber, dist);
                     double weightJtoI = c[j][i][vertices[j].beamsUsedNumber];
                     if (weightJtoI < threshold) {
                         continue;
                     }
                     ListElement elem1 = new ListElement(j, weightItoJ);
                     vertices[i].vertices.add(elem1);
                     // create a new list element and add it to vertix j's list
                     ListElement elem2 = new ListElement(i, weightJtoI);
                     vertices[j].vertices.add(elem2);
                     // Update local throughputs
                     double i_to_j = weightItoJ * cplex.getValue(x[i][j][vertices[i].beamsUsedNumber]);
                     double j_to_i = weightJtoI * cplex.getValue(x[j][i][vertices[j].beamsUsedNumber]);
                     vertices[i].outThroughput += i_to_j;
                     vertices[i].inThroughput += j_to_i;
                     vertices[j].outThroughput += j_to_i;
                     vertices[j].inThroughput += i_to_j;
                     // update total weight
                     totalWeight += weightItoJ + weightJtoI;
                 } catch (UnknownObjectException ex) {
                     Logger.getLogger(BrendansAlg.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IloException ex) {
                     Logger.getLogger(BrendansAlg.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     public double getTotalWeight() {
         return totalWeight;
 
 
     }
 
     public double getCplexTotal() {
         return cplexTotal;
 
 
     }
 
     public Vertex[] getGraph() {
         return vertices;
 
     }
 }
