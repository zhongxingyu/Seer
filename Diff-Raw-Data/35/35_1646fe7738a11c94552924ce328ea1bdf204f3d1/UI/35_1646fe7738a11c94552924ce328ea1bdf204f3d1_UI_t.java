 import java.io.File;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.DefaultTreeModel;
 import java.awt.dnd.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 
 
 //Comment
 public class UI extends JFrame implements DropTargetListener {
 	private static final long serialVersionUID = 1882056950753838627L;
 	private Document xml_a, xml_b;
 	private XmlTreeNode root_a, root_b;
 	private double divider_location = 0.5d;
 	
 	private DropTarget drop_a, drop_b;
 	private XmlTreeCellRenderer renderer = new XmlTreeCellRenderer();
 	
 	//Main UI
 	private JSplitPane splitPane;
 	private JScrollPane scrollPane_a, scrollPane_b;
 	private JTree tree_a, tree_b;
 	//Top Interface
 	private JPanel pn_top, pn_paths;
 	private JLabel lbl_path_a, lbl_path_b;
 	private JButton btn_file_a, btn_file_b;
 	private JTextField txtSearch;
 	//MenuBar
 	private JMenuBar menuBar;
 	private JMenu mnFile, mnAction, mnOptions, mnView, mnInformation;
 	private JMenuItem mntmCompare, mntmSearch, mntmCollapseAll, mntmExpandAll;
 	private JCheckBoxMenuItem mncb_AutoExp, mncb_regex;
 	
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		UI window = new UI();
 		try {
 			if (args.length == 2) {
 				window.loadFile(new File(args[0]), true);
 				window.loadFile(new File(args[1]), false);
 			}
 		} catch (Exception e) {
 			System.out.println("File paths not valid");
 		}		
 	}
 	
 	
 // ####################################################################
 //  | GUI INITIALIZATION
 //  V
 
 	/**
 	 * Create the application.
 	 */
 	public UI() {
 		super();
 		initWindow();
 		initMenu();
 		initTopInterface();
 		initTrees();		
 		setVisible(true);
 		splitPane.setDividerLocation(divider_location); //Has to stand after setVisible
 		
//		Icon icon = new ImageIcon(UI.class.getResource("/icons/tinyXMLCompare_64.png"));
//		String aboutGreeting = "Ein Tool der Schweizerischen Post";
//		JOptionPane.showMessageDialog(this,aboutGreeting, "tinyXMLCompare",JOptionPane.PLAIN_MESSAGE,icon);
 	}
 	/**
 	 * Initialize the Application Window (JFrame)
 	 */
 	private void initWindow() {	
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {System.out.println("Setting look an feel failed");}
 		this.setTitle("tinyXMLCompare");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(UI.class.getResource("/icons/tinyXMLCompare.png")));
 		this.setBounds(100, 100, 900, 740);
		this.setMinimumSize(new Dimension(700,450));
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.getContentPane().setLayout(new BorderLayout(0, 5));
 		addComponentListener(new ComponentAdapter() {
 			public void componentResized(ComponentEvent arg0) {
 				splitPane.setDividerLocation(divider_location);
 			}
 		});
 	}
 	/**
 	 * Initialize the MenuBar
 	 */
 	private void initMenu() {
 		menuBar = new JMenuBar();
 		this.setJMenuBar(menuBar);
 		//Menus
 		mnAction = new JMenu("Action");
 		menuBar.add(mnAction);
 		mnOptions = new JMenu("Options");
 		menuBar.add(mnOptions);
 		mnView = new JMenu("View");
 		menuBar.add(mnView);
 		mnInformation = new JMenu("Information");
 		menuBar.add(mnInformation);
 		//View
 		mntmCollapseAll = new JMenuItem("Collapse All");
 		mnView.add(mntmCollapseAll);
 		mntmExpandAll = new JMenuItem("Expand All");
 		mnView.add(mntmExpandAll);
 		//Actions
 		mntmCompare = new JMenuItem("Compare");
 		mnAction.add(mntmCompare);
 		mntmSearch = new JMenuItem("Search");
 		mnAction.add(mntmSearch);
 		//Options		
 		mncb_AutoExp = new JCheckBoxMenuItem("Automatic Expand");
 		mncb_AutoExp.setSelected(true);
 		mnOptions.add(mncb_AutoExp);		
 		mncb_regex = new JCheckBoxMenuItem("Regex Search");
 		mnOptions.add(mncb_regex);
 		
 		addMenuListeners();
 	}	
 	/**
 	 * Binds ActionListeners to the MenuItems
 	 */
 	private void addMenuListeners() {
 		//View
 		mntmCollapseAll.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				collapseAll();
 			}
 		});
 		mntmExpandAll.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				expandAll();
 			}
 		});
 		//Action
 		mntmCompare.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				compare();
 			}
 		});
 		//Options
 		mncb_regex.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (mncb_regex.getState()) {
 					txtSearch.setBackground(Color.YELLOW);
 				} else {
 					txtSearch.setBackground(Color.WHITE);
 				}
 				repaint();
 			}
 		});
 		//Information
 		mnInformation.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent arg0) {
 				System.out.println("hi");
 				showInformation();
 			}
 		});
 	}
 	/**
 	 * Initialize the Top Interface
 	 */
 	private void initTopInterface() {
 		pn_top = new JPanel();
 		this.getContentPane().add(pn_top, BorderLayout.NORTH);
 		pn_top.setLayout(new BorderLayout(5, 0));
 		//File Loading Buttons
 		btn_file_a = new JButton("");
 		btn_file_a.setIcon(new ImageIcon(UI.class.getResource("/icons/open_16.png")));
 		btn_file_a.setToolTipText("Load XML Left");
 		pn_top.add(btn_file_a, BorderLayout.WEST);
 		btn_file_b = new JButton("");
 		btn_file_b.setIcon(new ImageIcon(UI.class.getResource("/icons/open_16.png")));
 		btn_file_b.setToolTipText("Load XML Right");
 		pn_top.add(btn_file_b, BorderLayout.EAST);
 		//Path Labels and Search Field
 		pn_paths = new JPanel();
 		pn_paths.setLayout(new GridLayout(0, 3, 0, 0));
 		pn_top.add(pn_paths, BorderLayout.CENTER);
 		lbl_path_a = new JLabel("Path a");
 		lbl_path_a.setBackground(Color.WHITE);
 		pn_paths.add(lbl_path_a);
 		txtSearch = new JTextField();
 		txtSearch.setText("Search ...");
 		pn_paths.add(txtSearch);
 		lbl_path_b = new JLabel("Path b");
 		lbl_path_b.setBackground(Color.WHITE);
 		lbl_path_b.setHorizontalAlignment(SwingConstants.RIGHT);
 		pn_paths.add(lbl_path_b);
 		
 		addTopInterfaceListeners();
 	}
 	/**
 	 * Bind ActionListeners to the TopInterface
 	 */
 	private void addTopInterfaceListeners() {
 		//File Load Buttons
 		btn_file_a.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				chooseFile(true);
 			}
 		});
 		btn_file_b.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				chooseFile(false);
 			}
 		});
 		//Search Field
 		txtSearch.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
 					if (txtSearch.getText().equals("")) {
 						txtSearch.setText("Search ...");
 						renderer.setSearching(false);
 					} else {
 						renderer.setSearching(true);
 						if (XmlUtilities.find(txtSearch.getText(), mncb_regex.getState(), root_a) | XmlUtilities.find(txtSearch.getText(), mncb_regex.getState(), root_b)) {
 							collapseAll();
 							root_a.expandSearchMatches(tree_a);
 							root_b.expandSearchMatches(tree_b);						
 						}
 					}
 					repaint();
 				}
 			}
 		});
 	}
 	/**
 	 * Initialize the trees
 	 */
 	private void initTrees() {
 		//Panes
 		splitPane = new JSplitPane();
 		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent arg0) {
				if (splitPane.getWidth() - splitPane.getDividerLocation() < 30) {
					return;
				}
 				divider_location = (double) splitPane.getDividerLocation() / splitPane.getWidth();
 			}
 		});
 		this.getContentPane().add(splitPane, BorderLayout.CENTER);
 		scrollPane_a = new JScrollPane();
 		scrollPane_b = new JScrollPane();
 		splitPane.setLeftComponent(scrollPane_a);
 		splitPane.setRightComponent(scrollPane_b);
 		//Trees
 		tree_a = new JTree(new DefaultTreeModel(new XmlTreeNode("EmptyXml", null)));
 		tree_a.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		tree_a.setCellRenderer(renderer);
 		tree_a.setFocusable(true);
 		scrollPane_a.setViewportView(tree_a);
 		tree_b = new JTree(new DefaultTreeModel(new XmlTreeNode("EmptyXml", null)));
 		tree_b.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		tree_b.setCellRenderer(renderer);	
 		scrollPane_b.setViewportView(tree_b);
 		
 		addTreeListeners();
 	}
 	/**
 	 * Add action Listeners to trees
 	 */
 	private void addTreeListeners() {
 		//Expand an Collapse Listeners
 		tree_a.addTreeExpansionListener(new TreeExpansionListener() {
 			public void treeCollapsed(TreeExpansionEvent event) {
 				int id = ((XmlTreeNode)  event.getPath().getLastPathComponent()).getMatch_id();
 				if (id > 0 && mncb_AutoExp.getState()) {
 					XmlTreeNode b = XmlUtilities.getNodeByID(root_b, id);
 					if (b != null) {
 						tree_b.collapsePath(b.getPath());
 					}
 				}
 			}
 			public void treeExpanded(TreeExpansionEvent event) {
 				int id = ((XmlTreeNode)  event.getPath().getLastPathComponent()).getMatch_id();
 				if (id > 0 && mncb_AutoExp.getState()) {
 					XmlTreeNode b = XmlUtilities.getNodeByID(root_b, id);
 					if (b != null) {
 						tree_b.expandPath(b.getPath());
 					}
 				}
 			}
 		});
 		tree_b.addTreeExpansionListener(new TreeExpansionListener() {
 			public void treeCollapsed(TreeExpansionEvent event) {
 				int id = ((XmlTreeNode)  event.getPath().getLastPathComponent()).getMatch_id();
 				if (id > 0 && mncb_AutoExp.getState()) {
 					XmlTreeNode a = XmlUtilities.getNodeByID(root_a, id);
 					if (a != null) {
 						tree_a.collapsePath(a.getPath());
 					}
 				}
 			}
 			public void treeExpanded(TreeExpansionEvent event) {
 				int id = ((XmlTreeNode)  event.getPath().getLastPathComponent()).getMatch_id();
 				if (id > 0 && mncb_AutoExp.getState()) {
 					XmlTreeNode a = XmlUtilities.getNodeByID(root_a, id);
 					if (a != null) {
 						tree_a.expandPath(a.getPath());
 					}
 				}
 			}
 		});
 		//Select Listeners
 		tree_a.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent arg0) {
 				try {
 					XmlTreeNode node = (XmlTreeNode) tree_a.getLastSelectedPathComponent();
 					if (node != null) {
 						int id = node.getMatch_id();
 						if (id > 0) {
 							XmlTreeNode b = XmlUtilities.getNodeByID(root_b, id);
 							if (b != null) {
 								tree_b.setSelectionPath(b.getPath());
 							}
 						}
 					}
 				} catch (Exception e) {e.printStackTrace();}
 			}
 		});
 		tree_b.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent arg0) {
 				try {
 					XmlTreeNode node = (XmlTreeNode) tree_b.getLastSelectedPathComponent();
 					if (node != null) {
 						int id = node.getMatch_id();
 						if (id > 0) {
 							XmlTreeNode a = XmlUtilities.getNodeByID(root_a, id);
 							if (a != null) {
 								tree_a.setSelectionPath(a.getPath());
 							}
 						}
 					}
 				} catch (Exception e) {e.printStackTrace();}
 			}
 		});
 		//Drop Targets
 		tree_a.setDragEnabled(true);
 		tree_b.setDragEnabled(true);
 		drop_a = new DropTarget(tree_a, this);
 		drop_b = new DropTarget(tree_b, this);
 	}
 
 	
 // ####################################################################
 //  | HELPER METHODS FROM HERE ON
 //  V
 	
 	/**
 	 * Show FileChooseDialog and set chosen File to loadFile method
 	 * @param left
 	 */
 	private void chooseFile(boolean left) {
		System.out.println("#######################################");
//		JFileChooser fc = new JFileChooser();
//		fc.setDialogTitle("Choose a xml");
//		fc.showOpenDialog(this);
//		File f = fc.getSelectedFile();
//		loadFile(f, left);		
 	}
 	/**
 	 * Load given File into given tree
 	 * @param f File to load 
 	 * @param left if left tree is target
 	 */
 	private void loadFile(File f, boolean left) {
 		try {
 			renderer.setCompared(false);
 			if (left) {
 				lbl_path_a.setText(f.getAbsolutePath());
				lbl_path_a.setToolTipText(f.getAbsolutePath());
 				xml_a = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
 				root_a = XmlUtilities.buildTree(xml_a);
 				tree_a.setModel(new DefaultTreeModel(root_a));
 				this.repaint();
 				
 			} else {
 				lbl_path_b.setText(f.getAbsolutePath());
				lbl_path_b.setToolTipText(f.getAbsolutePath());
 				xml_b = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
 				root_b = XmlUtilities.buildTree(xml_b);
 				tree_b.setModel(new DefaultTreeModel(root_b));
 				this.repaint();
 			}
 		} catch (Exception e) {
 			System.out.println("Reading file failed for path: " + f.getAbsolutePath());
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Compares the tow loaded XML files with XmlUtilities method "matchTrees()"
 	 */
 	private void compare() {
 		renderer.setCompared(true);
 		XmlUtilities.matchTrees(root_a, root_b);
 		this.repaint();
 	}
 	
 	private void collapseAll() {
 		root_a.collapseAll(tree_a);
 		root_b.collapseAll(tree_b);
 	}
 	private void expandAll() {
 		root_a.expandAll(tree_a);
 		root_b.expandAll(tree_b);
 	}
 	
 	private void showInformation() {
 		JDialog inf = new InformationDialog(this);
 		inf.show();
 	}
 
 
 // ####################################################################
 //  | DRAG AND DROP INTERFACE IMPLEMENTATION
 //  V
 
 	@Override
 	public void dragEnter(DropTargetDragEvent arg0) {}
 	public void dragExit(DropTargetEvent arg0) {}
 	public void dragOver(DropTargetDragEvent arg0) {}
 	public void dropActionChanged(DropTargetDragEvent arg0) {}
 	public void drop(DropTargetDropEvent e) {
 		try {
 			Transferable tr = e.getTransferable();
 			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
 				e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
 				List files = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
 				if (files.size() == 1) {
 					if (e.getDropTargetContext().getDropTarget() == drop_a) {
 						loadFile((File) files.get(0), true);
 					} else if (e.getDropTargetContext().getDropTarget() == drop_b) {
 						loadFile((File) files.get(0), false);
 					}
 				} else if (files.size() > 1) {
 					loadFile((File) files.get(0), true);
 					loadFile((File) files.get(1), false);
 				}
 			}
 		} catch (Exception e2) {
 			e2.printStackTrace();
 		}
 	}
 }
