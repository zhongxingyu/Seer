 package a2;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Queue;
 
 import static a2.States.*;
 
 /**
  * Controlsystem repräsentiert die Warteschlangen vor den Ampeln, die Ampeln
  * und die Baustellendurchfahrt.
  * Der Algorithmus ist in manageTraffic() implementiert.
  * Controlsystem selbst ist kein Observer von Timer. Das Ampelsystem ist der Observer
  * und ruft bei bedarf manageTraffic() aus Controlsystem auf.
  */
 class Controlsystem  {
 
     final private int tB;
     final Parking parking;
     private Queue<Car> inQueue;
     private Queue<Car> outQueue;
     final TrafficLightController tlc;
     private int currentTime;
     final private int maxDuration;
     final private int intervalBetweenCars = 3;
     boolean terminated;
     
     /**
      * Standartkonstruktor. Wird beim Anlegen einer Simulation aufgerufen
      * @param tB Baustellendurchfarhzeit
      * @param maxDuration die maximale Ampelschaltzeit
      */
     Controlsystem(int tB, int maxDuration) {
         inQueue = new LinkedList<Car>();
         outQueue = new LinkedList<Car>();
         tlc = new TrafficLightController();
         this.tB = tB;
         this.parking = new Parking(this);
         this.maxDuration = maxDuration;
         terminated = false;
     }
 
     /**
      * Privater Copykonstruktor, der von clone() verwendet wird.
      * @param cs das Controlsystem, welches kopiert werden soll
      */
     private Controlsystem(Controlsystem cs) {
         tB = cs.tB;
         parking = new Parking(cs.parking, this);
         maxDuration = cs.maxDuration;
         inQueue = new LinkedList<Car>(cs.inQueue);
         outQueue = new LinkedList<Car>(cs.outQueue);
         tlc = new TrafficLightController(cs.tlc);
         terminated = cs.terminated;
     }
     
     /**
      * Wird von getStateSummary() aufgerufen
      * @return Stringrepräsentation von Controlsystem
      */
     @Override
     public String toString() {
         int p = parking.getParkingCars();
         return "InQueue: " + String.format("%5d", inQueue.size()) +  " " + tlc.toString() + " OutQueue:" + String.format("%5d", outQueue.size()) + " Parkplatz:" + String.format("%5d", p) + (p > Parking.space ? " !!!FEHLER!!!" : "");
     }
     
     
     @Override
     public Controlsystem clone() {
         return new Controlsystem(this);
     }
     
     /**
      * Methode zum Eintragen von Autos in die In-Queue. Wird von Street genutzt,
      * um neu-erstellte Autos in die Warteschlange vor die Baustelle einzureihen.
      * @param car das Auto, welches in die Warteschlange gestellt werden soll
      */
     void addToEntryQueue(Car car) {
             inQueue.add(car);
     }
 
     /**
      * Methode, zum Eintragen von Autos in die Out-Queue. Wird von Parking genutzt,
      * Autos in die Warteschlange zum Ausfahren einzureihen
      * @param car das Auto, das ausfahren will
      */
     void addToExitQueue(Car car) {
             outQueue.add(car);
     }
     
     /**
      * Die Objektgleichheit wird zurückgeführt auf die Inhaltsgleichheit der
      * InQueue,OutQueue und constructionRoad. Wird von Simulation.equals() 
      * für nextChangedStep() genutzt.     * 
      * @param o das Objekt, mit welchem verglichen werden soll
      * @return true, wenn Inhaltsgleich, ansonsten false
      */
     @Override
     public boolean equals(Object o) {
         if(this == o) return true;
         if(o == null) return false;
         if(getClass() == o.getClass()) {
             Controlsystem s = (Controlsystem) o;
             return (inQueue.equals(s.inQueue) &&
                     outQueue.equals(s.outQueue) &&
                     tlc.constructionRoad.equals(s.tlc.constructionRoad));
             
         }
         return false;
     }
 
     /**
      * Diese Methode repräsentiert den Algorithmus. 
      * Funktionsweise: siehe Pseudocode
      * @return false, falls die Simulation vorbei ist, ansonsten true
      */
     boolean manageTraffic() {
         int incommingCars = 0;
         if (tlc.constructionRoad.entrySet().size() > 0) {
             Iterator<Entry<Integer, Pair<States, Car>>> i = tlc.constructionRoad.entrySet().iterator();
             if (i.hasNext() && i.next().getValue().getKey() == IN)
                 incommingCars = tlc.constructionRoad.entrySet().size();
         }
         int inCount = Math.min(inQueue.size(), Parking.space-(outQueue.size()+incommingCars+parking.getParkingCars())); //zahl der Autos aus der Inqueue, die noch platz aufm dem parkplatz haben
         int inCountAsTime = (inCount-1)*intervalBetweenCars+1;											//zeit um alle autos auf den Parkplatz fahren zu lassen, die noch Platz haben
         int outQueueAsTime = (outQueue.size()-1)*intervalBetweenCars+1; //zeit um alle autos vom Parkplatz fahren zu lassen (aus der Outqueue)												
         
         if (currentTime >= Timer.CLOSETIME) {											//checks if it's time to close (no one can enter after this point)
             if (outQueue.isEmpty()) {
                 if (parking.getParkingCars() == 0) {
                     return false;														//parkinglot empty, everyone left -> program terminates
                 } else {
                     tlc.setGreen(NONE, 1);
                 }
             } else {
                 tlc.setGreen(OUT, outQueueAsTime);
             }
         } else {
             if (outQueue.isEmpty()) {
                 if(inQueue.isEmpty() || parking.getParkingCars()+incommingCars >= Parking.space) {
                     tlc.setGreen(NONE, 1);
                 } else {
                     tlc.setGreen(IN, Math.min(maxDuration,inCountAsTime));
                 }
             } else {
                 if(inQueue.isEmpty() || parking.getParkingCars()+outQueue.size()+incommingCars >= Parking.space) {
                     tlc.setGreen(OUT, Math.min(maxDuration,outQueueAsTime));
                 } else {																//autos wollen (und koennen) rein + autos wollen raus
                     if(tlc.currentState == OUT) {
                         tlc.setGreen(IN, Math.min(maxDuration,inCountAsTime));
                     } else if(tlc.currentState == IN){		
                         tlc.setGreen(OUT,Math.min(maxDuration,outQueueAsTime));
                     } else {
                         if(inCount > outQueue.size()) {
                             tlc.setGreen(IN, Math.min(maxDuration,inCountAsTime));
                         } else {
                             tlc.setGreen(OUT, Math.min(maxDuration,outQueueAsTime));
                         }
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * Diese Klasse repräsentiert die Ampel mit Baustellendruchfahrt.
      * Sie ist ebenfalls ein Observer des Timers, und ruft in update() manageTraffic() auf
      */
     private class TrafficLightController implements Observer {
         private States currentState;	//momentaner State
         private States nextState;	//vom Leitsystem vorgegebener State
         private int askAgain;				//n�chste anfrage an leitsystem
         private int waitUntil;				//internes warten bei state wechsel
         private Map<Integer, Pair<States,Car>> constructionRoad = new HashMap<Integer, Pair<States,Car>>(); //Baustellendurchfahrt Abbildung Ausfahrtzeit => Auto
 
         /**
          * Der Default-Konstruktor, der beim Anlegen einer neuen Simulation von
          * Controlsystem  aufgerufen
          * Die Baustellendurchfahrt wird als Map implementiert.
          * Ausfahrtzeit => ({IN,OUT}, Auto)
          */
         TrafficLightController() {
             currentState = NONE;
             nextState = NONE;
         }
         
         /**
          * CopyKonstruktor, der im Copykonstruktor von Controlsystem aufgerufen wird.
          */
         TrafficLightController(TrafficLightController tlc) {
             currentState = tlc.currentState;
             nextState = tlc.nextState;
             askAgain = tlc.askAgain;
             waitUntil = tlc.waitUntil;
             for(Entry<Integer, Pair<States,Car>> entry : tlc.constructionRoad.entrySet()) {
                if (entry.getValue() == null)
                    System.out.println("TrafficLightController(tlc)");
                 constructionRoad.put(entry.getKey(), entry.getValue());
             }
         }
         
         @Override
         public String toString() {
             return "Ampel: " + this.currentState + " Straße:" + String.format("%5d", this.constructionRoad.entrySet().size());
         }
         
         /**
          * Methode, um ein Auto in die Baustellendurchfahrt einfahren zu lassen.
          * Als Fahrtrichtung wird der aktuelle Zustand der Ampel genommen.
          * Wird in update() aufgerufen
          * @param car das Auto, dass in die Baustellendurchfahrt einfahren soll
          */
         void driveIn(Car car) {
             constructionRoad.put(currentTime+tB, new Pair<States, Car>(currentState, car));
         }
 
         /**
          * Methode zum schalten der Ampel für eine gewisse Zeit.
          * @param newState neuer Zustand
          * @param duration Dauer der neuen Ampelphase
          */
         void setGreen(States newState, int duration) {
              if (currentState == NONE) {
                 if(!constructionRoad.isEmpty()) {
                     Iterator<Pair<States,Car>> iter = constructionRoad.values().iterator();
                     iter.hasNext();
                     if(newState == iter.next().getKey()) {
                         currentState = newState;
                         nextState = newState;
                         waitUntil = currentTime;
                         askAgain = currentTime+duration;
                     } else {
                         currentState = newState;
                         nextState = newState;
                         waitUntil = currentTime+tB;
                         askAgain = waitUntil+duration;
                     }
                 } else {
                     currentState = newState;
                     nextState = newState;
                     waitUntil = currentTime;
                     askAgain = currentTime+duration;
                 }
             } else if (newState == NONE ){
                 currentState = newState;
                 nextState = currentState;
                 waitUntil = currentTime+intervalBetweenCars;
                 askAgain = waitUntil+duration;
             } else if (currentState != newState) {
                 currentState = NONE;
                 nextState = newState;
                 waitUntil = currentTime+tB;
                 askAgain = waitUntil+duration;
             } else {
                 waitUntil = currentTime;
                 askAgain = currentTime+duration;
             }
         }
 
         /**
          * Kümmert sich um das Aufrufen von manageTraffic() und um die Simulation
          * der Baustellendurchfahrt.
          * @param o Timer
          * @param arg aktueller Timerstand als Integer
          */
         @Override
         public void update(Observable o, Object arg) {
             if(o instanceof Timer && arg != null && arg instanceof Integer) {
                 int timestamp = (Integer) arg;
                 currentTime = timestamp;
                 if(timestamp >= waitUntil) {
                     currentState = nextState;
                     if(timestamp >= askAgain) {
                         if(!Controlsystem.this.manageTraffic()) {
                                 terminated = true;
                         }
                     }	
                 }
                 if((askAgain-currentTime)%3 == 1 ) {
                     if(currentState == IN && !inQueue.isEmpty()) {
                             driveIn(inQueue.poll());
                     } else if(currentState == OUT && !outQueue.isEmpty()) {
                             driveIn(outQueue.poll());
                     }
                 }
                 if (constructionRoad.containsKey(currentTime)) {
                     Pair<States,Car> pair = constructionRoad.get(currentTime);
                     constructionRoad.remove(currentTime);
                    if (pair.getValue() == null)
                        System.out.println("PAIR.VALUE!!!");
                     if (pair.getKey() == IN)
                         parking.parkCar(pair.getValue(), pair.getValue().getParkingDuration() + currentTime);
                 }
             }
         }
     }
 }
