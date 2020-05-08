 package sabazios;
 
 import sabazios.util.wala.viz.NodeDecorator;
 
 import com.ibm.wala.analysis.pointers.HeapGraph;
 import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
 import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.ReturnValueKey;
 import com.ibm.wala.util.graph.Graph;
 import com.ibm.wala.util.warnings.WalaException;
 
 public class HeapGraphNodeDecorator implements NodeDecorator {
 
 	private final Graph<Object> heapGraph;
 
 	public HeapGraphNodeDecorator(Graph<Object> heapGraph) {
 		this.heapGraph = heapGraph;
 	}
 	
 	@Override
 	public String getLabel(Object obj) throws WalaException {
 		if(obj instanceof AllocationSiteInNode) {
 			AllocationSiteInNode o = (AllocationSiteInNode) obj;
 			return o.getSite().getDeclaredType().getName().getClassName() + " [ "+o.getNode().getMethod().getName().toString() +  "@" + o.getSite().getProgramCounter()+" ]";
 		}
		if(obj instanceof InstanceFieldKey) {
		  InstanceFieldKey f = (InstanceFieldKey) obj;
 			return f.getField().getName().toString();
 		}
 		if(obj instanceof LocalPointerKey) {
 			LocalPointerKey p = (LocalPointerKey) obj;
 			return p.getNode().getMethod().getName().toString() + "-v" + p.getValueNumber();
 		}
 		if(obj instanceof ReturnValueKey) {
 			ReturnValueKey p = (ReturnValueKey) obj;
 			return "RET "+p.getNode().getMethod().getName();
 		}
 			
 		return obj.toString();
 	}
 
 	@Override
 	public String getDecoration(Object obj) {
 		if(obj instanceof LocalPointerKey) 
 			return "shape=diamond";
 		if(obj instanceof PointerKey)
 			return "shape=box";
 		return "shape=oval";
 	}
 
 	@Override
 	public boolean shouldDisplay(Object n) {
 		return heapGraph.getSuccNodeCount(n) > 0 || heapGraph.getPredNodeCount(n) > 0;
 	}
 }
