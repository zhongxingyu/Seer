 package org.gwaspi.model;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JTree;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.global.Config;
 import org.gwaspi.global.Text;
 import org.gwaspi.gui.CurrentMatrixPanel;
 import org.gwaspi.gui.CurrentStudyPanel;
 import org.gwaspi.gui.GWASpiExplorerPanel;
 import org.gwaspi.gui.IntroPanel;
 import org.gwaspi.gui.MatrixAnalysePanel;
 import org.gwaspi.gui.MatrixMarkerQAPanel;
 import org.gwaspi.gui.StudyManagementPanel;
 import org.gwaspi.gui.reports.ChartDefaultDisplay;
 import org.gwaspi.gui.reports.ManhattanChartDisplay;
 import org.gwaspi.gui.reports.Report_AnalysisPanel;
 import org.gwaspi.gui.reports.Report_HardyWeinbergSummary;
 import org.gwaspi.gui.reports.Report_QAMarkersSummary;
 import org.gwaspi.gui.reports.Report_QASamplesSummary;
 import org.gwaspi.gui.reports.Report_SampleInfoPanel;
 import org.gwaspi.gui.reports.SampleQAHetzygPlotZoom;
 import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;
 import org.gwaspi.model.GWASpiExplorerNodes.UncollapsableNodeElementInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class GWASpiExplorer {
 
 	private static final Logger log = LoggerFactory.getLogger(GWASpiExplorer.class);
 
 	private JTree tree;
 	private boolean playWithLineStyle;
 	/** Possible values are "Angled" (the default), "Horizontal", and "None". */
 	private String lineStyle;
 	// Optionally set the look and feel.
 	private static Icon customOpenIcon = initIcon("hex_open.png");
 	private static Icon customClosedIcon = initIcon("hex_closed.png");
 	private static Icon customLeafIcon = initIcon("leaf_sepia.png");
 
 	public GWASpiExplorer() {
 
 		this.tree = null;
 		this.playWithLineStyle = false;
 		this.lineStyle = "Horizontal";
 	}
 
 	public JTree getGWASpiTree() throws IOException {
 
 		if (tree == null) {
 			createGWASpiTree();
 		}
 
 		return tree;
 	}
 
 	private void createGWASpiTree() throws IOException {
 
 		// Create the nodes.
 		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new UncollapsableNodeElementInfo(
 				NodeElementInfo.NODE_ID_NONE,
 				NodeElementInfo.NODE_ID_NONE,
 				Text.App.treeParent,
 				Text.App.appName));
 		growTree(top);
 
 		// Create a tree that allows one selection at a time.
 		tree = new JTree(top);
 
 		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
 		renderer.setClosedIcon(customClosedIcon);
 		renderer.setOpenIcon(customOpenIcon);
 		renderer.setLeafIcon(customLeafIcon);
 		tree.setCellRenderer(renderer);
 
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
 		tree.setSelectionRow(0);
 
 		// Listen for when the selection changes.
 		tree.addTreeSelectionListener(treeListener);
 
 		// Add pre-expansion event listener
 		tree.addTreeWillExpandListener(new MyTreeWillExpandListener());
 
 		if (playWithLineStyle) {
 			tree.putClientProperty("JTree.lineStyle", lineStyle);
 		}
 	}
 
 	private static void growTree(DefaultMutableTreeNode top) throws IOException {
 
 		//<editor-fold defaultstate="expanded" desc="STUDY MANAGEMENT">
 		DefaultMutableTreeNode category = new DefaultMutableTreeNode(new UncollapsableNodeElementInfo(
 				NodeElementInfo.NODE_ID_NONE,
 				NodeElementInfo.NODE_ID_NONE,
 				Text.App.treeParent,
 				Text.App.treeStudyManagement));
 		top.add(category);
 
 		// LOAD ALL STUDIES
 		List<Study> studyList = StudyList.getStudyList();
 		for (int i = 0; i < studyList.size(); i++) {
 
 			// LOAD CURRENT STUDY
 			DefaultMutableTreeNode studyItem = GWASpiExplorerNodes.createStudyTreeNode(studyList.get(i).getId());
 
 			// LOAD SAMPLE INFO FOR CURRENT STUDY
 			DefaultMutableTreeNode sampleInfoItem = GWASpiExplorerNodes.createSampleInfoTreeNode(studyList.get(i).getId());
 			if (sampleInfoItem != null) {
 				studyItem.add(sampleInfoItem);
 			}
 
 			// LOAD MATRICES FOR CURRENT STUDY
 			List<Matrix> matrixList = MatricesList.getMatrixList(studyList.get(i).getId());
 			for (int j = 0; j < matrixList.size(); j++) {
 
 				DefaultMutableTreeNode matrixItem = GWASpiExplorerNodes.createMatrixTreeNode(matrixList.get(j).getId());
 
 				// LOAD Parent OPERATIONS ON CURRENT MATRIX
 				List<Operation> parentOperations = OperationsList.getOperationsList(matrixList.get(j).getId(), -1);
 				List<Operation> allOperations = OperationsList.getOperationsList(matrixList.get(j).getId());
 				for (int k = 0; k < parentOperations.size(); k++) {
 					// LOAD SUB OPERATIONS ON CURRENT MATRIX
 					Operation currentOP = parentOperations.get(k);
 					DefaultMutableTreeNode operationItem = GWASpiExplorerNodes.createOperationTreeNode(currentOP.getId());
 
 
 					List<Operation> childrenOpAL = getChildrenOperations(allOperations, currentOP.getId());
 					for (int m = 0; m < childrenOpAL.size(); m++) {
 						Operation subOP = childrenOpAL.get(m);
 						DefaultMutableTreeNode subOperationItem = GWASpiExplorerNodes.createSubOperationTreeNode(subOP.getId());
 
 						// LOAD REPORTS ON CURRENT SUB-OPERATION
 						if (!subOP.getOperationType().equals(OPType.HARDY_WEINBERG.toString())) { //NOT IF HW
 							List<Report> reportsList = ReportsList.getReportsList(subOP.getId(), Integer.MIN_VALUE);
 							for (int n = 0; n < reportsList.size(); n++) {
 								Report rp = reportsList.get(n);
								if (!rp.getReportType().equals(OPType.ALLELICTEST.toString())
										&& !rp.getReportType().equals(OPType.GENOTYPICTEST.toString())
										&& !rp.getReportType().equals(OPType.TRENDTEST.toString())) {
 									DefaultMutableTreeNode reportItem = GWASpiExplorerNodes.createReportTreeNode(reportsList.get(n).getId());
 									subOperationItem.add(reportItem);
 								}
 
 							}
 						}
 						operationItem.add(subOperationItem);
 					}
 
 					// START TESTING
 					// LOAD REPORTS ON CURRENT OPERATION
 					List<Report> reportsList = ReportsList.getReportsList(currentOP.getId(), Integer.MIN_VALUE);
 					if (!currentOP.getOperationType().equals(OPType.SAMPLE_QA.toString())) { //SAMPLE_QA MUST BE DEALT DIFFERENTLY
 						for (int n = 0; n < reportsList.size(); n++) {
 							DefaultMutableTreeNode reportItem = GWASpiExplorerNodes.createReportTreeNode(reportsList.get(n).getId());
 							operationItem.add(reportItem);
 						}
 					} else {
 						// DEAL WITH SAMPLE_HTZYPLOT
 						for (int n = 0; n < reportsList.size(); n++) {
 							Report rp = reportsList.get(n);
							if (rp.getReportType().equals(OPType.SAMPLE_HTZYPLOT.toString())) {
 								DefaultMutableTreeNode reportItem = GWASpiExplorerNodes.createReportTreeNode(reportsList.get(n).getId());
 								operationItem.add(reportItem);
 							}
 						}
 					}
 					// END TESTING
 
 					matrixItem.add(operationItem);
 
 				}
 				studyItem.add(matrixItem);
 			}
 
 			// ADD ALL TREE-NODES INTO TREE
 			category.add(studyItem);
 		}
 
 		top.add(category);
 		//</editor-fold>
 	}
 
 	//<editor-fold defaultstate="expanded" desc="LISTENER">
 	// TREE SELECTION LISTENER
 	private static TreeSelectionListener treeListener = new TreeSelectionListener() {
 		public void valueChanged(TreeSelectionEvent evt) {
 
 			JTree tree = (JTree) evt.getSource();
 			GWASpiExplorerPanel gwasPiExplorerPanel = GWASpiExplorerPanel.getSingleton();
 
 			tree.setEnabled(false);
 
 			// CHECK IF LISTENER IS ALLOWED TO UPDATE CONTENT PANEL
 			if (!gwasPiExplorerPanel.isRefreshContentPanel()) {
 				tree.setEnabled(true);
 				return;
 			}
 
 			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
 			if (currentNode == null) {
 				tree.setEnabled(true);
 				return;
 			}
 
 			// Check first if we are at the GWASpi root
 			if (currentNode.isRoot()) { // We are in GWASpi node
 				gwasPiExplorerPanel.setPnl_Content(new IntroPanel());
 				gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 			}
 
 			// Check where we are in tree and show appropiate content panel
 			Object currentElement = currentNode.getUserObject();
 			NodeElementInfo currentElementInfo = (NodeElementInfo) currentElement;
 
 			TreePath treePath = evt.getPath();
 			if (treePath != null && !currentElementInfo.getNodeType().equals(Text.App.treeParent)) {
 				try {
 					Config.setConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, currentElementInfo.getNodeUniqueName());
 				} catch (IOException ex) {
 					log.error(null, ex);
 				}
 			} else if (currentElement.equals(Text.App.treeStudyManagement)) {
 				try {
 					Config.setConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, Text.App.treeStudyManagement);
 				} catch (IOException ex) {
 					log.error(null, ex);
 				}
 			} else {
 				try {
 					Config.setConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, Text.App.appName);
 				} catch (IOException ex) {
 					log.error(null, ex);
 				}
 			}
 
 			// Get parent node of currently selected node
 			NodeElementInfo parentElementInfo = null;
 			if (treePath.getParentPath() != null) {
 				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treePath.getParentPath().getLastPathComponent();
 				Object parentElement = parentNode.getUserObject();
 				parentElementInfo = (NodeElementInfo) parentElement;
 			}
 
 			// Reference Databse Branch
 			if (currentElementInfo.getNodeUniqueName().equals(Text.App.treeReferenceDBs)) {
 			} // Study Management Branch
 			//else if(currentElementInfo.getNodeType().equals(Text.App.treeStudyManagement)) { // XXX
 			else if (currentElementInfo.getNodeUniqueName().equals(Text.App.treeStudyManagement)) {
 				try {
 					// We are in StudyList node
 					gwasPiExplorerPanel.setPnl_Content(new StudyManagementPanel());
 					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 				} catch (IOException ex) {
 					log.warn(null, ex);
 				}
 			} // Study Branch
 			//else if(parentNode != null && parentNode.toString().equals(Text.App.treeStudyManagement)){
 			else if (currentElementInfo.getNodeType().equals(Text.App.treeStudy)) {
 				try {
 					gwasPiExplorerPanel.setPnl_Content(new CurrentStudyPanel(currentElementInfo.getNodeId()));
 					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 				} catch (IOException ex) {
 					log.warn("StudyID: " + currentElementInfo.getNodeId(), ex);
 				}
 			} // Sample Info Branch
 			else if (currentElementInfo.getNodeType().equals(Text.App.treeSampleInfo)) {
 				try {
 					gwasPiExplorerPanel.setPnl_Content(new Report_SampleInfoPanel(parentElementInfo.getNodeId()));
 					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 				} catch (IOException ex) {
 					log.warn(null, ex);
 				}
 			} // Matrix Branch
 			else if (currentElementInfo.getNodeType().equals(Text.App.treeMatrix)) {
 				try {
 					// We are in MatrixItemAL node
 					tree.expandPath(treePath);
 					gwasPiExplorerPanel.setPnl_Content(new CurrentMatrixPanel(currentElementInfo.getNodeId()));
 					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 				} catch (IOException ex) {
 					log.warn(null, ex);
 				}
 			} // Operations Branch
 			else if (currentElementInfo.getNodeType().equals(Text.App.treeOperation)) {
 				try {
 					if (parentElementInfo.getNodeType().toString().equals(Text.App.treeOperation)) {
 						// Display SubOperation analysis panel
 						tree.expandPath(treePath);
 						Operation currentOP = OperationsList.getById(currentElementInfo.getNodeId());
 						Operation parentOP = OperationsList.getById(parentElementInfo.getNodeId());
 						if (currentOP.getOperationType().equals(OPType.HARDY_WEINBERG.toString())) {
 							// Display HW Report
 							List<Report> reportsList = ReportsList.getReportsList(currentOP.getId(), currentOP.getParentMatrixId());
 							if (reportsList.size() > 0) {
 								Report hwReport = reportsList.get(0);
 								String reportFile = hwReport.getFileName();
 								gwasPiExplorerPanel.setPnl_Content(new Report_HardyWeinbergSummary(hwReport.getStudyId(), reportFile, hwReport.getParentOperationId()));
 								gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 							}
 						} else if (currentOP.getOperationType().equals(OPType.ALLELICTEST.toString())) {
 							// Display Association Report
 							gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(currentOP.getStudyId(), currentOP.getParentMatrixId(), currentOP.getId(), null));
 							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 						} else if (currentOP.getOperationType().equals(OPType.GENOTYPICTEST.toString())) {
 							// Display Association Report
 							gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(currentOP.getStudyId(), currentOP.getParentMatrixId(), currentOP.getId(), null));
 							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 						} else if (currentOP.getOperationType().equals(OPType.TRENDTEST.toString())) {
 							// Display Trend Test Report
 							gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(currentOP.getStudyId(), currentOP.getParentMatrixId(), currentOP.getId(), null));
 							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 						} else {
 							//gwasPiExplorerPanel.pnl_Content = new MatrixAnalysePanel(parentOP.getParentMatrixId(), currentElementInfo.parentNodeId);
 							gwasPiExplorerPanel.setPnl_Content(new MatrixAnalysePanel(parentOP.getParentMatrixId(), currentElementInfo.getNodeId()));
 							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 						}
 					} else {
 						// Display Operation
 						tree.expandPath(treePath);
 						Operation currentOP = OperationsList.getById(currentElementInfo.getNodeId());
 						if (currentOP.getOperationType().equals(OPType.MARKER_QA.toString())) {
 							// Display MarkerQA panel
 							gwasPiExplorerPanel.setPnl_Content(new MatrixMarkerQAPanel(currentOP.getParentMatrixId(), currentOP.getId()));
 							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 						} else if (currentOP.getOperationType().equals(OPType.SAMPLE_QA.toString())) {
 							// Display SampleQA Report
 							List<Report> reportsList = ReportsList.getReportsList(currentOP.getId(), currentOP.getParentMatrixId());
 							if (reportsList.size() > 0) {
 								Report sampleQAReport = reportsList.get(0);
 								String reportFile = sampleQAReport.getFileName();
 								gwasPiExplorerPanel.setPnl_Content(new Report_QASamplesSummary(sampleQAReport.getStudyId(), reportFile, sampleQAReport.getParentOperationId()));
 								gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 							}
 						} else {
 							// Display Operation analysis panel
 							gwasPiExplorerPanel.setPnl_Content(new MatrixAnalysePanel(parentElementInfo.getNodeId(), currentElementInfo.getNodeId()));
 							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 						}
 					}
 				} catch (IOException ex) {
 					log.warn(null, ex);
 				}
 			} // Reports Branch
 			else if (currentElementInfo.getNodeType().equals(Text.App.treeReport)) {
 				try {
 					// Display report summary
 					tree.expandPath(treePath);
 					Report rp = ReportsList.getById(currentElementInfo.getNodeId());
 					String reportFile = rp.getFileName();
					if (rp.getReportType().equals(OPType.SAMPLE_HTZYPLOT.toString())) {
 						gwasPiExplorerPanel.setPnl_Content(new SampleQAHetzygPlotZoom(rp.getParentOperationId()));
 						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 					}
					if (rp.getReportType().equals(OPType.ALLELICTEST.toString())) {
 						//gwasPiExplorerPanel.pnl_Content = new Report_AssociationSummary(rp.getId(), reportFile, rp.getParentOperationId(), null);
 						gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(rp.getStudyId(), rp.getParentMatrixId(), rp.getParentOperationId(), null));
 						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 					}
					if (rp.getReportType().equals(OPType.QQPLOT.toString())) {
 						gwasPiExplorerPanel.setPnl_Content(new ChartDefaultDisplay(rp.getStudyId(), reportFile, rp.getParentOperationId()));
 						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 					}
					if (rp.getReportType().equals(OPType.MANHATTANPLOT.toString())) {
 						gwasPiExplorerPanel.setPnl_Content(new ManhattanChartDisplay(rp.getStudyId(), reportFile, rp.getParentOperationId()));
 						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 					}
					if (rp.getReportType().equals(OPType.MARKER_QA.toString())) {
 						gwasPiExplorerPanel.setPnl_Content(new Report_QAMarkersSummary(rp.getStudyId(), reportFile, rp.getParentOperationId()));
 						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 					}
 //					if(rp.getReportType().equals(OPType.SAMPLE_QA.toString())){
 //						gwasPiExplorerPanel.pnl_Content = new Report_QASamplesSummary(rp.getId(), reportFile, rp.getParentOperationId());
 //						gwasPiExplorerPanel.scrl_Content.setViewportView(gwasPiExplorerPanel.pnl_Content);
 //					}
					if (rp.getReportType().equals(OPType.HARDY_WEINBERG.toString())) {
 						gwasPiExplorerPanel.setPnl_Content(new Report_HardyWeinbergSummary(rp.getStudyId(), reportFile, rp.getParentOperationId()));
 						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 					}
 				} catch (IOException ex) {
 					log.warn(null, ex);
 				}
 			} else {
 				gwasPiExplorerPanel.setPnl_Content(new IntroPanel());
 				gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
 			}
 
 			// THIS IS TO AVOID RANDOM MONKEY CLICKER BUG
 			try {
 				Thread.sleep(300);
 			} catch (InterruptedException ex) {
 				log.warn(null, ex);
 			}
 
 			tree.setEnabled(true);
 		}
 	};
 
 	// PRE-EXPANSION/COLLAPSE LISTENER
 	private class MyTreeWillExpandListener implements TreeWillExpandListener {
 
 		public void treeWillExpand(TreeExpansionEvent evt) throws ExpandVetoException {
 			// Get the path that will be expanded
 			TreePath treePath = evt.getPath();
 
 			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
 			Object currentElement = currentNode.getUserObject();
 			NodeElementInfo currentNodeInfo = (NodeElementInfo) currentElement;
 			if (currentNodeInfo.isCollapsable()) {
 				// ALLWAYS ALLOW EXPANSION
 			}
 		}
 
 		public void treeWillCollapse(TreeExpansionEvent evt) throws ExpandVetoException {
 			// Get the path that will be expanded
 			TreePath treePath = evt.getPath();
 
 			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
 			Object currentElement = currentNode.getUserObject();
 			NodeElementInfo currentNodeInfo = (NodeElementInfo) currentElement;
 			if (!currentNodeInfo.isCollapsable()) {
 				// VETO EXPANSION
 				throw new ExpandVetoException(evt);
 			}
 		}
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="HELPERS">
 	private static List<Operation> getChildrenOperations(List<Operation> operations, int parentOpId) {
 
 		List<Operation> childrenOperations = new ArrayList<Operation>();
 
 		for (int i = 0; i < operations.size(); i++) {
 			int currentParentOPId = (Integer) operations.get(i).getParentOperationId();
 			if (currentParentOPId == parentOpId) {
 				childrenOperations.add(operations.get(i));
 			}
 		}
 
 		return childrenOperations;
 	}
 
 	// XXX this function could be used in other classes too!
 	private static Icon initIcon(String iconName) {
 		URL logoPath = GWASpiExplorer.class.getResource("/img/icon/" + iconName);
 		//String logoPath = Config.getConfigValue("ConfigDir", "") + "/" +iconName;
 		Icon logo = new ImageIcon(logoPath);
 		return logo;
 	}
 	//</editor-fold>
 }
