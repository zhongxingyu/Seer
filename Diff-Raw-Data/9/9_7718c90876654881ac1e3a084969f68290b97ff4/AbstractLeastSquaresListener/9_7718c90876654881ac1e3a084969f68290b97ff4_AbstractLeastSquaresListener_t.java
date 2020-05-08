 package april.sim;
 
 import java.awt.*;
 import java.util.*;
 
 import april.vis.*;
 import april.jmat.*;
 import april.util.*;
 import april.config.*;
 import april.sim.Simulator.odometry_t;
 import team.*;
 
 /**
  *
  * @author pdaquino
  */
 public abstract class AbstractLeastSquaresListener implements Simulator.Listener {
 
     private VisWorld vw;
     protected Config config;
     protected double baseline; // Robot baseline in [m]
     protected ArrayList<Node> nodes = new ArrayList<Node>();
     protected ArrayList<Edge> edges = new ArrayList<Edge>();
     protected RobotPose latestRobotPose;
     protected int currentStateVectorSize = 0;
     protected double xyt[] = new double[3]; // Best guess of our most recent pose
     protected ArrayList<double[]> trajectory = new ArrayList<double[]>();
     protected double chi2 = 500;
     private ArrayList<double[]> true_landmarks = new ArrayList<double[]>();
 
     public void init(Config config_, VisWorld vw_) {
         config = config_;
         vw = vw_;
 
         baseline = config.requireDouble("robot.baseline_m");
 
         latestRobotPose = new RobotPose(0);
         latestRobotPose.setPosition(new double[]{0, 0, 0});
         nodes.add(latestRobotPose);
         currentStateVectorSize += latestRobotPose.getNumDimensions();
 
         // Initialize robot position & landmarks
         for (int i = 0;; i++) {
             double[] li = config.getDoubles("landmarks.l" + i, null);
             if (li == null) {
                 break;
             }
             true_landmarks.add(LinAlg.resize(li, 3));
         }
 
         //edges.add(new ConstraintEdge(latestRobotPose, new double[3]));
     }
 
     public double getChi2() {
         return chi2;
     }
 
     private double chi2(Matrix deltaX, Matrix JTSigmaJ, Matrix JTSigmar, Matrix Sigma, Matrix r) {
         Matrix deltaXT = deltaX.transpose();
         double chi2 = deltaXT.times(JTSigmaJ).times(deltaX).get(0)
                 - 2 * deltaXT.times(JTSigmar).get(0)
                 + r.transpose().times(Sigma).times(r).get(0);
         // Normalize
        //System.out.printf("%d==%d\n", getNumJacobianRows(), currentStateVectorSize);
        int DOF = getNumJacobianRows() - currentStateVectorSize;    // # degrees of freedom
        //System.out.printf("%f--%d\n", chi2, DOF);
        if (DOF > 0)
            chi2 /= DOF;
         return chi2;
     }
 
     protected void addOdometryEdge(odometry_t odom) {
         RobotPose newRobotPose = new RobotPose(currentStateVectorSize);
         OdometryEdge odomEdge = new OdometryEdge(config, odom.obs[0], odom.obs[1],
                 baseline, latestRobotPose, newRobotPose);
         nodes.add(newRobotPose);
         currentStateVectorSize += newRobotPose.getNumDimensions();
         edges.add(odomEdge);
         this.latestRobotPose = newRobotPose;
     }
 
     protected Matrix[] buildJTSigmaJ() {
         StopWatch stopWatch = new StopWatch("Building JTSigmaJ");
         int numStates = getStateVectorSize();
         int numRows = getNumJacobianRows();
         Matrix J = new Matrix(numRows, numStates, Matrix.SPARSE);
         Matrix Sigma = new Matrix(numRows, numRows);
         int jRowCount = 0;
 
         //Matrix sum0 = new Matrix(numStates, numStates, Matrix.SPARSE);  // JTSigmaJ
         //Matrix sum1 = new Matrix(numStates, numRows, Matrix.SPARSE);    // JTSigma
 
         long timeGetJ = 0, timeCovInv = 0;
         int sigmaRowCount = 0;
         for (Edge e : edges) {
             StopWatch iterStopWatch = new StopWatch();
             // Get J rows...(Ji) as a matrix
             iterStopWatch.start();
             Matrix edgeJ = e.getJacobian(numStates);
             iterStopWatch.stop();
             timeGetJ += iterStopWatch.getLastTaskTimeMillis();
 
             for (int i = 0; i < edgeJ.getRowDimension(); i++) {        // sum1 = JTSigma1
                 J.setRow(jRowCount, edgeJ.getRow(i));
                 jRowCount++;
             }
 
             // Get covariance matrix
             iterStopWatch.start();
             Matrix edgeSigma = e.getCovarianceInverse(sigmaRowCount, numRows);
             for (int i = 0; i < edgeSigma.getRowDimension(); i++) {
                 Sigma.setRow(sigmaRowCount++, edgeSigma.getRow(i));
             }
             iterStopWatch.stop();
             timeCovInv += iterStopWatch.getLastTaskTimeMillis();
         }
 
         stopWatch.addTask(new StopWatch.TaskInfo("Building J", timeGetJ));
         stopWatch.addTask(new StopWatch.TaskInfo("Weight matrix", timeCovInv));
 
         // sum0 = JTSigmaJ
         stopWatch.start("Doing JT");
         Matrix JT = J.transpose();
         stopWatch.stop();
         stopWatch.start("JT*Sigma");
         Matrix JTSigma = JT.times(Sigma);
         stopWatch.stop();
         stopWatch.start("JT*Sigma*J");
         Matrix JTSigmaJ = JTSigma.times(J);
         stopWatch.stop();
         printTiming(stopWatch.prettyPrint());
 
         return new Matrix[]{JTSigmaJ, JTSigma, Sigma};
     }
 
     protected int getStateVectorSize() {
         return currentStateVectorSize;
     }
 
     protected int getNumJacobianRows() {
         int size = 0;
         for (Edge e : edges) {
             size += e.getNumberJacobianRows();
         }
         return size;
     }
 
     protected double[] buildResidual() {
         //Matrix residual = new Matrix(getNumJacobianRows(), 1, Matrix.DENSE);
         double[] residual = new double[getNumJacobianRows()];
         int currentRow = 0;
         double mse = 0;
         for (Edge e : edges) {
             double[] edgeResidual = e.getResidual();
             for (double r : edgeResidual) {
                 //residual.set(currentRow++, 0, r);
                 residual[currentRow++] = r;
                 mse += r * r;
             }
         }
         // Output mean square error
         //System.out.printf("MSE: %f\n", mse);
         // Chi^2 error?
         return residual;
     }
 
     protected void doLeastSquaresUpdate(int maxIterations, ArrayList<LandmarkPose> recentLmarks) {
         StopWatch stopWatch = new StopWatch();
         final double alpha = 1000;
         Matrix I = Matrix.identity(currentStateVectorSize, currentStateVectorSize).times(alpha);
         final double kMinChi2Improvement = 0.005;
         long timeJTSigmar = 0, timeCholesky = 0, timeDeltaX = 0, timeChi2 = 0;
         double lastChi2 = 500, chi2Diff = kMinChi2Improvement + 1;
         int countIter = 0;
 
         //while (chi2Diff > kMinChi2Improvement && countIter++ < maxIterations) {
         int ITERS = config.requireInt("iters.iters");;
         for (int iter = 0; iter < ITERS; iter++) {
             Matrix[] matrices = buildJTSigmaJ();
             Matrix Sigma = matrices[2];
             Matrix Tikhonov = matrices[0].plus(I);
 
             StopWatch iterStopWatch = new StopWatch();
 
             iterStopWatch.start();
             Matrix r = Matrix.columnMatrix(buildResidual());
             Matrix JTSigmar = matrices[1].times(r);
             iterStopWatch.stop();
             timeJTSigmar += iterStopWatch.getLastTaskTimeMillis();
 
             iterStopWatch.start();
             CholeskyDecomposition solver = new CholeskyDecomposition(Tikhonov);
             Matrix deltaX = solver.solve(JTSigmar);
             iterStopWatch.stop();
             timeCholesky += iterStopWatch.getLastTaskTimeMillis();
 
             iterStopWatch.start();
             // Draw our trajectory and detections
             double[] stateVector = getStateVector();
             for (int i = 0; i < stateVector.length; i++) {
                 stateVector[i] += deltaX.get(i, 0);
             }
             updateNodesPosition(stateVector);
             iterStopWatch.getLastTaskTimeMillis();
             iterStopWatch.stop();
             timeDeltaX += iterStopWatch.getLastTaskTimeMillis();
 
             // chi2 error
             iterStopWatch.start();
             double chi2 = chi2(deltaX, Tikhonov, JTSigmar, Sigma, r);
             chi2Diff = lastChi2 - chi2;
             lastChi2 = chi2;
             //System.out.println(chi2 + "(" + chi2Diff + ")");
             iterStopWatch.stop();
             timeChi2 += iterStopWatch.getLastTaskTimeMillis();
             if (recentLmarks != null) {
                 drawStuff(recentLmarks);
             }
         }
         stopWatch.addTask(new StopWatch.TaskInfo("Doing JT*Sigma*R", timeJTSigmar));
         stopWatch.addTask(new StopWatch.TaskInfo("Solving the system", timeCholesky));
         stopWatch.addTask(new StopWatch.TaskInfo("Updating the state", timeDeltaX));
         stopWatch.addTask(new StopWatch.TaskInfo(("Calculating Chi2"), timeChi2));
         this.chi2 = lastChi2;
         xyt = this.latestRobotPose.getPosition();
         stopWatch.start("Drawing stuff on the screen");
         if (recentLmarks != null) {
             drawStuff(recentLmarks);
         }
         stopWatch.stop();
         printTiming(stopWatch.prettyPrint());
         System.out.println(getChi2());
     }
 
     protected double[] getStateVector() {
         double[] stateVector = new double[getStateVectorSize()];
         for (Node n : nodes) {
             double[] position = n.getPosition();
             int idx = n.getIndex();
             for (int i = 0; i < position.length; i++) {
                 stateVector[idx + i] = position[i];
             }
         }
         return stateVector;
     }
 
     protected void updateNodesPosition(double[] updatedStateVector) {
         for (Node n : nodes) {
             double[] position = new double[n.getNumDimensions()];
             int idx = n.getIndex();
             for (int i = 0; i < position.length; i++) {
                 position[i] = updatedStateVector[idx + i];
             }
             n.setPosition(position);
         }
     }
 
     protected void drawStuff(ArrayList<LandmarkPose> recentLmarks) {
         ArrayList<double[]> rpoints = new ArrayList<double[]>();
         rpoints.add(new double[]{-.3, .3});
         rpoints.add(new double[]{-.3, -.3});
         rpoints.add(new double[]{.45, 0});
 
         trajectory.clear();
 
         // Draw estimated trajectory based on our state
         for (Node n : nodes) {
             if (!(n instanceof RobotPose)) {
                 continue;
             }
             trajectory.add(LinAlg.resize(n.getPosition(), 2));
         }
 
         {
             VisWorld.Buffer vb = vw.getBuffer("trajectory-local");
             vb.addBack(new VisLines(new VisVertexData(trajectory),
                     new VisConstantColor(new Color(160, 30, 30)),
                     1.5, VisLines.TYPE.LINE_STRIP));
             vb.swap();
         }
 
         // Draw estimated robot position
         {
             VisWorld.Buffer vb = vw.getBuffer("robot-local");
             VisObject robot = new VisLines(new VisVertexData(rpoints),
                     new VisConstantColor(Color.red),
                     3,
                     VisLines.TYPE.LINE_LOOP);
            xyt = this.latestRobotPose.getPosition();
             double xyzrpy[] = new double[]{xyt[0], xyt[1], 0,
                 0, 0, xyt[2]};
             vb.addBack(new VisChain(LinAlg.xyzrpyToMatrix(xyzrpy), robot));
             vb.swap();
         }
 
         // Draw landmark observations from this run
        /* {
         VisWorld.Buffer vb = vw.getBuffer("landmarks-noisy");
         for (LandmarkPose lmark : recentLmarks) {
         double[] xy = lmark.getPosition();
         ArrayList<double[]> obsPoints = new ArrayList<double[]>();
         obsPoints.add(LinAlg.resize(xyt, 2));
         obsPoints.add(xy);
         vb.addBack(new VisLines(new VisVertexData(obsPoints),
         new VisConstantColor(Color.cyan), 2, VisLines.TYPE.LINE_STRIP));
         //new VisConstantColor(lmark.id == -1 ? Color.gray : Color.cyan), 2, VisLines.TYPE.LINE_STRIP));
         }
         vb.swap();
         }
          */
         // Draw current esimated landmark positions
         {
             VisWorld.Buffer vb = vw.getBuffer("estimated-landmarks");
             ArrayList<double[]> points = new ArrayList<double[]>();
             // lines between the estimated position and the actual landmark
 
             for (Node node : this.nodes) {
                 if (node instanceof LandmarkPose) {
                     double[] nodePos = node.getPosition();
                     points.add(nodePos);
                 }
             }
             vb.addBack(new VisPoints(new VisVertexData(points),
                     new VisConstantColor(Color.red),
                     5));
             vb.swap();
         }
 
         // Draw lines between the estimated position and the actual landmark
         {
             VisWorld.Buffer vb = vw.getBuffer("landmarks-errors");
             vb.setDrawOrder(-3);
             ArrayList<ArrayList<double[]>> lmarkErrorLines = new ArrayList<ArrayList<double[]>>();
             for (Node node : this.nodes) {
                 if (node instanceof LandmarkPose) {
                     ArrayList<double[]> lineEndpoints = new ArrayList<double[]>();
                     lineEndpoints.add(node.getPosition());
                     lineEndpoints.add(true_landmarks.get(((LandmarkPose) node).getId()));
                     lmarkErrorLines.add(lineEndpoints);
                 }
             }
             for (ArrayList<double[]> line : lmarkErrorLines) {
                 vb.addBack(new VisLines(new VisVertexData(line),
                         new VisConstantColor(Color.white), 2, VisLines.TYPE.LINE_STRIP));
             }
             vb.swap();
         }
 
         // Draw the landmark edges
         {
             VisWorld.Buffer vb = vw.getBuffer("landmarks-edges");
             vb.setDrawOrder(-10);
             ArrayList<double[]> robotPoints = new ArrayList<double[]>(edges.size());
             for (Edge e : edges) {
                 if (!(e instanceof LandmarkEdge)) {
                     continue;
                 }
                 LandmarkEdge ledge = (LandmarkEdge) e;
                 double[] residual = e.getResidual();
                 assert residual.length == 2;
                 double residualNorm = LinAlg.normF(residual);
                 //System.out.println(residualNorm);
                 float weightFactor = Math.min(
                         (float) (residualNorm / 1.2 / 0.0015158025270275443),
                         0.98f);
                 // yellow (>0): push
                 // pink (<0): pull
                 float r = 1 * weightFactor;
                 float g = residual[0] > 0 ? 1 * weightFactor : 0;
                 float b = residual[0] < 0 ? 1 * weightFactor : 0;
                 VisConstantColor lineColor = new VisConstantColor(new Color(
                         r, g, b));
                 ArrayList<double[]> endpoints = ledge.getEndpoints();
                 // draw the edge
                 vb.addBack(new VisLines(new VisVertexData(endpoints),
                         lineColor, 2, VisLines.TYPE.LINE_STRIP));
 
 
             }
             vb.addBack(new VisPoints(new VisVertexData(robotPoints), new VisConstantColor(Color.yellow), 3));
             vb.swap();
         }
     }
 
     protected void printTiming(String timing) {
         if (false) {
             System.out.println(timing);
         }
     }
 }
