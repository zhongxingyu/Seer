 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import javax.sound.midi.*;
 import javax.swing.JOptionPane;
 import java.net.URL;  
 
 public class Fanorona extends JPanel implements ActionListener, MouseListener {
     StateMachine stateMachine; //contains grid
     //No flags necessary beyond the state machine itself
 
 	private JButton newGameButton;  
 	private JButton instructionsButton; 
 	private JButton nameButton;
 	private JButton aiButton;
 	private JLabel messageBox;  
 	private JLabel timerBox;
 
 	int BUTTON_SIZE_WIDTH = 120;
 	int BUTTON_SIZE_HEIGHT = 30;
    
 	double xGridAndExcess;
     double yGridAndExcess;
 	double xGridSize;
 	double yGridSize;
 	double changeFactor = 1.0;
 	
 	String playerName;
 	int timePerTurn;
 	
     int rowSize;
     int colSize;
     Boolean aiIsOn;
 
     AI ai;
 
 	public static void main(String[] args) throws Exception {//{{{
         //setup game window
 		JFrame window = new JFrame("Fanorona");
 		Fanorona content = new Fanorona();
 		window.setContentPane(content);
 		window.pack();
 		
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setResizable(false);  
 		window.setVisible(true);
 		URL url = new URL("http://www.vgmusic.com/music/console/nintendo/nes/advlolo1_theme.mid");
         Sequence sequence = MidiSystem.getSequence(url);
         Sequencer sequencer = MidiSystem.getSequencer(); 
         sequencer.open();
         sequencer.setSequence(sequence);
         sequencer.start();
 	}//}}}
 
 	public Fanorona() {//{{{
 		setLayout(null);  
 		askGridSize();
 		askTimePerTurn();
 		
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		
 		//resize the grid
 		System.out.println("screenWidth: " + screenSize.getWidth());
 		System.out.println("screenHeight: " + screenSize.getHeight());
 		
 		xGridAndExcess = (rowSize*100)+(BUTTON_SIZE_WIDTH*2+130);
 		yGridAndExcess = (colSize*100)+100;
 		System.out.println("xGridAndExcess: " + xGridAndExcess);
 		System.out.println("yGridAndExcess: " + yGridAndExcess);
 		
 		if(xGridAndExcess > screenSize.getWidth() || yGridAndExcess > screenSize.getHeight()) {
 			xGridSize = screenSize.getWidth() - ((BUTTON_SIZE_WIDTH*2)+30);
 			yGridSize = screenSize.getHeight() - ((BUTTON_SIZE_HEIGHT*2)+30);
 			System.out.println("xGridSize: " + xGridSize);
 			System.out.println("yGridSize: " + yGridSize);
 			
 			if(xGridAndExcess >= yGridAndExcess) {
 				changeFactor = xGridSize / xGridAndExcess;
 				System.out.println("% change: " + (xGridSize / xGridAndExcess));
 			}
 			else {
 				changeFactor = yGridSize / yGridAndExcess;
 				System.out.println("% change: " + (yGridSize / yGridAndExcess));
 			}				
 		}
 		
 		setPreferredSize(new Dimension((BUTTON_SIZE_WIDTH*2+30)+((int)((rowSize*100+100)*changeFactor)),(int)((colSize*100+100)*changeFactor)));
 		stateMachine = new StateMachine(rowSize, colSize, timePerTurn, changeFactor);			
 		
 		add(stateMachine.grid);
 		stateMachine.grid.setBounds(BUTTON_SIZE_WIDTH*2+30,1,(int)((rowSize*100+100)*changeFactor),(int)((colSize*100+100)*changeFactor)); 
 
         ai = new AI();
         aiIsOn = false;
 
         initButtons();
 
         //add listeners
         instructionsButton.addActionListener(this);
         newGameButton.addActionListener(this);
         nameButton.addActionListener(this);
         aiButton.addActionListener(this);
         addMouseListener(this);
 
         String message = stateMachine.run("NewGame", null);
         messageBox.setText(message);
 	} //}}}
 
     public void initButtons() {//{{{
         instructionsButton = new JButton("Instructions");
         newGameButton = new JButton("New Game");
         aiButton = new JButton("Toggle AI");
         nameButton = new JButton("Change Name");
         messageBox = new JLabel("",JLabel.LEFT);
         messageBox.setVerticalAlignment(JLabel.TOP);
         messageBox.setFont(new  Font("Serif", Font.BOLD, 14));
         messageBox.setForeground(Color.BLACK);
 
 		add(newGameButton);
         add(aiButton);
 		add(instructionsButton);	
 		add(nameButton);
 		add(messageBox);
 		
 		newGameButton.setBounds(10, 10, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		instructionsButton.setBounds(BUTTON_SIZE_WIDTH+20, 10, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		nameButton.setBounds(10, BUTTON_SIZE_HEIGHT+20, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		aiButton.setBounds(BUTTON_SIZE_WIDTH+20, BUTTON_SIZE_HEIGHT+20, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		
 		messageBox.setBounds(10, (BUTTON_SIZE_HEIGHT*2)+30, BUTTON_SIZE_WIDTH*2, BUTTON_SIZE_HEIGHT*4);
     }//}}}
 
     public void mouseEntered(MouseEvent evt) {}
     public void mouseExited(MouseEvent evt) {}
     public void mousePressed(MouseEvent evt) {}
     public void mouseReleased(MouseEvent evt) {}
     public void mouseClicked(MouseEvent evt) {//{{{
     	if(clickingIsAllowed()) {
             if(SwingUtilities.isRightMouseButton(evt)) {
                 String message = stateMachine.run("RClick", null);
                 messageBox.setText(message);
             } else { //left click
                 String message = stateMachine.run("Click", evt.getPoint());
                 messageBox.setText(message);
             }
         }
         //start AI on transition to enemy turn
         runAI();
     }//}}}
     
     private Boolean clickingIsAllowed() {//{{{
         return !aiIsOn || (aiIsOn && stateMachine.isPlayerTurn());
     }//}}}
 
     public void runAI() {//{{{
         //if at state of AI turn
         if(aiIsOn && (stateMachine.getState() == State.ENEMY_SELECT)) {
             //get the move from AI
             ArrayList<Point> points = ai.getMove(stateMachine.grid.getState());
 
             //feed all the points to the state machine
             for(Point p : points) {
                 String message = stateMachine.run("Click", p);
                 messageBox.setText(message);
             }
         }
     }//}}}
 
     public void actionPerformed(ActionEvent evt) {//{{{
         Object src = evt.getSource();
         if(src == newGameButton) {
             String message = stateMachine.run("NewGame", null);
             messageBox.setText(message);
         } else if(src == instructionsButton) {
             instructions();
         } else if(src == nameButton) {
             changeName();
         } else if(src == aiButton) {
             if(stateMachine.isPlayerTurn()) {
                 aiIsOn = !aiIsOn; //toggle
                 String aiState = aiIsOn?"on":"off";
                 JOptionPane.showMessageDialog(this, "AI is now " + aiState + ".", "AI toggle", JOptionPane.PLAIN_MESSAGE);
             } else {
                 JOptionPane.showMessageDialog(this, "Can only toggle AI during the white player's turn.", "AI toggle", JOptionPane.PLAIN_MESSAGE);
             }
         }
         
     }//}}}
 
     //asks the user for a grid size (row, col)
     void askGridSize() {
     	JPanel panel = new JPanel();
     	panel.add(new JLabel("Choose a Fanorona board size (row, column): "));
     	DefaultComboBoxModel rows = new DefaultComboBoxModel();
     	DefaultComboBoxModel cols = new DefaultComboBoxModel();
         for(int n = 1; n <= 13; n+=2) {
         	rows.addElement(n);
         	cols.addElement(n);
         }
         JComboBox colsBox = new JComboBox(cols);
         panel.add(colsBox);
        JComboBox rowsBox = new JComboBox(rows);
        panel.add(rowsBox);
         
         int result = JOptionPane.showConfirmDialog(null, panel, "Choose Fanorona Grid Size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
         switch (result) {
             case JOptionPane.OK_OPTION:
             	rowSize = (Integer)rowsBox.getSelectedItem();
             	colSize = (Integer)colsBox.getSelectedItem();
                 System.out.println("rows: " + rowSize);
                 System.out.println("columns: " + colSize);
                 break;
             default:
             	System.exit(0);
             	break;
         }    	
     }
     
     // prompts user for time per turn
     void askTimePerTurn() {
     	JPanel panel = new JPanel();
     	try {
     		String value = JOptionPane.showInputDialog(null, "Enter a time limit per turn (milliseconds): (If no time limit is wanted, just leave blank.)", "", JOptionPane.PLAIN_MESSAGE);
     		if(value == null)
     			System.exit(0);
     		timePerTurn = Integer.parseInt(value);
     	} catch(NumberFormatException e) {}
     }
     
     void changeName() {//{{{
         playerName = JOptionPane.showInputDialog(null, "Enter player name: ", "", JOptionPane.PLAIN_MESSAGE);
         if(playerName != null)
         	messageBox.setText("Player name is: " + playerName);
     }//}}}
 
     void instructions() {//{{{
         String instructionDialog = "From Wikipedia:\n" +
                 "* Players alternate turns, starting with White.\n" +
                 "* We distinguish two kinds of moves, non-capturing and capturing moves. A non-capturing move is called a paika move.\n" +
                 "* A paika move consists of moving one stone along a line to an adjacent intersection.\n" +
                 "* Capturing moves are obligatory and have to be played in preference to paika moves.\n" +
                 "* Capturing implies removing one or more pieces of the opponent. It can be done in two ways, either (1) by approach or (2) by withdrawal.\n" +
                 "  (1) An approach is moving the capturing stone to a point adjacent to an opponent stone provided that the stone is on the continuation of the capturing stone's movement line.\n" +
                 "  (2) A withdrawal works analogously to an approach except that the movement is away from the opponent stone.\n" +
                 "* When an opponent stone is captured, all opponent pieces in line behind that stone (as long as there is no interruption by an empty point or an own stone) are captured as well.\n" +
                 "* If a player can do an approach and a withdrawal at the same time, he has to choose which one he plays.\n" +
                 "* As in checkers, the capturing piece is allowed to continue making successive captures, with these restrictions:\n" +
                 "  (1) The piece is not allowed to arrive at the same position twice.\n" +
                 "  (2) It is not allowed to move a piece in the same direction as directly before in the capturing sequence. This can happen if an approach follows on a withdrawal.\n" +
                 "* The game ends when one player captures all stones of the opponent. If neither player can achieve this, the game is a draw.\n";
         JOptionPane.showMessageDialog(this, instructionDialog, "Fanorona Instructions", JOptionPane.PLAIN_MESSAGE);
     }//}}}
 }
