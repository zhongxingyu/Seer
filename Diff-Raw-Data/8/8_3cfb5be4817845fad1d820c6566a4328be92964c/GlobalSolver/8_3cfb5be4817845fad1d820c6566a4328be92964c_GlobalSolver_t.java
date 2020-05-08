 package dsolve;
 
 import ilog.concert.IloException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: imcu
  * Date: 11/18/12
  * Time: 1:23 PM
  */
 
 public class GlobalSolver {
 
 	private List<String> modelFileList;
 	private String       objectiveFile;
     private List<NamedCoordList> solutions = null;
     private List<LocalSolver> localSolvers = null;
     private NamedCoordList objective = null;
 
 
     public static Logger logger = Logger.getLogger( GlobalSolver.class.getName() );
 
 	public GlobalSolver( List<String> modelFiles, String objectiveFile ) {
 	 	this.modelFileList = modelFiles;
 		this.objectiveFile = objectiveFile;
 	}
 
 	private NamedCoordList readCurrentObjective() throws IloException, IOException {
 		return LocalSolver.readObjectiveFromFile( objectiveFile );
 	}
 
	public static NamedCoordList adjustCurrentObjective( NamedCoordList currentObj, List<NamedCoordList> solutions ) {
 
 		NamedCoordList newObj = new NamedCoordList( currentObj.size() );
 
 		// compute coordinate level mean on every solution point
 		for ( int i=0; i<currentObj.size(); i++ ) {
 			NamedCoord coord = new NamedCoord( currentObj.get(i).name, 0.0 );
 
 			double meanContrib = 0;
 			for ( NamedCoordList sol : solutions ) {
 				if ( sol.has( coord.name ) ) {
 					coord.val += sol.get( coord.name ).val;
 					meanContrib ++;
 				}
 			}
 			if ( meanContrib > 0 ) {
 				coord.val /= meanContrib;
 			}
 
 			newObj.add( coord );
 		}
 
 		return newObj.rebuild();
 	}
 
     public NamedCoordList runSolver( int maxIterCount) throws IloException, IOException
     {
         return runSolver(maxIterCount, 1e-8);
     }
 
     private void initCurrentSolvers() throws IOException, IloException {
         solutions = new ArrayList<NamedCoordList>( modelFileList.size() );
         localSolvers = new ArrayList<LocalSolver>( modelFileList.size() );
         objective = readCurrentObjective();
 
         logger.info( "Loading model files into local solvers" );
         for ( String model: modelFileList ) {
             logger.info( "loading model file: " + model );
 
             LocalSolver solver = new LocalSolver();
             solver.loadModelFromFile( model );
 
             localSolvers.add( solver );
         }
     }
 
     private void reInitCurrentSolvers() throws IloException {
         int i;
 
         for (LocalSolver lSolver : localSolvers)
             lSolver.reInit();
     }
 
     private boolean iterateSolver(int maxIterCount, double feasibilityThreshold) throws IloException {
 
         double distance2Objective = Double.MAX_VALUE;
         boolean systemIsStillFeasible = true;
         int iterCount = 0;
 
         while ( (iterCount < maxIterCount) && (distance2Objective >= feasibilityThreshold) ) {
 
             if ( !systemIsStillFeasible ) {
                 logger.warning( "global system in infeasible; aborting" );
                 return false;
             }
 
             solutions.clear();
 
             for ( LocalSolver solver: localSolvers ) {
 
                 solver.setTargetObjectivePoint( objective );
                 systemIsStillFeasible =  solver.runSolver();
 
                 if ( !systemIsStillFeasible ) { break; }
 
                 solutions.add( solver.getSolution() );
             }
 
             objective = adjustCurrentObjective( objective, solutions );
             distance2Objective = computeDistance( objective, solutions );
 
             iterCount ++;
         }
 
         logger.info(String.format("Infeasibility bound: %f", distance2Objective));
 
         if (!systemIsStillFeasible || distance2Objective >= feasibilityThreshold)
             return false;
 
         return true;
     }
 
 	public NamedCoordList runSolver( int maxIterCount, double feasibilityThreshold) throws IloException, IOException {
         initCurrentSolvers();
 
         if (iterateSolver(maxIterCount, feasibilityThreshold))
 		    return objective;
 
         return null;
 	}
 
     public NamedCoordList reRunSolver(int maxIterCount) throws IloException, IOException {
         return reRunSolver(maxIterCount, 1e-8);
     }
 
     public NamedCoordList reRunSolver(int maxIterCount, double feasibilityThreshold) throws IloException, IOException {
         //reInitCurrentSolvers();
 
         if (iterateSolver(maxIterCount, feasibilityThreshold))
             return objective;
 
         return null;
     }
 
	public static double euclidianDist( NamedCoordList v1, NamedCoordList v2 ) {
 		double distance = 0;
 		v1.rebuild(); v2.rebuild();
 
 		int i = 0, j=0;
 		while( i < v1.size() && j < v2.size() ) {
 			if ( v1.get(i).name.compareTo( v2.get(j).name ) < 0 ) { i++; continue; }
 			if ( v1.get(i).name.compareTo( v2.get(j).name ) > 0 ) { j++; continue; }
 
 			distance += Math.pow( v1.get(i).val - v2.get(j).val, 2 );
 
 			i++; j++;
 		}
 
 		return distance;
 	}
 
	public static double computeDistance( NamedCoordList objective, List<NamedCoordList> solutions ) {
 		double distance = 0;
 		for ( NamedCoordList sol : solutions ) {
 			distance += euclidianDist( objective, sol );
 		}
 		return distance;
 	}
 
     public void replaceConstraint(String constraintName, String constraintNameNew, double lowerBound, double upperBound, NamedCoordList expr) throws IloException, LocalSolver.LocalSolverInputException
     {
         for (LocalSolver localSolver : localSolvers)
             localSolver.replaceConstraint(constraintName, constraintNameNew, lowerBound, upperBound, expr);
     }
 }
