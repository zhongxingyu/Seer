 package hci;
 
 import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
 import hci.utils.Point;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDesktopPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 
 
 /**
  * Main class of the program - handles display of the main window
  * 
  * @author Michal
  * 
  */
 public class ImageLabeller extends JFrame {
 	/**
 	 * some java stuff to get rid of warnings
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/* the current polygon being edited*/
 	
 	/**
 	 * main window panel
 	 */
 	JPanel appPanel = null;
 
 	/**
 	 * toolbox - put all buttons and stuff here!
 	 */
 	JPanel toolboxPanel = null;
 	
 	/* containers to hold and display labels created by the user */
 	JInternalFrame  internalFrame=null;
 	JList LabelPanel = null;
 	JInternalFrame creationFrame= null;
 	JPanel optionsPanel =null;
 	JPanel creationPanel = null;
 	 
 	 /*Table of labels*/
 //	 	Hashtable<String,JLabel> label_lookup= new Hashtable<String,JLabel>();
 	
 	 /**
 	 * image panel - displays image and editing area
 	 */
 	 ImagePanel imagePanel = null;
 	 
 	 /* handles the Edit button  and its event corresponding to the  internal frame  */
 	 JButton Edit = new JButton("Edit");
 	 
 	 /*handles the remove button and its event*/
 	 JButton Remove = new JButton("Remove");
 //	 private boolean remove_clicked =false;
 	
 	 /*create the undo and redo action objects*/
 	 UndoAction undo = new UndoAction("Undo","Undo previous step",new Integer(KeyEvent.VK_3));
 	 RedoAction redo = new RedoAction("Redo","Redo previous step",new Integer(KeyEvent.VK_4));
 	
 	 
 	 /*Create Editor Frame*/
 	 JInternalFrame Editor = null;
 	 JPanel editPanel=null;
 	//buttons in the Editor
 	 JButton addP = new JButton("Add a Point");
 	 JButton remP = new JButton("Remove a Point");
 	 JButton adjP = new JButton ("Adjust a Point");
 	 JButton save = new JButton ("Save");
 	 
 
 	public void paint(Graphics g) {
 		super.paint(g);
 		imagePanel.repaint(); // update image pane
 	}
 
 	/**
 	 * sets up application window and the interactive interface
 	 * 
 	 * @param imageFilename
 	 *            image to be loaded for editing
 	 * @throws Exception
 	 */
 	public void setupGUI(String imageFilename) throws Exception {
 		
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent event) {
 				// asks whether the user really want to exit the app;
 				String[] options = {"Yes","No"};
 				int rep = JOptionPane.showOptionDialog(imagePanel, "Do you really wish to quit?", "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
 				if (rep == JOptionPane.YES_OPTION) System.exit(0);
 			}
 		});
 
 		//create the menu bar
 		JMenuBar menubar = new JMenuBar();
 		
 		//file menu
 		JMenu filemenu = new JMenu("File");
 		filemenu.setMnemonic(KeyEvent.VK_1);
 		menubar.add(filemenu);
 		
 		JMenuItem fileItem1 = new JMenuItem("Load New Image");
 		JMenuItem fileItem2 = new JMenuItem("Open Existing Project");
 		JMenuItem fileItem3 = new JMenuItem("Save Project");
 		JMenuItem fileItem4 = new JMenuItem("Close");
 		filemenu.add(fileItem1);
 		filemenu.add(new JSeparator());
 		filemenu.add(fileItem2);
 		filemenu.add(fileItem3);
 		filemenu.add(new JSeparator());
 		filemenu.add(fileItem4);
 		fileItem1.setAccelerator(KeyStroke.getKeyStroke('I', CTRL_DOWN_MASK));
 		fileItem2.setAccelerator(KeyStroke.getKeyStroke('O', CTRL_DOWN_MASK));
 		fileItem3.setAccelerator(KeyStroke.getKeyStroke('S', CTRL_DOWN_MASK));
 		fileItem4.setAccelerator(KeyStroke.getKeyStroke('Q', CTRL_DOWN_MASK));
 		
 		
 		//edit menu
 		JMenu editmenu = new JMenu("Edit");
 		editmenu.setMnemonic(KeyEvent.VK_2);
 		menubar.add(editmenu);
 		
 		JMenuItem editItem1 = new JMenuItem();
 		editItem1.setAction(undo);
 		JMenuItem editItem2 = new JMenuItem();
 		editItem2.setAction(redo);
 		JMenuItem editItem3 = new JMenuItem("Zoom in");
 		JMenuItem editItem4 = new JMenuItem("Zoom out");
 		
 		editmenu.add(editItem1);
 		editmenu.add(editItem2);
 		editmenu.add(new JSeparator());
 		editmenu.add(editItem3);
 		editmenu.add(editItem4);
 		
 		//add menubar to frame
 		setJMenuBar(menubar);
 		
 		
 		//create the action listeners for the menu items
 		
 		//open file
 		fileItem1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser();
 				CustomFileFilter filter = new CustomFileFilter("Images(.jpg, .png)");
 				filter.addExtension("jpg");
 				filter.addExtension("png");
 				
 			    chooser.setFileFilter(filter);
 				int rVal = chooser.showOpenDialog(imagePanel);
 				if(rVal == JFileChooser.APPROVE_OPTION) {
 					try {
 						imagePanel.changePicture(chooser.getSelectedFile().getAbsolutePath());
 			            internalFrame.revalidate();
 			            internalFrame.repaint();
 					} catch (Exception e1) {
 						e1.printStackTrace();
 						JOptionPane.showMessageDialog(null,
 								"The file you selected cannot be opened",
 								"Error opening file",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		
 		//open project
 		fileItem2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser();
 				CustomFileFilter filter = new CustomFileFilter("HCI projects (.ser)");
 				filter.addExtension("ser");
 			    chooser.setFileFilter(filter);
 				int rVal = chooser.showOpenDialog(imagePanel);
 				if(rVal == JFileChooser.APPROVE_OPTION) {
 					try
 			         {
 			            FileInputStream fileIn =
 			                          new FileInputStream(chooser.getSelectedFile());
 			            ObjectInputStream in = new ObjectInputStream(fileIn);
 			            imagePanel.loadProject((SerializableImage) in.readObject(), (Hashtable<String, ArrayList<Point>>) in.readObject(), (ArrayList<Point>) in.readObject());
 			            in.close();
 			            fileIn.close();
 			            
 			            //remove everything in the label panel
 			            LabelPanel.removeAll();
 			            
 //			            //put the new JLabels
 			            
 			            ArrayList<String> list = Collections.list(imagePanel.getPolygonTable().keys());
 				    	LabelPanel.setListData(list.toArray(new String[list.size()]));
 			            
 			        } catch (Exception e2) {
 						e2.printStackTrace();
 						JOptionPane.showMessageDialog(null,
 								"The project you selected cannot be opened",
 								"Error opening project",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		
 		//save
 		fileItem3.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser();
 				CustomFileFilter filter = new CustomFileFilter("HCI projects (.ser)");
 				filter.addExtension("ser");
 			    chooser.setFileFilter(filter);
 				int rVal = chooser.showSaveDialog(imagePanel);
 				if(rVal == JFileChooser.APPROVE_OPTION) {
 					try
 				      {
 						 File selectedFile = chooser.getSelectedFile();
 						 if (!getExtension(selectedFile.getName()).equals("ser")) {
 							 String filename = selectedFile.getAbsolutePath();;
 							 selectedFile = new File(filename.concat(".ser"));
 						 }
 				         FileOutputStream fileOut = new FileOutputStream(selectedFile);
 				         ObjectOutputStream out = new ObjectOutputStream(fileOut);
 				         out.writeObject(imagePanel.getSerializableImage());
 				         out.writeObject(imagePanel.getPolygonTable());
 				         out.writeObject(imagePanel.getCurrentPolygon());
 				         out.close();
 				         fileOut.close();
 				      }catch(IOException e3) {
 				          e3.printStackTrace();
 				      }
 				}
 			}
 		});
 
 		//close
 		fileItem4.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String[] options = {"Yes","No"};
 				int rep = JOptionPane.showOptionDialog(imagePanel, "Do you really wish to quit?", "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
 				if (rep == JOptionPane.YES_OPTION) System.exit(0);
 			}
 		});
 		
 		editItem3.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
				imagePanel.scale(1.1);
 			}
 		});
 		
 		editItem4.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
				imagePanel.scale(0.9);
 			}
 		});
 		
 		// setup main window panel
 		appPanel = new JPanel();
 		this.setLayout(new BoxLayout(appPanel, BoxLayout.X_AXIS));
 		this.setContentPane(appPanel);// this hold the images, image labels and the buttons
 	
 		
 		// Create and set up the image panel.
 		imagePanel = new ImagePanel(imageFilename);
 		imagePanel.setOpaque(true); //content panes must be opaque
 
         //create toolbox panel
         toolboxPanel = new JPanel();
         toolboxPanel.setLayout(new BoxLayout(toolboxPanel, BoxLayout.Y_AXIS));
         
         creationFrame = new JInternalFrame("Creation Panel", false, false, false);
         creationFrame.setVisible(true);
         creationPanel = new JPanel();
         creationPanel.setLayout(new BoxLayout(creationPanel, BoxLayout.Y_AXIS));
         creationFrame.add(creationPanel);
         
         //create internal frame to hold the list of labels
         internalFrame = new JInternalFrame("List of Labels", false,false,false);
         internalFrame.setOpaque(true);
 		internalFrame.setPreferredSize(new Dimension(toolboxPanel.getWidth(), 200));
 		internalFrame.setVisible(true);
        
         for(MouseListener listener : ((javax.swing.plaf.basic.BasicInternalFrameUI) this.internalFrame.getUI()).getNorthPane().getMouseListeners()){
         	((javax.swing.plaf.basic.BasicInternalFrameUI) this.internalFrame.getUI()).getNorthPane().removeMouseListener(listener);
         	}
         
         LabelPanel = new JList();// this ensures the new labels are stacked vertically.
         LabelPanel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         LabelPanel.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent event) {
 				if( event.getSource() == LabelPanel	&& !event.getValueIsAdjusting() ) {
 					if (LabelPanel.getSelectedValue()==null) {
 						Edit.setEnabled(false);
 						Remove.setEnabled(false);
 						return;
 					}
 					Edit.setEnabled(true);
 					Remove.setEnabled(true);
 					String label = (String) LabelPanel.getSelectedValue();
 					if (label != null) imagePanel.setSelectedPolygon(label);
 					else imagePanel.setSelectedPolygon(null);
 					imagePanel.repaint();
 				}
 			}
         });
         JScrollPane scroller = new JScrollPane(LabelPanel);
         scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         
         JScrollPane imageScroller = new JScrollPane(imagePanel);
         imageScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         imageScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         appPanel.add(imageScroller);
         
         internalFrame.getContentPane().add(scroller);
        
         
         optionsPanel = new JPanel();//this contains the edit and remove buttons
         optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
         internalFrame.getContentPane().add(optionsPanel,BorderLayout.SOUTH);
         
         //add listener to the edit button
         Edit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				if (LabelPanel.getSelectedValue() == null) return;
 				if (imagePanel.getCurrentPolygon()!= null) {
 					int reval = JOptionPane.showConfirmDialog(Editor, "If you got to Edit mode now, you will lose the current usaved polygon you are creating. Do you want to save this polygon now?", "Save unsaved polygon?", JOptionPane.YES_NO_CANCEL_OPTION);
 					if (reval == JOptionPane.CANCEL_OPTION) return;
 					if (reval == JOptionPane.YES_OPTION) {
 						JFrame dialogue_frame = new JFrame();
 						String label_msg = (String) JOptionPane.showInputDialog(dialogue_frame, "Please type in your prefered Label", "Annotator", JOptionPane.OK_OPTION, null, null, null);
 				    	//TODO: if the label is duplicate what do we want to do
 						if (label_msg == null){
 				    		//TODO: don't we want to say Error, please provide a name instead of just deleting the person's polygon?
 				    		return;
 				    	}
 						imagePanel.finalizePolygon(label_msg);
 					}
 				}
 				ArrayList<Point> to_edit = imagePanel.getPolygonTable().get((String)LabelPanel.getSelectedValue());
 				imagePanel.removePolygon((String)LabelPanel.getSelectedValue());
 				imagePanel.setCurrentPolygon(to_edit);
 				imagePanel.repaint();
 				imagePanel.currentLabel = (String) LabelPanel.getSelectedValue();
 				
 				Editor.setVisible(true);
 				internalFrame.setVisible(false);
 				optionsPanel.setVisible(false);
 				creationFrame.setVisible(false);
 				
 				Editor.pack();
 					
 				toolboxPanel.revalidate();
 				toolboxPanel.repaint();
 				
 			}
         });
         
         Remove.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if (LabelPanel.getSelectedValue() ==null) return;
 				int reval = JOptionPane.showConfirmDialog(Editor, "Do you really wish to remove this Polygon?", "Delete Polygon", JOptionPane.YES_NO_OPTION);
 				if (reval== JOptionPane.YES_OPTION){
 					System.out.print((String)LabelPanel.getSelectedValue());
 					imagePanel.removePolygon((String)LabelPanel.getSelectedValue());
 					//update the LabelPanel
 					ArrayList<String> list = Collections.list(imagePanel.getPolygonTable().keys());
 				    LabelPanel.setListData(list.toArray(new String[list.size()]));
 				    
 				    if (LabelPanel.getSelectedValue()!= null) 
 				    	imagePanel.setSelectedPolygon((String)LabelPanel.getSelectedValue());
 				    
 					internalFrame.revalidate();
 					internalFrame.repaint();
 					imagePanel.revalidate();
 					imagePanel.repaint();
 					LabelPanel.revalidate();
 					LabelPanel.repaint();
 					
 				}
 			}
         });
         
         Edit.setEnabled(false);
         Remove.setEnabled(false);
 		optionsPanel.add(Edit);
 		optionsPanel.add(Remove);
         
         ///creating the editor Panel
         Editor = new JInternalFrame("Editing",false,false, false);
         for(MouseListener listener : ((javax.swing.plaf.basic.BasicInternalFrameUI) this.Editor.getUI()).getNorthPane().getMouseListeners())
         	((javax.swing.plaf.basic.BasicInternalFrameUI) this.Editor.getUI()).getNorthPane().removeMouseListener(listener);
         
         editPanel = new JPanel();
         editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
         editPanel.add(addP);
         editPanel.add(remP);
         editPanel.add(adjP);
         editPanel.add(save);
         Editor.getContentPane().add(editPanel);
         
         
         adjP.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent arg0) {
 				imagePanel.stopEditing();
 				imagePanel.adjustPoint=true;
 			}
         });
         
         addP.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent arg0) {
 				imagePanel.stopEditing();
 				imagePanel.addpoint=true;
 				imagePanel.repaint();
 			}
         });
 
         remP.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent arg0) {
 				//stopp al preious editing
 				imagePanel.stopEditing();
 				//and allow the removing of points
 				imagePanel.removePoint=true;
 			}
         	
         });
         save.addActionListener(new ActionListener(){
         //when the save button is clicked , the edited polygon is added referenced by the current label
 			public void actionPerformed(ActionEvent arg0) {
 				
 			  imagePanel.stopEditing();
 			  
 			  imagePanel.finalizePolygon(imagePanel.currentLabel);
 			  String label = (String) LabelPanel.getSelectedValue();
 			  if (label != null) imagePanel.setSelectedPolygon(label);
 			  else imagePanel.setSelectedPolygon(null);
 			  imagePanel.repaint();
 			  imagePanel.currentLabel=null;
 			  Editor.setVisible(false);
 			  
 			  internalFrame.setVisible(true);
 			  optionsPanel.setVisible(true);
 			  creationFrame.setVisible(true);
 			  imagePanel.revalidate();
 			  imagePanel.repaint();
 			  
 			  toolboxPanel.revalidate();
 			  toolboxPanel.repaint();
 			}
         	
         	
         });
         
         //adding the internal frames to the toolbox
         
 		
         toolboxPanel.add (Editor);
         toolboxPanel.add(internalFrame);
         toolboxPanel.add(creationFrame);
         
         //Add button to the toolbox
 		JButton newPolyButton = new JButton("Create new object");
 		newPolyButton.setMnemonic(KeyEvent.VK_N);
 		newPolyButton.setSize(50, 20);
 		newPolyButton.setEnabled(true);
 		newPolyButton.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				if (imagePanel.getCurrentPolygon()!= null && imagePanel.getCurrentPolygon().size()>0) {
 					int reval = JOptionPane.showConfirmDialog(Editor, "If you got to Edit mode now, you will lose the current usaved polygon you are creating. Do you want to save this polygon now?", "Save unsaved polygon?", JOptionPane.YES_NO_CANCEL_OPTION);
 					if (reval == JOptionPane.CANCEL_OPTION) return;
 					if (reval == JOptionPane.YES_OPTION) {
 						JFrame dialogue_frame = new JFrame();
 						String label_msg = (String) JOptionPane.showInputDialog(dialogue_frame, "Please type in your prefered Label", "Annotator", JOptionPane.OK_OPTION, null, null, null);
 				    	//TODO: if the label is duplicate what do we want to do
 						if (label_msg == null){
 				    		//TODO: don't we want to say Error, please provide a name instead of just deleting the person's polygon?
 				    		return;
 				    	}
 						imagePanel.finalizePolygon(label_msg);
 					}
 				}
 				imagePanel.addpoint = true;
 				imagePanel.setCurrentPolygon(new ArrayList<Point>());
 				imagePanel.revalidate();
 				imagePanel.repaint();
 			}
 		});
 		newPolyButton.setToolTipText("Click to add new annotation object");
 		creationPanel.add(newPolyButton);
 
 		JButton closeButton = new JButton("Save object");
 		closeButton.setMnemonic(KeyEvent.VK_F);
 		closeButton.setSize(50, 20);
 		closeButton.setEnabled(true);
 		closeButton.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 			    	
 					if (imagePanel.getCurrentPolygon()==null){
 				        JOptionPane.showMessageDialog(Editor, "You haven't created a polygon to save. Please click on Create new Polygon first and then add points by clicking on the image");
 						return;
 					}
 					
 			    	// create a dialogue to ask the user for an annotation
 			    	JFrame dialogue_frame = new JFrame();
 			    	String label_msg = (String) JOptionPane.showInputDialog(dialogue_frame, "Please type in your prefered Label", "Annotator", JOptionPane.OK_OPTION, null, null, null);
 			    	if (label_msg==null){
 			    		//TODO: don't we want to say Error, please provide a name instead of just deleting the person's polygon?
 			    		return;
 			    	}
 			    	imagePanel.finalizePolygon(label_msg);//create new polygon indexed by this string
 			    	imagePanel.addpoint = false;
 			    	ArrayList<String> list = Collections.list(imagePanel.getPolygonTable().keys());
 			    	LabelPanel.setListData((list.toArray(new String[list.size()])));
 			    	
 			    	internalFrame.revalidate();
 			    	internalFrame.repaint();
 			}
 		});
 		closeButton.setToolTipText("Click to complete this annotation object");
 		creationPanel.add(closeButton);
 		
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.setMnemonic(KeyEvent.VK_C);
 		cancelButton.setSize(50,20);
 		cancelButton.setEnabled(true);
 		cancelButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 				
 				if (imagePanel.getCurrentPolygon()!=null){
 				    	
 				      imagePanel.setCurrentPolygon(null);
 					  imagePanel.repaint();
 				    	
 				   }
 			}
 		});
 		cancelButton.setToolTipText("Cancel creating the polygon");
 		creationPanel.add(cancelButton);
 	
 	
 		//add toolbox to window
 		toolboxPanel.setPreferredSize(new Dimension(200,600));
 		appPanel.add(toolboxPanel);
 
 		// display all the stuff
 		 this.setSize(1100,700);
 		pack();
 		setVisible(true);
 		//System.out.println(this.getSize().getHeight() + " "
 		//		+ this.getSize().getWidth());
 		
 		validate();
 		repaint();
 		
 		
 	}
 	
 
 	/**
 	 * Runs the program
 	 * 
 	 * @param argv
 	 *            path to an image
 	 */
 	public static void main(String argv[]) {
 		try {
 			// create a window and display the image
 			ImageLabeller window = new ImageLabeller();
 			window.setupGUI(argv[0]);
 			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		} catch (Exception e) {
 			System.err.println("Image: " + argv[0]);
 			e.printStackTrace();
 		}
 	}
 	
 	public String getExtension(String filename) {
 		String[] nameParts = filename.split("\\.");
 		//file has no extension and is not a folder so refuse it
 		if (nameParts.length == 0) return "";
 		return nameParts[nameParts.length - 1];
 	}
 	
 	class CustomFileFilter extends FileFilter {
 		
 		private ArrayList<String> accepted_extensions = new ArrayList<String>();
 		private String name;
 		
 		public boolean accept(File f) {
 			if (f.isDirectory()) return true;
 			String filename = f.getName();
 			String extension = getExtension(filename);
 			for (int i=0; i<accepted_extensions.size(); i++) {
 				if (extension.equals(accepted_extensions.get(i))) return true;
 			}
 			return false;
 		}
 		
 		public CustomFileFilter(String name) {
 			super();
 			this.name = name;
 		}
 
 		public String getDescription() {
 			return name;
 		}
 		
 		public void addExtension(String ext) {
 			accepted_extensions.add(ext);
 		}
 	}
 	
 	class UndoAction extends AbstractAction{
 		
 		private static final long serialVersionUID = 1L;
 		
 		public UndoAction(String text, String desc , Integer mnemonic){
 			super(text);
 			putValue(SHORT_DESCRIPTION,desc);
 			putValue(MNEMONIC_KEY, mnemonic);	
 			
 		}
 
 		public void actionPerformed(ActionEvent arg0) {
 			
 			if (imagePanel.getCurrentPolygon() != null){
 				ArrayList<Point> currentPolygon  = imagePanel.getCurrentPolygon();
 				//TODO: how does this get reflected in the polygon cache?
 				currentPolygon.remove(currentPolygon.size()-1);
 				
 				imagePanel.repaint();
 				//imagePanel.drawPolygon(currentPolygon,Color.GREEN);
 				///imagePanel.setCurrentPolygon(currentPolygon);
 				System.out.println( "is the cache and current equal "+ (currentPolygon.size()==imagePanel.currentPolygon_cache.size()) + " the size is "+ currentPolygon.size());
 				
 			}
 		}
 	}	
 		
 		class RedoAction extends AbstractAction{
 			
 			private static final long serialVersionUID = 2L;
 
 			public RedoAction(String text, String desc , Integer mnemonic){
 				super(text);
 				putValue(SHORT_DESCRIPTION,desc);
 				putValue(MNEMONIC_KEY, mnemonic);
 				
 				
 			}
 
 			public void actionPerformed(ActionEvent e) {
 						
 				if (imagePanel.getCurrentPolygon().size()!= imagePanel.currentPolygon_cache.size()){
 					System.out.println("it enters here");
 				  ArrayList<Point> currentPolygon  = imagePanel.getCurrentPolygon();
 				  currentPolygon.add(imagePanel.currentPolygon_cache.get(currentPolygon.size()));
 				
 				  //imagePanel.drawPolygon(currentPolygon, Color.GREEN);
 				}
 			}
 		}
 	}
 
