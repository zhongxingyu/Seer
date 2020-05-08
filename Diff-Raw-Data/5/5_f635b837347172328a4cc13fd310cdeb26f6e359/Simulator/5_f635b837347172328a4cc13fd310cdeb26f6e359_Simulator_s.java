 
 import java.util.*;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 
 /**
  *
  * @author hp
  */
 public class Simulator extends Thread implements SimulatorCommandInterface {
 
     private GlobalData globalData;
     private Collection<Plane> planes;
     private LinkedBlockingQueue<Runnable> transmissions;
     private Collection<Weather> weathers;
     private Controller controller;
     private Airport airportA;
     private Airport airportB;
     private double startTime = 0;
    private boolean useScenario = true;
 
     /**
      *
      * @param args
      * @throws InterruptedException
      */
     
     public static void main(String args[]) throws InterruptedException {
 
 
 
         GlobalData globalData = new GlobalData();
         final NewJFrame gui = new NewJFrame();
         Simulator simulator = new Simulator(globalData, gui);
         GUIController controller = new GUIController((ControllerDataInterface) globalData, gui);
 
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 gui.setVisible(true);
             }
         });
 
 
         simulator.setController(controller);
         simulator.setup();
         simulator.start();
 
 
 //        InterfaceGUI gui = new InterfaceGUI(simulator.airportsHash, simulator.planesHash);
 
 //        gui.run();
 //        gui.start();
 //        (new Thread(gui)).start();
 
         while (!Simulator.interrupted()) {
             controller.control();
         }
     }
     private final NewJFrame gui;
 
     private void setup() {
 
 
         this.controller.setSimulator(this);
         
         if (this.useScenario) {
             new ScenarioParser(this);
         } else {
 
             this.makeAirport("Winterfell", World.pxToKm(300), World.pxToKm(280), 0, 2);
             this.makeAirport("Dothraki Sea", World.pxToKm(700), World.pxToKm(600), 10, 2);
             this.makeAirport("Quarth", World.pxToKm(900), World.pxToKm(900), 0, 2);
             this.makeAirport("King's landing", World.pxToKm(385), World.pxToKm(530), 0, 2);
             this.makeAirport("Lannisport", World.pxToKm(120), World.pxToKm(700), 0, 2);
             this.makeAirport("The Wall", World.pxToKm(380), World.pxToKm(90), 0, 2);
             this.makeAirport("Pyke Castle", World.pxToKm(130), World.pxToKm(580), 0, 2);
 
             this.makeWeather(0.2, World.pxToKm(200),World.pxToKm(200),World.pxToKm(300),World.pxToKm(350),World.hToMs(0),World.hToMs(100),World.speedHToMs(100),World.speedHToMs(50));
             this.makeWeather(0.8, World.pxToKm(400),World.pxToKm(200),World.pxToKm(450),World.pxToKm(250),World.hToMs(0),World.hToMs(100),World.speedHToMs(100),World.speedHToMs(50));
         }
 
         this.gui.setAirports(this.globalData.airports);
         this.gui.setWeathers(this.weathers);
         this.gui.setPlanes(this.planes);
     }
 
     public Airport makeAirport(String name, double x, double y, double z, int runways) {
         System.out.println("Making airport " + name + "," + x + "," + y + "," + z);
         Airport airport = new Airport(new AirportCharacteristics(name, new Point3D(x, y, z), runways));
         this.globalData.airports.add(airport);
         return airport;
     }
     
     public Weather makeWeather(double speedRatio, double lon1, double lat1, double lon2, double lat2, double startTime, double endTime, double dx, double dy) {
         Weather weather = new Weather(speedRatio,lon1,lat1,lon2,lat2,startTime,endTime,dx,dy);
         this.weathers.add(weather);
         return weather;
     }
 
     /**
      *
      * @param globalData
      * @param gui
      */
     public Simulator(GlobalData globalData, NewJFrame gui) {
         this.gui = gui;
         this.globalData = globalData;
         this.planes = Collections.synchronizedSet(new HashSet<Plane>());
         this.transmissions = new LinkedBlockingQueue<Runnable>();
 
         this.weathers = Collections.synchronizedSet(new HashSet<Weather>());
     }
 
     /**
      * This function runs the whole simulation updating the positions of the
      * planes, answering to the queries..
      */
     private void simulate() {
         double startTime, time,now,last,dt;
         startTime = last = new Date().getTime();
         
         while (true) {
             now = new Date().getTime();
             time = World.duration(now,startTime);
             dt = World.duration(now,last);
             last = now;
             
             synchronized(this.weathers) {    
                 Iterator<Weather> it = this.weathers.iterator();
                 while (it.hasNext()) {
                     Weather weather = it.next();
                     weather.setActive(time);
                     if (weather.getActive()) {
                         weather.update(dt);
                     }
                 }
             }
             synchronized (this.planes) {
                 Iterator<Plane> it = this.planes.iterator();
                 while (it.hasNext()) {
                
                     Plane plane = it.next();
 
                     
                     if (plane.getStatus() == FlightStatus.STATUS_INFLIGHT || 
                         plane.getStatus() == FlightStatus.STATUS_EMERGENCY) {  
                         double speedRatio = 1;
                         
                         synchronized (this.weathers) {
                             Iterator<Weather> it2 = this.weathers.iterator();
                             while (it2.hasNext()) {
                                 Weather weather = it2.next();
                                 if (weather.getActive() && weather.contains(plane.getPosition())) {
                                     speedRatio *= weather.speedRatio;
                                 }
                             }
                         }
                         
                         plane.setSpeedRatio(speedRatio);
                         
                         plane.getTrajectory().update(dt, plane.getSpeed());
                                                
                         if (Plane.inerror(new Date().getTime(), plane)) {
                           TrajectoryError error = plane.isinerror((double)new Date().getTime());
                           Point3D e = new Point3D(error.getdx()*plane.getSpeed()*(last-now),error.getdy()*plane.getSpeed()*(last-now),plane.getTrajectory().current().z);
                           plane.getTrajectory().modify1 (e);                      
                         }
                         
                         plane.setLastUpdate(new Date((long)now));
                     }
                     
                     if (plane.getStatus() == FlightStatus.STATUS_INFLIGHT || 
                         plane.getStatus() == FlightStatus.STATUS_EMERGENCY || 
                         plane.getStatus() == FlightStatus.STATUS_WAITING_LANDING) {
                         
                        plane.setFuel(plane.fuel - dt);  
                         
 
                        if (plane.fuel <= 0) {
                           plane.setStatus(FlightStatus.STATUS_CRASHED);
                           System.out.println("crash_fuel" + plane.getID());
                           Statistics.incrnb_crash_fuel ();
                        }
                        if (plane.collision(this.planes, plane)) {
                            plane.setStatus(FlightStatus.STATUS_CRASHED);
                            System.out.println("crash_collision" + plane.getID());
                            Statistics.incrnb_crash_collision ();
                        }
 
                        if (plane.fuel < plane.initialFuel*0.1) {
                            plane.setStatus(FlightStatus.STATUS_EMERGENCY);
                            this.controller.requestEmergencyLanding(plane.getID(),plane.fuel);
                       }
 
                        if (plane.getSpeed() < (double)300/1000/3600) {
                            plane.setStatus(FlightStatus.STATUS_CRASHED);
                            System.out.println("crash_speed" + plane.getID());
                            Statistics.incrnb_crash_speed ();
                        }
                        if (plane.critical(this.planes, plane)) {
                           Plane p2 = plane.isCritical(this.planes, plane);
                           Point3D pos1 = plane.getPosition ();
                           Point3D pos2 = p2.getPosition ();
                             if (pos1.z < pos2.z) {
                            Trajectory l = plane.getTrajectory();
                               Point3D a = l.current ();
 //                              Point3D b = l.second ();
 //                              Point3D c = Point3D.moins(b,a);
 //                              Point3D d = Point3D.div(c, a.distance(b));
                               Point3D m = new Point3D(a.x , a.y , a.z-2);
                               Point3D n = new Point3D(m.x+15, m.y+15 , m.z);
                               Point3D o = new Point3D(n.x, n.y , n.z+2);                              
                               l.insert3(m,n,o);                           
                               
                             }
                             if (pos1.z >= pos2.z){
                               Trajectory l = plane.getTrajectory();
                               Point3D a = l.current ();
 //                              Point3D b = l.second ();
 //                              Point3D c = Point3D.moins(b,a);
 //                              Point3D d = Point3D.div(c, a.distance(b));
                               Point3D m = new Point3D(a.x , a.y , a.z+2);
                               Point3D n = new Point3D(m.x+15, m.y+15 , m.z);
                               Point3D o = new Point3D(n.x, n.y , n.z-2);                              
                               l.insert3(m,n,o);
                                 
                             }
                         }
          
                     }
                             
                     if (plane.getStatus() == FlightStatus.STATUS_INFLIGHT) {
                         if (plane.getTrajectory().terminated()) {
                             plane.setStatus(FlightStatus.STATUS_WAITING_LANDING);
                             this.controller.requestLanding(plane.getID());
                             Date a = plane.getTakeoffDate();
                             Statistics.addsum_time(World.duration(now, (double)a.getTime()));
                             
                         }
                     }
                 }
         
             }
             this.gui.repaintMap();
             
             Runnable task;
             task = this.transmissions.poll();
             if (task != null) {
                 task.run();
             }
             
             for (Airport airport : this.globalData.airports) {
                 //System.out.println("Airport " + airport + " : " + airport.acceptLanding() + ","+airport.acceptWaiting());
 //                System.out.println("ETat airport " + airport.name + " empty? " + airport.waitingPlanes.isEmpty() + " accept? " + airport.acceptLanding());
                 Collection<Plane> landed = airport.landPlanes();
                 for (Plane plane : landed) {
                     this.planes.remove(plane);
                     Statistics.incrnb_landing ();
                 }
             }
             
             try {
                 Thread.sleep(30);
                 
                 if (Math.random() < 0.10*90/500) { 
                     Pair<Airport, Airport> trip = this.getRandomTrip();
                     
                     this.controller.requestNewFlight(trip.fst.id, trip.snd.id);
                 }
             } catch (InterruptedException ex) {
                 Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
             }
 
 
         }
     }
 
     /**
      *
      * @param id
      * @return
      */
     public Plane getPlaneByID(FlightID id) {
         Iterator<Plane> it = this.planes.iterator();
         Plane plane;
         while (it.hasNext()) {
             plane = it.next();
             if (plane.getID() == id) {
                 return plane;
             }
         }
         throw new IllegalArgumentException("unknown flight id");
     }
     
     /**
      *
      * @return
      */
     public Pair<Airport, Airport> getRandomTrip() {
         Random random = new Random();
         int max = this.globalData.airports.size();
         int sourceIndex = random.nextInt(max);
         int destIndex = random.nextInt(max);
         while (sourceIndex == destIndex) { destIndex = random.nextInt(max); }
         Airport source = this.globalData.airports.get(sourceIndex);        
         Airport dest = this.globalData.airports.get(destIndex);
         return new Pair<Airport, Airport>(source, dest);
     }
         
     /**
      * The thread's main loop function
      */
     public void run() {
         simulate();
     }
 
     /* Implementation of PlaneSimulatorInterface interface.
      * The functions below are used for asynchronous communication
      * between PlaneSimulator and Controller. To make a request or answer 
      * an early request, Controller will call these functions, then wait
      * for the answer.
      */
     /**
      *
      * @param id
      */
     public void requestTrajectory(final FlightID id) {
         final Simulator self = this;
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 Trajectory trajectory = id.getPlane().getTrajectory();
                 self.controller.respondTrajectory(id, trajectory);
             }
             
             public TaskType type() { return TaskType.REQUEST_TRAJECTORY; }
         };
         this.transmissions.add(r);
     }
 
     /**
      *
      * @param id
      */
     public void requestStatus(final FlightID id) {
         final Simulator self = this;
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 FlightStatus status = id.getPlane().getStatus();
                 self.controller.respondStatus(id, status);
             }
             public TaskType type() { return TaskType.DEFAULT; }
         };
         this.transmissions.add(r);
     }
 
     /**
      *
      * @param id
      */
     public void requestSpeed(final FlightID id) {
         final Simulator self = this;
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 double speed = id.getPlane().getSpeed();
                 self.controller.respondSpeed(id, speed);
             }
             public TaskType type() { return TaskType.DEFAULT; }
         };
         this.transmissions.add(r);
     }
 
     /**
      *
      * @param id
      */
     public void requestInitialSourceDestination(final FlightID id) {
         final Simulator self = this;
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 Airport sourceAirport = id.getPlane().getInitialSourceAirport();
                 Airport destinationAirport = id.getPlane().getInitialDestinationAirport();
                 self.controller.respondInitialSourceDestination(id, sourceAirport, destinationAirport);
             }
             public TaskType type() { return TaskType.DEFAULT; }
         };
         this.transmissions.add(r);
     }
 
     /**
      *
      * @param id
      */
     public void requestDestinationAirport(final FlightID id) {
         final Simulator self = this;
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 AirportID airportId = id.getPlane().getInitialDestinationAirport().id;
                 self.controller.respondDestinationAirport(id, airportId);
             }
             public TaskType type() { return TaskType.DEFAULT; }
         };
         this.transmissions.add(r);
     }
 
     /* Private attributes */
     /**
      *
      * @param id
      */
     @Override
     public void respondTakeoff(final FlightID id) {
         final Simulator self = this;
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 Plane plane = id.getPlane();
                 plane.setStatus(FlightStatus.STATUS_INFLIGHT);
                 plane.setLastUpdate(new Date());
                 plane.setTakeoffDate(new Date()); 
             }
             public TaskType type() { return TaskType.RESPONSE_TAKEOFF; }
         };
         this.transmissions.add(r);
     }
 
     /**
      *
      * @param id
      * @param date
      */
    
     public void respondLanding(final FlightID id, final Date date) {
         final Simulator self = this;
         final Airport destination = id.getPlane().getInitialDestinationAirport();
         final long diff = date.getTime() - (new Date()).getTime();
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 Plane plane = id.getPlane();
                 plane.setLandingDate(date);
                 destination.addWaitingPlane(id);
             }
             public String toString() {
                 return "" + destination.name + "sera détruit dans" + (diff/1000) + " secondes !";
             }
             public TaskType type() { return TaskType.RESPONSE_LANDING; }
         };
         this.transmissions.add(r);
     }
     
     public void requestChangeCourse(final FlightID id, final Airport newDestination, final Trajectory newTrajectory) {
         final Simulator self = this;
         if (id.getPlane().insilence(new Date ().getTime())) { return;}
         Task r = new Task() {
             public void run() {
                 Plane plane = id.getPlane();
                 plane.setStatus(FlightStatus.STATUS_INFLIGHT);
                 
                 System.out.println("Oh hey, you should be changing course!!! " + id);
                 System.out.println("The current trajectory is :" + plane.getTrajectory());
                 plane.setDestination(newDestination);
                 plane.setTrajectory(newTrajectory);
             }
             
             public String toString() {
                 return "Nouvelle destination : " + newDestination.name + ".";
             }
             public TaskType type() { return TaskType.REQUEST_CHANGE_COURSE; }
         };
         this.transmissions.add(r);
     }
 
     /**
      *
      * @param id
      * @param s
      * @param d
      * @param traj
      */
     @Override
     public void respondNewFlight(final FlightID id, final AirportID s, final AirportID d, final Trajectory traj) {
 //        System.out.println("respondNewFlight sync" + id + " " + s + " " + d);
         final Simulator self = this;
        if ( id.getPlane().insilence(new Date ().getTime())) {return;}
             
         
         Task r = new Task() {
             public void run() {
 //                System.out.println("respondNewFlight async" + id + " " + s + " " + d);
                 Plane plane = new Plane(id, self.globalData.getAirportByID(s), self.globalData.getAirportByID(d));
                 self.planes.add(plane);
                 plane.setTrajectory(traj);
                 plane.setStatus(FlightStatus.STATUS_WAITING_TAKEOFF);
                 self.controller.requestTakeoff(plane.getID());
             }
             public TaskType type() { return TaskType.RESPONSE_NEWFLIGHT; }
         };
         this.transmissions.add(r);
     
     }
 
     /**
      *
      * @return
      */
     public Controller getController() {
         return controller;
     }
 
     /**
      *
      * @param controller
      */
     public void setController(Controller controller) {
         this.controller = controller;
     }
     
     public double time() {
         return World.duration(new Date().getTime(),startTime);
     }
 }
