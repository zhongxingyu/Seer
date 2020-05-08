 package hci;
 
 import javax.imageio.ImageIO;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import javax.swing.ImageIcon;
 import hci.ImageFileFilter;
 import java.util.ArrayList;
 import hci.utils.Point;
 import hci.SaveObjects;
 import hci.ReadObjects;
 import hci.XMLOutput;
 
 /**
  * Main class of the program - handles display of the main window
  * @author Michal
  *
  */
 public class ImageLabeller extends JFrame {
 	
 	BufferedImage newImage = null;
 	XMLOutput xOut = null;
 	String imageName = "./images/U1003_0000.jpg";
 	/**
 	 * some java stuff to get rid of warnings
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * main window panel
 	 */
 	JPanel appPanel = null;
 	JPanel bigassPanel = null;
 	/**
 	 * toolbox - put all buttons and stuff here!
 	 */
 	JPanel toolboxPanel = null;
 	
 	/**
 	 * image panel - displays image and editing area
 	 */
 	ImagePanel imagePanel = null;
 	SaveObjects objectSaver = new SaveObjects();
 	
 	/**
 	 * handles New Object button action
 	 */
 	public void addNewPolygon() {
 		imagePanel.addNewPolygon();
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		imagePanel.paint(g); //update image panel
 	}
 	
 	
 	/**
 	 * sets up application window
 	 * @param imageFilename image to be loaded for editing
 	 * @throws Exception
 	 */
 	public void setupGUI(String imageFilename) throws Exception {
 		//setup main window panel
 		bigassPanel = new JPanel();
 		bigassPanel.setLayout(new BoxLayout(bigassPanel, BoxLayout.LINE_AXIS));
 		appPanel = new JPanel();
 		appPanel.setLayout(new BoxLayout(appPanel, BoxLayout.PAGE_AXIS));
 		this.setContentPane(bigassPanel);
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		
         //Create and set up the image panel.
 		imagePanel = new ImagePanel(imageFilename);
 		this.addWindowListener(new WindowAdapter() {
 		  	public void windowClosing(WindowEvent event) {
 		  		//we exit the program, ask if the user really wants to do it
 		  		if (imagePanel.polygonsList.size() > 0){
 		  			int response = JOptionPane.showConfirmDialog(null, "You have unsaved changes.  Would you like to save before closing?");
 		  			if (response == JOptionPane.YES_OPTION){
 		  				objectSaver.buildXML(imagePanel.polygonsList, imagePanel.labelList, imageName);
 		  				System.exit(0);
 		  			} else if (response == JOptionPane.NO_OPTION) {
 		  				System.exit(0);
 		  			}
 		  		} else {
 		  			System.exit(0);
 		  		}
 		    	
 		  	}
 		});
 		imagePanel.setOpaque(true); //content panes must be opaque
 		
         appPanel.add(imagePanel);
 
         //create toolbox panel
         toolboxPanel = new JPanel();
         toolboxPanel.setLayout(new BoxLayout(toolboxPanel, BoxLayout.LINE_AXIS));
         
         ImageIcon open_icon = new ImageIcon("icons/open.gif");
         ImageIcon save_icon = new ImageIcon("icons/save.gif");
         ImageIcon image_icon = new ImageIcon("icons/image.gif");
         ImageIcon help_icon = new ImageIcon("icons/help.gif");
         ImageIcon edit_icon = new ImageIcon("icons/edit.gif");
         ImageIcon delete_icon = new ImageIcon("icons/delete.gif");
         
 		JButton openFileButton = new JButton();
 		openFileButton.setIcon(image_icon);
 		openFileButton.setToolTipText("Open Image");
 		openFileButton.setEnabled(true);
 		openFileButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ImageFileFilter filter = new ImageFileFilter();
 				JFileChooser imageChooser = new JFileChooser();
 				imageChooser.setFileFilter(filter);
 				int returnVal = imageChooser.showOpenDialog(appPanel);
 			    if(returnVal == JFileChooser.APPROVE_OPTION) {
 			    	System.out.println("You chose to open this file: " +
 			    		   imageChooser.getSelectedFile().getName());
 			    	imageName = imageChooser.getSelectedFile().getAbsolutePath();
 			    	try{
 			    		newImage = ImageIO.read(new File(imageName));
 			    		imagePanel.image = newImage;
 			    		imagePanel.polygonsList = new ArrayList<ArrayList<Point>>();
			    		imagePanel.labelList = new ArrayList<String>();
			    		imagePanel.drawLabels();
 			    	} catch (Exception a) {
 			    		a.printStackTrace();
 			    	}
 			    }
 			}
 		});
 		toolboxPanel.add(openFileButton);
 		
 		JButton saveButton = new JButton();
 		saveButton.setIcon(save_icon);
 		saveButton.setToolTipText("Save");
 		saveButton.setEnabled(true);
 		saveButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				objectSaver.buildXML(imagePanel.polygonsList, imagePanel.labelList, imageName);
 				JOptionPane.showMessageDialog(null, "Session saved");
 			}
 		});
 		toolboxPanel.add(saveButton);
 		
 		JButton loadButton = new JButton();
 		loadButton.setIcon(open_icon);
 		loadButton.setToolTipText("Load");
 		loadButton.setEnabled(true);
 		loadButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println(imageName);
 				File f = new File(imageName+".xml");
 				ReadObjects objectReader = new ReadObjects();
 				if (f.isFile()){
 					xOut = objectReader.loadFile(imageName);
 					imagePanel.polygonsList = xOut.getObjects();
 					imagePanel.labelList = xOut.getLabels();
					System.out.println(imagePanel.labelList);
 					imagePanel.drawLabels();
 					JOptionPane.showMessageDialog(null, "Session loaded");
 				} else {
 					JOptionPane.showMessageDialog(null, "Nothing to load");
 				}
 			}
 		});
 		toolboxPanel.add(loadButton);
 		
 		JButton helpButton = new JButton();
 		helpButton.setIcon(help_icon);
 		helpButton.setToolTipText("Help");
 		helpButton.setEnabled(true);
 		helpButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				try{
 					String url = "file:///afs/inf.ed.ac.uk/user/s09/s0901522/hci/hci-practical/help/help.html";
 					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
 				} catch (Exception b){
 					
 				}
 			}
 		});
 		
 		JButton editButton = new JButton();
 		editButton.setIcon(edit_icon);
 		editButton.setToolTipText("Edit Label");
 		editButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				imagePanel.editLabel();
 			}
 		});
 		
 		JButton deleteButton = new JButton();
 		deleteButton.setIcon(delete_icon);
 		deleteButton.setToolTipText("Delete Shape");
 		deleteButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				imagePanel.deleteLabel();
 			}
 		});
 
 		toolboxPanel.add(helpButton);
 		toolboxPanel.add(editButton);
 		toolboxPanel.add(deleteButton);
 		
 		//add toolbox to window
 		appPanel.add(toolboxPanel);
 		
 		bigassPanel.add(appPanel);		
 		bigassPanel.add(imagePanel.labelPanel);
 		
 		//display all the stuff
 		this.pack();
         this.setVisible(true);
 	}
 	
 	/**
 	 * Runs the program
 	 * @param argv path to an image
 	 */
 	public static void main(String[] argv) {
 		try {
 			//create a window and display the image
 			ImageLabeller window = new ImageLabeller();
 			window.setupGUI(argv[0]);
 		} catch (Exception e) {
 			System.err.println("Image: " + argv[0]);
 			e.printStackTrace();
 		}
 	}
 }
