 package gui;
 
 import agents.Kit;
 import agents.KitRobotAgent;
 import javax.swing.*;
 
 import state.FactoryState;
 
 public class GUI_KitRobot extends GUI_Component {
 
 	boolean kitHeld;
 	GUI_Kit guikit;
 	GUI_KitStand stand;
 	KitRobotAgent kitRobot;
 	GUI_Conveyor conveyor;
 	ImageIcon robotBody;
 	ImageIcon robotHands;
 	boolean move;
 	boolean fullyExtended;
 	boolean goToOne = false,goToTwo = false, yMoveDone = false, floatMode = false, goHome = false, goToFullStand = false, moveToOne = false,moveToTwo = false,moveToInspection = false,goToConveyor = false, finalMove = false, initialConveyor = false, placeOnOne = false, placeOnTwo = false, placeBrokenKit = false, placeBroken = false;
 	int x,y,robotX,moveToY,moveToX,x2,y2,x1,y1, tempX, tempY, floatCount = 0,standToGo = 0, originalY = 0, floatHappened = 0;
 	
 	public GUI_KitRobot(GUI_Conveyor con, GUI_KitStand kstand )//default constructor
 	{
 		guikit = null;
 		conveyor = con;
 		stand = kstand;
 		kitHeld = false;
 		robotBody = new ImageIcon("gfx/kitRobot.png");
 		robotHands = new ImageIcon("gfx/robotarm.png");
 		x = 30;
 		robotX = x+1;
 		y = 200;
 		moveToY = 0;
 		moveToX = 0;
 		move = false;
 		fullyExtended = false;
 		
 		myDrawing = new Drawing(0, 0, "NOPIC");
 		myDrawing.subDrawings.add(new Drawing(x,y,"kitRobot.png"));
 		myDrawing.subDrawings.add(new Drawing(robotX,y,"robotarm.png"));
 		
 	}
 	
 	public GUI_KitRobot(String body, String hands, int x1, int y1)//constructor
 	{
 		guikit = null;
 		kitHeld = false;
 		robotBody = new ImageIcon(body);
 		robotHands = new ImageIcon(hands);
 		x = x1;
 		robotX = x+20;
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
 			guikit = k;
 			guikit.setX(robotX);
 			guikit.setY(y);
 		}
 	}
 	
 	public GUI_Kit placeKit()//places kit down
 	{
 		GUI_Kit new_kit = null;
 		
 		if(kitHeld)
 		{
 			if(guikit != null)
 			{
 				new_kit = guikit;
 				guikit = null;
 				//guikit.posX+= 30;
 				//guikit = null;
 				kitHeld = false;
 			}
 
 		}
 		
 		return new_kit;
 	
 	}
 	
 	
 	public int getX()
 	{
 		return x;
 	}
 	
 	public int getY()
 	{
 		return y;
 	}
 	
 	public int getRobotX()
 	{
 		return robotX;
 	}
 
 	public void moveToStation(int x1,int y1)//moves to a given station
 	{
 
 		move = true;
 		if(x1 > 0)
 			x1 = x+40;
 
 		moveToY = y1;	
 		moveToX = x1;
 		
 	}
 	
 	public boolean arrivedAtStation()//notifies that robot has arrived at station
 	{
 		return true;
 	}
 	
 	 public void DoPlaceOnKitStand(Kit kit, KitRobotAgent robotAgent) 
 	 { 
 		if(floatHappened != 0)
 		{
 			y = originalY;
 		}
 			
 		floatHappened = 0;
 		 
 		 move = true;
 		 FactoryState.out.println("Moving and Placing Kit on Kit Stand");
 		 
 		 Kit k = kit;
 		 kitRobot = robotAgent;
 		 
 		 if (conveyor.checkKit() == null){
 			 System.out.println("ERROR");
 //			 System.out.println("kit that needs to be picked up ID: " + k.kitID);
 //			 System.out.println("shown kit on conveyor ID: " + conveyor.checkKit().kit.kitID);
 			 
 			 int x1 = 70;
 			 int y1 = 30;
 			 
 			 moveToStation(x1,y1);
 			 
 			 while(conveyor.checkKit() == null)
 			 {
 				 DoPlaceOnKitStand(k,robotAgent);
 			 }
 			 
 			 
 		 }
 
 		 if(conveyor.checkKit().kit == k)
 			 {
 			 	initialConveyor = true;
 			 	 
 			 	 int x1 = conveyor.getX();
 				 int y1 = conveyor.getY();
 				 
 				 //System.out.println("conveyor:" + conveyor.getY());
 				// System.out.println("new y1:" + y1);
 				
 				moveToStation(x1,y1);
 				//pickupKit(conveyor.robotRemoveKit());
 				if(stand.positionOpen(2))
 				{
 					goToTwo = true;
 
 					//System.out.println("hnnnnnnnnnngggggggggggggggggg");
 					
 					
 					x2 = stand.getX(2);
 					y2 = stand.getY(2);
 					
 					//moveToStation(x2,y2);
 					if(y == y2 || kitRobot.testing)
 					{
 						//stand.addkit(placeKit(), 2);
 						kitHeld = false;
 						guikit = null;
 						//kitRobot.AnimationLockRelease();
 					}
 				}
 				else if (stand.positionOpen(1))
 				{
 					goToOne = true;
 					
 					x2 = stand.getX(1);
 					y2 = stand.getY(1);
 					
 					//moveToStation(x2,y2);
 					if(y == y2 || kitRobot.testing)
 					{
 						//stand.addkit(placeKit(), 1);
 						kitHeld = false;
 						guikit = null;
 						//kitRobot.AnimationLockRelease();
 					}
 				
 			 }
 				
 		 }
 		 
 		 // Grab empty kit from conveyer
 		 // Place on kit stand
 		 //check of 2 is empty
 		 //if empty place there
 		 //else check if 1 is empty
 		 //if so place kit
 		 //if not do nothing
 		 
 	 }
 
 	   public void DoMoveFromKitStandToInspection(Kit kit, KitRobotAgent agent) 
 	    {
 		   if(floatHappened != 0)
 			{
 				y = originalY;
 			}
 		   floatHappened = 0;
 		   move = true;
 		   /*
 		   System.out.println("moving from stand to inspection");
 		   System.out.println("moving from stand to inspection");
 		   System.out.println("moving from stand to inspection");
 		   
 		   System.out.println(stand.positionOpen(0));
 		   System.out.println(stand.getX(2));
 		   System.out.println(stand.getY(2));
 		   */
 		   
 			 Kit k = kit;
 			 kitRobot = agent;
 			 		 
 			 if(stand.positionOpen(0))
 			 {
				 if (stand.checkKit(2) != null)
 				 {
 					// if(stand.checkKit(2).kit.complete)
 					// {
 						 
 						// System.out.println("going to Two!!!!");
 						 
 						 x1 = stand.getX(2);
 						 y1 = stand.getY(2);
 						 
 						 tempX = stand.getX(2);
 						 tempY = stand.getY(2);
 						 
 						 moveToTwo = true;
 						 						 
 						 moveToStation(x1,y1);
 						 			 
 						 x2 = stand.getX(0);
 						 y2 = stand.getY(0);
 						 
 					// }
 				 }
 				 
				 else if(stand.checkKit(1) != null)
 				 {
 					 
 					// if(stand.checkKit(1).kit.complete)
 					// {
 					 //System.out.println("going to One!!!!");
 						 
 						 moveToOne = true;
 						 
 						 x1 = stand.getX(1);
 						 y1 = stand.getY(1);
 						 
 						 tempX = stand.getX(1);
 						 tempY = stand.getY(1);
 						 
 						 moveToStation(x1,y1);
 						 
 						 x2 = stand.getX(0);
 						 y2 = stand.getY(0);
 						 
 					// }
 				 }
 			 }
 	    	//check if inspection stand is empty
 	    	//check kit id from kit passed in
 	    	//check if stand 2 kit id matches passed kit id
 	    	//if so, grab and move to inspection
 	    	//if not check if stand 1 kit id matches passed kit id
 	    	//if so, grab and move to inspection
 	    	//if not error
 	    	
 	    }
 
 	    public void DoMoveFromInspectionToConveyor(Kit kit)
 	    {
 	    	FactoryState.out.println("Moving kit from Inspection Stand to Conveyor");
 	    	
 	    	if(floatHappened != 0)
 			{
 				y = originalY;
 			}
 	    	 floatHappened = 0;
 	    	move = true;
 	    	Kit k = kit;
 	    	
 	    	if(k.getKitId() == stand.checkKit(0).getKitId())
 	    	{
 	    		//goToConveyor = true;
 	    		
 	    		 x1 = stand.getX(0);
 				 y1 = stand.getY(0);
 				 
 				 moveToStation(x1,y1);
 
 				 goToConveyor = true;
 				 
 	    		x2 = conveyor.x;
 	    		y2 = conveyor.y+30;
 	    		
 	    		//conveyor.robotAddKit(placeKit());
 	    		//kitHeld = false;
 				//guikit = null;
 	    		
 	    	}
 
 	    }
 	    
 	    public void DoMoveKitBackToStand(Kit broken)
 	    {
 	    	move = true;
 	    	if(floatHappened != 0)
 			{
 				y = originalY;
 			}
 	    	 floatHappened = 0;
 	    	
 	    	if(broken.getKitId() == stand.checkKit(0).getKitId())
 	    	{
 	    		//goToConveyor = true;
 	    		 placeBrokenKit = true;
 	    		
 	    		 x1 = stand.getX(0);
 				 y1 = stand.getY(0);
 				 
 				 moveToStation(x1,y1);
 				 
 				 	x2 = tempX;
 		    		y2 = tempY;
 
 				 placeBrokenKit = true;
 	    		
 	    		//conveyor.robotAddKit(placeKit());
 	    		//kitHeld = false;
 				//guikit = null;
 	    		
 	    	}
 
 	    }
 	    	//added by Sam to continue end of simulation
 	    	//should be placed in correct location though.
 	    	//kitRobot.inspectionToConveyorRelease();
 
 	    	
 	    	//check if inspection kit id matches passed kit id
 	    	//if so move to conveyor
 	    	//place in conveyor
 	    	//else errorrrrrrrrr
 
 	public void DoGoHome()
 	{
 		if(floatHappened != 0)
 		{
 			y = originalY;
 		}
 		 floatHappened = 0;
 		goHome = true;
 		move = true;
 		
 		x2 = 30;
     	y2 = 200;
     	
     	moveToStation(x2,y2);
 	}
 
 	    
 	
 	public void paintComponent()
 	{
 		myDrawing.posX = x;
 		myDrawing.posY = y;
 		//y
 		
 		myDrawing.subDrawings.clear();
 		
 		myDrawing.subDrawings.add(new Drawing(x,y,"kitRobot.png"));
 		myDrawing.subDrawings.add(new Drawing(robotX,y,"robotarm.png"));
 		if(guikit!=null)
 		{
 			guikit.paintComponent();
 			myDrawing.subDrawings.add(guikit.myDrawing);
 		}
 			
 	}
 
 
 	public void updateGraphics()
 	{
 		if(guikit!=null)
 		{
 			//set coord
 			guikit.posX = robotX;
 			guikit.posY = y;
 			guikit.updateGraphics();
 		}
 		
 		
 		//System.out.println(moveToY);
 		//System.out.println("y:" + y);
 		//System.out.println("y1:" + y1);
 		//System.out.println("move to y:" + moveToY);
 		
 		/*
 		System.out.println(robotX);
 		System.out.println(moveToX);
 		System.out.println(x);
 		System.out.println(move);
 		System.out.println(fullyExtended);*/
 //		System.out.println(placeOnOne);
 //		System.out.println(placeOnTwo);
 		
 		
 		if(move){
 
 			if(robotX == moveToX)
 	         {
 	          	fullyExtended = true;
 	          	//move = false;
 	         }
             if( y < moveToY)
             {
                 y+=4;
 
             }
             else if(y > moveToY)
             {
                 y-=4;
 
             }
            
             if( x < moveToX)
             {
                 robotX+=1;
             }
            /* else if(x > moveToX)
             {
                 robotX-=1;
                 System.out.println("moved");
             }*/
             else// if (y == moveToY && x == moveToX)
             {
                 move = false;
                 //System.out.println("STOPPPPPPP");
             }
 		}
 		
 		
 		if(floatMode)
 		{
 			floatHappened++;
 			
 			if(move == false && fullyExtended == false)
 			{
 
 				if(floatCount <= 70)
 				{
 					//if(floatCount!= 2 ||floatCount!= 4 ||floatCount!= 6 ||floatCount!=  8||floatCount!= 10 ||floatCount!= 12 ||floatCount!= 14 ||floatCount!=16 ||floatCount!= 18 ||floatCount!= 20 || floatCount!= 22 ||floatCount!= 24 ||floatCount!= 26 ||floatCount!=  28||floatCount!= 30 ||floatCount!= 32 ||floatCount!= 34 ||floatCount!=36 ||floatCount!= 38 ||floatCount!= 40 )
 					if(floatCount == 35)
 					{
 						y += 6;
 					}
 					floatCount++;
 				}
 				else if(floatCount > 70)
 				{
 					//if(floatCount!= 42 ||floatCount!= 44 ||floatCount!= 46 ||floatCount!=  48||floatCount!= 50 ||floatCount!= 52 ||floatCount!= 54 ||floatCount!=56 ||floatCount!= 58 ||floatCount!= 60 || floatCount!= 62 ||floatCount!= 64 ||floatCount!= 66 ||floatCount!=  68||floatCount!= 70 ||floatCount!= 72 ||floatCount!= 74 ||floatCount!=76 ||floatCount!= 78 ||floatCount!= 80 )
 					if(floatCount == 105)
 					{
 						y -= 6;
 					}
 					floatCount++;
 					
 					if(floatCount == 141)
 					{
 						floatCount = 0;
 					}
 				}
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
         if(kitHeld)
         {
         	guikit.posX =robotX;
         	guikit.posY =y;
        }
         
         if(initialConveyor)
         {
         	
         	floatMode = false;
         	if(((y - moveToY) < 4 && goToTwo) && ((y - moveToY) > -4 && goToTwo))
         	{	
         		
         		if(fullyExtended)
         		{
         			pickupKit(conveyor.robotRemoveKit());
         			initialConveyor = false;
         			goToTwo = false;
         			placeOnTwo = true;
         		}
         	}
         	else if(((y - moveToY) < 4 && goToOne) && ((y - moveToY) > -4 && goToOne))
         	{
         		
         		if(fullyExtended)
         		{
         			pickupKit(conveyor.robotRemoveKit());
         			initialConveyor = false;
         			goToOne = false;
         			placeOnOne = true;
         		}
         	}
         }
         
         if(placeOnTwo)
         {
         	moveToStation(x2,y2);
         	//kitRobot.beReadyForNextKitSoon();
         	
 	        if(((y - y2) < 4) && ((y-y2) > -4))
 			{
 	  
 	        	if(fullyExtended)
 	        	{
 	        		kitRobot.beReadyForNextKitSoon();
 					placeKit();
 					//kitRobot.beReadyForNextKitSoon();
                     kitHeld = false;
                                         
 					guikit = null;
                                         
 					kitRobot.AnimationLockRelease();
 					placeOnTwo = false;
 					move = false;
 					floatMode = true;
 					originalY = y;
 	        	}
 			}
         }
         else if (placeOnOne)
         {
         	floatMode = false;
         	moveToStation(x2,y2);
         	
         	
         	  if(((y - y2) < 4) && ((y-y2) > -4))
   			{
      
         		  if(fullyExtended)
         		  {
         			kitRobot.beReadyForNextKitSoon();
 	  				placeKit();
 	  				//kitRobot.beReadyForNextKitSoon();
                     kitHeld = false;
                                        
   					guikit = null;
                                         
 	  				kitRobot.AnimationLockRelease();
 	  				placeOnOne = false;
 	  				move = false;
 	  				floatMode = true;
 	  				originalY = y;
         		  }
   			}
         }
         if(moveToTwo)
         {
         	floatMode = false;
         	if(((y - y1) < 4) && ((y-y1) > -4))
         	{
      
         		if(fullyExtended)
         		{
         			pickupKit(stand.guiRemoveKit(2));
         			moveToTwo = false;
         			moveToInspection = true;
         		}
         	}
         }
         if(moveToOne)
         {
         	
         	floatMode = false;
         	if(((y - y1) < 4) && ((y-y1) > -4))
         	{
    
         		if(fullyExtended)
         		{
         			pickupKit(stand.guiRemoveKit(1));
         			moveToOne = false;
         			moveToInspection = true;
         		}
         	}
         }
         if(moveToInspection)
         {
         	moveToStation(x2,y2);
         	//guikit.posX = robotX;
         	//guikit.posY = y;
    
         	if(((y - y2) < 4) && ((y-y2) > -4))
         	{
        
         		if(fullyExtended)
         		{
  //       			kitRobot.inspectLockRelease();
         			stand.addkit(placeKit(), 0);
         			kitHeld = false;
         			guikit = null;
         			moveToInspection = false;
         			kitRobot.inspectLockRelease();
         			originalY = y;
         		}
         	}
         }
         
         if(goToConveyor)
         {
         	floatMode = false;
         	if(((y - y1) < 4) && ((y-y1) > -4))
         	{
       
         		if(fullyExtended)
         		{
         			pickupKit(stand.guiRemoveKit(0));
         			goToConveyor = false;
         			finalMove = true;
         		}
         	}
         }
         if(finalMove)
         {
         	moveToStation(x2,y2);
         	floatMode = false;
         	if(((y - y2) < 4) && ((y-y2) > -4))
         	{
  //
         		moveToX= 70;
         		if(fullyExtended)
         		{
         			conveyor.DoGetKitOut(placeKit(), kitRobot);
         			kitRobot.inspectionToConveyorRelease();
         			kitHeld = false;
         			guikit = null;
         			finalMove = false;
         			move = false;
         			//kitRobot.inspectionToConveyorRelease();
         			if(stand.checkKit(2) != null)
         			{
         				 x1 = stand.getX(2);
 						 y1 = stand.getY(2);
 						 
 						 moveToStation(x1,y1);
 						 
 						 goToFullStand = true;
  
         			}
         			else if(stand.checkKit(1) != null)
         			{
         				 x1 = stand.getX(1);
 						 y1 = stand.getY(1);
 						 
 						 moveToStation(x1,y1);
 						 
 						 goToFullStand = true;
         			}
         		}
         	}
         	
         }
         
         if(goToFullStand)
         {
 
         	if(((y - y1) < 4) && ((y-y1) > -4))
         	{
         		goToFullStand = false;
         		move = false;
         		floatMode = true;
         		originalY = y;
         	}
         }
         
         if(goHome)
         {
 
         	if(((y - y2) < 4) && ((y-y2) > -4))
         	{
         		goHome = false;
         		move = false;
         		floatMode = true;
         		originalY = y;
         		
         		//System.out.println("I'm coming home again doo doo doo");
         	}
         }
         
         if(placeBrokenKit)
         {
         	floatMode = false;
         	if(((y - y1) < 4) && ((y-y1) > -4))
         	{
       
         		if(fullyExtended)
         		{
         			pickupKit(stand.guiRemoveKit(0));
         			placeBrokenKit = false;
         			placeBroken = true;
         			//System.out.println("go back back back back back");
         			
         		}
         	}
         }
         
         if(placeBroken)
         {
         	moveToStation(x2,y2);
         	floatMode = false;
         	
         	if(((y - y2) < 4) && ((y-y2) > -4))
         	{
         		if(fullyExtended)
         		{
         		
 	        		if(tempX == stand.getX(2))
 	        		{
 	        			standToGo = 2;
 	        		}
 	        		else if(tempX == stand.getX(1))
 	        		{
 	        			standToGo = 1;
 	        		}
 	        		
 	        		placeKit();
 	        		kitRobot.AnimationLockRelease();
 	        		kitHeld = false;
         			guikit = null;
 	        		placeBroken = false;
         		}
 
         	}
         	
         }
 		
 	}
 }
