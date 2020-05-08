 /********************************************************
 	Life.java
 	
 	Main class for Conway's Game of Life
 	Contains the main class, which instantiates all of the top level objects 
 		and catches the mouse movements and actions
 *********************************************************/
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.awt.Graphics;
 
 public class Life extends JFrame implements MouseMotionListener, MouseListener, ActionListener
 {
 	private static final int CELL_WIDTH		= 5;
 	private static final int CELL_HEIGHT		= 5;
 	private static final int MATRIX_WIDTH		= 500;
 	private static final int MATRIX_HEIGHT		= 500;
 	private static final int TOP_MATRIX		= 40;
 
 	private static final String MENU_CMD_GO		= "Go";
 	private static final String MENU_CMD_STOP	= "Stop";
 	private static final String MENU_CMD_FORWARD	= "Step Forward";
 	private static final String MENU_CMD_BACKWARD	= "Step Backward";
 	
 	// External classes
 	private LifeMatrix lf;
 	private LifeStats stats;
 	
 	// Local members
 	private JButton goButton;
 	private JMenuItem goMenuItem;
 	private JMenuItem stepForwardMenuItem;
 	private JMenuItem stepBackwardMenuItem;
 
 	public static void main (String[] args)
 	{
 		// Place the menu at the top, as appropriate for Mac
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 
 		Life jframe = new Life();		
 	}
 					
 	public Life()
 	{
 	
 		// Add Options menu bar
 		JMenuBar jbar = new JMenuBar();
 		setJMenuBar(jbar);
 		JMenu options = new JMenu("Options");
 		jbar.add(options);
 
 		// Add menu items to Options
 		goMenuItem = new JMenuItem(MENU_CMD_GO);
 		goMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G,
                                            java.awt.Event.META_MASK));
 		options.add(goMenuItem);
 		goMenuItem.addActionListener(this);
 
 		stepForwardMenuItem = new JMenuItem(MENU_CMD_FORWARD);
 		stepForwardMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
                                            java.awt.Event.META_MASK));
 		options.add(stepForwardMenuItem);
 		stepForwardMenuItem.addActionListener(this);
 
 		stepBackwardMenuItem = new JMenuItem(MENU_CMD_BACKWARD);
 		stepBackwardMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
                                            java.awt.Event.META_MASK));
 		options.add(stepBackwardMenuItem);
 		stepBackwardMenuItem.addActionListener(this);
 	
 	
 		// Layout the frame
 		Container cp = getContentPane();		
 		cp.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(5,5,5,5);
 
 		// Add Go button
 		goButton = new JButton(MENU_CMD_GO);
 		goButton.addActionListener(this);
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = .01;
 		c.weighty = .01;
 		c.gridx = 0;
 		c.gridy = 0;
 		cp.add(goButton, c);
 
 		// Add the LifeMatrix - the board displaying the Game of Life
 		lf = new LifeMatrix ( MATRIX_HEIGHT/CELL_HEIGHT, MATRIX_WIDTH/CELL_WIDTH, CELL_HEIGHT, CELL_WIDTH );
 		lf.addMouseListener(this);
 		lf.addMouseMotionListener(this);
 
 		// Add stats
 		stats 		= new LifeStats();
 		c.fill 		= GridBagConstraints.NONE;
 		c.weightx 	= .01;
 		c.weighty 	= .01;
 		c.gridx 	= 1;
 		c.gridy 	= 0;
 		cp.add(stats, c);
 
 		lf.addStats(stats);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weightx		= .99;
 		c.weighty		= .99;
 		c.gridx			= 0;
 		c.gridy			= 1;
 		c.gridwidth		= MATRIX_WIDTH;
 		c.gridheight		= MATRIX_HEIGHT;
 		cp.add(lf, c);
 
 		// Finalize the frame
 		pack();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
		//setResizable(false);
		setSize(510,600);		
 		setVisible(true);
 	}
 	
 	public void mouseMoved(MouseEvent e)
 	{
 		lf.hover(e.getPoint());
 	}
 
 	public void mouseDragged(MouseEvent e)
 	{
 	}
 	
 	public void actionPerformed (ActionEvent e)
 	/*
 		Catch the Go, Stop, Forward and Backward commands
 	*/
 	{
 		
 		String txt = (String)e.getActionCommand();
 		
 		// Chosing "Go" toggles the button to stop, and vice-versa
 		if (txt.equals(MENU_CMD_GO))
 		{
 			goButton.setText(MENU_CMD_STOP);
 			goMenuItem.setText(MENU_CMD_STOP);
 			goMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                                            java.awt.Event.META_MASK));
 			stepForwardMenuItem.setEnabled(false);
 			lf.go();
 		}
 		else if (txt.equals(MENU_CMD_STOP))
 		{
 			goButton.setText(MENU_CMD_GO);	
 			goMenuItem.setText(MENU_CMD_GO);
 			goMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G,
                                            java.awt.Event.META_MASK));
 			stepForwardMenuItem.setEnabled(true);
 			lf.stop();		
 		}
 		else if (txt.equals(MENU_CMD_FORWARD))
 		{
 			lf.stepForward();
 		}
 		else if (txt.equals(MENU_CMD_BACKWARD))
 		{
 			lf.stepBackward();
 		}
 	}
 
 	public void mousePressed(MouseEvent e) 
 	{
 		lf.hitCell(new Point(e.getX(), e.getY()));
 	}
 	
 
   	public void mouseClicked(MouseEvent e)
 	{	
 	}
 
 	public void mouseReleased(MouseEvent e) 
 	{
 	}
 	
 	public void mouseEntered(MouseEvent e) 
 	{
 	}
 	
 	public void mouseExited(MouseEvent e) 
 	{
 		lf.noHover();
 	}	
 
 }
 
