 package UI;
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import model.*;
 
 
 public class GameBoardDisplay extends JPanel
 {
 	static final long serialVersionUID = 0L; // to shut up Eclipse
 	static Icon xPiece = new ImageIcon("mord.png");
 	static Icon oPiece = new ImageIcon("rigs.jpg");
 	private GameboardImp boardModel;
 	private JLabel[][] cells;
 	private JPanel board;
 	private JFrame frame;
 	private JLabel messages;
 	
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
 		return result;
 	}
 	
 	private void displayNewPiece(int xPosition, int yPosition, Icon piece) {
 			cells[xPosition][yPosition].setIcon(piece);
 			board.revalidate();
 	}
     
 	private void displayMessage(String message){
 		messages.setText(message);
 		board.revalidate();
 	}
 	
     public GameBoardDisplay(String username, String modeName)
     {
     	boardModel = new GameboardImp();
     	
        // This is the window that will be shown
     	frame = new JFrame ("Tic Tac Toe - " + modeName + " Mode");
         
         // set the size of the window
         frame.setSize(500, 500);
         
         // get players
         String[] players = Game.getPlayers(username);
         String player1 = players[0];
         String player2 = players[1];
         
         // add a label to the top of the window
         JLabel title = new JLabel(player1 + " vs " + player2);
         title.setFont(new Font("Serif", Font.BOLD, 36));  
         title.setForeground(Color.WHITE);
         
         // add the title label to the upper side of the window
         add(title, BorderLayout.NORTH); 
         
         // create the board size 3 by 3
         board = new JPanel();
         board.setLayout(new GridLayout(3,3));
 
         
         // show the board's borders
         Border whiteLine = BorderFactory.createLineBorder(Color.WHITE);
         board.setBackground(Color.BLACK);
         board.setPreferredSize(new Dimension(300, 300));
         
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
         board.add(cell_00);
         board.add(cell_01);
         board.add(cell_02);
         board.add(cell_10);
         board.add(cell_11);
         board.add(cell_12);
         board.add(cell_20);
         board.add(cell_21);
         board.add(cell_22);
         
         
         // add the board label to the lower side of the window
        add(board, BorderLayout.CENTER);         
         
         messages = new JLabel();
         messages.setFont(new Font("Serif", Font.BOLD, 24));  
         messages.setForeground(Color.WHITE);
         
         // add the messages label to the upper side of the window
        add(messages, BorderLayout.SOUTH); 
         
         
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
         frame.setResizable(false);    
         
         // show the window
         frame.setContentPane(this);
         frame.setVisible(true);
         setBackground(new Color(0,0,0));
     }
     
 }
