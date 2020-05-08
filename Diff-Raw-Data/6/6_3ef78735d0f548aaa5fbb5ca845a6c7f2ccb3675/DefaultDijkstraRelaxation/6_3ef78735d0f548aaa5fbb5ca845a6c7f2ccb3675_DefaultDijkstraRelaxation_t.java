 package edu.ppt.impossible.helpers;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.ppt.impossible.model.Edge;
 import edu.ppt.impossible.model.Graph;
 import edu.ppt.impossible.model.Node;
 
 public class DefaultDijkstraRelaxation extends DijkstraRelaxation {
 
 	private final MetricProvider metricProvider;
 
 	Map<Node, List<Double>> labels;
 	Map<Node, Double> aggregatedLabels;
 
 	public DefaultDijkstraRelaxation(MetricProvider metricProvider) {
 		this.metricProvider = metricProvider;
 	}
 
 	@Override
 	public void reset(Graph graph, Node from) {
 
		labels = new HashMap<>();
		aggregatedLabels = new HashMap<>();
		predecessors = new HashMap<>();

 		for (Node node : graph.getNodes()) {
 
 			double labelValue = (node.equals(from)) ? 0
 					: Double.POSITIVE_INFINITY;
 
 			List<Double> label = new ArrayList<>();
 			for (int m = 0; m < graph.getNumMetrics(); ++m) {
 				label.add(labelValue);
 			}
 
 			labels.put(node, label);
 			aggregatedLabels.put(node, labelValue);
 			predecessors.put(node, node);
 		}
 	}
 
 	@Override
 	public void relax(Graph graph, Node from, Node to) {
 
 		Edge edge = graph.getEdge(from.getId(), to.getId());
 
 		double candidateDistance = aggregatedLabels.get(from)
 				+ metricProvider.get(edge);
 
 		if (candidateDistance < aggregatedLabels.get(to)) {
 
 			List<Double> label = new ArrayList<>();
 			for (int m = 0; m < graph.getNumMetrics(); ++m) {
 				label.add(labels.get(from).get(m) + edge.getMetrics().get(m));
 			}
 
 			labels.put(to, label);
 			aggregatedLabels.put(to, candidateDistance);
 			predecessors.put(to, from);
 		}
 	}
 
 	@Override
 	public boolean isCheaper(Node from, Node to) {
 		return aggregatedLabels.get(from) < aggregatedLabels.get(to);
 	}
 
 }
