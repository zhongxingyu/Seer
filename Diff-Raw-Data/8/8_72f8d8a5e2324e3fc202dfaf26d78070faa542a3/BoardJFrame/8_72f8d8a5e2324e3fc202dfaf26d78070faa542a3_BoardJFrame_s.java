 package client;
 
 import java.awt.BorderLayout;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class BoardJFrame extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -9216930946594064363L;
 	// wymiary okienka
 	private final int WIDTH = 170;
 	private final int HEIGHT = 210;
 
 	private JLabel statusbar;
 	private MainMenuJFrame mainMenuJFrame;
 
 	public BoardJFrame(String host, int port, MainMenuJFrame m) {
 		mainMenuJFrame = m;
 		try {
 			// parametry okna
 			// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			setSize(WIDTH, HEIGHT);
 			setLocationRelativeTo(null);
 			setTitle("5-in-a-row");
 			// pasek stanu
 			statusbar = new JLabel("");
			add(statusbar, BorderLayout.SOUTH);
 			//nowa gra
 			add(new BoardJPanel(statusbar, host, port, this));
 			// wyswietlenie okna
 			setResizable(false);
 			setVisible(true);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void endOfGame() {
 		if(mainMenuJFrame != null)
 			mainMenuJFrame.setVisible(true);
 		dispose();
 	}
 
 	
 
 }
