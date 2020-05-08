 package Ass01.ML;
 
 import java.lang.*;
 import java.util.*;
 
 public class Main {
     static int time;
     static int speed = 1;
 
     public static void main(String [] args){
         System.out.println("Hello, World");
         time = 0;
         int k = 0;
 
         //Create intersection
         Intersection intersection = new Intersection(new Position(12,10));
         Random rnd = new Random();
         QLearning ql = new QLearning();
         boolean nextMove = false;
 
         while(k < 300){
             k++;
 
             // Get the next move and exectue it
             nextMove = ql.getNextMove(getClosetPos(intersection), intersection.getLightState());
 
             if (nextMove) {
             //if (time%10 == 0) {  //if time multiple of 10, change all lights TODO: update this later to use ML
                 for (int i=0; i < intersection.getNumRoads(); i++){
                     intersection.setLightState(i, (intersection.getLightState(i)+1)%2); //toggle light state
                 }
             }
 
             ListIterator<Road> roadItr = intersection.getRoads().listIterator();
             ListIterator<Integer> lightItr = intersection.getLightState().listIterator();
             Position obstacle;
             if (lightItr.equals(Intersection.red)) {
                 obstacle = intersection.getPos();
             }
 
             while(roadItr.hasNext() && lightItr.hasNext())
             {
                 Road nextRoad = roadItr.next();
 
                 obstacle = new Position(-1,-1);
                 if (lightItr.equals(Intersection.red)) {
                     obstacle = intersection.getPos();
                 }
                 ListIterator<Car> carItr = nextRoad.getCars().listIterator();
 
                 while (carItr.hasNext()){
                     Car nextCar = carItr.next();
                     //move forward - double check, not queued at light
                     //nextCar.moveCar(obstacle, nextRoad.getDirection());
 
                     //remove car if necessary
                    if (nextCar.removeCar(intersection.getPos(),nextRoad.getDirection())) {
                         nextRoad.removeCar();
                         obstacle = new Position(-1,-1);
                     } else {
                         obstacle = nextCar.getPos();
                     }
 
                 }
                 if (time%(rnd.nextInt(10)+5)==0) {   //IS THIS CORRECT?
                     Position p = new Position(0,0);
                     if (nextRoad.getDirection() == Road.horizontal){
                        p.setY(nextRoad.getOffset());
                     } else if (nextRoad.getDirection() == Road.vertical) {
                         p.setX(nextRoad.getOffset());
                     }
 
                     Car c = new Car(p,speed);
                     nextRoad.addCar(c);
                 }
 
             }
 
             // Update the qValues
             ql.updateQValue(getClosetPos(intersection), intersection.getLightState());
             //ql.performLearning(getClosetPos(intersection), intersection.getLightState());
 
             time++;
 
         }
     }
 
     public static List<Integer> getClosetPos(Intersection intersection) {
         ListIterator<Position> closetPosItr = intersection.getClosestCarsInt().listIterator();
         List<Integer> closetCars = new ArrayList<Integer>();
         Position intPos = intersection.getPos();
 
         while (closetPosItr.hasNext()) {
             Position nextPos = closetPosItr.next();
             if (nextPos.equals(intPos)) {
                 closetCars.add(9);
             } else {
                 int x =  (Math.abs(nextPos.getX() - intPos.getX()) +
                         Math.abs(nextPos.getY() - intPos.getY()));
                 if (x > 9) {
                     x = 9;
                 }
                 closetCars.add(x);
             }
         }
         return closetCars;
     }
 
 }
