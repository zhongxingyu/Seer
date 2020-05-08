 package GUI;
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.lang.String;
 import java.lang.System;
 
 /**
  * Die Application is the main class of the passenger simulation.
  * 
  * @author Roger Jaggi
  * @version 1.0
  */
 public class Application extends JFrame implements WindowListener, KeyListener {
 
     // Singleton Pattern
     public static Application INSTANCE = new Application();
 
     final SimulationPanel simulationPanel = new SimulationPanel();
     final WorldEditor worldEditor = new WorldEditor();
     final JMenuBar menuBar = new JMenuBar();
 
     /**
      * Startup the application
      * @param args application arguments
      */
     public static void main(String[] args) {
         // Let's go
         Application.INSTANCE.setVisible(true);
     }
 
     private Application() {
         super("Passenger Simulation");
         initUI();
 
         loadSimulationPanel();
     }
 
     public final void initUI() {
 
         this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         this.setSize(800, 600);
         this.setLocationRelativeTo(null);
         this.addKeyListener(this);
 
         menuBar.setBounds(0,0,800,20);
         JMenu menu = new JMenu("Options");
         menuBar.add(menu);
 
         JMenuItem itemSimulation = new JMenuItem("Start Simulation (F2)");
         //itemSimulation.setMnemonic(KeyEvent.VK_F2);
         itemSimulation.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 loadSimulationPanel();
             }
         });
         menu.add(itemSimulation);
 
         JMenuItem itemWorldEditor = new JMenuItem("World Editor");
         itemWorldEditor.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 loadWorldEditor();
             }
         });
         menu.add(itemWorldEditor);
 
         this.setJMenuBar(menuBar);
         this.setVisible(true);
     }
 
     public void loadSimulationPanel() {
         // set the Panel size to the Window size
         simulationPanel.setBounds(0, 0, 800, 580);
 
         for(int m = 0; m < menuBar.getMenuCount(); ++m) {
             if(menuBar.getMenu(m).getAccessibleContext().getAccessibleName().equals("Add")){
                 menuBar.remove(menuBar.getMenu(m));
             }
         }
 
         this.getContentPane().removeAll();
         this.getContentPane().add(simulationPanel);
         this.revalidate();
         this.repaint();
     }
 
     public void loadWorldEditor() {
         boolean menuAdd = false;
         for(int m = 0; m < menuBar.getMenuCount(); ++m) {
             if(menuBar.getMenu(m).getAccessibleContext().getAccessibleName().equals("Add")){
                 menuAdd = true;
             }
         }
         if(!menuAdd) {
             JMenu menu = new JMenu("Add");
             JMenuItem itemRectangle = new JMenuItem("Rectangle");
             itemRectangle.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                    Rectangle rectangle = new Rectangle();
                    worldEditor.add(rectangle);
                    rectangle.newRectangle();
                    worldEditor.repaint();
                    System.out.println("Test");
                 }
             });
             JMenuItem itemEllipse = new JMenuItem("Ellipse");
             itemEllipse.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
 
                 }
             });
             menu.add(itemRectangle);
             menu.add(itemEllipse);
             menuBar.add(menu);
         }
 
         worldEditor.setBounds(0, 0, 800, 580);
         Application.INSTANCE.getContentPane().removeAll();
         Application.INSTANCE.getContentPane().add(worldEditor);
         Application.INSTANCE.revalidate();
         worldEditor.setupEditor();
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
 	 */
 	public void windowClosing(WindowEvent e) {
 		System.exit(0);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
 	 */
 	public void windowActivated(WindowEvent e) { }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
 	 */
 	public void windowClosed(WindowEvent e) { }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
 	 */
 	public void windowDeactivated(WindowEvent e) { }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
 	 */
 	public void windowDeiconified(WindowEvent e) { }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
 	 */
 	public void windowIconified(WindowEvent e) { }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
 	 */
 	public void windowOpened(WindowEvent e) { }
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
 	 */
 	public void keyPressed(KeyEvent e) {
 		// Event an den aktuellen Component 0 weiter geben
 		if( getContentPane().getComponentCount() > 0 && getContentPane().getComponent(0) instanceof KeyListener)
         {
             ((KeyListener)(getContentPane().getComponent(0))).keyPressed(e);
         }
 
         // F2-Key starts the simulation
         if ( e.getKeyCode() == KeyEvent.VK_F2) {
             loadSimulationPanel();
         }
 
         // F3-Key starts the world editor
         if ( e.getKeyCode() == KeyEvent.VK_F3) {
             loadWorldEditor();
         }
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
 	 */
 	public void keyReleased(KeyEvent e) {
         // Event an den aktuellen Component 0 weiter geben
         if( getContentPane().getComponentCount() > 0 && getContentPane().getComponent(0) instanceof KeyListener)
         {
             ((KeyListener)(getContentPane().getComponent(0))).keyReleased(e);
         }
     }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
 	 */
 	public void keyTyped(KeyEvent e) {
         // Event an den aktuellen Component 0 weiter geben
         if( getContentPane().getComponentCount() > 0 && getContentPane().getComponent(0) instanceof KeyListener)
         {
             ((KeyListener)(getContentPane().getComponent(0))).keyTyped(e);
         }
     }
 
 }
