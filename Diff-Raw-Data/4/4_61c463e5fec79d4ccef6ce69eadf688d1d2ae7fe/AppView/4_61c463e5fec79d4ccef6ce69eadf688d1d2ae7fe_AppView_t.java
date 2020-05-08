 package generator;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 
 import parser.VisualParadigmXmlParser;
 import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import graph.DirectedGraph;
 import graph.Edge;
 import graph.Marker;
 import graph.Node;
 
 public class AppView extends JPanel implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static int logNumber = 1;
 
 	private AppController owner;
 
 	private File mInFile;
 	private File mOutFile;
 
 	private JButton mOpenButton;
 	private JButton mSaveButton;
 	private JButton mGenerateButton;
 
 	private JFileChooser mFileChooser;
 	private JTextArea mLog;
 	private JTextArea mGraphStatus;
 
 	private JButton mGraphGeneratingButton;
 
 	// komponent przechowujacy wizualizacje grafu
 	private VisualizationViewer<Node, Edge> mVisualizationViewer;
 	private JPanel mGraphsPanel;
 
 	public AppView(AppController owner) {
 		this();
 		this.owner = owner;
 	}
 
 	public AppView() {
 		super(new BorderLayout());
 		/* text areas */
 		mGraphStatus = new JTextArea(20, 30);
 		mGraphStatus.setMargin(new Insets(5, 5, 5, 5));
 		mGraphStatus.setEditable(false);
 		JScrollPane graphStatusScrollPane = new JScrollPane(mGraphStatus);
 
 		mLog = new JTextArea(8, 80);
 		mLog.setMargin(new Insets(5, 5, 5, 5));
 		mLog.setEditable(false);
 		mLog.setLineWrap(true);
 		JScrollPane logScrollPane = new JScrollPane(mLog);
 		logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		/* buttons */
 		mOpenButton = new JButton("Open a File...");
 		mOpenButton.addActionListener(this);
 
 		mSaveButton = new JButton("Save a File...");
 		mSaveButton.addActionListener(this);
 
 		mGenerateButton = new JButton("Generate formula!");
 		mGenerateButton.addActionListener(this);
 		mGenerateButton.setEnabled(false);
 
 		mGraphGeneratingButton = new JButton("generate sample graph");
 		mGraphGeneratingButton.addActionListener(this);
 
 		JPanel buttonPanel = new JPanel(); // use FlowLayout
 		buttonPanel.add(mOpenButton);
 		buttonPanel.add(mSaveButton);
 		buttonPanel.add(mGenerateButton);
 		buttonPanel.add(mGraphGeneratingButton);
 		// buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 		// JLabel inLabel = new JLabel("Input file");
 
 		/* graph */
 		mGraphsPanel = new JPanel();
 		// mGraphsPanel.setPreferredSize(new Dimension(400,400));
 
 		add(buttonPanel, BorderLayout.NORTH);
 		// add(inLabel);
 
 		add(mGraphsPanel, BorderLayout.LINE_START);
 		add(graphStatusScrollPane, BorderLayout.EAST);
 		add(logScrollPane, BorderLayout.SOUTH);
 
 		mFileChooser = new JFileChooser(new File("."));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == mOpenButton) {
 			int returnVal = mFileChooser.showOpenDialog(this);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				mInFile = mFileChooser.getSelectedFile();
 				mOpenButton.setText("In: " + mInFile.getName());
 				mGenerateButton.setEnabled(true);
 				owner.loadGraph(new VisualParadigmXmlParser().parse(mInFile.getAbsolutePath()));
 				owner.refresh();
 				mLog.setText("");
 			}
 		} else if (e.getSource() == mSaveButton) {
 			int returnVal = mFileChooser.showOpenDialog(this);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				mOutFile = mFileChooser.getSelectedFile();
 				mSaveButton.setText("Out: " + mOutFile.getName());
 			}
 		} else if (e.getSource() == mGenerateButton) {
 			generateFormula();
 		} else if (e.getSource() == mGraphGeneratingButton) {
 			generateSampleGraph();
 			mGenerateButton.setEnabled(true);
 		}
 	}
 
 	// do wywalenia
 	public void testOnly() {
 		generateSampleGraph();
 		mGenerateButton.setEnabled(true);
 		markGraph();
 	}
 
 	private void generateSampleGraph() {
 		DirectedGraph graph = new DirectedGraph();
 		graph.addVertex(new Node("t1", "t1", Marker.UNMARKED));
 		graph.addVertex(new Node("t2", "t2", Marker.UNMARKED));
 		graph.addVertex(new Node("t3", "t3", Marker.UNMARKED));
 		graph.addVertex(new Node("t4", "t4", Marker.UNMARKED));
 		graph.addVertex(new Node("t5", "t5", Marker.UNMARKED));
 		graph.addVertex(new Node("t6", "t6", Marker.UNMARKED));
 
 		graph.addVertex(new Node("t7", "t7", Marker.UNMARKED));
 		graph.addVertex(new Node("t8", "t8", Marker.UNMARKED));
 		graph.addVertex(new Node("t9", "t9", Marker.UNMARKED));
 		graph.addVertex(new Node("t10", "t10", Marker.UNMARKED));
 
 		graph.addVertex(new Node("t11", "t11", Marker.UNMARKED));
 		graph.addVertex(new Node("t12", "t12", Marker.UNMARKED));
 
 		graph.addVertex(new Node("g1", "g1", Marker.EXCLUSIVE_CHOICE));
 		graph.addVertex(new Node("g2", "g2", Marker.EXCLUSIVE_CHOICE));
 		graph.addVertex(new Node("g3", "g3", Marker.PARALLEL_SPLIT));
 		graph.addVertex(new Node("g4", "g4", Marker.PARALLEL_SPLIT));
 
 		graph.addEdge("t1", "g1");
 		graph.addEdge("g1", "t2");
 		graph.addEdge("g1", "t4");
 		graph.addEdge("t2", "t3");
 		graph.addEdge("t4", "g3");
 		graph.addEdge("g3", "t7");
 		graph.addEdge("g3", "t8");
 		graph.addEdge("t7", "t9");
 		graph.addEdge("t8", "t10");
 		graph.addEdge("t9", "t11");
 		graph.addEdge("t10", "t12");
 
 		graph.addEdge("t11", "g4");
 		graph.addEdge("t12", "g4");
 
 		graph.addEdge("g4", "t5");
 		graph.addEdge("t3", "g2");
 		graph.addEdge("t5", "g2");
 		graph.addEdge("g2", "t6");
 
 		owner.loadGraph(graph);
 		owner.refresh();
 		mLog.setText("");
 	}
 
 	public void drawGraph(DirectedGraph graph) {
 		Layout<Node, Edge> layout = new ISOMLayout<Node, Edge>(graph);
 		// layout.setSize(new Dimension(400, 400)); // bezposrednio wplywa na
 		// wielkosc wyswietlanych kolek
 
 		mGraphsPanel.removeAll();
 		mVisualizationViewer = new VisualizationViewer<Node, Edge>(layout);
 		mVisualizationViewer.setGraphMouse(new DefaultModalGraphMouse<Node, String>());
 		refreshNodeCaptions(graph);
 		mVisualizationViewer.setBackground(Color.WHITE);
 		mVisualizationViewer.setBorder(BorderFactory.createLoweredBevelBorder());
 		// mVisualizationViewer.setPreferredSize(new Dimension(500, 200));
 		mGraphStatus.setText("");
 		resetGraphStatus(graph);
 		mGraphsPanel.add(mVisualizationViewer);
 		mGraphsPanel.revalidate();
 	}
 
 	public void refreshNodeCaptions(DirectedGraph graph) {
 
 		mVisualizationViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Node>());
 		mVisualizationViewer.revalidate();
 		resetGraphStatus(graph);
 		mGraphsPanel.revalidate();
 
 	}
 
 	private void resetGraphStatus(DirectedGraph graph) {
 		mGraphStatus.setText("");
 		for (Node node : graph.getVertices()) {
 			mGraphStatus.append(node.toString() + "\n" + node.printStack());
 
 		}
 	}
 
 	// klikniecie przycisku
 	void generateFormula() {
 		owner.generateFormula();
 		// printToConsole("Wykonano 1 iteracje");
 	}
 
 	// klikniecie przycisku
 	void markGraph() {
 		owner.markVertices();
 		// printToConsole("Wykonano 1 iteracje");
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new AppController();
//		 edu.uci.ics.jung.samples.ShowLayouts.main(args);
 	}
 
 	public void printToConsole(String message) {
 		mLog.append(logNumber + ": " + message + "\n");
 		logNumber++;
 	}
 
 	public void foundFormulaHandling() {
 		mGraphGeneratingButton.setEnabled(false);
 		mGenerateButton.setEnabled(false);
 
 	}
 
 	public void setOutFile(File outFile) {
 		this.mOutFile = outFile;
 	}
 
 	public File getOutFile() {
 		return mOutFile;
 	}
 }
