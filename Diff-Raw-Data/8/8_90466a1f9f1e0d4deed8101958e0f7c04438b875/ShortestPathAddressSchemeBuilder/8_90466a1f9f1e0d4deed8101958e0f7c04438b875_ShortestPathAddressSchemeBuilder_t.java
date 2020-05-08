 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.proto.name;
 
 import java.util.Collections;
 
 import tags.proto.AddressScheme;
 import tags.proto.ProtoAddressScheme;
 import tags.proto.FullTGraph;
 import tags.util.Maps.U2Map;
 import tags.util.Union.U2;
 import tags.util.Tuple.X2;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Queue;
 import java.util.Comparator;
 import java.util.PriorityQueue;
 import java.util.HashMap;
 
 /**
 ** DOCUMENT.
 **
 ** @param <T> Type of tag
 ** @param <A> Type of address
 ** @param <U> Type of node-attribute
 ** @param <W> Type of arc-attribute
 ** @param <D> Type of distance
 */
 public class ShortestPathAddressSchemeBuilder<T, A, U, W, D> implements AddressSchemeBuilder<T, A, U, W> {
 
 	final protected DistanceMetric<D, U, W> dmetric;
 
 	public ShortestPathAddressSchemeBuilder(DistanceMetric<D, U, W> dmetric) {
 		this.dmetric = dmetric;
 	}
 
 	@Override public AddressScheme<T, A, W> buildAddressScheme(FullTGraph<T, A, U, W> graph, Set<T> completed, final T seed) {
 		ProtoAddressScheme<T, A, W> scheme = new ProtoAddressScheme<T, A, W>(seed, null);
 
 		U seedu = graph.nodeMap().K0Map().get(seed);
 		if (seedu == null) {
			// FIXME NOW this needs to be handled elsewhere, it's actually a valid
			// condition that can arise during normal operation
 			throw new IllegalArgumentException("seed " + seed + " is not in the given graph");
 		}
 
 		if (!completed.contains(seed)) {
 			scheme.setIncomplete();
 			return scheme;
 		}
 
 		/*System.out.println(completed + " " + seed);
 		System.out.println(graph.nodeMap());
 		System.out.println(graph.arcMap());*/
 
 		// Dijkstra's algorithm
 		// OPT NORM use a FibonnacciHeap instead of PriorityQueue. JGraphT has an implementation.
 
 		class DijkstraNode {
 
 			final public U2<T, A> node;
 			protected T parent;
 			protected D dist;
 
 			public DijkstraNode(U2<T, A> node) {
 				this.node = node;
 				this.dist = (seed.equals(node.val))? dmetric.getMinElement(): dmetric.getMaxElement();
 			}
 
 		}
 
 		// 1. Init
 		Queue<DijkstraNode> queue = new PriorityQueue<DijkstraNode>(graph.nodeMap().size(), new Comparator<DijkstraNode>() {
 			@Override public int compare(DijkstraNode d1, DijkstraNode d2) {
 				return dmetric.compare(d1.dist, d2.dist);
 			}
 		});
 		Map<U2<T, A>, DijkstraNode> dmap = new HashMap<U2<T, A>, DijkstraNode>();
 		for (U2<T, A> u: graph.nodeMap().keySet()) {
 			DijkstraNode n = new DijkstraNode(u);
 			dmap.put(u, n);
 			queue.add(n);
 		}
 
 		// 2. Loop
 		while (!queue.isEmpty()) {
 			DijkstraNode cur = queue.poll();
 			U2<T, A> node = cur.node;
 			T parent = cur.parent;
 			assert(dmap.get(node) == cur);
 			dmap.remove(node);
 
 			if (node.isT1()) {
 				scheme.pushNode(node, parent, graph.getIncomingG(node.getT1()).nodeAttrMap().K0Map().keySet());
 				continue;
 			}
 			T tag = node.getT0();
 
 			// tag is not fully loaded, set as incomplete
 			if (!completed.contains(tag)) {
 				scheme.pushNode(node, parent, Collections.<T>singleton(parent));
 				scheme.setIncomplete();
 				break;
 			}
 
 			// "relax" all out-neighbours
 			U srcu = graph.nodeMap().K0Map().get(tag);
 			for (Map.Entry<U2<T, A>, X2<U, W>> en: graph.getOutgoingT(tag).attrMap().entrySet()) {
 				U2<T, A> nb = en.getKey();
 				U dstu = en.getValue()._0;
 				W arcw = en.getValue()._1;
 
 				DijkstraNode out = dmap.get(nb);
 				if (out == null) { continue; } // already visited
 
 				D dist = dmetric.combine(cur.dist, dmetric.getDistance(srcu, dstu, arcw));
 				if (dmetric.compare(dist, out.dist) < 0) {
 					// PriorityQueue treats its elements as immutable, so need to remove first
 					queue.remove(out);
 					out.dist = dist;
 					out.parent = tag;
 					queue.add(out);
 				}
 			}
 
 			if (!tag.equals(seed)) {
 				scheme.pushNode(node, parent, graph.getIncomingT(tag).nodeAttrMap().K0Map().keySet());
 			}
 			scheme.arcAttrMap().put(tag, dmetric.getAttrFromDistance(seedu, srcu, cur.dist));
 		}
 
 		return scheme;
 	}
 
 }
