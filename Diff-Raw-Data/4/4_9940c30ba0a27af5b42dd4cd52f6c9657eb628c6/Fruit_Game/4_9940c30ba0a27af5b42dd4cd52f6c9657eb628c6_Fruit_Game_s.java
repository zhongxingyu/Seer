 package minigames.fruit;
 import java.awt.*;  
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;  
 
 public class Fruit_Game extends JPanel implements KeyListener,Runnable{  
 	BufferedImage basket;
 	BufferedImage banana;
 	BufferedImage bomb;
 	BufferedImage apple;
 	BufferedImage orange;
 	String currentItem;
 	Image background;
 	BufferedImage bf;
 	int level;
 	int bombsHit;
 	int cordX = 200;
 	int cordY = 300;
 	int X=0;
 	int Y=0;
 	int totalScore;
 	JLabel score;
 	Graphics graphics;
 	Timer timer;
 	static int totalFruits;
 	static int counter;
 
 	public Fruit_Game(int day) {
 		this.setPreferredSize(new Dimension(550, 450));
 		this.setFocusable(true);   // Allow this panel to get focus.
 		this.addKeyListener(this);
 		//this.setSize(550,450);
 		
 		this.setBackground(Color.white);
 		currentItem="apple";
 		bombsHit=0;
 		X=0;
 		Y=0;
 		
 		if(day>0 && day<4){
 			level = 1;// 1-3 depending on the level of the player
 			totalFruits=15;
 		}
 		else if(day>3 && day<8){
 			level = 2;
 			totalFruits=20;
 		}
 		else if(day>7 && day< 11){
 			level = 3;
 			totalFruits=30;
 		}
 		
 		totalScore=0;// used to keep the score
 		loadImages();// load the images for the game
 		score = new JLabel("0");
 		this.add(score,BorderLayout.SOUTH);
 		this.setVisible(true);
 		startAnimation();
 	}
 	
 	public void startAnimation(){
 		// if timer hasnt started
 		Random r = new Random();
 		X=r.nextInt(400);
 		if(timer==null)
 		{
 			//System.out.println("Start");
 			Y=0;
 			// create new timer
 			timer = new Timer(50, new TimerListener());
 			timer.start();
 		}
 		//sSystem.out.println("Running");
 	}
 
 	public void loadImages() {
 		try {
 			//path for image files
 			String backPath = "art/fruit/Kitchen background_s_wm.jpg";
 			background = ImageIO.read(new File(backPath));
 
 			String pathBasket = "art/fruit/basket.png";
 			basket = ImageIO.read(new File(pathBasket));
 
 			String pathBanana = "art/fruit/banana.png";
 			banana = ImageIO.read(new File(pathBanana));
 			
 			String pathApple = "art/fruit/apple.png";
 			apple = ImageIO.read(new File(pathApple));
 			
 			String pathOrange = "art/fruit/orange.png";
 			orange = ImageIO.read(new File(pathOrange));
 			
 			String pathBomb = "art/fruit/bomb.png";
 			bomb = ImageIO.read(new File(pathBomb));
 			
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		
 		//associate the keyboard listener with this JFrame
 		addKeyListener(this);
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		g.setColor(Color.black);
 		g.drawImage(apple, X, 700, this);
 		g.drawImage(banana, X, 700, this);
 		g.drawImage(bomb, X, 700, this);
 		g.drawImage(orange, X, 700, this);
 		g.drawImage(background, 0, 0, this);
 		g.drawImage(basket, cordX, cordY, this);
 		
		if(bombsHit<3){
 			if(currentItem.equals("apple")){
 				g.drawImage(apple, X, Y, this);
 			}
 			else if(currentItem.equals("banana")){
 				g.drawImage(banana, X, Y, this);
 			}
 			else if(currentItem.equals("bomb")){
 				g.drawImage(bomb, X, Y, this);
 			}
 			else if(currentItem.equals("orange")){
 				g.drawImage(orange, X, Y, this);
 			}
 		}
 		else{
 			g.drawImage(background, 0, 0, this);
			timer.stop();
 		}
 	}
 		
 	@Override
 	public void keyPressed(KeyEvent ke) {	
 		switch (ke.getKeyCode()) {
 		//if the right arrow in keyboard is pressed...
 		case KeyEvent.VK_RIGHT: {
 			if(cordX<400)
 				cordX+=15;
 		}
 		break;
 		//if the left arrow in keyboard is pressed...
 		case KeyEvent.VK_LEFT: {
 			if(cordX>0)
 				cordX-=15;
 		}
 		break;
 		}
 		repaint();
 	}
 
 	@Override
 	public void keyReleased(KeyEvent ke) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent ke) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void wait(int n){
 	    long t0, t1;
 	    t0 = System.currentTimeMillis();
 
 	    do {
 	        t1 = System.currentTimeMillis();            
 	    } while ((t1 - t0) < n);
 	}   
 	
 	private class TimerListener implements ActionListener
     {	
         public void actionPerformed(ActionEvent e)
         {   		
     		if(level==1)
     			Y+=10;
     		else if(level==2)
     			Y+=15;
     		else if(level==3)
     			Y+=18;
         
         	repaint();
         	//System.out.println("repaint");
         	if(Y>400)
         	{
         		timer.stop();
         		timer=null;
         		checkHit();
         		if(totalFruits>counter){
         			Random r = new Random();
         			int option = r.nextInt(4);
         			
         			if(option==0)
         				currentItem="apple";
         			else if(option==1)
         				currentItem="bomb";
         			else if(option==2)
         				currentItem="banana";
         			else if(option==3)
         				currentItem="orange";
         			
         			startAnimation();
         		}
         	}
         	
         	return;
     	}
     }
 	
 	public void checkHit(){
 		if(cordX-70<X && cordX+70>X)
 		{
 			if(currentItem.equals("bomb")){
     			totalScore-=2;
     			bombsHit++;
 			}
     		else{
     			totalScore++;
     			counter++;
     		}
 			
 		}
 		else if(!(currentItem.equals("bomb"))){
 			bombsHit++;// this will mean that the fruit fell and you were not able to catch it
 			counter++;
 		}
 		
 		score.setText("Score: "+Integer.toString(totalScore)+"/"+totalFruits+"  Life:"+(3-bombsHit));
 	}
 
 	@Override
 	public void run() 
 	{	
 	}
 	
 	/** main program (entry point) */
 	   public static void main(String[] args) {
 	      // Run GUI in the Event Dispatcher Thread (EDT) instead of main thread.
 	      javax.swing.SwingUtilities.invokeLater(new Runnable() {
 	         public void run() {
 	            // Set up main window (using Swing's Jframe)
 	            JFrame frame = new JFrame("Fruit Catching Game");
 	            Fruit_Game fg = new Fruit_Game(1);
 	            frame.setSize(550,450);
 	            frame.setBackground(Color.green);
 	            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	            frame.setContentPane(fg);
 	            frame.pack();
 	            frame.setVisible(true);
 	           // System.out.println(counter);
 	         }
 	      });
 	   }
 	
 }
 
 
 
 
 
 
 
 
 
 
