 package gr.uoi.cs.daintiness.hecate.gui.swing;
 
 import gr.uoi.cs.daintiness.hecate.Hecate;
 import gr.uoi.cs.daintiness.hecate.diff.Delta;
 import gr.uoi.cs.daintiness.hecate.parser.HecateParser;
 import gr.uoi.cs.daintiness.hecate.sql.Schema;
 import gr.uoi.cs.daintiness.hecate.transitions.Deletion;
 import gr.uoi.cs.daintiness.hecate.transitions.Insersion;
 import gr.uoi.cs.daintiness.hecate.transitions.TransitionList;
 import gr.uoi.cs.daintiness.hecate.transitions.Transitions;
 import gr.uoi.cs.daintiness.hecate.transitions.Update;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 
 import org.antlr.v4.runtime.RecognitionException;
 
 /**
  * The main window of Hecate
  * @author giskou
  *
  */
 @SuppressWarnings("serial")
 public class MainWindow extends JFrame{
 	
 	private Schema oldSchema;
 	private Schema newSchema;
 	
 	private MainPanel mainPanel;
 	private JMenuBar menuBar;
 	
 	// menu items
 	private JMenu file;
 	private JMenuItem fileOpen;
 	private JMenuItem fileFOpen;
 	private JMenuItem fileClose;
 	private JMenu view;
 	private JMenuItem viewMetrics;
 	private JMenu help;
 	private JMenuItem helpAbout;
 	
 	private OpenDialog openDialog;
 	private OpenFolderDialog openFolderDialog;
 	private MetricsDialog metricsDialog;
 	private AboutDialog aboutDialog;
 	private Image hecateIcon;
 	
 	private Delta delta;
 	
 	/**
 	 * Default Constructor
 	 */
 	public MainWindow(){
 		initialize();
 		
 		createMenu();
 		mainPanel = new MainPanel();
 		add(mainPanel);
 		
 		draw();
 	}
 	
 	/**
 	 * Sets
 	 * <li>the look and feel of the application
 	 * <li>the window title
 	 * <li>the window icon
 	 * <li>the default close operation of the window
 	 */
 	private void initialize() {
 		// look and feel
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		setTitle("Hecate");
 		hecateIcon = new ImageIcon(Hecate.class.getResource("art/icon.png")).getImage();
 		this.setIconImage(hecateIcon);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 	
 	/**
 	 * Creates the windows menu
 	 */
 	private void createMenu() {
 		menuBar = new JMenuBar();
 		// File
 		file = new JMenu("File");
 		file.setMnemonic(KeyEvent.VK_F);
 		// File->Open...
 		fileOpen = new JMenuItem("Open...");
 		fileOpen.setMnemonic(KeyEvent.VK_O);
 		fileOpen.setToolTipText("Create new diff tree");
 		fileOpen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				if (openDialog == null) {
 					openDialog = new OpenDialog();
 				}
 				openDialog.setVisible(true);
 				if (openDialog.getStatus() != 0) {
 					try {
 						drawTree(openDialog.getOldFile(), openDialog.getNewFile());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (RecognitionException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		file.add(fileOpen);
 		//File->Open Folder
 		fileFOpen = new JMenuItem("Open Folder...");
 		fileFOpen.setMnemonic(KeyEvent.VK_F);
 		fileFOpen.setToolTipText("Create diff tree from multiple files");
 		fileFOpen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				Transitions trs = new Transitions();
 				if (openFolderDialog == null) {
 					openFolderDialog = new OpenFolderDialog();
 				}
 				openFolderDialog.setVisible(true);
 				if (openFolderDialog.getStatus() != 0) {
 					String path = openFolderDialog.getFolder();
 					File dir = new File(path);
 					
 					String[] list = dir.list();
 					java.util.Arrays.sort(list);
 					
 					try {
 						BufferedWriter metrics = new BufferedWriter(new FileWriter("metrics"));
 						
 						for (int i = 0; i < list.length-1; i++)  {
 							try {
 								Schema schema = HecateParser.parse(path + File.separator + list[i]);
 								Schema schema2 = HecateParser.parse(path + File.separator + list[i+1]);
 								Delta delta = new Delta();
 								trs.add(delta.minus(schema, schema2));
 								System.out.println(list[i] + "-" + list[i+1]);
 								if (i == 0) {
									metrics.write("version-to-version\t\toldT\tnewT\toldA\tnewA\ttIns\ttDel\taIns\taDel\ttotAl\n");
 								}
 								metrics.write(
 									list[i] + "-" + list[i+1] + "\t" +
 									delta.getOldSizes()[0] + "\t" +
 									delta.getNewSizes()[0] + "\t" +
 									delta.getOldSizes()[1] + "\t" +
 									delta.getNewSizes()[1] + "\t" +
 									delta.getTableMetrics()[0] + "\t" +
 									delta.getTableMetrics()[1] + "\t" +
 									delta.getAttributeMetrics()[0] + "\t" +
 									delta.getAttributeMetrics()[1] + "\t" +
 									delta.getTotalMetrics()[2] +
 									"\n"
 								);
 
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} catch (RecognitionException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 						marshal(trs);
 						metrics.close();
 						try {
 							drawTree(new File(path + File.separator + list[0]), new File(path + File.separator + list[list.length-1]));
 						} catch (RecognitionException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		file.add(fileFOpen);
 		// File->Close
 		fileClose = new JMenuItem("Close");
 		fileClose.setMnemonic(KeyEvent.VK_C);
 		
 		fileClose.setToolTipText("Exit application");
 		fileClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				System.exit(0);
 			}
 		});
 		file.add(fileClose);
 		menuBar.add(file);
 
 		// View
 		view = new JMenu("View");
 		view.setMnemonic(KeyEvent.VK_V);
 		// View->Metrics...
 		viewMetrics = new JMenuItem("Metrics...");
 		viewMetrics.setMnemonic(KeyEvent.VK_M);
 		viewMetrics.setToolTipText("View diff Metrics");
 		viewMetrics.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				metricsDialog = new MetricsDialog(delta);
 				metricsDialog.setVisible(true);
 			}
 		});
 		view.add(viewMetrics);
 		menuBar.add(view);
 
 		// Help
 		help = new JMenu("Help");
 		help.setMnemonic(KeyEvent.VK_H);
 		// Help->About
 		helpAbout = new JMenuItem("About");
 		helpAbout.setMnemonic(KeyEvent.VK_A);
 		helpAbout.setToolTipText("About Hecate");
 		helpAbout.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				aboutDialog = new AboutDialog();
 				aboutDialog.setVisible(true);
 			}
 		});
 		help.add(helpAbout);
 		menuBar.add(help);
 		
 		setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * Sets the size of the window and centers it to the screen
 	 */
 	private void draw() {
 		setMinimumSize(new Dimension(400, 300));
 		pack();
 		// center on screen
 		Toolkit toolkit = getToolkit();
 		Dimension size = toolkit.getScreenSize();
 		setLocation(size.width/2 - getWidth()/2, 
 		            size.height/2 - getHeight()/2);
 	}
 	
 	/**
 	 * Calls the parser on the files, initializes the {@link Delta}
 	 * class and draws the trees at the {@link MainPanel}
 	 * @param oldFilePath
 	 *   The {@ink String} of the path of the original schema file
 	 * @param newFilePath
 	 *   The {@ink String} of the path of the modified schema file
 	 * @throws IOException
 	 * @throws RecognitionException
 	 */
 	private void drawTree(File oldFile, File newFile) throws IOException, RecognitionException {
 		oldSchema = HecateParser.parse(oldFile.getAbsolutePath());
 		newSchema = HecateParser.parse(newFile.getAbsolutePath());
 		
 		mainPanel.drawSchema(oldSchema, "old");
 		mainPanel.drawSchema(newSchema, "new");
 		draw();
 		
 		delta = new Delta();
 		delta.minus(oldSchema, newSchema);
 	}
 
 	private void marshal(Transitions trs) {
 		try {
 			JAXBContext jaxbContext = JAXBContext.newInstance(Update.class, Deletion.class, Insersion.class, TransitionList.class, Transitions.class);
 			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 			jaxbMarshaller.marshal(trs, new FileOutputStream("transitions.xml"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
