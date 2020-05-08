 package agents;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import gui.GUI_Nest;
 
 import java.util.*;
 
 public class NestAgent extends Agent implements Nest {
 
     /*** Data Structures **/
     GUI_Nest guiNest;
     int requested;
     int needed;
     String partType;        
     List<Part> parts; // parts in the Nest
     List<Part> sendParts; // parts to send
     PartRobot partRobot;
     PartVision vision;
     NestState nestState;
     Lane lane;
     
     enum NestState { none, inactive, requestReceived, requested, checking, checked }
     /*** Constructor **/
     
     // Lane lane in constructor too
     public NestAgent(String partType, PartRobot partRobot, GUI_Nest guiNest, PartVision vision) {
         this.requested = 0;
         this.needed = 0;
         this.partType = partType;
         this.parts = new ArrayList<Part>();
         this.partRobot = partRobot;
         this.nestState = NestState.inactive;
         this.guiNest = guiNest;
         this.vision = vision;
         this.lane = lane;
     }
 
     /*** Messages ***/
 
     /*  Message to ask NestAgent to get parts!
      *  Source: PartsRobot
      */
     public void msgRequestParts(String type, int count) {
     
         if(!partType.equals(type)) 
         { 
             DoFlushParts(type);
             parts = new ArrayList<Part>();
             partType = type;
         }
         requested = count;  
         nestState = NestState.requestReceived;
 
         stateChanged();
     }
 
     public void msgHereIsPart(Part p) {
 
         parts.add(p);
         DoPutPartArrivedAtNest(p); // agent first or graphics first?
         stateChanged();
     }
 
     public void msgPartsAreGood(List<Part> visionParts)
     {
         sendParts = visionParts;
         nestState = NestState.checked;
         stateChanged();
     }
 
     public boolean pickAndExecuteAnAction() {
 
         if(nestState == NestState.requestReceived)
         {
             int current = parts.size();
             needed = requested - current;
             requestParts(needed);
             nestState = NestState.requested;
             return true;
         }
 
         if(nestState == NestState.requested)
         {
            if(parts.size() >= needed)
             {
                needed = 0;
                 checkParts();
                 nestState = NestState.checking;
             }
             return true;
         }
 
         if(nestState == NestState.checked)
         {
             sendParts();
             nestState = NestState.inactive;
             return true;
         }
 
         return false;
     }
 
     private void requestParts(int num)
     {
         if(num > 0)
         {
             //lane.msgRequestParts(partType,num);
         }
        else needed = 0;
         stateChanged();
     }
 
     private void checkParts()
     {
         List<Part> visionParts = new ArrayList<Part>();
 
 
         for(int i=0; i<requested; i++)
         {
             visionParts.add(parts.get(i));
         }
         for(int i=0; i<requested; i++)
         {
             parts.remove(0);
         }
         requested = 0;
 
         vision.msgVerifyParts(visionParts, this);
         stateChanged();
     }
 
     private void sendParts()
     {
         partRobot.msgHereAreParts(sendParts);
         sendParts = new ArrayList<Part>();
     }
 
     // TODO
     private void DoFlushParts(String type)
     {
         // also case when nest isn't initialized yet
         // tell gui to flush parts and change GuiNest part type
         guiNest.setPartHeld(type);
         stateChanged();
     }
 
     // TODO
     private void DoPutPartArrivedAtNest(Part p)
     {
         // call gui to put parts on the nest
         guiNest.doPutPartArrivedAtNest(p);
         stateChanged();
     }
 
     public String getPartType()
     {
         return partType;
     }
 
 }
