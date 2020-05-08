 package snozama.ui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Line2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.StyledDocument;
 
 import snozama.amazons.global.GlobalFunctions;
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.settings.Settings;
 import snozama.ui.components.Line;
 import snozama.ui.components.MovementLayer;
 import snozama.ui.eventListeners.*;
 import snozama.ui.exception.TimeNotSetException;
 
 /**
  * 
  * @author Alex Yakovlev
  * 
  * Main UI For the Amazons Game
  * 
  *
  */
 
 public class AmazonUI extends AbstractAmazonUI
 {
 	
 	/**
 	 * Ready Functions to run when the UI is ready for them
 	 */
 	private static List<ReadyListener> readyFunctions = new ArrayList<ReadyListener>();
 	
 	/**
 	 * List of Amazon and Arrow move visuals.
 	 */
 	private static List<Line> movements = new ArrayList<Line>();
 	
 	/**
 	 * History of moves for quick visualization
 	 */
 	private static List<String> moveHistory = new ArrayList<String>();
 	/**
 	 * Current Move Number
 	 */
 	private static int currentMove;
 	/**
 	 * Move number if we are browsing history
 	 */
 	private static int historicMove;
 	
 	/**
 	 * Inherited Serial UID
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * Main Window Width
 	 */
 	private static final int WINDOW_WIDTH = 1192;
 	
 	/**
 	 * Main Window Height
 	 */
 	private static final int WINDOW_HEIGHT = 620;
 	
 	/**
 	 * Main Images used in the game
 	 */
 	private static BufferedImage boardImage;
 	private static BufferedImage boardImageLogo;
 	private static BufferedImage whiteQueenImage;
 	private static BufferedImage blackQueenImage;
 	private static BufferedImage wallImage;
 	
 	/**
 	 * Amazon Game Board passed in
 	 */
 	private Board board;
 	
 	/**
 	 * Axis labels
 	 */
 	private static String[][] labels = new String[2][10];
 	
 	/**
 	 * Width of a square on the board in pixels;
 	 */
 	private static final int SQUARE_WIDTH = 50;
 	/**
 	 * Distance from the left/top of the window to the board
 	 */
 	private static final int X_OFFSET = 40;
 	private static final int Y_OFFSET = 20;
 	
 	/** Swing Panels for the game */
 	private static JLayeredPane pane;
 	private static JPanel panel;
 	private static JLabel boardPanel;
 	private static JLayeredPane gameLayer;
 	private static MovementLayer movementLayer;
 	private static JTextPane log;
 	private static StyledDocument doc;
 	private static JScrollPane logScroll;
 	private static JLabel timerDisplay;
 	private static JLabel turnDisplay;
 	private static JLabel turnCountDisplay;
 	
 	private static TurnTimer turnTimer;
 	
 	/**
 	 * Auto-increment logId
 	 */
 	private static int logId = 1;
 	
 	private AmazonUI()
 	{
 		this.board = new Board();
 		initCommon();
 	}
 	
 	private AmazonUI( Board board )
 	{
 		this.board = board;
 		initCommon();
 	}
 	
 	private void initCommon()
 	{
 		currentMove = 0;
 		historicMove = 0;
 		initWindowSettings();
 		createMenuBar();
 		
 		setImages();
 	    createMainPanel();
 	    setUpLabels();
 	    
 	    createLogPanel();
 	    
 	    ready();
 	}
 	
 	/**
 	 * Get the UI
 	 * @return a new UI Instance
 	 */
 	public static AmazonUI getInstance()
 	{
 		return new AmazonUI();
 	}
 	
 	/**
 	 * Create the main panel for the UI.
 	 * Set all the colors/etc.
 	 * Add the game layer to it.
 	 */
 	private void createMainPanel()
 	{
 		pane = new JLayeredPane();
 		pane.setBounds(0,0,WINDOW_WIDTH,WINDOW_HEIGHT);
 		
 		panel = new JPanel( null );
 		pane.add(panel, new Integer(20));
 		
 		panel.setBounds(0,0,WINDOW_WIDTH,WINDOW_HEIGHT);
 		if (Settings.teamColour == Board.WHITE)
			panel.setBackground(new Color(250,250,250));
		else panel.setBackground(new Color(66,66,66));
 		
 		gameLayer = new JLayeredPane();
 		gameLayer.setBounds(X_OFFSET,Y_OFFSET,500,500);
 		
 		pane.add(gameLayer, new Integer( 100 ));
 		
 		movementLayer = new MovementLayer();
 		movementLayer.setBounds(X_OFFSET,Y_OFFSET,500,500);
 		
 		pane.add( movementLayer, new Integer( 50 ) );
 		
 		if( boardImage != null )
 		{
 			boardPanel = new JLabel( new ImageIcon( boardImageLogo ));
 			boardPanel.setBounds(X_OFFSET,Y_OFFSET,500,500);
 			panel.add(boardPanel,new Integer(5));
 		}
 		
 		// Get the amazon positions
 		readBoard();
 		
 		/* History Buttons */
 		JButton back = new JButton("Back");
 		JButton forward = new JButton("Forward");
 		
 		back.addActionListener( new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				historyBack();
 			}
 			
 		});
 		
 		forward.addActionListener( new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				historyForward();
 			}
 			
 		});
 		
 		back.setBounds(550,50,100,50);
 		forward.setBounds(670,50,100,50);
 		
 		panel.add(back);
 		panel.add(forward);
 		
 		timerDisplay = new JLabel( "Timer" );
 		
 		timerDisplay.setBounds( 790, 35, 20, 50 );
 		timerDisplay.setForeground( new Color( 255, 255, 255 ) );
 		
 		panel.add( timerDisplay );
 		
 		turnDisplay = new JLabel( "Turn" );
 		
 		turnDisplay.setBounds( 810, 35, 150, 50 );
 		turnDisplay.setForeground( new Color( 255, 255, 255 ) );
 		
 		panel.add( turnDisplay );
 		
 		turnCountDisplay = new JLabel( "Turn 0" );
 		turnCountDisplay.setBounds( 810, 50, 150, 50 );
 		turnCountDisplay.setForeground( new Color( 255, 255, 255 ) );
 		
 		panel.add( turnCountDisplay );
 		
 		add(pane);
 		
 	}
 	
 	/* Create the game log for seeing moves etc */
 	public void createLogPanel()
 	{
 		log = new JTextPane();
 		log.setEditable( Boolean.FALSE );
 		
 		
 		log.setText( "Log started." );
 		
 		logScroll = new JScrollPane( log );
 		logScroll.setBounds(X_OFFSET + boardPanel.getWidth() + 20, 120, 400, 400);
 		
 		doc = log.getStyledDocument();
 
 		panel.add( logScroll );
 	}
 	
 	/**
 	 * Reads the current board and places the Amazons and arrows appropriately.
 	 */
 	private void readBoard()
 	{
 		
 		if( blackQueenImage != null && whiteQueenImage != null )
 		{
 			for( int i = 0; i < 10; i ++ )
 			{
 				for( int j = 0; j < 10; j++ )
 				{
 					if(board.isBlack(i,j))
 					{
 						JLabel blackImage = new JLabel( new ImageIcon( blackQueenImage));
 						setPieceLocation( blackImage, i, j );
 						gameLayer.add(blackImage);
 					}
 					if(board.isWhite(i, j))
 					{
 						JLabel whiteImage = new JLabel( new ImageIcon( whiteQueenImage));
 						setPieceLocation( whiteImage, i, j );
 						gameLayer.add(whiteImage);
 					}
 					if(board.isArrow(i, j))
 					{
 						JLabel arrowImage = new JLabel( new ImageIcon( wallImage));
 						setPieceLocation(arrowImage, i, j);
 						gameLayer.add(arrowImage);
 					}
 				}
 			}
 		}
 	}
 	
 	public void post( String message )
 	{
 		try
 		{
 			doc.insertString( doc.getLength(), "\n" + logId++ + " | " + message, null );
 			log.setCaretPosition( log.getText().length() );
 		}
 		catch(BadLocationException e)
 		{
 			System.out.println( e.getStackTrace());
 		}
 	}
 	
 	/**
 	 * Move Amazon ? lol. This is an internal UI method. Please use AUI.moveAmazon()
 	 * @param row_s 
 	 * @param col_s
 	 * @param row_f
 	 * @param col_f
 	 * @param row_a
 	 * @param col_a
 	 * @return
 	 */
 	public Boolean moveAmazon( int row_s, int col_s, int row_f, int col_f, int row_a, int col_a)
 	{
 		int whoseMove = board.isWhite( row_s, col_s ) ? Board.WHITE : Board.BLACK;
 		
 		if( historicMove == currentMove )
 		{
 			/* Move Amazon */
 			movePiece(row_s, col_s, row_f, col_f);
 			
 			/* Place arrow */
 			placeArrow(row_a, col_a);
 			historicMove++;
 		}
 		
 		createMovementLine(row_s, col_s, row_f, col_f, whoseMove, false);
 		createMovementLine(row_f, col_f, row_a, col_a, whoseMove, true);
 		
 		/* Save move in history */
 		String move = row_s + "-" + col_s + "-" + row_f + "-" + col_f + "-" + row_a + "-" + col_a;
 		moveHistory.add(move);
 		
 		String message = whoseMove == Board.WHITE ? "White" : "Black";
 		message += " moved from " + labels[1][ row_s ] + "" + labels[0][ col_s ] + " to ";
 		message += labels[1][ row_f ] + "" + labels[0][ col_f ] 
 				+ " and shot an arrow to " + labels[1][ row_a ] + "" + labels[0][ col_a ];
 		post( message );
 		
 		board.move( row_s, col_s, row_f, col_f, row_a, col_a, whoseMove );
 		
 		currentMove++;
 		
 		// TODO: return based on whether move was successful.
 		return true;
 	}
 	
 	private void createMovementLine( int row_s, int col_s, int row_f, int col_f, int whoseMove, boolean arrow )
 	{
 		int x1 = col_s * SQUARE_WIDTH + SQUARE_WIDTH/2;
 		int y1 = row_s * SQUARE_WIDTH + SQUARE_WIDTH/2;
 		
 		int x2 = col_f * SQUARE_WIDTH + SQUARE_WIDTH/2;
 		int y2 = row_f * SQUARE_WIDTH + SQUARE_WIDTH/2;
 		
 		movementLayer.addMovement(new Line( x1, y1, x2, y2, whoseMove, arrow));
 		repaintMovementLayerByHistoricMove();
 	}
 	
 	private void repaintMovementLayerByHistoricMove()
 	{
 		movementLayer.setHistoricMove(historicMove);
 		movementLayer.repaint();
 	}
 	
 	/**
 	 * Add a new arrow piece to the board
 	 */
 	private void placeArrow( int row_a, int col_a )
 	{
 		JLabel arrowImage = new JLabel(new ImageIcon(wallImage));
 		setPieceLocation(arrowImage, row_a, col_a);
 		gameLayer.add(arrowImage);
 	}
 	
 	/**
 	 * Return the game to a previous state
 	 */
 	public void historyBack()
 	{
 		
 		if( historicMove > 0 )
 		{
 			String move = moveHistory.get( historicMove - 1 );
 			String[] params = move.split("-");
 			int col_s = Integer.parseInt(params[1]);
 			int row_s = Integer.parseInt(params[0]);
 			int col_f = Integer.parseInt(params[3]);
 			int row_f = Integer.parseInt(params[2]);
 			int col_a = Integer.parseInt(params[5]);
 			int row_a = Integer.parseInt(params[4]);
 			
 			/*Remove arrow*/
 			removePiece(row_a, col_a);
 			
 			/*Move the piece back */
 			movePiece( row_f, col_f, row_s, col_s );
 			
 			historicMove--;
 			
 			repaintMovementLayerByHistoricMove();
 		}
 	}
 	
 	/**
 	 * Advance the game to the next state
 	 */
 	public void historyForward()
 	{
 		if( historicMove < currentMove )
 		{
 			historicMove++;
 			
 			repaintMovementLayerByHistoricMove();
 			
 			String move = moveHistory.get( historicMove - 1 );
 			String[] params = move.split("-");
 			int col_s = Integer.parseInt(params[1]);
 			int row_s = Integer.parseInt(params[0]);
 			int col_f = Integer.parseInt(params[3]);
 			int row_f = Integer.parseInt(params[2]);
 			int col_a = Integer.parseInt(params[5]);
 			int row_a = Integer.parseInt(params[4]);
 			
 			movePiece(row_s, col_s, row_f, col_f);
 			placeArrow(row_a, col_a);
 		}
 	}
 	
 	/* Returns the JLabel at the given location
 	 * 
 	 */
 	private JLabel getPiece( int row_s, int col_s )
 	{
 		int x = col_s * SQUARE_WIDTH + 5;
 		int y = row_s * SQUARE_WIDTH + 5;
 		return (JLabel) gameLayer.findComponentAt( x, y );
 	}
 	
 	/**
 	 * Get an existing piece on the board and move it.
 	 */
 	private void movePiece( int row_s, int col_s, int row_f, int col_f)
 	{
 		setPieceLocation( getPiece( row_s, col_s), row_f, col_f );
 	}
 	
 	
 	/**
 	 * Removes a pieces at a given location
 	 * @param row_s row index of the piece
 	 * @param col_s col index of the piece
 	 */
 	private void removePiece( int row_s, int col_s )
 	{
 		JLabel piece = getPiece(row_s, col_s);
 		gameLayer.remove(piece);
 		gameLayer.repaint();
 	}
 	
 	/**
 	 * Set the location of a piece based on the current row and column
 	 * that it is in.
 	 * @param piece - JLabel piece that contains the image
 	 * @param row - int row position in byte array from Board
 	 * @param col - int col position in byte array from Board
 	 */
 	private void setPieceLocation( JLabel piece, int row, int col )
 	{
 		piece.setBounds( col*SQUARE_WIDTH + 5, row*SQUARE_WIDTH + 5, 40, 40 );
 		gameLayer.repaint();
 	}
 	
 	/**
 	 * Base Labels for the axises
 	 */
 	private void setUpLabels()
 	{
 		labels = Labels.INVERTED_ZERO;
 		
 		int top = SQUARE_WIDTH * 10 + Y_OFFSET;
 		int left = X_OFFSET + 20;
 		
 		Font font = new Font( "Verdana", 0, 14 );
 		
 		JLabel[] x_s =  new JLabel[10];
 		for( int i = 0; i < x_s.length ; i ++ )
 		{
 			x_s[i] = new JLabel( labels[0][i] );
 			x_s[i].setBounds( left, top, 30, 30 );
 			x_s[i].setFont( font );
 			x_s[i].setForeground( new Color( 240, 240, 240 ) );
 			x_s[i].setHorizontalTextPosition( SwingConstants.CENTER );
 			panel.add( x_s[i] );
 			
 			left+=SQUARE_WIDTH;
 		}
 		
 		top = Y_OFFSET + 10;
 		left = X_OFFSET - 25;
 		
 		JLabel[] y_s =  new JLabel[10];
 		for( int i = 0; i < 10 ; i ++ )
 		{
 			y_s[i] = new JLabel( labels[1][i] );
 			y_s[i].setBounds( left, top, 30, 30 );
 			y_s[i].setFont( font );
 			y_s[i].setForeground( new Color( 240, 240, 240 ) );
 			y_s[i].setHorizontalTextPosition( SwingConstants.RIGHT );
 			panel.add( y_s[i] );
 			
 			top+=SQUARE_WIDTH;
 		}
 		
 	}
 	
 	/**
 	 * Start a turn. Set the timer
 	 * @param whoseTurn
 	 * @param seconds
 	 */
 	public void startTurn( final int whoseTurn, final int seconds )
 	{
 		if( whoseTurn == Board.WHITE )
 		{
 			turnDisplay.setText( "WHITE is moving" );
 		}
 		else
 		{
 			turnDisplay.setText( "BLACK is moving" );
 		}
 		turnCountDisplay.setText( "Turn " + (currentMove + 1) );
 		if( turnTimer != null )
 		{
 			turnTimer.reset();
 		}
 		turnTimer = new TurnTimer( seconds );
 		try
 		{
 			turnTimer.interval( timerDisplay, new UITimerCompleteListener()
 			{
 				// This is the function when the timer completes.
 				@Override
 				public void complete()
 				{
 					// startTurn through AUI.startTurn called from the handleMessage
 					//startTurn( GlobalFunctions.flip( whoseTurn), seconds );
 				}
 			} );
 		}
 		catch( TimeNotSetException tnse )
 		{
 			System.out.println( tnse.getMessage() );
 		}
 	}
 	
 	public void endGame()
 	{
 		turnTimer.end(timerDisplay);
 	}
 	
 	/**
 	 * Change the current labels
 	 * @param type - Label._type
 	 */
 	private void changeLabels( String[][] type )
 	{
 		labels = type;
 		
 		int top = SQUARE_WIDTH * 10 + Y_OFFSET;
 		int left = X_OFFSET + 20;
 		
 		for( int i = 0; i < 10 ; i ++ )
 		{
 			JLabel t = (JLabel) panel.findComponentAt( left, top );
 			if( labels == null )
 			{
 				t.setText( "" );
 			}
 			else
 			{
 				t.setText( labels[0][i] );
 			}
 			left+=SQUARE_WIDTH;
 		}
 		
 		top = Y_OFFSET + 10;
 		left = X_OFFSET - 25;
 		
 		for( int i = 0; i < 10 ; i++ )
 		{
 			JLabel t = (JLabel) panel.findComponentAt( left, top );
 			if( labels == null )
 			{
 				t.setText( "" );
 			}
 			else
 			{
 				t.setText( labels[1][i] );
 			}
 			top+=SQUARE_WIDTH;
 		}
 		
 	}
 	
 	/**
 	 * Set up the main menu bar with possible options.
 	 */
 	private void createMenuBar()
 	{
 		JMenuBar menu = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem quit = new JMenuItem("Quit");
 		quit.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				System.exit(0);
 			}
 		} );
 		file.add(quit);
 		menu.add(file);
 		
 		/* Labels */
 		
 		JMenu view = new JMenu("View");
 		JMenu v_labels = new JMenu("Labels");
 		JMenuItem standard = new JMenuItem("Standard");
 		standard.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( Labels.STANDARD );
 			}
 		});
 		JMenuItem inverted = new JMenuItem("Inverted");
 		inverted.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( Labels.INVERTED );
 			}
 		});
 		JMenuItem standard_zero = new JMenuItem("Standard Zero-Based");
 		standard_zero.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( Labels.STANDARD_ZERO );
 			}
 		});
 		JMenuItem inverted_zero = new JMenuItem("Inverted Zero-Based");
 		inverted_zero.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( Labels.INVERTED_ZERO );
 			}
 		});
 		JMenuItem letters = new JMenuItem("Letters");
 		letters.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( Labels.LETTERS );
 			}
 		});
 		JMenuItem numbers = new JMenuItem("Numbers");
 		numbers.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( Labels.NUMBERS );
 			}
 		});
 		JMenuItem numbers_zero = new JMenuItem("Numbers Zero-Based");
 		numbers_zero.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( Labels.NUMBERS_ZERO );
 			}
 		});
 		JMenuItem none = new JMenuItem("None");
 		none.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				changeLabels( null );
 			}
 		});
 		v_labels.add( standard );
 		v_labels.add( inverted );
 		v_labels.add( standard_zero );
 		v_labels.add( inverted_zero );
 		v_labels.add( letters );
 		v_labels.add( numbers );
 		v_labels.add( numbers_zero );
 		v_labels.add( none );
 		
 		view.add( v_labels );
 		
 		JCheckBoxMenuItem seeBoard = new JCheckBoxMenuItem( "Board" );
 		seeBoard.setSelected( Boolean.TRUE );
 		
 		seeBoard.addActionListener( new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				JCheckBoxMenuItem src = (JCheckBoxMenuItem) event.getSource();
 				if( src.isSelected() )
 				{
 					boardPanel.setVisible( Boolean.TRUE );
 				}
 				else
 				{
 					boardPanel.setVisible( Boolean.FALSE );
 				}
 			}
 		} );
 		
 		JCheckBoxMenuItem seeGameLayer = new JCheckBoxMenuItem( "Game Layer" );
 		seeGameLayer.setSelected( Boolean.TRUE );
 		seeGameLayer.addActionListener( new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				JCheckBoxMenuItem src = (JCheckBoxMenuItem) event.getSource();
 				if( src.isSelected() )
 				{
 					gameLayer.setVisible( Boolean.TRUE );
 				}
 				else
 				{
 					gameLayer.setVisible( Boolean.FALSE );
 				}
 			}
 		} );
 		
 		JCheckBoxMenuItem seeMovementLayer = new JCheckBoxMenuItem( "Movement Layer" );
 		seeMovementLayer.addActionListener( new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				JCheckBoxMenuItem src = (JCheckBoxMenuItem) event.getSource();
 				if( src.isSelected() )
 				{
 					movementLayer.setVisible( Boolean.TRUE );
 				}
 				else
 				{
 					movementLayer.setVisible( Boolean.FALSE );
 				}
 			}
 		} );
 		
 		seeBoard.setEnabled( Boolean.FALSE );
 		seeGameLayer.setEnabled( Boolean.FALSE );
 		
 		view.add( seeBoard );
 		view.add( seeGameLayer );
 		view.add( seeMovementLayer );
 		
 		JCheckBoxMenuItem logo = new JCheckBoxMenuItem( "Logo" );
 		logo.setSelected( Boolean.TRUE );
 		
 		logo.addActionListener( new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				JCheckBoxMenuItem src = (JCheckBoxMenuItem) event.getSource();
 				if( src.isSelected() )
 				{
 					boardPanel.setIcon( new ImageIcon( boardImageLogo ) );
 				}
 				else
 				{
 					boardPanel.setIcon( new ImageIcon( boardImage ) );
 				}
 			}
 		} );
 		
 		view.add( logo );
 		
 		menu.add( view );
 		
 		setJMenuBar(menu);
 	}
 	
 	/**
 	 * @visibility private
 	 * 
 	 * @ Read and set up the images used in the game
 	 */
 	private void setImages()
 	{
 		boardImage = null;
 		boardImageLogo = null;
 		whiteQueenImage = null;
 		blackQueenImage = null;
 		wallImage = null;
 		
 		try
 		{
 			 boardImage = ImageIO.read( new File("src/snozama/ui/board.png" ));
 		}
 		catch(IOException ioe)
 		{
 			System.out.println("Error reading board.png");
 		}
 		try
 		{
 			 boardImageLogo = ImageIO.read( new File("src/snozama/ui/board_logo.png" ));
 		}
 		catch(IOException ioe)
 		{
 			System.out.println("Error reading board_logo.png");
 		}
 		try
 		{
 			 whiteQueenImage = ImageIO.read( new File("src/snozama/ui/white_queen.png" ));
 		}
 		catch(IOException ioe)
 		{
 			System.out.println("Error reading white_queen.png");
 		}
 		try
 		{
 			 blackQueenImage = ImageIO.read( new File("src/snozama/ui/black_queen.png" ));
 		}
 		catch(IOException ioe)
 		{
 			System.out.println("Error reading black_queen.png");
 		}
 		try
 		{
 			 wallImage = ImageIO.read( new File("src/snozama/ui/wall.png" ));
 		}
 		catch(IOException ioe)
 		{
 			System.out.println("Error reading wall.png");
 		}
 	}
 	
 	public void addReadyListener( final ReadyListener rl )
 	{
 		readyFunctions.add( rl );
 	}
 	
 	/**
 	 * Window settings for Game Window
 	 */
 	private void initWindowSettings()
 	{
 		setTitle("Amazons");
 	    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 	    setLocationRelativeTo(null);
 	    setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 	
 	/**
 	 * Called when the UI is finished working and should do some things that were added to the
 	 * Ready Listener.
 	 */
 	public void ready()
 	{
 		List<ReadyListener> copy = new ArrayList<ReadyListener>();
 		
 		copy.addAll( readyFunctions );
 		readyFunctions.clear();
 		Iterator<ReadyListener> it = copy.iterator();
 		
 		while(it.hasNext())
 		{
 			try
 			{
 				it.next().ready();
 			}
 			catch( Exception e )
 			{
 				System.out.println( "Could not invoke ready function." );
 			}
 		}
 		copy.clear();
 	}
 	
 	/**
 	 * export the log to text
 	 */
 	private void exportLog()
 	{
 		
 	}
 
 }
