 package view;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * The main widows of the game. All graphics in the game is presented in this frame.
  * 
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class Window extends JFrame {
 	private JPanel activePanel = null;
 	
 	public Window() {
 		super();
 		this.setTitle(resources.Name.getGameName());
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(1000, 800);
 		this.setLocationRelativeTo(null);
 		
//		setUndecorated(true);
//		setExtendedState(JFrame.MAXIMIZED_BOTH); 
 		this.setAlwaysOnTop(true);
 		this.setVisible(true);
 	}
 	/**
 	 * Adds a JPanel to the window and removes the old ones.
 	 * @param p the panel to be added.
 	 */
 	public void add(JPanel p){
 		//TODO
 		super.add(p);
 
 		if (activePanel != null){
 			remove(activePanel);
 		}
 		activePanel=p;
 //		revalidate();
 //		invalidate();
 		validate();
 		p.repaint();
 		p.requestFocus();
 		p.validate();
 		
 	}
 
 	
 
 }
