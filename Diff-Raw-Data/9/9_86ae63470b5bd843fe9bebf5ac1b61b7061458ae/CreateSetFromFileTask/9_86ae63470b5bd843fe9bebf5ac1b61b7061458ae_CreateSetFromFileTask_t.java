 package edu.ucsf.rbvi.setsApp.internal.tasks;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyRow;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.work.AbstractTask;
 import org.cytoscape.work.ProvidesTitle;
 import org.cytoscape.work.TaskMonitor;
 import org.cytoscape.work.Tunable;
 import org.cytoscape.work.util.ListSingleSelection;
 
 import edu.ucsf.rbvi.setsApp.internal.model.Set;
 import edu.ucsf.rbvi.setsApp.internal.model.SetsManager;
 
 public class CreateSetFromFileTask extends AbstractTask {
 
 	private ListSingleSelection<String> type = new ListSingleSelection<String>("Node", "Edge");
 
 	@ProvidesTitle
 	public String getTitle() { return "Import set from file"; }
 
 	// @Tunable(description="Type of set to import:", gravity=1.0)
 	@Tunable(description="Type of set to import:")
 	public ListSingleSelection<String> getType() {
 		return type;
 	}
 	public void setType(ListSingleSelection<String>input) {}
 
 	// @Tunable(description="Select column to use for ID:", listenForChange="Type", gravity=2.0)
 	@Tunable(description="Select column to use for ID:", listenForChange="Type")
 	public ListSingleSelection<String> getColumns() {
 		if (type.getSelectedValue().equals("Node")) {
 			return nodesColumn;
 		} else {
 			return edgesColumn;
 		}
 	}
 	public void setColumns(ListSingleSelection<String>input) {}
 
 	public ListSingleSelection<String> nodesColumn = null;
 
 	public ListSingleSelection<String> edgesColumn = null;
 
 	// @Tunable(description="Enter name for new set:", gravity=.5)
 	@Tunable(description="Enter name for new set:")
 	public String name;
 
 	@Tunable(description="File containing set data", params="input=true;fileCategory=unspecified")
 	public File setFile;
 	
 	private SetsManager mgr;
 	private CyNetwork network = null;
 	
 	public CreateSetFromFileTask(SetsManager setsManager, CyNetworkManager cnm) {
 		mgr = setsManager;
 		java.util.Set<CyNetwork> networkSet = cnm.getNetworkSet();
 		for (CyNetwork net: networkSet) {
 			if (net.getRow(net).get(CyNetwork.SELECTED, Boolean.class))
 				network = net;
 		}
 		if (network != null) {
 			CyTable table = null;
 			table = network.getDefaultNodeTable();
 			if (table != null) {
 				Collection<CyColumn> cyColumns = table.getColumns();
 				ArrayList<String> columnNames = new ArrayList<String>();
 				columnNames.add("none");
 				for (CyColumn c: cyColumns) 
 					if (c.getType() == String.class) columnNames.add(c.getName());
 				nodesColumn = new ListSingleSelection<String>(columnNames);
 				nodesColumn.setSelectedValue("none");
 			}
 			table = network.getDefaultEdgeTable();
 			if (table != null) {
 				Collection<CyColumn> cyColumns = table.getColumns();
 				ArrayList<String> columnNames = new ArrayList<String>();
 				for (CyColumn c: cyColumns) 
 					if (c.getType() == String.class) columnNames.add(c.getName());
 				columnNames.add("none");
 				edgesColumn = new ListSingleSelection<String>(columnNames);
 				edgesColumn.setSelectedValue("none");
 			}
 		}
 	}
 	
 	@Override
 	public void run(TaskMonitor arg0) throws Exception {
 		if (network != null && nodesColumn != null) {
 			BufferedReader reader = new BufferedReader(new FileReader(setFile));
 			if (! nodesColumn.getSelectedValue().equals("none") && edgesColumn.getSelectedValue().equals("none"))
 				createSetFromStream(name, nodesColumn.getSelectedValue(), reader, CyNode.class);
 			if (! edgesColumn.getSelectedValue().equals("none") && nodesColumn.getSelectedValue().equals("none"))
 				createSetFromStream(name, edgesColumn.getSelectedValue(), reader, CyEdge.class);
 		}
 	}
 
 	private void createSetFromStream(String name, String column, 
 	                                 BufferedReader reader, Class<? extends CyIdentifiable> setType) throws Exception {
 		if (mgr.getSet(name) != null)
 			throw new Exception("Set \"" + name + "\" already exists. Choose a different name.");
 
 		CyTable table;
 		Set<? extends CyIdentifiable> newSet;
 
 		if (setType == CyNode.class) {
 			table = network.getDefaultNodeTable();
 			newSet = new Set<CyNode>(name, network, CyNode.class);
 		} else {
 			table = network.getDefaultEdgeTable();
 			newSet = new Set<CyEdge>(name, network, CyEdge.class);
 		}
 
 		// Use a HashSet to avoid duplicates
 		HashSet<Long> matches = new HashSet<Long>();
 
 		String curLine;
 		while ((curLine = reader.readLine()) != null) {
 			Collection<CyRow> rows = table.getMatchingRows(column, curLine);
 			for (CyRow row: rows) {
 				matches.add(row.get(CyIdentifiable.SUID, Long.class));
 			}
 		}
 
 		if (matches.size() > 0) {
 			// Now, add our matches to our set
 			newSet.addElementsByID(new ArrayList<Long>(matches));
 		}
		mgr.addSet(newSet);
 	}
 }
