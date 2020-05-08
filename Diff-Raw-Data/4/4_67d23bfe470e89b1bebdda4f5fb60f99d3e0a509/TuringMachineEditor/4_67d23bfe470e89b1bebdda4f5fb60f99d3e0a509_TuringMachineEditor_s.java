 package gui.turing;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import machine.turing.Edge;
 import machine.turing.State;
 import machine.turing.TuringMachine;
 
 import com.mxgraph.swing.mxGraphComponent;
 
 import com.mxgraph.view.mxGraphSelectionModel;
 import com.mxgraph.view.mxGraph;
 
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventSource;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.util.mxEventObject;
 
 
 import gui.MachineEditor;
 
 public class TuringMachineEditor extends MachineEditor {
 	private TuringMachine machine = null;
 	private Object selectedObject = null;
 	
 	protected JPanel jPanelLeft = null;
 	protected JPanel jPanelGraph = null;
 	protected mxGraph graph = null;
 	protected JSplitPane jSplitPaneHorizontal = null;
 	protected JPanel jPanelToolBox = null;
 	protected JPanel jPanelProperties = null;
 	
 	public TuringMachineEditor(TuringMachine machine) {
 		super();
 		this.machine = machine;
 
 		//create left panel
 		this.jPanelLeft = new JPanel();
 
 		//create main graph panel
 		this.jPanelGraph = new JPanel();
 		this.jPanelGraph.setLayout(new BorderLayout());
 		
 		//create split pane
 		this.jSplitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				this.jPanelLeft, this.jPanelGraph);
 		this.jSplitPaneHorizontal.setOneTouchExpandable(true);
 		this.jSplitPaneHorizontal.setDividerLocation(150);
 		Dimension minimumSize = new Dimension(100, 50);
 		this.jPanelLeft.setMinimumSize(minimumSize);
 		this.jPanelGraph.setMinimumSize(minimumSize);
 		this.setLayout(new BorderLayout());
 		this.add(this.jSplitPaneHorizontal, BorderLayout.CENTER);
 
 		//create the graph
 		mxGraph graph = new mxGraph();
 
 		Object parent = graph.getDefaultParent();
 
 		graph.getModel().beginUpdate();
 		try
 		{
 			Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80,
 					30);
 			Object v2 = graph.insertVertex(parent, null, "World!", 240, 150,
 					80, 30);
 			graph.insertEdge(parent, null, "Edge", v1, v2);
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 
 		mxGraphComponent graphComponent = new mxGraphComponent(graph);
 		this.jPanelGraph.add(graphComponent, BorderLayout.CENTER);
 		
 		
 		System.out.println("TEST: " + this.machine);
 		//this.displayProperties(this.machine.getEdges().get(0));
 	}
 	
	private void displayProperties(Edge prop){
		PropertiesEdge propertiesEdge = new PropertiesEdge(3); //TODO: fix
 		jPanelProperties.removeAll();
 		jPanelProperties.add(propertiesEdge);
 	}
 	
 	private void displayProperties(State prop){
 		jPanelProperties.removeAll();
 		//jPanelProperties.add(prop);
 	}
 
 
 
 }
