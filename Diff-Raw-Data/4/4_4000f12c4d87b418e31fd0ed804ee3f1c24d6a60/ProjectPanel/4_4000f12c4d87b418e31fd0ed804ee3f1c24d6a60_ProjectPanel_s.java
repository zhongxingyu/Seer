 package org.geworkbench.builtin.projects;
 
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.ObjectInputStream;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.APSerializable;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.datastructure.properties.DSExtendable;
 import org.geworkbench.bison.datastructure.properties.DSNamed;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.bison.util.colorcontext.ColorContext;
 import org.geworkbench.builtin.projects.SaveFileFilterFactory.CustomFileFilter;
 import org.geworkbench.engine.config.GUIFramework;
 import org.geworkbench.engine.config.MenuListener;
 import org.geworkbench.engine.config.VisualPlugin;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.management.Publish;
 import org.geworkbench.engine.management.Script;
 import org.geworkbench.engine.management.Subscribe;
 import org.geworkbench.engine.management.TypeMap;
 import org.geworkbench.engine.preferences.GlobalPreferences;
 import org.geworkbench.engine.skin.Skin;
 import org.geworkbench.events.AdjacencyMatrixEvent;
 import org.geworkbench.events.CaArrayQueryEvent;
 import org.geworkbench.events.CaArrayQueryResultEvent;
 import org.geworkbench.events.CaArrayRequestEvent;
 import org.geworkbench.events.CommentsEvent;
 import org.geworkbench.events.HistoryEvent;
 import org.geworkbench.events.ImageSnapshotEvent;
 import org.geworkbench.events.NormalizationEvent;
 import org.geworkbench.events.PendingNodeCancelledEvent;
 import org.geworkbench.events.PendingNodeLoadedFromWorkspaceEvent;
 import org.geworkbench.events.ProjectEvent;
 import org.geworkbench.events.ProjectNodeAddedEvent;
 import org.geworkbench.events.ProjectNodePostCompletedEvent;
 import org.geworkbench.events.ProjectNodeRemovedEvent;
 import org.geworkbench.events.ProjectNodeRenamedEvent;
 import org.geworkbench.events.StructureAnalysisEvent;
 import org.geworkbench.parsers.DataSetFileFormat;
 import org.geworkbench.util.FilePathnameUtils;
 import org.geworkbench.util.SaveImage;
 import org.geworkbench.util.Util;
 import org.ginkgo.labs.ws.GridEndpointReferenceType;
 
 // TODO this class has a large number of subscribe methods,
 // many of these should be replaced with direct method call
 // if the event is specifically meant to be received by this class only.
 /**
  *
  * Description: Project Panel of geWorkbench is a key controlling element.
  * </p>
  * <p>
  * Copyright: Copyright (c) 2002
  * </p>
  * <p>
  * Company: First Genetic Trust Inc.
  * </p>
  *
  * @author First Genetic Trust
  * @version $Id$
  */
 @SuppressWarnings("unchecked")
 public class ProjectPanel implements VisualPlugin, MenuListener {
 
 	static Log log = LogFactory.getLog(ProjectPanel.class);
 
 	private static final String WORKSPACE_DIR = "workspaceDir";
 
 	/**
 	 * Additional Menu related instance variables that do not exist in parent
 	 */
 
 	private static TypeMap<ImageIcon> iconMap = new TypeMap<ImageIcon>();
 
 	static {
 		DefaultIconAssignments.initializeDefaultIconAssignments();
 	}
 
 	// Initialize default icons
 
 	private LoadData loadData = new LoadData(this);
 
 	private ProjectSelection selection = new ProjectSelection(this);
 
 	private HashMap<GridEndpointReferenceType, PendingTreeNode> eprPendingNodeMap = new HashMap<GridEndpointReferenceType, PendingTreeNode>();
 
 	// The undo buffer
 	ProjectTreeNode undoNode = null;
 
 	ProjectTreeNode undoParent = null;
 
 	/**
 	 * XQ uses dataSetMenu to save/modify the new generated/old Fasta file
 	 * dataSetSubMenu to save sequence alignment result.
 	 */
 	private JPopupMenu dataSetMenu = new JPopupMenu();
 
 	private JPopupMenu dataSetSubMenu = new JPopupMenu();
 
 	private JPopupMenu pendingMenu = new JPopupMenu();
 
 	JProgressBar progressBar = new JProgressBar();
 
 	private JMenuItem jRemoveProjectItem = new JMenuItem();
 
 	private JMenuItem jRemoveDatasetItem = new JMenuItem();
 
 	private JMenuItem jRemovePendingItem = new JMenuItem();
 
 	private JMenuItem jRemoveSubItem = new JMenuItem();
 
 	private JMenuItem jRenameSubItem = new JMenuItem();
 
 	private JMenuItem jEditItem = new JMenuItem();
 
 	private JMenuItem jViewAnnotations = new JMenuItem();
 
 	private JMenuItem jSaveMenuItem = new JMenuItem();
 
 	private JMenuItem jRenameMenuItem = new JMenuItem();
 
 	/*
 	 * enforce ProjectPanel to be singleton: 'regular' method of making
 	 * constructor private is not applicable because of the cglib
 	 * parsing/loading process
 	 */
 	private static ProjectPanel INSTANCE = null;
 
 	public static ProjectPanel getInstance() {
 		if (INSTANCE != null)
 			return INSTANCE;
 		else
 			try {
 				return new ProjectPanel();
 			} catch (Exception e) { // exception only for INSTANCE is not null
 				return INSTANCE;
 			}
 	}
 
 	/**
 	 * Constructor. Initialize GUI and selection variables
 	 *
 	 * @throws Exception
 	 */
 	public ProjectPanel() throws Exception {
 		// singleton: this constructor should never be called the second time.
 		if (INSTANCE != null)
 			throw new Exception(
 					"Second instance of ProjectPanle cannot be created.");
 
 		// Initializes Random number generator to generate unique ID's
 		// because of the unique seed
 		RandomNumberGenerator.setSeed(System.currentTimeMillis());
 
 		try {
 			initializeSwingComponents();
 			initializeMore();
 
 			// Checks if a default workspace exists and loads it
 			File defaultWS = new File("./default.wsp");
 			if (defaultWS.exists()) {
 				deserialize(defaultWS.getName());
 				Enumeration<?> children = root.children();
 				while (children.hasMoreElements()) {
 					TreeNode node = (TreeNode) children.nextElement();
 					projectTree.expandPath(new TreePath(node));
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		INSTANCE = this;
 	}
 
 	private void initializeMore() {
 
 		jSaveMenuItem.setText("Save");
 		jSaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveNodeAsFile();
 			}
 		});
 
 		jRenameMenuItem.setText("Rename");
 		jRenameMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameDataset_actionPerformed(e);
 			}
 		});
 
 		jRemoveDatasetItem.setText("Remove");
 		jRemoveDatasetItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 
 		jRemovePendingItem.setText("Remove");
 		jRemovePendingItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 
 		jRenameSubItem.setText("Rename");
 		jRenameSubItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameDataset_actionPerformed(e);
 			}
 		});
 
 		jRemoveSubItem.setText("Remove");
 		jRemoveSubItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 		jEditItem.setText("View in Editor");
 		jEditItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (selection.getSelectedNode() instanceof DataSetNode) {
 					DSDataSet<? extends DSBioObject> ds = selection.getDataSet();
 
 					GlobalPreferences prefs = GlobalPreferences.getInstance();
 					String editor = prefs.getTextEditor();
 					if (editor == null) {
 						log.info("No editor configured.");
 						JOptionPane
 						.showMessageDialog(
 								null,
 								"No editor configured.",
 								"Unable to Edit",
 								JOptionPane.INFORMATION_MESSAGE);
 					} else {
 						if (ds.getFile() == null) {
 							JOptionPane
 									.showMessageDialog(
 											null,
 											"There is no local file for this data set.",
 											"Unable to Edit",
 											JOptionPane.INFORMATION_MESSAGE);
 						} else {
 							if (Util.isRunningOnAMac()) {
 								editor = "Open";
 							}
 
 							String[] args = { editor,
 									ds.getFile().getAbsolutePath() };
 							try {
 								Runtime.getRuntime().exec(args);
 							} catch (IOException e1) {
 								log.info("Error opening editor:");
 								JOptionPane
 								.showMessageDialog(
 										null,
 										"IOException in opening editor: "+e1.getMessage(),
 										"Unable to Edit",
 										JOptionPane.INFORMATION_MESSAGE);
 								e1.printStackTrace();
 							} catch (SecurityException se) {
 								JOptionPane
 								.showMessageDialog(
 										null,
 										"SecurityException in opening editor: "+se.getMessage(),
 										"Unable to Edit",
 										JOptionPane.INFORMATION_MESSAGE);
 							} catch (Exception ee) {
 								JOptionPane
 								.showMessageDialog(
 										null,
 										"Other Exception in opening editor: "+ee.getMessage(),
 										"Unable to Edit",
 										JOptionPane.INFORMATION_MESSAGE);
 							}
 						}
 					}
 				}
 			}
 		});
 		jViewAnnotations.setText("View Annotations");
 		jViewAnnotations.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (selection.getSelectedNode() instanceof DataSetNode) {
 					DSDataSet<? extends DSBioObject> ds = selection.getDataSet();
 					if (!(ds instanceof CSMicroarraySet)) {
 						return;
 					}
 					CSMicroarraySet<? extends DSMicroarray> microarraySet = (CSMicroarraySet<? extends DSMicroarray>) ds;
 					String annotationFileName = microarraySet
 							.getAnnotationFileName();
 					if (annotationFileName == null) {
 						JOptionPane
 								.showMessageDialog(
 										null,
 										"There are no annotations loaded for this dataset.",
 										"Unable to View",
 										JOptionPane.INFORMATION_MESSAGE);
 						return;
 					}
 
 					GlobalPreferences prefs = GlobalPreferences.getInstance();
 					String editor = prefs.getTextEditor();
 					if (editor == null) {
 						log.info("No editor configured.");
 					} else {
 						if (Util.isRunningOnAMac()) {
 							editor = "Open";
 						}
 						String[] args = { editor, annotationFileName };
 						try {
 							Runtime.getRuntime().exec(args);
 						} catch (IOException e1) {
 							log.info("Error opening editor:");
 							e1.printStackTrace();
 						}
 					}
 				}
 			}
 		});
 
 		dataSetMenu.add(jSaveMenuItem);
 		dataSetMenu.addSeparator();
 		dataSetMenu.add(jRenameMenuItem);
 		dataSetMenu.add(jRemoveDatasetItem);
 		dataSetMenu.add(jEditItem);
 		dataSetMenu.add(jViewAnnotations);
 
 		dataSetSubMenu.add(jRenameSubItem);
 		dataSetSubMenu.add(jRemoveSubItem);
 
 		pendingMenu.add(jRemovePendingItem);
 
 		jRemoveProjectItem.setText("Remove Project");
 		jRemoveProjectItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 
 		jProjectMenu.addSeparator();
 		jProjectMenu.add(jRemoveProjectItem);
 	}
 
 	void populateFromSaveTree(SaveTree saveTree) {
 		java.util.List<DataSetSaveNode> projects = saveTree.getNodes();
 		ProjectTreeNode selectedNode = null;
 		Collection<GridEndpointReferenceType> pendingGridEprs = new HashSet<GridEndpointReferenceType>();
 		for (DataSetSaveNode project : projects) {
 			ProjectNode projectNode = new ProjectNode(project.getName());
 			projectNode.setDescription(project.getDescription());
 			addToProject(projectNode, true);
 			selection.setNodeSelection(projectNode);
 			/* add data sets next */
 			java.util.List<DataSetSaveNode> dataSets = project.getChildren();
 			for (DataSetSaveNode dataNode : dataSets) {
 				setComponents(dataNode);
 				DSDataSet<? extends DSBioObject> dataSet = dataNode.getDataSet();
 				dataSet.setExperimentInformation(dataNode.getDescription());
 				/* pending node */
 				if (dataSet.getLabel() != null
 						&& dataSet.getLabel().equals(
 								PendingTreeNode.class.getName())) {
 					// FIXME These are stored by class name in SaveTree. Not
 					// sure I like this.
 					GridEndpointReferenceType pendingGridEpr = (GridEndpointReferenceType) dataSet
 							.getObject(GridEndpointReferenceType.class
 									.getName());
 					addPendingNode(pendingGridEpr, (String) dataSet
 							.getObject(String.class.getName()), dataSet
 							.getDescriptions()[0], true);
 					pendingGridEprs.add(pendingGridEpr);
 				}
 				/* real node */
 				else {
 					addDataSetNode(dataSet, true);
 				}
 				if (dataSet == saveTree.getSelected()) {
 					selectedNode = selection.getSelectedNode();
 				}
 				/* add ancillary data sets */
 				java.util.List<DataSetSaveNode> ancSets = dataNode
 						.getChildren();
 				for (DataSetSaveNode ancNode : ancSets) {
 					setComponents(ancNode);
 
 					/* pending node */
 					if (ancNode.getDataSet().getDataSetName() != null
 							&& ancNode.getDataSet().getDataSetName().equals(
 									PendingTreeNode.class.getName())) {
 						// FIXME These are stored by class name in SaveTree. Not
 						// sure I like this.
 						GridEndpointReferenceType pendingGridEpr = (GridEndpointReferenceType) ancNode
 								.getDataSet().getObject(
 										GridEndpointReferenceType.class
 												.getName());
 						String history = (String) ancNode.getDataSet()
 								.getObject(String.class.getName());
 						addPendingNode(pendingGridEpr,
 								(String) ancNode.getDataSet().getObject(
 										String.class.getName()), history, true);
 						pendingGridEprs.add(pendingGridEpr);
 					} else {
 						DSAncillaryDataSet<? extends DSBioObject> ancSet = null;
 
 						if (ancNode.getDataSet() instanceof ImageData) {
 							ancSet = (ImageData) ancNode.getDataSet();
 						} else {
 							ancSet = (DSAncillaryDataSet<? extends DSBioObject>) ancNode.getDataSet();
 						}
 
 						ancSet.setExperimentInformation(ancNode
 								.getDescription());
 						addDataSetSubNode(ancSet);
 						if (ancSet == saveTree.getSelected()) {
 							selectedNode = selection.getSelectedNode();
 						}
 						selection.setNodeSelection((ProjectTreeNode) selection
 								.getSelectedDataSetSubNode().getParent());
 					}
 				}
 				selection.setNodeSelection((ProjectTreeNode) selection
 						.getSelectedDataSetNode().getParent());
 			}
 			publishPendingNodeLoadedFromWorkspaceEvent(new PendingNodeLoadedFromWorkspaceEvent(
 					pendingGridEprs));
 		}
 		// Set final selection
 		if (selectedNode != null) {
 			projectTree.scrollPathToVisible(new TreePath(selectedNode));
 			// serialize("default.ws");
 			projectTree.setSelectionPath(new TreePath(selectedNode.getPath()));
 			selection.setNodeSelection(selectedNode);
 		}
 	}
 
 	private void setComponents(DataSetSaveNode saveNode) {
 		Skin skin = (Skin) GeawConfigObject.getGuiWindow();
 		skin.setVisualLastSelected(saveNode.getDataSet(), saveNode
 				.getVisualSelected());
 		skin.setCommandLastSelected(saveNode.getDataSet(), saveNode
 				.getCommandSelected());
 		skin.setSelectionLastSelected(saveNode.getDataSet(), saveNode
 				.getSelectionSelected());
 	}
 
 	public static ImageIcon getIconForType(Class<? extends DSNamed> type) {
 		ImageIcon icon = iconMap.get(type);
 		if (icon == null) {
 			return Icons.GENERIC_ICON;
 		} else {
 			return icon;
 		}
 	}
 
 	public static void setIconForType(Class<? extends DSNamed> type,
 			ImageIcon icon) {
 		iconMap.put(type, icon);
 	}
 
 	private void saveNodeAsFile() {
 		ProjectTreeNode ds = selection.getSelectedNode();
 		if (ds != null) {
 			if (ds instanceof ImageNode) {
 				Image currentImage = ((ImageNode) ds).image.getImage();
 				SaveImage si = new SaveImage(currentImage);
 				JFileChooser fc = new JFileChooser(".");
 
 				FileFilter bitmapFilter = new ImageFileFilter.BitmapFileFilter();
 				FileFilter jpegFilter = new ImageFileFilter.JPEGFileFilter();
 				FileFilter pngFilter = new ImageFileFilter.PNGFileFilter();
 				FileFilter tiffFilter = new ImageFileFilter.TIFFFileFilter();
 				fc.setFileFilter(tiffFilter);
 				fc.setFileFilter(pngFilter);
 				fc.setFileFilter(jpegFilter);
 				fc.setFileFilter(bitmapFilter);
 
 				int choice = fc.showSaveDialog(jProjectPanel);
 				if (choice == JFileChooser.APPROVE_OPTION) {
 					String imageFilename = fc.getSelectedFile()
 							.getAbsolutePath();
 					String filename = fc.getSelectedFile().getName();
 					String ext = null;
 					int i = filename.lastIndexOf('.');
 					if (i > 0 && i < filename.length() - 1) {
 						ext = filename.substring(i + 1).toLowerCase();
 					} else {
 						FileFilter filter = fc.getFileFilter();
 						if (filter instanceof ImageFileFilter) {
 							ImageFileFilter selectedFilter = (ImageFileFilter) filter;
 							ext = selectedFilter.getExtension();
 							log.info("File extension: " + ext);
 						}
 					}
 					if (ext == null)
 						ext = "jpg";
 					if (imageFilename != null) {
 						si.save(imageFilename, ext);
 					}
 				}
 			} else if (ds instanceof DataSetSubNode) {
 				DataSetSubNode dataSetSubNode = (DataSetSubNode) ds;
 				if (dataSetSubNode._aDataSet instanceof CSTTestResultSet) {
 					CSTTestResultSet<? extends DSGeneMarker> tTestResultSet = (CSTTestResultSet<? extends DSGeneMarker>) dataSetSubNode._aDataSet;
 					tTestResultSet.saveDataToCSVFile();
 				} else
 					saveAsFile();
 			} else
 				saveAsFile();
 		}
 
 	}
 
 	private boolean saveAsFile() {
 		Object mSetSelected = projectTree.getSelectionPath()
 				.getLastPathComponent();
 		if (mSetSelected != null && mSetSelected instanceof DataSetNode) {
 			DSDataSet<? extends DSBioObject> ds = ((DataSetNode) mSetSelected).dataFile;
 			File f = ds.getFile();
 			JFileChooser jFileChooser1 = new JFileChooser(f);
 			jFileChooser1.setSelectedFile(f);
 			CustomFileFilter filter = SaveFileFilterFactory.createFilter(ds);
 			jFileChooser1.setFileFilter(filter);
 
 			// Use the SAVE version of the dialog, test return for
 			// Approve/Cancel
 			if (JFileChooser.APPROVE_OPTION == jFileChooser1
 					.showSaveDialog(jSaveMenuItem)) {
 				// Set the current file name to the user's selection,
 				// then do a regular saveFile
 				String newFileName = jFileChooser1.getSelectedFile().getPath();
 				newFileName = jFileChooser1.getSelectedFile().getAbsolutePath();
 
 				if (filter.accept(new File(newFileName))) {
 					// Use the current file name.
 				} else {
 					newFileName += "." + filter.getExtension();
 				}
 
 				// repaints menu after item is selected
 				log.info(newFileName);
 				// if(f != null) {
 				// return saveFile(f, newFileName);
 				// } else {
 				if (new File(newFileName).exists()) {
 					int o = JOptionPane.showConfirmDialog(null,
 
 					"Replace the file", "Replace the existing file?",
 							JOptionPane.YES_NO_CANCEL_OPTION);
 					if (o != JOptionPane.YES_OPTION) {
 						return false;
 					}
 				}
 
 				ds.writeToFile(newFileName);
 
 			} else {
 				// this.repaint();
 				return false;
 			}
 		} else if (mSetSelected != null
 				&& mSetSelected instanceof DataSetSubNode) {
 			DSDataSet<? extends DSBioObject> ds = ((DataSetSubNode) mSetSelected)._aDataSet;
 			File f = ds.getFile();
 			JFileChooser jFileChooser1 = new JFileChooser(f);
 			jFileChooser1.setSelectedFile(f);
 
 			// Use the SAVE version of the dialog, test return for
 			// Approve/Cancel
 			if (JFileChooser.APPROVE_OPTION == jFileChooser1
 					.showSaveDialog(jSaveMenuItem)) {
 				// Set the current file name to the user's selection,
 				// then do a regular saveFile
 				String newFileName = jFileChooser1.getSelectedFile().getPath();
 				// repaints menu after item is selected
 				log.info(newFileName);
 				// if(f != null) {
 				// return saveFile(f, newFileName);
 				// } else {
 				if (new File(newFileName).exists()) {
 					int o = JOptionPane.showConfirmDialog(null,
 
 					"Replace the file", "Replace the existing file?",
 							JOptionPane.YES_NO_CANCEL_OPTION);
 					if (o != JOptionPane.YES_OPTION) {
 						return false;
 					}
 				}
 				try {
 					ds.writeToFile(newFileName);
 				} catch (RuntimeException e) {
 					JOptionPane.showMessageDialog(null,
 							e.getMessage()+" "+ds.getClass().getName(), "Save Error",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			} else {
 				// this.repaint();
 				return false;
 			}
 
 		} else {
 			JOptionPane.showMessageDialog(null,
 					"This node contains no Dataset.", "Save Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 		return false;
 	}
 
 	/**
 	 * Change the comment text
 	 *
 	 * @param ce
 	 */
 	@Subscribe
 	public void receive(CommentsEvent ce, Object source) {
 		ProjectTreeNode selectedNode = selection.getSelectedNode();
 		selectedNode.setDescription(ce.getText());
 	}
 
 	/**
 	 * Inserts a new data set as a new node in the project tree. The node is a
 	 * child of the currently selected project
 	 *
 	 * @param _dataSet
 	 */
 	void addDataSetNode(DSDataSet<? extends DSBioObject> _dataSet, boolean select) {
 		// Retrieve the project node for this node
 		ProjectNode pNode = selection.getSelectedProjectNode();
 
 		if (pNode != null) {
 			// Inserts the new node and sets the menuNode and other variables to
 			// point to it
 			DataSetNode node = new DataSetNode(_dataSet);
 			node.setDescription(_dataSet.getExperimentInformation());
 			projectTreeModel.insertNodeInto(node, pNode, pNode.getChildCount());
 			HistoryEvent event = new HistoryEvent(_dataSet);
 
 			// add to history
 			if (_dataSet instanceof CSMicroarraySet) {
 				CSMicroarraySet<? extends DSMicroarray> microarraySet = (CSMicroarraySet<? extends DSMicroarray>) _dataSet;
 				String annotationFileName = microarraySet
 						.getAnnotationFileName();
 
 				String annotationFileNameString = "";
 				if (annotationFileName != null) {
 					int i = annotationFileName
 							.lastIndexOf(FilePathnameUtils.FILE_SEPARATOR);
 					if (i >= 0) {
 						annotationFileName = annotationFileName
 								.substring(i + 1);
 					}
 					annotationFileNameString = "Loaded annotation file:  " + annotationFileName + "\n";
 				} else{
 					annotationFileNameString = "Loaded annotation file:  None" + "\n";
 				}
 
 				String setName = _dataSet.getDataSetName();
 				String dataSetString = "Data file:  " + setName + "\n" ;
 
 				String datasetHistory = dataSetString + annotationFileNameString + "_____________________" + "\n";
 				ProjectPanel.addToHistory(_dataSet, datasetHistory);
 			}
 			publishHistoryEvent(event);
 
 			if (select) {
 				// Make sure the user can see the lovely new node.
 				projectTree.scrollPathToVisible(new TreePath(node));
 				// serialize("default.ws");
 				projectTree.setSelectionPath(new TreePath(node.getPath()));
 				selection.setNodeSelection(node);
 			}
 		}
 		Skin skin = (Skin) GeawConfigObject.getGuiWindow();
 		skin.resetSelectorTabOrder();
 	}
 
 	/**
 	 * This method is used to trigger HistoryPanel to refresh.
 	 *
 	 * @param event
 	 * @return
 	 */
 	@Publish
 	public HistoryEvent publishHistoryEvent(HistoryEvent event) {
 		return event;
 	}
 
 	/**
 	 * Inserts a new pending node a new node in the project tree. The node is a
 	 * child of the curently selected project
 	 *
 	 * @param _dataSet
 	 */
 	public PendingTreeNode addPendingNode(GridEndpointReferenceType gridEpr,
 			String description, String history, boolean startNewThread) {
 		// Retrieve the project node for this node
 		ProjectTreeNode pNode = selection.getSelectedNode();
 		PendingTreeNode node = null;
 		if (pNode == null) {
 		}
 		if (pNode != null) {
 			/*
 			 * Inserts the new node and sets the menuNode and other variables to
 			 * point to it.
 			 */
 			node = new PendingTreeNode(description, history, gridEpr);
 			projectTreeModel.insertNodeInto(node, pNode, pNode.getChildCount());
 			// Make sure the user can see the lovely new node.
 			projectTree.scrollPathToVisible(new TreePath(node));
 			projectTree.setSelectionPath(new TreePath(node.getPath()));
 			selection.setNodeSelection(node);
 			eprPendingNodeMap.put(gridEpr, node);
 		}
 		return node;
 	}
 
 	private void addCompletedNode(GridEndpointReferenceType gridEpr,
 			DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet) {
 		PendingTreeNode node = eprPendingNodeMap.get(gridEpr);
 		String history = node.getDescription();
 		boolean pendingNodeFocused = false;
 		if (node != null) {
 			if (ancillaryDataSet != null) {
 				TreePath pathNow = projectTree.getSelectionPath();
 				Object lastSelected = projectTree
 						.getLastSelectedPathComponent();
 				if (lastSelected.getClass().getName() == PendingTreeNode.class
 						.getName()) {
 					if (((PendingTreeNode) lastSelected).getGridEpr() == gridEpr)
 						pendingNodeFocused = true;
 				}
 				ProjectTreeNode parent = (ProjectTreeNode) node.getParent();
 				int index = parent.getIndex(node);
 				projectTreeModel.removeNodeFromParent(node);
 				// FIXME: we should check if parent is a DataSetNode or not,
 				// before casting. If not we should find a way to deal with it.
 				((CSAncillaryDataSet<? extends DSBioObject>) ancillaryDataSet)
 						.setParent(((DataSetNode) parent).dataFile);
 				DataSetSubNode newNode = new DataSetSubNode(ancillaryDataSet);
 				projectTreeModel.insertNodeInto(newNode, parent, index);
 				eprPendingNodeMap.remove(gridEpr);
 				// TODO: now we need to put history on new node
 				ProjectPanel.addToHistory(ancillaryDataSet, history);
 
 				// Make sure the user can see the lovely new node.
 				projectTree.scrollPathToVisible(new TreePath(newNode));
 				projectTree.setSelectionPath(new TreePath(newNode.getPath()));
 				projectTree.setSelectionPath(pathNow);
 				// If the pending node is focused,
 				// we assume the user is interested in this result.
 				// we visually set the focus to the new node,
 				// and select the node so user can see the result)
 				if (pendingNodeFocused) {
 					projectTree
 							.setSelectionPath(new TreePath(newNode.getPath()));
 					selection.setNodeSelection(newNode);
 				}
 				// PS: this post processing event has to follow the node
 				// selection. otherwise it might affect wrong node.
 				// ex: significance result set will add a significant markers in
 				// the panel for wrong node.
 				publishPostProcessingEvent(new ProjectNodePostCompletedEvent(
 						ancillaryDataSet.getDataSetName(), gridEpr,
 						ancillaryDataSet, parent));
 
 			} else {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"The service didn't return any results. Please check your input parameters and try again");
 				node.setUserObject("No Results");
 			}
 		}
 	}
 
 	private void removeCanceledNode(GridEndpointReferenceType gridEpr) {
 		PendingTreeNode node = eprPendingNodeMap.get(gridEpr);
 		if (node != null) {
 			ProjectTreeNode parent = (ProjectTreeNode) node.getParent();
 			projectTreeModel.removeNodeFromParent(node);
 			// node.setUserObject("No Results");
 			// now nothing is selected, which is annoying, let's select it's
 			// parent
 			projectTree.setSelectionPath(new TreePath(parent.getPath()));
 			selection.setNodeSelection(parent);
 		}
 	}
 
 	/**
 	 *
 	 * @param pnode
 	 * @param parentData
 	 * @return
 	 */
 	private DataSetNode getMatchNode(ProjectTreeNode pnode, DSDataSet<? extends DSBioObject> parentData) {
 
 		DSDataSet<? extends DSBioObject> dNodeFile = null;
 		if ((pnode instanceof DataSetNode)) {
 			dNodeFile = ((DataSetNode) pnode).dataFile;
 
 		}
 		if ((dNodeFile != null && dNodeFile.hashCode() == parentData.hashCode())) {
 			return (DataSetNode) pnode;
 		} else if (pnode != null) {
 			Enumeration<?> children = pnode.children();
 			while (children.hasMoreElements()) {
 				Object obj = children.nextElement();
 				if (getMatchNode((ProjectTreeNode) obj, parentData) != null) {
 
 					return getMatchNode((ProjectTreeNode) obj, parentData);
 				}
 			}
 
 		}
 
 		return null;
 	}
 
 	/**
 	 * Inserts a new ancillary data set as a new node in the project tree. The
 	 * node is a child of the currently selected data set
 	 *
 	 * @param _ancDataSet
 	 */
 	void addDataSetSubNode(DSAncillaryDataSet<? extends DSBioObject> _ancDataSet) {
 		DataSetNode dNode = selection.getSelectedDataSetNode();
 		DataSetNode matchedDNode = null;
 		DSDataSet<? extends DSBioObject> parentSet = _ancDataSet.getParentDataSet();
 		if (parentSet != null) {
 			if (dNode != null) {
 
 				DSDataSet<? extends DSBioObject> dNodeFile = dNode.dataFile;
 				if (dNodeFile.hashCode() == parentSet.hashCode()) {
 					_ancDataSet.setDataSetFile(dNode.dataFile.getFile());
 
 				} else {
 					// get the matched node in case the node selected changed.
 					matchedDNode = getMatchNode(root, parentSet);
 				}
 
 			} else {
 				matchedDNode = getMatchNode(root, parentSet);
 			}
 		}
 		if (matchedDNode != null) {
 			dNode = matchedDNode;
 		}
 		if (dNode == null) {
 			log.info("There is no node at project panel!");
 			return;
 		}
 
 		_ancDataSet.setDataSetFile(dNode.dataFile.getFile());
 		// Makes sure that we do not already have an exact instance of this
 		// ancillary file
 		Enumeration<?> children = dNode.children();
 		while (children.hasMoreElements()) {
 			Object obj = children.nextElement();
 			if (obj instanceof DataSetSubNode) {
 				DSAncillaryDataSet<? extends DSBioObject> ads = ((DataSetSubNode) obj)._aDataSet;
 				if (_ancDataSet.equals(ads)) {
 					return;
 				}
 			}
 		}
 
 		DataSetSubNode node = null;
 		if (_ancDataSet instanceof ImageData) {
 			node = new ImageNode(((ImageData) _ancDataSet).getImageIcon());
 		} else {
 			node = new DataSetSubNode(_ancDataSet);
 		}
 
 		String originalLabel = _ancDataSet.getLabel();
 		String newLabel = originalLabel;
 		String existingLabel = "";
 		boolean foundOne = false;
 		int count = 1;
 		while (true) {
 			foundOne = false;
 			children = dNode.children();
 			while (children.hasMoreElements()) {
 				Object obj = children.nextElement();
 				if (obj instanceof DataSetSubNode) {
 					existingLabel = ((DataSetSubNode) obj)._aDataSet.getLabel();
 					if (newLabel.equals(existingLabel)) {
 						foundOne = true;
 						break;
 					}
 				}
 			}
 
 			if (foundOne) {
 				newLabel = originalLabel + " (" + count++ + ")";
 			} else {
 				_ancDataSet.setLabel(newLabel);
 				break;
 			}
 		}
 
 		// Inserts the new node and sets the menuNode and other variables to
 		// point to it
 		node.setDescription(_ancDataSet.getExperimentInformation());
 		projectTreeModel.insertNodeInto(node, dNode, dNode.getChildCount());
 		// Make sure the user can see the lovely new node.
 		projectTree.scrollPathToVisible(new TreePath(node));
 		// serialize("default.ws");
 		projectTree.setSelectionPath(new TreePath(node.getPath()));
 		selection.setNodeSelection(node);
 	}
 
 	/**
 	 * Reads from a datafile
 	 *
 	 * @param filename
 	 */
 	void deserialize(String filename) {
 		try {
 			FileInputStream in = new FileInputStream(filename);
 			ObjectInputStream s = new ObjectInputStream(in);
 			SaveTree saveTree = (SaveTree) s.readObject();
 			populateFromSaveTree(saveTree);
 			APSerializable aps = (APSerializable) s.readObject();
 			AnnotationParser.setFromSerializable(aps);
 			// root = (ProjectTreeNode) s.readObject();
 			// selection.clearNodeSelections();
 			// projectTreeModel = new DefaultTreeModel(root);
 			// projectTree.setModel(projectTreeModel);
 		} catch (ClassNotFoundException ex) {
 			log.error("Error: " + ex);
 		} catch (IOException ex) {
 			log.error("Error: " + ex);
 		}
 	}
 
 	/**
 	 * Action listener responding to the selection of a project tree node.
 	 *
 	 * @param e
 	 */
 	private void jProjectTree_mouseClicked(MouseEvent e) {
 
 		TreePath path = projectTree.getSelectionPath();
 		if (path != null) {
 			path.getLastPathComponent();
 			selectedNode = selection.getSelectedNode();
 			ProjectTreeNode clickedNode = (ProjectTreeNode) path
 					.getLastPathComponent();
 			// Take action only if a new node is selected.
 			if (path != null && selectedNode != clickedNode) {
 				setNodeSelection(clickedNode);
 			}
 			if ((clickedNode != null) && clickedNode instanceof DataSetSubNode) {
 				// DSAncillaryDataSet ds = ((DataSetSubNode)
 				// clickedNode)._aDataSet;
 				// publishProjectEvent(new ProjectEvent("ProjectNode", ds));
 			}
 			if ((clickedNode != null) && clickedNode instanceof ImageNode) {
 				if (e.getClickCount() == 1) {
 					publishImageSnapshot(new ImageSnapshotEvent(
 							"Image Node Selected",
 							((ImageNode) clickedNode).image,
 							ImageSnapshotEvent.Action.SHOW));
 					sendCommentsEvent(clickedNode);
 				}
 			}
 			sendCommentsEvent(clickedNode);
 		}
 	}
 
 	private boolean isPathSelected(TreePath path) {
 		TreePath[] selectedPaths = projectTree.getSelectionPaths();
 		if (selectedPaths == null) {
 			return false;
 		}
 		for (int i = 0; i < selectedPaths.length; i++) {
 			TreePath selectedPath = selectedPaths[i];
 			if (path == selectedPath) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Mouse release event. Used to popup menus
 	 *
 	 * @param e
 	 */
 	private void jProjectTree_mouseReleased(MouseEvent e) {
 		TreePath path = projectTree.getPathForLocation(e.getX(), e.getY());
 		TreePath[] paths = projectTree.getSelectionPaths();
 		if (path != null) {
 			ProjectTreeNode mNode = (ProjectTreeNode) path
 					.getLastPathComponent();
 			selection.setMenuNode(mNode);
 
 			if (e.isMetaDown() || e.getClickCount() >= 2) {
 
 				if (!isPathSelected(path)) {
 					// Force selection of this path
 					projectTree.setSelectionPath(path);
 					jProjectTree_mouseClicked(e);
 
 				}
 				// Make the jPopupMenu visible relative to the current mouse
 				// position in the container.
 				if (paths != null && paths.length > 1) {
 					this.jNewProjectItem.setEnabled(false);
 					this.jSaveMenuItem.setEnabled(false);
 					this.jOpenRemotePDBItem.setEnabled(false);
 					this.jLoadMArrayItem.setEnabled(false);
 					this.jRenameDataset.setEnabled(false);
 					this.jRenameMenuItem.setEnabled(false);
 					this.jRenameProjectItem.setEnabled(false);
 					this.jRenameSubItem.setEnabled(false);
 					this.jSaveMenuItem.setEnabled(false);
 					this.jEditItem.setEnabled(false);
 					this.jViewAnnotations.setEnabled(false);
 				} else {
 					this.jNewProjectItem.setEnabled(true);
 					this.jSaveMenuItem.setEnabled(true);
 					this.jOpenRemotePDBItem.setEnabled(true);
 					this.jLoadMArrayItem.setEnabled(true);
 					this.jRenameDataset.setEnabled(true);
 					this.jRenameMenuItem.setEnabled(true);
 					this.jRenameProjectItem.setEnabled(true);
 					this.jRenameSubItem.setEnabled(true);
 					this.jSaveMenuItem.setEnabled(true);
 					this.jEditItem.setEnabled(true);
 					this.jViewAnnotations.setEnabled(true);
 				}
 
 				if ((mNode == null) || (mNode == root)) {
 					jRootMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof ProjectNode) {
 					jProjectMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof DataSetNode) {
 					dataSetMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof DataSetSubNode) {
 					dataSetMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof PendingTreeNode) {
 					pendingMenu.show(projectTree, e.getX(), e.getY());
 				}
 			} else
 				jProjectTree_mouseClicked(e);
 
 			Skin skin = (Skin) GeawConfigObject.getGuiWindow();
 			skin.resetSelectorTabOrder();
 		}
 	}
 
 	/**
 	 * key release event.
 	 *
 	 * @param e
 	 */
 	private void jProjectTree_keyReleased(KeyEvent e) {
 		TreePath path = projectTree.getSelectionPath();
 		if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)
 				&& path != null) {
 			ProjectTreeNode mNode = (ProjectTreeNode) path
 					.getLastPathComponent();
 			selection.setMenuNode(mNode);
 
 			jProjectTree_keyReleased();
 
 		}
 	}
 
 	/**
 	 * Key listener responding to the selection of a project tree node.
 	 *
 	 * @param e
 	 */
 	private void jProjectTree_keyReleased() {
 
 		TreePath path = projectTree.getSelectionPath();
 
 		if (path != null) {
 			path.getLastPathComponent();
 			selectedNode = selection.getSelectedNode();
 			ProjectTreeNode clickedNode = (ProjectTreeNode) path
 					.getLastPathComponent();
 			// Take action only if a new node is selected.
 			if (path != null && selectedNode != clickedNode) {
 				setNodeSelection(clickedNode);
 			}
 
 			if ((clickedNode != null) && clickedNode instanceof ImageNode) {
 
 				publishImageSnapshot(new ImageSnapshotEvent(
 						"Image Node Selected", ((ImageNode) clickedNode).image,
 						ImageSnapshotEvent.Action.SHOW));
 				sendCommentsEvent(clickedNode);
 
 			}
 			sendCommentsEvent(clickedNode);
 		}
 	}
 
 	@Subscribe
 	public void receive(org.geworkbench.events.ProjectNodeAddedEvent pnae,
 			Object source) {
 		DSDataSet<? extends DSBioObject> dataSet = pnae.getDataSet();
 		if (dataSet instanceof DSMicroarraySet) {
 			addColorContext((DSMicroarraySet<? extends DSMicroarray>) dataSet);
 		}
 		DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet = pnae.getAncillaryDataSet();
 		if (dataSet != null) {
 			addDataSetNode(dataSet, true);
 		} else if (ancillaryDataSet != null) {
 			addDataSetSubNode(ancillaryDataSet);
 		}
 	}
 
 	public void processNodeCompleted(GridEndpointReferenceType gridEPR,
 			DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet) {
 		if (ancillaryDataSet == null) {
 			// no result from grid server? let's delete this node!
 			removeCanceledNode(gridEPR);
 		} else {
 			addCompletedNode(gridEPR, ancillaryDataSet);
 		}
 	}
 
 	/**
 	 * Action listener handling user requests for opening a file containing
 	 * microarray set data.
 	 *
 	 * @param e
 	 */
 	private void jLoadMArrayItem_actionPerformed(ActionEvent e) {
 		// Proceed only if there is a single node selected and that node
 		// is a project node.
 		if (projectTree.getSelectionCount() != 1
 				|| !(projectTree.getSelectionPath().getLastPathComponent() instanceof ProjectNode)) {
 			JOptionPane.showMessageDialog(null, "Select a project node.",
 					"Open File Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		String dir = LoadData.getLastDataDirectory();
 		String format = LoadData.getLastDataFormat();
 		loadData.setDirectory(dir);
 		loadData.setFormat(format);
 		// setupInputFormats() is called at every invocation of the "Open File"
 		// dialog. This guarantees that any dynamically loaded file format
 		// plugins
 		// will be taken into account.
 		loadData.setupInputFormats();
 		loadData.validate();
 		loadData.checkCaArraySupportingClasses();
 		loadData.setVisible(true);
 	}
 
 	/**
 	 * Action listener handling user requests for opening a pdb file from RCSB
 	 * Protein Data Bank.
 	 *
 	 * @param e
 	 */
 	private void jOpenRemotePDBItem_actionPerformed(ActionEvent e) {
 		// Proceed only if there is a single node selected and that node
 		// is a project node.
 		if (projectTree.getSelectionCount() != 1
 				|| !(projectTree.getSelectionPath().getLastPathComponent() instanceof ProjectNode)) {
 			JOptionPane.showMessageDialog(null, "Select a project node.",
 					"Open File Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		PDBDialog pd = new PDBDialog(this);
 		pd.create();
 	}
 
 	/**
 	 * Action listener handling user requests to merge 2 or more microarray sets
 	 * into 1.
 	 *
 	 * @param e
 	 *            <code>ActionEvent</code>
 	 */
 	private void jMergeDatasets_actionPerformed(ActionEvent e) {
 		TreePath[] selections;
 
 		MutableTreeNode node = null;
 		Object parentProject = null;
 		TreePath sibling = null;
 		int count = projectTree.getSelectionCount();
 		int i;
 		// Obtain the selected project tree nodes.
 		selections = projectTree.getSelectionPaths();
 		DSMicroarraySet<? extends DSMicroarray>[] sets = new DSMicroarraySet[count];
 		// Check that the user has designated only microarray set nodes and that
 		// all microarray sets are from the same project.
 		// Also, identify the node that will become the parent of the new,
 		// merged
 		// microarray set node.
 		for (i = 0; i < count; i++) {
 
 			node = (MutableTreeNode) selections[i].getLastPathComponent();
 			if (node instanceof DataSetNode) {
 				try {// Provide fix for bug 666, only merge
 					// Microarraydatasets.
 					sets[i] = (DSMicroarraySet<? extends DSMicroarray>) ((DataSetNode) node).dataFile;
 					if (sibling == null
 							|| sibling.getPathCount() > selections[i]
 									.getPathCount()) {
 						sibling = selections[i];
 					}
 					if (parentProject == null) {
 						parentProject = selections[i].getPath()[1];
 					} else if (parentProject != selections[i].getPath()[1]) {
 						JOptionPane.showMessageDialog(null,
 								"Select nodes from 1 project only.",
 								"Merge Error", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 				} catch (ClassCastException ex) {
 					JOptionPane.showMessageDialog(null,
 							"Only microarray sets of the same"
 									+ " underlying platform can be merged.",
 							"Merge Error", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 			} else {
 				JOptionPane.showMessageDialog(null,
 						"Select microarray set nodes only.", "Merge Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		}
 		// Verify that at least 2 microarray sets have been selected for merging
 		if (i < 2) {
 			JOptionPane.showMessageDialog(null, "Select 2 or more data nodes.",
 					"Merge Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		// Verify that all microarrays are of the same base type.
 		for (i = 0; i < count; ++i) {
 			if (!sets[0].getClass().isAssignableFrom(sets[i].getClass())) {
 				JOptionPane.showMessageDialog(null,
 						"Only microarray sets of the same"
 								+ " underlying platform can be merged.",
 						"Merge Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		}
 		doMergeSets(sets);
 	}
 
 	/**
 	 * Check for markers in DSMicroarraySets, if markers are all the same,
 	 * return true. This method assume there's no duplicate markers within each
 	 * set.
 	 *
 	 * @param sets
 	 * @return
 	 */
 	private static boolean isSameMarkerSets(DSMicroarraySet<? extends DSMicroarray>[] sets) {
 		if (sets == null || sets.length <= 1)
 			return true;
 
 		HashSet<DSGeneMarker> set1 = new HashSet<DSGeneMarker>();
 		set1.addAll(sets[0].getMarkers());
 
 		HashSet<DSGeneMarker> set2 = new HashSet<DSGeneMarker>();
 		for (int i = 1; i < sets.length; i++) {
 			set2.clear();
 			set2.addAll(sets[i].getMarkers());
 			if (!set1.equals(set2))
 				return false;
 		}
 		return true; // all marker sets are identical
 	}
 
 	/**
 	 * Merger an array of MSMicroarraySets and create a new dataset node.
 	 *
 	 * @param sets
 	 */
 	public void doMergeSets(DSMicroarraySet<? extends DSMicroarray>[] sets) {
 		if (!isSameMarkerSets(sets)) {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"Can't merge datasets.  Only datasets with the same markers can be merged.",
 							"Operation failed while merging",
 							JOptionPane.INFORMATION_MESSAGE);
 			return;
 		}
 		DSMicroarraySet<DSMicroarray> mergedSet = null;
 		int i;
 		DSMicroarraySet<DSMicroarray> set;
 		if (sets != null) {
 			String desc = "Merged DataSet: ";
 			for (i = 0; i < sets.length; i++) {
 				set = (DSMicroarraySet<DSMicroarray>)sets[i];
 				if (mergedSet == null) {
 					try {
 						mergedSet = set.getClass().newInstance();
 						mergedSet.addObject(ColorContext.class, set
 								.getObject(ColorContext.class));
 						// mergedSet.setMarkerNo(set.size());
 						// mergedSet.setMicroarrayNo(set.size());
 
 						((DSMicroarraySet<DSMicroarray>) mergedSet)
 								.setCompatibilityLabel(set
 										.getCompatibilityLabel());
 						((DSMicroarraySet<DSMicroarray>) mergedSet)
 								.getMarkers().addAll(set.getMarkers());
 						DSItemList<DSGeneMarker> markerList = set.getMarkers();
 						for (int j = 0; j < markerList.size(); j++) {
 							DSGeneMarker dsGeneMarker = markerList.get(j);
 							((DSMicroarraySet<DSMicroarray>) mergedSet)
 									.getMarkers().add(dsGeneMarker.deepCopy());
 						}
 						for (int k = 0; k < set.size(); k++) {
 							mergedSet.add(set.get(k).deepCopy());
 						}
 						desc += set.getLabel() + " ";
 						// XQ fix bug 1539, add annotation information to the
 						// merged dataset.
 						String chipType = AnnotationParser.getChipType(set);
 						AnnotationParser.setChipType(mergedSet, chipType);
 					} catch (InstantiationException ie) {
 						ie.printStackTrace();
 					} catch (IllegalAccessException iae) {
 						iae.printStackTrace();
 					}
 				} else {
 					desc += set.getLabel() + " ";
 					try {
 						mergedSet.mergeMicroarraySet(set);
 					} catch (Exception ex) {
 						ex.printStackTrace();
 						JOptionPane
 								.showMessageDialog(
 										null,
 										"Only microarray sets created"
 												+ " from the same chip set can be merged",
 										"Merge Error",
 										JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 				}
 			}
 
 			if (mergedSet != null) {
 				mergedSet.setLabel("Merged array set");
 				mergedSet.setLabel(desc);
 				mergedSet.addDescription(desc);
 				((CSMicroarraySet<? extends DSMicroarray>)mergedSet).setAnnotationFileName(
 						((CSMicroarraySet<? extends DSMicroarray>)sets[0]).getAnnotationFileName());
 			}
 			// Add color context
 			addColorContext(mergedSet);
 
 			// Add the new dataset to the project tree.
 			addDataSetNode((DSDataSet<? extends DSBioObject>) mergedSet, true);
 		}
 	}
 
 	/**
 	 * Invoked from the "Open File" dialog box to handle opening a local
 	 * dataset.
 	 *
 	 * @param dataSetFiles
 	 *            The file containing the data to be parsed.
 	 * @param inputFormat
 	 *            The format that the file is expected to conform to.
 	 * @throws org.geworkbench.parsers.InputFileFormatException
 	 *
 	 */
 	void fileOpenAction(final File[] dataSetFiles,
 			final org.geworkbench.parsers.FileFormat inputFormat,
 			boolean merge)
 			throws org.geworkbench.parsers.InputFileFormatException,
 			InterruptedIOException {
 
 		final boolean mergeFiles = dataSetFiles.length == 1 ? false : merge;
 		if (inputFormat instanceof DataSetFileFormat) {
 			FileOpenHandler handler = new FileOpenHandler(dataSetFiles,
 					inputFormat, mergeFiles, this);
 			handler.openFiles();
 		} else {
 			log
 					.error("unreachable branch: all FileFormat's are DataSetFileFormat");
 		}
 	}
 
 	void addColorContext(DSMicroarraySet<? extends DSMicroarray> maSet) {
 		GlobalPreferences prefs = GlobalPreferences.getInstance();
 		Class<? extends ColorContext> type = prefs.getColorContextClass();
 		try {
 			ColorContext context = type.newInstance();
 			maSet.addObject(ColorContext.class, context);
 			updateColorContext(maSet);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Action listener handling user requests for renaming a project.
 	 *
 	 * @param e
 	 */
 	private void jRenameProjectItem_actionPerformed(ActionEvent e) {
 		if (projectTree == null || selection == null
 				|| (selection.areNodeSelectionsCleared())
 				|| selection.getSelectedProjectNode() == null) {
 			JOptionPane.showMessageDialog(null, "Select a project node.",
 					"Rename Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		ProjectNode project = selection.getSelectedProjectNode();
 		String inputValue = JOptionPane.showInputDialog("Project Name:",
 				project.toString());
 		if (inputValue != null) {
 			project.setUserObject(inputValue);
 			projectTreeModel.nodeChanged(project);
 		}
 	}
 
 	public void ccmUpdate() {
 		GeawConfigObject.getGuiWindow().setVisualizationType(null);
 		if (root != null) {
 			// FIXME This almost works, but you need to click on the DataNode
 			// before you click on visual node, to make it work.
 			// projectTreeModel.reload((selection.getSelectedNode()).getParent());
 			projectTreeModel.reload(root);
 		}
 
 		if (projectTree == null) {
 			return;
 		}
 
 		DataSetNode selectedDataSetNode = selection.getSelectedDataSetNode();
 		String message = "CCM update";
 		DSMicroarraySet<? extends DSMicroarray> maSet = null;
 		if (selectedDataSetNode != null) {
 			if (selectedDataSetNode.dataFile instanceof DSMicroarraySet) {
 				maSet = (DSMicroarraySet<? extends DSMicroarray>) selectedDataSetNode.dataFile;
 				publishProjectEvent(new ProjectEvent(message, maSet,
 						selectedDataSetNode));
 			} else {
 				publishProjectEvent(new ProjectEvent(message,
 						selectedDataSetNode.dataFile, selectedDataSetNode));
 			}
 			sendCommentsEvent(selectedDataSetNode);
 		}
 
 		selection.setNodeSelection(null);
 	}
 
 	/*
 	 * This method will remove an added subnode. If the current selected node is
 	 * not the added subNode, then do nothing.
 	 *
 	 */
 	public void removeAddedSubNode(DSAncillaryDataSet aDataSet) {
 
 		if (selection.getSelectedNode() instanceof DataSetSubNode) {
 			if (((DataSetSubNode) (selection.getSelectedNode()))._aDataSet != aDataSet) {
 				log.warn("the added node is not the selected node.");
 				return;
 			}
 
 			publishNodeRemovedEvent(new ProjectNodeRemovedEvent("remove", null,
 					((DataSetSubNode) (selection.getSelectedNode()))._aDataSet));
 
 			ProjectTreeNode node = selection.getSelectedNode();
 
 			ProjectTreeNode parentNode = (ProjectTreeNode) node.getParent();
 
 			projectTreeModel.removeNodeFromParent(node);
 
 			setNodeSelection(parentNode);
 		}
 	}
 
 	protected void remove_actionPerformed() {
 		if (projectTree == null || selection == null ) {
 			JOptionPane.showMessageDialog(null, "Please make a selection.",
 					"Delete Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		else if (selection.getSelectedNode().isRoot())
 		{
 			JOptionPane.showMessageDialog(null, "Please don't select a ROOT node.",
 					"Delete Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		TreePath[] paths = projectTree.getSelectionPaths();
 
 		if (paths.length <= 0)
 			return;
 
 		ProjectTreeNode node = new ProjectTreeNode();
 		ProjectTreeNode parentNode = null;
 
 		for (TreePath path : paths) {
 			node = (ProjectTreeNode) path.getLastPathComponent();
 
 			if (paths == null || node.isRoot())
 				continue;
 
 			boolean isExist = false;
 			for (int i = 0; i < paths.length; i++) {
 				if (paths[i].equals(path)) {
 					isExist = true;
 					break;
 				}
 
 			}
 			if (!isExist)
 				continue;
 
 			parentNode = (ProjectTreeNode) node.getParent();
 
 			if (node instanceof ProjectNode)
 				projectRemove_actionPerformed((ProjectNode) node);
 			else if (node instanceof ImageNode)
 				imageRemove_actionPerformed(node);
 			else
 				fileRemove_actionPerformed(node);
 			paths = projectTree.getSelectionPaths();
 		}
 		// If there are any remaining projects, select the first of them to
 		// be the next one to get the focus.
 
 		if (parentNode == null)
 			return;
 		else if (parentNode.isRoot() && parentNode.getChildCount() <= 0) {
 			clear();
 			setNodeSelection(parentNode);
 		} else if (parentNode.isRoot() && parentNode.getChildCount() > 0) {
 			ProjectTreeNode pNode = (ProjectTreeNode) parentNode.getChildAt(0);
 			if (pNode.getChildCount() > 0) {
 				setNodeSelection((DataSetNode) pNode.getChildAt(0));
 			}
 		} else if (parentNode.getChildCount() == 0
 				&& parentNode instanceof ProjectNode) {
 			setNodeSelection(parentNode);
 			publishProjectEvent(new ProjectEvent(ProjectEvent.CLEARED, null,
 					parentNode));
 			return;
 		} else if (parentNode.getChildCount() > 0
 				&& parentNode instanceof ProjectNode) {
 			setNodeSelection((DataSetNode) parentNode.getChildAt(0));
 		} else if (parentNode.getChildCount() > 0
 				&& parentNode instanceof DataSetNode) {
 			setNodeSelection((DataSetSubNode) parentNode.getChildAt(0));
 		} else {
 			setNodeSelection(parentNode);
 		}
 
 	}
 
 	/**
 	 * Action listener handling user requests for removing a dataset.
 	 *
 	 * @param e
 	 */
 	protected void fileRemove_actionPerformed(ProjectTreeNode node) {
 		// clear out unused mark annotation from memory
 		if (node instanceof DataSetNode) {
 			AnnotationParser
 					.cleanUpAnnotatioAfterUnload(((DataSetNode) node).dataFile);
 
 			if (node.getChildCount() > 0) {
 				for (Enumeration en = node.children(); en.hasMoreElements();) {
 					ProjectTreeNode childNode = (ProjectTreeNode) en
 							.nextElement();
 					if (childNode instanceof DataSetSubNode)
 						publishNodeRemovedEvent(new ProjectNodeRemovedEvent(
 								"remove", null,
 								((DataSetSubNode) (childNode))._aDataSet));
 					if (childNode instanceof PendingTreeNode) {
 
 						publishPendingNodeCancelledEvent(new PendingNodeCancelledEvent(
 								((PendingTreeNode) childNode).getGridEpr()));
 					}
 
 				}
 			}
 		}
 
 		if (node instanceof PendingTreeNode) { // if
 			// it's
 			// a
 			// pending
 			// node,
 			// we
 			// fire
 			// a
 			// PendingNodeCancelledEvent.
 			publishPendingNodeCancelledEvent(new PendingNodeCancelledEvent(
 					((PendingTreeNode) node).getGridEpr()));
 		}
 
 		if (node instanceof DataSetSubNode)
 			publishNodeRemovedEvent(new ProjectNodeRemovedEvent("remove", null,
 					((DataSetSubNode) (node))._aDataSet));
 
 		projectTreeModel.removeNodeFromParent(node);
 
 	}
 
 	@Publish
 	public PendingNodeCancelledEvent publishPendingNodeCancelledEvent(
 			PendingNodeCancelledEvent event) {
 		return event;
 	}
 
 	/**
 	 * Action listener handling user requests for removing a project.
 	 *
 	 * @param e
 	 */
 	protected void projectRemove_actionPerformed(ProjectNode node) {

 		if (node.getChildCount() > 0) {
 			for (Enumeration en = node.children(); en.hasMoreElements();) {
 				ProjectTreeNode childNode = (ProjectTreeNode) en
 						.nextElement();
 				if (childNode instanceof DataSetNode)
 					fileRemove_actionPerformed(childNode);
 			}
 		}
 		projectTreeModel.removeNodeFromParent(node);

 	}
 
 	/**
 	 * Action listener handling user requests for renaming a dataset.
 	 *
 	 * @param e
 	 */
 	protected void jRenameDataset_actionPerformed(ActionEvent e) {
 		if (projectTree == null || selection == null
 				|| (selection.areNodeSelectionsCleared())) {
 			JOptionPane.showMessageDialog(null,
 					"Select a dataset or ancillary dataset.", "Rename Error",
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		DSDataSet ds = null;
 		ProjectTreeNode dsNode = null;
 
 		if (selection.getSelectedNode() instanceof DataSetNode) {
 			ds = selection.getDataSet();
 			dsNode = selection.getSelectedDataSetNode();
 		} else if (selection.getSelectedNode() instanceof DataSetSubNode) {
 			dsNode = selection.getSelectedDataSetSubNode();
 			ds = selection.getDataSubSet();
 		}
 		String oldName = ds.getLabel();
 		if (ds != null && dsNode != null) {
 			String inputValue = JOptionPane.showInputDialog("Dataset Name:",
 					dsNode.toString());
 			if (inputValue != null) {
 				dsNode.setUserObject(inputValue);
 				ds.setLabel(inputValue);
 				projectTreeModel.nodeChanged(dsNode);
 				publishNodeRenamedEvent(new ProjectNodeRenamedEvent("rename",
 						selection.getDataSubSet(), oldName, inputValue));
 			}
 		}
 	}
 
 	/**
 	 * Action listener handling user requests for the creation of new projects
 	 * in the workspace.
 	 *
 	 * @param e
 	 */
 	protected void jNewProjectItem_actionPerformed(ActionEvent e) {
 		ProjectNode childNode = new ProjectNode("Project");
 		addToProject(childNode, true);
 
 		// removeDeletedAcceptorComponents();
 	}
 
 	/**
 	 * Used to add a new node to a project tree
 	 *
 	 * @param child
 	 *            The node to be added
 	 * @param shouldBeVisible
 	 *            wether it should be visible or not
 	 * @return
 	 */
 	public ProjectNode addToProject(ProjectNode child, boolean shouldBeVisible) {
 		// ProjectNodeOld childNode = new ProjectNodeOld(child);
 		projectTreeModel.insertNodeInto(child, root, root.getChildCount());
 		// Make sure the user can see the lovely new node.
 		if (shouldBeVisible) {
 			projectTree.scrollPathToVisible(new TreePath(child.getPath()));
 			projectTree.setSelectionPath(new TreePath(child.getPath()));
 			selection.setNodeSelection(child);
 		}
 		// serialize("default.wsp");
 		return child;
 	}
 
 	/**
 	 * Sets the currently selected node within the project tree.
 	 *
 	 * @param node
 	 *            The project tree node to show up as selected.
 	 */
 	protected void setNodeSelection(ProjectTreeNode node) {
 
 		if (node == null) {
 
 			return;
 		}
 		selection.setNodeSelection(node);
 		projectTree.setSelectionPath(new TreePath(node.getPath()));
 		// todo - watkin - replace with a more appropriate event firing
 		// projectTreeModel.nodeStructureChanged(node);
 	}
 
 	public ProjectSelection getSelection() {
 		return selection;
 	}
 
 	// ----------------------------------------------------------------------
 
 	protected ProjectTreeNode selectedNode = null;
 
 	protected MicroarraySetNode previousMANode = null;
 
 	@Publish
 	public ProjectEvent publishProjectEvent(ProjectEvent event) {
 		return event;
 	}
 
 	@Publish
 	public CaArrayRequestEvent publishCaArrayRequestEvent(
 			CaArrayRequestEvent event) {
 
 		return event;
 	}
 
 	@Publish
 	public CaArrayQueryEvent publishCaArrayQueryEvent(CaArrayQueryEvent event) {
 		return event;
 	}
 
 	@Publish
 	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
 			ProjectNodeAddedEvent event) {
 		return event;
 	}
 
 	@Publish
 	public CommentsEvent publishCommentsEvent(CommentsEvent event) {
 		return event;
 	}
 
 	@Publish
 	public AdjacencyMatrixEvent publishAdjacencyMatrixEvent(
 			AdjacencyMatrixEvent ae) {
 		return ae;
 	}
 
 	/**
 	 * Throws an application event that designates the selection of a project or
 	 * microarray node in the project window.
 	 *
 	 * @param node
 	 */
 	protected void fireNodeSelectionEvent(ProjectTreeNode node) {
 		if (node != null && node != root) {
 			publishProjectEvent(new ProjectEvent(
 					node instanceof ProjectNode ? "Project Node Selected"
 							: "Microarray Node Selected",
 					projectRenderer.microarraySetNodeSelection == null ? null
 							: projectRenderer.microarraySetNodeSelection
 									.getMicroarraySet(),
 					projectRenderer.microarraySetNodeSelection));
 			sendCommentsEvent(node);
 		}
 	}
 
 	public void sendCommentsEvent(ProjectTreeNode forNode) {
 		if (forNode != null) {
 			String description = forNode.getDescription();
 			if (description == null) {
 				description = "";
 			}
 			publishCommentsEvent(new CommentsEvent(description));
 		}
 	}
 
 	private void updateColorContext(DSMicroarraySet maSet) {
 		ColorContext colorContext = (ColorContext) maSet
 				.getObject(ColorContext.class);
 		if (colorContext != null) {
 			CSMicroarraySetView view = new CSMicroarraySetView(maSet);
 			colorContext.updateContext(view);
 		}
 	}
 
 	/**
 	 * Interface <code>ImageSnapshotListener</code> method for receiving
 	 * <code>ImageSnapshotEvent</code> from Visual Plugins. These events
 	 * contain <code>ImageIcon</code> representing visual state of the plugins
 	 * throwing this event.
 	 *
 	 * @param event
 	 *            <code>ImageSnapshotEvent</code>
 	 */
 	@Subscribe
 	public void receive(ImageSnapshotEvent event, Object source) {
 		if (event.getAction() == ImageSnapshotEvent.Action.SAVE) {
 			TreePath path = projectTree.getSelectionPath();
 			if (path != null) {
 				ImageNode imageNode = new ImageNode(event.getImage());
 				ProjectTreeNode node = (ProjectTreeNode) path
 						.getLastPathComponent();
 				projectRenderer.imageNodeSelection = imageNode;
 				if (node instanceof DataSetNode) {
 					projectTreeModel.insertNodeInto(imageNode, node, node
 							.getChildCount());
 				} else if (node instanceof ImageNode) {
 					node = projectRenderer.microarraySetNodeSelection;
 					projectTreeModel.insertNodeInto(imageNode, node, node
 							.getChildCount());
 				} else if (node instanceof DataSetSubNode) {
 					DataSetSubNode subNode = (DataSetSubNode) node;
 					node = (ProjectTreeNode) subNode.getParent();
 					if (node instanceof DataSetNode) {
 						projectTreeModel.insertNodeInto(imageNode, node, node
 								.getChildCount());
 					}
 				}
 
 			}
 		} else {
 			// Ignore all other actions.
 		}
 	}
 
 	@Subscribe
 	public void receive(org.geworkbench.events.PhenotypeSelectorEvent e,
 			Object source) {
 		if (e.getDataSet() instanceof DSMicroarraySet) {
 			DSMicroarraySet microarraySet = (DSMicroarraySet) e.getDataSet();
 			updateColorContext(microarraySet, e);
 		}
 	}
 
 	private void updateColorContext(DSMicroarraySet microarraySet,
 			org.geworkbench.events.PhenotypeSelectorEvent e) {
 		ColorContext colorContext = (ColorContext) microarraySet
 				.getObject(ColorContext.class);
 		if (colorContext != null) {
 			CSMicroarraySetView view = new CSMicroarraySetView(microarraySet);
 			view.useItemPanel(true);
 			if (e.getTaggedItemSetTree() != null
 					&& e.getTaggedItemSetTree().size() > 0) {
 				DSPanel activatedArrays = e.getTaggedItemSetTree()
 						.activeSubset();
 				view.setItemPanel(activatedArrays);
 			}
 			colorContext.updateContext(view);
 		}
 	}
 
 	@Subscribe
 	public void receive(CaArrayQueryResultEvent ce, Object source) {
 
 		loadData.receive(ce);
 	}
 
 	/**
 	 * For receiving the results of applying a normalizer to a microarray set.
 	 *
 	 * @param ne
 	 */
 	@Subscribe
 	public void receive(NormalizationEvent ne, Object source) {
 		if (ne == null) {
 			return;
 		}
 		DSMicroarraySet sourceMA = ne.getOriginalMASet();
 		if (sourceMA == null) {
 			return;
 		}
 
 		DSMicroarraySet resultMA = ne.getNormalizedMASet();
 		updateColorContext(resultMA);
 		// Set up the "history" information for the new dataset.
 		Object[] prevHistory = sourceMA.getValuesForName(HISTORY);
 
 		Object[] historyDetail = sourceMA.getValuesForName(HISTORYDETAIL);
 		String detail = (historyDetail == null ? "" : (String) historyDetail[0]);
 		sourceMA.clearName(HISTORYDETAIL);
 
 		if (prevHistory != null) {
 			sourceMA.clearName(HISTORY);
 		}
 		sourceMA.addNameValuePair(HISTORY, (prevHistory == null ? ""
 				: (String) prevHistory[0])
 				+ "Normalized with " + ne.getInformation() + "\n" + detail + "\n");
 
 		// Notify interested components that the selected dataset has changed
 		// The event is thrown only if the normalized dataset is the one
 		// currently selectd in the project panel.
 		DSDataSet currentDS = (selection != null ? selection.getDataSet()
 				: null);
 		if (currentDS != null && currentDS instanceof DSMicroarraySet
 				&& (DSMicroarraySet) currentDS == sourceMA) {
 			publishProjectEvent(new ProjectEvent(ProjectEvent.SELECTED,
 					sourceMA, selectedNode));
 		}
 	}
 
 	/**
 	 * For receiving the submission / results of comparative modeling analysis
 	 * to PDB protein structure
 	 *
 	 * @param sae
 	 */
 	@Subscribe
 	public void receive(StructureAnalysisEvent sae, Object source) {
 		if (sae == null) {
 			return;
 		}
 		DSProteinStructure dsp = sae.getDataSet();
 		if (dsp == null) {
 			return;
 		}
 
 		String res = sae.getAnalyzedStructure();
 
 		String desc = new String("SkyLine job submitted for ");
 		if (res != null && res == "SkyLine results available") {
 			desc = res + " for ";
 		}
 
 		// Set up the "history" information for the new dataset.
 		Object[] prevHistory = dsp.getValuesForName(HISTORY);
 		if (prevHistory != null) {
 			dsp.clearName(HISTORY);
 		}
 		dsp.addNameValuePair(HISTORY, (prevHistory == null ? ""
 				: (String) prevHistory[0])
 				+ desc + dsp.getLabel() + "\n");
 		// Notify interested components that the selected dataset has changed
 		// The event is thrown only if the analyzed dataset is the one
 		// currently selectd in the project panel.
 		DSDataSet currentDS = (selection != null ? selection.getDataSet()
 				: null);
 		if (currentDS != null && currentDS instanceof DSProteinStructure
 				&& (DSProteinStructure) currentDS == dsp) {
 			publishProjectEvent(new ProjectEvent(ProjectEvent.SELECTED, dsp,
 					selectedNode));
 		}
 	}
 
 	public static void addHistoryDetail(DSExtendable objectWithHistory,
 			String detail) {
 		objectWithHistory.clearName(HISTORYDETAIL);
 		objectWithHistory.addNameValuePair(HISTORYDETAIL, detail);
 	}
 
 	public static void addToHistory(DSExtendable objectWithHistory,
 			String newHistory) {
 
 		Object[] prevHistory = objectWithHistory.getValuesForName(HISTORY);
 		if (prevHistory != null) {
 			objectWithHistory.clearName(HISTORY);
 		}
 		objectWithHistory.addNameValuePair(HISTORY, (prevHistory == null ? ""
 				: (String) prevHistory[0])
 				+ newHistory + "\n");
 	}
 
 	/**
 	 * For receiving the results of applying a filter to a microarray set.
 	 *
 	 * @param fe
 	 */
 	@Subscribe
 	public void receive(org.geworkbench.events.FilteringEvent fe, Object source) {
 		if (fe == null) {
 			return;
 		}
 		DSMicroarraySet sourceMA = fe.getOriginalMASet();
 		if (sourceMA == null) {
 			return;
 		}
 
 		// Set up the "history" information for the new dataset.
 		Object[] prevHistory = sourceMA.getValuesForName(HISTORY);
 		if (prevHistory != null) {
 			sourceMA.clearName(HISTORY);
 		}
 		sourceMA.addNameValuePair(HISTORY, (prevHistory == null ? ""
 				: (String) prevHistory[0])
 				+ "Filtered with " + fe.getInformation() + "\n");
 		// Notify interested components that the selected dataset has changed
 		// The event is thrown only if the dataset filtered is the one
 		// currently selectd in the project panel.
 		DSDataSet currentDS = (selection != null ? selection.getDataSet()
 				: null);
 
 		if (currentDS != null && currentDS instanceof DSMicroarraySet
 				&& (DSMicroarraySet) currentDS == sourceMA) {
 			publishProjectEvent(new ProjectEvent(ProjectEvent.SELECTED,
 					sourceMA, selectedNode));
 		}
 	}
 
 	/**
 	 * Clears the current workspace from the project window and notifies all
 	 * componets that have registered to receive workspace clearing events.
 	 */
 	protected void clear() {
 		if (root != null) {
 			root.removeAllChildren();
 			projectTreeModel.reload(root);
 			projectRenderer.clearNodeSelections();
 			selectedNode = null;
 		}
 
 		publishProjectEvent(new ProjectEvent(ProjectEvent.CLEARED, null, null));
 		selection.clearNodeSelections();
 	}
 
 	/**
 	 * Used as the "name" in the name-value pair that keeps track of the history
 	 * of changes that a given dataset is being submitted to.
 	 */
 	public static final String HISTORY = "History";
 
 	public static final String HISTORYDETAIL = "HistoryDetail";
 
 	/**
 	 * Used as the "name" in the name-value pair that keeps track of the
 	 * comments of a microarray set have been modified.
 	 */
 	protected final String COMMENTS_MODIFIED = "Comments modified";
 
 	@Publish
 	public ImageSnapshotEvent publishImageSnapshot(ImageSnapshotEvent event) {
 		return event;
 	}
 
 	/**
 	 * Interface <code>MenuListener</code> method that returns the appropriate
 	 * <code>ActionListener</code> to handle <code>MenuEvent</code>
 	 * generated by <code>MenuItem</code> referenced by <code>menuKey</code>
 	 * attribute
 	 *
 	 * @param menuKey
 	 *            refers to <code>MenuItem</code>
 	 * @return <ActionListener> to handle <code>MenuEvent</code> generated by
 	 *         <code>MenuItem</code>
 	 */
 	public ActionListener getActionListener(String menuKey) {
 		return (ActionListener) listeners.get(menuKey);
 	}
 
 	/**
 	 * Interface <code>VisualPlugin</code> method that returns a
 	 * <code>Component</code> which is the visual representation of the this
 	 * plugin.
 	 *
 	 * @return <code>Component</code> visual representation of
 	 *         <code>ProjectPane</code>
 	 */
 	public Component getComponent() {
 		return jProjectPanel;
 	}
 
 	/**
 	 * <code>JComponent</code> types that constitute the
 	 * <code>ProjectPanel</code>
 	 */
 	protected JPanel jProjectPanel = new JPanel();
 
 	private JLabel projectPanelTitleLabel = new JLabel("Project Folders");
 
 	protected JScrollPane jDataSetScrollPane = new JScrollPane();
 
 	protected BorderLayout borderLayout1 = new BorderLayout();
 
 	protected ProjectTreeNode root = new ProjectTreeNode("Workspace");
 
 	protected DefaultTreeModel projectTreeModel = new DefaultTreeModel(root);
 
 	protected JTree projectTree = new JTree(projectTreeModel);
 
 	protected TreeNodeRenderer projectRenderer = new TreeNodeRenderer(selection);
 
 	protected JPopupMenu jRootMenu = new JPopupMenu();
 
 	protected JPopupMenu jProjectMenu = new JPopupMenu();
 
 	protected JMenuItem jLoadProjectItem = new JMenuItem();
 
 	protected JMenuItem jNewProjectItem = new JMenuItem();
 
 	protected JMenuItem jLoadMArrayItem = new JMenuItem();
 
 	protected JMenuItem jOpenRemotePDBItem = new JMenuItem();
 
 	protected JMenuItem jLoadRemoteMArrayItem = new JMenuItem();
 
 	protected JMenuItem jMergeDatasets = new JMenuItem();
 
 	protected JMenuItem jRenameProjectItem = new JMenuItem();
 
 	protected JMenuItem jRemoveDataSetItem = new JMenuItem();
 
 	protected JMenuItem jRenameDataset = new JMenuItem();
 
 	/**
 	 * PlaceHolder for <code>JComponent</code> listeners to be added to the
 	 * application's <code>JMenuBar</code> through the application
 	 * configuration functionality
 	 */
 	protected HashMap<String, ActionListener> listeners = new HashMap<String, ActionListener>();
 
 	private void initializeSwingComponents() {
 		projectPanelTitleLabel.setBorder(BorderFactory.createEtchedBorder());
 		projectPanelTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
 		ActionListener listener = null;
 		jDataSetScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
 		jDataSetScrollPane.setMinimumSize(new Dimension(122, 80));
 
 		ToolTipManager.sharedInstance().registerComponent(projectTree);
 		projectTree.setCellRenderer(projectRenderer);
 		projectTree.getSelectionModel().setSelectionMode(
 				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 		jRenameProjectItem.setText("Rename Project");
 		listener = new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameProjectItem_actionPerformed(e);
 			}
 
 		};
 		listeners.put("Edit.Rename.Project", listener);
 		jRenameProjectItem.addActionListener(listener);
 		jRenameDataset.setText("Rename File");
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameDataset_actionPerformed(e);
 			}
 
 		};
 		listeners.put("Edit.Rename.File", listener);
 		jRenameDataset.addActionListener(listener);
 
 		jProjectPanel.setLayout(new BorderLayout());
 		jProjectPanel.add(projectPanelTitleLabel, BorderLayout.NORTH);
 		jProjectPanel.add(jDataSetScrollPane, BorderLayout.CENTER);
 		jProjectPanel.add(progressBar, BorderLayout.SOUTH);
 		//jProjectPanel.setName("Projects"); // no use
 
 		projectTree.setBorder(new EmptyBorder(1, 1, 0, 0));
 		jDataSetScrollPane.getViewport().add(projectTree, null);
 
 		jNewProjectItem.setText("New Project");
 		listener = new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jNewProjectItem_actionPerformed(e);
 			}
 
 		};
 		listeners.put("File.New.Project", listener);
 		jNewProjectItem.addActionListener(listener);
 		jLoadMArrayItem.setText("Open File(s)");
 		listener = new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jLoadMArrayItem_actionPerformed(e);
 			}
 
 		};
 		listeners.put("File.Open.File", listener);
 		jLoadMArrayItem.addActionListener(listener);
 		jMergeDatasets.setText("Merge Files");
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jMergeDatasets_actionPerformed(e);
 			}
 
 		};
 		listeners.put("File.Merge Datasets", listener);
 		jMergeDatasets.addActionListener(listener);
 
 		jOpenRemotePDBItem.setText("Open PDB File from RCSB Protein Data Bank");
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jOpenRemotePDBItem_actionPerformed(e);
 			}
 
 		};
 		listeners.put("File.OpenRemotePDB.File", listener);
 		jOpenRemotePDBItem.addActionListener(listener);
 
 		jRootMenu.add(jNewProjectItem);
 		jProjectMenu.add(jLoadMArrayItem);
 		jProjectMenu.addSeparator();
 		jProjectMenu.add(jOpenRemotePDBItem);
 		jProjectMenu.addSeparator();
 		jProjectMenu.add(jRenameProjectItem);
 
 		projectTree.addMouseListener(new java.awt.event.MouseAdapter() {
 
 			public void mouseReleased(MouseEvent e) {
 				jProjectTree_mouseReleased(e);
 			}
 
 		});
 
 		projectTree.addKeyListener(new java.awt.event.KeyAdapter() {
 
 			public void keyReleased(KeyEvent e) {
 				jProjectTree_keyReleased(e);
 			}
 
 		});
 
 		// Add the action listeners that respond to the various menu selections
 		// and popup selections.
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveWorkspace_actionPerformed(false);
 			}
 
 		};
 		listeners.put("File.Save.Workspace", listener);
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openWorkspace_actionPerformed();
 			}
 
 		};
 		listeners.put("File.Open.Workspace", listener);
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newWorkspace_actionPerformed(e);
 			}
 
 		};
 		listeners.put("File.New.Workspace", listener);
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 
 		};
 		listeners.put("File.Remove", listener);
 
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveNodeAsFile();
 			}
 
 		};
 		listeners.put("File.Export", listener);
 
 		// Let the main frame listen to window-closing event ZJ 2008-05-01
 		GeawConfigObject.getGuiWindow().addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				int n = JOptionPane
 						.showConfirmDialog(
 								null,
 								"You're closing geWorkbench. \nDo you want to save the current workspace?",
 								"Save or not?",
 								JOptionPane.YES_NO_CANCEL_OPTION);
 				if (n == JOptionPane.CANCEL_OPTION)
 					return;
 
 				if (n == JOptionPane.YES_OPTION) {
 					saveWorkspace_actionPerformed(true);
 				} else { // if choosing No
 					GeawConfigObject.getGuiWindow().dispose();
 					System.exit(0);
 				}
 			}
 		});
 
 	}
 
 	private void saveWorkspace_actionPerformed(boolean terminating) {
 		WorkspaceHandler ws = new WorkspaceHandler();
 		ws.save(WORKSPACE_DIR, terminating);
 		if (!StringUtils.isEmpty(ws.getWorkspacePath()))
 			GUIFramework.getFrame().setTitle(
 					System.getProperty("application.title") + " ["
 							+ ws.getWorkspacePath() + "]");
 	}
 
 	private void openWorkspace_actionPerformed() {
 		WorkspaceHandler ws = new WorkspaceHandler();
 		ws.open(WORKSPACE_DIR);
 		if (!StringUtils.isEmpty(ws.getWorkspacePath()))
 			GUIFramework.getFrame().setTitle(
 					System.getProperty("application.title") + " ["
 							+ ws.getWorkspacePath() + "]");
 	}
 
 	/**
 	 *
 	 * @param pendingEvent
 	 * @return
 	 */
 	@Publish
 	public PendingNodeLoadedFromWorkspaceEvent publishPendingNodeLoadedFromWorkspaceEvent(
 			PendingNodeLoadedFromWorkspaceEvent event) {
 		return event;
 	}
 
 	private void newWorkspace_actionPerformed(ActionEvent e) {
 		WorkspaceHandler ws = new WorkspaceHandler();
 		if (!ws.confirmLoading(WORKSPACE_DIR, null))
 			return;
 		clear();
 		GUIFramework.getFrame().setTitle(
 				System.getProperty("application.title"));
 	}
 
 	protected void imageRemove_actionPerformed(ProjectTreeNode node) {
 
 		projectTreeModel.removeNodeFromParent(node);
 		publishImageSnapshot(new ImageSnapshotEvent("ImageSnapshot", null,
 				ImageSnapshotEvent.Action.SHOW));
 	}
 
 	@Script
 	public DSDataSet getDataSet() {
 		return selection.getDataSet();
 	}
 
 	@Publish
 	public ProjectNodePostCompletedEvent publishPostProcessingEvent(
 			ProjectNodePostCompletedEvent event) {
 		return event;
 	}
 
 	@Publish
 	public ProjectNodeRemovedEvent publishNodeRemovedEvent(
 			ProjectNodeRemovedEvent event) {
 		return event;
 	}
 
 	@Publish
 	public ProjectNodeRenamedEvent publishNodeRenamedEvent(
 			ProjectNodeRenamedEvent event) {
 		return event;
 	}
 }
