 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 
 public class SelectionView extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2451946204670756691L;
 	
 	//Creates all needed JMenuItems
 	private JMenuItem jmiLoad = new JMenuItem("Load Scholarship");
 	private JMenuItem jmiSave = new JMenuItem("Save Scholarship");
 	private JMenuItem jmiExit = new JMenuItem("Exit");
 	private JMenuItem jmiTP = new JMenuItem("Type of Publicataion");
 	private JMenuItem jmiPY = new JMenuItem("Publications Per Year");
 	private JMenuItem jmiCPY = new JMenuItem("Conference Papers Per Year");
 	private JMenuItem jmiJAY = new JMenuItem("Journal Articles Per Year");
 	private JMenuItem jmiCA = new JMenuItem("Coauthors Per Publication");
 	
 	//Creates all JMenus and JMenuBar
 	private JMenuBar jmBar = new JMenuBar();
 	private JMenu jmFile = new JMenu("File");
 	private JMenu jmPlot = new JMenu("Plot");
 	
 	//Creates all JButtons
 	private JButton jbAddScholar = new JButton("Add Scholar");
 	
 	
 
 	private JButton jbDeleteSelectedScholar = new JButton("Delete Selected Scholar(s)");
 	private JButton jbDeleteAllScholar = new JButton("Delete All Scholars");
 	private JButton jbAddSerial = new JButton("Add Serial");
 	private JButton jbDeleteSelectedSerial = new JButton("Delete Selected Serial(s)");
 	private JButton jbDeleteAllSerial = new JButton("Delete All Serials");
 	private JButton jbAddPaper = new JButton("Add Paper");
 	private JButton jbDeleteSelectedPaper = new JButton("Delete Selected Paper(s)");
 	private JButton jbDeleteAllPaper = new JButton("Delete All Papers");
 	
 	//Creates all JLists 
	private JList jlScholars = new JList();
	private JList jlSerials = new JList();
	private JList jlPapers = new JList();
 
 	//Creates all JScrollPanes
 	private JScrollPane jspScholars = new JScrollPane(jlScholars);
 	private JScrollPane jspSerials = new JScrollPane(jlSerials);
 	private JScrollPane jspPapers = new JScrollPane(jlPapers);
 	
 	//Creates all JLabels
 	private JLabel jlblScholar = new JLabel("Scholar");
 	private JLabel jlblSerial = new JLabel("Serial");
 	private JLabel jlblPaper = new JLabel("Paper");
 
 	
 	// Creates a JFileChooser
 	private JFileChooser jFileChooser = new JFileChooser(new File("."));
 	
 	private ScholarshipModel model;
 
 	/**
 	 *  Will fill the frame with content and hold the addActionListeners
 	 */
 	public SelectionView(){
 		
 		setTitle("Add New Academic Papers to Your List of Academic Papers");
 
 		JPanel jplScholar = new JPanel(new GridLayout(3, 1, 2, 2));
 		JPanel scholarLabelPanel = new JPanel();
 		JPanel scholarButtonPanel = new JPanel(new GridLayout(3, 1));
 		JPanel scholarAddPanel = new JPanel();
 		JPanel scholarDelPanel = new JPanel();
 		JPanel scholarDelAllPanel = new JPanel();
 		scholarAddPanel.add(jbAddScholar);
 		scholarDelPanel.add(jbDeleteSelectedScholar);
 		scholarDelAllPanel.add(jbDeleteAllScholar);
 		scholarButtonPanel.add(scholarAddPanel);
 		scholarButtonPanel.add(scholarDelPanel);
 		scholarButtonPanel.add(scholarDelAllPanel);
 		scholarLabelPanel.add(jlblScholar);
 		jplScholar.add(scholarLabelPanel);
 		jplScholar.add(jspScholars);
 		jplScholar.add(scholarButtonPanel);
 
 		
 		JPanel jplSerial = new JPanel(new GridLayout(3, 1, 2, 2));
 		JPanel serialLabelPanel = new JPanel();
 		JPanel serialButtonPanel = new JPanel(new GridLayout(3, 1));
 		JPanel serialAddPanel = new JPanel();
 		JPanel serialDelPanel = new JPanel();
 		JPanel serialDelAllPanel = new JPanel();
 		serialAddPanel.add(jbAddSerial);
 		serialDelPanel.add(jbDeleteSelectedSerial);
 		serialDelAllPanel.add(jbDeleteAllSerial);
 		serialButtonPanel.add(serialAddPanel);
 		serialButtonPanel.add(serialDelPanel);
 		serialButtonPanel.add(serialDelAllPanel);
 		serialLabelPanel.add(jlblSerial);
 		jplSerial.add(serialLabelPanel);
 		jplSerial.add(jspSerials);
 		jplSerial.add(serialButtonPanel);
 
 		
 		JPanel jplPaper = new JPanel(new GridLayout(3, 1, 2, 2));
 		JPanel paperLabelPanel = new JPanel();
 		JPanel paperButtonPanel = new JPanel(new GridLayout(3, 1));
 		JPanel paperAddPanel = new JPanel();
 		JPanel paperDelPanel = new JPanel();
 		JPanel paperDelAllPanel = new JPanel();
 		paperAddPanel.add(jbAddPaper);
 		paperDelPanel.add(jbDeleteSelectedPaper);
 		paperDelAllPanel.add(jbDeleteAllPaper);
 		paperButtonPanel.add(paperAddPanel);
 		paperButtonPanel.add(paperDelPanel);
 		paperButtonPanel.add(paperDelAllPanel);
 		paperLabelPanel.add(jlblPaper);
 		jplPaper.add(paperLabelPanel);
 		jplPaper.add(jspPapers);
 		jplPaper.add(paperButtonPanel);
 		
 		
 		jmFile.add(jmiLoad);
 		jmFile.add(jmiSave);
 		jmFile.add(jmiExit);
 		
 		jmPlot.add(jmiTP);
 		jmPlot.add(jmiPY);
 		jmPlot.add(jmiCPY);
 		jmPlot.add(jmiJAY);
 		jmPlot.add(jmiCA);
 		
 		jmBar.add(jmFile);
 		jmBar.add(jmPlot);
 
 		// Set up the content pane and add all the panels to it.
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new GridLayout(1, 3, 5, 5));
 		setJMenuBar(jmBar);
 		add(jplScholar);
 		add(jplSerial);
 		add(jplPaper);
 		pack();
 		setVisible(true);
 		
 	}
 	
 	/**
 	 * Loads scholarship from binary file
 	 * @param filename
 	 */
 	private void loadFile(String filename){
 		//TODO 
 		ScholarshipModel  model = new ScholarshipModel();
 		
 		try {
 			model.loadScholarship(filename);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * Saves the scholarship to binary file
 	 */
 	private void saveFile(){
 		
 	}
 	
 	/**
 	 * Saves the scholarship using a specified binary file
 	 * @param filename
 	 */
 	private void saveFile(String filename){
 		
 	}
 	
 	/**
 	 * Opens a graph given the type of graph and scholar
 	 * @param typeOfGraph type of graph to open
 	 * @param scholar scholar who's graph to open
 	 */
 	private void openGraph(String typeOfGraph, String scholar, HashMap<String, Scholar> scholars){
 		Graph display = new Graph(typeOfGraph, scholar, scholars);
 	}
 	
 
 	public ScholarshipModel getModel() {
 		return model;
 	}
 
 	public void setModel(ScholarshipModel model) {
 		this.model = model;
 		
 		if (this.model != null) {
 			// Register the view as listener for the model
 			model.addActionListener(this);
 	}
 	
 	public JButton getJbAddScholar() {
 		return jbAddScholar;
 	}
 
 	public JButton getJbDeleteSelectedScholar() {
 		return jbDeleteSelectedScholar;
 	}
 
 	public JButton getJbDeleteAllScholar() {
 		return jbDeleteAllScholar;
 	}
 
 	public JButton getJbAddSerial() {
 		return jbAddSerial;
 	}
 
 	public JButton getJbDeleteSelectedSerial() {
 		return jbDeleteSelectedSerial;
 	}
 
 	public JButton getJbDeleteAllSerial() {
 		return jbDeleteAllSerial;
 	}
 
 	public JButton getJbAddPaper() {
 		return jbAddPaper;
 	}
 
 	public JButton getJbDeleteSelectedPaper() {
 		return jbDeleteSelectedPaper;
 	}
 
 	public JButton getJbDeleteAllPaper() {
 		return jbDeleteAllPaper;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 	
 }
