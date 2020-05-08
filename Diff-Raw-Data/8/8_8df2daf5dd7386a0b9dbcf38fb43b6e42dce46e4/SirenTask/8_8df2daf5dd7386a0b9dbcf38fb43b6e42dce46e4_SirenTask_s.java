 package ca.utoronto.siren.internal;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyRow;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.work.AbstractTask;
 import org.cytoscape.work.ProvidesTitle;
 import org.cytoscape.work.TaskMonitor;
 import org.cytoscape.work.Tunable;
 import org.cytoscape.work.util.ListMultipleSelection;
 import org.cytoscape.work.util.ListSingleSelection;
 
 public class SirenTask extends AbstractTask {
 	static final String READ_FROM_FILE = "File";
 	static final String READ_FROM_ATTRIBUTES = "Node attributes";
 	
 	@Tunable(description="Use gene expression values from",
 			 groups={"Step 1"})
 	public ListSingleSelection<String> readFrom;
 	
 	@Tunable(description="Gene expression file name",
 			 groups={"Step 2a: Select gene expression file"},
 			 params="input=true",
 			 dependsOn="readFrom=" + READ_FROM_FILE)
 	public File expressionFile;
 
 	@Tunable(description="Gene identifier",
 			 groups={"Step 2a: Select gene expression file"},
 			 dependsOn="readFrom=" + READ_FROM_FILE)
 	public ListSingleSelection<String> nodeIdentifier;
 
 	@Tunable(groups={"Step 2b: Select gene expression attribute(s)"},
 			 dependsOn="readFrom=" + READ_FROM_ATTRIBUTES)
 	public ListMultipleSelection<String> attributeNames;
 	
 	private CyNetwork network;
 	
 	public SirenTask(CyNetwork network) {
 		this.network = network;
 		
 		CyTable table = network.getDefaultNodeTable();
 		List<String> expressionColumnNames = new ArrayList<String>();
 		List<String> columnNames = new ArrayList<String>();
 		for (CyColumn column : table.getColumns()) {
 			Class<?> type = column.getType();
 			if (Double.class.equals(type) || Integer.class.equals(type)) {
 				expressionColumnNames.add(column.getName());
 			}
 			columnNames.add(column.getName());
 		}
 		
 		if (expressionColumnNames.size() == 0) {
 			readFrom = new ListSingleSelection<String>(READ_FROM_FILE);
 			
 			// ListMultipleSelection needs at least 1 element or it'll
 			// cause a NPE.
 			attributeNames = new ListMultipleSelection<String>("");
 		} else {
 			readFrom = new ListSingleSelection<String>(READ_FROM_FILE, READ_FROM_ATTRIBUTES);
 			Collections.sort(expressionColumnNames);
 			attributeNames = new ListMultipleSelection<String>(expressionColumnNames);
 		}
 		
 		Collections.sort(columnNames);
 		nodeIdentifier = new ListSingleSelection<String>(columnNames);
 		nodeIdentifier.setSelectedValue(CyNetwork.NAME);
 	}
 	
 	@ProvidesTitle
 	public String getTitle() {
 		return "Compute SIREN scores for " + network.getRow(network).get(CyNetwork.NAME, String.class);
 	}
 	
 	@Override
 	public void run(TaskMonitor taskMonitor) throws Exception {
 		List<String> columnNames = attributeNames.getSelectedValues();
 		CyColumn[] columns = getColumns(network, columnNames);
 		
 		List<CyNode> nodes = network.getNodeList();
 		List<CyEdge> edges = network.getEdgeList();
 		
 		taskMonitor.setTitle("Computing SIREN scores...");
 		taskMonitor.setStatusMessage(String.format("Computing SIREN scores for %d interactions, %d gene, and %d conditions...", edges.size(), nodes.size(), columnNames.size()));
 		
 		int[][] networkMatrix = extractNetworkMatrix(nodes, edges);
 		
 		double[][] expressionMatrix;
 		if (READ_FROM_FILE.equals(readFrom.getSelectedValue())) {
 			expressionMatrix = loadExpressionData(expressionFile, network, nodes, nodeIdentifier.getSelectedValue());
 		} else {
 			expressionMatrix = extractExpressionMatrix(network, nodes, columns);
 		}
 		
 		double[] scores = Siren.computeScores(expressionMatrix, networkMatrix, Siren.DEFAULT_WEIGHT_MATRIX);
 		
 		CyTable table = network.getDefaultEdgeTable();
 		String columnName = "SIREN";
 		if (table.getColumn(columnName) == null) {
 			table.createColumn(columnName, Double.class, false);
 		}
 		
 		for (int i = 0; i < scores.length; i++) {
 			CyEdge edge = edges.get(i);
 			network.getRow(edge).set(columnName, scores[i]);
 		}
 	}
 
 	private static CyColumn[] getColumns(CyNetwork network, List<String> columnNames) {
 		CyTable table = network.getDefaultNodeTable();
 		CyColumn[] columns = new CyColumn[columnNames.size()];
 		int index = 0;
 		for (String name : columnNames) {
 			columns[index++] = table.getColumn(name);
 		}
 		return columns;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static double[][] extractExpressionMatrix(CyNetwork network, List<CyNode> nodes, CyColumn[] columns) {
 		double[][] result = new double[nodes.size()][columns.length];
 		int nodeIndex = 0;
 		for (CyNode node : nodes) {
 			for (int i = 0; i < columns.length; i++) {
 				CyColumn column = columns[i];
 				CyRow row = network.getRow(node);
 				Class<Number> type = (Class<Number>) column.getType();
 				Number number = row.get(column.getName(), type);
 				result[nodeIndex][i] = number == null ? Double.NaN : number.doubleValue(); 
 			}
 			nodeIndex++;
 		}
 		return result;
 	}
 
 	private static int[][] extractNetworkMatrix(List<CyNode> nodes, List<CyEdge> edges) {
 		Map<CyNode, Integer> nodeIndexes = new IdentityHashMap<CyNode, Integer>();
 		int nodeIndex = 0;
 		for (CyNode node : nodes) {
 			nodeIndexes.put(node, nodeIndex++);
 		}
 
 		int[][] result = new int[edges.size()][];
 		int edgeIndex = 0;
 		for (CyEdge edge : edges) {
 			result[edgeIndex++] = new int[] {
 				nodeIndexes.get(edge.getSource()),
 				nodeIndexes.get(edge.getTarget()),
 			};
 		}
 		return result;
 	}
 	
 	private static double[][] loadExpressionData(File file, CyNetwork network, List<CyNode> nodes, String identifier) throws IOException {
 		CyTable table = network.getDefaultNodeTable();
 		CyColumn column = table.getColumn(identifier);
 		Class<?> type = column.getType();
 		
 		Map<String, Integer> nodeIndexes = new HashMap<String, Integer>();
 		int nodeIndex = 0;
 		for (CyNode node : nodes) {
 			CyRow row = network.getRow(node);
 			nodeIndexes.put(row.get(identifier, type).toString(), nodeIndex++);
 		}
 		
 		int[] dimensions = Siren.getMatrixDimensions(file.getPath());
 		// First column is gene name
 		int columns = dimensions[1] - 1;
 		int rows = dimensions[0];
 		
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		double[][] result = new double[rows][columns];
 		Siren.clearMatrix(result, Double.NaN);
 		try {
 			String line = reader.readLine();
 			while (line != null) {
 				try {
 					String[] values = line.split("\t");
					int rowIndex = nodeIndexes.get(values[0]);
 					for (int i = 1; i < values.length; i++) {
 						result[rowIndex][i - 1] = Double.parseDouble(values[i]);
 					}
 				} finally {
 					line = reader.readLine();
 				}
 			}
 		} finally {
 			reader.close();
 		}
 		return result;
 	}
 }
