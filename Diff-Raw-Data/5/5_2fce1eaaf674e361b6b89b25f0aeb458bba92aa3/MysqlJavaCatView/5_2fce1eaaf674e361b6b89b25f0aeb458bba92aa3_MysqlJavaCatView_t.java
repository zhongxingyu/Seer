 package mysqljavacat;
 
 import com.mysql.jdbc.StringUtils;
 import java.awt.Component;
 import mysqljavacat.dialogs.MysqlJavaCatAboutBox;
 import mysqljavacat.databaseobjects.DatabaseObj;
 import mysqljavacat.databaseobjects.TableObj;
 import mysqljavacat.renders.ColoredTableCellRenderer;
 import mysqljavacat.renders.DatabaseTreeCellRender;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.JTree;
 import javax.swing.MenuElement;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import jsyntaxpane.DefaultSyntaxKit;
 import mysqljavacat.dialogs.ExportToExcel;
 import mysqljavacat.dialogs.OpenDialog;
 import mysqljavacat.dialogs.SaveDialod;
 import org.jdesktop.application.Task;
 
 /**
  * The application's main frame.
  */
 public class MysqlJavaCatView extends FrameView {
     private MysqlJavaCatApp application = MysqlJavaCatApp.getApplication();
     private DatabaseObj selected_db;
     private HashMap<String,DatabaseObj> databases = new HashMap<String,DatabaseObj>();
     private SqlTabbedPane tabs;
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
     private Task queryTask;
 
     private JDialog aboutBox;
     private JDialog confDialog;
 
 
     public SqlTabbedPane getTabsMain(){
         return tabs;
     }
 
     public DatabaseObj getSelectedDb(){
         return selected_db;
     }    
     public HashMap<String,DatabaseObj> getDbMap(){
         return databases;
     }
     public JTable getResultTable(){
         return resultTable;
     }
 
     public JLabel getRowCountLabel(){
         return rowCount;
     }
     public JLabel getResTimeLabel(){
         return requestTime;
     }
 
     public MysqlJavaCatView(SingleFrameApplication app) {
         super(app);        
         DefaultSyntaxKit.initKit();
         initComponents();
         jTabbedPane1.setSelectedIndex(0);
         getFrame().setTitle("Mysql Java Based Object Browser");
         getFrame().setIconImage(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/databases.png")).getImage());
         tabs = new SqlTabbedPane();
         tabs.setFocusable(false);
         tabs.setName("sqlTabs");
         horisontalSplit.setLeftComponent(tabs);
         KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
             public boolean dispatchKeyEvent(KeyEvent e) {
                 if(e.getID() == KeyEvent.KEY_PRESSED){
                     if(e.getKeyCode() == 84 && e.isControlDown()) {
                         tabs.createTab();
                     }else if(e.getKeyCode() == 87 && e.isControlDown()){
                         tabs.getSelectedtab().close();
                     }else if(e.getKeyCode() == 120){
                         if(RunButton.isEnabled())
                             RunButtonActionPerformed(null);                    
                     }else if(e.getKeyCode() == 27){
                         for(SqlTab tab : tabs.getSqlTabs()){
                             tab.getComboDialog().hideVithPrepared();
                         }
                         return true;
                     }
                     return false;
                 }
                 return false;
             }
         });
 
         resultTable.setDragEnabled(false);
         resultTable.setDefaultRenderer(Object.class,new ColoredTableCellRenderer());
 
         DatabaseTreeCellRender cell = new DatabaseTreeCellRender();
         DatabaseTree.setModel(null);
         DatabaseTree.setCellRenderer(cell);
         DatabaseTree.setRootVisible(false);
         DatabaseTree.setShowsRootHandles(true);        
         propText.setContentType("text/sql");
         propText.setEditable(false);
         propText.setEnabled(true);
         MouseAdapter table_ma = new MouseAdapter() {
             private String getValue(JTable table,int row,int col){
                 if(table.getValueAt(row, col) == null){
                     return "";
                 }else
                     return table.getValueAt(row, col).toString();
             }
             private void myPopupEvent(MouseEvent e) {
                     int x = e.getX();
                     int y = e.getY();
                     final JTable table = (JTable) e.getSource();
                     final int row = table.rowAtPoint(new Point(x, y));
                     final int col = table.columnAtPoint(new Point(x, y));
                     if(table.getSelectedRowCount() <= 1){
                         DefaultListSelectionModel sel_model = new DefaultListSelectionModel();
                         sel_model.setSelectionInterval(row, row);
                         table.setSelectionModel(sel_model);
                     }
                     JPopupMenu popup = new JPopupMenu();
                     JMenuItem menu_item = new JMenuItem();
                     menu_item.setText("Copy cell data to clipboard");
                     menu_item.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/page_copy.png")));
                     menu_item.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                             StringSelection stringSelection = new StringSelection(getValue(table,row, col));
                             clipboard.setContents(stringSelection, null);
                         }
                     });
                     popup.add(menu_item);
                     JMenuItem menu_item1 = new JMenuItem();
                     menu_item1.setText("Copy all data to clipboard");
                     menu_item1.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/page_copy.png")));
                     menu_item1.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                             StringBuilder out = new StringBuilder();
                             boolean first_row = true;
                             for(int i = 0;i < table.getRowCount();i = i + 1){
                                 StringBuffer row = new StringBuffer();
                                 boolean first_col = true;
                                 for(int k = 0; k < table.getColumnCount();k = k + 1){
                                     if(first_col){
                                         row.append(getValue(table,i, k));
                                         first_col = false;
                                     }else{
                                         row.append("\t");
                                         row.append(getValue(table,i, k));
                                     }
                                 }
                                 if(first_row){
                                     out.append(row);
                                     first_row = false;
                                 }else{
                                     out.append("\n");
                                     out.append(row);
                                 }
                             }
                             StringSelection stringSelection = new StringSelection(out.toString());
                             clipboard.setContents(stringSelection, null);
                         }
                     });
                     popup.add(menu_item1);
 
                     JMenuItem menu_item2 = new JMenuItem();
                     menu_item2.setText("Copy column data to clipboard");
                     menu_item2.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/page_copy.png")));
                     menu_item2.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                             StringBuilder out = new StringBuilder();
                             boolean first_row = true;
                             for(int i = 0;i < table.getRowCount();i = i + 1){                               
                                 if(first_row){
                                     out.append(getValue(table, i, col));
                                     first_row = false;
                                 }else{
                                     out.append("\n");
                                     out.append(getValue(table, i, col));
                                 }
                             }
                             StringSelection stringSelection = new StringSelection(out.toString());
                             clipboard.setContents(stringSelection, null);
                         }
                     });
                     popup.add(menu_item2);
                     JMenuItem menu_item3 = new JMenuItem();
                     menu_item3.setText("Copy row data to clipboard");
                     menu_item3.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/page_copy.png")));
                     menu_item3.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                             StringBuilder out = new StringBuilder();
                             boolean first_row = true;
                             for(int i:table.getSelectedRows()){
                                 StringBuffer row = new StringBuffer();
                                 boolean first_col = true;
                                 for(int k = 0; k < table.getColumnCount();k = k + 1){
                                     if(first_col){
                                         row.append(getValue(table,i, k));
                                         first_col = false;
                                     }else{
                                         row.append("\t");
                                         row.append(getValue(table,i, k));
                                     }
                                 }
                                 if(first_row){
                                     out.append(row);
                                     first_row = false;
                                 }else{
                                     out.append("\n");
                                     out.append(row);
                                 }
                             }
                             StringSelection stringSelection = new StringSelection(out.toString());
                             clipboard.setContents(stringSelection, null);
                         }
                     });
                     popup.add(menu_item3);
 
                     popup.show(table, x, y);
                     return;
             }
             @Override
             public void mousePressed(MouseEvent e) {
                     if (e.isPopupTrigger()) myPopupEvent(e);
             }
             @Override
             public void mouseReleased(MouseEvent e) {
                     if (e.isPopupTrigger()) myPopupEvent(e);
             }
 
         };
         
         resultTable.addMouseListener(table_ma);
         MouseAdapter ma = new MouseAdapter() {
             private void addDefSelect(String table){
                 SqlTab tab = tabs.createTab(table);
                 String query =
                         "SELECT\n"+
                         "\t*\n"+
                         "FROM\n" +
                         "\t" + table;
                 tab.getEditPane().setText(query);
                 application.getView().RunButtonActionPerformed(null);
             }
             private void myPopupEvent(MouseEvent e) {
                     int x = e.getX();
                     int y = e.getY();
                     JTree tree = (JTree)e.getSource();
                     TreePath path = tree.getPathForLocation(x, y);
                     if (path == null){
                         JPopupMenu popup = new JPopupMenu();
                         JMenuItem menu_item = new JMenuItem();
                         menu_item.setText("Refresh(F5)");
                         menu_item.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/arrow_refresh.png")));
                         menu_item.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent e) {
                                 updateTree();
                             }
                         });
 
                         popup.add(menu_item);
                         popup.show(tree, x, y);
                         return;
                     }
                     tree.setSelectionPath(path);
                     final DefaultMutableTreeNode node =(DefaultMutableTreeNode)path.getLastPathComponent();
                     if(node.getUserObject().getClass() == TableObj.class){
                         JPopupMenu popup = new JPopupMenu();
                         JMenuItem menu_item = new JMenuItem();
                         menu_item.setText("Show table data");
                         menu_item.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/table.png")));
                         menu_item.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent e) {
                                 addDefSelect(node.getUserObject().toString());
                             }
                         });
 
                         JMenuItem menu_item1 = new JMenuItem();
                         menu_item1.setText("Refresh(F5)");
                         menu_item1.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/arrow_refresh.png")));
                         menu_item1.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent e) {
                                 ((TableObj)node.getUserObject()).refereshTable();
                             }
                         });
 
 
                         popup.add(menu_item);
                         popup.add(menu_item1);
 
                         popup.show(tree, x, y);
                     }else if(node.getUserObject().getClass() == DatabaseObj.class){
                         JPopupMenu popup = new JPopupMenu();
                         JMenuItem menu_item1 = new JMenuItem();
                         menu_item1.setText("Refresh(F5)");
                         menu_item1.setIcon(new ImageIcon(getClass().getResource("/mysqljavacat/resources/ledicons/arrow_refresh.png")));
                         menu_item1.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent e) {
                                 Task task = new Task(application) {
                                     @Override
                                     protected Object doInBackground() throws Exception {
                                         DatabaseTree.setEnabled(false);
                                         selected_db.refreshDatabase(true,true);
                                         DatabaseTree.setEnabled(true);
                                         return null;
                                     }
                                 };
                                 application.getContext().getTaskService().execute(task);
                                 application.getContext().getTaskMonitor().setForegroundTask(task);
                             }
                         });
                         popup.add(menu_item1);
 
                         popup.show(tree, x, y);
                     }
                     
             }
             @Override
             public void mousePressed(MouseEvent e) {
                     if (e.isPopupTrigger()) myPopupEvent(e);
             }
             @Override
             public void mouseReleased(MouseEvent e) {
                     if (e.isPopupTrigger()) myPopupEvent(e);                    
             }
             @Override
             public void mouseClicked(MouseEvent e){
                 if(e.getClickCount() == 2){
                     int x = e.getX();
                     int y = e.getY();
                     JTree tree = (JTree)e.getSource();                    
                     TreePath path = tree.getPathForLocation(x, y);
                     if (path == null)
                             return;
                     tree.setSelectionPath(path);
                     final DefaultMutableTreeNode node =(DefaultMutableTreeNode)path.getLastPathComponent();
                     if(node.getUserObject().getClass() == TableObj.class){
                         String text = tabs.getSelectedtab().getEditPane().getText();
                         text.replace("\r\n", "\n");
                         String before_caret = text.substring(0,tabs.getSelectedtab().getEditPane().getCaretPosition());
                         String after_caret = text.substring(tabs.getSelectedtab().getEditPane().getCaretPosition(),text.length());
                         text = before_caret + node.getUserObject().toString() + after_caret;                        
                         text.replace("\n", "\r\n");
                         tabs.getSelectedtab().getEditPane().setText(text);
                         tabs.getSelectedtab().getEditPane().requestFocusInWindow();
                         
                         //TODO Prevent tree expand && set caret pos
                         //tabs.getSelectedtab().getEditPane().setCaretPosition(tabs.getSelectedtab().getEditPane().getCaretPosition());
                         //DatabaseTree.setToggleClickCount(0);
                      }
 
                 }
             }
 
 
         };
         DatabaseTree.addKeyListener(new KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent e){
                 if(e.getKeyCode() == 116){
                     DefaultMutableTreeNode node = (DefaultMutableTreeNode)DatabaseTree.getSelectionPath().getLastPathComponent();
                     if(node.getUserObject().getClass() == TableObj.class){
                         ((TableObj)node.getUserObject()).refereshTable();
                     }else if(node.getUserObject().getClass() == DatabaseObj.class){
                         Task task = new Task(application) {
                             @Override
                             protected Object doInBackground() throws Exception {
                                 DatabaseTree.setEnabled(false);
                                 selected_db.refreshDatabase(true,true);
                                 DatabaseTree.setEnabled(true);
                                 return null;
                             }
                         };
                         application.getContext().getTaskService().execute(task);
                         application.getContext().getTaskMonitor().setForegroundTask(task);
                     }
 
                 }
             }
         });
         DatabaseTree.addMouseListener(ma);
         databaseCombo.setModel(new DefaultComboBoxModel());
         resultTable.setModel(new javax.swing.table.DefaultTableModel());
         treePane.setEnabled(false);        
 
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
         messageTimer = new Timer(messageTimeout, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 statusMessageLabel.setText("");
             }
         });
         messageTimer.setRepeats(false);
         int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
         for (int i = 0; i < busyIcons.length; i++) {
             busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
         }
         busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                 statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
             }
         });
         idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
 
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor(application.getContext());
         taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 String propertyName = evt.getPropertyName();
                 if ("started".equals(propertyName)) {
                     if (!busyIconTimer.isRunning()) {
                         statusAnimationLabel.setIcon(busyIcons[0]);
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(true);
                 } else if ("done".equals(propertyName)) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon(idleIcon);
                     progressBar.setVisible(false);
                     progressBar.setValue(0);
                     statusMessageLabel.setText(null);
                 } else if ("message".equals(propertyName)) {
                     String text = (String)(evt.getNewValue());
                     statusMessageLabel.setText((text == null) ? "" : text);
                     messageTimer.restart();
                 } else if ("progress".equals(propertyName)) {
                     int value = (Integer)(evt.getNewValue());
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(value);                    
                 }
             }
         });
     }
 
     @Action
     public void showAboutBox() {
         if (aboutBox == null) {
             JFrame mainFrame = MysqlJavaCatApp.getApplication().getMainFrame();
             aboutBox = new MysqlJavaCatAboutBox(mainFrame);
             aboutBox.setLocationRelativeTo(mainFrame);
         }
         MysqlJavaCatApp.getApplication().show(aboutBox);
     }
     public JTree getDbTree(){
         return DatabaseTree;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         jToolBar3 = new javax.swing.JToolBar();
         connectButton = new javax.swing.JButton();
         disconnectButton = new javax.swing.JButton();
         databaseCombo = new javax.swing.JComboBox();
         RunButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         jButton1 = new javax.swing.JButton();
         jSplitPane1 = new javax.swing.JSplitPane();
         horisontalSplit = new javax.swing.JSplitPane();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         resultTable = new javax.swing.JTable();
         jPanel2 = new javax.swing.JPanel();
         requestTime = new javax.swing.JLabel();
         rowCount = new javax.swing.JLabel();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         propText = new javax.swing.JEditorPane();
         jPanel4 = new javax.swing.JPanel();
         treePane = new javax.swing.JScrollPane();
         DatabaseTree = new javax.swing.JTree();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         saveMenu = new javax.swing.JMenuItem();
         saveAsMenu = new javax.swing.JMenuItem();
         openMenu = new javax.swing.JMenuItem();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         jMenu1 = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenu2 = new javax.swing.JMenu();
         jMenuItem2 = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
 
         mainPanel.setName("mainPanel"); // NOI18N
 
         jToolBar3.setBorder(null);
         jToolBar3.setRollover(true);
         jToolBar3.setName("jToolBar3"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(mysqljavacat.MysqlJavaCatApp.class).getContext().getResourceMap(MysqlJavaCatView.class);
         connectButton.setIcon(resourceMap.getIcon("connectButton.icon")); // NOI18N
         connectButton.setText(resourceMap.getString("connectButton.text")); // NOI18N
         connectButton.setToolTipText(resourceMap.getString("connectButton.toolTipText")); // NOI18N
         connectButton.setFocusable(false);
         connectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         connectButton.setName("connectButton"); // NOI18N
         connectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         connectButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 connectButtonActionPerformed(evt);
             }
         });
         jToolBar3.add(connectButton);
 
         disconnectButton.setIcon(resourceMap.getIcon("disconnectButton.icon")); // NOI18N
         disconnectButton.setText(resourceMap.getString("disconnectButton.text")); // NOI18N
         disconnectButton.setToolTipText(resourceMap.getString("disconnectButton.toolTipText")); // NOI18N
         disconnectButton.setEnabled(false);
         disconnectButton.setFocusable(false);
         disconnectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         disconnectButton.setName("disconnectButton"); // NOI18N
         disconnectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         disconnectButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 disconnectButtonActionPerformed(evt);
             }
         });
         jToolBar3.add(disconnectButton);
 
         databaseCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         databaseCombo.setEnabled(false);
         databaseCombo.setMaximumSize(new java.awt.Dimension(200, 150));
         databaseCombo.setName("databaseCombo"); // NOI18N
         databaseCombo.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 databaseComboItemStateChanged(evt);
             }
         });
         jToolBar3.add(databaseCombo);
 
         RunButton.setIcon(resourceMap.getIcon("RunButton.icon")); // NOI18N
         RunButton.setText(resourceMap.getString("RunButton.text")); // NOI18N
         RunButton.setToolTipText(resourceMap.getString("RunButton.toolTipText")); // NOI18N
         RunButton.setEnabled(false);
         RunButton.setFocusable(false);
         RunButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         RunButton.setName("RunButton"); // NOI18N
         RunButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         RunButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 RunButtonActionPerformed(evt);
             }
         });
         jToolBar3.add(RunButton);
 
         cancelButton.setIcon(resourceMap.getIcon("cancelButton.icon")); // NOI18N
         cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
         cancelButton.setToolTipText(resourceMap.getString("cancelButton.toolTipText")); // NOI18N
         cancelButton.setEnabled(false);
         cancelButton.setFocusable(false);
         cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cancelButton.setName("cancelButton"); // NOI18N
         cancelButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
         jToolBar3.add(cancelButton);
 
         jSeparator1.setName("jSeparator1"); // NOI18N
         jToolBar3.add(jSeparator1);
 
         jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
         jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
         jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
         jButton1.setEnabled(false);
         jButton1.setFocusable(false);
         jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton1.setName("jButton1"); // NOI18N
         jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
         jToolBar3.add(jButton1);
 
         jSplitPane1.setBorder(null);
         jSplitPane1.setName("jSplitPane1"); // NOI18N
 
         horisontalSplit.setBorder(null);
         horisontalSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         horisontalSplit.setName("horisontalSplit"); // NOI18N
 
         jTabbedPane1.setFocusable(false);
         jTabbedPane1.setName("jTabbedPane1"); // NOI18N
 
         jPanel1.setName("jPanel1"); // NOI18N
 
         jScrollPane2.setBorder(null);
         jScrollPane2.setName("jScrollPane2"); // NOI18N
 
         resultTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         resultTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         resultTable.setName("resultTable"); // NOI18N
         jScrollPane2.setViewportView(resultTable);
 
         jPanel2.setName("jPanel2"); // NOI18N
 
         requestTime.setText(resourceMap.getString("requestTime.text")); // NOI18N
         requestTime.setName("requestTime"); // NOI18N
 
         rowCount.setText(resourceMap.getString("rowCount.text")); // NOI18N
         rowCount.setName("rowCount"); // NOI18N
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(requestTime)
                 .addGap(231, 231, 231)
                 .addComponent(rowCount)
                 .addContainerGap(433, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(rowCount, javax.swing.GroupLayout.DEFAULT_SIZE, 8, Short.MAX_VALUE)
                     .addComponent(requestTime))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
 
         jPanel3.setName("jPanel3"); // NOI18N
 
         jScrollPane1.setBorder(null);
         jScrollPane1.setEnabled(false);
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         propText.setName("propText"); // NOI18N
         jScrollPane1.setViewportView(propText);
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N
 
         horisontalSplit.setBottomComponent(jTabbedPane1);
 
         jSplitPane1.setRightComponent(horisontalSplit);
 
         jPanel4.setName("jPanel4"); // NOI18N
 
         treePane.setBorder(null);
         treePane.setName("treePane"); // NOI18N
 
         DatabaseTree.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         DatabaseTree.setName("DatabaseTree"); // NOI18N
         DatabaseTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
             public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
             }
             public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                 DatabaseTreeTreeExpanded(evt);
             }
         });
         DatabaseTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
             public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                 DatabaseTreeValueChanged(evt);
             }
         });
         treePane.setViewportView(DatabaseTree);
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(treePane, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(treePane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
         );
 
         jSplitPane1.setLeftComponent(jPanel4);
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
             .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
         );
 
         menuBar.setName("menuBar"); // NOI18N
 
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         saveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         saveMenu.setIcon(resourceMap.getIcon("saveMenu.icon")); // NOI18N
         saveMenu.setText(resourceMap.getString("saveMenu.text")); // NOI18N
         saveMenu.setName("saveMenu"); // NOI18N
         saveMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveMenuActionPerformed(evt);
             }
         });
         fileMenu.add(saveMenu);
 
         saveAsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         saveAsMenu.setIcon(resourceMap.getIcon("saveAsMenu.icon")); // NOI18N
         saveAsMenu.setText(resourceMap.getString("saveAsMenu.text")); // NOI18N
         saveAsMenu.setName("saveAsMenu"); // NOI18N
         saveAsMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveAsMenuActionPerformed(evt);
             }
         });
         fileMenu.add(saveAsMenu);
 
         openMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         openMenu.setIcon(resourceMap.getIcon("openMenu.icon")); // NOI18N
         openMenu.setText(resourceMap.getString("openMenu.text")); // NOI18N
         openMenu.setName("openMenu"); // NOI18N
         openMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 openMenuActionPerformed(evt);
             }
         });
         fileMenu.add(openMenu);
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(mysqljavacat.MysqlJavaCatApp.class).getContext().getActionMap(MysqlJavaCatView.class, this);
         exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
         exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
         exitMenuItem.setName("exitMenuItem"); // NOI18N
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
         jMenu1.setName("jMenu1"); // NOI18N
 
         jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
         jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
         jMenuItem1.setName("jMenuItem1"); // NOI18N
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuItem1);
 
         menuBar.add(jMenu1);
 
         jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
         jMenu2.setName("jMenu2"); // NOI18N
 
         jMenuItem2.setIcon(resourceMap.getIcon("jMenuItem2.icon")); // NOI18N
         jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
         jMenuItem2.setName("jMenuItem2"); // NOI18N
         jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem2ActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuItem2);
 
         menuBar.add(jMenu2);
 
         helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
         helpMenu.setName("helpMenu"); // NOI18N
 
         aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
         aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
         aboutMenuItem.setName("aboutMenuItem"); // NOI18N
         helpMenu.add(aboutMenuItem);
 
         menuBar.add(helpMenu);
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(statusMessageLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 687, Short.MAX_VALUE)
                 .addComponent(statusAnimationLabel)
                 .addContainerGap())
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                 .addContainerGap(401, Short.MAX_VALUE)
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(58, 58, 58))
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(statusMessageLabel)
                         .addComponent(statusAnimationLabel))
                     .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(3, 3, 3))
         );
 
         setComponent(mainPanel);
         setMenuBar(menuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
         try {
             connectButton.setEnabled(false);
             resultTable.setModel(new DefaultTableModel());
             application.connectToDb();
             if(application.getConnection() != null){
                 updateTree();
             }
         } catch (SQLException ex) {
             application.showError(ex.getMessage());
         }
     }//GEN-LAST:event_connectButtonActionPerformed
 
     private void RunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunButtonActionPerformed
         jTabbedPane1.setSelectedIndex(0);
         queryTask = new Task(application) {
             private Statement stmt;
             private ResultSet r1;
             private void openGui(){
                 RunButton.setEnabled(true);
                 DatabaseTree.setEnabled(true);
                 cancelButton.setEnabled(false);
                 databaseCombo.setEnabled(true);
                 disconnectButton.setEnabled(true);
                 for(MenuElement m : menuBar.getSubElements()){
                     m.getComponent().setEnabled(true);
                 }
 
             }
             private void closeGui(){
                 RunButton.setEnabled(false);
                 DatabaseTree.setEnabled(false);
                 cancelButton.setEnabled(true);
                 databaseCombo.setEnabled(false);
                 disconnectButton.setEnabled(false);
                 menuBar.setEnabled(false);
                 for(MenuElement m : menuBar.getSubElements()){
                     m.getComponent().setEnabled(false);
                 }
             }
             @Override
             protected Object doInBackground() throws Exception {
                 closeGui();
                 SqlTab tab =  tabs.getSelectedtab();
                 tab.save();
                 Object res = null;
                 if(application.getConnection() == null || tab.getEditPane().getText().isEmpty()){
                     return res;
                 }                
                 Long time = System.currentTimeMillis();
                 stmt = application.getConnection().createStatement();
                 stmt.setFetchSize(Integer.MIN_VALUE);
                 
                 try{
                     setMessage("Executing Query");
                     if(stmt.execute(tab.getEditPane().getText())){
                         res = stmt.getResultSet();
                     }else{
                         res = stmt.getUpdateCount();
                     }                    
                 }catch(SQLException e){                    
                     resultTable.setModel(new javax.swing.table.DefaultTableModel());
                     if(e.getErrorCode() != 1317){
                         application.showError(e.getMessage());
                     }
                     return null;
                 }
                 Long milisec = System.currentTimeMillis() - time;
                 tab.setRequestTime(milisec);
                 Float sec = milisec.floatValue() / 1000;                
                 javax.swing.table.DefaultTableModel defaultColumnModel = new javax.swing.table.DefaultTableModel();
                 int size = 0;
                 if(res.getClass() == com.mysql.jdbc.JDBC4ResultSet.class){
                     setMessage("Fetching Result");
                     r1 = (ResultSet)res;
                     int col_count = r1.getMetaData().getColumnCount();
                     for(int i = 1;i <= col_count;i = i + 1){
                         defaultColumnModel.addColumn(r1.getMetaData().getColumnLabel(i));
                     }
                     while(r1.next()){                        
                         Object [] arr = new Object[col_count];
                         for(int i = 1; i <= col_count ; i = i + 1){
                             Object obj = null;
                             try{
                                 obj = r1.getObject(i);
                             }catch(SQLException e){
                                 obj = "0000-00-00";
                             }
                            try{
                                byte[] b = (byte[])obj;
                                obj = new String(b);
                            }catch(Exception e){
                            }
                             arr[i-1] = obj;
                         }
                         defaultColumnModel.addRow(arr);
                         size = size + 1;
                     }                    
                 }else{
                     defaultColumnModel.addColumn("Rows Affected");
                     ArrayList<Object> single_cell = new ArrayList<Object>();
                     single_cell.add(res);
                     size = 1;
                     defaultColumnModel.addRow(single_cell.toArray());
                 }
                 tab.setRowCount(size);
                 
                 if(tabs.getSelectedtab() == tab){
                     resultTable.setModel(defaultColumnModel);
                     rowCount.setText("Row count: " + new Integer(size).toString());
                     requestTime.setText("Request time: " + sec.toString() + " sec");
                     TableColumnModel columns = resultTable.getColumnModel();
                     for (int i = columns.getColumnCount() - 1; i >= 0; --i){
                         columns.getColumn(i).setPreferredWidth(200);
                     }
                 }
                 tab.setResultColModel(defaultColumnModel);
                 resultTable.getTableHeader().setReorderingAllowed(false);
                 return null;
             }
             @Override
             public void cancelled() {
                 try {
                     if(r1 != null)
                         r1.close();
                     stmt.cancel();
                 } catch (SQLException ex) {
                     application.showError(ex.getMessage());
                 }
             }
             @Override
             public void finished() {
                 try {
                     if(stmt != null){
                         stmt.clearBatch();
                         stmt.close();
                     }
                     openGui();
                 } catch (SQLException ex) {
                     application.showError(ex.getMessage());
                 }
             }
         };
 
         application.getContext().getTaskService().execute(queryTask);
         application.getContext().getTaskMonitor().setForegroundTask(queryTask);
     }//GEN-LAST:event_RunButtonActionPerformed
 
     private void disconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectButtonActionPerformed
         application.closeConnection();
         disconnectButton.setEnabled(false);
         connectButton.setEnabled(true);
         RunButton.setEnabled(false);
         databaseCombo.setEnabled(false);
         DatabaseTree.setModel(null);
         databaseCombo.setModel(new DefaultComboBoxModel());
     }//GEN-LAST:event_disconnectButtonActionPerformed
 
     private void databaseComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_databaseComboItemStateChanged
         if(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED){
             application.executeSql("USE " + evt.getItem().toString());
             selected_db = (DatabaseObj)evt.getItem();
             Task task = new Task(application) {
                 @Override
                 protected Object doInBackground() throws Exception {
                     DatabaseTree.setEnabled(false);
                     selected_db.refreshDatabase(true,false);
                     DatabaseTree.setEnabled(true);
                     ((DefaultTreeModel)DatabaseTree.getModel()).reload((DefaultMutableTreeNode)DatabaseTree.getModel().getRoot());
                     return null;
                 }
             };
             application.getContext().getTaskService().execute(task);
             application.getContext().getTaskMonitor().setForegroundTask(task);
         }
     }//GEN-LAST:event_databaseComboItemStateChanged
 
     private void DatabaseTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_DatabaseTreeValueChanged
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
         if(node.getUserObject().getClass() == DatabaseObj.class){
             databaseCombo.setSelectedItem(node.getUserObject());
         }
         if(node.getUserObject().getClass() == TableObj.class){
             databaseCombo.setSelectedItem(((TableObj)node.getUserObject()).getDatabase());
             String createTable = application.getRows(application.executeSql("SHOW CREATE TABLE " + node.getUserObject())).get(0).get(1).toString();
             propText.setText(createTable);
         }
     }//GEN-LAST:event_DatabaseTreeValueChanged
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         application.show(application.getConfigDialog());
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void DatabaseTreeTreeExpanded(javax.swing.event.TreeExpansionEvent evt) {//GEN-FIRST:event_DatabaseTreeTreeExpanded
         TreeSelectionEvent event = new TreeSelectionEvent(DatabaseTree, evt.getPath(), true, null, null);
         DatabaseTreeValueChanged(event);
     }//GEN-LAST:event_DatabaseTreeTreeExpanded
 
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
         queryTask.cancel(true);
     }//GEN-LAST:event_cancelButtonActionPerformed
 
     private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
         new ExportToExcel(application.getMainFrame(), true).setVisible(true);
     }//GEN-LAST:event_jMenuItem2ActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         /*
         int response;
         response = JOptionPane.showConfirmDialog(null, "Close all tabs?","",JOptionPane.OK_CANCEL_OPTION);
         if(response == JOptionPane.YES_OPTION){
             tabs.createTab(System.currentTimeMillis() + "");
             while(tabs.getSqlTabs().size() != 1){
                 tabs.getSqlTabs().get(0).close();
             }
             tabs.getSelectedtab().setFilename("Untitled.sql");
             tabs.getSelectedtab().setTabLabel("Untitled.sql");
         }
          * 
          */
 
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuActionPerformed
         if(!tabs.getSelectedtab().isSaved() && !tabs.getSelectedtab().getFile().exists()){
             SaveDialod dialog = new SaveDialod(this.getFrame(), true);
             dialog.setVisible(true);
         } else {
             tabs.getSelectedtab().save();
         }
     }//GEN-LAST:event_saveMenuActionPerformed
 
     public void proxiSaveAction(){
         saveMenuActionPerformed(null);
     }
     private void saveAsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuActionPerformed
         SaveDialod dialog = new SaveDialod(this.getFrame(), true);
         dialog.setVisible(true);
     }//GEN-LAST:event_saveAsMenuActionPerformed
 
     private void openMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuActionPerformed
         OpenDialog dialog = new OpenDialog(this.getFrame(), true);
         dialog.setVisible(true);
     }//GEN-LAST:event_openMenuActionPerformed
 
     public void proxyRunConnect(){
         connectButtonActionPerformed(null);
     }
     private void updateTree(){                
                 Task task = new Task(application) {
                     @Override
                     protected Object doInBackground() throws Exception {
                         DatabaseTree.setModel(null);
                         DatabaseTree.setEnabled(false);
                         RunButton.setEnabled(false);
                         DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Databases");
                         DatabaseTree.setModel(new DefaultTreeModel(rootNode));
                         DefaultComboBoxModel combo_model = new DefaultComboBoxModel();
                         ArrayList<ArrayList<Object>> all_databases  =  application.getRows(application.executeSql("SHOW DATABASES"));
                         for(ArrayList<Object> row :  all_databases){
                             DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode();
                             DatabaseObj database = new DatabaseObj(row.get(0).toString(),databaseNode);
                             databaseNode.setUserObject(database);
                             combo_model.addElement(database);
                             rootNode.add(databaseNode);
                             selected_db = database;
                             databases.put(database.toString(), database);
                         }
                         ((DefaultTreeModel)DatabaseTree.getModel()).reload();                       
                         for(ArrayList<Object> row :  all_databases){
                             setMessage("Database scan:" + row.get(0).toString());
                             databases.get(row.get(0).toString()).refreshDatabase(false,false);
                         }
                         databaseCombo.setModel(combo_model);
                         DatabaseTree.setEnabled(true);
                         connectButton.setEnabled(false);
                         disconnectButton.setEnabled(true);
                         databaseCombo.setEnabled(true);
                         RunButton.setEnabled(true);
                         combo_model.setSelectedItem(selected_db);
                         return null;
                     }
                 };
                 application.getContext().getTaskService().execute(task);
                 application.getContext().getTaskMonitor().setForegroundTask(task);                     
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTree DatabaseTree;
     private javax.swing.JButton RunButton;
     private javax.swing.JButton cancelButton;
     private javax.swing.JButton connectButton;
     private javax.swing.JComboBox databaseCombo;
     private javax.swing.JButton disconnectButton;
     private javax.swing.JSplitPane horisontalSplit;
     private javax.swing.JButton jButton1;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JToolBar jToolBar3;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenuItem openMenu;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JEditorPane propText;
     private javax.swing.JLabel requestTime;
     private javax.swing.JTable resultTable;
     private javax.swing.JLabel rowCount;
     private javax.swing.JMenuItem saveAsMenu;
     private javax.swing.JMenuItem saveMenu;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     private javax.swing.JScrollPane treePane;
     // End of variables declaration//GEN-END:variables
 
 }
