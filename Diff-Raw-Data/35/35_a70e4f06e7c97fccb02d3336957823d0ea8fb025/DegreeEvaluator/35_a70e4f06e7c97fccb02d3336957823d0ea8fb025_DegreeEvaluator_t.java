 package sim_tst;
 
 import graph_gen_utils.NeoFromFile;
 import graph_gen_utils.memory_graph.MemGraph;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.TreeSet;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 public class DegreeEvaluator {
 
 	public static void main(String[] args) {
 		GraphDatabaseService db = null;
 		try {
 			String outFolder = args[0];
 			String dbDir = args[1];
 
 			db = new EmbeddedGraphDatabase(dbDir);
 			MemGraph memDb = NeoFromFile.readMemGraph(db);
 			DegreeEvaluator ev = new DegreeEvaluator(memDb);
 			ev.calcDegree();
 			File f = new File(outFolder);
 			ev.toFile(f);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			db.shutdown();
 		}
 	}
 	
 	private GraphDatabaseService db;
 
 	private HashMap<Integer, Long> inDegHist = new HashMap<Integer, Long>();
 	private HashMap<Integer, Long> outDegHist = new HashMap<Integer, Long>();
 	private HashMap<Integer, Long> degHist = new HashMap<Integer, Long>();
 
 	private long nodeCount = 0;
 	private long relCount = 0;
 	private double inDegSum = 0;
 	private double outDegSum = 0;
 	private double degSum = 0;
 
 	int minInDegree = Integer.MAX_VALUE;
 	int minOutDegree = Integer.MAX_VALUE;
 	int minDegree = Integer.MAX_VALUE;
 
 	int maxInDegree = -1;
 	int maxOutDegree = -1;
 	int maxDegree = -1;
 
 	double avgOutDegree = -1;
 	double avgInDegree = -1;
 	double avgDegree = -1;
 
 	double stdInDegree = -1;
 	double stdOutDegree = -1;
 	double stdDegree = -1;
 
 	public DegreeEvaluator(GraphDatabaseService db) {
 		this.db = db;
 	}
 	
 	public void calcDegree() {
 		// initiate
 		nodeCount = 0;
 		relCount = 0;
 		inDegSum = 0;
 		outDegSum = 0;
 		degSum = 0;
 
 		minInDegree = Integer.MAX_VALUE;
 		minOutDegree = Integer.MAX_VALUE;
 		minDegree = Integer.MAX_VALUE;
 
 		maxInDegree = 0;
 		maxOutDegree = 0;
 		maxDegree = 0;
 
 		inDegHist.clear();
 		outDegHist.clear();
 		degHist.clear();
 
 		// calculate degree
 		for (Node n : db.getAllNodes()) {
 			nodeCount++;
 			int deg = 0;
 			int inDeg = 0;
 			int outDeg = 0;
 			for (@SuppressWarnings("unused") Relationship rs : n.getRelationships(Direction.OUTGOING)) {
 				outDeg++;
 				relCount++;
 			}
 			for (@SuppressWarnings("unused") Relationship rs : n.getRelationships(Direction.INCOMING)) {
 				inDeg++;
 			}
 			deg = inDeg + outDeg;
 
 			Long val = inDegHist.get(inDeg);
 			if (val == null) {
 				val = 1l;
 			} else {
 				val++;
 			}
 			inDegHist.put(inDeg, val);
 
 			val = outDegHist.get(outDeg);
 			if (val == null) {
 				val = 1l;
 			} else {
 				val++;
 			}
 			outDegHist.put(outDeg, val);
 
 			val = degHist.get(deg);
 			if (val == null) {
 				val = 1l;
 			} else {
 				val++;
 			}
 			degHist.put(deg, val);
 		
 			inDegSum += inDeg;
 			outDegSum += outDeg;
 			degSum += deg;
 
 			if (minDegree == -1) {
 				minDegree = deg;
 				minInDegree = inDeg;
 				minOutDegree = outDeg;
 
 				maxDegree = deg;
 				maxInDegree = inDeg;
 				maxOutDegree = outDeg;
 			} else {
 
 				if (deg < minDegree) {
 					minDegree = deg;
 				} else if (deg > maxDegree) {
 					maxDegree = deg;
 				}
 
 				if (inDeg < minInDegree) {
 					minInDegree = inDeg;
 				} else if (inDeg > maxInDegree) {
 					maxInDegree = inDeg;
 				}
 
 				if (outDeg < minOutDegree) {
 					minOutDegree = outDeg;
 				} else if (outDeg > maxOutDegree) {
 					maxOutDegree = outDeg;
 				}
 			}
 		}
 
 		avgInDegree = inDegSum / (double) nodeCount;
 		avgOutDegree = outDegSum / (double) nodeCount;
 		avgDegree = degSum / (double) nodeCount;
	
 		for (Integer k : degHist.keySet()) {
 			stdDegree += Math.pow((k - avgDegree), 2) * degHist.get(k);
 		}
 		for (Integer k : inDegHist.keySet()) {
 			stdInDegree += Math.pow((k - avgInDegree), 2) * inDegHist.get(k);
 		}
 		for (Integer k : outDegHist.keySet()) {
 			stdOutDegree += Math.pow((k - avgOutDegree), 2) * outDegHist.get(k);
 		}
 	}
 
 	public void toFile(File f) throws FileNotFoundException {
 		double relRate = relCount / (double) nodeCount;
 		PrintStream ps = new PrintStream(f);
 		ps.println("nodeCount = " + nodeCount);
 		ps.println("relCount = " + relCount);
 		ps.println("rel/node = " + relRate);
 		ps.println();
 		ps = new PrintStream(f);
 		ps.println("minInDegree = " + minInDegree);
 		ps.println("maxInDegree = " + maxInDegree);
 		ps.println("avgInDegree = " + avgInDegree);
 		ps.println("stdInDegree = " + stdInDegree);
 		ps.println();
 		ps.println(inDegHist);
 		ps.println();
 		ps.println("minOutDegree = " + minOutDegree);
 		ps.println("maxOutDegree = " + maxOutDegree);
 		ps.println("avgOutDegree = " + avgOutDegree);
 		ps.println("stdOutDegree = " + stdOutDegree);
 		ps.println();
		ps.println(outDegHist);
		ps.println();
 		ps.println("minDegree = " + minDegree);
 		ps.println("maxDegree = " + maxDegree);
 		ps.println("avgDegree = " + avgDegree);
 		ps.println("stdDegree = " + stdDegree);
 		ps.println();
		ps.println(degHist);
		ps.println();
 		ps.close();
 
 		File histFile = new File(f.getAbsoluteFile() + "degHist");
 		ps = new PrintStream(histFile);
 		int max = printHist(degHist, ps, 0);
 		ps.close();
 
 		File inHistFile = new File(f.getAbsoluteFile() + "inDegHist");
 		ps = new PrintStream(inHistFile);
 		printHist(inDegHist, ps, max);
 		ps.close();
 
 		File outHistFile = new File(f.getAbsoluteFile() + "outDegHist");
 		ps = new PrintStream(outHistFile);
 		printHist(outDegHist, ps, max);
 		ps.close();

		File inDegRawFile = new File(f.getAbsoluteFile() + "/rawInDeg.txt");
		ps = new PrintStream(inDegRawFile);
		printRaw(inDegHist, ps);
		ps.close();

		File outDegRawFile = new File(f.getAbsoluteFile() + "/rawOutDeg.txt");
		ps = new PrintStream(outDegRawFile);
		printRaw(outDegHist, ps);
		ps.close();

		File degRawFile = new File(f.getAbsoluteFile() + "/rawDeg.txt");
		ps = new PrintStream(degRawFile);
		printRaw(degHist, ps);
		ps.close();

 	}
 
 
 	private int printHist(HashMap<Integer, Long> hist, PrintStream ps, int max){
 		TreeSet<Integer> keys = new TreeSet<Integer>(hist.keySet());
 		int cur = 0;
 		for (int k : keys) {
 			while (cur < k) {
 				ps.println(0);
 				cur++;
 			}
 			ps.println(hist.get(k));
 			cur++;
 		}
 		while (max > cur) {
 			ps.println(0);
 			max--;
 		}
 		return cur;
 	}
	
	private void printRaw(HashMap<Integer, Long> hist, PrintStream ps) {
		TreeSet<Integer> keys = new TreeSet<Integer>(hist.keySet());
		for (int k : keys) {
			for (int i = 0; i < hist.get(k); i++) {
				ps.println(k);
			}
		}
	}
 }
