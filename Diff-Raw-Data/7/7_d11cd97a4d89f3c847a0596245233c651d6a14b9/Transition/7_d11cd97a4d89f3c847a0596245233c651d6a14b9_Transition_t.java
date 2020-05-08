 package layout;
 
 import sun.security.tools.policytool.PolicyTool;
 
 import java.util.*;
 
 public class Transition {
 
    private static boolean debug=true;
 
     private final Map<RoadSegment, RoadSegmentConnection> roadSegmentConnectionMap;
     private RoadSegmentConnection previousRoadSegmentConnection;
     private RoadSegmentConnection firstRoadSegmentConnection;
     private String name;
     private ArrayList<Choice>choices;
 
 
     public Transition(String name) {
         roadSegmentConnectionMap = new LinkedHashMap<RoadSegment, RoadSegmentConnection>();
         choices = new ArrayList<Choice>();
         firstRoadSegmentConnection = previousRoadSegmentConnection = null;
         this.name = name;
     }
 
     public Transition addRoadSegment(RoadSegment roadSegment, End end) {
         roadSegment.setEndTransition(end, this);
         RoadSegmentConnection roadSegmentConnection = new RoadSegmentConnection(roadSegment, end);
         roadSegmentConnectionMap.put(roadSegment, roadSegmentConnection);
         doubleLinkRoadSegmentConnection(roadSegmentConnection);
         return this;
     }
 
     private void doubleLinkRoadSegmentConnection(RoadSegmentConnection roadSegmentConnection) {
         if(previousRoadSegmentConnection != null) {
             linkRoadSegmentConnection(previousRoadSegmentConnection, roadSegmentConnection);
             linkRoadSegmentConnection(roadSegmentConnection, firstRoadSegmentConnection);
         }
         else {
             firstRoadSegmentConnection = roadSegmentConnection;
         }
         previousRoadSegmentConnection = roadSegmentConnection;
     }
 
     private void linkRoadSegmentConnection(RoadSegmentConnection a, RoadSegmentConnection b) {
         a.setNext(b);
         b.setPrevious(a);
     }
 
     public String getName() {
         return name;
     }
 
     public void mate(RoadSegment roadSegment1, RoadSegment roadSegment2){
         RoadSegmentConnection connection1 = getConnection(roadSegment1);
         RoadSegmentConnection connection2 = getConnection(roadSegment2);
         if(connection1 != null && connection2 != null) {
             connection1.setMate(connection2);
             connection2.setMate(connection1);
         }
     }
 
     public RoadSegment getMateRoadSegment(RoadSegment roadSegment) {
         return getConnection(roadSegment).getMate().getRoadSegment();
     }
 
     public boolean roadSegmentLaneHasChoices(RoadSegment roadSegment, Lane lane) {
         End end = getEnd(roadSegment);
         if(lane.isTo()) {
             if(end == End.A)return false;
         }
         if(lane.isFrom()) {
             if(end == End.B)return false;
         }
         return true;
     }
 
     public End getEnd(RoadSegment roadSegment) {
         return getConnection(roadSegment).getEnd();
     }
 
     public RoadSegment nextRoadSegment(RoadSegment roadSegment) {
         return getConnection(roadSegment).getNext().getRoadSegment();
     }
 
     private RoadSegmentConnection getConnection(RoadSegment roadSegment) {
         return roadSegmentConnectionMap.get(roadSegment);
     }
 
     public RoadSegment previousRoadSegment(RoadSegment roadSegment) {
         return getConnection(roadSegment).getPrevious().getRoadSegment();
     }
 
     public List<Lane> getPossibleLaneList(RoadSegment roadSegment, Lane lane, RoadSegment mateRoadSegment, Direction direction) {
         List<Lane>laneList = roadSegment.getLaneList(lane.getTravel());
         List<Lane>mateLaneList = getLeavingLaneList(mateRoadSegment);
         List<Lane>resultLaneList = new ArrayList<Lane>();
         int laneIndex= laneList.indexOf(lane);
         int mateCheckIndex = getMateIndexForLane(laneList, laneIndex, mateLaneList, direction);
         if(mateCheckIndex >= 0) {
             int testCheckIndex, laneCheckIndex=laneIndex;
             resultLaneList.add(mateLaneList.get(mateCheckIndex));
             while (true) {
 
                 if(laneIndex == 0) { // innermost, pick up all left turn lanes and 1st straight  lane
                     testCheckIndex = getNextLaneIndex(mateLaneList, mateCheckIndex, Direction.ALL);
                    if(laneList.size() > 1 && mateLaneList.get(mateCheckIndex).canGoStraight())break;
                 }
                 else {
                     testCheckIndex = getNextLaneIndex(mateLaneList, mateCheckIndex, Direction.STRAIGHT, Direction.RIGHT);
                     laneCheckIndex = getNextLaneIndex(laneList, laneCheckIndex, direction);
                 }
                 if(testCheckIndex == mateCheckIndex)break;
                 if(laneCheckIndex != laneIndex)break;
 
                 mateCheckIndex = testCheckIndex;
                 resultLaneList.add(mateLaneList.get(mateCheckIndex));
             }
         }
         return resultLaneList;
     }
 
     private int getMateIndexForLane(List<Lane>laneList, int laneIndex, List<Lane>mateLaneList, Direction direction) {
         int laneCheckIndex = -1;
         int mateCheckIndex = -1;
         do {
             laneCheckIndex = getNextLaneIndex(laneList, laneCheckIndex, direction);
             if(laneIndex == 0) {
                 mateCheckIndex = getNextLaneIndex(mateLaneList, mateCheckIndex, Direction.ALL);
             }
             else {
                 mateCheckIndex = getNextLaneIndex(mateLaneList, mateCheckIndex, Direction.STRAIGHT, Direction.RIGHT);
             }
         }while(laneCheckIndex != laneIndex);
         return mateCheckIndex;
     }
 
     private int getNextLaneIndex(List<Lane>lanes, int index, Direction ... directions) {
         // returns next index, until no more, then returns the input value
         int oldIndex = index;
         for(int i = ++index; i< lanes.size(); i++) {
             for(Direction direction: directions) {
                if(lanes.get(i).canGo(direction))return i;
             }
         }
         return oldIndex;
     }
 
     private List<Lane> getLeavingLaneList(RoadSegment roadSegment) {
         End roadSegmentEnd = getEnd(roadSegment);
         if(roadSegmentEnd == End.A) {
             return roadSegment.getLaneList(Travel.TO);
         }
         else {
             return roadSegment.getLaneList(Travel.FROM);
         }
     }
 
     private List<Lane> getEnteringLaneList(RoadSegment roadSegment) {
         End roadSegmentEnd = getEnd(roadSegment);
         if(roadSegmentEnd == End.A) {
             return roadSegment.getLaneList(Travel.FROM);
         }
         else {
             return roadSegment.getLaneList(Travel.TO);
         }
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
 
     public void plot() {
         for(Choice choice: choices) {
             choice.plot();
         }
     }
 
     public void defineLaneConnections() {
         End end,turnEnd;
         List<Lane>enteringLaneList,straightLaneList,leftTurnLaneList,rightTurnLaneList,uTurnLaneList;
         for(RoadSegment roadSegment : roadSegmentConnectionMap.keySet()) {
             if(debug==true)System.out.println("\n" + roadSegment);
             end = getEnd(roadSegment);
             enteringLaneList = getEnteringLaneList(roadSegment);
             for(Lane enteringLane: enteringLaneList) {
                if(roadSegmentLaneHasChoices(roadSegment, enteringLane)) {
                    RoadSegment turnRoadSegment, mateRoadSegment = getMateRoadSegment(roadSegment);
                    if(enteringLane.canGoLeft()) {
                        if(enteringLane.isInner()) {
                            uTurnLaneList = roadSegment.getUTurnLaneList(enteringLane.getTravel());
                            for(Lane uTurnLane: uTurnLaneList) {
                                if(debug==true)System.out.println("lane " + enteringLane.getId() + ": U-Turn   to roadSegment " + roadSegment.getId() + " lane " + uTurnLane + " (end=" + end + ")");
                            }
                        }
                        turnRoadSegment = roadSegment;
                        while((turnRoadSegment = nextRoadSegment(turnRoadSegment)) != mateRoadSegment)  {
                            turnEnd = getEnd(turnRoadSegment);
                            leftTurnLaneList = getPossibleLaneList(roadSegment, enteringLane, turnRoadSegment, Direction.LEFT);
                            for(Lane leftTurnLane: leftTurnLaneList) {
                                if(debug==true)System.out.println("lane " + enteringLane.getId() + ": L-turn   to roadSegment " + turnRoadSegment.getId() + " lane " + leftTurnLane + " (end=" + turnEnd + ")");
                                choices.add(new Choice(roadSegment, enteringLane, end, turnRoadSegment, leftTurnLane, turnEnd));
                            }
                        }
                    }
                    if(enteringLane.canGoStraight()) {
                        straightLaneList = getPossibleLaneList(roadSegment, enteringLane, getMateRoadSegment(roadSegment), Direction.STRAIGHT);
                        End mateEnd = getEnd(mateRoadSegment);
                        for(Lane straightLane: straightLaneList) {
                            if(debug==true)System.out.println("lane " + enteringLane.getId() + ": Straight to roadSegment " + mateRoadSegment.getId() + " lane " + straightLane + " (end=" +  mateEnd + ")");
                            choices.add(new Choice(roadSegment, enteringLane, end, getMateRoadSegment(roadSegment), straightLane, mateEnd));
                        }
                    }
                    if(enteringLane.canGoRight()) {
                        turnRoadSegment = roadSegment;
                        while((turnRoadSegment = previousRoadSegment(turnRoadSegment)) != mateRoadSegment)  {
                            turnEnd = getEnd(turnRoadSegment);
                            rightTurnLaneList = getPossibleLaneList(roadSegment, enteringLane, turnRoadSegment, Direction.RIGHT);
                            for(Lane rightTurnLane: rightTurnLaneList) {
                                if(debug==true)System.out.println("lane " + enteringLane.getId() + ": R-turn   to roadSegment " + turnRoadSegment.getId() + " lane " + rightTurnLane + " (end=" + turnEnd + ")");
                                choices.add(new Choice(roadSegment, enteringLane, end, turnRoadSegment, rightTurnLane, turnEnd));
                            }
                        }
                    }
                }
             }
         }
     }
 
     class Choice {
 
         ChoicePoint fromPoint,toPoint;
         public Choice(
                 RoadSegment roadSegmentFrom, Lane laneFrom, End endFrom,
                 RoadSegment roadSegmentTo, Lane laneTo, End endTo) {
              fromPoint = new ChoicePoint(roadSegmentFrom, laneFrom, endFrom);
              toPoint = new ChoicePoint(roadSegmentTo, laneTo, endTo);
 
         }
 
         private void plot() {
             double [] xy1 = fromPoint.roadSegment.getLaneEndXYLocation(fromPoint.lane, fromPoint.end);
             double [] xy2 = toPoint.roadSegment.getLaneEndXYLocation(toPoint.lane, toPoint.end);
             Plot.line(xy1, xy2, fromPoint.lane.getColor());
         }
 
     }
 
     class ChoicePoint {
         private RoadSegment roadSegment;
         private Lane lane;
         private End end;
 
         public ChoicePoint(RoadSegment roadSegment, Lane lane, End end) {
             this.roadSegment = roadSegment;
             this.lane = lane;
             this.end = end;
         }
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
