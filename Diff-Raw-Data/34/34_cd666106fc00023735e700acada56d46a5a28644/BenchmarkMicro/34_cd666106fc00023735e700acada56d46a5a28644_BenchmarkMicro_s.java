 package com.tinkerpop.bench.benchmark;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 import com.tinkerpop.bench.Bench;
 import com.tinkerpop.bench.ConsoleUtils;
 import com.tinkerpop.bench.GraphDescriptor;
 import com.tinkerpop.bench.LogUtils;
 import com.tinkerpop.bench.cache.Cache;
 import com.tinkerpop.bench.generator.GraphGenerator;
 import com.tinkerpop.bench.generator.SimpleBarabasiGenerator;
 import com.tinkerpop.bench.operation.OperationDeleteGraph;
 import com.tinkerpop.bench.operation.operations.*;
 import com.tinkerpop.bench.operationFactory.OperationFactory;
 import com.tinkerpop.bench.operationFactory.OperationFactoryGeneric;
 import com.tinkerpop.bench.operationFactory.factories.OperationFactoryRandomVertex;
 import com.tinkerpop.bench.operationFactory.factories.OperationFactoryRandomVertexPair;
 import com.tinkerpop.blueprints.pgm.impls.bdb.BdbGraph;
 import com.tinkerpop.blueprints.pgm.impls.dex.DexGraph;
 import com.tinkerpop.blueprints.pgm.impls.dup.DupGraph;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowGraph;
 import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
 //import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.rdf.RdfGraph;
//import com.tinkerpop.blueprints.pgm.impls.rdf.impls.NativeStoreRdfGraph;
 import com.tinkerpop.blueprints.pgm.impls.sql.SqlGraph;
 //import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 /**
  * @author Alex Averbuch (alex.averbuch@gmail.com)
  * @author Daniel Margo
  * @author Peter Macko
  */
 public class BenchmarkMicro extends Benchmark {
 	
 	/// The default file for ingest
 	private static final String DEFAULT_INGEST_FILE = "barabasi_1000_5000.graphml";
 	
	/// The list of supported databases
 	private static final String[] DATABASE_SHORT_NAMES = { "bdb", "dex", "dup", "hollow", "neo", "rdf", "sql" };
 	
 	/// The list of supported database classes
 	private static final Class<?>[] DATABASE_CLASSES = { BdbGraph.class, DexGraph.class, DupGraph.class,
		HollowGraph.class, Neo4jGraph.class, RdfGraph.class, SqlGraph.class };
 	
 	/// The defaults
 	private static final int DEFAULT_OP_COUNT = 1000;
 	private static final int DEFAULT_K_HOPS = 2;
 	
 	
 	/**
 	 * Print the help
 	 */
 	protected static void help() {
 		
 		System.err.println("Usage: runBenchmarkSuite.sh OPTIONS");
 		System.err.println("");
 		System.err.println("General options:");
 		System.err.println("  --dir, -d DIR     Set the database and results directory");
 		System.err.println("  --help            Print this help message");
 		System.err.println("  --no-warmup       Disable the initial warmup run");
 		System.err.println("");
 		System.err.println("Options to select a database (select one):");
 		System.err.println("  --bdb             Berkeley DB, using massive indexing");
 		System.err.println("  --dex             DEX");
 		System.err.println("  --dup             Berkeley DB with duplicates "+
 										"on edge lookups and properties");
 		System.err.println("  --hollow          The hollow implementation with no "+
 										"backing database");
 		System.err.println("  --neo             neo4j");
 		System.err.println("  --rdf             Sesame RDF");
		System.err.println("  --sql             MySQL");
 		System.err.println("");
 		System.err.println("Options to select a workload (select multiple):");
 		System.err.println("  --add             Adding nodes and edges to the database");
 		System.err.println("  --delete-graph    Delete the entire graph");
 		System.err.println("  --dijkstra        Dijkstra's shortest path algorithm");
 		System.err.println("  --ingest          Ingest a file to the database "+
 										" (delete the existing graph first)");
 		System.err.println("  --generate MODEL  Generate (or grow) the graph "+
 										" based on the given model");
 		System.err.println("  --get             \"Get\" microbenchmarks");
 		System.err.println("");
 		System.err.println("Ingest options:");
 		System.err.println("  --file, -f FILE   Select the file to ingest");
 		System.err.println("");
 		System.err.println("Benchmark options:");
 		System.err.println("  --op-count N      Set the number of operations");
 		System.err.println("  --k-hops K        Set the number of k-hops");
 		System.err.println("");
 		System.err.println("Options for model \"Barabasi\":");
 		System.err.println("  --barabasi-n N    The number of vertices");
 		System.err.println("  --barabasi-m M    The number of outgoing edges "+
 										" generated for each vertex");
 	}
 
 	
 	/**
 	 * Run the benchmarking program
 	 * 
 	 * @param args the command-line arguments
 	 * @throws Exception on error
 	 */
 	public static void run(String[] args) throws Exception {		
 
 		/*
 		 * Parse the command-line arguments
 		 */
 		
 		OptionParser parser = new OptionParser();
 		
 		parser.accepts("d").withRequiredArg().ofType(String.class);
 		parser.accepts("dir").withRequiredArg().ofType(String.class);
 		parser.accepts("help");
 		parser.accepts("no-warmup");
 		
 		
		// Databases
 		
		for (int i = 0; i < DATABASE_SHORT_NAMES.length; i++) {
 			parser.accepts(DATABASE_SHORT_NAMES[i]);
 		}
 		
 		
 		// Workloads
 		
 		parser.accepts("add");
 		parser.accepts("delete-graph");
 		parser.accepts("dijkstra");
 		parser.accepts("ingest");
 		parser.accepts("generate").withRequiredArg().ofType(String.class);
 		parser.accepts("get");
 		
 		
 		// Ingest modifiers
 		
 		parser.accepts("f").withRequiredArg().ofType(String.class);
 		parser.accepts("file").withRequiredArg().ofType(String.class);
 		
 		
 		// Benchmark modifiers
 		
 		parser.accepts("op-count").withRequiredArg().ofType(Integer.class);
 		parser.accepts("k-hops").withRequiredArg().ofType(Integer.class);
 		
 		
 		// Generator modifiers
 		
 		parser.accepts("barabasi-n").withRequiredArg().ofType(Integer.class);
 		parser.accepts("barabasi-m").withRequiredArg().ofType(Integer.class);
 		
 		
 		// Parse the options
 		
 		OptionSet options;
 		
 		try {
 			options = parser.parse(args);
 		}
 		catch (Exception e) {
 			System.err.println("Invalid options (please use --help for a list): " + e.getMessage());
 			return;
 		}
 		
 		
 		// Handle the options
 		
 		if (options.has("help") || !options.hasOptions()) {
 			help();
 			return;
 		}
 		
 		String dbShortName = null;
 		Class<?> dbClass = null;
 		for (int i = 0; i < DATABASE_SHORT_NAMES.length; i++) {
 			if (options.has(DATABASE_SHORT_NAMES[i])) {
 				if (dbShortName != null) {
 					System.err.println("Error: Multiple databases selected.");
 					return;
 				}
 				dbShortName = DATABASE_SHORT_NAMES[i];
 				dbClass = DATABASE_CLASSES[i];
 				break;
 			}
 		}
 		if (dbShortName == null) {
 			ConsoleUtils.error("No database is selected (please use --help for a list of options).");
 			return;
 		}
 		
 		String ingestFile = DEFAULT_INGEST_FILE;		
 		if (options.has("f") || options.has("file")) {
 			ingestFile = options.valueOf(options.has("f") ? "f" : "file").toString();
 		}
 		
 		boolean warmup = true;
 		if (options.has("no-warmup")) {
 			warmup = false;
 		}
 		
 		boolean withGraphPath = true;
 		if (dbClass == HollowGraph.class) withGraphPath = false;
 		
 		int opCount = DEFAULT_OP_COUNT;
 		int kHops = DEFAULT_K_HOPS;
 		if (options.has("op-count")) opCount = (Integer) options.valueOf("op-count");
 		if (options.has("k-hops")) kHops = (Integer) options.valueOf("k-hops");
 		
 		
 		/*
 		 * Setup the graph generator
 		 */
 		
 		GraphGenerator graphGenerator = null;
 		if (options.has("generate")) {
 			String model = options.valueOf("generate").toString().toLowerCase();
 			
 			if (model.equals("barabasi")) {
 				int n = options.has("barabasi-n") ? (Integer) options.valueOf("barabasi-n") : 1000;
 				int m = options.has("barabasi-m") ? (Integer) options.valueOf("barabasi-m") : 5;
 				graphGenerator = new SimpleBarabasiGenerator(n, m);
 			}
 			
 			if (graphGenerator == null) {
 				ConsoleUtils.error("Unrecognized graph generation model");
 				return;
 			}
 		}
 		
 		
 		/*
 		 * Get the name of the results directory
 		 */
 		
 		String dirResults;
 		if (options.has("d") || options.has("dir")) {
 			dirResults = options.valueOf(options.has("d") ? "d" : "dir").toString();
 			if (!dirResults.endsWith("/")) dirResults += "/";
 		}
 		else {
 			String propDirResults = Bench.benchProperties.getProperty(Bench.RESULTS_DIRECTORY);
 			if (propDirResults == null) {
 				ConsoleUtils.error("Property \"" + Bench.RESULTS_DIRECTORY + "\" is not set and --dir is not specified.");
 				return;
 			}
 			if (!propDirResults.endsWith("/")) propDirResults += "/";
 			dirResults = propDirResults + "Micro/";
 		}
 		
 		
 		/*
 		 * Get the name of the ingest file (if necessary)
 		 */
 		
 		if (options.has("ingest")) {
 			if (!(new File(ingestFile)).exists()) {
 				String dirGraphML = Bench.benchProperties.getProperty(Bench.DATASETS_DIRECTORY);
 				if (dirGraphML == null) {
 					ConsoleUtils.warn("Property \"" + Bench.DATASETS_DIRECTORY + "\" is not set.");
 					ConsoleUtils.error("File \"" + ingestFile + "\" does not exist.");
 					return;
 				}
 				if (!dirGraphML.endsWith("/")) dirGraphML += "/";
 				if (!(new File(dirGraphML + ingestFile)).exists()) {
 					ConsoleUtils.error("File \"" + ingestFile + "\" does not exist.");
 					return;
 				}
 				else {
 					ingestFile = dirGraphML + ingestFile;
 				}
 			}
 		}
 		else {
 			ingestFile = null;
 		}
 		
 		
 		/*
 		 * Setup the benchmark
 		 */
 		
 		String[] graphmlFiles = new String[] { ingestFile };
 		GraphGenerator[] graphGenerators = new GraphGenerator[] { graphGenerator };
 		
 		Benchmark benchmark = new BenchmarkMicro(dirResults + "benchmark_micro.csv",
 				graphmlFiles, graphGenerators, options, opCount, kHops);
 		
 		
 		/*
 		 * Setup the database
 		 */
 		
 		GraphDescriptor graphDescriptor = null;
 		
 		StringBuilder sb = new StringBuilder();
 		for (String s : args) {
 			sb.append(s.substring(2));
 			sb.append('-');
 		}
 		sb.append(Runtime.getRuntime().maxMemory());
		String argString = sb.toString();
 		
 		LinkedHashMap<String, String> resultFiles = new LinkedHashMap<String, String>();
 		
 		
 		/*
 		 * Print info
 		 */
 		
 		ConsoleUtils.sectionHeader("Tinkubator Graph Database Benchmark");
 		
 		System.out.println("Database : " + dbShortName);
 		System.out.println("Directory: " + dirResults);
 		
 		
 		/*
 		 * Load operation logs
 		 */
 		
 		if (warmup) {
 			ConsoleUtils.sectionHeader("Warmup Run");
 			graphDescriptor = new GraphDescriptor(dbClass,
					dirResults + dbShortName + "/warmup",
					withGraphPath ? dirResults + dbShortName + "/warmup" : null);
 			benchmark.loadOperationLogs(graphDescriptor,
 					dirResults + dbShortName + "/" + dbShortName + "-warmup-" + argString + ".csv");
 			resultFiles.put(dbShortName + "-warmup", dirResults + dbShortName + "/" + dbShortName + "-warmup-" + argString + ".csv");
 			Cache.dropAll();
 		}
 			
 		ConsoleUtils.sectionHeader("Benchmark Run");
 		graphDescriptor = new GraphDescriptor(dbClass,
				dirResults + dbShortName + "/db",
				withGraphPath ? dirResults + dbShortName + "/db" : null);
 		benchmark.loadOperationLogs(graphDescriptor,
 				dirResults + dbShortName + "/" + dbShortName + "-" + argString + ".csv");
 		resultFiles.put(dbShortName, dirResults + dbShortName + "/" + dbShortName + "-" + argString + ".csv");
 		
 		
 		/*
 		 * Create file with summarized results from all databases and operations
 		 */
 		
 		ConsoleUtils.sectionHeader("Summary");
 		LogUtils.makeResultsSummary(
 				dirResults + dbShortName + "/summary-" + argString + ".csv", resultFiles);
 	}
 
 	
 	/*
 	 * Instance Code
 	 */
 	
 	private int opCount = 1000;
 	private String PROPERTY_KEY = "_id";
 	private int kHops = 2;
 
 	private String[] graphmlFilenames = null;
 	private GraphGenerator[] graphGenerators = null;
 	private OptionSet options = null;
 
 	public BenchmarkMicro(String log, String[] graphmlFilenames,
 			GraphGenerator[] graphGenerators, OptionSet options,
 			int opCount, int kHops) {
 		super(log);
 		this.graphmlFilenames = graphmlFilenames;
 		this.graphGenerators = graphGenerators;
 		this.options = options;
 		this.opCount = opCount;
 		this.kHops = kHops;
 	}
 
 	@Override
 	protected ArrayList<OperationFactory> getOperationFactories() {
 		ArrayList<OperationFactory> operationFactories = new ArrayList<OperationFactory>();
 
 		for (String graphmlFilename : graphmlFilenames) {
 
 			// DELETE the graph (also invoked for the ingest benchmark)
 			if (options.has("ingest") || options.has("delete-graph")) {
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationDeleteGraph.class, 1));
 			}
 
 			// INGEST benchmarks
 			if (options.has("ingest")) {
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationLoadGraphML.class, 1,
 						new String[] { graphmlFilename }, LogUtils.pathToName(graphmlFilename)));
 			}
 			
 			// GENERATE benchmarks
 			if (options.has("generate")) {
 				for (GraphGenerator g : graphGenerators) {
 					operationFactories.add(new OperationFactoryGeneric(
 							OperationGenerateGraph.class, 1, new GraphGenerator[] { g }));
 				}
 			}
 
 			// GET microbenchmarks
 			if (options.has("get")) {
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyVertices.class, 1,
 						new Integer[] { opCount }));
 				//operationFactories.add(new OperationFactoryRandomVertex(
 				//		OperationGetVertex.class, OP_COUNT));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyVertexProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 				//operationFactories.add(new OperationFactoryRandomVertex(
 				//		OperationGetVertexProperty.class, OP_COUNT, new String[] { PROPERTY_KEY }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyEdges.class, 1,
 						new Integer[] { opCount }));
 				//operationFactories.add(new OperationFactoryRandomEdge(
 				//		OperationGetEdge.class, OP_COUNT));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyEdgeProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 				//operationFactories.add(new OperationFactoryRandomEdge(
 				//		OperationGetEdgeProperty.class, OP_COUNT, new String[] { PROPERTY_KEY }));
 	
 				// GET_NEIGHBORS ops and variants
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetFirstNeighbor.class, opCount, new Integer[] { kHops }));
 				
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetRandomNeighbor.class, opCount, new Integer[] { kHops }));
 				
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetAllNeighbors.class, opCount, new Integer[] { kHops }));
 				
 				// GET_K_NEIGHBORS ops and variants
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetKFirstNeighbors.class, opCount));
 				
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetKRandomNeighbors.class, opCount));
 				
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetKHopNeighbors.class, opCount));
 			}
 			
 			// SHORTEST PATH (Djikstra's algorithm)
 			if (options.has("dijkstra")) {
 				operationFactories.add(new OperationFactoryRandomVertexPair(
 						OperationGetShortestPath.class, opCount / 2));
 				
 				operationFactories.add(new OperationFactoryRandomVertexPair(
 						OperationGetShortestPathProperty.class, opCount / 2));
 			}
 			
 			// ADD/SET microbenchmarks
 			if (options.has("add")) {
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationAddManyVertices.class, 1,
 						new Integer[] { opCount }));
 				//operationFactories.add(new OperationFactoryGeneric(
 				//		OperationAddVertex.class, OP_COUNT));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationSetManyVertexProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 				//operationFactories.add(new OperationFactoryRandomVertex(
 				//		OperationSetVertexProperty.class, OP_COUNT, new String[] { PROPERTY_KEY }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationAddManyEdges.class, 1,
 						new Integer[] { opCount }));
 				//operationFactories.add(new OperationFactoryRandomVertexPair(
 				//		OperationAddEdge.class, OP_COUNT));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationSetManyEdgeProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 				//operationFactories.add(new OperationFactoryRandomEdge(
 				//		OperationSetEdgeProperty.class, OP_COUNT, new String[] { PROPERTY_KEY}));
 			}
 					
 		}
 
 		return operationFactories;
 	}
 
 }
