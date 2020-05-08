 package gui;
 import javax.swing.*;
 
 import storage.*;
 
 import com.sun.awt.AWTUtilities;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 
 //for textbox?
 import java.awt.TextArea;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 
 
 
 
 
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseAdapter;
 
 
 /**
  * Gui will be the gui used for our Newton's Toolbox project (EDIT DESCRIPTION LATER K THX)
  *  
  * @author Michelle Len
  * @author Jonathan Tan
  * @author http://www.java2s.com/Code/Java/Swing-JFC/Createundecoratedframe.htm
  * @version 03/04/2012 for CS 48 Project, W12
  */
 public class GUIMain {
 
 	//For mouse clicks.
 	private static Point point = new Point();
 
 	// Declare our private variables
	public static JFrame frame;
 	private NewtonsToolboxPanel panel;
 
 	// Dynamic content panel
 	private JPanel contentPanel = new JPanel();
 
 	//These are the databases from which all modules can refer to.
 	//Use GUIMain.<your>Database
 	public static FormulaDatabase FORMULAS = (FormulaDatabase)Saver.loadForms();
 	public static VariableDatabase VARIABLES = (VariableDatabase)Saver.loadVars();
 	public static UnitDatabase UNITS = (UnitDatabase)Saver.loadUnits();
 
 	/**
 	 * Creates the JFrame, and the JPanel
 	 */
 	public void go() {
 
 		// Initialize our frame and set the size
 		frame = new JFrame("Newton's Toolbox");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(900,600);
 		frame.setUndecorated(true);
 
 		// Declaring and creating a panel
 		panel = new NewtonsToolboxPanel();
 
 		//Adds invisible dummy panel;
 		contentPanel.setOpaque(false);
 		panel.add(contentPanel);
 
 		//Default menu state
 		//changeContent(new GUISearch());
 
 		//THIS IS HOW YOU ADD BUTTONS TO THE BACKGROUND PANEL
 		//Create a new panel
 		//Use the setBounds(int x, int y, int width, int height); method to set the size of the panel
 		//Then append it to the main panel panel.
 		JPanel quitPanel = new JPanel(new BorderLayout());
 		quitPanel.setOpaque(false);
 		quitPanel.setBounds(830,20,50,25);
 
 		JButton quitButton = new JButton("x");
 		quitButton.addActionListener(new quitListener());
 
 		quitPanel.add(quitButton);
 		panel.add(quitPanel);
 
 		//Minimize button
 		JPanel minimizePanel = new JPanel(new BorderLayout());
 		minimizePanel.setOpaque(false);
 		//I know it's too small right now, I'll fix it later.
 		minimizePanel.setBounds(805,20,25,25);
 
 		JButton minimizeButton = new JButton("_");
 		minimizeButton.addActionListener(new minimizeListener());
 
 		minimizePanel.add(minimizeButton);
 		panel.add(minimizePanel);
 
 		//controlPanel - main controls
 		JPanel controlPanel = new JPanel();
 		controlPanel.setOpaque(false);
 		controlPanel.setBounds(130,520,600,35);
 		panel.add(controlPanel);
 
 		//controlPanel - buttons
 		JToggleButton searchFormsButton = new JToggleButton("Search Formulas");
 		searchFormsButton.addActionListener(new searchListener());
 		JToggleButton printFormsButton = new JToggleButton("Print Formulas");
 		printFormsButton.addActionListener(new printListener());
 		JToggleButton solveFormsButton = new JToggleButton("Solve Formulas");
 		solveFormsButton.addActionListener(new solveListener());
 		JToggleButton addFormsButton = new JToggleButton("Add Formulas");
 		addFormsButton.addActionListener(new addListener());
 
 		//cpButtons - button group for buttons on the controlPanel.
 		ButtonGroup cpButtons = new ButtonGroup();
 
 		cpButtons.add(searchFormsButton);
 		cpButtons.add(printFormsButton);
 		cpButtons.add(solveFormsButton);
 		cpButtons.add(addFormsButton);
 
 		//Adding buttons to controlPanel.
 		controlPanel.add(searchFormsButton);
 		controlPanel.add(printFormsButton);
 		controlPanel.add(solveFormsButton);
 		controlPanel.add(addFormsButton);
 
 		// Adding the Border Layout
 		frame.getContentPane().add(BorderLayout.CENTER, panel);
 
 		//SHAPE
 		Quadrilateral blueQuadrilateral = new Quadrilateral();
 		blueQuadrilateral.addPoint((int)(0.03125*frame.getWidth()), (int)(0.06250*frame.getHeight())); //  1/32(W) &  1/16(H)
 		blueQuadrilateral.addPoint((int)(frame.getWidth()),         (int)(0)); // 32/32(W) &  0/32(H)
 		blueQuadrilateral.addPoint((int)(0.90625*frame.getWidth()), (int)(frame.getHeight())); // 29/32(W) & 32/32(H)
 		blueQuadrilateral.addPoint((int)(0.06250*frame.getWidth()), (int)(0.93750*frame.getHeight())); //  1/16(W) & 15/16(H)
 
 		AWTUtilities.setWindowShape(frame, blueQuadrilateral);
 
 		//Makes frame draggable.
 		//Has a ghosting problem on some machines.
 		frame.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				point.x = e.getX();
 				point.y = e.getY();
 			}
 		});
 
 		frame.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseDragged(MouseEvent e) {
 				Point p = frame.getLocation();
 				frame.setLocation(p.x + e.getX() - point.x, p.y + e.getY() - point.y);
 			}
 		});
 
 		// Decorations
 		JLabel titlegraphic = new JLabel(new ImageIcon("img/ntbtitle.png"));
 		titlegraphic.setBounds(530,23,265,20);
 		titlegraphic.setVisible(true);
 		panel.add(titlegraphic);
 
 		// Displays the frame
 		frame.setVisible(true);	
 
 
 	}
 
 	public void changeContent (JPanel newPanel){
 		panel.remove(contentPanel);
 		contentPanel = newPanel;
 		System.out.println("Components changed: "+contentPanel.getComponentCount());
 		contentPanel.setBounds(80,60,720,480);
 		contentPanel.setOpaque(false);
 		panel.add(contentPanel);
 		updateUI();
 
 	}
 
 	/**
 	 * 
 	 * @author Michelle Len
 	 * Newton's Toolbox panel is a class that creates a JPanel area for drawing stuff associated with
 	 * drawing stuff needed for our Newton's Toolbox (EDIT LATER K THX)
 	 */
 	public class NewtonsToolboxPanel extends JPanel {     
 
 		public NewtonsToolboxPanel(){
 			super(null);
 		}
 		// Included to suppress Eclipse Warning
 		private static final long serialVersionUID = 1L;
 
 		/** 
 		 * This is method that will draw objects on our DamagePanel
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics) for more details
 		 */
 		public void paintComponent(Graphics g) {
 
 			Graphics2D g2d = (Graphics2D) g;
 
 			// Enable anti-aliasing for smoothing out shapes :)
 			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 
 			// Draws the Outer Blue Quadrilateral
 			Quadrilateral blueQuadrilateral = new Quadrilateral();
 			blueQuadrilateral.addPoint((int)(0.03125*this.getWidth()), (int)(0.06250*this.getHeight())); //  1/32(W) &  1/16(H)
 			blueQuadrilateral.addPoint((int)(this.getWidth()),         (int)(0)); // 32/32(W) &  1/32(H)
 			blueQuadrilateral.addPoint((int)(0.90625*this.getWidth()), (int)(this.getHeight())); // 29/32(W) & 32/32(H)
 			blueQuadrilateral.addPoint((int)(0.06250*this.getWidth()), (int)(0.93750*this.getHeight())); //  1/16(W) & 15/16(H)
 			g2d.setColor(new Color(0x3399CC));	// Web-safe Color. Can change if you guys don't like it.
 			g2d.drawPolygon(blueQuadrilateral);
 			g2d.fillPolygon(blueQuadrilateral);
 
 			//Scratch code
 			//	    		Quadrilateral blueQuadrilateral = new Quadrilateral();
 			//	    		blueQuadrilateral.addPoint((int)(0.03125*this.getWidth()), (int)(0.06250*this.getHeight())); //  1/32(W) &  1/16(H)
 			//	    		blueQuadrilateral.addPoint((int)(0.96875*this.getWidth()), (int)(0.03125*this.getHeight())); // 31/32(W) &  1/32(H)
 			//	    		blueQuadrilateral.addPoint((int)(0.90625*this.getWidth()), (int)(0.96875*this.getHeight())); // 29/32(W) & 31/32(H)
 			//	    		blueQuadrilateral.addPoint((int)(0.06250*this.getWidth()), (int)(0.93750*this.getHeight())); //  1/16(W) & 15/16(H)
 			//	    		g2d.setColor(new Color(0x3399CC));	// Web-safe Color. Can change if you guys don't like it.
 			//	    		g2d.drawPolygon(blueQuadrilateral);
 			//	    		g2d.fillPolygon(blueQuadrilateral);
 
 			//	    		// Draws the Inner Lighter Blue Quadrilateral
 			//	    		Quadrilateral lighterBlueQuadrilateral = new Quadrilateral();
 			//	    		lighterBlueQuadrilateral.addPoint((int)(0.062500*this.getWidth()), (int)(0.093750*this.getHeight())); //  1/32(W) & 15/16(H)
 			//	    		lighterBlueQuadrilateral.addPoint((int)(0.718750*this.getWidth()), (int)(0.093750*this.getHeight())); // 23/32(W) & 15/16(H)
 			//	    		lighterBlueQuadrilateral.addPoint((int)(0.703125*this.getWidth()), (int)(0.250000*this.getHeight())); // 45/64(W) &  1/4 (H)
 			//	    		lighterBlueQuadrilateral.addPoint((int)(0.093750*this.getWidth()), (int)(0.281250*this.getHeight())); //  1/16(W) &  9/32(H)
 			//	    		g2d.setColor(new Color(0x66CCFF));	// Web-safe Color. Can change if you guys don't like it.
 			//	    		g2d.drawPolygon(lighterBlueQuadrilateral);
 			//	    		g2d.fillPolygon(lighterBlueQuadrilateral);
 			//	    		
 			//	    		
 			//	    		// Color is set to White for Search Box
 			//	    		g2d.setColor(Color.WHITE);
 			//	    		g2d.fillRect((int)(0.0937500*this.getWidth()), (int)(0.1250000*this.getHeight()),  //  3/32 (W) &  1/8  (H)
 			//	    				     (int)(0.6015625*this.getWidth()), (int)(0.1015625*this.getHeight())); // 77/128(W) & 13/128(H)
 		}
 
 	}//NewtonToolBoxPanel Delimit
 
 	//Button Listener Classes:
 	class quitListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 	}
 
 	class minimizeListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			frame.setState(Frame.ICONIFIED);
 		}
 	}
 
 	class searchListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			changeContent(new GUISearch());
 		}
 	}
 
 	class printListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			changeContent(new GUIPrint());
 		}
 	}
 
 	class solveListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	class addListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			changeContent(new GUIAdd());
 		}
 	}
 
 	/* Calls updateUI on all sub-components of the JFrame */
	public static void updateUI() {
 		SwingUtilities.updateComponentTreeUI(frame);
 	}
 
 	// I'm sticking this Main here so I can Ctrl+F11 and view what's drawn
 	public static void main (String[] args) {
 
 		GUIMain gui = new GUIMain();
 		gui.go();
 	}
 
 }
