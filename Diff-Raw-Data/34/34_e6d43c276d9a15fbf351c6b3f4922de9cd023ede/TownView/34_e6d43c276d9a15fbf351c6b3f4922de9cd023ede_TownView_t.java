 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.ImageIcon;
 
 
 public class TownView {
 	
 	private float progress = 0.0f;
 	private JLabel playerImage;
 	private JLabel townImage;
 	private int playerWidth;
 	private int playerHeight;
 	private int playerX=200;
 	private int tempPlayerX=200;
 	private int playerY=200;
 	private int tempPlayerY=200;
 	private int townWidth;
 	private int townHeight;
 	private JPanel panel; 
 	private JFrame frame;
 	int pubX=28;
 	int pubY=32;
 	int storeX=28;
 	int storeY=242;
 	int landOfficeX=348;
 	int landOfficeY=242;
 	int otherX=348;
 	int otherY=32;
 	int width=166;
 	int heightT=108;
 	int heightB=140;
	static final int SPEED = 4;
 	
 	/**
 	 * 
 	 */
 	public TownView(Player player){
 		//Sets the widths and heights for the player and the town. If the player is set to the correct width odd things happen. 
 		//If the player is set to 28x42 it works normally except near the edges.
 		ImageIcon playerIcon = player.getImage();
 		playerImage = new JLabel(playerIcon);
 		playerWidth=28;//playerIcon.getIconWidth();
 		playerHeight=42;//playerIcon.getIconHeight();
 		
 		ImageIcon townIcon = new ImageIcon("townview.jpg");
 		townImage = new JLabel(townIcon);
 		townWidth=townIcon.getIconWidth();
 		townHeight=townIcon.getIconHeight();
 		
 	}
 	
 	/**
 	 * displays initial town with player in the center of the board
 	 * @param frame 
 	 */
 	public void displayTownSquare(JFrame frame){
 		this.frame=frame;
 		
 		//creates layeredPane
 		JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setOpaque(false);
 		layeredPane.setPreferredSize(new Dimension(townWidth, townHeight));
 		
 		//sets bounds on images so they can be drawn
 		townImage.setBounds(0,0,townWidth, townHeight);
 		playerImage.setBounds(playerX,playerY,playerX+playerWidth,playerY+playerHeight);
 		
 		//adds images to the layeredPane
 		layeredPane.add(townImage,JLayeredPane.DEFAULT_LAYER);
 		layeredPane.add(playerImage,JLayeredPane.POPUP_LAYER);
 		
 		//creates a new JPanel, and adds the layered pane to it
 		panel = new JPanel();
 		panel.add(layeredPane);
 		
 		//adds the pane to the frame, the JFrame has a grid layout but changing the layout does not seem to help the size problem
 		frame.setSize(new Dimension(townWidth, townHeight));
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		panel.setLayout(new GridLayout(1,1));
 		frame.add(panel);
         frame.pack();
         frame.setVisible(true);
        animate();
 	}
 	
 	/**
 	 * updates player's location
 	 */
 	public void updatePlayer(){
		//calculate new location
 		boolean touchingInner =checkInnerBoundaries();
 		boolean touchingOuter =checkOuterBoundaries();
 
 		if(!touchingInner&&!touchingOuter){
 			playerX=tempPlayerX;
 			playerY=tempPlayerY;
 		}
 		else{
 			tempPlayerX=playerX;
 			tempPlayerY=playerY;
 		}
 		
 		playerImage.setBounds(playerX,playerY,playerX+playerWidth,playerY+playerHeight);
 	}
 	
 	/* checks if player is within outer bounds */
 	private boolean checkOuterBoundaries(){
 		//if the code is running correctly then the 404 should be replaced by townHeight and the 534 should be replaced by townWidth
 		//the 10s should be replaced with 0
 		boolean touching=false;
 		
 		if(tempPlayerX+playerWidth>523){
 			touching=true;
 		}
 		if(tempPlayerX<22){
 			touching=true;
 		}
 		if(tempPlayerY+playerHeight>395){
 			touching=true;
 		}
 		if(tempPlayerY<22){
 			touching=true;
 		}
 		return touching;
 	}
 
 	/* checks if player is inside a shop */
 	private boolean checkInnerBoundaries(){
 		return (checkForSpecificLocation(pubX, pubY, width, heightT)||
 				checkForSpecificLocation(storeX, storeY, width, heightB)||
 				checkForSpecificLocation(landOfficeX, landOfficeY, width, heightB)||
 				checkForSpecificLocation(otherX, otherY, width, heightT));
 	}
 	
 	/**
 	 * checks if the player is inside a specific location and updates players location to not be inside the specific location
 	 * @param locX
 	 * @param locY
 	 * @param locWidth
 	 * @param locHeight
 	 */
 	private boolean checkForSpecificLocation(int locX,int locY,int locWidth, int locHeight){
 		//This is a very painful read through, basically it checks if the corner of the player overlaps with the corner of the location
 		//or if the player is hitting one of the sides of the location
 		//I believe this code to be correct
 		
 		boolean touching=false;
 		/*checking if corners are overlapping */
 		if(tempPlayerX+playerWidth>locX&&tempPlayerX<locX){
 			if(tempPlayerY+playerHeight>locY&&tempPlayerY<locY){//top right corner of loc
 				touching=true;
 			}
 			if(tempPlayerY<locY+locHeight&&tempPlayerY+playerHeight>locY+locHeight){//bottom right corner of loc
 				touching=true;
 			}
 			
 		}
 		
 		if(tempPlayerX<locX+locWidth&&tempPlayerX+playerWidth>locX+locWidth){
 			if(tempPlayerY+playerHeight>locY&&tempPlayerY<locY){//top left corner of loc
 				touching=true;
 			}
 			if(tempPlayerY<locY+locHeight&&tempPlayerY+playerHeight>locY+locHeight){//bottom left corner of loc
 				touching=true;
 			}
 			
 		}
 		/*checking if the player is overlapping the location from the size*/
 		if(tempPlayerY+playerHeight<locY+locHeight&&tempPlayerY>locY){
 			if(tempPlayerX+playerWidth>locX&&tempPlayerX<locX){//right side of loc
 				touching=true;
 			}
 			
 		    if(tempPlayerX+playerWidth>locX+locWidth&&tempPlayerX<locX+locWidth){//left side of loc
 		    	touching=true;
 		    }
 		}
 		if(tempPlayerX+playerWidth<locX+locWidth&&tempPlayerX>locX){
 			if(tempPlayerY+playerHeight>locY&&tempPlayerY<locY){//top side of loc
 				touching=true;
 			}
 			
 		    if(tempPlayerY+playerHeight>locY+locHeight&&tempPlayerY<locY+locHeight){//bottom side of loc
 		    	touching=true;
 		    }
 		}
 		return touching;
 	
 	}	
 	/**
 	 * moves player around the screen
 	 */
 	private void animate() {
 		//sets a KeyListener to listen for directions
 		KeyStroke stork = new KeyStroke();
 		if(frame==null)
 			return;
 		frame.addKeyListener(stork);
 		
 		//animates the player moving across the screen at a certain fps
 		//also has an unused timer feature that could be used later on in the project.
 		final int animationTime = 1000;
         int fps = 30;
         int delay = 1000 / fps;
         final long start = System.currentTimeMillis();
         final Timer timer = new Timer(delay, null);
         timer.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 final long now = System.currentTimeMillis();
                 final long elapsed = now - start;
                 progress = (float) elapsed / animationTime;
                 updatePlayer();
                 if (elapsed >= animationTime) {
                  //   timer.stop();
                 }
             }
         });
         timer.start();
     }
 
 	
 	
 	private class KeyStroke implements KeyListener{
 		
 		public void keyTyped(KeyEvent e){}
 		public void keyPressed(KeyEvent e){
 			//Listens for key storkes.
 			//Updates the player location.
 			//Monitors which direction the player is going in, currently this is being used to decide information about 
 			//the movement of the player around the corners of locations in the checkForSpecificLocation() method.
 			//Given time a better method of moving around corners should be devised.
 			 if(KeyEvent.VK_UP==e.getKeyCode()||KeyEvent.VK_W==e.getKeyCode()){
				 tempPlayerY-=SPEED;
 			 }
 			 else if(KeyEvent.VK_DOWN==e.getKeyCode()||KeyEvent.VK_S==e.getKeyCode()){
				 tempPlayerY+=SPEED;
 			 }
 			 else if(KeyEvent.VK_RIGHT==e.getKeyCode()||KeyEvent.VK_D==e.getKeyCode()){
				 tempPlayerX+=SPEED;
 			 }
 			 else if(KeyEvent.VK_LEFT==e.getKeyCode()||KeyEvent.VK_A==e.getKeyCode()){
				 tempPlayerX-=SPEED;
 			 }
 		 }
 		 public void keyReleased(KeyEvent e){}
 	}
 }
