 package resources;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JComboBox;
 import javax.swing.border.BevelBorder;
 import javax.swing.DefaultComboBoxModel;
 import java.awt.Font;
 import java.awt.Color;
 
 /**
  * Class that implements main window of the program
  * 
  * @author Alex Artyomov
  *
  */
 public class DrCleanerView {
 	
 	private String[] monthArray;
    private final int MAX_MONTH = 37;
     private File selectedDest;
     private int month;
     private JComboBox<String> timeComboBox;
     private javax.swing.JLabel lblTime;
     private javax.swing.JButton browseButton;
     private javax.swing.JLabel lblDirectory;
     private javax.swing.JLabel lblDirectory2;
     private javax.swing.JPanel directoryPanel;
     private javax.swing.JCheckBox rdbtnExel;
     private javax.swing.JCheckBox rdbtnWord;
     private javax.swing.JCheckBox rdbtnPdf;
     private javax.swing.JCheckBox rdbtnPowerpoint;
     private javax.swing.JButton startButton;
     private javax.swing.JPanel timePanel;
     private javax.swing.JLabel lblType;
     private javax.swing.JPanel typePanel;
 	private JFrame frame;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					DrCleanerView window = new DrCleanerView();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public DrCleanerView() {
 		initialize();
 		selectedDest = null;
 		month = 0;
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 
 		frame = new JFrame();
 		frame.setResizable(false);
 		frame.setBounds(100, 100, 862, 642);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		
 		typePanel = new JPanel();
 		typePanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		typePanel.setBounds(10, 11, 645, 226);
 		frame.getContentPane().add(typePanel);
 		typePanel.setLayout(null);
 		
 		lblType = new JLabel("Please select documents type");
 		lblType.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 16));
 		lblType.setBounds(10, 11, 299, 29);
 		typePanel.add(lblType);
 		
 		rdbtnWord = new JCheckBox("Word");
 		rdbtnWord.setBounds(6, 47, 109, 23);
 		typePanel.add(rdbtnWord);
 		
 		rdbtnExel = new JCheckBox("Exel");
 		rdbtnExel.setBounds(6, 73, 109, 23);
 		typePanel.add(rdbtnExel);
 		
 		rdbtnPowerpoint = new JCheckBox("PowerPoint");
 		rdbtnPowerpoint.setBounds(6, 99, 109, 23);
 		typePanel.add(rdbtnPowerpoint);
 		
 		rdbtnPdf = new JCheckBox("PDF");
 		rdbtnPdf.setBounds(6, 125, 109, 23);
 		typePanel.add(rdbtnPdf);
 		
 		timePanel = new JPanel();
 		timePanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		timePanel.setBounds(10, 248, 645, 222);
 		frame.getContentPane().add(timePanel);
 		timePanel.setLayout(null);
 		
		lblTime = new JLabel("Please Choose time period (in months).");
 		lblTime.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 16));
 		lblTime.setBounds(10, 11, 260, 25);
 		timePanel.add(lblTime);
 		
 		monthArray = new String[MAX_MONTH];
 		for (int i = 1; i < MAX_MONTH; i++) 
 		{
 			monthArray[i] = (""+i);
 			monthArray[0] = "All Files";
 		}
 		ComboBoxModel<String> model = new DefaultComboBoxModel<String>(monthArray);
 		timeComboBox = new JComboBox<String>();
 		timeComboBox.setBounds(10, 47, 90, 20);
 		timeComboBox.setModel(model);
 		timeComboBox.setSelectedIndex(0);
 		timePanel.add(timeComboBox);
 		
 		
 		directoryPanel = new JPanel();
 		directoryPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		directoryPanel.setBounds(10, 481, 826, 112);
 		frame.getContentPane().add(directoryPanel);
 		directoryPanel.setLayout(null);
 		
 		lblDirectory = new JLabel("Please choose directory for search");
 		lblDirectory.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 16));
 		lblDirectory.setBounds(10, 11, 322, 25);
 		directoryPanel.add(lblDirectory);
 		
 		lblDirectory2 = new JLabel("");
 		lblDirectory2.setForeground(Color.RED);
 		lblDirectory2.setFont(new Font("Calibri", Font.PLAIN, 13));
 		lblDirectory2.setBounds(10, 68, 585, 31);
 		directoryPanel.add(lblDirectory2);
 		
 		browseButton = new JButton("Browse ...");
 		browseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {				
 				File temp = getDerctory();   
 				if(temp != null && temp.exists() && temp.isDirectory())
 			    {
 			    	lblDirectory2.setText("Choosen directory is: " + temp.getAbsolutePath());
 			    }
 			    else 
 			    	lblDirectory2.setText("");
 			}
 		});
 		browseButton.setBounds(608, 11, 208, 46);
 		directoryPanel.add(browseButton);
 		
 		startButton = new JButton("");
 		startButton.setIcon(new ImageIcon(DrCleanerView.class.getResource("123.jpg")));
 		startButton.addActionListener(new ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
                 startButtonActionPerformed(evt);
             }
 		});
 		startButton.setBounds(665, 11, 171, 459);
 		frame.getContentPane().add(startButton);
 	}
 	
 	/**
 	 * Function that executes when user clicks on start button.
 	 * @param evt - event that occurred on start button.
 	 */
 	private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
 
 	    month = timeComboBox.getSelectedIndex();
 	    if(atLeastOneSelected())
 	    {          
 	        if(selectedDest!= null)           
 	        {                
 	        	HandlerView handler = new HandlerView(this);
 	            handler.setVisible(true); 
 	        }        
 	        else        
 	        {           
 	            JOptionPane.showMessageDialog(null, "Please choose directory for search", "Error", JOptionPane.ERROR_MESSAGE);         
 	        }        
 	    }
 	    else
 	    {
 	        JOptionPane.showMessageDialog(null, "Please select at least one file type", "Error", JOptionPane.ERROR_MESSAGE);
 	    }
 	}
 	
 	/**
 	 * Function that returns true if at least one file type was selected
 	 * @return true if at least one file type was selected and false otherwise.
 	 */
 	private boolean atLeastOneSelected(){
 	    if(rdbtnWord.isSelected()||rdbtnExel.isSelected()||rdbtnPowerpoint.isSelected()
 	            || rdbtnPdf.isSelected())
 	        return true;
 	    return false;
 	}
 	
 	/**
 	 * Function that launches {@link JFileChooser} to select root directory for search.
 	 * @return Root directory for search.
 	 */
 	private File getDerctory()
 	{
 	    JFileChooser fileChooser = new JFileChooser();
 	    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 	    File file = null;
 	    int r = fileChooser.showOpenDialog(null);
 	    if(r == JFileChooser.APPROVE_OPTION)
 	        file = fileChooser.getSelectedFile();
 	    selectedDest = file;
 	    return file;	
 	}
 	/**
 	 * Getter for "word" radioButton
 	 * @return true - if "Word" type was selected.
 	 */
 	public boolean getWordStatus()
 	{
 	    return rdbtnWord.isSelected();
 	}
 
 	/**
 	 * Getter for "Exel" radioButton
 	 * @return true - if "Exel" type was selected.
 	 */
 	public boolean getExelStatus()
 	{
 	    return rdbtnExel.isSelected();
 	}
 	
 	/**
 	 * Getter for "PowerPoint" radioButton
 	 * @return true - if "PowerPoint" type was selected.
 	 */
 	public boolean getPowStatus()
 	{
 	    return rdbtnPowerpoint.isSelected();
 	}
 	/**
 	 * Getter for "PDF" radioButton
 	 * @return true - if "PDF" type was selected.
 	 */
 	public boolean getPDFStatus()
 	{
 	    return rdbtnPdf.isSelected();
 	}
 
 	/**
 	 * Getter for "_numOfMonth" value.
 	 * @return number of month that user selected for search.
 	 */
 	public int getNumOfMonth()
 	{
 	    return month;
 	}
 
 	/**
 	 * Getter for selectedDest.
 	 * @return Directory to start search from.
 	 */
 	public File getRoot()
 	{
 	    return selectedDest;
 	}
 }
