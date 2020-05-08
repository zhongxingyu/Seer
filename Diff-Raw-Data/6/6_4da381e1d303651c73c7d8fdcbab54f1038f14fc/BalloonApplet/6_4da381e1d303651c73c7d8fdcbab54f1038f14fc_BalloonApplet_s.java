 package sectionOne;
 
 import java.awt.*;
 import java.applet.*;
 import java.awt.event.*;//import java.awt.event.*;  goes with ActionListener and actionPerformed
 
 public class BalloonApplet extends Applet
                              implements ActionListener//import java.awt.event.*;  goes with ActionListener and actionPerformed
 {//variable & object declarations and initializations                                        
     
 	private static final long serialVersionUID = 1L; //serialID, don't touch
 	
 	
 	Button east, west, north, south, northWest, northEast, southWest, southEast, farUL, farUR, farLL, farLR, midpoint;
     public int displayWidth = 600;
     public int displayHeight = 600;
     public int boundX = displayWidth-50;
     public int boundY = displayHeight-76;
     
     // Set to true to print coordinates of balloon to console /////
     public static final boolean CONSOLE_LOGGING_ENABLED = false; //
     ///////////////////////////////////////////////////////////////
     
     private int startX = boundX/2;
     private int startY = boundY/2;    
     public void init()
     {
     	
 		west = new Button ("West");
         add (west);
         west.addActionListener (this); 
         
         north = new Button ("North");
         add (north);
         north.addActionListener (this); 
         
         south = new Button ("South");
         add (south);
         south.addActionListener (this); 
         
         east = new Button ("East");
         add (east);
         east.addActionListener (this); 
         
         northWest = new Button ("Northwest");
         add (northWest);
         northWest.addActionListener (this); 
         
         northEast = new Button ("Northeast");
         add (northEast);
         northEast.addActionListener (this); 
         
         southWest = new Button ("Southwest");
         add (southWest);
         southWest.addActionListener (this); 
         
         southEast = new Button ("Southeast");
         add (southEast);
         southEast.addActionListener (this); 
         
         farUL = new Button ("Go to upper-left corner");
         add (farUL);
         farUL.addActionListener (this); 
         
         farUR = new Button ("Go to upper-right corner");
         add (farUR);
         farUR.addActionListener (this); 
         
         farLL = new Button ("Go to lower-left corner");
         add (farLL);
         farLL.addActionListener (this); 
         
         farLR = new Button ("Go to lower-right corner");
         add (farLR);
         farLR.addActionListener (this); 
         
         midpoint = new Button ("Go to the exact center");
         add (midpoint);
         midpoint.addActionListener (this); 
         
         if (CONSOLE_LOGGING_ENABLED)
         {
         	System.out.println("Console tracking enabled.");
         	System.out.println("");
         	System.out.println("   x    |   y    ");
         	System.out.println("--------+--------");
         }
         
     }// endInit 
     
     public void paint(Graphics g)
     {
         resize(displayWidth, displayHeight);
         setBackground(Color.CYAN);
         g.setColor(Color.BLUE);
        g.fillOval(startX,startY,50,75);//no longer a weird balloon  
     }//endPaint
     
     public void actionPerformed(ActionEvent clic)//import java.awt.event.*;  goes with ActionListener and actionPerformed
     {
         if (clic.getSource()== east)
             goEast();
         else if (clic.getSource()== west)
         	goWest();
         else if (clic.getSource()== north)
         	goNorth();
         else if (clic.getSource()== south)
         	goSouth();
         else if (clic.getSource()== northWest)
         	goNorthWest();
         else if (clic.getSource()== northEast)
         	goNorthEast();
         else if (clic.getSource()== southWest)
         	goSouthWest();
         else if (clic.getSource()== southEast)
         	goSouthEast();
         else if (clic.getSource()== farUL)
         	goToULCorner();
         else if (clic.getSource()== farUR)
         	goToURCorner();
         else if (clic.getSource()== farLL)
         	goToLLCorner();
         else if (clic.getSource()== farLR)
         	goToLRCorner();
         else if (clic.getSource()== midpoint)
         	goToCenter();
         	
         if (CONSOLE_LOGGING_ENABLED)
         System.out.println(" " + startX + "\t| " + startY);
         
         repaint();
     }//endActionPerformed
     
     public void goEast()
     {
     	if (startX+10 <= boundX)
     		startX+=10;
     	else
     		startX += Math.abs(boundX-startX);
     }
     public void goWest()
     {
     	if (startX-10 >= 0)
     		startX-=10;
     	else
     		startX -= Math.abs(0+startX);
     }
     
     public void goNorth()
     {
     	if (startY-10 >= 0)
     		startY-=10;
     	else
     		startY -= Math.abs(0+startY);
     }
     
     public void goSouth() 
     {
     	if (startY+10 <= boundY)
     		startY+=10;
     	else
     		startY += Math.abs(boundY-startY);
     }
     public void goNorthEast() 
     {
     	goNorth();
     	goEast();
     }
     public void goNorthWest() 
     {
     	goNorth();
     	goWest();
     }
     public void goSouthEast() 
     {
     	goSouth();
     	goEast();
     }
     public void goSouthWest() 
     {
     	goSouth();
     	goWest();
     }
     public void goToCenter()
     {
     	startX=boundX/2;
         startY=boundY/2;
     }
     public void goToULCorner()
     {
     	startX=0;
         startY=0;
     }
     public void goToURCorner()
     {
     	startX=boundX;
         startY=0;
     }
     public void goToLLCorner()
     {
     	startX=0;
         startY=boundY;
     }
     public void goToLRCorner()
     {
     	startX=boundX;
         startY=boundY;
     }
     
 }   //endProgram    
 
 
 
 
 
 
 
 
 
