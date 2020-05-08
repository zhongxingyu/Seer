 package clueGame;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 public class ClueGame extends JFrame {
 	private JMenuBar menuBar;
 	private Board board;
 	
 	public ClueGame() {
 		super();
 		
 		setTitle("Clue?");
 		setSize(new Dimension(800, 800));
 		setMinimumSize(new Dimension(500, 500));
 		
 		setLayout(new BorderLayout());
 		board = new Board();
 		
 		try {
 			board.loadConfigFiles("ClueBoardLegend.txt", "ClueBoardLayout.csv", "weapons.txt", "players.txt");
 		} catch (BadConfigFormatException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 		
 		add(board, BorderLayout.CENTER);
 		
 		buildMenu();
 	}
 	
 	private void buildMenu() {
 		JMenuItem item = new JMenuItem("Quit");
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		
 		JMenu menu = new JMenu("File");
 		menu.add(item);
 		menuBar = new JMenuBar();
 		menuBar.add(menu);
 		setJMenuBar(menuBar);
 	}
 
 	public static void main(String[] args) {
 		try {
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 		} catch (Exception e) {
 			// the program isn't running on a Mac
 		}
 		
 		ClueGame gui = new ClueGame();
 		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		gui.setVisible(true);
 	}
 }
