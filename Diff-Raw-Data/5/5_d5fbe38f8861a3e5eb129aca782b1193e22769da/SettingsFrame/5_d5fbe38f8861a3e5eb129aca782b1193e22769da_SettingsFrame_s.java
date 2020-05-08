 package display;
 
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JPanel;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.pushingpixels.substance.api.SubstanceLookAndFeel;
 import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;
 
 import settings.SystemSettings;
 
 import javax.swing.JSplitPane;
 
 public class SettingsFrame extends JFrame {
 	
 	private JTabbedPane tabbedPane;
 	private JTextField txtDicomDirectory;
 	private JTextField niftiField;
 	private JTextField txtBufferdir;
 	private JTextField txtTempdir;
 	private JTextField txtServerDir;
 	public SettingsFrame() {
 		getContentPane().setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));
 		
 		JSplitPane splitPane = new JSplitPane();
 		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		getContentPane().add(splitPane);
 		
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		splitPane.setLeftComponent(tabbedPane);
 		
 		JPanel filesSettingPanel = new JPanel();
 		JPanel serverSettingPanel = new JPanel();
 		tabbedPane.addTab("Server", null, serverSettingPanel, null);
 		serverSettingPanel.setLayout(new MigLayout("", "[][grow][]", "[]"));
 		
 		JLabel lblRootServerDirectory = new JLabel("Root server directory");
 		serverSettingPanel.add(lblRootServerDirectory, "cell 0 0,alignx trailing");
 		
 		txtServerDir = new JTextField(SystemSettings.SERVER_INFO.getServerDir().toString());
 		txtServerDir.setToolTipText("Directory of the data server ( Default : J:/ )");
 		serverSettingPanel.add(txtServerDir, "cell 1 0,growx");
 		txtServerDir.setColumns(10);
 		ImageIcon icon=new ImageIcon(MainWindow.class.getResource("/images/folder.png"));
 		Image img = icon.getImage();  
 		Image newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
 		ImageIcon icon2 = new ImageIcon(newimg); 
 		
 		JButton btnSelectserverdir = new JButton(icon2);
 		serverSettingPanel.add(btnSelectserverdir, "cell 2 0");
 		tabbedPane.addTab("Converter", null, filesSettingPanel, null);
 		filesSettingPanel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][]"));
 		
 		JLabel lblDicomDirectory = new JLabel("Dicom directory ");
 		filesSettingPanel.add(lblDicomDirectory, "cell 0 0,alignx left");
 		
 		txtDicomDirectory = new JTextField(SystemSettings.SERVER_INFO.getDicomDir().toString());
 		txtDicomDirectory.setToolTipText("Directory where the dicom images will be saved.");
 		filesSettingPanel.add(txtDicomDirectory, "cell 1 0,growx");
 		txtDicomDirectory.setColumns(10);
 		
 		
 		
 		JButton btnSelectdicomdir = new JButton(icon2);
 		filesSettingPanel.add(btnSelectdicomdir, "cell 2 0");
 		
 		JLabel lblNiftiDirectry = new JLabel("Nifti directory");
 		filesSettingPanel.add(lblNiftiDirectry, "cell 0 1,alignx left");
 		
 		niftiField = new JTextField(SystemSettings.SERVER_INFO.getNiftiDir().toString());
 		niftiField.setToolTipText("Directory where nifti files will be saved.");
 		filesSettingPanel.add(niftiField, "cell 1 1,growx");
 		niftiField.setColumns(10);
 		
 		JButton niftiSelectbutton = new JButton(icon2);
 		filesSettingPanel.add(niftiSelectbutton, "cell 2 1");
 		
 		JLabel lblBufferDirectory = new JLabel("Buffer directory");
 		filesSettingPanel.add(lblBufferDirectory, "cell 0 2,alignx left");
 		
 		txtBufferdir = new JTextField(SystemSettings.SERVER_INFO.getIncomingDir().toString());
 		filesSettingPanel.add(txtBufferdir, "cell 1 2,growx");
 		txtBufferdir.setColumns(10);
 		
 		JButton btnbufferselect = new JButton(icon2);
 		filesSettingPanel.add(btnbufferselect, "cell 2 2");
 		
 		JLabel lblTempDirectory = new JLabel("Temp directory");
 		filesSettingPanel.add(lblTempDirectory, "cell 0 3,alignx left");
 		
 		txtTempdir = new JTextField(SystemSettings.SERVER_INFO.getTempDir().toString());
 		txtTempdir.setToolTipText("Directory for temp files.");
 		filesSettingPanel.add(txtTempdir, "cell 1 3,growx");
 		txtTempdir.setColumns(10);
 		
 		JButton btnSelecttemp = new JButton(icon2);
 		filesSettingPanel.add(btnSelecttemp, "cell 2 3");
 		
 		JPanel panel = new JPanel();
 		splitPane.setRightComponent(panel);
 		panel.setLayout(new MigLayout("", "[57px,grow][grow]", "[23px,grow]"));
 		
 		JButton btnSave = new JButton("Save");
 		panel.add(btnSave, "cell 0 0,grow");
 		
 		JButton btnClose = new JButton("Close");
 		panel.add(btnClose, "cell 1 0,grow");
 		
 		btnClose.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 			}
 		});
 		btnSave.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				SystemSettings.SERVER_INFO.setIncomingDir(txtBufferdir.getText());
 				SystemSettings.SERVER_INFO.setDicomDir(txtDicomDirectory.getText());
 				SystemSettings.SERVER_INFO.setNiftiDir(niftiField.getText());
 				SystemSettings.SERVER_INFO.setTempDir(txtTempdir.getText());
 				SystemSettings.SERVER_INFO.setServerDir(txtServerDir.getText());
 				SystemSettings.SERVER_INFO.saveConfiguration();
 				dispose();
 				
 			}
 		});
 		btnbufferselect.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getIncomingDir().toString());
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				int retval = fc.showOpenDialog(SettingsFrame.this);
 	            if (retval == JFileChooser.APPROVE_OPTION) {
 	            	File file = fc.getSelectedFile();
 	            	txtBufferdir.setText(file.getAbsolutePath());
 	            }
 			}
 		});
 		btnSelectdicomdir.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getDicomDir().toString());
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				int retval = fc.showOpenDialog(SettingsFrame.this);
 	            if (retval == JFileChooser.APPROVE_OPTION) {
 	            	File file = fc.getSelectedFile();
 	            	txtDicomDirectory.setText(file.getAbsolutePath());
 	            }
 			}
 		});
 		btnSelecttemp.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getTempDir().toString());
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				int retval = fc.showOpenDialog(SettingsFrame.this);
 	            if (retval == JFileChooser.APPROVE_OPTION) {
 	            	File file = fc.getSelectedFile();
	            	txtDicomDirectory.setText(file.getAbsolutePath());
 	            }
 			}
 		});
 		niftiSelectbutton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getNiftiDir().toString());
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				int retval = fc.showOpenDialog(SettingsFrame.this);
 	            if (retval == JFileChooser.APPROVE_OPTION) {
 	            	File file = fc.getSelectedFile();
	            	txtDicomDirectory.setText(file.getAbsolutePath());
 	            }
 			}
 		});
 		btnSelectserverdir.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getServerDir().toString());
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				int retval = fc.showOpenDialog(SettingsFrame.this);
 	            if (retval == JFileChooser.APPROVE_OPTION) {
 	            	File file = fc.getSelectedFile();
 	            	txtServerDir.setText(file.getAbsolutePath());
 	            }
 			}
 		});
 	}
 
 	/**
 	 * i correspond a l'onglet actif par defaut
 	 * @param i
 	 */
 	public SettingsFrame(int i) {
 		this();
 		tabbedPane.setSelectedIndex(i);
 	}
 
 	public void createAndShowGUI(){
 		JFrame.setDefaultLookAndFeelDecorated(true);
 		try {
 	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
         } catch (Exception e) {
           System.out.println("Substance Graphite failed to initialize");
         }
 		UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
 		setTitle("Settings");
 		setSize(400, 250);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setIconImage(new ImageIcon(this.getClass().getResource("/images/mainicon.png")).getImage());
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 	public static void main(String args[]){
 
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run(){
 				JFrame.setDefaultLookAndFeelDecorated(true);
 				try {
 			          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
 		        } catch (Exception e) {
 		          System.out.println("Substance Graphite failed to initialize");
 		        }
 				UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
 				JFrame jf = new JFrame("Settings");
 				jf.getContentPane().add(new SettingsFrame());
 				jf.setSize(400, 250);
 				jf.setIconImage(new ImageIcon(this.getClass().getResource("/images/mainicon.png")).getImage());
 				jf.setLocationRelativeTo(null);
 				jf.setVisible(true);
 				
 				
 			}
 		});
 	}
 }
