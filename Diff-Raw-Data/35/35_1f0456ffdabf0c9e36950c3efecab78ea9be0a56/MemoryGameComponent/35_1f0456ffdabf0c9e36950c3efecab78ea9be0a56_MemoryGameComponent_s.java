 package edu.ucsb.cs56.S12.issues.issue0000805;
 
 import java.awt.*;
 import java.awt.event.*; // for ActionListener and ActionEvent
 import javax.swing.*;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.lang.Math;
 
 /**
  * A Swing component for playing the Memory Card Game
    @author Bryce McGaw and Jonathan Yau (with some of Phill Conrad's code as a basis)
    @version CS56 Spring 2012
    @see MemoryGrid 
  */
 public class MemoryGameComponent extends JComponent
 {
     
     private JButton []        buttons;
     private ArrayList<Icon>   imgIcons = new ArrayList<Icon>();
     public  JComponent        restartB = new JButton("Restart");
     private Icon              imgBlank;
     
     private MemoryGrid        grid;
     private int               currentLevel;
     private MemoryGameLevel[] levels;
     private MemoryGameLevel   level = new MemoryGameLevel(36, 100, 2000);
     private long              startTime = 0;
     private boolean           cheatEnabled=false;
     private int               gameCounter=0;

     /**
        Loads a basic set of levels for the game
      */
     private void loadLevelSet1() {
 	levels = new MemoryGameLevel[3];
 	levels[0] = new MemoryGameLevel(16, 750, 2000);
 	levels[1] = new MemoryGameLevel(36, 200, 1000);
 	levels[2] = new MemoryGameLevel(36, 2000, 1000);
 	currentLevel = 0;
 	level = levels[currentLevel];
     }
 
     // The first 8 images. These will be used
     // for a 4 by 4 game.
     private String[] images8 = {
 	"/images/200.jpg", "/images/201.jpg",
 	"/images/202.jpg", "/images/203.jpg",
 	"/images/204.jpg", "/images/205.jpg",
 	"/images/206.jpg", "/images/207.jpg",
     };
     // The next ten images. These, in addition
     // to the first 8 will be used for a
     // 6 by 6 game.
     private String[] images10 = {
 	"/images/208.jpg", "/images/209.jpg",
 	"/images/210.jpg", "/images/211.jpg",
 	"/images/212.jpg", "/images/213.jpg",
 	"/images/214.jpg", "/images/215.jpg",
        	"/images/216.jpg", "/images/217.jpg",
         
     };
 
     /** Constructor
 	
 	@param game an object that implements the MemoryGrid interface
 	to keep track of the moves in each game, ensuring the rules are
 	followed and detecting when the user has won.
     */
     public MemoryGameComponent(MemoryGrid game) {
 	super(); 
 	this.grid = game;
         int gridSize = grid.getSize();
 	buttons= new JButton[gridSize];
 	
 	loadLevelSet1();
 	loadImageIcons(); // loads the array list of icons and sets imgBlank
 	buildTiles();
 	
 	startTime = new Date().getTime();
     }
 
     public void buildTiles() {
 	this.removeAll();
 	int gridSize = grid.getSize();
 
 	//set layout to a grid of length sqrt(grid size)
 	this.setLayout(new GridLayout(0,(int)Math.sqrt(gridSize))); 
 	buttons = new JButton[gridSize];
 	for(int i=0; i<=(gridSize-1); i++) {
 	    //initially all buttons are blank
 	    JButton jb = new JButton(imgBlank);   
 	    
 	    buttons[i] = jb;
 	    jb.addActionListener(new ButtonListener(i));
 
 	    //get rid of annoying boxes appearing around icon next to clicked icon
 	    jb.setFocusPainted(false);          
 	    
 	    this.add(jb);  
 	    }
 	this.repaint();
 	this.validate();
 	startTime = new Date().getTime();
     }
 
     class ButtonListener implements ActionListener {
 	private int num;
 
 	public ButtonListener(int i) {
 	    super();
 	    this.num = i;
 	}
 
         @Override
 	public void actionPerformed (ActionEvent event) {
 	    
 	    Class classs = this.getClass();
 	    Icon imgBlank = new ImageIcon(
 				classs.getResource("/images/000.jpg"));
             
 	    //if 2 MemoryCards are flipped, flip back over
 	    flipBack();
             //if no MemoryCards are flipped, flip one
             if (!grid.isOneFlipped()){
 		grid.flip(num);
 		JButton jb = buttons[num];
 		Icon i = imgIcons.get(grid.getVal(num)-1);
 		jb.setIcon(i);            //set image according to val
 		if(num!=1)                //cheat code
 		    jb.setEnabled(false); //make unclickable
                 else
 		    cheatEnabled=true;	
 		    
 	    }
 
             //if one MemoryCard is flipped, flip other
             //then check if theyre matching
             else{
 		if((num==1&&cheatEnabled))//cheat code
 		    {
 		        endGame();
 			newGame(true); // true to start the next level
 		    }
                 grid.flip(num);
                 JButton jb = buttons[num];
 		
 		jb.setIcon(imgIcons.get(grid.getVal(num)-1));      //set image according to val
       
                 jb.setEnabled(false);
                 if (grid.flippedEquals(num)){    //if they're matching keep num displayed and set flipped as false
                     gameCounter++;
                     grid.flip(num); 
                     grid.flip(grid.getFlipped());
                     
                     //check if game is over
                     if(gameCounter==grid.getSize()/2){
 		        endGame();
 			newGame(true); // true to start the next level
                     } 
                 } else {
 		    // start the flip back timer
 		    int delay = level.getFlipTime();
 		    ActionListener listener = new ActionListener() {
 			    public void actionPerformed(ActionEvent e) { flipBack(); }
 			};
 		    Timer t = new Timer(delay, listener);
 		    t.setRepeats(false);
 		    t.start();
 		} // end of inner if else
 		
             } // end of outer if else
         }
     }
     /** Checks if Game is Over*/
     public boolean isOver(){
	return (gameCounter==grid.getSize()/2);
     }
     /**
        Starts a new level or restarts the current level
      */
     public void newGame(boolean startNextLevel) {
 	gameCounter = 0;
 	if (startNextLevel && currentLevel < levels.length) {
 	    currentLevel++;
 	    level = levels[currentLevel];
 	}
 	int gridSize = level.getGridSize();
 	grid = new MemoryGrid(gridSize);
 	buildTiles();
     }
 
     public void endGame() {
 	long finalTime = new Date().getTime();
 	long deltaTime = finalTime - startTime;
 	
 	// Should be replaced with code to decide whether the user should pass
 	// the current level.
         System.out.println("You solved under the target time by "
 	+ ((int)(deltaTime/1000.0) - level.getSecondsToSolve()) + "seconds");
 
         grid.isOver=true;
                         
         JOptionPane popup = new JOptionPane("Good Job!");
         JOptionPane.showMessageDialog(
 		    popup,
 		    "-~*´¨¯¨`*·~-.¸-  You won!!  -,.-~*´¨¯¨`*·~-",
 		    "Good Job!",
 		    1);
     }
 
     /**
        If two cards are showing, flips them back over
     */
     public void flipBack() {
 	
 	if(grid.isTwoFlipped()){
 	    JButton jb = buttons[grid.getFlipped()];
 	    jb.setEnabled(true);
 	    jb.setIcon(imgBlank);
 	    grid.flip(grid.getFlipped());
 	    jb = buttons[grid.getFlipped()];
 	    jb.setEnabled(true);
 	    jb.setIcon(imgBlank);
 	    grid.flip(grid.getFlipped());
 
 	}
     }
 
     public void loadImageIcons() {
 	//get the current classloader (needed for getResource method..  )
 	//                            (which is required for jws to work)
 	//ClassLoader classLoader = this.getClass().getClassLoader();
 	Class classs = this.getClass();
 	//load Icons 
 	for (String image : images8) {
 	    imgIcons.add(new ImageIcon(classs.getResource(image)));
 	}
 	for (String image : images10) {
 	    imgIcons.add(new ImageIcon(classs.getResource(image)));
 	}
 	imgBlank = new ImageIcon(classs.getResource("/images/000.jpg"));
     }
 }
