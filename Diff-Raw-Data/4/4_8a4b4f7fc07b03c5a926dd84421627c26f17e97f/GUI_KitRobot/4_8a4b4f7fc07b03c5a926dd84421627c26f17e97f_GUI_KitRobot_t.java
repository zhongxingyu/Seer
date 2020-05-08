 package gui;
 
 import java.awt.Graphics2D;
 import java.util.*;
 import javax.swing.*;
 
 public class GUI_KitRobot implements GUI_Component {
 
 	boolean kitHeld;
 	ArrayList<GUI_Kit> kit;
 	ImageIcon robotBody;
 	ImageIcon robotHands;
 	boolean move;
 	boolean fullyExtended;
 	int x,y,robotX;
 	int moveToY;
 	int moveToX;
 	
 	GUI_KitRobot()//default constructor
 	{
 		kit = new ArrayList<GUI_Kit>();
 		kitHeld = false;
 		robotBody = new ImageIcon("team06/gfx/kitRobot.png");
 		robotHands = new ImageIcon("team06/gfx/robotarm.png");
 		x = 10;
 		robotX = x+1;
 		y = 10;
 		moveToY = 0;
 		moveToX = 0;
 		move = false;
 		fullyExtended = false;
 	}
 	
 	GUI_KitRobot(String body, String hands, int x1, int y1)//constructor
 	{
 		kit = new ArrayList<GUI_Kit>();
 		kitHeld = false;
 		robotBody = new ImageIcon(body);
 		robotHands = new ImageIcon(hands);
 		x = x1;
 		robotX = x+1;
 		y = y1;
 		moveToY = 0;
 		moveToX = 0;
 		move =false;
 		fullyExtended = false;
 	}
 	
 	public void pickupKit(GUI_Kit k)//picks up kit passed through to it
 	{
 		if(!kitHeld)
 		{
 			kitHeld = true;
 			kit.add(k);
 		}
 	}
 	
 	public GUI_Kit placeKit()//places kit down
 	{
 		GUI_Kit new_kit = null;
 		
 		if(kitHeld)
 		{
 			new_kit = kit.get(0);
 			kit.remove(0);
 			kitHeld = false;
 		}
 		
 		if(new_kit != null)
 			return new_kit;
 		
 		else return null;
 		
 	}
 	
 	public void moveToStation(int y1,int x1)//moves to a given station
 	{
 		move = true;
 		moveToY = y1;	
 		moveToX = x1;
 	}
 	
 	public boolean arrivedAtStation()//notifies that robot has arrived at station
 	{
 		return true;
 	}
 	
 	public void paintComponent(JPanel j, Graphics2D g)
 	{
 		robotBody.paintIcon(j, g, x, y);
 		robotHands.paintIcon(j, g, robotX, y);
 		
		for(int i=0;i<kit.size();i++)
			kit.get(i).paintComponent(j,g);
 	}
 
 
 	public void updateGraphics()
 	{
 		if(move){
             if(y < moveToY)
             {
                 y+=1;
             }
             else if(y > moveToY)
             {
                 y-=1;
             }
             else if(x < moveToX)
             {
                 robotX+=1;
             }
             else if(x > moveToX)
             {
                 robotX-=1;
             }
             else if(robotX == x)
             {
             	fullyExtended = true;
             }
             else
             {
                 move = false;
             }
 		}
         if(fullyExtended)
             {
             	if(robotX > (x+1))
             	{
             		robotX-=1;
             	}
             	else
             	{
             		fullyExtended = false;
             	}
             }
 		
 	}
 
 }
