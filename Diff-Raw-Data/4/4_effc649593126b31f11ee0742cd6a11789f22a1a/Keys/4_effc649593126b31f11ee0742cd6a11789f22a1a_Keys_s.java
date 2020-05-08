 package com.NSBCoding;
 
 
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 //test
 
 
 public class Keys extends JPanel {
 	
 	
 
 	private static final long serialVersionUID = 1L;
 	
 	public Rectangle character;
     public Rectangle StartingPoint;
     public Rectangle Top;
     public Rectangle Bottom;
     public Rectangle Left;
     public Rectangle Line;
     public Rectangle Line2;
     public Rectangle InvLine;
     public Rectangle InvLine2;
     public Rectangle Line3;
     public Rectangle Line4;
     public Rectangle Line5;
     public Rectangle Line6;
     public Rectangle Line7;
     public Rectangle Line8;
     public Rectangle Line9;
     public Rectangle Line10;
     public Rectangle Line11;
     public Rectangle line3;
     public Rectangle line4;
     public Rectangle line5;
     public Rectangle line6;
     public Rectangle line7;
     public Rectangle line8;
     public Rectangle line9;
     public Rectangle line10;
     public Rectangle line11;
 
 
 	public int charW = 25;
 	public int charH = 25;
     public int LineH = 720;
     public int LineW = 5;
     public int Line2H = 242;
     public int LineInv = 60;
     public int InvRectH = 720;
     public int InvRectW = 100;
 
     public long jumpingTime = 200;
 
 
     public float verticalSpeed = 5.0f;
 
 
 
 
 	public boolean right = false;
 	public boolean left = false;
 	public boolean mouseActive = false;
     public boolean LeftSide = false;
     public boolean RightSide = false;
     public boolean up = false;
     public boolean down = false;
     public boolean jumping = false;
     public boolean Passed = false;
     public boolean Reset = false;
     public boolean StopReset1 = false;
     public boolean Restart = false;
     public boolean KeysIns = false;
     public boolean Dead = false;
     public boolean DeathScreen = false;
     public boolean isMoving = true;
     
 
 
 
     public Point mouse;
 
 	public Keys(Display f, Images i){
         //if(i.imagesLoaded){
 		//Rectangles Being Drawn
 		
 		character = new Rectangle(52, 52, charW, charH);
         StartingPoint = new Rectangle(52, 52, charW, charH);
         Top = new Rectangle(0, 0, 1280, 1);
         Bottom = new Rectangle(0, 720, 1280, 1);
         Left = new Rectangle(0, 0, 1, 720);
         Line = new Rectangle(1180, 302, LineW, LineH);
         Line2 = new Rectangle(1180, 0, LineW,  Line2H);
         InvLine = new Rectangle(1180, 242, LineW, LineInv);
         InvLine2 = new Rectangle(1190, 0, InvRectW, InvRectH);
         Line3 = new Rectangle(109, 0, LineW, 533);
         Line4 = new Rectangle(235, 0, LineW, 303);
         Line5 = new Rectangle(403, 0, LineW, 118);
         Line6 = new Rectangle(511, 0, LineW, 24);
         line3 = new Rectangle(109, 565, LineW, 1000);
         line4 = new Rectangle(235, 335, LineW, 1000);
         line5 = new Rectangle(403, 150, LineW, 1000);
         line6 = new Rectangle(511, 105, LineW, 1000);
         Line7 = new Rectangle(580, 0, LineW, 50);
         line7 = new Rectangle(580, 80, LineW, 1000);
         Line8 = new Rectangle(671, 0, LineW, 300);
         line8 = new Rectangle(671, 400, LineW, 10000);
         Line9 = new Rectangle(770, 0, LineW, 200);
         line9 = new Rectangle(770, 250, LineW, 100000);
         Line10 = new Rectangle(838, 0, LineW, 500);
         line10 = new Rectangle(838, 550, LineW, 10000);
        Line11 = new Rectangle(936, 0, LineW, 650);
        line11 = new Rectangle(936, 700, LineW, 100000);
         
        
     //KeyListener    
         if(isMoving){
         f.addKeyListener(new KeyAdapter(){       	
 			public void keyPressed(KeyEvent e){
 				if(e.getKeyCode() == KeyEvent.VK_D){
 					right = true;
                     RightSide = true;
 
 				}
                 if(e.getKeyCode() == KeyEvent.VK_RIGHT){
                     right = true;
                     RightSide = true;
 
                 }
 				if(e.getKeyCode() == KeyEvent.VK_A){
 					left = true;
                     LeftSide = true;
 
 				}
                 if(e.getKeyCode() == KeyEvent.VK_K){
                     KeysIns = true;
                 }
                 if(e.getKeyCode() == KeyEvent.VK_LEFT){
                     left = true;
                     LeftSide = true;
 
                 }
                 if(e.getKeyCode() == KeyEvent.VK_M) {
                     mouseActive = true;
                     System.out.println(mouse.x);
                     System.out.println(mouse.y);
                 }
                 if(e.getKeyCode() == KeyEvent.VK_S){
                     down = true; }
                 if(e.getKeyCode() == KeyEvent.VK_DOWN){
                     down = true; }
 
                 if(e.getKeyCode() == KeyEvent.VK_W){
                     up = true; }
                 if(e.getKeyCode() == KeyEvent.VK_UP){
                     up = true; }
                 if(e.getKeyCode() == KeyEvent.VK_P){
                     Passed = true; }
                 if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    //jumping = true;
                     //new Thread(new thread().start();
                      }
                      if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                     }
                  if(e.getKeyCode() == KeyEvent.VK_R) {
                      Restart = true;
                  }
                  if(e.getKeyCode() == KeyEvent.VK_C) {
                      mouseActive = true;}
                  
                 } 
        
         
 			public void keyReleased(KeyEvent e){
                 if(e.getKeyCode() == KeyEvent.VK_D){
 					right = false;
                     mouseActive = false;
                     RightSide = false;
 
 				}
                 if(e.getKeyCode() == KeyEvent.VK_RIGHT){
                     right = false;
                     mouseActive = false;
                     RightSide = false;
 
                 }
 
 				if(e.getKeyCode() == KeyEvent.VK_A){
 					left = false;
 		           mouseActive = false;
                     LeftSide = false;   }                
                 if(e.getKeyCode() == KeyEvent.VK_LEFT){
                     left = false;
                     mouseActive = false;
                     LeftSide = false;   }
                 if(e.getKeyCode() == KeyEvent.VK_M) {
                     mouseActive = false;
                 }
 
                 if(e.getKeyCode() == KeyEvent.VK_S){
                     down = false; }
                 if(e.getKeyCode() == KeyEvent.VK_DOWN){
                     down = false; }
 
                 if(e.getKeyCode() == KeyEvent.VK_W){
                     up = false; }
                 if(e.getKeyCode() == KeyEvent.VK_UP){
                     up = false; }
                 if(e.getKeyCode() == KeyEvent.VK_P){
                     Passed = false; }
                 if(e.getKeyCode() == KeyEvent.VK_R) {
                     Restart = false;
                 }
                 if(e.getKeyCode() == KeyEvent.VK_K) {
                     KeysIns = false;
                 }
                 isMoving = false;
                 if(e.getKeyCode() == KeyEvent.VK_C) {
                     mouseActive = false;}
 				}
 
 
 		});
 
         //MouseListener
 
 		f.addMouseMotionListener(new MouseMotionAdapter() {
 
             public void mouseMoved(MouseEvent e){
                 mouse = new Point(e.getX(), e.getY() -25);
                  if(mouseActive){
                 	character.x = mouse.x;
                  }
 
                 repaint();
              }
 
         }); }
         }
 
 
 
     //}
 	
 	public void paintComponent(Graphics g){
         //if(Main.f.i.imagesLoaded) {
         super.paintComponent(g);
         this.setBackground(Color.WHITE);
         if(Reset){
             character.x -= 10;
 
         }
 
  //Invisible lines being colored and filled
         g.setColor(getBackground());
         g.fillRect(InvLine.x, InvLine.y, InvLine.width, InvLine.height);
         g.setColor(getBackground());
         g.fillRect(InvLine2.x, InvLine2.y, InvLine2.width, InvLine2.height);
         g.fillRect(StartingPoint.x, StartingPoint.y, StartingPoint.width, StartingPoint.height);
 
 
   //Rectangle Being colored and filled
 
         if(RightSide)    
         g.setColor(Color.BLUE);
         else             
 		g.setColor(Color.BLUE);
         if(LeftSide)
         g.setColor(Color.BLUE);
         g.fillRect(character.x, character.y, character.width, character.height);
         g.setColor(Color.BLACK);
         g.fillRect(Top.x, Top.y, Top.width, Top.height);
         g.fillRect(Bottom.x, Bottom.y, Bottom.width, Bottom.height);
         g.fillRect(Left.x, Left.y, Left.width, Left.height);
         g.fillRect(Line.x, Line.y, Line.width, Line.height);
         g.fillRect(Line2.x, Line2.y, Line2.width, Line2.height);
         g.setColor(Color.GREEN);
         g.fillRect(Line3.x, Line3.y, Line3.width, Line3.height);
         g.fillRect(Line4.x, Line4.y, Line4.width, Line4.height);
         g.fillRect(Line5.x, Line5.y, Line5.width, Line5.height);
         g.fillRect(Line6.x, Line6.y, Line6.width, Line6.height);
         g.fillRect(line3.x, line3.y, line3.width, line3.height);
         g.fillRect(line4.x, line4.y, line4.width, line4.height);
         g.fillRect(line5.x, line5.y, line5.width, line5.height);
         g.fillRect(line6.x, line6.y, line6.width, line6.height);
         g.fillRect(line7.x, line7.y, line7.width, line7.height);
         g.fillRect(Line7.x, Line7.y, Line7.width, Line7.height);
         g.fillRect(Line8.x, Line8.y, Line8.width, Line8.height);
         g.fillRect(line8.x, line8.y, line8.width, line8.height);
         g.fillRect(Line9.x, Line9.y, Line9.width, Line9.height);
         g.fillRect(line9.x, line9.y, line9.width, line9.height);
         g.fillRect(Line10.x, Line10.y, Line10.width, Line10.height);
         g.fillRect(line10.x, line10.y, line10.width, line10.height);
         g.fillRect(Line11.x, Line11.y, Line11.width, Line11.height);
         g.fillRect(line11.x, line11.y, line11.width, line11.height);
         
 
   //Information If statement       
 
         if(KeysIns){
             g.setColor(Color.BLACK);
             g.setFont(g.getFont().deriveFont(30f));
             g.drawString("Your goal", 123, 140);
             g.drawString("is to get", 126, 200);
             g.drawString("your character", 126, 250);
             g.drawString("to the SafeZone", 126, 300);
             g.drawString("Without touching the black line", 126, 350);
             g.drawString("Careful that you can go to the left", 126, 400);
             g.drawString("Through the walls but not right.", 126, 450);
             g.setColor(Color.BLUE);
             g.drawString("Keys:", 248, 500);
             g.drawString("WASD / arrow", 251,550);
             g.drawString("keys to move", 251, 600);
             g.drawString("R to Reset", 251, 650);
             g.drawString("Escape to Quit", 251, 670);
         
    //IF Statements        
         
         if(DeathScreen){
         	g.setColor(Color.BLACK);
         	g.setFont(g.getFont().deriveFont(30f));
         	g.drawString("Press R to restart", 496, 300);
         	isMoving = false;
             }
         	 
         }
 
        if(Dead) {
             g.setColor(Color.RED);
             g.setFont(g.getFont().deriveFont(30f));
             g.drawString("You Died", 496, 260); 
             g.drawString("Press R to restart", 496, 310); 
             isMoving = false;
             }
 
         if(Restart){
             character.x = 52;
             character.y = 52;
             Dead = false;
             DeathScreen = false;
 
         }
 
         if(StopReset1) {
             Reset = false;
         }
 
     //If Statements for walls    
         
         if(character.intersects(Left))  {
             character.x += 10;
             StopReset1 = true;
 
         }
 
         if(character.intersects(Bottom))  {
             character.y -= 10;
             StopReset1 = true;
 
         }
 
         if(character.intersects(Top))  {
             character.y += 10;
             StopReset1 = true;
 
         }
 
         if(character.intersects(Line))  {
                      
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
            
 
         }
         if(character.intersects(Line2))  {
         	       
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
             
         }
         if(character.intersects(Line3))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(Line4))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(Line5))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(Line6))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line3))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line4))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line5))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line6))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line7))  {
             Reset = true;
             StopReset1 = true;
         }
         if(character.intersects(Line7))  {
             Reset = true;
             StopReset1 = true;
         }
         if(character.intersects(line8))  {
             Reset = true;
             StopReset1 = true;}
             
         if(character.intersects(Line8))  {
                 Reset = true;
                 StopReset1 = true;}
         if(character.intersects(line9))  {
             Reset = true;          
             StopReset1 = true;}    
                                    
         if(character.intersects(Line9))  {
                 Reset = true;
                 StopReset1 = true;}
         
         if(character.intersects(line10))  {
             Reset = true;           
             StopReset1 = true;}     
                                     
         if(character.intersects(Line10))  {
                 Reset = true;
                 StopReset1 = true;}
         
         if(character.intersects(line11))  {
             Reset = true;            
             StopReset1 = true;}      
                                      
         if(character.intersects(Line11))  {
                 Reset = true;
                 StopReset1 = true;}
         
 
        //If Statements for Invisible lines 
         
         if(character.intersects(InvLine2)) {
 
             g.setColor(Color.BLUE);
             g.setFont(g.getFont().deriveFont(30f));
             g.drawString("You", 1215, 200);
             g.drawString("Pass!", 1215, 250);
         }
 
         if(character.intersects(StartingPoint)) {
         g.setColor(Color.BLACK);
         g.setFont(g.getFont().deriveFont(30f));
         g.drawString("Press K", 258, 100);
         g.drawString("for Instructions", 260, 145);
         g.setColor(Color.BLUE);
         g.drawString("WASD / arrow", 251,550);
         g.drawString("keys to move", 251, 600);
         }
   //If Statements for character movement
 
         if(jumping){
             character.y --;
         }
 
 		if(right){
 			character.x += 1;
         }
 		if(left){
 			character.x -= 1;
 		}
             if(up)
                 character.y += -1;
 
             if(down){
                 character.y += 1;
             }
             g.drawImage(Main.f.i.bg, 0, 0, 1280, 720, null);
 
 		repaint();
 	}
 
 	}
 
 	
      //}
 
