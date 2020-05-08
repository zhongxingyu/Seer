 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package twelve.team;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.WindowConstants;
 
 import twelve.team.Board.moveType;
 import twelve.team.Piece.Team;
 
 
 
 /**
  *
  * @author Matthew
  */
 public class VisualBoard extends JFrame implements MouseListener, MouseMotionListener   {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 3881233408615887932L;
 	/**
      * Creates new form VisualBoard
      */
     public VisualBoard() {
         initComponents();
         updateBoard();
     }
     
     public VisualBoard(GameController c) {
     	controller = c;
     	initComponents();
     	updateBoard();
     }
     
     public void paint(Graphics g){
     	super.paintComponents(g);
     	
     	timeLeftLabel.setText("Time Left: " + timeLeft);
     	whitePlayerLabel.setText("White Score: " + controller.getWins(Team.WHITE));
     	blackPlayerLabel.setText("Black Score: " + controller.getWins(Team.BLACK));
     }
         
     private void initComponents() {
 
 
     	whiteWins = controller.getWins(Team.WHITE);
     	blackWins = controller.getWins(Team.BLACK);
     	
         optionsButton = new JButton();
         boardPanel = new BoardPanel(this.controller.getBoard().getBoard());
         boardBackground = new JLabel();
         helpButton = new JButton();
         newGameButton = new JButton();
         whitePlayerLabel = new JLabel();
         blackPlayerLabel = new JLabel();
         currentTurnLabel = new JLabel();
         currentTurn = new JLabel();
         statusScrollPane = new JScrollPane();
         statusTextArea = new JTextArea();
         timeLeftLabel = new javax.swing.JLabel();
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setResizable(false);
 
         optionsButton.setText("Options");
 
         //boardPanel.setPreferredSize(new java.awt.Dimension(600, 300));
 
         try {
 			boardBackground.setIcon(new ImageIcon(ImageIO.read(new File("imgs/board.png"))));
 		} catch (IOException e) {
 			e.printStackTrace();
 		} // NOI18N
 
         GroupLayout boardPanelLayout = new GroupLayout(boardPanel);
         boardPanel.setLayout(boardPanelLayout);
         boardPanelLayout.setHorizontalGroup(
             boardPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addComponent(boardBackground)
         );
         boardPanelLayout.setVerticalGroup(
             boardPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(GroupLayout.Alignment.TRAILING, boardPanelLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(boardBackground))
         );
 
         helpButton.setText("Help");
 
         newGameButton.setText("New Game");
 
 
         whitePlayerLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         whitePlayerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         whitePlayerLabel.setText("White Score: " + controller.getWins(Team.WHITE));
 
         blackPlayerLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         blackPlayerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         blackPlayerLabel.setText("Black Score: " + controller.getWins(Team.WHITE));
 
 
         currentTurnLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         currentTurnLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         currentTurnLabel.setText("Current Turn:");
 
         currentTurn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         currentTurn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         currentTurn.setText("White");
 
         statusTextArea.setEditable(false);
         statusTextArea.setColumns(20);
         statusTextArea.setLineWrap(true);
         statusTextArea.setRows(5);
         statusTextArea.setText("Starting Game...");
         statusScrollPane.setViewportView(statusTextArea);
         
         timeLeftLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         timeLeftLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         timeLeftLabel.setText("Time Left: ");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(optionsButton)
                     .addComponent(newGameButton)
                     .addComponent(helpButton))
                 .addGap(18, 18, 18)
                 .addComponent(boardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(whitePlayerLabel)
                     .addComponent(blackPlayerLabel)
                     .addComponent(currentTurnLabel)
                     .addComponent(currentTurn)
                     .addComponent(timeLeftLabel)
                     .addComponent(statusScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {currentTurn, currentTurnLabel, whitePlayerLabel, blackPlayerLabel, statusScrollPane, timeLeftLabel});
 
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(newGameButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(optionsButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(helpButton)
                 .addGap(94, 94, 94))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(whitePlayerLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(blackPlayerLabel)
                 .addGap(28, 28, 28)
                 .addComponent(currentTurnLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(currentTurn)
                 .addGap(18, 18, 18)
                 .addComponent(timeLeftLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(statusScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(boardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         
         helpButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
                 new HelpPanel().setVisible(true);
 				
 			}
 		});
 
         newGameButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 controller.reset();
             }
         });
         
         optionsButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 controller.showOptions(true);
                 controller.showBoard();
                 controller.reset();
             }
         });
         
         boardPanel.addMouseListener(this);
         boardPanel.addMouseMotionListener(this);
         pack();
     }// </editor-fold>  
 
     public void setTurn(String turnText){
     	currentTurn.setText(turnText);
     	repaint();
     }
     
     public void setTimer(int time) {
     	timeLeft = time;
     	repaint();
     }
     
     public void updateBoard() {
     	boardPanel.setBoard(controller.getBoard().getBoard());
     	repaint();
     }
     
     public Point reversePoint(Point p){
     	return new Point(p.y, p.x);
     }
     
     public double distance(Point a, Point b){
     	return Math.pow((a.x-b.x), 2) + Math.pow(a.y-b.y, 2);
     }
     
     public void updateScore(int wWins, int bWins){
     	whitePlayerLabel.setText("White Score: " + whiteWins);
     	blackPlayerLabel.setText("Black Score: " + blackWins);
     }
     
     // Variables declaration - do not modify   
     
     //logical components -- should not be here, move to gameController
     
     GameController controller;
     
     //data for visuals
     private int whiteWins;
     private int blackWins;
     private int timeLeft;
     
     //visual components
     private JLabel boardBackground;
     private BoardPanel boardPanel;
     private static JLabel currentTurn;
     private JLabel currentTurnLabel;
     private JLabel timeLeftLabel;
     private JButton helpButton;
     private JButton newGameButton;
     private JButton optionsButton;
     private JLabel whitePlayerLabel;
     private JLabel blackPlayerLabel;
     private JScrollPane statusScrollPane;
     private JTextArea statusTextArea;
     private Point movingPiece;
     private boolean moving;
     
     // End of variables declaration                   
 	@Override
 	public void mouseDragged(MouseEvent e) {
		if(controller.isNetworked && controller.getTurn() != controller.network.localPlayer)
			return;
 		if(moving){
 			boardPanel.setPiecePosition(movingPiece, e.getPoint());
 //			boardPieces[movingPiece.x][movingPiece.y].x = e.getX();
 //			boardPieces[movingPiece.x][movingPiece.y].y = e.getY();
 			repaint();
 		}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
		if(controller.isNetworked && controller.getTurn() != controller.network.localPlayer)
			return;
		
 		Point p = boardPanel.closestPiece(e.getPoint());
 		if(boardPanel.PieceExists(p)){
 			if(controller.getTurn() != Team.WHITE && controller.getBoard().getBoard()[p.x][p.y].getTeam() == Team.WHITE)
 				return;
 			if(controller.getTurn() == Team.WHITE && controller.getBoard().getBoard()[p.x][p.y].getTeam() == Team.BLACK)
 				return;
 			try {
 				controller.move(reversePoint(p), null, moveType.SACRIFICE);
 			} catch (Exception e1) {
 				statusTextArea.setText("Error while sacrificing piece!");
 				updateBoard();
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
		if(controller.isNetworked && controller.getTurn() != controller.network.localPlayer)
			return;
		
 		Point p = e.getPoint();
 		//get starting position here
 		if(boardPanel.PieceExists((movingPiece = boardPanel.closestPiece(p)))){
 			if(controller.getTurn() != Team.WHITE && controller.getBoard().getBoard()[movingPiece.x][movingPiece.y].getTeam() == Team.WHITE)
 				return;
 			if(controller.getTurn() == Team.WHITE && controller.getBoard().getBoard()[movingPiece.x][movingPiece.y].getTeam() == Team.BLACK)
 				return;
 			
 			moving = true;
 			statusTextArea.setText("Moving Piece");
 			repaint();
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// check if the movement was valid using starting position and ending position
		if(controller.isNetworked && controller.getTurn() != controller.network.localPlayer)
			return;
		
 		if(!moving)
 			return;
 		moving = false;
 		final Point p = boardPanel.closestPiece(e.getPoint());
 		if(!boardPanel.PieceExists(p)){
 			try {
 				this.controller.move(this.reversePoint(movingPiece), this.reversePoint(p));
 			} catch (MoveException e1) {
 				moving = false;
 				final AdvanceFrame frame = new AdvanceFrame(this, true);
 				
 				frame.addWindowListener(new WindowAdapter(){
 					@Override
 					public void windowClosed(WindowEvent e){
 						try{
 							if(frame.getAdvance()){
 								controller.move(reversePoint(movingPiece), reversePoint(p), moveType.ADVANCE);
 							} else {
 								controller.move(reversePoint(movingPiece), reversePoint(p), moveType.RETREAT);
 							}
 						} catch(Exception e3){
 							controller.debug(e3.getLocalizedMessage());
 						}
 						updateBoard();
 					}
 				});
 				frame.setVisible(true);
 				//e1.printStackTrace();
 			} catch (Exception e1) {
 				statusTextArea.setText(e1.getMessage());
 				e1.printStackTrace();
 			}
 			updateBoard();
 			
 		} else if(p.x == -1){
 			Point origin = boardPanel.piecePosition(movingPiece.x, movingPiece.y);
 			boardPanel.setPiecePosition(movingPiece, origin);
 		} else {
 			statusTextArea.setText("Invalid Move");
 			Point origin = boardPanel.piecePosition(movingPiece.x, movingPiece.y);
 			boardPanel.setPiecePosition(movingPiece, origin);
 		}
 		repaint();
 	}
 }
