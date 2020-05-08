 package MusicCrawler;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 // The Download Manager.
 
 public class DownloadManager extends JFrame implements Observer
 {
     
     /**
 	 * 
 	 */
 	JPanelWithBackground contentPane;
 	private String MainBgPath="res/background.jpg";
 	String url;
 	private static final long serialVersionUID = 1L;
 
 	// Add download text field.
     private JTextField addTextField;
     
     // Download table's data model.
     private DownloadsTableModel tableModel;
     
     // Table listing downloads.
     private JTable table;
     
     // These are the buttons for managing the selected download.
     private JButton pauseButton, resumeButton;
     private JButton cancelButton, clearButton;
     
     // Currently selected download.
     private Download selectedDownload;
     
     // Flag for whether or not table selection is being cleared.
     private boolean clearing;
     
     // Constructor for Download Manager.
     public DownloadManager()
     {
     	setResizable(false);
         // Set application title.
         setTitle("Crawler 2.0 Download Manager");
         setDefaultCloseOperation(HIDE_ON_CLOSE);
         // Set window size.
         setMinimumSize(new Dimension(900, 650));
         contentPane = new JPanelWithBackground(MainBgPath,this.getWidth(),this.getHeight());
 		contentPane.setBackground(Color.GRAY);
 		contentPane.setForeground(Color.WHITE);
 		contentPane.setToolTipText("This window is where or download links are managed..");
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(null);
         
         // Handle window closing events.
         addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 hide();
             }
         });
         
         setContentPane(contentPane);
         // Set up file menu.
         JMenuBar menuBar = new JMenuBar();
         menuBar.setForeground(Color.BLACK);
         menuBar.setBackground(Color.LIGHT_GRAY);
         JMenu fileMenu = new JMenu("File");
         fileMenu.setForeground(Color.BLACK);
         fileMenu.setBackground(Color.LIGHT_GRAY);
         fileMenu.setMnemonic(KeyEvent.VK_F);
         JMenuItem fileExitMenuItem = new JMenuItem("Exit",
                 KeyEvent.VK_X);
         fileExitMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 actionExit();
             }
         });
         fileMenu.add(fileExitMenuItem);
         menuBar.add(fileMenu);
         setJMenuBar(menuBar);
         
         // Set up add panel.
         JPanelWithBackground addPanel = new JPanelWithBackground(MainBgPath,this.getWidth(),this.getHeight());
         
         JLabel lblNewLabel = new JLabel("Link");
         lblNewLabel.setForeground(Color.WHITE);
 		lblNewLabel.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 24));
         addPanel.add(lblNewLabel);
         
         addTextField = new JTextField(30);
         addPanel.add(addTextField);
         
         JButton addButton = new JButton("Add Download");
         addButton.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 24));
         addButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 actionAdd();
             }
         });
         addPanel.add(addButton);
         
         // Set up Downloads table.
         tableModel = new DownloadsTableModel();
         table = new JTable(tableModel);
         table.getSelectionModel().addListSelectionListener(new
                 ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 tableSelectionChanged();
             }
         });
         // Allow only one row at a time to be selected.
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
         // Set up ProgressBar as renderer for progress column.
         ProgressRenderer renderer = new ProgressRenderer(0, 100);
         renderer.setStringPainted(true); // show progress text
         table.setDefaultRenderer(JProgressBar.class, renderer);
         
         // Set table's row height large enough to fit JProgressBar.
         table.setRowHeight(
                 (int) renderer.getPreferredSize().getHeight());
         
         // Set up downloads panel.
         JPanelWithBackground downloadsPanel = new JPanelWithBackground(MainBgPath,this.getWidth(),this.getHeight());
         downloadsPanel.setBackground(Color.WHITE);
         downloadsPanel.setForeground(Color.WHITE);
         TitledBorder dP = new TitledBorder(BorderFactory.createLineBorder(Color.WHITE),"Downloads");
         dP.setTitleColor(Color.WHITE);
         
         downloadsPanel.setBorder(dP);
         downloadsPanel.setLayout(new BorderLayout());
         JScrollPane scrollPane = new JScrollPane(table);
         scrollPane.setBackground(Color.BLACK);
         scrollPane.setForeground(Color.WHITE);
         downloadsPanel.add(scrollPane,BorderLayout.CENTER);
         
         // Set up buttons panel.
         JPanelWithBackground buttonsPanel =new JPanelWithBackground(MainBgPath,this.getWidth(),this.getHeight());
         pauseButton = new JButton("Pause");
         pauseButton.setForeground(Color.BLACK);
         pauseButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 actionPause();
             }
         });
         pauseButton.setEnabled(false);
         buttonsPanel.add(pauseButton);
         resumeButton = new JButton("Resume");
         resumeButton.setForeground(Color.BLACK);
         resumeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 actionResume();
             }
         });
         resumeButton.setEnabled(false);
         buttonsPanel.add(resumeButton);
         cancelButton = new JButton("Cancel");
         cancelButton.setForeground(Color.BLACK);
         cancelButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 actionCancel();
             }
         });
         cancelButton.setEnabled(false);
         buttonsPanel.add(cancelButton);
         clearButton = new JButton("Clear");
         clearButton.setBackground(Color.WHITE);
         clearButton.setForeground(Color.BLACK);
         clearButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 actionClear();
             }
         });
         clearButton.setEnabled(false);
         buttonsPanel.add(clearButton);
         
         // Add panels to display.
         contentPane.setLayout(new BorderLayout());
         getContentPane().add(addPanel, BorderLayout.NORTH);
         getContentPane().add(downloadsPanel, BorderLayout.CENTER);
         getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
         
     }
     
     // Exit this program.
     private void actionExit() 
     {
         System.exit(0);
     }
     
     // Add a new download.
     private void actionAdd() 
     {
     	 URL verifiedUrl = verifyUrl(addTextField.getText());
     	 url=addTextField.getText();
     	 String path=showSaveFileDialog(url);
 	     if (verifiedUrl != null && !path.isEmpty()) 
 	     {
 	            tableModel.addDownload(new Download(verifiedUrl,path));
 	            addTextField.setText(""); // reset add text field
 	     } 
 	     else
 	     {
 	            JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error",JOptionPane.ERROR_MESSAGE);
 	     }
     }
     
     void actionAdd(String link)
     {
     	
     	
     			
     			 URL verifiedUrl = verifyUrl(link);
     			 url=link;
     			 String path=showSaveFileDialog(url.toString());
     		     if (verifiedUrl != null && !path.isEmpty()) 
     		     {
     		            tableModel.addDownload(new Download(verifiedUrl,path));
     		            addTextField.setText(""); // reset add text field
     		     } 
     		     else
     		     {
     		            JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error",JOptionPane.ERROR_MESSAGE);
     		     }
     }
     
     
     // Verify download URL.
     private URL verifyUrl(String url) {
         // Only allow HTTP URLs.
         if (!url.toLowerCase().startsWith("http://"))
             return null;
         
         // Verify format of URL.
         URL verifiedUrl = null;
         try {
             verifiedUrl = new URL(url);
         } catch (Exception e) {
             return null;
         }
         
         // Make sure URL specifies a file.
         if (verifiedUrl.getFile().length() < 2)
             return null;
         
         return verifiedUrl;
     }
     
     // Called when table row selection changes.
     private void tableSelectionChanged() {
     /* Unregister from receiving notifications
        from the last selected download. */
         if (selectedDownload != null)
             selectedDownload.deleteObserver(DownloadManager.this);
         
     /* If not in the middle of clearing a download,
        set the selected download and register to
        receive notifications from it. */
         if (!clearing) {
             selectedDownload =
                     tableModel.getDownload(table.getSelectedRow());
             selectedDownload.addObserver(DownloadManager.this);
             updateButtons();
         }
     }
     
     // Pause the selected download.
     private void actionPause() {
         selectedDownload.pause();
         updateButtons();
     }
     
     // Resume the selected download.
     private void actionResume() {
         selectedDownload.resume();
         updateButtons();
     }
     
     // Cancel the selected download.
     private void actionCancel() {
         selectedDownload.cancel();
         updateButtons();
     }
     
     // Clear the selected download.
     private void actionClear() {
         clearing = true;
         tableModel.clearDownload(table.getSelectedRow());
         clearing = false;
         selectedDownload = null;
         updateButtons();
     }
     
   /* Update each button's state based off of the
      currently selected download's status. */
     private void updateButtons() {
         if (selectedDownload != null) {
             int status = selectedDownload.getStatus();
             switch (status) {
                 case Download.DOWNLOADING:
                     pauseButton.setEnabled(true);
                     resumeButton.setEnabled(false);
                     cancelButton.setEnabled(true);
                     clearButton.setEnabled(false);
                     break;
                 case Download.PAUSED:
                     pauseButton.setEnabled(false);
                     resumeButton.setEnabled(true);
                     cancelButton.setEnabled(true);
                     clearButton.setEnabled(false);
                     break;
                 case Download.ERROR:
                     pauseButton.setEnabled(false);
                     resumeButton.setEnabled(true);
                     cancelButton.setEnabled(false);
                     clearButton.setEnabled(true);
                     break;
                 default: // COMPLETE or CANCELLED
                     pauseButton.setEnabled(false);
                     resumeButton.setEnabled(false);
                     cancelButton.setEnabled(false);
                     clearButton.setEnabled(true);
             }
         } else {
             // No download is selected in table.
             pauseButton.setEnabled(false);
             resumeButton.setEnabled(false);
             cancelButton.setEnabled(false);
             clearButton.setEnabled(false);
         }
     }
     
   /* Update is called when a Download notifies its
      observers of any changes. */
     public void update(Observable o, Object arg) 
     {
         // Update buttons if the selected download has changed.
         if (selectedDownload != null && selectedDownload.equals(o))
             updateButtons();
     }
     
     String showSaveFileDialog(String Url)
 	{
     	String defaultPath="";
 		JFileChooser fileChooser = new JFileChooser();
 		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		fileChooser.setDialogTitle("Save "+Url.substring(Url.lastIndexOf('/')+1)+" as ....");
 		fileChooser.setPreferredSize(new Dimension(500,400));
 		fileChooser.setCurrentDirectory(new File(Url.substring(Url.lastIndexOf('/')+1)));
 		try 
 		{
			defaultPath= "" + new java.io.File(".").getCanonicalPath()+Url.substring(Url.lastIndexOf('/'));
 			System.out.println("default path is "+defaultPath);
 		} 
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int userSelection = fileChooser.showSaveDialog(this);
 		fileChooser.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 23));
 		
 		if (userSelection == JFileChooser.APPROVE_OPTION)
 		{
 			File fileToSave = fileChooser.getSelectedFile();
 			return fileToSave.getAbsolutePath();
 		}
 		
 		return defaultPath;
 	}
 }
