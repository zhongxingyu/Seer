 package gui;
 
 import agents.PartRobotAgent;
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.concurrent.Semaphore;
 
 import state.FactoryState;
 import state.transducers.*;
 
 public class GUI_PartRobot extends GUI_Component{
     
     int x, y;   
     
     int gripX, gripY;
     int vrailX, railGripY;
     boolean move = false, changeGripper = false, getPart = false, putPart = false;
     int moveToX, moveToY;
     
     int gripperRailOffset = 0;
     int moveToGripper;
     int[] gripperCoords = {0,7,35,42,70,77,105,112};        //gripper coordinate offsets
     
     int partsHeld = 0;
     int curGripper;
     boolean partIn = false;
     
     GUI_Part[] myParts = {null,null,null,null};
         
     FactoryState myState;   
     PartRobotAgent myAgent;
     
     Semaphore waiter;
 
     public GUI_PartRobot(FactoryState factoryState, PartRobotAgent agent, int xpos, int ypos) {
         myState = factoryState;
         myAgent = agent;
 
         this.x = xpos;
         this.y = ypos;
         
         
         vrailX = 10;
         railGripY = 50;   
         curGripper = 0;
         
         myDrawing = new Drawing(x+0, y+0, "hrail.png");
         
         this.paintComponent();               
         waiter = new Semaphore(0);       
 
     }
     
     public void doTransferParts(ArrayList<GUI_Part> partsToGet, ArrayList<Integer> nestIndices){
         System.out.println("=============================DO TRANSFER PARTS CALLED============================================");
        for(int i = 0; i < partsToGet.size(); i++){
            System.out.println(partsToGet.get(i).name);            
        }
        System.out.println("==================================================================================================");
         if(partsToGet.size() > 0){
         try{
             for(int i = 0; i < 8; i++){
                 doMoveToNest(i);
                 waiter.acquire();
                 for(int j = 0; j < partsToGet.size(); j++){
                     if(partsToGet.get(j).name.equals(myState.guiNestList.get(i).getPartHeld())){
                         doSelectGripper(j,true);
                         waiter.acquire();
                         doGetPart(partsToGet.get(j),i);
                         waiter.acquire();
                     }
                 }                
                 if(partsHeld == partsToGet.size()){
                     break;
                 }
             }
             for(int k = 0; k < partsToGet.size(); k++){
                 doMoveToKitStand(nestIndices.get(k));
                 waiter.acquire();
                 doSelectGripper(k,false);
                 waiter.acquire();
                 doPutPart(partsToGet.get(k),Math.abs(nestIndices.get(k) - 2));
                 waiter.acquire();
             }  
             doSelectGripper(0,true);
             waiter.acquire();
             doGoHome(); 
             
             waiter.acquire();
             
             //myState.transducer.fireEvent(TChannel.Agents, TEvent.DonePickingUpParts, null);
             myAgent.DoPickedUpParts();
         }
         catch(Exception e){
             e.printStackTrace();
         }
         }
                 
     }
         
     void doSelectGripper(int n, boolean nestside) //Select active gripper (n = 0,1,2,3), boolean for which side (kitstand or nests)
     {
         
         changeGripper = true;
         if(nestside){
             moveToGripper = n*35;        
         }
         else{
             moveToGripper = n*35 + 40;        
         }
         curGripper = n;
         
     }
 
     void doMoveToNest(int x) //Move to nest x (x = 0-7)
     {
         if(x <= 7 && x >= 0){
             move = true;        
             moveToX = myState.guiNestList.get(x).getPosX() - this.x;
            moveToY = myState.guiNestList.get(x).getPosY()+30 - this.y;  
         }        
     }
 
     void doMoveToKitStand(int x) //Move to kitting stand position x (x = 0,1)
     {
         if(x <= 1 && x >= 0){
         move = true;        
         moveToX = myState.guiKitStand.getX(x) - this.x + 150;
         moveToY = myState.guiKitStand.getY(x)+30 - this.y;  
         }
     }
 
     void doGoHome() //Move to home position
     {        
         move = true;        
         moveToX = 10;
         moveToY = 50;  
         
     }
 
     void doGetPart(GUI_Part gpart,int nest) //Pick up part from nest.
     {
         
         myState.guiNestList.get(nest).removeGUIPart(gpart);
         myParts[curGripper] = gpart;     
         myParts[curGripper].x = this.x+vrailX-gripperRailOffset+gripperCoords[2*curGripper]+5;
         myParts[curGripper].y = this.y+railGripY-20;       
         
         partsHeld++;
         getPart = true;
     }
 
     void doPutPart(GUI_Part gpart,int kitStandNum) //Put down part in into kitting stand 
     {
     	//Tests
         //System.out.println("kitStandNum: " + kitStandNum );
         //System.out.println("gpart NULL? " + gpart == null);
         myState.guiKitStand.addPart(kitStandNum, gpart);
         
         myParts[curGripper] = null;
         partsHeld--;
         putPart = true;
     }
     
     void movePartsInGrippersX(int move){        
         for(int i = 0; i < 4; i++){
             if(myParts[i] != null){                
                 myParts[i].x += move; 
                 
             }
         }        
     }
     
     void movePartsInGrippersY(int move){
         for(int i = 0; i < 4; i++){
             if(myParts[i] != null){
                 myParts[i].y += move;                 
             }
         }        
     }
 
     public void updateGraphics() {
         
         if(move){
             if(vrailX < moveToX){
                 vrailX+=1;
                 movePartsInGrippersX(1);
             }
             else if(vrailX > moveToX){
                 vrailX-=1;
                 movePartsInGrippersX(-1);
             }
             else if(railGripY < moveToY){
                 railGripY+=1;
                 movePartsInGrippersY(1);
             }
             else if(railGripY > moveToY){
                 railGripY-=1;
                 movePartsInGrippersY(-1);
             }
             else{
                 move = false;
                 
                 waiter.release();
             }
         }
         if(changeGripper){
             if(gripperRailOffset < moveToGripper){
                 gripperRailOffset+=1;
                 movePartsInGrippersX(-1);
                 
             }
             else if(gripperRailOffset > moveToGripper){
                 gripperRailOffset-=1;
                 movePartsInGrippersX(+1);
                 
                 
             }            
             else{
                 changeGripper = false;
                 
                 waiter.release();
             }
             
         }
         if(getPart){
             if((gripperCoords[2*curGripper+1] < 35*(curGripper+1)) && !partIn){
                 gripperCoords[2*curGripper+1]+=1;
             }            
             else if(gripperCoords[2*curGripper+1] >= (35*(curGripper+1) - 5)){                
                 if(!partIn){
                     partIn = true;
                 }
                 gripperCoords[2*curGripper+1]-=1;
             }
             else{
                 getPart = false;
                 partIn = false;
                 
                 waiter.release();
             }
         }
         if(putPart){
             if((gripperCoords[2*curGripper+1] < 35*(curGripper+1)) && !partIn){
                 gripperCoords[2*curGripper+1]+=1;
                 
             }            
             else if(gripperCoords[2*curGripper+1] > (35*(curGripper) + 7)){                
                 if(!partIn){
                     partIn = true;
                 }
                 gripperCoords[2*curGripper+1]-=1;
             }
             else{
                 putPart = false;
                 partIn = false;
                 
                 waiter.release();
             }
         }
         
 
     }  
     
 
     public void paintComponent() //update PartRobot's drawing
     {
         myDrawing.subDrawings.clear();
         
                
         myDrawing.subDrawings.add(new Drawing(x+vrailX, y+15, "vrail.png"));
         myDrawing.subDrawings.add(new Drawing(x+vrailX, y+railGripY, "railgrip.png"));
         myDrawing.subDrawings.add(new Drawing(x+vrailX-gripperRailOffset, y+railGripY, "GripperRail.png"));
         
         for(int i = 0; i < 8; i++){
             if(i%2 == 0){
                 myDrawing.subDrawings.add(new Drawing(x+vrailX-gripperRailOffset+gripperCoords[i], y+railGripY-45, "Gripper.png"));
             }            
             else{
                 myDrawing.subDrawings.add(new Drawing(x+vrailX-gripperRailOffset+gripperCoords[i], y+railGripY-45, "GripperR.png"));            
             }
         }       
         
         for(GUI_Part p : myParts) {
         	if(p != null) {
         		p.paintComponent();
         		myDrawing.subDrawings.add(p.myDrawing);
         	}
         }
        
     }
     
     public void paintComponent(Component j, Graphics2D g) //print card at desired location
     {
         
     }
 }
