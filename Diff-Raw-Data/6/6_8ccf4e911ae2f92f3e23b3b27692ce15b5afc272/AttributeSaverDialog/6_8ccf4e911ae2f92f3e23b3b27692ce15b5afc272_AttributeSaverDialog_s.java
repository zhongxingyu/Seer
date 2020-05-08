 package cytoscape.data;
 
 import cytoscape.Cytoscape;
 import cytoscape.CyNetwork;
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.plugin.CytoscapePlugin;
 import cytoscape.CytoscapeInit;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JLabel;
 import javax.swing.JDialog;
 import javax.swing.JList;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.JScrollPane;
 import javax.swing.JFileChooser;
 import javax.swing.BoxLayout;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.event.TableModelEvent;
 import javax.swing.tree.TreePath;
 import javax.swing.table.TableModel;
 import javax.swing.event.TableModelListener;
 
 import java.awt.Container;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import java.util.Collection;
 import java.util.Collections;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.LinkedList;
 import javax.swing.JPanel;
 
 /**
  * This class provides a Dialog box to save various attributes
  */
 
 public class AttributeSaverDialog extends JDialog {
 	/**
 	 * Default suffix for node attribute files
 	 */
 	protected static String NODE_SUFFIX = ".NA";
 	/**
 	 * Default suffix for edge attribute files
 	 */
 	protected static String EDGE_SUFFIX = ".EA";
 
 	/**
 	 * The max preferred size for the jscrollpane, will not let the jtable
 	 * expand past this point
 	 */
 	protected static int MAX_PREFERRED_SIZE = 100;
 
 	/**
 	 * Constant ot specify nodes
 	 */
 	protected static int NODES = 0;
 
 	/**
 	 * Constant to specify edges
 	 */
 	protected static int EDGES = 1;
 
 	/**
 	 * Show a dialog of hte specified type, see above constants
 	 */
 	protected static void showDialog(int type) {
 		AttributeSaverDialog dialog = new AttributeSaverDialog(type);
 		dialog.setVisible(true);
 		return;
 	}
 
 	/**
 	 * Show a dialog box to save edge attributes
 	 */
 	public static void showEdgeDialog() {
 		showDialog(EDGES);
 	}
 
 	/**
 	 * Show a dialog box to save node attributes
 	 */
 	public static void showNodeDialog() {
 		showDialog(NODES);
 	}
 
 	/**
 	 * The state associated with the attribute table, keeps track of the
 	 * attribute, filename and booleans. Edited through the jtable
 	 */
 	AttributeSaverState state;
 
 	/**
 	 * JTable for displaying boolean, attribute and filename
 	 */
 	JTable attributeTable;
 
 	/**
 	 * Create a dialog box of the specified type. Instead of constructor, use
 	 * static methods to create dialog box
 	 */
 	public AttributeSaverDialog(int type) {
 		super(Cytoscape.getDesktop(), "Save Attributes", true);
 		Container contentPane = getContentPane();
 		contentPane.setLayout(new BorderLayout());
 
 		// get the current CyNetwork
 		CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
 
 		// get the graph attributes
                 String[] edgeAttributes = Cytoscape.getEdgeAttributes().getAttributeNames();
 // 		String[] edgeAttributes = currentNetwork.getEdgeAttributesList();
                 String[] nodeAttributes = Cytoscape.getNodeAttributes().getAttributeNames();
 // 		String[] nodeAttributes = currentNetwork.getNodeAttributesList();
 
 		// create the objects which will maintain the state of the dialog
 		String suffix = null;
 		String[] attributes = null;
 		if (type == NODES) {
 			suffix = NODE_SUFFIX;
                         attributes = Cytoscape.getNodeAttributes().getAttributeNames();
 // 			attributes = currentNetwork.getNodeAttributesList();
 		} // end of if ()
 		else {
 			suffix = EDGE_SUFFIX;
                         attributes = Cytoscape.getEdgeAttributes().getAttributeNames();
 // 			attributes = currentNetwork.getEdgeAttributesList();
 		} // end of else
 
 		state = new AttributeSaverState(attributes, suffix, type, Cytoscape
 				.getCurrentNetwork());
 
 		String toolTipText = "Select multiple attributes to save. Modify \"Filename\" field to specify filename";
 		attributeTable = new JTable(state);
 		attributeTable.setToolTipText(toolTipText);
 		attributeTable.setCellSelectionEnabled(false);
 		// initialize the directory browser component
 
 		JButton saveButton = new JButton("Choose Directory and Save");
 		saveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				JFileChooser myChooser = new JFileChooser(CytoscapeInit
 						.getMRUD());
 				myChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				if (myChooser.showOpenDialog(Cytoscape.getDesktop()) == JFileChooser.APPROVE_OPTION) {
 					state.setSaveDirectory(myChooser.getSelectedFile());
 					CytoscapeInit.setMRUD(myChooser.getSelectedFile());
 					int count = state.writeState(attributeTable
 							.getSelectedRows());
 					JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 							"Successfully saved " + count + " files");
 					AttributeSaverDialog.this.dispose();
 				}
 			}
 		});
 
 		JPanel centerPanel = new JPanel();
 		JScrollPane scrollPane = new JScrollPane(attributeTable);
 		// scrollPane.setPreferredSize(new
 		// Dimension(MAX_PREFERRED_SIZE,(int)Math.min(MAX_PREFERRED_SIZE,attributeTable.getPreferredSize().getHeight()+attributeTable.getRowCount()*attributeTable.getRowMargin()+attributeTable.getRowHeight())));
 		scrollPane.setPreferredSize(new Dimension(MAX_PREFERRED_SIZE,
 				MAX_PREFERRED_SIZE));
 		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
 		centerPanel
 				.add(new JLabel(
 						"Select multiple attributes to save. Edit table to change filenames"));
 		centerPanel.add(scrollPane);
 
 		JPanel southPanel = new JPanel();
 		southPanel.add(saveButton);
 
 		contentPane.add(centerPanel, BorderLayout.CENTER);
 		contentPane.add(southPanel, BorderLayout.SOUTH);
 		pack();
 	}
 
 }
 
 /**
  * Holds the state associated with the dialog
  */
 class AttributeSaverState implements TableModel {
 	public static String newline = System.getProperty("line.separator");
 	/**
 	 * The default string to append for an attribute filename
 	 */
 	protected String suffix;
 
 	/**
 	 * The directory in which to save the files
 	 */
 	File saveDirectory;
 
 	/**
 	 * Type of graph object to save
 	 */
 	int type;
 
 	/**
 	 * List of all attributes
 	 */
 	Vector attributes;
 
 	/**
 	 * List of all filenames
 	 */
 	Vector filenames;
 
 	/**
 	 * List of all booleans, tells whether to save
 	 */
 	Vector booleans;
 
 	/**
 	 * A vector of all the objects that are listening to this TableModel
 	 */
 	Vector listeners;
 	/**
 	 * Network to from which to read graph objects
 	 */
 	CyNetwork cyNetwork;
 
 	// colum identities
 	protected static final int FILE_COLUMN = 2;
 	protected static final int ATTRIBUTE_COLUMN = 1;
 	protected static final int SAVE_COLUMN = 0;
 
 	/**
 	 * Initialize the state
 	 * 
 	 * @param nodeAttributes
 	 *            An array of strings containing all node attributes
 	 * @param type
 	 *            operate on NODES or EDGES
 	 * @param cyNetwork
 	 *            the network to save
 	 */
 	public AttributeSaverState(String[] nodeAttributes, String suffix,
 			int type, CyNetwork cyNetwork) {
 		this.type = type;
 		this.cyNetwork = cyNetwork;
 		this.listeners = new Vector();
 		this.attributes = new Vector();
 		this.filenames = new Vector();
 		this.booleans = new Vector();
 		for (int idx = 0; idx < nodeAttributes.length; idx++) {
 			attributes.add(nodeAttributes[idx]);
 		} // end of for ()
 		Collections.sort(attributes);
 
 		for (Iterator stringIt = attributes.iterator(); stringIt.hasNext();) {
 			String attribute = (String) stringIt.next();
 			filenames.add(attribute + suffix);
 			booleans.add(new Boolean(false));
 		} // end of for ()
 	}
 
 	/**
 	 * Set the directory where the files will be saved to
 	 */
 	public void setSaveDirectory(File saveDirectory) {
 		this.saveDirectory = saveDirectory;
 	}
 
 	/**
 	 * Write out the state for the given attributes
 	 * 
 	 * @return number of files successfully saved, the better way to do this
 	 *         would just be to throw the error and display a specific message
 	 *         for each failure, but oh well.
 	 */
 	public int writeState(int[] selectedRows) {
 		List graphObjects = null;
 
 		CyAttributes cyAttributes = null;
 
 		if (type == AttributeSaverDialog.NODES) {
 			cyAttributes = Cytoscape.getNodeAttributes();
 			graphObjects = Cytoscape.getCyNodesList();
 		} else {
 			cyAttributes = Cytoscape.getEdgeAttributes();
 			graphObjects = Cytoscape.getCyEdgesList();
 		} // end of else
 
 		Vector canonicalNames = new Vector();
 		for (Iterator objIt = graphObjects.iterator(); objIt.hasNext();) {
			String canonicalName = cyAttributes.getStringAttribute((String) objIt.next(),"canonicalName");
 			if (canonicalName != null) {
 				canonicalNames.add(canonicalName);
 			} // end of if ()
 			else {
 				System.err.println("Canonical name not found");
 			} // end of else
 		} // end of for ()
 
 		int count = 0;
 		for (int idx = 0; idx < attributes.size(); idx++) {
 			if (((Boolean) booleans.get(idx)).booleanValue()) {
 				try {
 					String attribute = (String) attributes.get(idx);
 					// File attributeFile = new
 					// File(saveDirectory,(String)attribute2File.get(attribute));
 					File attributeFile = new File(saveDirectory,
 							(String) filenames.get(idx));
 					FileWriter fileWriter = new FileWriter(attributeFile);
 					fileWriter.write(attribute + newline);
 					MultiHashMap attributeMap = cyAttributes.getMultiHashMap();
 					if (attributeMap != null) {
 						for (Iterator canonicalIt = canonicalNames.iterator(); canonicalIt
 								.hasNext();) {
 							String name = (String) canonicalIt.next();
 							//Object value = attributeMap..get(name);
							Object value = attributeMap.getAttributeValue(attribute,name,null);
 							
 							if (value != null) {
 								if (value instanceof Collection) {
 									String result = name + " = ";
 									Collection collection = (Collection) value;
 									if (collection.size() > 0) {
 										Iterator objIt = collection.iterator();
 										result += "(" + objIt.next();
 										while (objIt.hasNext()) {
 											result += "::" + objIt.next();
 										}
 										result += ")" + newline;
 										fileWriter.write(result);
 									}
 								} else {
 									fileWriter.write(name + " = " + value
 											+ newline);
 								}
 							} else {
 								// System.err.println("Value was null for
 								// "+name);
 							}
 						}
 					} else {
 						// System.err.println("Attribute name map is null");
 					}
 					fileWriter.close();
 					count++;
 				} catch (Exception e) {
 					e.printStackTrace();
 				} // end of try-catch
 			}
 		} // end of for ()
 		return count;
 	}
 
 	// below here is implementing the tableModel
 	// see the interface for description of the methods
 	public void addTableModelListener(TableModelListener tml) {
 		this.listeners.add(tml);
 		return;
 	}
 
 	public void removeTableModelListener(TableModelListener tml) {
 		this.listeners.remove(tml);
 		return;
 	}
 
 	public java.lang.Class getColumnClass(int columnIndex) {
 		if (columnIndex == SAVE_COLUMN) {
 			return Boolean.class;
 		} // end of if ()
 		else {
 			return String.class;
 		}
 	}
 
 	public int getColumnCount() {
 		return 3;
 	}
 
 	public int getRowCount() {
 		return attributes.size();
 	}
 
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		switch (columnIndex) {
 		case SAVE_COLUMN:
 			return booleans.get(rowIndex);
 		case ATTRIBUTE_COLUMN:
 			return attributes.get(rowIndex);
 		case FILE_COLUMN:
 			return filenames.get(rowIndex);
 		default:
 			throw new IllegalArgumentException();
 		} // end of switch ()
 	}
 
 	public String getColumnName(int columnIndex) {
 		switch (columnIndex) {
 		case SAVE_COLUMN:
 			return "Save";
 		case ATTRIBUTE_COLUMN:
 			return "Attribute";
 		case FILE_COLUMN:
 			return "Filename";
 		default:
 			throw new IllegalArgumentException();
 		} // end of switch ()
 	}
 
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		if (columnIndex != ATTRIBUTE_COLUMN) {
 			return true;
 		} // end of if ()
 		return false;
 	}
 
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		switch (columnIndex) {
 		case ATTRIBUTE_COLUMN:
 			throw new RuntimeException("Cell is not editable");
 		case SAVE_COLUMN:
 			booleans.set(rowIndex, aValue);
 			break;
 		case FILE_COLUMN:
 			filenames.set(rowIndex, aValue);
 			break;
 		default:
 			break;
 		} // end of switch ()
 	}
 
 }
