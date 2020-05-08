 package eighteen;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import eighteen.Board.BadMoveException;
 import eighteen.Piece.adjLoc;
 
 public class FanoronaGUI extends JFrame {
 	//TODO if choice between pieces, pick attacking or retreating
 	//TODO end move button
	//TODO show whose turn it is
	//TODO show the utility value of the board (for the user)
	JButton helpb; //TODO add to a tool bar
 	Board board; //contains the array of pieces and board management functions
 	DrawnPiece[][] gamePieces;
 	DrawnPiece prevMoveDrawn;
 	Piece prevMove;
 	JLabel blackPieces,whitePieces,movesMade,utilityVal,currentTurn;
 	private String playerTurn = "WHITE";
 	Container game = getContentPane();
 	PieceListener clicked;//given to every piece in a loop
 	
 	Boolean isMoveState2 = false;
     public static FanoronaGUI GUI;
     
     public static void main(String[] args) {
        GUI =  new FanoronaGUI();
     }
     public void changeTurn()
     {
     	if (playerTurn=="WHITE")
     		playerTurn = "BLACK";
     	else
     		playerTurn = "WHITE";
     	
     }
     public void makeInfoPanel()
     {
     	JPanel InfoPanel = new JPanel();
     	GridLayout Lay = new GridLayout();
     	InfoPanel.setLayout(Lay);
     	InfoPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
     	blackPieces = new JLabel("Black: "+board.blacks);
     	whitePieces = new JLabel("White: "+board.whites);
     	movesMade = new JLabel("Move: "+board.moves);
     	utilityVal = new JLabel("Utility: "+board.Utility());
     	currentTurn = new JLabel("Turn: " + playerTurn);
     	InfoPanel.add(blackPieces);
     	InfoPanel.add(whitePieces);
     	InfoPanel.add(movesMade);
     	InfoPanel.add(utilityVal);
     	InfoPanel.add(currentTurn);
     	game.add(InfoPanel,BorderLayout.SOUTH);
     }
     public void updateInfoPanel()
     {
     	blackPieces.setText("Black: "+board.blacks);
     	whitePieces.setText("White: "+board.whites);
     	movesMade.setText("Move: "+board.moves);
     	utilityVal.setText("Utility: "+board.Utility());
     	currentTurn.setText("Turn: " + playerTurn);
     }
     public void makePieces()
     {
    	System.out.print("Making the pieces\n");
     	JPanel jBoard = new JPanel();
     	jBoard.setLayout(new GridLayout(board.ROWS,board.COLUMNS,15,15));
         for(int x= 0;x<board.ROWS;x++)
         {
         	for(int y = 0; y<board.COLUMNS; y++)
         	{
        		//System.out.print("Making the piece x:" + x + " y:" + y);
         		if(board.theBoard[x][y].getColor() == Color.BLACK)
         		{
         			gamePieces[x][y] = new DrawnPiece(x,y,Color.BLACK);
        			//gamePieces[x][y].setBackground(Color.BLACK);
         		}
         		else if(board.theBoard[x][y].getColor() == Color.WHITE)
         		{
         			gamePieces[x][y] = new DrawnPiece(x,y,Color.WHITE);
        			//gamePieces[x][y].setBackground(Color.WHITE);
         		}
         		else
         		{
         			gamePieces[x][y] = new DrawnPiece(x,y,Color.GRAY);
        			//gamePieces[x][y].setBackground(Color.GRAY);
         		}
         		jBoard.add(gamePieces[x][y]);
         	}
         }
         game.add(jBoard,BorderLayout.CENTER);
     }
     public void makeHelpButton()
     {
     	 helpb = new JButton("Help");
          helpb.setBounds(50,60,80,30);
          helpb.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent event) {
                  HelpGUI helpgui = new HelpGUI();
                  helpgui.setVisible(true);
              }
          });
          game.add(helpb,BorderLayout.NORTH);
     }
     public void makeMoveCancelButton()
     {
     	 helpb = new JButton("Cancel Move");
          helpb.setBounds(50,60,80,30);
          helpb.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent event) {
             	 isMoveState2 = false;
             	 resetPrevMove();
              }
          });
          game.add(helpb,BorderLayout.NORTH);
     }
     public void resetPrevMove()
     {
     	//eventually display that the move was reset visually
     	System.out.print("ResetPrevMove\n");
     	if(prevMove != null)
     	{
 	    	for(adjLoc p : prevMove.adjacentLocations)
 			{
 				if (gamePieces[p.row][p.column].pColor == Color.YELLOW)
 				{
 					gamePieces[p.row][p.column].pColor = Color.GRAY;
 					gamePieces[p.row][p.column].repaint();
 				}
 			}
 	    	prevMoveDrawn = null;
 	    	prevMove = null;
 	    	isMoveState2=false;
     	}
     }
     public void makePieceListeners()
     {
     	clicked = new PieceListener(); 
 		for(int x= 0;x<board.ROWS;x++)
         {
         	for(int y = 0; y<board.COLUMNS; y++)
         	{
 			gamePieces[x][y].addActionListener(clicked);
         	}
 		}
     }
     public void makeBoard()
     {
     	//this will make the diagonals
     }
     public FanoronaGUI() {
     	game.setLayout(new BorderLayout());
     	board = new Board(5,13);//eventually need user input here
     	gamePieces = new DrawnPiece[Board.ROWS][Board.COLUMNS];
     	prevMoveDrawn = new DrawnPiece(0,0,Color.GRAY);
     	
     	makePieces();
     	makeBoard();
     	//makeHelpButton();
     	makeInfoPanel();
     	makeMoveCancelButton();
     	makePieceListeners();
     	setSize(500,350);
         setTitle("Fanorona");
         
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
         setVisible(true);
     }
     class PieceListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
     	{
     		DrawnPiece gamePiece = (DrawnPiece) e.getSource();
     		Piece pieceClicked = board.theBoard[gamePiece.xLoc][gamePiece.yLoc];
     		//first check if the piece clicked is an available piece
     		if (gamePiece.pColor == Color.YELLOW && isMoveState2)//already selected
     		{
     			try
     			{
 	    			Move moveToPlay = null;
 					moveToPlay = new Move(prevMove, new Piece.adjLoc(pieceClicked), true, true);
 	    			if (board.isValidMove(moveToPlay))
 	    			{
 		    			//make the point clicked the color of the previous point clicked
 		    			gamePieces[pieceClicked.row][pieceClicked.column].pColor = prevMoveDrawn.pColor;
 		    			gamePieces[pieceClicked.row][pieceClicked.column].repaint();
 		    			//make the previous point turn gray
 		    			gamePieces[prevMoveDrawn.xLoc][prevMoveDrawn.yLoc].pColor = Color.GRAY;
 		    			gamePieces[prevMoveDrawn.xLoc][prevMoveDrawn.yLoc].repaint();
 		    			//modify potential moves back to gray
 		    	    	for(adjLoc p : prevMove.adjacentLocations)
 		    			{
 		    				if (gamePieces[p.row][p.column].pColor == Color.YELLOW)
 		    				{
 		    					gamePieces[p.row][p.column].pColor = Color.GRAY;
 		    					gamePieces[p.row][p.column].repaint();
 		    				}
 		    			}
 		    	    	//returns the list of pieces that need to be painted
 		    	    	ArrayList<Piece> piecesToColor = board.move(moveToPlay);
 		    	    	//re-color captured pieces
 		    	    	for(Piece p : piecesToColor)
 		    			{
 		    					gamePieces[p.row][p.column].pColor = Color.GRAY;
 		    					gamePieces[p.row][p.column].repaint();
 		    			}
 		    			
 		    	    	System.out.print("Moved to x:" + gamePiece.xLoc+" y:"+gamePiece.yLoc + "from x:" +prevMove.column +" y:"+prevMove.row +"\n" );
 		    	    	//set up for next turn
 		    	    	prevMoveDrawn = null;
 		    	    	prevMove = null;
 		    			isMoveState2 = false;
 		    			changeTurn();
 		    			updateInfoPanel();
 	    			}
 				} 
     			catch (BadMoveException e1) 
     			{
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
     		}
     		else if (gamePiece.pColor !=Color.YELLOW && gamePiece.pColor != Color.GRAY && !isMoveState2)
     		{
     			
     			//highlight all available moves, if not making a move, and if the piece clicked isn't gray
     			//THIS NEEDS TO TAKE INTO ACCOUNT OTHER CAPTURES ON THE BOARD EVENTUALLY
     			boolean ColorChanged = false;
 	    		for(adjLoc p : pieceClicked.adjacentLocations)
 	    		{
 	    			if (gamePieces[p.row][p.column].pColor == Color.GRAY)
 	    			{
 	    				ColorChanged = true;
 	    				gamePieces[p.row][p.column].pColor = Color.YELLOW;
 	    				gamePieces[p.row][p.column].repaint();
 	    			}
 	    			//gamePieces[prevMoveDrawn.xLoc][prevMoveDrawn.yLoc].pColor = Color.GRAY;
 	    		}
 	    		if(ColorChanged)
 	    		{
 	    			prevMoveDrawn=gamePiece;
 	    			prevMove=pieceClicked;
 	    			isMoveState2 = true;
 	    		}
 	    		System.out.print("Clicked x:" + gamePiece.xLoc+" y:"+gamePiece.yLoc+"\n");
     		}
     		
     		//System.out.print("Clicked x:" + gamePiece.xLoc+" y:"+gamePiece.yLoc+"\n");
     		
     		//WAIT FOR AI OR OPPONENT MOVE HERE
     		//System.out.print(isMoveState2 +" \n");
     		
     	}
     }
     
 }
