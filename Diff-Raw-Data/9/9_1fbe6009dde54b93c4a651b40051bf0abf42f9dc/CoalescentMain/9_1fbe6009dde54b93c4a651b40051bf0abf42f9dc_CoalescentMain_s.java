 package coalescent;
 
 import nodes.nodeWorker;
 import coalesce.coalesce;
 import recomb.recListMaker;
 import simulator.sim;
 import sweep.sweep;
 import geneConversion.gc;
 import haplos.hap;
 import haplos.hapWorker;
 import historical.histWorker;
 import migration.migrationWorker;
 import mutate.mutList;
 import mutate.mutations;
 import demography.demography;
 import files.fileReader;
 
 public class CoalescentMain {
 	static ArgHandler fileHolder;
 	static fileReader inputHandler;
 	public static demography dem;
 	public static mutList aMutList;
 	public static gc geneConversion;
 	//public static mutWorker muts;
 	public static recListMaker recomb;
 	public static sweep sweeper;
 	public static sim simulator;
 	public static hap haps;
 	public static hapWorker hapFactory;
 	public static histWorker histFactory;
 	public static coalesce coalesce;
 	public static migrationWorker migFactory;
 	public static nodeWorker nodeFactory;
 	public static void main(String[] args) {
 		haps = new hap();
 		simulator = new sim();
 		migFactory = new migrationWorker();
 		nodeFactory = new nodeWorker();
 		fileHolder = new ArgHandler(args);
 		fileHolder.setArguments();
 		recomb = new recListMaker();
 		dem = new demography();
 		dem.setLogFile(fileHolder.getLogFile());
 		geneConversion = new gc();
 		histFactory = new histWorker();
 		coalesce = new coalesce();
 		inputHandler = new fileReader(fileHolder);
 		inputHandler.paramFileProcess();
 		sweeper = new sweep();
 		aMutList = new mutList();//mutlist init
 		
 		hapFactory = new hapWorker(haps);
 		hapFactory.hapAssignChroms();
 		dem.initRecomb();
 		sweeper.sweepInitMut(aMutList, haps);
 		simulator.simExecute();
 		
 		
 		
 		
 		
 	}
 
 }
