 //Sam Dickson and Niraj Venkat
 //CS251 Project 5--Map Editor
 
 import javax.swing.*; 
 import javax.swing.event.*; 
 import javax.swing.text.*; 
 import javax.swing.border.*; 
 import javax.swing.colorchooser.*; 
 import javax.swing.filechooser.*; 
 import javax.accessibility.*; 
 import javax.imageio.ImageIO;
 
 import java.awt.*; 
 import java.awt.event.*; 
 import java.beans.*; 
 import java.util.*; 
 import java.io.*; 
 import java.io.FileFilter;
 import java.applet.*; 
 import java.net.*;
 
 public class MapEditor extends JFrame implements ActionListener, MouseListener
 {
     //Constants
     public static final int PREFERRED_WIDTH = 680;
     public static final int PREFERRED_HEIGHT = 600;
     public static final double MAX_ZOOM = 50.0;
     public static final double MIN_ZOOM = 11.0;
     public static final double ZOOM_INCREMENT = 3.0;
     
     //GUI components
     private JScrollPane scrollPane;
     private ZoomPane zoomPane;
     private MapScene map;
     private JPopupMenu popup;
     
     //Menu items for file menu:
     private JMenuItem exitAction;
     private JMenuItem openAction;
     private JMenuItem saveAction;
     private JMenuItem saveAsAction;
     private JMenuItem newAction;
     
     //Menu items for map menu:
     private JMenuItem zoomInAction;
     private JMenuItem zoomOutAction;
     public JRadioButtonMenuItem insertLocationMode;
     public JRadioButtonMenuItem deleteLocationMode;
     public JRadioButtonMenuItem insertPathMode;
     public JRadioButtonMenuItem deletePathMode;
     
     //Menu items for help menu:
     private JMenuItem aboutAction;
     private JMenuItem helpAction;
     
     //Menu items for the right-click menu:
     private JMenuItem info_rightClick;
     private JMenuItem edit_rightClick;
     private JMenuItem delete_rightClick;
 
     
     //Session variables
     public static ArrayList<Vertex> points = new ArrayList<Vertex>();
     public static ArrayList<Path> paths = new ArrayList<Path>();
     
     public static String imagePath = "Resources/purdue-map.jpg"; //Default map image location
     public static String filePath = ""; //Default xml location
     XML mapXML = new XML();
     int vertex_id = 0;
     double zoomValue = 20.00;
     public static double scale_feet_per_pixel;
     
     //Temporary variables
     Point p;
     
     public static void main(String[] args) 
     { 
     	MapEditor mapEditor = new MapEditor(); 
     	mapEditor.setVisible(true);
     } 
 
     /*public void paint(Graphics g)
     {
     	
     }*/
     
     //Handle events for mouse actions
     public void mouseClicked(MouseEvent me)
     {
     	if (me.isPopupTrigger()) 
     	{
             popup.show(me.getComponent(), me.getX(), me.getY());
         }
     }
     
     
     
     //Implemented methods from interface. Not used.
     public void mousePressed(MouseEvent me)
     {
     	if (me.isPopupTrigger()) 
     	{
             popup.show(me.getComponent(), me.getX(), me.getY());
         }
     }
     public void mouseEntered(MouseEvent me){}
     public void mouseReleased(MouseEvent me){}
     public void mouseExited(MouseEvent me){}
     
     //Handle events for menu objects
     public void actionPerformed(ActionEvent evt)
     {
     	//Actions for file menu:
     	if(evt.getSource().equals(exitAction)) //Exit program
     	{
     		System.exit(0);
     	}
     	else if(evt.getSource().equals(newAction)) //Create new map
     	{
     		String response = null;
     		String tmpPath = imagePath;
     		imagePath = null;
     		double tmp_scale_feet_per_pixel = scale_feet_per_pixel;
     		scale_feet_per_pixel = 0.0;
     		boolean done = false;
     		
     		JFileChooser fileChooser = new JFileChooser();
     		fileChooser.setFileFilter(new FileNameExtensionFilter("Valid Map Files", "jpg", "gif"));
     		int result = fileChooser.showOpenDialog(this);
     			
     		if(result == JFileChooser.APPROVE_OPTION)
     		{
     			 imagePath = fileChooser.getSelectedFile().getAbsolutePath();
 
 	    		done = false;
 	    		if(imagePath != null)
 	    		{
 		    		while(!done)
 		    		{
 		    			response = JOptionPane.showInputDialog(null, "Enter the feet-per-pixel constant: ", "New Map", JOptionPane.OK_CANCEL_OPTION);
 		    			
 		    			if(response == null)
 		    			{
 		    				break;
 		    			}
 		    			
 		    			try
 		    			{
 		    				scale_feet_per_pixel = Double.parseDouble(response);
 		    				done = true;
 		    			}
 		    			catch(Exception e)
 		    			{
 		    				JOptionPane.showMessageDialog(null, "Invalid feet-per-pixel constant.", "Error", JOptionPane.ERROR_MESSAGE);
 		    			}
 		    		}
 	    		}
     		}
     		
     		if(response != null && imagePath != null)
     		{
     			loadImage();
     			//JOptionPane.showMessageDialog(null, "New map successfully created!", "New Map", JOptionPane.PLAIN_MESSAGE);
     		}
     		else
     		{
     			imagePath = tmpPath;
     			scale_feet_per_pixel = tmp_scale_feet_per_pixel;
     		}
     	}
     	else if(evt.getSource().equals(openAction)) //Open existing XML 
     	{
     		JFileChooser fileChooser = new JFileChooser();
     		fileChooser.setFileFilter(new FileNameExtensionFilter("Valid Map Files", "xml"));
     		int result = fileChooser.showOpenDialog(this);
 			
 			if(result == JFileChooser.APPROVE_OPTION)
 			{
 				filePath = fileChooser.getSelectedFile().getAbsolutePath();
 				mapXML.openMap(filePath);
 				loadImage();
 			}
 			
     	}
     	else if(evt.getSource().equals(saveAction)) //Save current XML
     	{
     		if(filePath != null && imagePath != null)
     		{
     			mapXML.saveMap(filePath, imagePath, scale_feet_per_pixel);
     		}
     	}
     	else if(evt.getSource().equals(saveAsAction)) //Save current XML with different name
     	{
     		JFileChooser fileChooser = new JFileChooser();
     		int result = fileChooser.showSaveDialog(this);
     		
     		if(result == JFileChooser.APPROVE_OPTION)
     		{
     			filePath = fileChooser.getSelectedFile().getAbsolutePath();
     			mapXML.saveMap(filePath, imagePath, scale_feet_per_pixel);
     		}
     	}
     	
     	//Actions for map menu:
     	else if(evt.getSource().equals(zoomInAction)) //Zoom in
     	{
     		if(zoomValue < MAX_ZOOM)
     		{
     			double scale = (zoomValue + ZOOM_INCREMENT) / 20.0;
     			zoomValue+=ZOOM_INCREMENT;
     			zoomPane.zoom(scale);
     			zoomPane.repaint();
     		}
     	}
     	else if(evt.getSource().equals(zoomOutAction)) //Zoom out
     	{
     		if(zoomValue > MIN_ZOOM)
     		{
     			double scale = (zoomValue - ZOOM_INCREMENT) / 20.0;
     			zoomValue-=ZOOM_INCREMENT;
     			zoomPane.zoom(scale);
     			zoomPane.repaint();
     		}
     	}
     	
     	//Actions for help menu:
     	else if(evt.getSource().equals(aboutAction)) //Display about dialog
     	{
     		JOptionPane.showMessageDialog(null, "Map Viewer:\n\nDeveloped by Sam Dickson and Niraj Venkat\nPurdue University, CS 251\nApril 2013", "About Map Viewer", JOptionPane.PLAIN_MESSAGE);
     	}
     	else if(evt.getSource().equals(helpAction)) //Display help dialog
     	{
     		JOptionPane.showMessageDialog(null, "Map Viewer Commands:\n\n", "Help", JOptionPane.PLAIN_MESSAGE);
     	}
     }
     
     public boolean verifyFile(String fp)
     {
     	try
     	{
     		File tmp = new File(fp);
     	}
     	catch(Exception e)
     	{
     		return false;
     	}
     	
     	return true;
     }
     
     public void loadImage()
     {
     	Image image = new ImageIcon(imagePath).getImage();
     	getContentPane().remove(zoomPane);
 		map.setImage(image);
 	    zoomPane.setScene(map);
 	    getContentPane().add(zoomPane);
 	    zoomPane.repaint();
     }
     
     public MapEditor() 
     {
 		setTitle("Map Editor");
 		setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
 		setBackground(Color.gray);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 		JPanel panel = new JPanel();
 		panel.setLayout( new BorderLayout()); 
 		getContentPane().add(panel);
 		
 		//Create and set up menubars:
 		JMenuBar menubar = new JMenuBar();
 		JMenu fileMenu = new JMenu("File");
 		JMenu mapMenu = new JMenu("Map");
 		JMenu helpMenu = new JMenu("Help");
 		popup = new JPopupMenu();
 		popup.addMouseListener(this);
 		
 		//Menu items for file menu:
 		exitAction = new JMenuItem("Exit");
 		exitAction.addActionListener(this);
 		exitAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		openAction = new JMenuItem("Open");
 		openAction.addActionListener(this);
 		openAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 		saveAction = new JMenuItem("Save");
 		saveAction.addActionListener(this);
 		saveAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		newAction = new JMenuItem("New");
 		newAction.addActionListener(this);
 		newAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 		saveAsAction = new JMenuItem("Save As...");
 		saveAsAction.addActionListener(this);
 		saveAsAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
 		
 		fileMenu.add(newAction);
 		fileMenu.add(openAction);
 		fileMenu.add(saveAction);
 		fileMenu.add(saveAsAction);
 		fileMenu.add(exitAction);
 		
 		//Menu items for map menu:
 		zoomInAction = new JMenuItem("Zoom In");
 		zoomInAction.addActionListener(this);
 		zoomInAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
 		zoomOutAction = new JMenuItem("Zoom Out");
 		zoomOutAction.addActionListener(this);
 		zoomOutAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
 		ButtonGroup modeOptions = new ButtonGroup();
 		insertLocationMode = new JRadioButtonMenuItem("Insert Location Mode");
 		deleteLocationMode = new JRadioButtonMenuItem("Delete Location Mode");
 		insertPathMode = new JRadioButtonMenuItem("Insert Path Mode");
 		deletePathMode = new JRadioButtonMenuItem("Delete Path Mode");
 		modeOptions.add(insertLocationMode);
 		modeOptions.add(deleteLocationMode);
 		modeOptions.add(insertPathMode);
 		modeOptions.add(deletePathMode);
 		
 		mapMenu.add(zoomInAction);
 		mapMenu.add(zoomOutAction);
 		mapMenu.addSeparator();
 		mapMenu.add(insertLocationMode);
 		mapMenu.add(deleteLocationMode);
 		mapMenu.add(insertPathMode);
 		mapMenu.add(deletePathMode);
 		
 		//Menu items for help menu:
 		aboutAction = new JMenuItem("About");
 		aboutAction.addActionListener(this);
 		helpAction = new JMenuItem("Help");
 		helpAction.addActionListener(this);
 		helpMenu.add(aboutAction);
 		helpMenu.add(helpAction);
 		
 		//Menu items for right-click menu:
 		info_rightClick = new JMenuItem("Info");
 		info_rightClick.addMouseListener(this);
 		edit_rightClick = new JMenuItem("Edit");
 		info_rightClick.addMouseListener(this);
 		delete_rightClick = new JMenuItem("Delete");
 		info_rightClick.addMouseListener(this);
 		
 		menubar.add(fileMenu);
 		menubar.add(mapMenu);
 		menubar.add(helpMenu);
 		popup.add(info_rightClick);
 		popup.add(edit_rightClick);
 		popup.add(delete_rightClick);
 		setJMenuBar(menubar);
 		
 		Image image = new ImageIcon(imagePath).getImage();
 		map = new MapScene(image);
 	    zoomPane = new ZoomPane(map);
 	    
 	    
 	    MouseAdapter listener = new MouseAdapter() {
 	    	public void mouseClicked(MouseEvent e)
 	        {
 	    		if(insertLocationMode.isSelected())
 	        	{
 		        	String name = JOptionPane.showInputDialog(null, "Name for this point?", "Add Point", JOptionPane.OK_CANCEL_OPTION);
		        	if(name != null || name.equals(""))
 		        	{
 		        		Point point = zoomPane.toViewCoordinates(e.getPoint());
 		        		points.add(new Vertex(name, (vertex_id++), (int)point.getX(), (int)point.getY()));
 		        		map.mouseClicked();
 		        	}
 	        	}
 	    		else if(deleteLocationMode.isSelected())
 	    		{
 	    			Point point = zoomPane.toViewCoordinates(e.getPoint());
 	    			ArrayList<Path> toBeRemoved = new ArrayList<Path>();
 	    			
 	    			 for(Vertex v : points)
 			         {
 	    				 if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 	    				 {
 	    				 
 	    					 for(Path p : paths)
 	    					 {
 	    						 if(p.getStart().equals(v) || p.getEnd().equals(v))
 	    						 {
 	    							 toBeRemoved.add(p);
 	    						 }
 	    					 }
 	    					 
 	    					 points.remove(v);
 	    					 break;
 	    				 }
 			         }
 	    			 
 	    			 for(Path condemned : toBeRemoved)
 	    			 {
 	    				 paths.remove(condemned);
 	    			 }
 	    			 toBeRemoved = null;
 	    			 map.mouseClicked();
 	    			 
 	    		}
 	        }
 	    	public void mousePressed(MouseEvent e) 
 	    	{
 	    		if(insertPathMode.isSelected())
 	    		{
 		            Point point = zoomPane.toViewCoordinates(e.getPoint());
 		            if(paths.size() > 0)
 		            {
 		            	try
 		            	{
 			            	if(paths.get(paths.size() - 1).getEnd() == null)
 			            	{
 			            		paths.remove(paths.size() - 1);
 			            	}
 		            	}
 		            	catch(Exception e2){}
 		            }
 		            for(Vertex v : points)
 		            {
 		            	if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 		            	{
 		            		paths.add(new Path(v, null));
 		            		break;
 		            	}
 		            }
 		            map.mousePressed(point);
 	    		}
 	        }
 	    	
 	    	public void mouseReleased(MouseEvent e)
 	        {
 	    		if(insertPathMode.isSelected())
 	    		{
 		            Point point = zoomPane.toViewCoordinates(e.getPoint());
 		            for(Vertex v : points)
 		            {
 		            	if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 		            	{
 		            		paths.get(paths.size() - 1).setEnd(v);
 		            		break;
 		            	}
 		            }
 		            map.mouseReleased();
 	    		}
 	        }
 	    	
 	    	 
 	      };
 
 	      MouseMotionAdapter motionListener = new MouseMotionAdapter() {
 	        public void mouseDragged(MouseEvent e) 
 	        {
 	        	if(insertPathMode.isSelected())
 	    		{
 	        		try
 	        		{
 			            if(paths.get(paths.size() - 1).getEnd() == null)
 			            {
 			            	Point point = zoomPane.toViewCoordinates(e.getPoint());
 			  	          	map.mouseDragged(point);
 			            }
 	        		}
 	        		catch(Exception e2){}
 		            
 	    		}
 	          
 	        }
 	        
 	      };
 
 	      zoomPane.getZoomPanel().addMouseListener(listener);
 	      zoomPane.getZoomPanel().addMouseMotionListener(motionListener);
 
 	    getContentPane().add(zoomPane);
 
     }
     
     
 };
