 package gui;
 
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import sharedfiles.Board;
 import sharedfiles.Piece;
 
 public class BoardGUIDevelopment {
 
 	private JFrame frame;
 	private String[][] pieces;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					BoardGUIDevelopment window = new BoardGUIDevelopment();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public BoardGUIDevelopment() {
 		readBoard(new Board());
 		initialize();
 	}
 
 	private void readBoard(Board board) {
 		Piece[][] temp = board.getBoardArray();
 		for (int i = 0; i < temp.length; i++) {
 			for (int j = 0; j < temp.length; j++) {
 				pieces[i][j] = temp[i][j].toString();
 			}
 		}
 	}
 
 	protected ImageIcon createImageIcon(String path, String description) {
 		java.net.URL url = getClass().getResource(path);
 		if (url != null) {
 			return new ImageIcon(url, description);
 		} else {
 			System.err.println("DERPYFILE COULDN'T BE LOCATED: " + path);
 			return null;
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 
 	private void initialize() {
 		frame = new JFrame();
 		frame.setSize(425, 425);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.setLocationRelativeTo(null);
 		int i = 8;
 		int j = 8;
 
 		GridLayout steven = new GridLayout(i, j);
 		steven.setHgap(2);
 		steven.setVgap(2);
 
 		JPanel[][] panelHolder = new JPanel[i][j];
 		frame.setLayout(steven);
 
 		for (int m = 0; m < i; m++) {
 			for (int n = 0; n < j; n++) {
 				panelHolder[m][n] = new JPanel();
 				panelHolder[m][n].setSize(60, 60);
 				frame.add(panelHolder[m][n]);
 			}
 		}
 
 		for (int m = 0; m < i; m++) {
 			for (int n = 0; n < j; n++) {
 				ImageIcon icon = createImageIcon("bBishop.png", "Derpy Spot");
 				Image img = icon.getImage();
 				BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null),
 						BufferedImage.TYPE_INT_ARGB);
 				Graphics g = bi.createGraphics();
 				g.drawImage(img, 0, 0, 50, 50, null);
 				JLabel label = new JLabel();
 				label.setIcon(new ImageIcon(bi));
 				panelHolder[m][n].add(label);
 			}
 		}
 
 		frame.getContentPane().setLayout(steven);
 	}
 
 }
