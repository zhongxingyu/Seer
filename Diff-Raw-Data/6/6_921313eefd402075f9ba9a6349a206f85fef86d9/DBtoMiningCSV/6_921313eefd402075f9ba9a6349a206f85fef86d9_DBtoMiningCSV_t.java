 package me.turnerha.infovis;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import me.turnerha.infovis.data.Bicluster;
 import me.turnerha.infovis.data.Link;
 import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
 import edu.uci.ics.jung.graph.UndirectedSparseGraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 
 /**
  * Spits out the mining.csv file, plus the ' Importance' column, which scales
  * from [0..1] -- the max and min are always 0 and 1.
  * 
  * @author hamiltont
  * 
  */
 public class DBtoMiningCSV {
 
 	public static void main(String[] args) {
 		UndirectedSparseGraph<Integer, Integer> graph = new UndirectedSparseGraph<Integer, Integer>();
 
 		List<Bicluster> clusters = removeNoisyElements();
 
 		int linkID = 0;
 		for (Bicluster bc : clusters) {
 			graph.addVertex(bc.getBiclusterId());
 			for (Link link : bc.getAllLinks()) {
 				graph
 						.addEdge(linkID++, link.getTarget().getBiclusterId(),
 								link.getDestination().getBiclusterId(),
 								EdgeType.UNDIRECTED);
 			}
 		}
 
 		BetweennessCentrality<Integer, Integer> centrality = new BetweennessCentrality<Integer, Integer>(
 				graph);
 
 		centrality.setRemoveRankScoresOnFinalize(false);
 		centrality.evaluate();
 
 		final double N = clusters.size();
 		final double scaleFactor = (N - 1) * (N - 2) / 2.0;
 
 		Collection<Number> values = centrality.getVertexRankScores(
 				"centrality.BetweennessCentrality").values();
 		Collection<Double> v2 = (Collection) values;
 		final double min = Collections.min(v2) / scaleFactor;
 		final double max = Collections.max(v2) / scaleFactor;
 
 		try {
 			PrintWriter writer = new PrintWriter(new File("mining.csv"));
 			writer.println("BiCluster Id, Row Type, Array of Rows, "
 					+ "Column Type, Array of Columns, Importance, Doc Id");
 
 			for (Bicluster c : clusters) {
 				// ID
 				writer.print(c.getBiclusterId());
 				writer.append(',');
 
 				// Row info
 				writer.append(c.getRow().getName());
 				writer.append(",\"");
 				for (String val : c.getRow().getValues())
 					writer.append(val).append(',');
 				writer.append("\",");
 
 				// Col info
 				writer.append(c.getCol().getName());
 				writer.append(",\"");
 				for (String val : c.getCol().getValues())
 					writer.append(val).append(',');
 				writer.append("\",");
 
 				// Importance
 				double score = centrality
 						.getVertexRankScore(c.getBiclusterId());
 				score = score / scaleFactor;
 
 				// We also want to normalize the values so that the min is
 				// *always* 0 and the max is *always* one. Luckily this is
 				// always a scale up operation, so we don't lose any precision
 				// Math from
 				// http://stackoverflow.com/questions/5294955/how-to-scale-down-a-range-of-numbers-with-a-known-min-and-max-value
 				double normalized = (score - min) / (max - min);
 
 				writer.printf("%.20f", normalized);
 
 				// Document ID
 				writer.print("," + c.getDocumentId() + "\n");
 			}
 
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// System.out.println("Creating graph took " + create);
 		// System.out.println("Solving and Storing graph took "
 		// + (System.currentTimeMillis() - start));
 
 	}
 
 	public static List<Bicluster> removeNoisyElements() {
 		List<Bicluster> clusters = Bicluster.getAllBiclusters();
 
 		for (Bicluster bc : clusters)
 			if (bc.getCol().getValues().remove("USA")
					| bc.getRow().getValues().remove("USA")
					| bc.getCol().getValues().remove("FBI")
					| bc.getRow().getValues().remove("FBI")) {
 				ArrayList<Link> toBeRemoved = new ArrayList<Link>();
 				for (Link l : bc.getAllLinks())
 					if (l.isLinkValid() == false)
 						toBeRemoved.add(l);
 				bc.getAllLinks().removeAll(toBeRemoved);
 			}
 
 		List<Bicluster> toRemove = new ArrayList<Bicluster>();
 
 		for (Bicluster bc : clusters)
 			if (bc.getCol().getValues().size() == 0
 					|| bc.getRow().getValues().size() == 0)
 				toRemove.add(bc);
 
 		clusters.removeAll(toRemove);
 
 		return clusters;
 	}
 }
