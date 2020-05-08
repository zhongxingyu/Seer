 package gui.turing;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.UUID;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import machine.turing.*;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 
 import machine.turing.Edge;
 import machine.turing.State;
 import machine.turing.TuringMachine;
 
 import com.mxgraph.swing.mxGraphComponent;
 
 import com.mxgraph.view.mxGraphSelectionModel;
 import com.mxgraph.view.mxGraph;
 import com.mxgraph.view.mxStylesheet;
 import com.mxgraph.model.*;
 
 import com.mxgraph.util.mxConstants;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventSource;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.util.mxEventObject;
 
 
 import gui.MachineEditor;
 
 public class TuringMachineEditor extends MachineEditor implements KeyListener, ItemListener, ActionListener, MouseListener {
 	private static final long serialVersionUID = 7647012826073382156L;
 	private final int GRID_SIZE = 100;
 	private TuringMachine machine = null;
 	private Object selectedObject = null;
 	private StateList graphicalStates = null;
 	private ArrayList<mxCell> graphicalEdges = null;
 
 	protected JPanel jPanelLeft = null;
 	protected JPanel jPanelGraph = null;
 	protected mxGraph graph = null;
 	protected JSplitPane jSplitPaneHorizontal = null;
 	protected JPanel jPanelToolBox = null;
 	protected JPanel jPanelProperties = null;
 	protected ToolBox toolBox = new ToolBox();
 
 	private JMenu editMenu;
 	private JMenu viewMenu;
 	private JMenuItem copyAction;
 	private JMenuItem cutAction;
 	private JMenuItem pasteAction;
 	private JCheckBoxMenuItem gridToggleAction;
 	
 	private boolean gridEnabled = false;
 
 	/**
 	 * 
 	 * @author Philipp
 	 * Nested class to extend ArrayList<mxCell> to find mxCells with specified State Object
 	 */
 	class StateList extends ArrayList<mxCell>{
 		public StateList(int size){
 			super(size);
 		}
 		/**
 		 * Method to find mxCell with specified value of type State
 		 * @param state
 		 * @return mxCell
 		 */
 		mxCell getMxCell(State state){							
 			for (int i = 0; i < this.size(); i++) {
 				if(this.get(i).getValue().equals((Object) state)){
 					return this.get(i);
 				}
 			}
 			return null;
 		}
 	}
 
 	public TuringMachineEditor(TuringMachine machine) {
 		super();
 		this.machine = machine;
 
 		this.initEditor();
 
 		this.graphicalStates = new StateList(machine.getStates().size());
 		this.graphicalEdges = new ArrayList<mxCell>(machine.getEdges().size());
 
 		//create left panel
 		this.jPanelLeft = new JPanel();
 		this.jPanelLeft.setLayout(new BorderLayout());
 		this.jPanelToolBox = new JPanel();
 		this.jPanelProperties = new JPanel();
 		this.jPanelLeft.add(this.jPanelToolBox, BorderLayout.NORTH);
 		this.jPanelLeft.add(this.jPanelProperties, BorderLayout.CENTER);
         this.jPanelProperties.setLayout(new BorderLayout());
         this.jPanelToolBox.setLayout(new BorderLayout());
 
 		//create main graph panel
 		this.jPanelGraph = new JPanel();
 		this.jPanelGraph.setLayout(new BorderLayout());
 
 		//create split pane
 		this.jSplitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				this.jPanelLeft, this.jPanelGraph);
 		this.jSplitPaneHorizontal.setOneTouchExpandable(true);
 		this.jSplitPaneHorizontal.setDividerLocation(250);
 		Dimension minimumSize = new Dimension(100, 50);
 		this.jPanelLeft.setMinimumSize(minimumSize);
 
 
 		this.jPanelGraph.setMinimumSize(minimumSize);
 		this.setLayout(new BorderLayout());
 		this.add(this.jSplitPaneHorizontal, BorderLayout.CENTER);
 		jPanelToolBox.add(toolBox);
 
 		//create the graph
 		this.graph = new mxGraph();
 		this.graph.setAllowDanglingEdges(false);
 		this.graph.setAllowLoops(true);
 		this.graph.setAutoSizeCells(true);
 		this.graph.setCellsResizable(true);
 		this.graph.setCellsEditable(false);
 		this.graph.setAllowNegativeCoordinates(false);
 //		this.graph.setDefaultLoopStyle(null);
 		this.graph.addListener(mxEvent.MOVE_CELLS, new mxIEventListener() {
 
 			@Override
 			public void invoke(Object obj, mxEventObject e) {
 				
 				for(Object cellObj: (Object[]) e.getProperty("cells")){
 					mxCell cell = (mxCell) cellObj;
 					if(cell.isVertex()){
 						int x = (int) cell.getGeometry().getX();
 						int y = (int) cell.getGeometry().getY();
 						((State)cell.getValue()).setXcoord((int)cell.getGeometry().getX());
 						((State)cell.getValue()).setYcoord((int)cell.getGeometry().getY());
 						x = (int) Math.ceil(x / GRID_SIZE);
 						y = (int) Math.ceil(y / GRID_SIZE);
 						graph.getModel().beginUpdate();
 						try {
 							cell.setGeometry(new mxGeometry(x * GRID_SIZE, y * GRID_SIZE, cell.getGeometry().getWidth(), cell.getGeometry().getHeight()));
 							graph.repaint();
 						}
 						finally {
 							graph.getModel().endUpdate();
 						}
 					}
 				}
 			}
 		});
 		this.graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {
 
 			@Override
 			public void invoke(Object obj, mxEventObject e) {
 				mxGraphSelectionModel model = (mxGraphSelectionModel) obj;
 				mxCell c = (mxCell) model.getCell();
 				if (c == null) {
 					jPanelProperties.removeAll();
 					jPanelProperties.validate();
 					jPanelProperties.repaint();
 				}
 					
 				for(Object cellObj: model.getCells()){
 					mxCell cell = (mxCell) cellObj;
 					if(cell.isVertex()){
 						displayProperties((State) cell.getValue());	
 
 					} else if (cell.isEdge()) {
 						displayProperties((Edge) cell.getValue());
 					}
 				}
 
 			}
 		});
 		
 		// set style
 		mxStylesheet stylesheet = graph.getStylesheet();
 		Hashtable<String, Object> style = new Hashtable<String, Object>();
 		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
 		stylesheet.putCellStyle("CIRCLE", style);
//		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_ELLIPSE);
//		stylesheet.putCellStyle("FINAL", style);
//		stylesheet.setStyles();
 		
 		this.drawGraph();
 		
 		mxGraphComponent graphComponent = new mxGraphComponent(graph);
 		graphComponent.addKeyListener(this);
 		graphComponent.getGraphControl().addMouseListener(this);
 		this.jPanelGraph.add(graphComponent, BorderLayout.CENTER);
 
 		//		if (this.machine != null && this.machine.getEdges().size() > 0) {
 		//			this.displayProperties(this.machine.getEdges().get(0));
 		//		}
 	}
 
 	/**
 	 * Initializes the editor
 	 */
 	public void initEditor() {
 		editMenu = new JMenu("Edit");
 		viewMenu = new JMenu("View");
 		copyAction = new JMenuItem("Copy");
 		cutAction = new JMenuItem("Cut");
 		pasteAction = new JMenuItem("Paste");
 		gridToggleAction = new JCheckBoxMenuItem("Grid enabled");
 		
 		editMenu.add(copyAction);
 		editMenu.add(cutAction);
 		editMenu.add(pasteAction);
 		viewMenu.add(gridToggleAction);
 		
 		this.getMenus().add(editMenu);
 		this.getMenus().add(viewMenu);
 
 		copyAction.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		cutAction.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		pasteAction.setAccelerator(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 
 		copyAction.addActionListener(this);
 		cutAction.addActionListener(this);
 		pasteAction.addActionListener(this);
 		gridToggleAction.addItemListener(this);
 	}
 
 
 	private void displayProperties(Edge edge) {
 		PropertiesEdge propertiesEdge = new PropertiesEdge(this.machine.getNumberOfTapes(), edge);
 		jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesEdge, BorderLayout.CENTER);
 		jPanelProperties.validate();
 	}
 
 	private void displayProperties(State state) {
 		PropertiesState propertiesState = new PropertiesState(state);
 		this.jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		this.jPanelProperties.add(propertiesState, BorderLayout.CENTER);
 		jPanelProperties.validate();
 	}
 
 	private void drawGraph(){
 		ArrayList<State> states = this.machine.getStates();
 		ArrayList<Edge> edges = this.machine.getEdges();
 
 		//load graphical states
 		graph.getModel().beginUpdate();
 		try	{
 			for (int i = 0;  i < states.size(); i++){
 				int x = states.get(i).getXcoord();
 				int y = states.get(i).getYcoord();
 				x = (int) Math.ceil(x / GRID_SIZE);
 				y = (int) Math.ceil(y / GRID_SIZE);
 				graphicalStates.add(i, (mxCell) graph.insertVertex(graph.getDefaultParent(), null, 
 
 				states.get(i), x * GRID_SIZE, y * GRID_SIZE, 
 				states.get(i).getWidth(), states.get(i).getHeight(), (states.get(i).isFinalState() ? "FINAL" : "CIRCLE")));
 			}
 			//insert graphical Edges
 			Edge currentEdge = null;
 			Object v1 = null;
 			Object v2 = null;
 			for (int i = 0; i < edges.size(); i++){
 				currentEdge = edges.get(i);
 				v1 = graphicalStates.getMxCell(currentEdge.getFrom());
 				v2 = graphicalStates.getMxCell(currentEdge.getTo());
 				graphicalEdges.add(i,(mxCell) graph.insertEdge(graph.getDefaultParent(), null, currentEdge, v1, v2));
 
 			}
 		} finally {
 			graph.getModel().endUpdate();
 		}
 	}
 	
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		if (e.getSource() == gridToggleAction) {
 			gridEnabled = gridToggleAction.isSelected();
 		}
 	}
 
 	/**
 	 * Responds to a clicked button
 	 * @param e ActionEvent that indicates changes
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == copyAction) {
 			JOptionPane.showMessageDialog(null, "Not implemented yet!");
 		}
 		else if (e.getSource() == cutAction) {
 			JOptionPane.showMessageDialog(null, "Not implemented yet!");
 		}
 		else if (e.getSource() == pasteAction) {
 			JOptionPane.showMessageDialog(null, "Not implemented yet!");
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (toolBox.getClicked() != null) {
 			int x = e.getX();
 			int y = e.getY();
 			int xGrid = (int) Math.ceil(x / GRID_SIZE);
 			int yGrid = (int) Math.ceil(y / GRID_SIZE);
 			graph.getModel().beginUpdate();
 			try	{
 				if (toolBox.getClicked().equals("State")) {
 					State state = new State(UUID.randomUUID().toString(), "New...", false, false);
 					state.setXcoord(x);
 					state.setXcoord(y);
 					this.machine.getStates().add(state);
 					graphicalStates.add((mxCell) graph.insertVertex(graph.getDefaultParent(), null, state, xGrid * GRID_SIZE, yGrid * GRID_SIZE, 50, 50, "CIRCLE"));
 					toolBox.setClicked(null);
 				}
 				else if (toolBox.getClicked().equals("System")) {
 
 				}
 
 				else if (toolBox.getClicked().equals("Text")) {
 
 				}
 			} finally {
 				graph.getModel().endUpdate();
 			}
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
 			Object[] deletedCells = this.graph.removeCells();
 			for (int i = 0; i < deletedCells.length; i++) {
 				mxCell currentCell = (mxCell) deletedCells[i];
 				if (currentCell.isEdge()) {
 					System.out.println("I'm not deleting anything from the machine :P");
 				}
 				else if(currentCell.isVertex()) {
 					System.out.println("I'm not deleting anything from the machine :P");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 	}
 }
