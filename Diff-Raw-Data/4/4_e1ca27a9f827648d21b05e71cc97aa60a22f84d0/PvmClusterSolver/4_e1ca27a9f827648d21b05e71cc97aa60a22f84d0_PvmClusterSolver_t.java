 package pvm;
 
 import dsolve.LocalSolver;
 import dsolve.SolverHelper;
 import ilog.concert.IloException;
 import ilog.concert.IloNumVar;
 import ilog.concert.IloObjective;
 import ilog.concert.IloRange;
 import ilog.cplex.IloCplex;
 import org.apache.commons.lang3.mutable.MutableDouble;
 import pvm.KernelProducts.KernelProductManager;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Andrei
  * Date: 1/27/13
  * Time: 2:01 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PvmClusterSolver extends PvmSolver{
     PvmClusterDataCore clusterCore;
     PvmClusterSystem pvmClusterSys;
     public static double relativeObjectiveThresh = 0.9999;
     public static double objectiveMinimalDifference = 1.5e-15;
     public static double relativeAberrationInitialThresh = 0.95;
     public static double relativeAberrationThreshDecayRate = 0.85;
     public static double relativeAberrationMinimalThresh = 0.01;
 
 
     public PvmClusterSolver(){
         core = clusterCore = new PvmClusterDataCore();
         pvmSys = pvmClusterSys = new PvmClusterSystem();
     }
 
     @Override
     public boolean TrainSingleLPWithBias(double positiveBias) throws IloException {
         double splitAberrationThresh = relativeAberrationInitialThresh;
         double relativeObjective;
         double clusterObjective, nonClusteredObjective;
         double [] resT = new double[1];
        int iterCount = 0, maxIterCount;
         int localIters, maxLocalIters = 10;
 
         if (core.getClass() == PvmClusterDataCore.class)
             clusterCore = (PvmClusterDataCore)core;
 
        maxIterCount = core.entries.size();
         //clusterCore.Init();
 
         do{
             pvmClusterSys.buildSingleLPSystemWithBias(clusterCore, positiveBias);
             if (!pvmClusterSys.solveSingleLPWithBias(resT, positiveBias))
                 return false;
 
             clusterCore.recomputeAverages();
             clusterCore.recomputeSigmas();
             nonClusteredObjective = clusterCore.computeNonClusteredObjectiveValue(positiveBias);
             clusterObjective = clusterCore.computeClusteredObjectiveValue(positiveBias);
 
             if (clusterObjective + objectiveMinimalDifference < nonClusteredObjective * relativeObjectiveThresh){
                 localIters = 0;
                 while (!clusterCore.splitClustersDescendingAccordingToAberration(splitAberrationThresh) &&
                         localIters < maxLocalIters){
                     splitAberrationThresh *= relativeAberrationThreshDecayRate;
                     localIters++;
                 }
 
                 /*while (!clusterCore.splitClustersWithAberrationOverThreshold(splitAberrationThresh) &&
                         splitAberrationThresh > relativeAberrationMinimalThresh)*/
 
                 if (splitAberrationThresh <= relativeAberrationMinimalThresh)
                     splitAberrationThresh *= relativeAberrationThreshDecayRate;
             }
             else
                 break;
 
             iterCount++;
         } while (iterCount < maxIterCount);
 
         if (resT[0] == 0){
 
             pvmClusterSys.buildSecondaryLpSystem(clusterCore, positiveBias);
             if (!pvmClusterSys.solveSingleLPSecondary(resT, positiveBias))
                 return false;
 
             nonClusteredObjective = clusterCore.computeNonClusteredObjectiveValue(positiveBias);
             clusterObjective = clusterCore.computeClusteredObjectiveValue(positiveBias);
         }
 
         return true;
     }
 
 	@Override
 	public int getClusterCount() {
 		return this.clusterCore.clustersCount;
 	}
 
 	@Override
     protected PvmSolver instantiateLocalSolver(){
         return new PvmClusterSolver();
     }
 
 	public static void main(String[] args ) throws Exception {
 
 		if (args.length < 1) return;
 
 		try { SolverHelper.dropNativeCplex(); } catch ( URISyntaxException ignored ) {}
 
 		PvmClusterSolver solver = new PvmClusterSolver();
 		PvmTrainParameters bestTrainParams = new PvmTrainParameters();
 		MutableDouble acc = new MutableDouble(0), sens = new MutableDouble(0), spec = new MutableDouble(0);
 
 		solver.clusterCore.ReadFile( args[0] );
 		solver.clusterCore.buildKMeansClusters( 0.05 );
 
 		KernelProductManager.KerType[] kernelTypes = new KernelProductManager.KerType[2];
 		kernelTypes[0] = KernelProductManager.KerType.KERSCALAR;
 		kernelTypes[1] = KernelProductManager.KerType.KERRBF;
 
 		solver.searchTrainParameters( 10, kernelTypes, bestTrainParams, acc, sens, spec );
 	}
 
 }
