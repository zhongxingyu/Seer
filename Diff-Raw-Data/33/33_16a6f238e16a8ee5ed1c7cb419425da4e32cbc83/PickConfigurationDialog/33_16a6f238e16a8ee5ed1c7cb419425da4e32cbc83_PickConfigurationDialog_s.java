 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import javax.swing.table.*;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Collections;
 import java.util.EventObject;
 
 import confdb.data.*;
 import confdb.db.*;
 
 
 /**
  * PickConfigurationDialog
  * -----------------------
  * @author Philipp Schieferdecker
  *
  * Pick a configuration, e.g. to open, import, etc.
  */
 public class PickConfigurationDialog extends JDialog
 {
     //
     // member data
     //
     
     /** parent frame */
     private JFrame jFrame;
 
     /** configuration database */
     private ConfDB database;
     
     /** root directory of configurations */
     private Directory rootDir;
     
     /** selected configuration */
     private ConfigInfo configInfo = null;
     
     /** tree model */
     private DirectoryTreeModel treeModel;
     
     /** list model */
     private ConfigInfoListModel listModel;
     
     /** table model */
     private ConfigVersionTableModel tableModel;
     
     /** GUI components */
     private JTree         jTree;
     private JList         jList;
     private JTable        jTable;
     
     private JSplitPane    jSplitPane        = new JSplitPane();
     private JPanel        jPanelTree        = new JPanel();
     private JTextField    jTextFieldFilter  = new JTextField();
     private JComboBox     jComboBoxRelease  = new JComboBox();
     private JScrollPane   jScrollPaneTree   = new JScrollPane();
     private JToggleButton jToggleButtonView = new JToggleButton();
     private JButton       jButtonOk         = new JButton();
     private JButton       jButtonCancel     = new JButton();
     private JScrollPane   jScrollPaneTable  = new JScrollPane();
 
     /** allow user to unlock his own configurations? */
     private boolean allowUnlocking = false;
     
 
     //
     // construction
     //
 
     /** standard constructor */
     public PickConfigurationDialog(JFrame jFrame,String title,ConfDB database)
     {
 	super(jFrame,true);
 	this.jFrame = jFrame;
 	this.database = database;
 	setTitle(title);
 	
 	// initialize tree
 	try {
 	    rootDir   = database.loadConfigurationTree();
 	    treeModel = new DirectoryTreeModel(rootDir);
 	} catch (DatabaseException e) {}
 	jTree = new JTree(treeModel) {
 		public String getToolTipText(MouseEvent evt) {
 		    return jTreeGetToolTipText(evt);
 		}
 	    };
 	jTree.setToolTipText("");
 	jTree.setEditable(false);
 	
 	DefaultTreeCellRenderer treeRenderer = new DirectoryTreeCellRenderer();
 	jTree.setCellRenderer((DefaultTreeCellRenderer)treeRenderer);
 	jTree.setCellEditor(new DirectoryTreeCellEditor(jTree,treeRenderer));
 	
 	// initialize list
 	listModel = new ConfigInfoListModel(rootDir);
 	jList = new JList(listModel) {
 		public String getToolTipText(MouseEvent evt) {
 		    return jListGetToolTipText(evt);
 		}	
 	    };
 	jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	jList.setCellRenderer(new ListCellRenderer() {
 		Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
 		JLabel label = new JLabel();
 		public Component getListCellRendererComponent(JList list,
 							      Object value,
 							      int index,
 							      boolean isSelected,
 							      boolean cellHasFocus)
 		{
 		    ConfigInfo ci = (ConfigInfo)value;
 		    String text = ci.parentDir().name()+"/"+ci.name();
 		    label.setText(text);
 		    label.setOpaque(true);
 		    label.setBackground(isSelected ?
 					list.getSelectionBackground() :
 					list.getBackground());
 		    label.setForeground(isSelected ?
 					list.getSelectionForeground() :
 					list.getForeground());
 		    label.setBorder(isSelected ?
 				    UIManager.getBorder
 				    ("List.focusCellHighlightBorder") :
 				    noFocusBorder);
 		    if (ci.isLocked()) label.setForeground(Color.red);
 		    return label;
 		}
 	    });
 	
 	// initialize table
 	tableModel = new ConfigVersionTableModel();
 	jTable = new JTable(tableModel) {
 		public String getToolTipText(MouseEvent evt) {
 		    return jTableGetToolTipText(evt);
 		}	
 	    };
 	jTable.setToolTipText("");
 	jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	jTable.setShowGrid(false);
 	jTable.getTableHeader().setReorderingAllowed(false);
 	jTable.getColumnModel().getColumn(0).setPreferredWidth(50);
 	jTable.getColumnModel().getColumn(1).setPreferredWidth(150);
 	jTable.getColumnModel().getColumn(2).setPreferredWidth(60);
 	jTable.getColumnModel().getColumn(3).setPreferredWidth(120);
 	
 	DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();
 	tableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
 	jTable.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
 	jTable.getColumnModel().getColumn(1).setCellRenderer(tableRenderer);
 	jTable.getColumnModel().getColumn(2).setCellRenderer(tableRenderer);
 	jTable.getColumnModel().getColumn(3).setCellRenderer(tableRenderer);
 
 
 	// initialize release combobox
 	try {
 	    jComboBoxRelease = new JComboBox(database.getReleaseTags());
 	}
 	catch (DatabaseException e) {}
 	
 	// register listener callbacks
 	jTree.addTreeSelectionListener(new TreeSelectionListener() {
 		public void valueChanged(TreeSelectionEvent e) {
 		    jTreeValueChanged(e);
 		}
 	    });
 	jTree.addMouseListener(new MouseAdapter() {
 		public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
 		public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
 		public void maybeShowPopup(MouseEvent e) {
 		    if (e.isPopupTrigger()) jTreeMaybeShowPopup(e);
 		}
 	    });
 	jList.addListSelectionListener(new ListSelectionListener() {
 		public void valueChanged(ListSelectionEvent e) {
 		    jListValueChanged(e);
 		}
 	    });
 	jList.addMouseListener(new MouseAdapter() {
 		public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
 		public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
 		public void maybeShowPopup(MouseEvent e) {
 		    if (e.isPopupTrigger()) jListMaybeShowPopup(e);
 		}
 	    });
 	jTable.getSelectionModel()
 	    .addListSelectionListener(new ListSelectionListener() {
 		    public void valueChanged(ListSelectionEvent e) {
 			jTableValueChanged(e);
 		    }
 		});
 	jTextFieldFilter.getDocument().addDocumentListener(new DocumentListener() {
 		public void insertUpdate(DocumentEvent e) {
 		    jTextFieldFilterInsertUpdate(e);
 		}
 		public void removeUpdate(DocumentEvent e) {
 		    jTextFieldFilterRemoveUpdate(e);
 		}
 		public void changedUpdate(DocumentEvent e) {}
 	    });
 	jComboBoxRelease.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jComboBoxReleaseActionPerformed(e);
 		}
 	    });
 	jToggleButtonView.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jToggleButtonViewActionPerformed(e);
 		}
 	    });
 	jButtonOk.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonOkActionPerformed(e);
 		}
 	    });
 	jButtonCancel.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonCancelActionPerformed(e);
 		}
 	    });
 	
 	setContentPane(initComponents());
 	jTable.getParent().setBackground(new Color(255,255,255));	
 	jToggleButtonView.setIcon(new ImageIcon(getClass().
 						getResource("/ListViewIcon.png")));
     }
     
     
     //
     // member functions
     //
     
     /** get the chosen configuration */
     public ConfigInfo configInfo() { return configInfo; }
 
     /** indicate if a valid choice was made */
     public boolean validChoice() { return (configInfo!=null); }
     
     /** fix the release */
     public boolean fixReleaseTag(String releaseTag)
     {
 	if (releaseTag.length()==0) return false;
 	DefaultComboBoxModel cbm=(DefaultComboBoxModel)jComboBoxRelease.getModel();
 	for (int i=0;i<cbm.getSize();i++) {
 	    if (cbm.getElementAt(i).toString().equals(releaseTag)) {
 		cbm.setSelectedItem(releaseTag);
 		jComboBoxRelease.setEnabled(false);
 		updateTreeAndList();
 		return true;
 	    }
 	}
 	return false;
     }
 
     /** allow unlocking */
     public void allowUnlocking()
     {
 	allowUnlocking = true;
     }
 
     //
     // private member functions
     //
 
     /** jTree: JTree::getToolTipText() */
     private String jTreeGetToolTipText(MouseEvent evt)
     {
 	String text = "";
 	if (jTree.getRowForLocation(evt.getX(),evt.getY()) == -1) return text;
 	TreePath tp = jTree.getPathForLocation(evt.getX(), evt.getY());
 	Object selectedNode = tp.getLastPathComponent();
 	if (selectedNode instanceof ConfigInfo) {
 	    ConfigInfo info = (ConfigInfo)selectedNode;
 	    if (info.isLocked()) {
 		text = "locked by user '" + info.lockedByUser() + "'";
 	    }
 	}
 	return text;
     }
     
     /** jList: JList::getToolTipText() */
     private String jListGetToolTipText(MouseEvent evt)
     {
 	String text = "";
 	int    index = jList.locationToIndex(new Point(evt.getX(),evt.getY()));
 	if (index == -1) return text;
 	ConfigInfo info = (ConfigInfo)listModel.getElementAt(index);
 	if (info.isLocked()) text = "locked by user '" + info.lockedByUser() + "'";
 	return text;
     }
     
     /** jTable: JTable::getToolTipText() */
     private String jTableGetToolTipText(MouseEvent evt)
     {
 	String text = "";
 	int    row = jTable.rowAtPoint(new Point(evt.getX(),evt.getY()));
 	if (row == -1) return text;
 	ConfigVersion configVersion = configInfo.version(row);
 	text =
 	    "id:" + configVersion.dbId() + "   " +
 	    "\"" + configVersion.comment() + "\"";
 	return text;
     }
     
     /** listener callbacks */
     private void jTreeValueChanged(TreeSelectionEvent e)
     {
 	String releaseTag = (String)jComboBoxRelease.getSelectedItem();
 	Object o = jTree.getLastSelectedPathComponent();
 	if (o instanceof Directory) {
 	    Directory d = (Directory)o;
 	    
 	    // table
 	    if (configInfo!=null) {
 		configInfo.setVersionIndex(0);
 		configInfo = null;
 	    }
 	    jButtonOk.setEnabled(false);
 	    tableModel.setConfigInfo(configInfo);
 	}
 	else if (o instanceof ConfigInfo) {
 	    configInfo = (ConfigInfo)o;
 	    
 	    tableModel.setConfigInfo(configInfo);
 	    
 	    int selectedIndex = -1;
 	    int iVersion=0;
 	    while (selectedIndex<0&&iVersion<configInfo.versionCount()) {
 		if (releaseTag.equals(new String())||
 		    releaseTag.equals(configInfo.version(iVersion).releaseTag()))
 		    selectedIndex=iVersion;
 		++iVersion;
 	    }
 	    if (selectedIndex<0) {
 		configInfo = null;
 		jButtonOk.setEnabled(false);
 		return;
 	    }
 	    
 	    jTable.getSelectionModel().setSelectionInterval(selectedIndex,
 							    selectedIndex);
 	    jButtonOk.setEnabled(true);
 
 	}
     }
     private void jTreeMaybeShowPopup(MouseEvent e)
     {
 	TreePath treePath = jTree.getPathForLocation(e.getX(),e.getY());
 	if (treePath==null) return; jTree.setSelectionPath(treePath);
 	if (configInfo==null) return;
 	
 	String userName = System.getProperty("user.name");
 	ArrayList<String> admins = new ArrayList<String>();
 	admins.add("gruen");
 	admins.add("martin");
 	admins.add("fwyzard");
 	admins.add("schiefer");
 	
 	if (allowUnlocking&&
 	    (userName.equalsIgnoreCase(configInfo.lockedByUser())||
 	     admins.contains(userName))) showPopup(e);
     }
     private void jListValueChanged(ListSelectionEvent e)
     {
 	if (e.getValueIsAdjusting()) return;
 
 	String releaseTag = (String)jComboBoxRelease.getSelectedItem();
 	configInfo = (ConfigInfo)jList.getSelectedValue();
 	if (configInfo==null) {
 	    tableModel.setConfigInfo(null);
 	    jButtonOk.setEnabled(false);
 	    return;
 	}
 	
 	tableModel.setConfigInfo(configInfo);
 	
 	int selectedIndex = -1;
 	int iVersion=0;
	while (selectedIndex<0&&iVersion<configInfo.versionCount()) {
 	    if (releaseTag.equals(new String())||
 		releaseTag.equals(configInfo.version(iVersion).releaseTag()))
 		selectedIndex=iVersion;
 	    ++iVersion;
 	}
 	if (selectedIndex<0) {
 	    configInfo = null;
 	    jButtonOk.setEnabled(false);
 	    return;
 	}
 	
 	jTable.getSelectionModel().setSelectionInterval(selectedIndex,
 							selectedIndex);
 	jButtonOk.setEnabled(true);
     }
     private void jListMaybeShowPopup(MouseEvent e)
     {
 	int index = jList.locationToIndex(new Point(e.getX(),e.getY()));
 	jList.setSelectedIndex(index);
 	if (configInfo==null) return;
 	
 	if (allowUnlocking&&System.getProperty("user.name")
 	    .equalsIgnoreCase(configInfo.lockedByUser())) showPopup(e);
     }
     private void jTableValueChanged(ListSelectionEvent e)
     {
 	if (e.getValueIsAdjusting()||configInfo==null) return;
 	String releaseTag = (String)jComboBoxRelease.getSelectedItem();
 	ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 	if (!lsm.isSelectionEmpty()) {
 	    int selectedIndex = lsm.getMinSelectionIndex();
 	    configInfo.setVersionIndex(releaseTag,selectedIndex);
 	    //if (releaseTag.equals(new String())||
 	    //	releaseTag.equals(configInfo.version(selectedIndex).releaseTag()))
 	    //	configInfo.setVersionIndex(selectedIndex);
 	}
     }
     private void jTextFieldFilterInsertUpdate(DocumentEvent e)
     {
 	updateTreeAndList();
     }
     private void jTextFieldFilterRemoveUpdate(DocumentEvent e)
     {
 	updateTreeAndList();
     }
     private void jComboBoxReleaseActionPerformed(ActionEvent e)
     {
 	updateTreeAndList();
     }
     private void jToggleButtonViewActionPerformed(ActionEvent e)
     {
 	AbstractButton b = (AbstractButton)e.getSource();
 
 	// list view
 	if (b.isSelected()) {
 	    jScrollPaneTree.setViewportView(jList);
 	    DefaultListSelectionModel lsm =
 		(DefaultListSelectionModel)jList.getSelectionModel();
 	    if (configInfo!=null) {
 		int selectedIndex = listModel.indexOf(configInfo);
 		lsm.setSelectionInterval(selectedIndex,selectedIndex);
 	    }
 	    else {
 		lsm.clearSelection();
 	    }
 	}
 	// tree view
 	else {
 	    jScrollPaneTree.setViewportView(jTree);
 	    if (configInfo!=null) {
 		TreePath tp = new TreePath(treeModel.getPathToRoot(configInfo));
 		jTree.expandPath(tp);
 		jTree.setSelectionPath(tp);
 	    }
 	    else {
 		jTree.clearSelection();
 	    }
 	}
     }
     private void jButtonOkActionPerformed(ActionEvent e)
     {
 	setVisible(false);
     }
     private void jButtonCancelActionPerformed(ActionEvent e)
     {
 	configInfo = null;
 	setVisible(false);
     }
     
     private void updateTreeAndList()
     {
 	String    filterString  = jTextFieldFilter.getText();
 	String    releaseTag    = (String)jComboBoxRelease.getSelectedItem();
 	Directory filteredRootDir=rootDir.filter(filterString,releaseTag);
 
 	treeModel.setRootDir(filteredRootDir);
 	if (filterString.length()>0) expandTree();
 	listModel.setDirectory(filteredRootDir);	
 	tableModel.fixReleaseTag(releaseTag);
     }
     
     /** expand the whole tree */
     private void expandTree()
     {
 	int row = 0;
 	while (row<jTree.getRowCount()) {
 	    jTree.expandRow(row);
 	    row++;
 	}
     }
     
     /** show 'unlock' popup, both in jTree and jList */
     private void showPopup(MouseEvent e) {
 	JPopupMenu popup    = new JPopupMenu();
 	JMenuItem  menuItem = new JMenuItem("Unlock");
 	menuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    try {
 			Configuration c=new Configuration(configInfo,null);
 			database.unlockConfiguration(c);
 			configInfo.unlock();
 			treeModel.nodeChanged(configInfo);
 			listModel.elementChanged(configInfo);
 		    }
 		    catch (DatabaseException ex) {
 			String errMsg =
 			    "Failed to unlock configuration: "+ex.getMessage();
 			JOptionPane.showMessageDialog(jFrame,errMsg,
 						      "Failed to unlock",
 						      JOptionPane.ERROR_MESSAGE);
 			ex.printStackTrace();
 		    }
 		}
 	    });
 	popup.add(menuItem);
 	popup.show(e.getComponent(),e.getX(),e.getY());
     }
 
     /** initComponents(), generated with netbeans */
     private JPanel initComponents()
     {
 	JPanel jPanel = new JPanel();
 	
         JLabel jLabelFilter = new JLabel();
         JLabel jLabelRelease = new JLabel();
 	
         jSplitPane.setDividerLocation(300);
         jSplitPane.setResizeWeight(0.5);
 
         jLabelFilter.setText("Filter:");
         jLabelRelease.setText("Release:");
 	
         jComboBoxRelease.setBackground(new java.awt.Color(255, 255, 255));
         
 	jScrollPaneTree.setViewportView(jTree);
 
         jButtonOk.setText("OK");
         jButtonCancel.setText("Cancel");
 	
         org.jdesktop.layout.GroupLayout jPanelTreeLayout = new org.jdesktop.layout.GroupLayout(jPanelTree);
         jPanelTree.setLayout(jPanelTreeLayout);
         jPanelTreeLayout.setHorizontalGroup(
 					    jPanelTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					    .add(jPanelTreeLayout.createSequentialGroup()
 						 .addContainerGap()
 						 .add(jPanelTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 						      .add(jLabelRelease)
 						      .add(jLabelFilter))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jPanelTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 						      .add(jComboBoxRelease, 0, 241, Short.MAX_VALUE)
 						      .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelTreeLayout.createSequentialGroup()
 							   .add(jTextFieldFilter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
 							   .add(18, 18, 18)
 							   .add(jToggleButtonView, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
 						 .addContainerGap())
 					    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelTreeLayout.createSequentialGroup()
 						 .add(jButtonOk, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
 						 .add(jButtonCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
 					    .add(jScrollPaneTree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
 					    );
         jPanelTreeLayout.setVerticalGroup(
 					  jPanelTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					  .add(jPanelTreeLayout.createSequentialGroup()
 					       .addContainerGap()
 					       .add(jPanelTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						    .add(jLabelFilter)
 						    .add(jToggleButtonView, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						    .add(jTextFieldFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 					       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 					       .add(jPanelTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						    .add(jComboBoxRelease, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						    .add(jLabelRelease))
 					       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 					       .add(jScrollPaneTree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
 					       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 					       .add(jPanelTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						    .add(jButtonOk)
 						    .add(jButtonCancel)))
 					  );
 	
         jPanelTreeLayout.linkSize(new java.awt.Component[] {jComboBoxRelease, jTextFieldFilter}, org.jdesktop.layout.GroupLayout.VERTICAL);
 	
         jPanelTreeLayout.linkSize(new java.awt.Component[] {jButtonOk, jToggleButtonView}, org.jdesktop.layout.GroupLayout.VERTICAL);
 	
         jSplitPane.setLeftComponent(jPanelTree);
         jScrollPaneTable.setViewportView(jTable);
         jSplitPane.setRightComponent(jScrollPaneTable);
 	
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
         jPanel.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 880, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
 				);
 	return jPanel;
     }
 
 }
 
