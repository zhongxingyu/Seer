 /* GMBrain - Decompiled by JODE
  * Visit http://jode.sourceforge.net/
  */
 package gmbrain;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.URL;
 import java.util.Vector;
 import javax.swing.*;
 import javax.swing.border.*;
 
 public class GMBrain extends JFrame {
   // fields
   private GMNode presentNode;
   public boolean bidirectionalLinkDefault = true;
   private NodeWeb presentWeb = new NodeWeb();
   private JButton btnSpawn;
   private JMenuItem mnuInsertTxtFile;
   private JMenuItem mnuGetAlphaPrintPut;
   private JMenuItem mnuGetGroupedPrintPut;
   private JPanel pnlLinkViewer;
   private JMenuItem mnuAddTableTags;
   private JMenuItem mnuAbout;
   private JSeparator jSeparator1;
   private JMenuItem mnuToggleIconVisibility;
   private JMenu jMenu9;
   private JMenuItem mnuMonospace;
   private JMenu jMenu8;
   private JMenu jMenu7;
   private JButton btnTableDimReady;
   private JMenu jMenu6;
   private JTextArea txtDescription;
   private JMenu jMenu5;
   private JMenu jMenu4;
   private JMenu jMenu3;
   private JTextArea txtRows;
   private JMenu jMenu2;
   private JList linkDisplay;
   private JMenu jMenu1;
   private JTextField txtNodeTitle;
   private JMenuItem mnuOpen;
   private JMenuItem mnuChangeIcon;
   private JPanel jPanel11;
   private JScrollPane txtHelpPane;
   private JMenuItem mnuToggleRootPreserve;
   private JMenuItem mnuSpawnNewLink;
   private JMenuItem mnuSetTypeName;
   private JMenuItem mnuProgramHelp;
   private JFrame tblFrame;
   private JTextArea txtColumns;
   private JMenuItem mnuCreateNewType;
   private JMenuItem mnuAddMonoTags;
   private JLabel jLabel3;
   private JMenu jMenu10;
   private JLabel jLabel2;
   private JLabel jLabel1;
   private JMenuItem mnuHTMLPort;
   private JButton btnAddLink;
   private JPanel jPanel1;
   private JMenuItem mnuChangeDefaultType;
   private JPanel nodeViewer;
   private JScrollPane jScrollPane2;
   private JLabel lblNodeType;
   private JScrollPane jScrollPane1;
   private JMenuItem mnuExit;
   private JMenuItem mnuGetHelpWeb;
   private JMenuItem mnuDeLink;
   private JMenuItem mnuLicense;
   private JFrame helpFrame;
   private JMenuItem mnuGroupTypeManager;
   private JMenuItem mnuAddLink;
   private JMenuItem mnuXMLPort;
   private JMenuItem mnuSaveAs;
   private JMenuItem mnuSetAsRoot;
   private JTextArea txtHelpText;
   private JMenuItem mnuChangeType;
   private JMenuItem mnuTypeInfo;
   private JButton btnDeLink;
   private JMenuItem mnuFontBigger;
   private JPanel linkActionPanel;
   private JLabel lblNotificationBar;
   private JMenuItem mnuFontSmaller;
   private JMenuItem mnuSave;
   private JMenuItem mnuFindLost;
   private JMenuItem mnuGPL;
   private JMenuItem mnuSearchForNode;
   private JPanel titlePanel;
   private JMenuItem mnuNew;
   private JLabel jLabel31;
   private JMenuBar jMenuBar1;
   private JMenuItem mnuAlphaSortManager;
   private JMenuItem mnuGetPrintPut;
   private JButton btnGotoNode;
   private File presentFile;
   private JFileChooser fc = new JFileChooser();
   private String defaultType = "Undefined";
   private boolean monospaceToggle = false;
   private NodeWebManager nwManager = new NodeWebManager();
   private boolean preserveRoot = true;
   private File initFile;
   private boolean iconsAreVisible = true;
     
   
   // ---------------------- myNodeCellRenderer class ------------------
   public class myNodeCellRenderer extends JLabel implements ListCellRenderer {
     GMNode n;
   
     public Component getListCellRendererComponent(JList jList, Object obj, int param, boolean param3,boolean param4) {
       try {
         setOpaque(true);
         if (obj instanceof GMNode) {
           n = (GMNode) obj;
           setText(n.getTitle());
           try {
             if (iconsAreVisible) setIcon(n.getIcon());
             else setIcon(new ImageIcon(n.getClass().getResource("empty.gif")));
           } catch (Exception e) {
             System.out.println(e.toString());
           }
         } 
         else setText("no Node here.");
         if (param3) {
           setBackground(jList.getSelectionBackground());
           setForeground(jList.getSelectionForeground());
         } else {
           setBackground(jList.getBackground());
           setForeground(jList.getForeground());
         }
         return this;
       } catch (Exception e) {
         System.out.println("Trouble in CellRenderer");
         System.out.println(e.getMessage());
         return this;
       }
     }
   }
   // ------------------------------------------------------------------
   
   
   public GMBrain() {
     System.out.println("initComponents");
     initComponents();
     System.out.println("setUpFile");
     setUpFile();
     System.out.println("toFront");
     toFront();
     System.out.println("transferFocus");
     transferFocus();
     System.out.println("statusMessage");
     statusMessage(initFile.getPath());
   }
   
   // ------------------------------------ initComponents --------------
   @SuppressWarnings("unchecked")
   private void initComponents() {
     helpFrame = new JFrame();
     txtHelpPane = new JScrollPane();
     txtHelpText = new JTextArea();
     jLabel2 = new JLabel();
     tblFrame = new JFrame();
     jPanel1 = new JPanel();
     jLabel3 = new JLabel();
     txtColumns = new JTextArea();
     jPanel11 = new JPanel();
     jLabel31 = new JLabel();
     txtRows = new JTextArea();
     btnTableDimReady = new JButton();
     pnlLinkViewer = new JPanel();
     linkActionPanel = new JPanel();
     btnAddLink = new JButton();
     btnDeLink = new JButton();
     btnSpawn = new JButton();
     btnGotoNode = new JButton();
     jLabel1 = new JLabel();
     jScrollPane1 = new JScrollPane();
     linkDisplay = new JList();
     nodeViewer = new JPanel();
     titlePanel = new JPanel();
     txtNodeTitle = new JTextField();
     lblNodeType = new JLabel();
     lblNotificationBar = new JLabel();
     jScrollPane2 = new JScrollPane();
     txtDescription = new JTextArea();
     jMenuBar1 = new JMenuBar();
     jMenu1 = new JMenu();
     mnuNew = new JMenuItem();
     mnuOpen = new JMenuItem();
     mnuSave = new JMenuItem();
     mnuSaveAs = new JMenuItem();
     mnuExit = new JMenuItem();
     jSeparator1 = new JSeparator();
     mnuXMLPort = new JMenuItem();
     mnuHTMLPort = new JMenuItem();
     jMenu2 = new JMenu();
     mnuAddLink = new JMenuItem();
     mnuDeLink = new JMenuItem();
     mnuSpawnNewLink = new JMenuItem();
     jMenu6 = new JMenu();
     mnuChangeType = new JMenuItem();
     mnuChangeIcon = new JMenuItem();
     mnuCreateNewType = new JMenuItem();
     mnuSetTypeName = new JMenuItem();
     mnuChangeDefaultType = new JMenuItem();
     jMenu3 = new JMenu();
     mnuFindLost = new JMenuItem();
     mnuSearchForNode = new JMenuItem();
     mnuGetPrintPut = new JMenuItem();
     jMenu5 = new JMenu();
     mnuGetAlphaPrintPut = new JMenuItem();
     mnuGetGroupedPrintPut = new JMenuItem();
     jMenu8 = new JMenu();
     mnuToggleRootPreserve = new JMenuItem();
     mnuAlphaSortManager = new JMenuItem();
     mnuGroupTypeManager = new JMenuItem();
     mnuSetAsRoot = new JMenuItem();
     jMenu4 = new JMenu();
     mnuProgramHelp = new JMenuItem();
     mnuAbout = new JMenuItem();
     mnuTypeInfo = new JMenuItem();
     mnuGPL = new JMenuItem();
     mnuLicense = new JMenuItem();
     mnuGetHelpWeb = new JMenuItem();
     jMenu7 = new JMenu();
     mnuToggleIconVisibility = new JMenuItem();
     jMenu9 = new JMenu();
     mnuAddMonoTags = new JMenuItem();
     mnuAddTableTags = new JMenuItem();
     mnuInsertTxtFile = new JMenuItem();
     jMenu10 = new JMenu();
     mnuFontBigger = new JMenuItem();
     mnuFontSmaller = new JMenuItem();
     mnuMonospace = new JMenuItem();
     helpFrame.getContentPane().setLayout(new BorderLayout(10, 10));
     helpFrame.setTitle("Text Viewer");
     helpFrame.setMaximizedBounds(new Rectangle(300, 250, 300, 250));
     helpFrame.setName("helpFrame");
     txtHelpPane.setName("scrHelpPane");
     txtHelpPane.setAutoscrolls(true);
     txtHelpText.setColumns(40);
     txtHelpText.setEditable(false);
     txtHelpText.setLineWrap(true);
     txtHelpText.setRows(30);
     txtHelpText.setWrapStyleWord(true);
     txtHelpText.setMinimumSize(new Dimension(100, 16));
     txtHelpPane.setViewportView(txtHelpText);
     helpFrame.getContentPane().add(txtHelpPane, "Center");
     jLabel2.setText(" ");
     helpFrame.getContentPane().add(jLabel2, "West");
     tblFrame.getContentPane().setLayout(new FlowLayout());
     tblFrame.setTitle("Table Dimensions");
     tblFrame.setMaximizedBounds(new Rectangle(0, 0, 205, 90));
     tblFrame.setResizable(false);
     jLabel3.setText("Columns");
     jPanel1.add(jLabel3);
     txtColumns.setColumns(3);
     txtColumns.setText("0");
     jPanel1.add(txtColumns);
     tblFrame.getContentPane().add(jPanel1);
     jLabel31.setText("Rows");
     jPanel11.add(jLabel31);
     txtRows.setColumns(3);
     txtRows.setText("0");
     jPanel11.add(txtRows);
     tblFrame.getContentPane().add(jPanel11);
     btnTableDimReady.setText("OK");
     btnTableDimReady.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.btnTableDimReadyActionPerformed(evt);
       }
     });
     tblFrame.getContentPane().add(btnTableDimReady);
     getContentPane().setLayout(new BorderLayout(5, 3));
     setDefaultCloseOperation(0);
     setTitle("GM's Second Brain");
     addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent evt) {
        GMBrain.this.exitForm(evt);
       }
     });
     pnlLinkViewer.setLayout(new BorderLayout(0, 5));
     pnlLinkViewer.setBorder(new SoftBevelBorder(0));
     pnlLinkViewer.setMaximumSize(new Dimension(300, 2147483647));
     pnlLinkViewer.setMinimumSize(new Dimension(200, 109));
     linkActionPanel.setLayout(new GridLayout(2, 2, 3, 3));
     btnAddLink.setMnemonic('A');
     btnAddLink.setText("Add Link");
     btnAddLink.setToolTipText("Select a new Node to link to.");
     btnAddLink.setActionCommand("addLink");
     btnAddLink.setMaximumSize(new Dimension(100, 26));
     btnAddLink.setMinimumSize(new Dimension(100, 26));
     btnAddLink.setPreferredSize(new Dimension(100, 26));
     btnAddLink.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.btnAddLinkActionPerformed(evt);
       }
     });
     linkActionPanel.add(btnAddLink);
     btnDeLink.setMnemonic('D');
     btnDeLink.setText("De-Link");
     btnDeLink.setToolTipText("This will unlink the Nodes selected above.");
     btnDeLink.setMaximumSize(new Dimension(100, 26));
     btnDeLink.setMinimumSize(new Dimension(100, 26));
     btnDeLink.setPreferredSize(new Dimension(50, 26));
     btnDeLink.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.btnDeLinkActionPerformed(evt);
       }
     });
     linkActionPanel.add(btnDeLink);
     btnSpawn.setMnemonic('W');
     btnSpawn.setText("Spawn New");
     btnSpawn.setToolTipText("Create a new Node linked to this one.");
     btnSpawn.setMaximumSize(new Dimension(50, 26));
     btnSpawn.setMinimumSize(new Dimension(50, 26));
     btnSpawn.setPreferredSize(new Dimension(50, 26));
     btnSpawn.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.btnSpawnActionPerformed(evt);
       }
     });
     linkActionPanel.add(btnSpawn);
     btnGotoNode.setMnemonic('G');
     btnGotoNode.setText("Goto");
     btnGotoNode.setMaximumSize(new Dimension(100, 26));
     btnGotoNode.setMinimumSize(new Dimension(100, 26));
     btnGotoNode.setPreferredSize(new Dimension(100, 26));
     btnGotoNode.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.btnGotoNodeActionPerformed(evt);
       }
     });
     linkActionPanel.add(btnGotoNode);
     pnlLinkViewer.add(linkActionPanel, "South");
     jLabel1.setHorizontalAlignment(0);
     jLabel1.setText("Links From This Node");
     pnlLinkViewer.add(jLabel1, "North");
     linkDisplay.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
     linkDisplay.setToolTipText("These nodes are linked to this one.");
     linkDisplay.setCellRenderer(new myNodeCellRenderer());
     linkDisplay.setSelectionMode(0);
     linkDisplay.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
         GMBrain.this.linkDisplayMouseClicked(evt);
       }
     });
     jScrollPane1.setViewportView(linkDisplay);
     pnlLinkViewer.add(jScrollPane1, "Center");
     getContentPane().add(pnlLinkViewer, "East");
     nodeViewer.setLayout(new BorderLayout(3, 3));
     titlePanel.setLayout(new BorderLayout());
     txtNodeTitle.setText("txtNodeTitle");
     txtNodeTitle.addKeyListener(new KeyAdapter() {
       public void keyTyped(KeyEvent evt) {
         GMBrain.this.txtNodeTitleKeyTyped(evt);
       }
     });
     titlePanel.add(txtNodeTitle, "Center");
     lblNodeType.setText("type");
     lblNodeType.setToolTipText("Double-Click to change this Node's type.");
     lblNodeType.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
         GMBrain.this.lblNodeTypeMouseClicked(evt);
       }
     });
     titlePanel.add(lblNodeType, "East");
     nodeViewer.add(titlePanel, "North");
     lblNotificationBar.setBackground(new Color(204, 255, 204));
     lblNotificationBar.setForeground(new Color(255, 0, 255));
     lblNotificationBar.setText("jLabel1");
     nodeViewer.add(lblNotificationBar, "South");
     txtDescription.setColumns(40);
     txtDescription.setLineWrap(true);
     txtDescription.setRows(30);
     txtDescription.setWrapStyleWord(true);
     txtDescription.addKeyListener(new KeyAdapter() {
       public void keyTyped(KeyEvent evt) {
         GMBrain.this.txtDescriptionKeyTyped(evt);
       }
     });
     jScrollPane2.setViewportView(txtDescription);
     nodeViewer.add(jScrollPane2, "Center");
     getContentPane().add(nodeViewer, "Center");
     jMenu1.setMnemonic('F');
     jMenu1.setText("File");
     mnuNew.setMnemonic('N');
     mnuNew.setText("New");
     mnuNew.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuNewActionPerformed(evt);
       }
     });
     jMenu1.add(mnuNew);
     mnuOpen.setMnemonic('O');
     mnuOpen.setText("Open");
     mnuOpen.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuOpenActionPerformed(evt);
       }
     });
     jMenu1.add(mnuOpen);
     mnuSave.setMnemonic('S');
     mnuSave.setText("Save");
     mnuSave.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuSaveActionPerformed(evt);
       }
     });
     jMenu1.add(mnuSave);
     mnuSaveAs.setMnemonic('v');
     mnuSaveAs.setText("Save As");
     mnuSaveAs.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuSaveAsActionPerformed(evt);
       }
     });
     jMenu1.add(mnuSaveAs);
     mnuExit.setMnemonic('x');
     mnuExit.setText("Exit");
     mnuExit.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuExitActionPerformed(evt);
       }
     });
     jMenu1.add(mnuExit);
     jMenu1.add(jSeparator1);
     mnuXMLPort.setMnemonic('M');
     mnuXMLPort.setText("Export to XML");
     mnuXMLPort.setToolTipText("creates \"brain.xml\"");
     mnuXMLPort.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuXMLPortActionPerformed(evt);
       }
     });
     jMenu1.add(mnuXMLPort);
     mnuHTMLPort.setMnemonic('H');
     mnuHTMLPort.setText("Export to HTML");
     mnuHTMLPort.setToolTipText("exports information to Brain.html");
     mnuHTMLPort.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuHTMLPortActionPerformed(evt);
       }
     });
     jMenu1.add(mnuHTMLPort);
     jMenuBar1.add(jMenu1);
     jMenu2.setMnemonic('L');
     jMenu2.setText("Link");
     mnuAddLink.setMnemonic('A');
     mnuAddLink.setText("Add Link");
     mnuAddLink.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuAddLinkActionPerformed(evt);
       }
     });
     jMenu2.add(mnuAddLink);
     mnuDeLink.setText("De-Link");
     mnuDeLink.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuDeLinkActionPerformed(evt);
       }
     });
     jMenu2.add(mnuDeLink);
     mnuSpawnNewLink.setText("Spawn New Node and Link");
     mnuSpawnNewLink.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuSpawnNewLinkActionPerformed(evt);
       }
     });
     jMenu2.add(mnuSpawnNewLink);
     jMenuBar1.add(jMenu2);
     jMenu6.setMnemonic('T');
     jMenu6.setText("Types");
     mnuChangeType.setMnemonic('c');
     mnuChangeType.setText("Select Type for Node");
     mnuChangeType.setToolTipText("select a type for this node");
     mnuChangeType.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuChangeTypeActionPerformed(evt);
       }
     });
     jMenu6.add(mnuChangeType);
     mnuChangeIcon.setMnemonic('g');
     mnuChangeIcon.setText("Change Icon for type");
     mnuChangeIcon.setToolTipText("You select an icon for nodes of this type.");
     mnuChangeIcon.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuChangeIconActionPerformed(evt);
       }
     });
     jMenu6.add(mnuChangeIcon);
     mnuCreateNewType.setMnemonic('T');
     mnuCreateNewType.setText("Create New Type");
     mnuCreateNewType.setToolTipText("Changes Name of this node's type to a value you specify");
     mnuCreateNewType.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuCreateNewTypeActionPerformed(evt);
       }
     });
     jMenu6.add(mnuCreateNewType);
     mnuSetTypeName.setMnemonic('m');
     mnuSetTypeName.setText("Change Type Name");
     mnuSetTypeName.setToolTipText("resets type name without creating a new type");
     mnuSetTypeName.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuSetTypeNameActionPerformed(evt);
       }
     });
     jMenu6.add(mnuSetTypeName);
     mnuChangeDefaultType.setMnemonic('D');
     mnuChangeDefaultType.setText("Select Default Node Type");
     mnuChangeDefaultType.setToolTipText("Change the default type for newly created nodes.");
     mnuChangeDefaultType.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuChangeDefaultTypeActionPerformed(evt);
       }
     });
     jMenu6.add(mnuChangeDefaultType);
     jMenuBar1.add(jMenu6);
     jMenu3.setMnemonic('N');
     jMenu3.setText("Node Web");
     mnuFindLost.setText("Find Lost ");
     mnuFindLost.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuFindLostActionPerformed(evt);
       }
     });
     jMenu3.add(mnuFindLost);
     mnuSearchForNode.setText("Search for Node");
     mnuSearchForNode.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuSearchForNodeActionPerformed(evt);
       }
     });
     jMenu3.add(mnuSearchForNode);
     mnuGetPrintPut.setText("Show this Node's Printed Form");
     mnuGetPrintPut.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuGetPrintPutActionPerformed(evt);
       }
     });
     jMenu3.add(mnuGetPrintPut);
     jMenu5.setText("Get Web Text");
     mnuGetAlphaPrintPut.setText("Alphabetical List");
     mnuGetAlphaPrintPut.setToolTipText("Provides a text output of the web with the nodes alphabetized.");
     mnuGetAlphaPrintPut.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuGetAlphaPrintPutActionPerformed(evt);
       }
     });
     jMenu5.add(mnuGetAlphaPrintPut);
     mnuGetGroupedPrintPut.setText("Grouped by Type");
     mnuGetGroupedPrintPut.setToolTipText("Provides a text listing of the web sorted into groups by Node Type.");
     mnuGetGroupedPrintPut.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuGetGroupedPrintPutActionPerformed(evt);
       }
     });
     jMenu5.add(mnuGetGroupedPrintPut);
     jMenu3.add(jMenu5);
     jMenu8.setText("Management");
     mnuToggleRootPreserve.setFont(new Font("Dialog", 2, 12));
     mnuToggleRootPreserve.setText("Do Not Preserve First Node");
     mnuToggleRootPreserve.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuToggleRootPreserveActionPerformed(evt);
       }
     });
     jMenu8.add(mnuToggleRootPreserve);
     mnuAlphaSortManager.setText("Alpha Sort");
     mnuAlphaSortManager.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuAlphaSortManagerActionPerformed(evt);
       }
     });
     jMenu8.add(mnuAlphaSortManager);
     mnuGroupTypeManager.setText("Group by Type");
     mnuGroupTypeManager.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuGroupTypeManagerActionPerformed(evt);
       }
     });
     jMenu8.add(mnuGroupTypeManager);
     mnuSetAsRoot.setText("Set This Node as Root");
     mnuSetAsRoot.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuSetAsRootActionPerformed(evt);
       }
     });
     jMenu8.add(mnuSetAsRoot);
     jMenu3.add(jMenu8);
     jMenuBar1.add(jMenu3);
     jMenu4.setMnemonic('?');
     jMenu4.setText("Help?");
     mnuProgramHelp.setText("How to Use This");
     mnuProgramHelp.setToolTipText("Displays a text file with instructions for using the program");
     mnuProgramHelp.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuProgramHelpActionPerformed(evt);
       }
     });
     jMenu4.add(mnuProgramHelp);
     mnuAbout.setText("What is this program for?");
     mnuAbout.setToolTipText("Displays a text file that describes the purposes and orgins of the program");
     mnuAbout.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuAboutActionPerformed(evt);
       }
     });
     jMenu4.add(mnuAbout);
     mnuTypeInfo.setText("Info about Node Types");
     mnuTypeInfo.setToolTipText("Displays a text file that discusses some concerns about the Node Types");
     mnuTypeInfo.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuTypeInfoActionPerformed(evt);
       }
     });
     jMenu4.add(mnuTypeInfo);
     mnuGPL.setText("GPL/copying");
     mnuGPL.setToolTipText("Your License for this software");
     mnuGPL.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuGPLActionPerformed(evt);
       }
     });
     jMenu4.add(mnuGPL);
     mnuLicense.setText("License");
     mnuLicense.setToolTipText("Your license");
     mnuLicense.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuLicenseActionPerformed(evt);
       }
     });
     jMenu4.add(mnuLicense);
     mnuGetHelpWeb.setText("Load Help Web");
     mnuGetHelpWeb.setToolTipText("Save Your Web first");
     mnuGetHelpWeb.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuGetHelpWebActionPerformed(evt);
       }
     });
     jMenu4.add(mnuGetHelpWeb);
     jMenuBar1.add(jMenu4);
     jMenu7.setMnemonic('E');
     jMenu7.setText("Edit/View");
     mnuToggleIconVisibility.setText("Hide Type Icons");
     mnuToggleIconVisibility.setToolTipText("toggles icon visibility");
     mnuToggleIconVisibility.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuToggleIconVisibilityActionPerformed(evt);
       }
     });
     jMenu7.add(mnuToggleIconVisibility);
     jMenu9.setText("Tags/Insert");
     mnuAddMonoTags.setText("Monospaced");
     mnuAddMonoTags.setToolTipText("Insert monospacing tags at the end of the description");
     mnuAddMonoTags.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuAddMonoTagsActionPerformed(evt);
       }
     });
     jMenu9.add(mnuAddMonoTags);
     mnuAddTableTags.setText("Table Tags");
     mnuAddTableTags.setToolTipText("insert HTML tags at the end of description");
     mnuAddTableTags.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuAddTableTagsActionPerformed(evt);
       }
     });
     jMenu9.add(mnuAddTableTags);
     mnuInsertTxtFile.setText("Insert Text File");
     mnuInsertTxtFile.setToolTipText("Inserts a text file from the \"Home\" directory.");
     mnuInsertTxtFile.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuInsertTxtFileActionPerformed(evt);
       }
     });
     jMenu9.add(mnuInsertTxtFile);
     jMenu7.add(jMenu9);
     jMenu10.setText("Font Changes");
     mnuFontBigger.setText("Make Larger");
     mnuFontBigger.setToolTipText("Increase font size by 2");
     mnuFontBigger.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuFontBiggerActionPerformed(evt);
       }
     });
     jMenu10.add(mnuFontBigger);
     mnuFontSmaller.setText("Make Smaller");
     mnuFontSmaller.setToolTipText("decrease font size by 2");
     mnuFontSmaller.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuFontSmallerActionPerformed(evt);
       }
     });
     jMenu10.add(mnuFontSmaller);
     mnuMonospace.setText("Monospaced");
     mnuMonospace.setToolTipText("Toggles Monospaced view of Description text");
     mnuMonospace.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         GMBrain.this.mnuMonospaceActionPerformed(evt);
       }
     });
     jMenu10.add(mnuMonospace);
     jMenu7.add(jMenu10);
     jMenuBar1.add(jMenu7);
     setJMenuBar(jMenuBar1);
     pack();
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     setSize(new Dimension(507, 342));
     setLocation((screenSize.width - 507) / 2, (screenSize.height - 342) / 2);
   }
   // ------------------------------------------------------------------
   
   // ------------- various event listeners ----------------------------
   private void mnuInsertTxtFileActionPerformed(ActionEvent evt) {
     txtDescription.setText(txtDescription.getText() + getTxtFromHomeFile());
     viewNode(presentNode);
   }
     
   private void mnuGetHelpWebActionPerformed(ActionEvent evt) {
     loadHelpWeb();
   }
     
   private void mnuFontSmallerActionPerformed(ActionEvent evt) {
     Font presentFont = txtDescription.getFont();
     Font smallerFont = new Font(presentFont.getFamily(), 0, presentFont.getSize() - 2);
     txtDescription.setFont(smallerFont);
   }
     
   private void mnuFontBiggerActionPerformed(ActionEvent evt) {
     Font presentFont = txtDescription.getFont();
     Font biggerFont = new Font(presentFont.getFamily(), 0, presentFont.getSize() + 2);
     txtDescription.setFont(biggerFont);
   }
     
   private void mnuToggleIconVisibilityActionPerformed(ActionEvent evt) {
     iconsAreVisible = !iconsAreVisible;
     if (iconsAreVisible)
       mnuToggleIconVisibility.setText("Hide Icons");
     else
       mnuToggleIconVisibility.setText("Show Icons");
     viewNode(presentNode);
   }
     
   private void btnTableDimReadyActionPerformed(ActionEvent evt) {
     tblFrame.setVisible(false);
     generateTableTags();
     transferFocus();
   }
     
   private void mnuAddTableTagsActionPerformed(ActionEvent evt) {
     tblFrame.setLocation(getLocation());
     tblFrame.setSize(205, 95);
     tblFrame.setVisible(true);
     tblFrame.transferFocus();
   }
     
   private void mnuAddMonoTagsActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     txtDescription.setText(txtDescription.getText() + "<PRE></PRE> ");
     viewNode(presentNode);
   }
     
   private void mnuSetAsRootActionPerformed(ActionEvent evt) {
     nwManager.setNodeAsRoot(presentNode);
     statusMessage(presentNode.getTitle() + " set as root.");
   }
     
   private void mnuGroupTypeManagerActionPerformed(ActionEvent evt) {
     nwManager.groupByType(preserveRoot);
     statusMessage("Nodes grouped by type.");
   }
     
   private void mnuAlphaSortManagerActionPerformed(ActionEvent evt) {
     nwManager.alphabetize(preserveRoot);
     statusMessage("Nodes sorted by title.");
   }
     
   private void mnuToggleRootPreserveActionPerformed(ActionEvent evt) {
     preserveRoot = !preserveRoot;
     if (preserveRoot)
       mnuToggleRootPreserve.setText("Do Not Preserve First Node");
     else
       mnuToggleRootPreserve.setText("Preserve First Node");
   }
     
   private void mnuMonospaceActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     if (monospaceToggle) {
       txtDescription.setFont(new Font("Serif", 0, txtDescription.getFont().getSize()));
       mnuMonospace.setText("Monospaced");
       statusMessage("Font set to \"Serif\" ");
     } else {
       txtDescription.setFont(new Font("Monospaced", 0,txtDescription.getFont().getSize()));
       mnuMonospace.setText("Serif");
       statusMessage("Font set to \"Monospaced\" ");
     }
     monospaceToggle = !monospaceToggle;
   }
     
   private void mnuLicenseActionPerformed(ActionEvent evt) {
     showResourceTextFile("License.txt");
   }
     
   private void mnuGPLActionPerformed(ActionEvent evt) {
     showResourceTextFile("COPYING.txt");
   }
     
   private void mnuHTMLPortActionPerformed(ActionEvent evt) {
     if (presentFile == null) mnuSaveAsActionPerformed(evt);
     statusMessage("Attempting to create HTML file");
     try {
       File xmlDir = presentFile.getParentFile();
       statusMessage("Directory = " + xmlDir.getAbsolutePath());
       if (xmlDir.isDirectory()) {
         File xmlFile = new File(xmlDir, "brain.html");
         xmlFile.createNewFile();
         if (xmlFile.canWrite()) {
           FileWriter xmlOutf = new FileWriter(xmlFile);
           PrintWriter po = new PrintWriter(xmlOutf);
           po.print(presentWeb.getHTMLString());
           po.close();
           statusMessage(xmlFile.getAbsolutePath()
             + " created as brain.html in"
             + xmlDir.getAbsolutePath());
         } else statusMessage("Can't write to " + xmlFile.getAbsolutePath());
       } else statusMessage("Invalid Directory");
     } catch (IOException ie) {
         statusMessage("Error during creation of html file.");
     }
     reFresh();
   }
     
   private void mnuXMLPortActionPerformed(ActionEvent evt) {
     statusMessage("Attempting to create XML file");
     try {
       if (presentFile == null) mnuSaveAsActionPerformed(evt);
       File xmlDir = presentFile.getParentFile();
       statusMessage("Directory = " + xmlDir.getAbsolutePath());
       if (xmlDir.isDirectory()) {
         File xmlFile = new File(xmlDir, "brain.xml");
         xmlFile.createNewFile();
         if (xmlFile.canWrite()) {
           FileWriter xmlOutf = new FileWriter(xmlFile);
           PrintWriter po = new PrintWriter(xmlOutf);
           po.print(presentWeb.getXMlString());
           po.close();
           System.out.println(presentWeb.getXMlString());
           statusMessage(xmlFile.getAbsolutePath() + " created brain.xml in same directory as file");
         } else statusMessage("Can't write to " + xmlFile.getAbsolutePath());
       } else statusMessage("Invalid Directory");
     } catch (IOException ie) {
       statusMessage("Error during creation of xml file.");
     }
     reFresh();
   }
     
   private void mnuSetTypeNameActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     presentNode.setTypeName(JOptionPane.showInputDialog(getContentPane(), "Rename this type of node."));
     reFresh();
   }
     
   private void mnuCreateNewTypeActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     presentNode.setNewType(JOptionPane.showInputDialog(getContentPane(), "What is the name of the new Type?"));
     reFresh();
   }
     
   private void mnuChangeIconActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     File p = new File("unnamed");
     File former = fc.getCurrentDirectory();
     if (presentFile != null) {
       viewNode(presentNode);
       JFileChooser jfilechooser = fc;
       if (this != null) {
         /* empty */
       }
       jfilechooser.setFileSelectionMode(0);
       p = new File(presentFile.getPath());
     }
     fc.setCurrentDirectory(p);
     int answer = fc.showOpenDialog(getContentPane());
     int i = answer;
     if (this != null) {
       /* empty */
     }
     if (i == 0) {
       try {
         presentNode.setIconURL(fc.getSelectedFile().toURI().toURL());
       } catch (Exception e) {
         System.out.println("error with URL");
         statusMessage("Not a valid icon URL!");
       }
     } else statusMessage("No valid file selected.");
     fc.setCurrentDirectory(former);
     reFresh();
   }
     
   private void mnuChangeTypeActionPerformed(ActionEvent evt) {
     changePresentType();
   }
     
   private void mnuTypeInfoActionPerformed(ActionEvent evt) {
     showResourceTextFile("typestuff.txt");
   }
     
   private void mnuChangeDefaultTypeActionPerformed(ActionEvent evt) {
     Object[] options = GMNode.nodeTypes.toArray();
     Object optionSelected = options[0];
     Object nt = null;
     String title1 = "Node Type Selector";
     String message1 = "Select a Default Type for new Nodes.";
     nt = JOptionPane.showInputDialog(getContentPane(), message1, title1, 3,
            null, options, options[0]);
     if (nt != null) defaultType = nt.toString();
   }
     
   private void mnuGetGroupedPrintPutActionPerformed(ActionEvent evt) {
     showTotalPrintPut("Type");
   }
     
   private void mnuGetAlphaPrintPutActionPerformed(ActionEvent evt) {
     showTotalPrintPut("Title");
   }
     
   private void mnuGetPrintPutActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     txtHelpText.setText(presentWeb.getPrintputByTitle(presentNode.getTitle()));
     helpFrame.setSize(getSize());
     helpFrame.setLocation(getLocation());
     helpFrame.setVisible(true);
     helpFrame.transferFocus();
   }
     
   private void mnuExitActionPerformed(ActionEvent evt) {
     programClose();
   }
     
   private void mnuAboutActionPerformed(ActionEvent evt) {
     showResourceTextFile("whatthe.txt");
   }
     
   private void mnuProgramHelpActionPerformed(ActionEvent evt) {
     showResourceTextFile("Howto.txt");
   }
     
   private void mnuSaveAsActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     JFileChooser jfilechooser = fc;
     if (this != null) {
       /* empty */
     }
     jfilechooser.setFileSelectionMode(0);
     int answer = fc.showSaveDialog(getContentPane());
     int i = answer;
     if (this != null) {
       /* empty */
     }
     if (i == 0) {
       presentFile = fc.getSelectedFile();
       System.out.println(presentFile.getAbsolutePath());
       System.out.println("Writeable? =" + presentFile.canWrite());
       saveFile();
     } else statusMessage("No valid file selected.");
   }
     
   private void mnuSaveActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     if (!presentFile.exists() || presentFile == null)
       mnuSaveAs.doClick();
     saveFile();
   }
     
   private void mnuOpenActionPerformed(ActionEvent evt) {
     int answer = 0;
     viewNode(presentNode);
     answer = (JOptionPane.showConfirmDialog(getContentPane(),
       "Would you like to save the present file before proceeeding?",
       "Save?", 0, 3));
     if (answer == 0)
       mnuSaveAsActionPerformed(evt);
     fc = new JFileChooser(presentFile);
     JFileChooser jfilechooser = fc;
     if (this != null) {
       /* empty */
     }
     jfilechooser.setFileSelectionMode(0);
     answer = fc.showOpenDialog(getContentPane());
     if (answer == 0) {
       presentFile = fc.getSelectedFile();
       openFile();
     }
   }
     
   private void mnuNewActionPerformed(ActionEvent evt) {
     System.out.println("new action performed");
     viewNode(presentNode);
     int answer = (JOptionPane.showConfirmDialog(getContentPane(),
       "Would you like to save the present file before proceeeding?",
       "Save?", 0, 3));
     if (answer == 1) {
       NodeWeb n = new NodeWeb();
       loadWeb(n);
     } else {
       mnuSaveAs.doClick();
       NodeWeb n = new NodeWeb();
       loadWeb(n);
     }
     presentFile = null;
   }
     
   private void linkDisplayMouseClicked(MouseEvent evt) {
     if (evt.getClickCount() > 1
       && linkDisplay.getSelectedValue() instanceof GMNode)
         viewNode((GMNode) linkDisplay.getSelectedValue());
   }
     
   private void mnuFindLostActionPerformed(ActionEvent evt) {
     Vector orphans = presentWeb.retrieveOrphans();
     viewNode(presentNode);
     if (!orphans.isEmpty()) {
       String[] options = new String[orphans.size()];
       String optionSelected = options[0];
       String title1 = "Orphan Selector";
       String message1 = "Search for your Orphans here.";
       GMNode n = (GMNode) JOptionPane.showInputDialog(getContentPane(),
                    message1, title1, 3,
                    null, orphans.toArray(),
                    orphans.toArray()[0]);
       if (n != null) viewNode(n);
       reFresh();
     } else {
       JOptionPane.showMessageDialog(getContentPane(),
             new String("No Orphans!"));
       statusMessage("No Orphans Found!");
     }
   }
     
   private void mnuSearchForNodeActionPerformed(ActionEvent evt) {
     viewNode(presentNode);
     String[] options = new String[presentWeb.getLength()];
     options = getNodeNames(true);
     String optionSelected = options[0];
     String title1 = "Node Selector";
     String message1 = "Search for your Node here.";
     String nt = ((String)JOptionPane.showInputDialog(getContentPane(), message1, title1,
              3, null, options, options[0]));
     if (nt != null)
       viewNode(presentWeb.getNodeByName(nt));
     else
       statusMessage("no node selected");
     reFresh();
   }
     
   private void mnuSpawnNewLinkActionPerformed(ActionEvent evt) {
     spawnNode();
   }
     
   private void mnuDeLinkActionPerformed(ActionEvent evt) {
     removeLink();
   }
     
   private void mnuAddLinkActionPerformed(ActionEvent evt) {
     addNodeLink();
   }
     
   private void btnDeLinkActionPerformed(ActionEvent evt) {
     removeLink();
   }
     
   private void btnGotoNodeActionPerformed(ActionEvent evt) {
     if (linkDisplay.getSelectedValue() instanceof GMNode)
       viewNode((GMNode) linkDisplay.getSelectedValue());
     else
       statusMessage("Not a proper Node");
   }
     
   private void txtDescriptionKeyTyped(KeyEvent evt) {
     /* empty */
   }
     
   private void txtNodeTitleKeyTyped(KeyEvent evt) {
     /* empty */
   }
     
   private void btnAddLinkActionPerformed(ActionEvent evt) {
     addNodeLink();
   }
     
   private void btnSpawnActionPerformed(ActionEvent evt) {
     spawnNode();
   }
     
   private void lblNodeTypeMouseClicked(MouseEvent evt) {
     changePresentType();
   }
     
   private void exitForm(WindowEvent evt) {
     programClose();
   }
     
   public static void main(String[] args) {
     GMBrain thisProg = new GMBrain();
     System.out.println("transferFocus");
     thisProg.transferFocus();
     System.out.println("focus transferred.");
     thisProg.setVisible(true);
   }
     
   public void viewNode(GMNode n) {
     if (presentNode != null) {
       presentNode.setTitle(txtNodeTitle.getText());
       presentNode.setDescription(txtDescription.getText());
     }
     presentNode = n;
     reFresh();
   }
     
   public void spawnNode() {
     GMNode n = new GMNode("Edit your Title.", defaultType);
     presentNode.addLinkTo(n, bidirectionalLinkDefault);
     viewNode(n);
     presentWeb.addNode(n);
   }
     
   public void loadWeb(NodeWeb nw) {
     presentWeb = nw;
     nwManager.setWeb(nw);
     viewNode(presentWeb.getRoot());
     presentWeb.trimNodeTypes();
   }
   
   @SuppressWarnings("unchecked")
   public void reFresh() {
     txtNodeTitle.setText(presentNode.getTitle());
     txtDescription.setText(presentNode.getDescription());
     lblNodeType.setText(presentNode.getType());
     try {
       if (iconsAreVisible)
         lblNodeType.setIcon(presentNode.getIcon());
       else
         lblNodeType.setIcon(new ImageIcon(this.getClass().getResource("empty.gif")));
     } catch (Exception exception) {
       /* empty */
     }
     try {
       linkDisplay.setListData(presentNode.getLinks());
     } catch (Exception e) {
       System.out.println(e.getLocalizedMessage());
     }
     if (presentFile == null)
       setTitle("GM's Second Brain: This file is not saved!");
     else
       setTitle("GM's 2nd Brain: " + presentFile.getName());
     javax.swing.SwingUtilities.invokeLater(new Runnable() {
       public void run() { 
         jScrollPane2.getVerticalScrollBar().setValue(0);
       }
     });
     jScrollPane2.getViewport().setViewPosition(new Point(0,0));
     repaint();
   }
     
   public void addNodeLink() {
     viewNode(presentNode);
     String[] options = new String[presentWeb.getLength()];
     options = getNodeNames(true);
     String optionSelected = options[0];
     String nt = "none";
     String title1 = "Data Selector";
     String message1 = "Select a Node for a new Link.";
     nt = (String) JOptionPane.showInputDialog(getContentPane(), message1,
               title1, 3, null, options,
               options[0]);
     if (nt != null)
       presentNode.addLinkTo(presentWeb.getNodeByName(nt), bidirectionalLinkDefault);
     reFresh();
   }
     
   public void removeLink() {
     presentNode.unLinkFrom((GMNode) linkDisplay.getSelectedValue(), true);
     reFresh();
   }
     
   public void statusMessage(String m) {
     lblNotificationBar.setText(m);
     System.out.println(m);
   }
     
   public void saveFile() {
     try {
       if (!presentFile.exists())
     presentFile.createNewFile();
     } catch (Exception e) {
       statusMessage("Can't create file." + e.toString());
     }
     if (presentFile.canWrite()) {
       try {
         FileOutputStream fileOut = new FileOutputStream(presentFile);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(presentWeb);
       } catch (Exception e) {
         statusMessage("Error writing File." + e.toString());
       }
     }
   }
     
   public void openFile() {
     try {
       if (presentFile.exists()) {
         FileInputStream fileIn = new FileInputStream(presentFile);
         ObjectInputStream in = new ObjectInputStream(fileIn);
         Object o = in.readObject();
         if (o instanceof NodeWeb) {
           presentWeb = (NodeWeb) o;
           loadWeb(presentWeb);
         } else statusMessage("File wasn't a NodeWeb");
       } else {
         statusMessage(presentFile.getPath() + "didn't exist");
         presentWeb = new NodeWeb();
         loadWeb(presentWeb);
       }
     } catch (IOException ie) {
       statusMessage("Error opening File." + ie.toString());
     } catch (ClassNotFoundException ce) {
       statusMessage("File is not correct type.");
     } catch (Exception e) {
       System.out.println("Other error : " + e.toString());
       presentWeb = new NodeWeb();
       loadWeb(presentWeb);
     }
     setTitle("GMBrain " + presentFile.getName());
   }
     
   @SuppressWarnings("unchecked")
   public void showTotalPrintPut(String SortType) {
     Vector sortList = new Vector();
     int i = 0;
     int j = 0;
     String printput = " ";
     boolean sorted = false;
     String nl = System.getProperty("line.separator");
     viewNode(presentNode);
     for (i = 0; i < presentWeb.getLength(); i++)
       sortList.add(presentWeb.getTitleByIndex(i));
     sorted = false;
     while (!sorted) {
       sorted = true;
       for (i = 0; i < sortList.size() - 1; i++) {
         if (sortList.elementAt(i).toString().compareTo(sortList.elementAt(i + 1).toString()) >= 1) {
           Object temp = sortList.elementAt(i + 1);
           sortList.removeElementAt(i + 1);
           sortList.insertElementAt(temp, i);
           sorted = false;
         }
       }
     }
     if (SortType.equals("Title")) {
       for (i = 0; i < sortList.size(); i++)
         printput += (presentWeb.getPrintputByTitle(presentWeb.getNodeByName
                 (sortList.elementAt(i).toString()).getTitle())
                  + (String) nl + (String) nl);
     } else {
       for (j = 0; j < GMNode.nodeTypes.size(); j++) {
         printput += ((String) nl + "==" + GMNode.nodeTypes.elementAt(j)
                  + "==" + (String) nl + (String) nl);
         for (i = 0; i < sortList.size(); i++) {
           if (presentWeb.getNodeByName(sortList.elementAt(i).toString()).getType()
             .equals(GMNode.nodeTypes.elementAt(j).toString()))
               printput += (presentWeb.getPrintputByTitle(presentWeb.getNodeByName(sortList.elementAt(i).toString())
               .getTitle())) + (String) nl + (String) nl;
         }
       }
     }
     txtHelpText.setText(printput);
     helpFrame.setSize(getSize());
     helpFrame.setLocation(getLocation());
     helpFrame.setVisible(true);
     helpFrame.transferFocus();
   }
     
   public void changePresentType() {
     viewNode(presentNode);
     Object[] options = GMNode.nodeTypes.toArray();
     String optionSelected = options[0].toString();
     Object nt = presentNode.getType();
     String title1 = "Node Type Selector";
     String message1 = "Select a Type for this Node.";
     nt = JOptionPane.showInputDialog(getContentPane(), message1, title1, 3,
            null, options, options[0]);
     if (nt != null)
       presentNode.setType(nt.toString());
     reFresh();
   }
     
   public void showResourceTextFile(String f) {
     String s = "";
     String nl = System.getProperty("line.separator");
     viewNode(presentNode);
     try {
       InputStream rin = this.getClass().getResourceAsStream(f);
       DataInputStream dn = new DataInputStream(rin);
       s = "";
       int i = dn.available();
       for (i = dn.available(); i > 0; i -= 2) {
         char c = dn.readChar();
         s += c;
       }
       txtHelpText.setText(s);
     } catch (Exception e) {
       System.out.println(e.toString());
       s += e.toString();
     }
     txtHelpText.setText(s);
     helpFrame.setSize(getSize());
     helpFrame.setLocation(getLocation());
     helpFrame.setVisible(true);
     helpFrame.transferFocus();
   }
     
   public String[] getNodeNames(boolean alphabetize) {
     String[] names = new String[presentWeb.getLength()];
     int i = 0;
     for (i = 0; i < names.length; i++)
       names[i] = presentWeb.getTitleByIndex(i);
     if (alphabetize) {
       String dummy = " ";
       boolean sorted = false;
       while (!sorted) {
         sorted = true;
         for (i = 0; i < names.length - 1; i++) {
           if (names[i].compareTo(names[i + 1]) >= 1) {
             dummy = names[i];
             names[i] = names[i + 1];
             names[i + 1] = dummy;
             sorted = false;
           }
         }
       }
     }
     return names;
   }
     
   public void generateTableTags() {
     int tableWidth = 0;
     int tableHeight = 0;
     try {
       tableWidth = Integer.parseInt(txtColumns.getText());
       tableHeight = Integer.parseInt(txtRows.getText());
     } catch (Exception e) {
       statusMessage(e.toString());
     }
     String tableHTML = "<TABLE BORDER=\"1\">\n";
     if (tableHeight * tableWidth != 0) {
       for (int i = 0; i < tableHeight; i++) {
         tableHTML += "<TR>";
         for (int j = 0; j < tableWidth; j++)
           tableHTML += ("<TD>" + "R" + String.valueOf(i) + "C"
                     + String.valueOf(j) + "</TD>");
           tableHTML += "</TR>\n";
       }
       tableHTML += "</TABLE>\n";
       txtDescription.setText(txtDescription.getText() + tableHTML);
     }
     viewNode(presentNode);
   }
     
   public void setUpFile() {
     initFile = new File(System.getProperty("user.dir") + File.separator + "secbrain.ini");
     if(initFile.exists()) {
       // read init file if exists and possible
       try {
         FileInputStream fileIn = new FileInputStream(initFile);
         ObjectInputStream in = new ObjectInputStream(fileIn);
         Object f = in.readObject();
         if (f instanceof File)
           presentFile = (File) f;
         else if (f instanceof String)
           presentFile = null;
         //System.out.println("  presentFile: " + f);
         monospaceToggle = in.readBoolean();
         //System.out.println("  monospaceToggle: " + monospaceToggle);
         preserveRoot = in.readBoolean();
         //System.out.println("  preserveRoot: " + preserveRoot);
         this.setSize((Dimension) in.readObject());
         //System.out.println("  helpframe size: " + helpFrame.getSize());
         this.setLocation((Point) in.readObject());
         //System.out.println("  helpframe location: " + helpFrame.getLocation());
         txtDescription.setFont((Font) in.readObject());
         //System.out.println("  txtdescription font: " + txtDescription.getFont());
         iconsAreVisible = in.readBoolean();
         //System.out.println("  iconsAreVisible: " + iconsAreVisible);
         in.close();
         fileIn.close();
         if (presentFile == null) loadHelpWeb();
         else openFile();
       } catch (Exception e) {
         System.out.println("Error reading File." + e.toString());
         setUpDefaults();
       }
     } else {
       // else, set up defaults and continue
       setUpDefaults();
       saveInitFile();
     }
     reFresh();
   }
   
   public void setUpDefaults() {
     presentFile = null;
     monospaceToggle = false;
     preserveRoot = true;
    this.setSize(new Dimension(720, 450));
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((screenSize.width - 720) / 2, (screenSize.height - 450) / 2);
     txtDescription.setFont(new Font("Serif", 0, 12));
     iconsAreVisible = true;
     loadHelpWeb();
   }
     
   public void saveInitFile() {
     try {
       initFile.createNewFile();
     } catch (Exception e) {
       statusMessage("Can't create" + initFile.getPath() + " "
         + e.toString());
     }
     if (initFile.canWrite()) {
       try {
         FileOutputStream fileOut = new FileOutputStream(initFile);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         if (presentFile == null)
           out.writeObject(new String("No File Written"));
         else
           out.writeObject(presentFile);
         out.writeBoolean(monospaceToggle);
         out.writeBoolean(preserveRoot);
         out.writeObject(getSize());
         out.writeObject(getLocation());
         out.writeObject(txtDescription.getFont());
         out.writeBoolean(iconsAreVisible);
         statusMessage("initFile written!");
         out.close();
         fileOut.close();
       } catch (Exception e) {
         System.out.println("Error writing File." + e.toString());
       }
     } else
       statusMessage("couldn't write to " + initFile.getPath());
   }
     
   public void loadHelpWeb() {
     InputStream rin = this.getClass().getResourceAsStream("HelpBrain");
     ObjectInputStream oin = null;
     Object o = null;
     try {
       oin = new ObjectInputStream(rin);
       o = oin.readObject();
     } catch (Exception e) {
       statusMessage(e.toString());
     }
     if (o instanceof NodeWeb)
       loadWeb((NodeWeb) o);
     else
       statusMessage("HelpBrain not there!");
     reFresh();
   }
     
   public String[] getTxtFilesAtHome() {
     File home = initFile.getParentFile();
     String[] txtFiles = home.list(new FilenameFilter() {
       public boolean accept(File dir, String name) {
         return name.endsWith(".txt");
       }
     });
     return txtFiles;
   }
     
   public String getTxtFromHomeFile() {
     String[] options = getTxtFilesAtHome();
     String optionSelected = options[0];
     String title1 = "Text File Selector";
     String message1 = "Select a Text File to insert.";
     String filename = new String();
     filename = JOptionPane.showInputDialog
            (getContentPane(), message1, title1, 3, null, options,options[0])
            .toString();
     String s = "";
     String nl = System.getProperty("line.separator");
     viewNode(presentNode);
     try {
       StringBuffer stringbuffer = new StringBuffer()
           .append(initFile.getParentFile().getCanonicalPath());
       if (this != null) {
         /* empty */
       }
       filename = stringbuffer.append(File.separator).append(filename).toString();
       FileInputStream fin = new FileInputStream(filename);
       DataInputStream dn = new DataInputStream(fin);
       s = "";
       int i = dn.available();
       for (i = dn.available(); i > 0; i -= 2) {
         char c = dn.readChar();
         s += c;
       }
     } catch (Exception e) {
       System.out.println(e.toString());
       s += e.toString();
     }
     return s;
   }
     
   public void programClose() {
     saveInitFile();
     viewNode(presentNode);
     int answer = (JOptionPane.showConfirmDialog(getContentPane(),
       "Would you like to save the present file before proceeeding?",
       "Save?", 0, 3));
     System.out.println(answer);
     if (answer == 0) {
       mnuSaveAs.doClick();
       saveInitFile();
       System.exit(0);
     } else if (answer == 1)
       System.exit(0);
   }
 }
