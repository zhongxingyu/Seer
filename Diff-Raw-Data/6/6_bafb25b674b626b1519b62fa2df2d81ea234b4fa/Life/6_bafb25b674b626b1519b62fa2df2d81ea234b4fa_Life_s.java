 package GameOfLife;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 public class Life {
 
 //---------------------------CLASS VARIABLES--------------------------------------//
 	
 	private static final int DIM1 = 100;
 	private static final int DIM2 = 100;
 	
 	private static final int FRAME_WIDTH = 700;
 	private static final int FRAME_HEIGHT = 530;
 	
 	private static final int FRAME_START_X = 300;
 	private static final int FRAME_START_Y = 100;
 	
 	private static boolean[][] board;
 	
 	private static JFrame mainFrame = new JFrame("Game of Life");
 	
 	private static final BorderLayout mainLayout = null;
 	
 	private static JButton startButton = new JButton();
 	private static int START_BUTTON_WIDTH = 100;
 	private static int START_BUTTON_HEIGHT = 100;
 	private static int START_BUTTON_X = 500;
 	private static int START_BUTTON_Y = 0;
 	private static boolean stopped = true;
 	
 	private static JButton resetButton = new JButton();
 	private static int RESET_BUTTON_WIDTH = 100;
 	private static int RESET_BUTTON_HEIGHT = 100;
 	private static int RESET_BUTTON_X = 600;
 	private static int RESET_BUTTON_Y = 0;
 	
 	private static JSlider timeSlider = new JSlider(JSlider.HORIZONTAL, 0,1000,500);
 	private static int TIME_SLIDER_WIDTH = 200;
 	private static int TIME_SLIDER_HEIGHT = 100;
 	private static int TIME_SLIDER_X = 500;
 	private static int TIME_SLIDER_Y = 150;
 	
 	private static OurPanel[][] pane;
 	private static final int PANE_WIDTH = 5;
 	private static final int PANE_HEIGHT = 5;
 
 	private static JPanel mainPanel;
 	private static final int MAIN_PANEL_WIDTH = 500;
 	private static final int MAIN_PANEL_HEIGHT = 500;
 	
 	private static BufferedReader reader;
 	
 	private static boolean isRunning = true;
 	
 	private static int timeBetweenSteps = 500;
 	
 	private static Color aliveColour = Color.WHITE;
 	private static Color deadColour = Color.BLACK;
 	
 	
 	// sound stuff
 	private static MidiPlayer midiPlayer;
 	private static int nAliveCells = 0;
 	private static int nAliveCellsLastRound = 0;
 	
 	private static int instrument = 10;
 	
 	
 //--------------------------------INITIALISATION----------------------------------//	
 	
 	public static void initialise(){
 		
 		// Initialisation of the main board
 		board = createRandomBoard(DIM1,DIM2);
 		
 		// Initialisation of the main frame
 	    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    mainFrame.setSize(FRAME_WIDTH,FRAME_HEIGHT);
 	    mainFrame.setLocation(FRAME_START_X, FRAME_START_Y);
 	    mainFrame.setLayout(mainLayout);
 
 	    
 	    
 	    // Initialisation of the main panel
 	    mainPanel = new JPanel();
 	    mainPanel.setSize(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT);
 	    mainPanel.setLocation(0, 0);
 	    mainPanel.setLayout(null);
 	    mainFrame.add(mainPanel);
 	    
 	    
 	    
 	    // Initilisation of the start button
 	    startButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e)
             {
                 if (stopped) startButton.setText("Stop");
                 else 		 startButton.setText("Start");
                 stopped =!stopped;
             }
 	    });
 	    startButton.setSize(START_BUTTON_WIDTH,START_BUTTON_HEIGHT);
 	    startButton.setLocation(START_BUTTON_X,START_BUTTON_Y);
 	    startButton.setText("Start");
 	    startButton.setVisible(true);
 	    mainFrame.add(startButton);
 	    
 	    
 	    // Initilisation of the reset button
 	    resetButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e)
             {
             	board = createRandomBoard(DIM1,DIM2);
             	display(false);
             }
 	    });
 	    resetButton.setSize(RESET_BUTTON_WIDTH,RESET_BUTTON_HEIGHT);
 	    resetButton.setLocation(RESET_BUTTON_X,RESET_BUTTON_Y);
 	    resetButton.setText("Reset");
 	    resetButton.setVisible(true);
 	    mainFrame.add(resetButton);
 	    
 	    
 	    // Initialisation of the time slider
 	    timeSlider.addChangeListener(new ChangeListener() {
 	    	public void stateChanged(ChangeEvent e){
 	    	    JSlider source = (JSlider)e.getSource();
 	    	    if (!source.getValueIsAdjusting()) {
 	    	        timeBetweenSteps = (int)source.getValue();
 	    	        midiPlayer.duration = timeBetweenSteps;
 	    	    }
 	    	}
 	    });
 	    timeSlider.setSize(TIME_SLIDER_WIDTH,TIME_SLIDER_HEIGHT);
 	    timeSlider.setLocation(TIME_SLIDER_X,TIME_SLIDER_Y);
 	    timeSlider.setName("Time between steps");
 	    timeSlider.setVisible(true);
 	    mainFrame.add(timeSlider);
 	    
 	    
 	    // Initialisation of the panels that depict the cells
 	    pane = new OurPanel[DIM1][DIM2];
 	    for(int x=0; x<DIM1; x++){
 	    	for(int y=0; y<DIM2; y++){
 		    	pane[x][y]=new OurPanel();
 		    	mainPanel.add(pane[x][y]);
 		    	pane[x][y].setLocation(x*PANE_WIDTH, y*PANE_HEIGHT);
 		    	pane[x][y].setSize(PANE_WIDTH, PANE_WIDTH);
 		    	pane[x][y].setAlive(board[x][y]);
 			    pane[x][y].setVisible(true);
 	    	}
 	    }
 		// Setting main frame visibility true after everything else is done
 	    // avoids bugs
 	    mainFrame.setVisible(true);
 	    mainPanel.setVisible(true);
 	    
 	    
 		// Initialisation of the input reader
 		reader = new BufferedReader(new InputStreamReader(System.in));
 		
 		// Initialisation of the midi player
<<<<<<< HEAD
 		midiPlayer = new MidiPlayer(instrument,80,timeBetweenSteps);
=======
		midiPlayer = new MidiPlayer(10,80,timeBetweenSteps);
>>>>>>> bfec6f1ebb09e26ae4fd8c7759d66a50c6d26698
 		midiPlayer.scale = new MidiScale("F","dur");
 	
 	}
 	
 	
 	public static boolean[][] createEmptyBoard(int width, int height){
 		// Mit false gefuellt
 		return new boolean[width][height];
 	}
 	
 	public static boolean[][] createRandomBoard(int width, int height){
 		boolean[][] rBoard = new boolean[width][height];
 		for(int i = 0; i<width;i++){
 			for(int j = 0; j<height;j++){
 				rBoard[i][j] = (Math.random() > 0.5);
 			}
 		}
 		return rBoard;
 	}
 	
 
 //-----------------------------------DISPLAY--------------------------------------//
 	
 	public static void display(boolean console){
 		int width = DIM1;
 		int height = DIM2;
 		
 		
 		if (console){
 			for(int j = 0; j<height;j++){
 				for(int i = 0; i<width;i++){
 					System.out.print(board[i][j]?'x':'o');
 				}
 				System.out.println();
 			}
 			
 			System.out.println();
 			System.out.println();
 			System.out.println();	
 		}
 		else {
 			for(int j = 0; j<height;j++){
 				for(int i = 0; i<width;i++){
 					pane[j][i].setAlive(board[i][j]);
 					if (pane[j][i].isAlive()) pane[j][i].setAliveColour(aliveColour);
 					else pane[j][i].setDeadColour(deadColour);
 				}
 				
 			}
 
 		}
 
 	}
 	
 	
 //-------------------------------UPDATE-------------------------------------------//
 	
 	public static void update(){
 		int width = DIM1;
 		int height = DIM2;
 		
 		boolean[][] newBoard = createEmptyBoard(width, height);
 	
 		
 		int c;
 		nAliveCellsLastRound = nAliveCells;
 		nAliveCells = 0;
 		for(int i = 0; i<width;i++){
 			for(int j = 0; j<height;j++){
 				
 				//resetting neighbours variable to zero and counting neighbours
 				c = 0;
 				if(j> 0 && board[i][j-1]) c++;
 				if(j < (height-1) && board[i][j+1]) c++;
 				if(i > 0 && j > 0 && board[i-1][j-1]) c++;
 				if(i > 0 && j < (height-1) && board[i-1][j+1]) c++;
 				if(i > 0 && board[i-1][j]) c++;
 				if(i < (width-1) && j < (height-1) && board[i+1][j+1]) c++;
 				if(i < (width-1) && j > 0 && board[i+1][j-1]) c++;
 				if(i <  (width-1) && board[i+1][j]) c++;
 				
 				//applying rules
 
 				if (board[i][j]){
 					nAliveCells++;
 					if (c<2 || c>3) newBoard[i][j] = false;
 					else newBoard[i][j] = true;
 				}else{
 					if (c==3) newBoard[i][j] = true;
 					else newBoard[i][j] = false;
 				}			
 			}
 			
 			
 		}
 		board = newBoard;
 		
 		// Midi player stuff
 		int nPitch = nAliveCells; // total number of alive cells
 		int mPitch = Math.abs(nAliveCells - nAliveCellsLastRound); // use difference
 		if (nPitch>DIM1*DIM2) nPitch = DIM1*DIM2;
 		midiPlayer.playIntWithScale(nPitch);
 		//midiPlayer.playMultipleNotesInInterval(new int[] {nPitch,mPitch});
 		// colour changing
 		
 		int r1 = (int)(Math.random()*20);
 		int r2 = (int)(Math.random()*20);
 		int r3 = (int)(Math.random()*20);
 		
 		int newAliveRed = ((aliveColour.getRed()+r1) % 156) + 100;
 		int newAliveGreen = ((aliveColour.getGreen()+r2) % 156) + 100;
 		int newAliveBlue = ((aliveColour.getBlue()+r3) % 156) + 100;
 
 		
 		aliveColour = new Color(newAliveRed, newAliveGreen, newAliveBlue);
 		
 
 		/* too much going on
 		int newDeadRed = (deadColour.getRed()+r3) % 256;
 		int newDeadGreen = (deadColour.getGreen()+r1) % 256;
 		int newDeadBlue = (deadColour.getBlue()+r2) % 256;
 		deadColour = new Color(newDeadRed, newDeadGreen, newDeadBlue);	*/
 	}
 	
 //-----------------------------INPUT HANDLING-------------------------------------//
 	
 	public static void handleInput(){
 		/*try{
 			String line = reader.readLine();
 		} catch(Exception emptyException){};
 		*/  // Not useable in graphic mode
 		try{
 			Thread.sleep(timeBetweenSteps);
 		} catch (Exception emptyException){};
 	}
 	
 
 	
 //---------------------------------MAIN-------------------------------------------//	
 	
 	public static void main(String[] args) {
 		
 		initialise();
 		
 		// Main loop
 		while (isRunning){
 			if (!stopped) {
 				display(false);
 				mainFrame.repaint();
 				update();
 				handleInput();
 			}
 		}
 	}
 
 }
