 /*************************************************************************
  * Copyright (c) 2012 Federal University of Minas Gerais - UFMG 
  * All rights avaiable. This program and the accompanying materials
  * are made avaiable under the terms of the Eclipse Public Lincense v1.0
  * which accompanies this distribution, and is avaiable at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Alcemir R. Santos - improvements on the ConcernMapper
  * 			architeture. ConcernMapper is available at
  * 			http://www.cs.mcgill.ca/~martin/cm/
  *************************************************************************/
 package br.ufmg.dcc.tabuleta.views;
 
 import java.util.HashSet;
 
 import javax.swing.JComponent;
 
 import org.eclipse.albireo.core.SwingControl;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.jacoco.core.analysis.ICoverageNode;
 
 import prefuse.data.Graph;
 import prefuse.data.Node;
 import br.ufmg.dcc.tabuleta.Tabuleta;
 import br.ufmg.dcc.tabuleta.actions.util.CmFilesOperations;
 import br.ufmg.dcc.tabuleta.ui.ProblemManager;
 import br.ufmg.dcc.tabuleta.views.components.GraphManager;
 import ca.utoronto.cs.prefuseextensions.demo.StarburstDemo;
 
 import com.mountainminds.eclemma.core.CoverageTools;
 import com.mountainminds.eclemma.core.ICoverageSession;
 import com.mountainminds.eclemma.core.ScopeUtils;
 
 /**
  * This class is responsible to draw a a Sunburst of the test classes associated
  * with the features in concern model.
  * 
  * @author Alcemir R. Santos
  * 
  */
 public class FeatureSunburstView extends ViewPart {
 
 
 	private Composite parent;
 	private static Composite myContents;
 
	private static GraphsViewer graphsViewer;
 
 	private Action selectCMAction;
 	private Action updateViewWithCoverageSession;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
 	 * .Composite)
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		this.parent = parent;
 		myContents = new Composite(parent, SWT.NONE);
 
 		graphsViewer = new GraphsViewer();
 
 		GridLayout gLayout = new GridLayout(1, false);
 		myContents.setLayout(gLayout);
 
 		Label lbl = new Label(myContents, SWT.NULL);
 		lbl.setText("Here goes the visualization.");
 
 		makeActions();
 		contributeToActionBars();
 	}
 
 	/**
 	 * 
 	 */
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(selectCMAction);
 		manager.add(updateViewWithCoverageSession);
 		manager.add(new Separator());
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(selectCMAction);
 		manager.add(updateViewWithCoverageSession);
 	}
 
 	/**
 	 * 
 	 */
 	private void makeActions() {
 		selectCMAction = new CmFileSelectAction();
 		updateViewWithCoverageSession = new UpdateViewWithCoverageSessionAction();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
 	 */
 	@Override
 	public void setFocus() {}
 
 	public static void refresh() {
 		for (Control con : myContents.getChildren()) {
 			con.dispose();
 		}
 
 		getSwingControl();
 	}
 
 	protected static SwingControl getSwingControl() {
 		SwingControl swingControl = new SwingControl(myContents, SWT.NONE) {
 			protected JComponent createSwingComponent() {
 				Graph graph = GraphManager.getInstance().getLastGraph();
 				return StarburstDemo.demo(graph, "name");
 				// return StarburstDemo.demo("/socialnet.xml", "name");
 			}
 
 			public Composite getLayoutAncestor() {
 				return myContents;
 			}
 		};
 		return swingControl;
 	}
 
 	private void showMessage(String message) {
 		MessageDialog.openInformation(myContents.getShell(),
 				"Feature Sunburst View", message);
 	}
 
 	/**
 	 * @return
 	 */
	public static GraphsViewer getGraphsViewer() {
 		return graphsViewer;
 	}
 
 	/**
 	 * This viewer helps to update the SunBurst. 
 	 */
	public class GraphsViewer {
 	
 		public void refresh() {
 			refresh();
 		}
 	}
 
 	/**
 	 * This action builds the SunBurst from a .cm file selected in a FileDialog. 
 	 */
 	protected class CmFileSelectAction extends Action {
 
 		public CmFileSelectAction() {
 			setText("Select CM");
 			setToolTipText("Select CM action tooltip");
 			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
 					.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
 		}
 
 		public void run() {
 			// File standard dialog
 			FileDialog fileDialog = new FileDialog(myContents.getShell());
 			// Set the text
 			fileDialog.setText("Select File");
 			// Set filter on .txt files
 			fileDialog.setFilterExtensions(new String[] { "*.cm" });
 			// Put in a readable name for the filter
 			fileDialog.setFilterNames(new String[] { "CM Files(*.cm)" });
 			// Open Dialog and save result of selection
 			String selected = fileDialog.open();
 
 			System.out.println(selected);
 
 			Graph g;
 			try {
 				g = CmFilesOperations.getCMGraphML(selected);
 				GraphManager.getInstance().addGraph(g);
 				CmFilesOperations.writeCMGraphMLFile(g,
 						selected.substring(0, selected.length() - 3));
 			} catch (Exception e) {
 				e.printStackTrace();
 				ProblemManager.reportException(e);
 			}
 		}
 
 		private String[] getDirectoryFiles(String cM_PATH) {
 			return null;
 		}
 	}
 
 	/**
 	 * This action retrieves the coverage information from Eclemma to build the SunBurst. 
 	 */
 	protected class UpdateViewWithCoverageSessionAction extends Action {
 		public UpdateViewWithCoverageSessionAction() {
 			setText("update View With Coverage Session");
 			setToolTipText("update View With Coverage Session  tooltip");
 			setImageDescriptor(Tabuleta.imageDescriptorFromPlugin(
 					Tabuleta.ID_PLUGIN, "icons/refresh.gif"));
 		}
 
 		public void run() {
 			boolean thereIsActiveCoverageSession = false;
 
 			ICoverageSession activeSession = CoverageTools.getSessionManager()
 					.getActiveSession();
 			thereIsActiveCoverageSession = activeSession == null ? false : true;
 
 			HashSet<IPackageFragmentRoot> escopo = null;
 
 			// se existe uma sessão ativa
 			if (thereIsActiveCoverageSession) {
 				try {
 					escopo = (HashSet<IPackageFragmentRoot>) ScopeUtils
 							.filterJREEntries(activeSession.getScope());
 				} catch (JavaModelException e) {
 					e.printStackTrace();
 				}
 
 				Graph g = new Graph();
 
 				g.addColumn("id", String.class);
 				g.addColumn("name", String.class);
 				g.addColumn("degree", String.class);
 				g.addColumn("type", String.class);
 
 				Object[] array = escopo.toArray();
 				IJavaProject project = null;
 				if (array[0] instanceof IPackageFragmentRoot)
 					project = ((IPackageFragmentRoot) array[0])
 							.getJavaProject();
 
 				Node root = g.addNode();
 				root.set("name", project.getElementName());
 				root.set("type", "Project");
 				root.set("degree", "100");
 				root.set("id", "project-" + project.getElementName());
 
 				IPackageFragment[] fragments = null;
 				try {
 					fragments = project.getPackageFragments();
 				} catch (JavaModelException e) {
 					e.printStackTrace();
 				}
 
 				for (IPackageFragment fragment : fragments) {
 					try {
 						if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
 							ICoverageNode node = (ICoverageNode) fragment.getAdapter(ICoverageNode.class);
 							if (node == null) {
 								continue;
 							}
 							double ratio = node.getLineCounter().getCoveredRatio();
 							Node pfNode = addCoverageNodeToGraph(g, node, root, "PackageFragment", String.valueOf(ratio));
 							printICompilationUnitInfo(fragment, g, pfNode);
 						}
 					} catch (JavaModelException e) {
 						e.printStackTrace();
 					}
 				}
 
 				GraphManager.getInstance().addGraph(g);
 			} else {
 				showMessage("You must run test suite with EclEmma coverage mode first.");
 			}
 		}
 
 		
 		private void printICompilationUnitInfo(IPackageFragment mypackage, Graph g, Node root)
 				throws JavaModelException {
 			for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
 				printCompilationUnitDetails(unit, g, root);
 			}
 		}
 		
 		private void printCompilationUnitDetails(ICompilationUnit unit, Graph g, Node root)
 				throws JavaModelException {
 			
 			ICoverageNode node = (ICoverageNode) unit.getAdapter(ICoverageNode.class);
 			
 			if (node == null) {
 				return;
 			}
 			double ratio = node.getLineCounter().getCoveredRatio();
 			Node cuNode = addCoverageNodeToGraph(g, node, root, "CompilationUnit", String.valueOf(ratio));
 			
 			printIMethods(unit,g,cuNode);
 		}
 		
 		private void printIMethods(ICompilationUnit unit, Graph g, Node root) throws JavaModelException {
 			IType[] allTypes = unit.getAllTypes();
 			for (IType type : allTypes) {
 				printIMethodDetails(type, g, root);
 			}
 		}
 		
 		private void printIMethodDetails(IType type, Graph g, Node root) throws JavaModelException {
 			IMethod[] methods = type.getMethods();
 			for (IMethod method : methods) {
 				
 				ICoverageNode node;
 				node = (ICoverageNode) method.getAdapter(ICoverageNode.class);
 				if (node == null) {
 					continue;
 				}
 				double ratio = node.getLineCounter().getCoveredRatio();			
 				addCoverageNodeToGraph(g, node, root, "Method", String.valueOf(ratio));
 			}
 		}
 		
 		private Node addCoverageNodeToGraph(Graph g, ICoverageNode node,
 				Node parent, String type, String degree) {
 			
 			Node child = g.addNode();
 			child.set("type", type);
 			child.set("degree", degree);
 			child.set("name", node.getName());
 			child.set("id", type + node.getName());
 			
 			g.addEdge(parent, child);
 			
 			return child;
 		}
 	}
 }
