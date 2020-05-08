 package info.gehrels.diplomarbeit.flockdb;
 
 import com.google.common.base.Stopwatch;
 import info.gehrels.diplomarbeit.AbstractBenchmarkStep;
 import info.gehrels.flockDBClient.FlockDB;
 
 public class FlockDBBenchmarkStep extends AbstractBenchmarkStep {
 	public FlockDBBenchmarkStep(String algorithm, String inputPath) {
 		super(algorithm, inputPath);
 	}
 
 	@Override
 	protected void runImporter(String inputPath) throws Exception {
 		Stopwatch stopwatch = new Stopwatch().start();
 		FlockDBImporter flockDBImporter = new FlockDBImporter(inputPath);
 		flockDBImporter.importNow();
 		flockDBImporter.ensureImportCompleted();
 		stopwatch.stop();
 		System.err.println(stopwatch);
 	}
 
 	@Override
 	protected void readWholeGraph() throws Exception {
 		Stopwatch stopwatch = new Stopwatch().start();
		new FlockDBReadWholeGraph(FlockDBHelper.createFlockDB(), maxNodeId, true).readWholeGraph();
 		stopwatch.stop();
 		System.err.println(stopwatch);
 	}
 
 	@Override
 	protected void calcSCC() throws Exception {
 		warmUpDatabaseAndMeasure(new Measurement() {
 			public void execute(FlockDB flockDB) throws Exception {
 				new FlockDBStronglyConnectedComponents(flockDB, maxNodeId).calculateStronglyConnectedComponents();
 			}
 
 		});
 	}
 
 	@Override
 	protected void calcFoF() throws Exception {
 		warmUpDatabaseAndMeasure(new Measurement() {
 			public void execute(FlockDB flockDB) throws Exception {
 				new FlockDBFriendsOfFriends(flockDB, maxNodeId).calculateFriendsOfFriends();
 			}
 		});
 	}
 
 	@Override
 	protected void calcCommonFriends() throws Exception {
 		warmUpDatabaseAndMeasure(new Measurement() {
 			public void execute(FlockDB flockDB) throws Exception {
 				new FlockDBCommonFriends(flockDB, maxNodeId).calculateCommonFriends();
 			}
 		});
 	}
 
 	@Override
 	protected void calcRegularPathQueries() throws Exception {
 		warmUpDatabaseAndMeasure(new Measurement() {
 			public void execute(FlockDB flockDB) throws Exception {
 				new FlockDBRegularPathQuery(flockDB, maxNodeId).calculateRegularPaths();
 			}
 		});
 	}
 
 	private void warmUpDatabaseAndMeasure(Measurement measurement) throws Exception {
 		FlockDB flockDB = FlockDBHelper.createFlockDB();
		new FlockDBReadWholeGraph(flockDB, maxNodeId, false).readWholeGraph();
 		Stopwatch stopwatch = new Stopwatch().start();
 		measurement.execute(flockDB);
 		stopwatch.stop();
 		System.err.println(stopwatch);
 	}
 
 	interface Measurement {
 		void execute(FlockDB flockDB) throws Exception;
 	}
 }
