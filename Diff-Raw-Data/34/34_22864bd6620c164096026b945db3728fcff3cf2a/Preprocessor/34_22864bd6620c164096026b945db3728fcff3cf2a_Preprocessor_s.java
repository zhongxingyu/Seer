 package br.usp.pf.preprocess;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import br.usp.pf.core.Conformation;
 import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
 import edu.uci.ics.jung.graph.UndirectedSparseGraph;
 import edu.uci.ics.jung.graph.util.Pair;
 
 public class Preprocessor {
 	
 	private String input;
 	private String output;
 	private int noThreads;
 	private int cut;
 	
 	private HashMap<Conformation, State> conformations;
 	private ArrayList<Integer> simulationDynamic;
 	private UndirectedSparseGraph<Integer, Integer> graph;
 
 	private ArrayList<State> live;
 	private ArrayList<State> dead;
 	
 	private HashMap<Integer, Integer> keysMap;
 
 	public Preprocessor(String input, String output, int cut, int noThreads) {
 		this.input = input;
 		this.output = output;
 		this.cut = cut;
 		this.noThreads = noThreads;
 		
 		conformations = new HashMap<Conformation, State>();
 		simulationDynamic = new ArrayList<Integer>();
 		graph = new UndirectedSparseGraph<Integer, Integer>();
 		
 		keysMap = new HashMap<Integer, Integer>();
 		live = new ArrayList<State>();
 		dead = new ArrayList<State>();
 	}
 	
 	public int getDistinctStates() {return conformations.size();}
 	public int getReadStates() {return simulationDynamic.size();}
 
 	public void process(boolean buildGraph) throws Exception {
 
 		PrintWriter log = new PrintWriter(new File(output + "log" + ".txt").getAbsoluteFile());
 		String message;
 		
 		long start = System.currentTimeMillis();
 		message = "Opening file " + input; 
 		System.out.println(message); log.println(message);
 		message = "Loading simulation data..."; 
 		System.out.println(message); log.println(message);
 		
 		loadSimulation(); System.gc();
 		
 		message = getReadStates() + " states were read.";
 		System.out.println(message); log.println(message);
 		message = getDistinctStates() + " states are distinct.";
 		System.out.println(message); log.println(message);
 		
 		message = "Mean incidence: " + Math.round(getReadStates() / (double) getDistinctStates());
 		System.out.println(message); log.println(message);
 		
 		message = "Applying incidence cut of " + cut + ".";
 		System.out.println(message); log.println(message);
 		
 		applyCut();
 		
 		message = live.size() + " states survived the cut.";
 		System.out.println(message); log.println(message);
 		
 		
 		if (buildGraph) {
 			message = "Analysing process dynamic and building graph...";
 			System.out.println(message); log.println(message);
 			buildGraph();
 		}
 		
 		message = "Writing files...";
 		System.out.println(message); log.println(message);
 		
 		printDataFile();
 		printNoJumps();
 		
 		long finish = System.currentTimeMillis();
		System.out.printf("Preprocess took: %.2f mins\n",
				((finish - start) / 60000.0f));

 	}
 
 	private void loadSimulation() throws Exception {				
 		
 		BufferedReader in;
 		String line;
 		StringTokenizer st;
 
 		in = new BufferedReader(new FileReader(input));
 		line = in.readLine();
 		
 		if ((line.startsWith("E")) || (line.startsWith("[E]"))) { // check if there is a header
 			line = in.readLine(); // ignore first line (header)
 		}
 
 		while (line != null) {
 
 			if (line.equals("")) if ((line = in.readLine()) == null) {break;} // ignore empty lines
 			
 			st = new StringTokenizer(line, " ");
 			
 			if (st.countTokens() == 3) {
 				
 				int energy = Integer.parseInt(st.nextToken()); 
 				st.nextToken(); // ignore second token
 				int noContacts = Integer.parseInt(st.nextToken());
 
 				Conformation conformation = new Conformation();
 				line = conformation.read(in);
 
 				State state = conformations.get(conformation);
 				if (state == null) {
 					conformations.put(conformation, new State(conformations.size(), 1, energy, noContacts, conformation));
 				} else {
 					state.increaseIncidence(); 
 				}
 				simulationDynamic.add(conformations.get(conformation).getId()); // add state to experiment array
 			} else { // if it is not a valid state it is a asterisk	
 				while (line.startsWith("*")) {
 					if ((line = in.readLine()) == null) {break;} // ignore all lines with *
 				}
 				simulationDynamic.add(-1);
 			}
 		}
 		if (in != null) {
 			in.close();
 		}
 	}
 	
 	private void applyCut() {
 		
 		for (State state : conformations.values()) {
 			if (state.getIncidence() > cut) {
 				keysMap.put(state.getId(), live.size());
 				live.add(state);
 			}
 		}
 		for (State state : conformations.values()) {
 			if (state.getIncidence() <= cut) {
 				keysMap.put(state.getId(), live.size() + dead.size());
 				dead.add(state);
 			}
 		}
 	}
 
 	private void buildGraph() throws Exception {
 
 		HashSet<EdgesPair> pairs = new HashSet<EdgesPair>();
 
 		int id = 0;
 		for (int i = 0; i < simulationDynamic.size() - 1; i++) {
 			
 			int source = keysMap.get(simulationDynamic.get(i));
 			int target = keysMap.get(simulationDynamic.get(i + 1));
 			
 			EdgesPair pair = new EdgesPair(source, target);
 			if (!pairs.contains(pair)) {
 				pairs.add(pair);
 				graph.addEdge(id++, target, source);
 			}
 		}
 	}
 	
 	private void printDataFile() throws Exception {
 
 		PrintWriter out = new PrintWriter(new File(output + "dy_file" + ".data").getAbsoluteFile());
 		
 		out.println("DY"); // file header
 		out.println(live.size()); // number of states (only distinct and live)
 		out.println(27 * 27); // number of columns
 		out.println(); // a blank line
 		
 		Collections.sort(live);
 		
 		// start printing all states
 		for (State state : live) {
			out.print(state.getId() + ";");
 			for (boolean b : state.getConformation().getChain()) {
 				if (b) {
 					out.print(1 + ";");
 				} else {
 					out.print(0 + ";");
 				}
 			}
 			out.println(state.getEnergy());
 		}
 		
 		if (out != null) {
 			out.close();
 		}
 	}
 	
 	private void printNoJumps() throws Exception {
 
 		PrintWriter out = new PrintWriter(new File(output + "jumps_file" + ".data").getAbsoluteFile());
 		
 		out.println("x y [path]"); // print header
 		
 		out.println(live.size());
 
 		Integer[] vertices = new Integer[graph.getVertices().size()];
 		graph.getVertices().toArray(vertices);
 
 		int div = vertices.length / noThreads;
 		int begin = 0;
 		int end = div + (vertices.length % noThreads);
 
 		DistanceGetter[] threads = new DistanceGetter[noThreads];
 		
 		for (int i = 0; i < threads.length ; i++) {
 			threads[i] = new DistanceGetter(i, vertices, begin, end, graph, out);
 			threads[i].start();
 			begin = end;
 			end = begin + div;
 		}
 		// wait for threads to finish
 		for (int i = 0; i < threads.length; i++) {
 			threads[i].join();
 		}
 
 		if (out != null) {
 			out.close();
 		}
 	}
 }
 class EdgesPair {
 	
 	int source;
 	int target;
 	
 	public EdgesPair(int source, int target) {
 		this.source = source;
 		this.target = target;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + source;
 		result = prime * result + target;
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false; 
 		EdgesPair other = (EdgesPair) obj;
 		if (source != other.source) {
 			return false;
 		}
 		if (target != other.target) {
 			return false;
 		}
 		return true;
 	}
 }
