 package main.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.MenuItem;
 import java.awt.Panel;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.FlowLayout;
 import javax.swing.JSplitPane;
 import javax.swing.BoxLayout;
 import java.awt.Dimension;
 import javax.swing.JScrollPane;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.UIManager;
 import java.awt.Font;
 import javax.swing.JSeparator;
 import java.awt.SystemColor;
 import javax.swing.JList;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.io.ObjectOutputStream.PutField;
 import java.util.ArrayList;
 
 import javax.swing.JTabbedPane;
 import javax.swing.ListSelectionModel;
 
 import main.classes.DrugData;
 import main.classes.DrugSearchController;
 import main.classes.RemoteDataAccess;
 import main.classes.SearchResult;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 public class FrmMain extends JFrame {
 	private JTextField txtSearch;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					UIManager
 							.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
 					FrmMain frame = new FrmMain();
 					frame.setVisible(true);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public FrmMain() {
 		setTitle("Pharmashup");
 		setSize(new Dimension(1024, 768));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocationRelativeTo(null);
 		getContentPane().setLayout(
 				new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
 		getRootPane().putClientProperty("JRootPane.MenuInTitle", Boolean.TRUE);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setFont(new Font("Ubuntu Medium", Font.BOLD, 13));
 		JMenu aboutMenu = new JMenu("Help");
 		JMenuItem aboutItem = new JMenuItem("About");
 		aboutMenu.add(aboutItem);
 
 		menuBar.add(aboutMenu);
 		setJMenuBar(menuBar);
 
 		JSplitPane splitPane = new JSplitPane();
 		splitPane.setDividerSize(5);
 
 		getContentPane().add(splitPane);
 
 		JSplitPane paneLeft = new JSplitPane();
 		paneLeft.setName("");
 		paneLeft.setMinimumSize(new Dimension(254, 760));
 		paneLeft.setMaximumSize(new Dimension(254, 760));
 		paneLeft.setDividerSize(5);
 		paneLeft.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		splitPane.setLeftComponent(paneLeft);
 
 		JSplitPane paneLeftBottom = new JSplitPane();
 		paneLeftBottom.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		paneLeft.setRightComponent(paneLeftBottom);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBackground(UIManager.getColor("MenuBar.borderColor"));
 		scrollPane.setMinimumSize(new Dimension(22, 180));
 		paneLeftBottom.setLeftComponent(scrollPane);
 
 		JLabel lblResults = new JLabel("Results");
 		lblResults.setForeground(SystemColor.text);
 		lblResults.setBackground(UIManager
 				.getColor("ArrowButton[Pressed].foreground"));
 		lblResults.setFont(new Font("Droid Sans", Font.BOLD, 15));
 		scrollPane.setColumnHeaderView(lblResults);
 
 		final JList listSearchResults = new JList();
 		listSearchResults.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 			
 				if (listSearchResults.getSelectedValuesList().size() > 0)
 				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
 					{
 						DrugSearchController controller = new DrugSearchController();
 						DrugData resultData = controller.getDrugData((SearchResult)listSearchResults.getSelectedValue());
 					}
 						
 				}
 					
 				
 			}
 		});
 		listSearchResults.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			
				if (e.getClickCount() == 2)
 				{
 					DrugSearchController controller = new DrugSearchController();
 					DrugData resultData = controller.getDrugData((SearchResult)listSearchResults.getSelectedValue());
 				}
 			
 			}
 		});
 		listSearchResults.setFont(new Font("Ubuntu Light", Font.BOLD, 13));
 		listSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		listSearchResults.setVisibleRowCount(100);
 		listSearchResults.setBackground(SystemColor.window);
 
 		scrollPane.setViewportView(listSearchResults);
 
 		JScrollPane scrollPane_1 = new JScrollPane();
 		paneLeftBottom.setRightComponent(scrollPane_1);
 
 		JLabel lblRecentSearches = new JLabel("Recent Searches");
 		lblRecentSearches.setForeground(SystemColor.text);
 		lblRecentSearches.setBackground(SystemColor.activeCaption);
 		lblRecentSearches.setFont(new Font("Droid Sans", Font.BOLD, 15));
 		scrollPane_1.setColumnHeaderView(lblRecentSearches);
 
 		JList listRecentSearches = new JList();
 		listRecentSearches.setBackground(SystemColor.window);
 		scrollPane_1.setViewportView(listRecentSearches);
 		paneLeftBottom.setDividerLocation(340);
 
 		JPanel panel_1 = new JPanel();
 		panel_1.setBackground(UIManager.getColor("Separator.shadow"));
 		paneLeft.setLeftComponent(panel_1);
 		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"), }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
 
 		txtSearch = new JTextField();
 		txtSearch.addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					try {
 						DrugSearchController controller = new DrugSearchController();
 						ArrayList<SearchResult> resultList = controller
 								.searchDrug(txtSearch.getText());
 
 					    
 						if (resultList.isEmpty())
 						{
 							DefaultListModel<String> model = new DefaultListModel<String>();
 							model.addElement("No result found");
 							listSearchResults.setModel(model);
 							
 						}
 						else
 						{
 							DefaultListModel<SearchResult> model = new DefaultListModel<SearchResult>();
 							for (SearchResult s : resultList)
 								model.addElement(s);
 							
 							listSearchResults.setModel(model);
 							
 						}
 						
 					} catch (Exception ex) {
 						JOptionPane.showMessageDialog(null, ex.getMessage());
 					}
 				}
 			}
 		});
 		txtSearch.putClientProperty("JTextField.variant", "search");
 		txtSearch.putClientProperty("JTextField.Search.PlaceholderText",
 				"Enter Drug Name Here");
 
 		txtSearch.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// if (txtSearch.getText().compareTo("Search...") == 0)
 				// txtSearch.setText("");
 			}
 		});
 
 		txtSearch.setPreferredSize(new Dimension(10, 25));
 		panel_1.add(txtSearch, "2, 2, 11, 1, fill, fill");
 		txtSearch.setColumns(10);
 		paneLeft.setDividerLocation(40);
 
 		JSplitPane paneRight = new JSplitPane();
 		paneRight.setBackground(UIManager.getColor("MenuBar.borderColor"));
 		paneRight.setDividerSize(5);
 		paneRight.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		splitPane.setRightComponent(paneRight);
 
 		JScrollPane scrollPane_2 = new JScrollPane();
 		scrollPane_2.setBackground(UIManager.getColor("Separator.shadow"));
 		paneRight.setRightComponent(scrollPane_2);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setFont(new Font("Droid Sans", Font.BOLD, 15));
 		tabbedPane.setBackground(SystemColor.window);
 		tabbedPane.addTab("General Info", null, new pnlGeneral());
 		tabbedPane.addTab("Pharmacokinetics", null);
 		tabbedPane.addTab("Pharmacodynamics", null);
 		tabbedPane.addTab("Clinical Trials", null);
 		scrollPane_2.setViewportView(tabbedPane);
 		
 		
 		JPanel panel = new JPanel();
 		paneRight.setLeftComponent(panel);
 		panel.setLayout(new BorderLayout(0, 0));
 
 		JLabel lblLbldrugname = new JLabel("lblDrugName");
 		lblLbldrugname.setForeground(SystemColor.activeCaption);
 		lblLbldrugname.setFont(new Font("Ubuntu Medium", Font.BOLD, 20));
 		panel.add(lblLbldrugname, BorderLayout.CENTER);
 		paneRight.setDividerLocation(40);
 		// setBounds(100, 100, 682, 537);
 	}
 
 }
