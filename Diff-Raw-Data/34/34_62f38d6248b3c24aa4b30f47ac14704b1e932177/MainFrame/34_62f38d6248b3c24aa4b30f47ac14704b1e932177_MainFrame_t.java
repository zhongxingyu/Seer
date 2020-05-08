 package screen;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.*;
 import main.Main;
 
 public class MainFrame extends JFrame {	
 	private final String[] players = {"Human", "Computer-Adam", "Computer-Mateusz", "Computer-Piotr"};	
 	
 	static private GamePanel pnbot;
 	
 	static private JButton playButton, stopButton, undoButton, exitButton;
 	static private JTextField txth, txtw, txtedges;
 	static private JComboBox cb1, cb2;
 	
 	MainFrame() {		
 		super("Paper Soccer");
 		
 		//size of the application
 		Dimension minSize = new Dimension(960,720);
 		
 		this.setSize(minSize);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setVisible(true);
 		this.setMinimumSize(minSize);
 		
 		//handling keyboard
 		class keyListener implements KeyListener {
 			@Override
 			public void keyPressed(KeyEvent arg0) {}
 			@Override
 			public void keyReleased(KeyEvent arg0) {}
 			@Override
 			public void keyTyped(KeyEvent e) {
 				if (e.getKeyChar() < '0' || e.getKeyChar() > '9') e.consume();
 			}
 		}
 		
 		keyListener kl = new keyListener();
 		
 		pnbot = new GamePanel();
 		
 		//creating menu
 		JPanel pntop = new JPanel();
 		pntop.setLayout(new GridLayout(3,1));
 		
 		JPanel pnl1 = new JPanel();
 		pnl1.setLayout(new FlowLayout());
 		pnl1.add(new JLabel("Select players"));
 		cb1 = new JComboBox();
 		for (int i=0; i<players.length; i++) cb1.addItem(players[i]);
 		pnl1.add(cb1);
 		pnl1.add(new JLabel("vs"));
 		cb2 = new JComboBox();
 		for (int i=0; i<players.length; i++) cb2.addItem(players[i]);
 		pnl1.add(cb2);
 		pntop.add(pnl1);
 		
 		JPanel pnl2 = new JPanel();
 		pnl2.setLayout(new FlowLayout());
 		pnl2.add(new JLabel("Enter height"));
 		txth = new JTextField(8);
 		txth.setText("10");
 		txth.addKeyListener(kl);
 		pnl2.add(txth);
 		pnl2.add(new JLabel("and width"));
 		txtw = new JTextField(8);
 		txtw.setText("8");
 		txtw.addKeyListener(kl);
 		pnl2.add(txtw);
 		pnl2.add(new JLabel("and max edge number"));
 		txtedges = new JTextField(8);
 		txtedges.setText("0");
 		txtedges.addKeyListener(kl);
 		pnl2.add(txtedges);
 		pntop.add(pnl2);
 		
 		
 		//creating buttons and their functions
 		playButton = new JButton("Play");
 		playButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Start");
 				try {
 					int bH = Integer.valueOf(txth.getText());
 					int bW = Integer.valueOf(txtw.getText());
 					int bE = Integer.valueOf(txtedges.getText());
 					if (bH < 8 || bW < 8 || bH > 16 || bW > 16
 						|| bH%2 != 0 || bW%2 != 0 || bE % 1 != 0 || bE < 0) throw new Exception();
 					pnbot.boardHeight = bH;
 					pnbot.boardWidth = bW;
 					if (bE == 0)
 						pnbot.maxEdges = bE;
 					else
 						pnbot.maxEdges = bE+1;
 					
 					pnbot.setClickable(true);
 					playButton.setEnabled(false);
 					stopButton.setEnabled(true);
 					undoButton.setEnabled(true);
 					cb1.setEnabled(false);
 					cb2.setEnabled(false);
 					txth.setEnabled(false);
 					txtw.setEnabled(false);
 					txtedges.setEnabled(false);
					Main.controller.start(pnbot.boardWidth, pnbot.boardHeight, pnbot.maxEdges, cb2.getSelectedIndex(), cb1.getSelectedIndex());
 				}
 				catch (Exception e) {
 					System.out.println("Exception");
 					JOptionPane.showMessageDialog(null, "Width and height dimmensions have to be even, not lower than 8 and not bigger than 16. In addition, only positive number values can be accepted.",
 							"Error", JOptionPane.ERROR_MESSAGE);
 				}				
 			}			
 		});
 		
 		stopButton = new JButton("Stop");
 		stopButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Main.controller.stop();
 				repaint();
 				playButton.setEnabled(true);
 				stopButton.setEnabled(false);
 				undoButton.setEnabled(false);
 				pnbot.setClickable(false);
 				cb1.setEnabled(true);
 				cb2.setEnabled(true);
 				txth.setEnabled(true);
 				txtw.setEnabled(true);
 				txtedges.setEnabled(true);
 				
 			}
 		});
 		
 		undoButton = new JButton("Undo");
 		undoButton.setToolTipText("Press ctrl to undo move and reflections");
 		undoButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if ((arg0.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
 					Main.controller.undo(0);
 				}
 				else {
 					Main.controller.undo(-1);
 				}
 				pnbot.repaint();
 			}			
 		});
 		
 		exitButton = new JButton("Exit");
 		exitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 					System.exit(0);
 			}			
 		});
 		
 		//adding buttons
 		JPanel pnl4 = new JPanel();
 		pnl4.setLayout(new FlowLayout());
 		pnl4.add(playButton);
 		pnl4.add(stopButton);
 		pnl4.add(undoButton);
 		pnl4.add(exitButton);
 		pntop.add(pnl4);
 		
 		stopButton.setEnabled(false);
 		undoButton.setEnabled(false);
 		
 		this.setLayout(new BorderLayout());
 		
 		this.add(pntop, BorderLayout.NORTH);		
 		
 		this.add(pnbot);
 	}
 	
 	protected static void unlockButtons() {
 		playButton.setEnabled(true);
 		stopButton.setEnabled(false);
 		undoButton.setEnabled(false);
 		cb1.setEnabled(true);
 		cb2.setEnabled(true);
 		txth.setEnabled(true);
 		txtw.setEnabled(true);
 		txtedges.setEnabled(true);
 	}
 	
 	public static void drawBoard() {
 		pnbot.repaint();
 	}
 	
 }
