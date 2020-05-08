 package layout;
 
 import java.util.*;
 
 public class Transition {
 
     private final Map<RoadSegment, RoadSegmentConnection> roadSegmentConnectionMap;
     private RoadSegmentConnection previousRoadSegmentConnection;
     private RoadSegmentConnection firstRoadSegmentConnection;
 
     public Transition() {
         roadSegmentConnectionMap = new LinkedHashMap<RoadSegment, RoadSegmentConnection>();
         firstRoadSegmentConnection = previousRoadSegmentConnection = null;
     }
 
     public void addRoadSegment(RoadSegment roadSegment, End end) {
         roadSegment.setEndTransition(end, this);
         RoadSegmentConnection roadSegmentConnection = new RoadSegmentConnection(roadSegment, end);
         roadSegmentConnectionMap.put(roadSegment, roadSegmentConnection);
         doubleLinkRoadSegmentConnection(roadSegmentConnection);
     }
 
     private void doubleLinkRoadSegmentConnection(RoadSegmentConnection roadSegmentConnection) {
         if(previousRoadSegmentConnection != null) {
             previousRoadSegmentConnection.setNext(roadSegmentConnection);
             roadSegmentConnection.setPrevious(previousRoadSegmentConnection);
             roadSegmentConnection.setNext(firstRoadSegmentConnection);
         }
         else {
             firstRoadSegmentConnection = roadSegmentConnection;
         }
         previousRoadSegmentConnection = roadSegmentConnection;
     }
 
     public void mate(RoadSegment roadSegment1, RoadSegment roadSegment2){
         RoadSegmentConnection connection1 = roadSegmentConnectionMap.get(roadSegment1);
         RoadSegmentConnection connection2 = roadSegmentConnectionMap.get(roadSegment2);
         if(connection1 == null || connection2 == null)return;
         connection1.setMate(connection2);
         if(!connection2.hasMate()) {
             connection2.setMate(connection1);
         }
     }
 
     public RoadSegment getMateRoadSegment(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment).getMate().getRoadSegment();
     }
 
     public RoadSegment getNextRoadSegment(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment).getNext().getRoadSegment();
     }
 
     public RoadSegment getPreviousRoadSegment(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment).getPrevious().getRoadSegment();
     }
 
     public End getRoadSegmentEnd(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment).getEnd();
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(" connects to  ");
         for(RoadSegmentConnection roadSegmentConnection : roadSegmentConnectionMap.values()) {
             sb.append("\n     ").append(roadSegmentConnection);
         }
         sb.append("\n");
         return sb.toString();
     }
 
     public boolean roadSegmentLaneHasChoices(RoadSegment roadSegment, Lane lane) {
         End end = roadSegmentConnectionMap.get(roadSegment).getEnd();
         if(lane.isTo()) {
             if(end == End.A)return false;
         }
         if(lane.isFrom()) {
             if(end == End.B)return false;
         }
         return true;
     }
 
     public Lane getStraightLane(RoadSegment roadSegment, Lane lane) {
         RoadLayout roadLayout = roadSegment.getLayout();
         RoadLayout mateRoadLayout = roadSegmentConnectionMap.get(roadSegment).getMate().getRoadSegment().getLayout();
         return RoadLayout.getMateLane(roadLayout, mateRoadLayout, lane);
     }
 
     public End getEnd(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment).getEnd();
     }
 
     public RoadSegment nextRoadSegment(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment).getNext().getRoadSegment();
     }
 
     public RoadSegment previousRoadSegment(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment).getPrevious().getRoadSegment();
     }
 
     public List<Lane> getTurnLanes(RoadSegment turnRoadSegment) {
         End turnRoadSegmentEnd = roadSegmentConnectionMap.get(turnRoadSegment).getEnd();
         if(turnRoadSegmentEnd == End.A) {
             return turnRoadSegment.getLayout().getLaneList(Travel.FROM);
         }
         else if(turnRoadSegmentEnd == End.B) {
             return turnRoadSegment.getLayout().getLaneList(Travel.TO);
         }
         return null;
     }
 
     class RoadSegmentConnection {
         private final RoadSegment roadSegment;
         private final End end;
         private RoadSegmentConnection mateRoadSegmentConnection;
         private RoadSegmentConnection previousRoadSegmentConnection, nextRoadSegmentConnection;
 
         public RoadSegmentConnection(RoadSegment roadSegment, End end) {
             this.roadSegment = roadSegment;
             this.end = end;
         }
 
         public String toString() {
             if(!hasMate())return roadSegment.getId() + "-" + end;
             return  roadSegment.getId() + "-" + end + "[mate: " +
                     mateRoadSegmentConnection.getRoadSegment().getId() + "-" + mateRoadSegmentConnection.getEnd() + "]";
         }
 
         public End getEnd() {
             return end;
         }
 
         public boolean hasMate() {
             return mateRoadSegmentConnection != null;
         }
 
         public RoadSegmentConnection getMate() {
             return mateRoadSegmentConnection;
         }
 
         public RoadSegment getRoadSegment() {
             return roadSegment;
         }
 
        public void setMate(RoadSegmentConnection mateRoadSegmentConnection) {
            this.mateRoadSegmentConnection = mateRoadSegmentConnection;
         }
 
         public void setNext(RoadSegmentConnection roadSegmentConnection) {
             nextRoadSegmentConnection = roadSegmentConnection;
         }
 
         public void setPrevious(RoadSegmentConnection roadSegmentConnection) {
             previousRoadSegmentConnection = roadSegmentConnection;
         }
 
         public RoadSegmentConnection getNext() {
             return nextRoadSegmentConnection;
         }
 
         public RoadSegmentConnection getPrevious() {
             return previousRoadSegmentConnection;
         }
     }
 
 }
