 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 
 /**
  * Class for the Flood it game JFrame
  * includes the main method
  * 
  * @author Sophia Mao
  * @author Kai Jann
  */
 
 public class FloodItGUI extends JFrame implements ActionListener{
     
     //private variables for all the GUI components
     private JFrame frame;
     private Container textContainer;
     private FloodItInstructGui instructions;
     private JTextArea messageArea;
     private JButton buttonRed;
     private JButton buttonBlue;
     private JButton buttonGreen;
     private JButton buttonYellow;
     private JButton buttonInstruction;
     private JPanel buttonPanel;
     private JTextField countdown;
     private JLabel movesLeft;
     
     //static variables
     static Integer MOVES_LEFT = new Integer(25);
 	
 
     //initialize JFrame
     public void init(){
 	//set JFrame properties
	frame = new JFrame("Flood It!");
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	frame.setSize(1000,800);
 	
 
 	buttonInstruction = new JButton("Instructions");
 	
 	//set JTextArea properties for the big message returning box
  	messageArea = new JTextArea(50,20);
 	messageArea.setEditable(false);
 
 	//set JTextField properties for countdown box
 	countdown = new JTextField(MOVES_LEFT.toString(),2);
 	countdown.setEditable(false);
 
 	//JLabel for the countdown JTextArea
 	movesLeft = new JLabel("moves left:");
 	
 	//Panel that holds the color buttons and countdown
 	buttonPanel = new JPanel();
 	
 	//add Countdown components to buttonPanel
 	buttonPanel.add(movesLeft);
 	buttonPanel.add(countdown);
 	
 	//buttonRed properties
 	buttonRed = new JButton("Red");
 	buttonRed.setBackground(Color.RED);
 	buttonPanel.add(buttonRed);
 	buttonRed.addActionListener(new ActionListener(){
 		public void actionPerformed( ActionEvent e ){
 		    messageArea.append("red \n");
 		    countdown.setText(decrementAMove().toString());
 
 		}
 	    });
 	
 	//buttonBlue properties
 	buttonBlue = new JButton("Blue");
 	buttonBlue.setBackground(Color.BLUE);
 	buttonBlue.setForeground(Color.WHITE);
 	buttonPanel.add(buttonBlue);
 	buttonBlue.addActionListener(new ActionListener(){
 		public void actionPerformed( ActionEvent e ){
 		    messageArea.append("blue \n");
 		    countdown.setText(decrementAMove().toString());
 
 		}
 	    });
 	
 	//buttonGreen properties
 	buttonGreen = new JButton("Green");
         buttonGreen.setBackground(Color.GREEN);
 	buttonPanel.add(buttonGreen);
 	buttonGreen.addActionListener(new ActionListener(){
 		public void actionPerformed( ActionEvent e ){
 		    messageArea.append("green \n");
 		    countdown.setText(decrementAMove().toString());
 
 		}
 	    });
 	
 	//buttonYellow properties
 	JButton buttonYellow = new JButton("Yellow");
         buttonYellow.setBackground(Color.YELLOW);
        	buttonPanel.add(buttonYellow);
 	buttonYellow.addActionListener(new ActionListener(){
 		public void actionPerformed( ActionEvent e ){
 		    messageArea.append("yellow \n");
 		    countdown.setText(decrementAMove().toString());
 
 		}
 	    });
 	
 	
 	
 	
 	//add buttonPanel to South component in BorderLayout of JFrame
 	frame.getContentPane().add(BorderLayout.SOUTH,buttonPanel);
 	
 	//Container for text and instructions button
         textContainer = new Container();
 	textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
 	
 	//add Components to textContainer
 	textContainer.add(messageArea);
 	textContainer.add(buttonInstruction);
 	buttonInstruction.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    instructions = new FloodItInstructGui();
 		    messageArea.append("you have clicked the instructions\n");
 		}
 	    });
 	
 	
 	//add textContainer to JFrame
 	frame.getContentPane().add(BorderLayout.EAST,textContainer);
 	
 	frame.setVisible(true);
     }
     
 
 
     //@@@ TODO: add javadoc documentation
     public void run(){
 	init();
     }
 
 
 
 
     //@@@ TODO: add javadoc documentation
     public Integer decrementAMove(){
 	if(MOVES_LEFT <= 0){
 	    messageArea.append("Out of moves!\n");
 	    return 0;
 	}
 	else{
 	    Integer numMoves = new Integer(--MOVES_LEFT);
 	    return numMoves;
 	}
     }
     
 
 
     //main method for Game Flood It
     public static void main(String args[]){
 
 	FloodItGUI game = new FloodItGUI();
 	game.run();
 	
 	
     }
     
     @Override
     public void actionPerformed(ActionEvent e) {
 	// TODO Auto-generated method stub
 	
     }
 }
