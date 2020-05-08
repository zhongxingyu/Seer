 package UI;
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import model.*;
 
 
 public class GameBoardDisplay extends JPanel
 {
 	static final long serialVersionUID = 0L; // to shut up Eclipse
 	static Icon xPiece = new ImageIcon("X.png");
 	static Icon oPiece = new ImageIcon("O.png");
 	private GameboardImp boardModel;
 	private JLabel[][] cells;
 	private JPanel boardPanel;
 	private JFrame frame;
 	private JLabel bottomMessage;
 	private JPanel buttonsPanel;
 	private JLabel buttonsMessage;
 	private String player1;
 	private String player2;
 	
 	public boolean attemptMove(int xPosition, int yPosition){
 		boolean result = false;
 		if(boardModel.getResult() == GameResult.PENDING){
     		if(boardModel.xsTurn()){
     			result = boardModel.requestMove(xPosition, yPosition, PlaceValue.X);
     			if(result){
         			displayNewPiece(xPosition, yPosition, xPiece);
     			}
     		}else if(boardModel.osTurn()){
     			result = boardModel.requestMove(xPosition, yPosition, PlaceValue.O);
     			if(result){
         			displayNewPiece(xPosition, yPosition, oPiece);
     			}
     		}
     	}
 		GameResult status = boardModel.getResult();
 		if (status == GameResult.PENDING){
 			if(boardModel.xsTurn()){
 				displayStatus(player1 + "'s Turn");
 			}
 			else {
 				displayStatus(player2 + "'s Turn");
 			}
 		}
 		else if (status == GameResult.CAT) {
 			displayStatus("Cat's game!");
 		}
 		else if (status == GameResult.OWIN) {
			displayStatus(player2 + " Wins!");
 		}
 		else if (status == GameResult.XWIN) {
			displayStatus(player1 + " Wins!");
 		}
 		return result;
 	}
 	
 	private void displayNewPiece(int xPosition, int yPosition, Icon piece) {
 		cells[xPosition][yPosition].setIcon(piece);
 		boardPanel.revalidate();
 	}
     
 	private void displayMessage(String message){
 		bottomMessage.setText(message);
 		boardPanel.revalidate();
 	}
 	
 	private void displayStatus(String message){
 		buttonsMessage.setText(message);
 		boardPanel.revalidate();
 	}
 	
     public GameBoardDisplay(String user1, String user2, String modeName)
     {
     	boardModel = new GameboardImp();
     	setLayout(new BorderLayout());
     	
        // This is the window that will be shown
     	frame = new JFrame ("Tic Tac Toe - " + modeName + " Mode");
         
         // set the size of the window
         frame.setSize(700, 500);
         
         // get players
         String[] players = Game.getPlayers(user1, user2);
         player1 = players[0];
         player2 = players[1];
         
         // add a label to the top of the window
         JLabel title = new JLabel(player1 + " vs " + player2);
         title.setFont(new Font("Serif", Font.BOLD, 36));  
         title.setForeground(Color.WHITE);
         
         // add the title label to the upper side of the window
 
         
         // create the board size 3 by 3
         boardPanel = new JPanel();
         boardPanel.setLayout(new GridLayout(3,3));
 
         
         // show the board's borders
         Border whiteLine = BorderFactory.createLineBorder(Color.WHITE);
         boardPanel.setBackground(Color.BLACK);
         boardPanel.setPreferredSize(new Dimension(300, 300));
         
         cells = new JLabel[3][3];
         
         //create cells
         JLabel cell_00 = new JLabel ();
         cell_00.setBorder(whiteLine);
         cells[0][0] = cell_00;
         JLabel cell_01 = new JLabel ();
         cell_01.setBorder(whiteLine);
         cells[0][1] = cell_01;
         JLabel cell_02 = new JLabel ();
         cell_02.setBorder(whiteLine);
         cells[0][2] = cell_02;
         JLabel cell_10 = new JLabel ();
         cell_10.setBorder(whiteLine);
         cells[1][0] = cell_10;
         JLabel cell_11 = new JLabel ();
         cell_11.setBorder(whiteLine);
         cells[1][1] = cell_11;
         JLabel cell_12 = new JLabel ();
         cell_12.setBorder(whiteLine);
         cells[1][2] = cell_12;
         JLabel cell_20 = new JLabel ();
         cell_20.setBorder(whiteLine);
         cells[2][0] = cell_20;
         JLabel cell_21 = new JLabel ();
         cell_21.setBorder(whiteLine);
         cells[2][1] = cell_21;
         JLabel cell_22 = new JLabel ();
         cell_22.setBorder(whiteLine);
         cells[2][2] = cell_22;
         
         
         // create a listener for all cells
         MouseListener listener00 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(0,0))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener01 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(0,1))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener02 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(0,2))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener10 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(1,0))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener11 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(1,1))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener12 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(1,2))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener20 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(2,0))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener21 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(2,1))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
         MouseListener listener22 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(2,2))displayMessage("Good move!"); else displayMessage("Can't place a piece there!");}};
          
         
         
         // link listeners to cells
         cell_00.addMouseListener(listener00);
         cell_01.addMouseListener(listener01);
         cell_02.addMouseListener(listener02);
         cell_10.addMouseListener(listener10);
         cell_11.addMouseListener(listener11);
         cell_12.addMouseListener(listener12);
         cell_20.addMouseListener(listener20);
         cell_21.addMouseListener(listener21);
         cell_22.addMouseListener(listener22);
         
         
         
         // add cells to the board
         boardPanel.add(cell_00);
         boardPanel.add(cell_01);
         boardPanel.add(cell_02);
         boardPanel.add(cell_10);
         boardPanel.add(cell_11);
         boardPanel.add(cell_12);
         boardPanel.add(cell_20);
         boardPanel.add(cell_21);
         boardPanel.add(cell_22);
         
         
         // add the board label to the lower side of the window
         
         buttonsMessage = new JLabel(player1 + "'s Turn");
         buttonsMessage.setFont(new Font("Serif", Font.BOLD, 28));  
         buttonsMessage.setForeground(Color.WHITE);
         
         buttonsPanel = new JPanel();
         buttonsPanel.setLayout(new GridLayout(3,1));
         buttonsPanel.setBackground(Color.BLACK);
         buttonsPanel.setPreferredSize(new Dimension(300, 100));
         
         buttonsPanel.add(buttonsMessage);
         buttonsPanel.add(new JLabel("Add buttons"));
         buttonsPanel.add(new JLabel("to this JPanel"));
        
 
         
         bottomMessage = new JLabel("Ready to play!");
         bottomMessage.setFont(new Font("Serif", Font.BOLD, 28));  
         bottomMessage.setForeground(Color.WHITE);
         
         // add the messages label to the upper side of the window
         add(title, BorderLayout.NORTH); 
         add(boardPanel, BorderLayout.CENTER);  
         add(buttonsPanel, BorderLayout.EAST);
         add(bottomMessage, BorderLayout.SOUTH); 
         
         
         // Place the window in the middle of the screen
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension frameSize = frame.getSize();
         if (frameSize.height > screenSize.height) {
             frameSize.height = screenSize.height;
         }
         if (frameSize.width > screenSize.width) {
             frameSize.width = screenSize.width;
         }
         frame.setLocation((screenSize.width - frameSize.width) / 2,
                           (screenSize.height - frameSize.height) / 2);         
         
         
         // set close operation
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // set window not resizable
        frame.setResizable(true);    
         
         frame.setIconImage(new ImageIcon("mord.png").getImage()); //If we want favicon for our window.
         
         // show the window
         frame.setContentPane(this);
         frame.setVisible(true);
         setBackground(Color.BLACK);
     }
     
 }
