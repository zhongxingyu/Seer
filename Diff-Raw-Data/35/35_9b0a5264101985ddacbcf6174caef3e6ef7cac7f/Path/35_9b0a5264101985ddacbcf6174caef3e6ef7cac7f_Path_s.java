 package tspsolver.model.scenario.path;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Set;
 
 import tspsolver.model.scenario.grid.Edge;
 import tspsolver.model.updates.path.PathUpdate;
 import tspsolver.model.updates.path.PathUpdateAction;
 
 public class Path extends Observable implements Serializable {
 
 	private static final long serialVersionUID = 8102936507138748772L;
 
 	private final Set<Edge> edges;
 	private double weight;
	private boolean weightNeedsUpdate;
 
 	public Path() {
 		this.edges = new HashSet<Edge>();
 		this.weight = 0.0;
		this.weightNeedsUpdate = false;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 
 		result = prime * result + ((this.edges == null) ? 0 : this.edges.hashCode());
 
 		final long temp = Double.doubleToLongBits(this.weight);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 
 		if (obj == null) {
 			return false;
 		}
 
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 
 		final Path other = (Path) obj;
 
 		if (this.edges == null) {
 			if (other.edges != null) {
 				return false;
 			}
 		}
 		else if (!this.edges.equals(other.edges)) {
 			return false;
 		}
 
 		if (Double.doubleToLongBits(this.weight) != Double.doubleToLongBits(other.weight)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public double getWeight() {
		if (this.weightNeedsUpdate) {
			this.updateWeight();
		}

 		return this.weight;
 	}
 
 	public Edge[] getEdges() {
 		return this.edges.toArray(new Edge[this.edges.size()]);
 	}
 
 	public int getNumberOfEdges() {
 		return this.edges.size();
 	}
 
 	public boolean isEmpty() {
 		return this.getNumberOfEdges() < 1;
 	}
 
 	public boolean containsEdge(Edge edge) {
 		return this.edges.contains(edge);
 	}
 
 	protected synchronized void addEdge(Edge edge) {
 		if (this.edges.add(edge)) {
			this.weightNeedsUpdate = true;
 		}
 	}
 
 	protected synchronized void removeEdge(Edge edge) {
 		if (this.edges.remove(edge)) {
			this.weightNeedsUpdate = true;
		}
	}

	private void updateWeight() {

		this.weight = 0.0;

		for (Edge edge : this.edges) {
			this.weight += edge.getWeight();
 		}

		this.weightNeedsUpdate = false;
 	}
 
 	protected void firePathUpdate(Edge edge, PathUpdateAction action) {
 		final PathUpdate update = new PathUpdate(edge, action);
 
 		this.setChanged();
 		this.notifyObservers(update);
 	}
 }
