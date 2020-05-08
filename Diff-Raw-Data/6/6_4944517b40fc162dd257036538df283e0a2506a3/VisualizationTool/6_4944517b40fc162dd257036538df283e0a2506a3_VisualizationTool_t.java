 package org.meta_environment.eclipse.visualization;
 
 import nl.cwi.sen1.relationstores.Factory;
 import nl.cwi.sen1.relationstores.types.RStore;
 import nl.cwi.sen1.relationstores.types.RTuple;
 import nl.cwi.sen1.relationstores.types.RTupleRtuples;
 
 import org.eclipse.imp.pdb.facts.IValue;
 import org.meta_environment.eclipse.facts.FactsTool;
 import org.meta_environment.eclipse.viz.graph.GraphView;
 
import toolbus.adapter.eclipse.EclipseTool;
 import aterm.ATerm;
 
public class VisualizationTool extends EclipseTool {
 	
 	private static Factory factory;
 	
 	private static class InstanceKeeper {
 		private static VisualizationTool sInstance = new VisualizationTool();
 		static {
 			sInstance.connect();
 			factory = Factory.getInstance(getFactory());
 		}
 	}
 
 	public static VisualizationTool getInstance() {
 		return InstanceKeeper.sInstance;
 	}
 
 	private VisualizationTool() {
 		super("visualization-tool");
 	}
 	
 	public void viewGraph(final String path, final ATerm aterm) {
 		FactsTool factsTool = FactsTool.getInstance(); 
 		RStore store = factory.RStoreFromTerm(aterm);
 		
 		System.out.printf("path: %s, rstore: %s\n", path, store.toString());
 		
 		RTupleRtuples tuples = store.getRtuples();
 
 		if (!tuples.isEmpty()) {
 			RTuple tuple = tuples.getHead();
 			IValue value = factsTool.convertValue(tuple.getValue(), tuple.getRtype());
 			
 			GraphView.showGraph(path, value);
 		}		
 	}	
 }
