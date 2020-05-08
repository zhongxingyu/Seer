 package FanoronaGame;
 
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 public class FanoronaGame extends JPanel implements ActionListener {
 	JFrame window = new JFrame("Fanorona Game");
 
 	// Game Menu variables
 	JMenuBar mainMenu = new JMenuBar();
 	JMenuItem newGame = new JMenuItem("New Game"), about = new JMenuItem(
 			"About"), instructions = new JMenuItem("Instructions"),
 			exit = new JMenuItem("Exit");
 	// New Game buttons
 	Object[] options = { "Easy", "Medium", "Hard" };
 	Object[] remote = { "Human vs. CPU", "Client Server" };
 
 	// Buttons for Menus
	JButton pve = new JButton("Player vs CPU"), back = new JButton("exit");
 	GamePieces buttonArray[][] = new GamePieces[9][5];
 
 	// Panels for Graphic interface
 	JPanel newGamePanel = new JPanel(), northPanel = new JPanel(),
 			southPanel = new JPanel(), topPanel = new JPanel(),
 			bottomPanel = new JPanel();
 	static JPanel playingFieldPanel = new JPanel(); //static so it can be accessed outside this class
 	DrawBoard visibleBoard = new DrawBoard();
 	JLabel gameTitle = new JLabel("Fanorona");
 	//JTextArea text = new JTextArea();
 
 	// Board gameBoard = new Board(); //this will work soon
 
 	// set window size and default color
 	int windowX = 900, windowY = 500, color = 190;
 
 	
 	
 	public FanoronaGame() throws IOException {
 	//	class gameGrid extends JPanel {
 			
 			
 			/*public void paintComponent(Graphics g) {
 				int x = getWidth();
 				int y = getHeight();
 				g.setColor(Color.black);
 				g.fillRect(0, 0, x, y);
 
 				Graphics2D g2 = (Graphics2D) g;
 				int w = x / 10;
 				int h = y / 10;
 				g.setColor(Color.cyan);
 
 				g2.setStroke(new BasicStroke(1));
 				for (int i = 1; i < 10; i++)
 					g2.drawLine(i * w, 0, i * w, y);
 				for (int i = 1; i < 10; i++)
 					g2.drawLine(0, i * h, x, i * h);
 				
 				 * g2.setColor(Color.red); double rowH = getHeight() / 10.0; for
 				 * (int i = 1; i < 10; i++) { Line2D line = new
 				 * Line2D.Double(0.0, (double) i * rowH, (double) getWidth(),
 				 * (double) i * rowH); g2.draw(line); }
 				 */
 			//}
 		
 
 		// Game window formatting
 		window.setSize(windowX, windowY);
 		window.setLocation(400, 400);
 		window.setResizable(false);
 		window.setLayout(new BorderLayout());
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 	
 		// Panel properties
 		newGamePanel.setLayout(new GridLayout(2, 1, 2, 10));
 		northPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		southPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 
 		northPanel.setBackground(new Color(color - 20, color - 20, color - 20));
 		southPanel.setBackground(new Color(color, color, color));
 		bottomPanel.setBackground(new Color(color, color, color));
 		topPanel.setBackground(new Color(color, color, color));
 
 		// Create Menu Bars
 		mainMenu.add(newGame);
 		mainMenu.add(about);
 		mainMenu.add(instructions);
 		mainMenu.add(exit);
 
 		newGamePanel.add(pve);
 
 		// Add action listeners
 		newGame.addActionListener(this);
 		exit.addActionListener(this);
 		instructions.addActionListener(this);
 		about.addActionListener(this);
 		back.addActionListener(this);
 		pve.addActionListener(this);
 
 		// Game board setup
 		//playingFieldPanel.add(visibleBoard);
 		
 		BufferedImage myPicture = null;
 		Image pic = null; //pic that is scaled
 		try {
 			//myPicture = ImageIO.read(new File("H:/csce315/killme/FanoronaGame/src/FanoronaGame/board.jpg"));
                         myPicture = ImageIO.read(new File("C:/Users/Meg/Documents/CSCE 315/ProjectNumberTwo/FanoronaGame/src/fanoronagame/board.jpg"));
 			pic = myPicture.getScaledInstance(800, 400, 0); //how far spaced the buttons are seems to be determined by how large this is scaled..
 		} catch (IOException e) {
 			System.out.println("Could not load image.");
 		}
 		
 		JLabel picLabel = new JLabel(new ImageIcon( pic ));
                 
                 GridLayout grid = new GridLayout(5,9,15,8);
                 grid.setHgap(30);
                 grid.setVgap(20);
 		
 		picLabel.setLayout(grid);
 
 		//\playingFieldPanel.setOpaque(true);
 		playingFieldPanel.setBackground(new Color(110,110,110));
 
 		
 		
 		//THIS IS TO FIND X Y COORDINATES ON THE CURRENT PLANE OR PIC BY CLICKING
 //		picLabel.addMouseListener(new MouseListener() {
 //            public void mouseClicked(MouseEvent e) {
 //                System.out.println(" (" + e.getX() + ", " + e.getY() + ") ;");
 //            }
 //
 //
 //			@Override
 //			public void mouseEntered(MouseEvent arg0) {
 //				// TODO Auto-generated method stub
 //				
 //			}
 //
 //			@Override
 //			public void mouseExited(MouseEvent arg0) {
 //				// TODO Auto-generated method stub
 //				
 //			}
 //
 //			@Override
 //			public void mousePressed(MouseEvent arg0) {
 //				// TODO Auto-generated method stub
 //				
 //			}
 //
 //			@Override
 //			public void mouseReleased(MouseEvent arg0) {
 //				// TODO Auto-generated method stub
 //				
 //			}
 //		});
 
 		playingFieldPanel.add( picLabel );
 		playingFieldPanel.setBounds(77, 70, 770, 390); //not sure if this is used?
 
 		// Create Black Pieces
 		for (int x = 0; x < 9; x++) {
 			for(int y =0; y < 2; y++) {
 				buttonArray[x][y] = new GamePieces('B');
 			//	buttonArray[x][y].setLocation(79+x*85, 70+y*77);
 				buttonArray[x][y].setBackground(new Color(0, 0, 0));
 				buttonArray[x][y].addActionListener(this);
 				buttonArray[x][y].setVisible(true);
 				
 				picLabel.add(buttonArray[x][y]);
 			}
 		}
 
 		// Manually set Row 3
 		buttonArray[0][2] = new GamePieces('W');
 		//buttonArray[0][2].setLocation(82, 230);
 		//buttonArray[0][2].setAlignmentX(82); //DONT THINK THIS LINE
 		//buttonArray[0][2].setAlignmentY(230); //OR THIS ONE ARE NEEDED. BUT IT TELLS YOU WHERE THE PIECES ARE
 		buttonArray[0][2].setBackground(new Color(255, 255, 255)); //white
 		buttonArray[0][2].addActionListener(this);
 		buttonArray[0][2].setVisible(true);
 		picLabel. add(buttonArray[0][2]);
 
 		buttonArray[1][2] = new GamePieces('B');
 		//buttonArray[1][2].setLocation(167, 230);
 		//buttonArray[1][2].setAlignmentX(167);
 		//buttonArray[1][2].setAlignmentY(230);
 		buttonArray[1][2].setBackground(new Color(0, 0, 0)); //black
 		buttonArray[1][2].addActionListener(this);
 		buttonArray[1][2].setVisible(true);
 		picLabel.add(buttonArray[1][2]);
 
 		buttonArray[2][2] = new GamePieces('W');
 		//buttonArray[2][2].setLocation(252, 230);
 		//buttonArray[2][2].setAlignmentX(252);
 		//buttonArray[2][2].setAlignmentY(230);
 		buttonArray[2][2].setBackground(new Color(255, 255, 255));
 		buttonArray[2][2].addActionListener(this);
 		buttonArray[2][2].setVisible(true);
 		picLabel.add(buttonArray[2][2]);
 
 		buttonArray[3][2] = new GamePieces('B');
 		//buttonArray[3][2].setLocation(167, 230);
 		//buttonArray[3][2].setAlignmentX(337);
 		//buttonArray[3][2].setAlignmentY(230);
 		buttonArray[3][2].setBackground(new Color(0, 0, 0));
 		buttonArray[3][2].addActionListener(this);
 		buttonArray[3][2].setVisible(true);
 		picLabel.add(buttonArray[3][2]);
 
 		buttonArray[4][2] = new GamePieces('E');
 		//buttonArray[4][2].setLocation(422, 230);
 		//buttonArray[4][2].setAlignmentX(422);
 		//buttonArray[4][2].setAlignmentY(230);
 		buttonArray[4][2].setBackground(new Color(110,110,110)); //background pic color (dark grey)
 		buttonArray[4][2].setContentAreaFilled(false); //trying to make it invisible
 		buttonArray[4][2].setBorderPainted(false);	//but still clickable.. maybe setEnabled(true)
 		buttonArray[4][2].addActionListener(this);	// and have setVisible(false)?
 		//buttonArray[4][2].setVisible(false);
 		picLabel.add(buttonArray[4][2]);
 
 		buttonArray[5][2] = new GamePieces('W');
 		//buttonArray[5][2].setLocation(507, 230);
 		//buttonArray[5][2].setAlignmentX(507);
 		//buttonArray[5][2].setAlignmentY(230);
 		buttonArray[5][2].setBackground(new Color(255, 255, 255));
 		buttonArray[5][2].addActionListener(this);
 		buttonArray[5][2].setVisible(true);
 		picLabel.add(buttonArray[5][2]);
 
 		buttonArray[6][2] = new GamePieces('B');
 		//buttonArray[6][2].setLocation(592, 230);
 		//buttonArray[6][2].setAlignmentX(592);
 		//buttonArray[6][2].setAlignmentY(230);
 		buttonArray[6][2].setBackground(new Color(0, 0, 0));
 		buttonArray[6][2].addActionListener(this);
 		buttonArray[6][2].setVisible(true);
 		picLabel.add(buttonArray[6][2]);
 
 		buttonArray[7][2] = new GamePieces('W');
 		//buttonArray[7][2].setLocation(677, 230);
 		//buttonArray[7][2].setAlignmentX(677);
 		//buttonArray[7][2].setAlignmentY(230);
 		buttonArray[7][2].setBackground(new Color(255, 255, 255));
 		buttonArray[7][2].addActionListener(this);
 		buttonArray[7][2].setVisible(true);
 		picLabel.add(buttonArray[7][2]);
 
 		buttonArray[8][2] = new GamePieces('B');
 		//buttonArray[8][2].setLocation(762, 230);
 		//buttonArray[8][2].setAlignmentX(762);
 		//buttonArray[8][2].setAlignmentY(230);
 		buttonArray[8][2].setBackground(new Color(0, 0, 0));
 		buttonArray[8][2].addActionListener(this);
 		buttonArray[8][2].setVisible(true);
 		picLabel.add(buttonArray[8][2]);
 
 		// Create White Pieces
 		for(int y = 3; y < 5; y++ ) {
 			for(int x = 0; x < 9; x++) {
 				buttonArray[x][y] = new GamePieces('W');
 				//buttonArray[x][y].setLocation(79+x*85, 70+y*77);
 				//buttonArray[x][y].setAlignmentX(82 + x*85);
 				//buttonArray[x][y].setAlignmentY(230 + y*77);
 				buttonArray[x][y].setBackground(new Color(255, 255, 255));
 				buttonArray[x][y].addActionListener(this);
 				buttonArray[x][y].setVisible(true);
 				picLabel.add(buttonArray[x][y]);
 			}
 		}
 		playingFieldPanel.setVisible(true);
 
 		northPanel.add(mainMenu);
 		southPanel.add(gameTitle);
 
 		window.add(northPanel, BorderLayout.NORTH);
 		window.add(southPanel, BorderLayout.CENTER);
 		window.setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent click) {
 		Object actionSource = click.getSource(); //Get the source of what object is having an action performed
 		if (actionSource == newGame) { //If you click the button New Game
 			int option = JOptionPane.showOptionDialog(newGamePanel,
 								"Would you like to play over a server? Or directly with the CPU?"
 							+ " WARNING: All current progress will be lost.\n", "New Game",
 								JOptionPane.YES_NO_OPTION,
 								JOptionPane.QUESTION_MESSAGE, null, remote, remote[1]);
 //					"Would you like to play over a server? Or directly with the CPU?"
 //							+ " WARNING: All current progress will be lost.\n", "New Game",
 //                                                        JOptionPane.YES_NO_OPTION);
 			if (option == JOptionPane.YES_OPTION) {
 				int playOption = JOptionPane.showOptionDialog(newGamePanel,
 								"You've chosen to play against the CPU\n Select your difficulty", "Difficulty Level",
 								JOptionPane.YES_NO_CANCEL_OPTION,
 								JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
 				if (playOption == JOptionPane.OK_OPTION) { //Easy difficutly
 					window.remove(southPanel);
 					window.add(playingFieldPanel, BorderLayout.CENTER);
 					window.validate(); // is validate needed?
 					//window.setVisible(true);
 				} else if (playOption == JOptionPane.NO_OPTION) { //Medium difficutly
 					window.remove(southPanel);
 					window.add(playingFieldPanel, BorderLayout.CENTER);
 					window.validate();
 					//window.setVisible(true);
 				} else if (playOption == JOptionPane.CANCEL_OPTION) { //Hard difficulty
 					window.remove(southPanel);
 					window.add(playingFieldPanel, BorderLayout.CENTER);
 					window.validate();
 					//window.setVisible(true);
 				}
 
 			}
                         if(option == JOptionPane.NO_OPTION) {
                         	int playerOption = askMessage("You have chosen to play across a server, AI vs. AI \n Continue? \n",
                                                             "Client Server", JOptionPane.YES_NO_OPTION);
                             if(playerOption == JOptionPane.YES_OPTION) {
                         	int newOption = JOptionPane.showOptionDialog(newGamePanel,
 								"Ok great, choose the difficulty of the AIs", "Difficulty AI Level",
 								JOptionPane.YES_NO_CANCEL_OPTION,
 								JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
 					if (newOption == JOptionPane.OK_OPTION) { //Easy difficutly
 						window.remove(southPanel);
 						window.add(playingFieldPanel, BorderLayout.CENTER);
 						//Server e_srv = new Server(); //this doesn't work
 						//Client e_clt = new Client(); //this doesn't work
 						window.validate(); // is validate needed?
 						//window.setVisible(true);
 					} else if (newOption == JOptionPane.NO_OPTION) { //Medium difficutly
 						window.remove(southPanel);
 						window.add(playingFieldPanel, BorderLayout.CENTER);
 						//Server m_srv = new Server(); //this doesn't work
 						//Client m_clt = new Client(); //this doesn't work
 						window.validate();
 						//window.setVisible(true);
 					} else if (newOption == JOptionPane.CANCEL_OPTION) { //Hard difficulty
 						window.remove(southPanel);
 						window.add(playingFieldPanel, BorderLayout.CENTER);
 						//Server h_srv = new Server(); //this doesn't work
 						//Client h_clt = new Client(); //this doesn't work
 						window.validate();
 						//window.setVisible(true);
 					}
                             }  
                                 
                         }
                 }
                         
                 else if (actionSource == exit) { //If you clicked the Exit button
                     int exit_option = askMessage("Are you sure you want to exit?",
                                     "Exit Game", JOptionPane.YES_NO_OPTION);
                     if (exit_option == JOptionPane.YES_OPTION) //yes == close window. No, do nothing.
                             System.exit(0);
                 
 		} else if (actionSource == about) { //If you clicked the About button, tells who created it.
 			JOptionPane.showMessageDialog(null,
 					"This Game was created by Megan Kernis, Patrick Casey, and Matt Hacker.\n"
 							+ "Current Version: 0.1\n"
 							+ "Team 04, CSCE 315-501\n", "About",
 					JOptionPane.ERROR_MESSAGE);
                         
 		} else if (actionSource == instructions) { //The Instructions button, tells how to play the game and rules
 			JOptionPane.showMessageDialog(null,
                                         "Move your piece toward or way from an enemy piece to capture it.\n"
                                                         + "You may only move a piece that will cause the capture of an enemy piece.\n"
                                                         + "The Game will continue as long as there are valid move to be made.\n"
                                                         + "More instructions to follow...", "Instructions", 
                                         JOptionPane.ERROR_MESSAGE);
 		}
 				else if(actionSource instanceof GamePieces) {
 			if(source == null) {
 				source = (GamePieces)actionSource;
 			}
 			else if(source != null && (GamePieces)actionSource != source) {
 				target = (GamePieces)actionSource;
 				
 				if(source.legalMove(target)) {
 					target.setColor(source.getBackground());
 					source.setColor(new Color(0,0,0,0));
 					//if(checkCapture()) then capture
 				}else if(!(source.legalMove(target))){
 					JOptionPane.showMessageDialog(null, "Illegal Move", "Move Type", JOptionPane.ERROR_MESSAGE);
 				}
 				
 				source = null;
 				target = null;
 			}
 		}
 	
 	}
 
 	public int askMessage(String message, String title, int option) {
 		return JOptionPane.showConfirmDialog(null, message, title, option);
 	}
 
 	public static void main(String[] args) throws IOException {
 		FanoronaGame game = new FanoronaGame();
 		//Client client = new Client();
 		//Server server = new Server();
 	}
 }
