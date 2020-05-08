 package org.bh.plugin.gcc.branchspecific;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.dnd.DnDConstants;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTree;
 import javax.swing.UIManager;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.bh.data.DTOBranch;
 import org.bh.data.DTOBusinessData;
 import org.bh.data.DTOCompany;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.StringValue;
 import org.bh.gui.IBHComponent;
 import org.bh.gui.swing.BHContent;
 import org.bh.gui.swing.BHMenuBar;
 import org.bh.gui.swing.BHMenuItem;
 import org.bh.gui.swing.BHPopupFrame;
 import org.bh.gui.swing.comp.BHButton;
 import org.bh.gui.swing.comp.BHComboBox;
 import org.bh.gui.swing.comp.BHComboBox.Item;
 import org.bh.gui.swing.tree.BHTree;
 import org.bh.gui.swing.tree.BHTreeTransferHandler;
 import org.bh.platform.PlatformController;
 import org.bh.platform.PlatformKey;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 import org.jfree.util.Log;
 
 /**
  * Frame to maintain company data to calculate branch specific representative.
  * 
  * <p>
  * This class should be the base class for maintaining all the company data,
  * which is necessary to calculate the branch specific representative. This is
  * supposed to be the entry point to change and maintain company data.
  * 
  * @author Yannick Rödl
  * @version 1.0, 27.12.2011
  * 
  */
 public class MaintainCompanyDataFrame extends BHPopupFrame implements
 		ActionListener {
 	JPanel panev2 = null;
 	JTree tree = null;
 	JSplitPane panev3 = null;
 	JPanel content = null;
 	JSplitPane paneV = null;
 	static BHComboBox branchbox = null;
 	static DTOBusinessData myDTO = null;
 	static BHBusinessDataTreeNode root1 = null;
 	JScrollPane bhTreeScroller = null;
 	private JScrollPane resultForm;
 
 	public static BHComboBox getbranchbox() {
 		return branchbox;
 	}
 
 	public enum GUI_KEYS {
 		TITLE,BranchAdd;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	public enum MenuBar {
 		MENU_EXTRAS;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	/**
 	 * Generated <code>serialVersionUID</code>
 	 */
 	private static final long serialVersionUID = -6127674860072304710L;
 	
 	
 	public void buildTree() {
 		
 		root1 = null;
 		myDTO = PlatformController.getBusinessDataDTO();
 		root1 = new BHBusinessDataTreeNode(myDTO);
 		for (DTOBranch branches : myDTO.getChildren()) {
 			BHBusinessDataTreeNode j = new BHBusinessDataTreeNode(branches);
 			for (DTOCompany companies : branches.getChildren()) {
 				BHBusinessDataTreeNode g = new BHBusinessDataTreeNode(companies);
 				j.add(g);
 				for (DTOPeriod periods : companies.getChildren()) {
 					BHBusinessDataTreeNode h = new BHBusinessDataTreeNode(
 							periods);
 					g.add(h);
 				}
 			}
 			root1.add(j);
 		}
 		tree = null;
 		tree = new BHBDTree(root1);		
 		BHBDTreeModel treemodel = new BHBDTreeModel(root1);
 		treemodel.reload();		
 		tree.setModel(treemodel);
 		tree.setRootVisible(false);	
 		tree.repaint();	
 		bhTreeScroller = new JScrollPane(tree);
 		panev3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panev2,
 				bhTreeScroller);
 		panev3.setTopComponent(bhTreeScroller);
 		
 		
 	}
 	
 	public void clearTree() {
 		tree = new BHBDTree(new BHBusinessDataTreeNode(null));
 	}
 
 	public MaintainCompanyDataFrame() {
 		super();
 
 		Container desktop = getContentPane();
 		BHButton add = new BHButton(translator.translate(MaintainCompanyDataFrame.GUI_KEYS.BranchAdd));
 		content = new BHContent();
 		this.setAlwaysOnTop(false);
 		// build the tree
 	    buildTree();
 
 		tree.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent e) {
 				BHBusinessDataTreeNode node = (BHBusinessDataTreeNode) e
 						.getPath().getLastPathComponent();
 				System.out.println("You selected " + node);
 				/* if nothing is selected */
 				if (node.getUserObject() instanceof DTOBusinessData) {
 					content = new BHContent();
 					paneV.setTopComponent(content);
 					System.out.println("You have selected content");
 					content.revalidate();
 				} else if (node.getUserObject() instanceof DTOBranch) {
 					content = new BHContent();
 					paneV.setTopComponent(content);
 					System.out.println("You have selected Branch");
 					content.revalidate();
 				} else if (node.getUserObject() instanceof DTOCompany) {
 					System.out.println("You have selected Company");
 					content = new BHContent();
 					paneV.setTopComponent(content);
 				} else if (node.getUserObject() instanceof DTOPeriod) {
 					BHPeriodFrame p = new BHPeriodFrame((DTOPeriod) node
 							.getUserObject());
 					content = p.getFrame();
 					paneV.setTopComponent(content);
 
 				} else {
 					Log.debug("no valid class");
 				}
 			}
 		});
 
 		bhTreeScroller = new JScrollPane(tree);
 		panev2 = new JPanel();
 		panev2.setLayout(new BorderLayout());
 		branchbox = new BHComboBox(DTOScenario.Key.REPRESENTATIVE);
 		panev2.add(branchbox, BorderLayout.NORTH);
 		panev2.add(add, BorderLayout.SOUTH);
 
 		// fills the branch box
 		String[] myBranchString = new String[] { "D.35.2", "D.35.3", "D.35.1",
 				"E.36.0", "E.37.0", "E.38.3", "E.38.2", "E.38.1", "E.39.0",
 				"F.42.9", "F.42.1", "F.42.2", "F.43.9", "F.43.1", "F.43.2",
 				"F.43.3", "F.41.2", "F.41.1", "G.47.5", "G.47.4", "G.47.7",
 				"G.47.6", "G.47.9", "G.47.8", "G.47.1", "G.47.2", "G.47.3",
 				"G.45.2", "G.45.3", "G.45.4", "G.45.1", "G.46.4", "G.46.3",
 				"G.46.2", "G.46.1", "G.46.9", "G.46.7", "G.46.5", "G.46.6",
 				"A.01.1", "A.01.2", "A.01.3", "A.01.4", "A.01.5", "A.01.6",
 				"A.01.7", "A.02.4", "A.02.3", "A.02.2", "A.02.1", "A.03.1",
 				"A.03.2", "B.06.2", "B.06.1", "B.07.2", "B.07.1", "B.05.1",
 				"B.05.2", "B.08.1", "B.08.9", "B.09.9", "B.09.1", "C.28.4",
 				"C.28.3", "C.28.2", "C.28.1", "C.28.9", "C.27.1", "C.27.4",
 				"C.27.5", "C.27.2", "C.27.3", "C.27.9", "C.29.1", "C.29.2",
 				"C.29.3", "C.23.1", "C.23.7", "C.23.6", "C.23.9", "C.23.3",
 				"C.23.2", "C.23.5", "C.23.4", "C.24.1", "C.24.2", "C.24.3",
 				"C.24.4", "C.24.5", "C.25.9", "C.25.5", "C.25.4", "C.25.7",
 				"C.25.6", "C.25.1", "C.25.3", "C.25.2", "C.26.7", "C.26.8",
 				"C.26.5", "C.26.6", "C.26.3", "C.26.4", "C.26.1", "C.26.2",
 				"C.20.6", "C.20.5", "C.20.1", "C.20.2", "C.20.3", "C.20.4",
 				"C.21.2", "C.21.1", "C.22.1", "C.22.2", "C.32.9", "C.32.5",
 				"C.32.4", "C.32.2", "C.32.3", "C.32.1", "C.33.2", "C.33.1",
 				"C.30.4", "C.30.3", "C.30.2", "C.30.1", "C.30.9", "C.31.0",
 				"C.19.2", "C.19.1", "C.18.1", "C.18.2", "C.17.1", "C.17.2",
 				"C.16.1", "C.16.2", "C.10.8", "C.10.9", "C.10.4", "C.10.5",
 				"C.10.6", "C.10.7", "C.10.1", "C.10.3", "C.10.2", "C.11.0",
 				"C.14.3", "C.14.2", "C.14.1", "C.15.2", "C.15.1", "C.12.0",
 				"C.13.9", "C.13.1", "C.13.2", "C.13.3", "L.68.2", "L.68.3",
 				"L.68.1", "M.75.0", "M.74.1", "M.74.3", "M.74.2", "M.74.9",
 				"M.73.1", "M.73.2", "M.72.2", "M.72.1", "M.71.2", "M.71.1",
 				"M.70.2", "M.70.1", "M.69.1", "M.69.2", "N.80.2", "N.80.3",
 				"N.80.1", "N.81.1", "N.81.2", "N.81.3", "N.82.3", "N.82.2",
 				"N.82.1", "N.82.9", "N.78.3", "N.78.1", "N.78.2", "N.77.4",
 				"N.77.3", "N.77.2", "N.77.1", "N.79.1", "N.79.9", "O.84.1",
 				"O.84.2", "O.84.3", "H.53.2", "H.53.1", "H.50.4", "H.50.3",
 				"H.50.2", "H.50.1", "H.52.1", "H.52.2", "H.51.2", "H.51.1",
 				"H.49.1", "H.49.2", "H.49.5", "H.49.4", "H.49.3", "I.56.2",
 				"I.56.3", "I.56.1", "I.55.9", "I.55.3", "I.55.1", "I.55.2",
 				"J.58.1", "J.58.2", "J.59.2", "J.59.1", "J.61.3", "J.61.1",
 				"J.61.2", "J.61.9", "J.60.2", "J.60.1", "J.62.0", "J.63.9",
 				"J.63.1", "K.65.1", "K.65.2", "K.65.3", "K.66.3", "K.66.2",
 				"K.66.1", "K.64.1", "K.64.2", "K.64.3", "K.64.9", "U.99.0",
 				"T.97.0", "T.98.1", "T.98.2", "Q.86.1", "Q.86.2", "Q.86.9",
 				"Q.88.1", "Q.88.9", "Q.87.1", "Q.87.3", "Q.87.2", "Q.87.9",
 				"P.85.2", "P.85.1", "P.85.5", "P.85.6", "P.85.3", "P.85.4",
 				"S.94.9", "S.94.1", "S.94.2", "S.95.2", "S.95.1", "S.96.0",
 				"R.90.0", "R.93.2", "R.93.1", "R.92.0", "R.91.0" };
 		
 		LinkedList<String> c = new LinkedList<String>();
 
 		// Get current BranchList
 		List<DTOBranch> branchList = myDTO.getChildren();
 		boolean addBranch;
 
 		for (String currBranchString : myBranchString) {
 
 			addBranch = true;
 			Iterator<DTOBranch> itr = branchList.iterator();
 			while (itr.hasNext()) {
 				DTOBranch currBranch = itr.next();
 				String key = currBranch.get(
 						DTOBranch.Key.BRANCH_KEY_MAIN_CATEGORY).toString()
 						+ "."
 						+ currBranch.get(DTOBranch.Key.BRANCH_KEY_MID_CATEGORY)
 								.toString()
 						+ "."
 						+ currBranch.get(DTOBranch.Key.BRANCH_KEY_SUB_CATEGORY)
 								.toString();
 
 				if (key.equals(currBranchString))
 					addBranch = false;
 
 			}
 			if (addBranch)
 				c.add(currBranchString);
 		}
 		// add relevant Branch data to ComboBox
 		for (String currRelBranchString : c) {
 			branchbox.addItem(new Item(currRelBranchString, null));
 		}
 
 		// Add Action Listener
 		add.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				// add branch to tree
 				BHComboBox bb = branchbox;
 				String valueComboBox = bb.getSelectedItem().toString();
 				String splittedValueDD = valueComboBox.split(":")[0];
 				String[] splittedValue = splittedValueDD.split("\\.");		
 
 				// create new Branch
 				DTOBranch myNewBranch = new DTOBranch();
 				myNewBranch.put(DTOBranch.Key.BRANCH_KEY_MAIN_CATEGORY,
 						new StringValue(splittedValue[0]));
 				myNewBranch.put(DTOBranch.Key.BRANCH_KEY_MID_CATEGORY,
 						new StringValue(splittedValue[1]));
 				myNewBranch.put(DTOBranch.Key.BRANCH_KEY_SUB_CATEGORY,
 						new StringValue(splittedValue[2]));
 
 				// add Branch to BusinessData DOM
 				myDTO.addChild(myNewBranch);
 				
 				DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 				model.insertNodeInto(new BHBusinessDataTreeNode(myNewBranch), root1, root1.getChildCount());
 				
 				int index = branchbox.getSelectedIndex();
				try{
 				branchbox.setSelectedIndex(index+1);
				}catch (Exception exp){
					branchbox.setSelectedIndex(0);
				}
 				branchbox.removeItemAt(index);
 
 
 			}
 
 		});
 
 		 panev3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panev2,
 				bhTreeScroller);
 		paneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, content, resultForm);
 		paneV.setOneTouchExpandable(true);
 
 		panev3.setSize(200, 1000);
 
 		// Create the horizontal split pane and put the treeBar and the content
 		// in it.
 		JSplitPane paneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				bhTreeScroller, paneV);
 
 		paneH.setOneTouchExpandable(true);
 
 		Services.initNumberFormats();
 
 
 		bhTreeScroller.setMinimumSize(new Dimension(250 + UIManager
 				.getInt("JTree.minimumWidth"),
 				bhTreeScroller.getMinimumSize().height));
 
 		desktop.add(panev2, BorderLayout.NORTH);
 		desktop.add(paneH, BorderLayout.CENTER);
 		this.setSize(1120, 720);
 	}
 
 	@Override
 	public void setAdditionalMenuEntriesInMainFrame(BHMenuBar menuBar) {
 		ITranslator translator = BHTranslator.getInstance();
 
 		JMenu extras = new JMenu(
 				translator
 						.translate(MaintainCompanyDataFrame.MenuBar.MENU_EXTRAS
 								.toString()));
 		extras.setMnemonic(translator.translate(
 				MaintainCompanyDataFrame.MenuBar.MENU_EXTRAS.toString(),
 				ITranslator.MNEMONIC).charAt(0));
 
 		// All JMenuItems for BSR Company Data
 
 		BHMenuItem maintainCompanyData = new BHMenuItem(
 				PlatformKey.MAINTAIN_COMPANY_DATA, 0);
 		maintainCompanyData.addActionListener(this);
 
 		BHMenuItem exportCompanyData = new BHMenuItem(
 				PlatformKey.EXPORT_COMPANY_DATA, 0);
 		exportCompanyData.addActionListener(this);
 
 		BHMenuItem importCompanyData = new BHMenuItem(
 				PlatformKey.IMPORT_COMPANY_DATA, 0);
 		importCompanyData.addActionListener(this);
 
 		extras.add(maintainCompanyData);
 		extras.addSeparator();
 		extras.add(exportCompanyData);
 		extras.add(importCompanyData);
 
 		menuBar.add(extras);
 	}
 
 	public String getUniqueId() {
 		return BHPopupFrame.ID.MAINTAIN_COMPANIES.toString();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		IBHComponent comp = (IBHComponent) arg0.getSource();
 		if (comp.getKey().equals("MmaintainCompData")) {
 			Logger.getLogger(MaintainCompanyDataFrame.class).info(
 					"Popup should be loaded now.");
 			this.setVisible(true);
 
 		} else if (comp.getKey().equals("MexportCompanyData")) {
 			BSRManualPersistance.saveBranches();
 
 		} else if (comp.getKey().equals("MimportCompanyData")) {
 			BSRManualPersistance.loadBranches();
 
 		} else {
 			System.out.println("Something definitly went wrong");
 		}
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		// TODO We have to do something with the data here.
 	}
 
 	/* Specified by interface/super class. */
 	@Override
 	public String getTitleKey() {
 		return MaintainCompanyDataFrame.GUI_KEYS.TITLE.toString();
 	}
 }
