 package bomberman;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import javax.swing.*;
 import readSpielfeld.ReadFile;
 
 public class Spielfeld extends JFrame {
 	private static final long serialVersionUID = 1L;
	private JPanel jPanel = new JPanel(null, true);
 	private JButton startButton = new JButton();
 	private JButton beendenButton = new JButton();
 	private JLabel introBild = new JLabel(new ImageIcon("src/gfx/intro.jpg"));
 	private JLabel walls[][] = new JLabel[21][15];
 	private char[][] spielFeld;
 	private Action moveRight, moveLeft, moveUp, moveDown, moveRight2, moveLeft2, moveUp2, moveDown2, Bombe;
 	public Tuere exit;
 	private BomberMan player1 = new BomberMan(1);
 	private BomberMan player2 = new BomberMan(2);
 
 	Spielfeld(String title) {
 		super(title);
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		int frameWidth = 960;
 		int frameHeight = 750;
 		setSize(frameWidth, frameHeight);
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (d.width - getSize().width) / 2;
 		int y = (d.height - getSize().height) / 2;
 		setLocation(x, y);
 		setResizable(false);
 		Container cp = getContentPane();
 		cp.setLayout(null);
 
 		Color DARKGREEN = new Color(51, 153, 0);
 		jPanel.setBounds(60, 60, 840, 600);
 		jPanel.setBackground(DARKGREEN);
 		jPanel.setBorder(new javax.swing.border.LineBorder(Color.BLACK, 3));
 		cp.add(jPanel);
 		jPanel.add(introBild);
 		introBild.setBounds(1, 1, 840, 600);
 		startButton.setBounds(56, 680, 137, 25);
 		startButton.setText("Starten");
 		startButton.setMargin(new Insets(2, 2, 2, 2));
 		startButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				introBild.setVisible(false);
 				createWorld();
 				player1.put(jPanel);
 				player2.put(jPanel);
 			}
 		});
 		cp.add(startButton);
 
 		beendenButton.setBounds(760, 680, 137, 25);
 		beendenButton.setText("Beenden");
 		beendenButton.setMargin(new Insets(2, 2, 2, 2));
 		beendenButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				System.exit(0);
 			}
 		});
 		cp.add(beendenButton);
 		setVisible(true);
 
 		// Moves for first player
 		moveRight = new Move(player1, "right", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("D"), "moveRight");
 		startButton.getActionMap().put("moveRight", moveRight);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("D"), "moveRight");
 		beendenButton.getActionMap().put("moveRight", moveRight);
 
 		moveLeft = new Move(player1, "left", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("A"), "moveLeft");
 		startButton.getActionMap().put("moveLeft", moveLeft);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("A"), "moveLeft");
 		beendenButton.getActionMap().put("moveLeft", moveLeft);
 
 		moveUp = new Move(player1, "up", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("W"), "moveUp");
 		startButton.getActionMap().put("moveUp", moveUp);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("W"), "moveUp");
 		beendenButton.getActionMap().put("moveUp", moveUp);
 
 		moveDown = new Move(player1, "down", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("S"), "moveDown");
 		startButton.getActionMap().put("moveDown", moveDown);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("S"), "moveDown");
 		beendenButton.getActionMap().put("moveDown", moveDown);
 
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("K"), "Bombe");
 		startButton.getActionMap().put("Bombe", Bombe);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("K"), "Bombe");
 		beendenButton.getActionMap().put("Bombe", Bombe);
 
 		// Moves for second player
 		moveRight2 = new Move(player2, "right", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "moveRight2");
 		startButton.getActionMap().put("moveRight2", moveRight2);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "moveRight2");
 		beendenButton.getActionMap().put("moveRight2", moveRight2);
 
 		moveLeft2 = new Move(player2, "left", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "moveLeft2");
 		startButton.getActionMap().put("moveLeft2", moveLeft2);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "moveLeft2");
 		beendenButton.getActionMap().put("moveLeft2", moveLeft2);
 
 		moveUp2 = new Move(player2, "up", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("UP"), "moveUp2");
 		startButton.getActionMap().put("moveUp2", moveUp2);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("UP"), "moveUp2");
 		beendenButton.getActionMap().put("moveUp2", moveUp2);
 
 		moveDown2 = new Move(player2, "down", this);
 		startButton.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "moveDown2");
 		startButton.getActionMap().put("moveDown2", moveDown2);
 		beendenButton.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "moveDown2");
 		beendenButton.getActionMap().put("moveDown2", moveDown2);
 
 		// Bombe = new Bombe(bomberMan2, jPanel);
 		// bomberMan.setBombe((Bombe)Bombe);
 		// startButton.getInputMap().put(KeyStroke.getKeyStroke("released K"),"Bombe");
 		// startButton.getActionMap().put("Bombe",Bombe);
 		// beendenButton.getInputMap().put(KeyStroke.getKeyStroke("released K"),"Bombe");
 		// beendenButton.getActionMap().put("Bombe",Bombe);
 	}
 
 	private void createWorld() {
 		ReadFile rf = new ReadFile("src/readSpielfeld/ohneWaende.txt");
 		try {
 			this.spielFeld = rf.read();
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 		}
 		
 		for(int i=0; i<spielFeld.length; i++) {
 			for(int j=0; j<spielFeld[0].length; j++) {
 				// Unzerstrbare Wand
 				if(spielFeld[i][j] == 'X') {
 					walls[i][j] = new JLabel(new ImageIcon("src/gfx/solid_wall.png"));
 					walls[i][j].setBounds(40 * i, 40 * j, 40, 40);
 					walls[i][j].setName("nowalkable");
 					jPanel.add(walls[i][j]);
 				}
 				
 				// Zerstrbare Wand
 				if(spielFeld[i][j] == 'Z') {
 					walls[i][j] = new JLabel(new ImageIcon("src/gfx/explodable_wall.png"));
 					walls[i][j].setBounds(40 * i, 40 * j, 40, 40);
 					walls[i][j].setName("nowalkable");
 					jPanel.add(walls[i][j]);
 				}
 				
 				// Nichts
 				if(spielFeld[i][j] == 'O') {
 					walls[i][j] = new JLabel(new ImageIcon(""));
 					walls[i][j].setBounds(40 * i, 40 * j, 40, 40);
 					walls[i][j].setName("walkable");
 				}
 				
 				// Ausgang
 				if(spielFeld[i][j] == 'T' ) {
 					walls[i][j] = new JLabel(new ImageIcon("src/gfx/door/door.png"));
 					walls[i][j].setBounds(40 * i, 40 * j, 40, 40);
 					walls[i][j].setName("exit");
 					jPanel.add(walls[i][j]);
 					
 					JLabel[] toRemove = new JLabel[3];
 					toRemove[0] = this.player1.getBomberMan();
 					toRemove[1] = this.player2.getBomberMan();
 					toRemove[2] = walls[i][j];
 					
 					exit = new Tuere(i, j, jPanel, toRemove);
 				}
 				
 				// Player(s)
 				if(spielFeld[i][j] == 'P' || spielFeld[i][j] == 'L') {
 					walls[i][j] = new JLabel(new ImageIcon(""));
 					walls[i][j].setBounds(40 * i, 40 * j, 40, 40);
 					walls[i][j].setName("walkable");
 				}
 			}
 		}
 	}
 
 	public JLabel[][] getWalls() {
 		return walls;
 	}
 
 	public JButton getStartButton() {
 		return startButton;
 	}
 
 	public void setKeysBack() {
 		startButton.getActionMap().put("moveRight", moveRight);
 		startButton.getActionMap().put("moveLeft", moveLeft);
 		startButton.getActionMap().put("moveUp", moveUp);
 		startButton.getActionMap().put("moveDown", moveDown);
 	}
 
 	public static void main(String[] args) {
 		new Spielfeld("bomberman");
 	}
 }
