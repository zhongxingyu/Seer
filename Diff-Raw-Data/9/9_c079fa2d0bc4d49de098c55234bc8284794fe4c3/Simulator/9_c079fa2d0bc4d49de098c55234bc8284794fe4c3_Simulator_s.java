 package air;
 
 import java.util.Random;
 import java.util.Vector;
 
 import mpi.RecvThread;
 import mpi.SendThread;
 import p2pmpi.mpi.MPI;
 import utils.AirportLogger;
 import worldgui.WorldGui;
 
 
 /**
  * @author ps
  * Manages simulation clock, a event queue and world consisting of aircrafts
  * and airports
  */
 public class Simulator implements EventScheduler{
 
 	private SimWorld world;
 	private WorldGui wg; 
 	
 	private Vector<Event> evList; // time ordered list
 	
 	private WorldClock clock = new WorldClock();
 	
 	private int mpiRank;
 	
 	public long getRtStartTime() {
 		return clock.getRtStartTime();
 	}
 	
 	public Simulator (SimWorld world){
 		this.world = world;
 		world.setSimulator(this);
 		evList = new Vector<Event>();
 	}
 
 	@Override
 	public synchronized long getCurrentSimulationTime() {
 		return clock.getCurrentSimulationTime();
 	}
 	
 	/* New events which lay in the past cause a causality error 
 	 * @see EventScheduler#scheduleEvent(Event)
 	 */
 	public void scheduleEvent(Event e){
 		long tim = e.getTimeStamp();
 		if (tim < clock.getCurrentSimulationTime()) throw new RuntimeException("Causality error: "+this);
 		int pos=0;
 		while (pos < evList.size()){
 			Event n = evList.get(pos); 
 			if (n.getTimeStamp() > tim) break;
 			pos++;
 		}
 		evList.add(pos,e);
 	}
 	
 	/**
 	 * Advances the time to the time of the oldest event in the event queue
 	 * and processes the event
 	 */
 	public void processNextEvent(){
 		Event e = evList.remove(0);
 		
 		if (e.getTimeStamp() > this.clock.getCurrentSimulationTime()) {		
 			clock.sleepUntil(e.getTimeStamp());
 		}
 		
 		// log the event
 		AirportLogger.getLogger().info("Process event: " + e.toString());
 		//	log the state of the object which is the target of this 
 		AirportLogger.getLogger().info("Event info: " + e.getEventHandler().toString());
 		
 		// process 
 		e.getEventHandler().processEvent(e,this);
 
 	}
 
 	/**
 	 * This is the main simulation loop
 	 *  
 	 * The simulation carries on even if there are no
 	 * events around anymore, since it might be possible
 	 * that some time an event might be sent to this 
 	 * LP. Until then the simulation is just synchronised
 	 * with the other LPs.
 	 */
 	public void runSimulation(){
 		this.clock.setRtStartTime(System.currentTimeMillis());
 		
 		SimWorld.getInstance().setSendThread(new SendThread(this));
 		SimWorld.getInstance().setRecvThread(new RecvThread());
 		SimWorld.getInstance().getRecvThread().start();
 		SimWorld.getInstance().getSendThread().start();
 		
 		LookaheadQueue lq = SimWorld.getInstance().getLookaheadQueue();
 		long safeLookahead = 0l;
 		
 		while(lq.hasEmptyQueues()){
 			// wait with the simulation until the queues are filled
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 			}		
 		}
 		
 		safeLookahead = lq.getSmallestLookahead();
 		AirportLogger.getLogger().debug("Initial safe lookahead " + safeLookahead);
 		
 		while(true){
 			while(evList.size() > 0 && evList.get(0).getTimeStamp() <= safeLookahead){
 				AirportLogger.getLogger().debug("Process events until " + safeLookahead);
 				processNextEvent();
 				AirportLogger.getLogger().debug("CST " + this.clock.getCurrentSimulationTime());
 			}
 			
 			this.clock.sleepUntil(safeLookahead);
 			lq.removeLookeahead(safeLookahead);
 			AirportLogger.getLogger().debug("CST " + this.clock.getCurrentSimulationTime());
 			
 			while (lq.hasEmptyQueues()){
 				AirportLogger.getLogger().debug("Queue still empty");
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 				}
 			}
 			
 			safeLookahead = lq.getSmallestLookahead();
 			AirportLogger.getLogger().debug("Setting new safe lookahead to " + safeLookahead);
 			
 			// make one processor really slow, check synchronisation
 //			if(this.getMpiRank() == 1){
 //				try {
 //					Thread.sleep(2000);
 //				} catch (InterruptedException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 //			}
 			
 		}
 	}
 	
 	public void init(int mpiRank) {
 		this.mpiRank = mpiRank;
 		
 		SimWorld.getInstance().setSimulator(this);
 		this.initWorld(mpiRank);
 		SimWorld.getInstance().setLookaheadQueue(new LookaheadQueue());
 		
 		// start simulation frontend
 		wg = new WorldGui();
 		new Thread(wg).start();
 		try {
 			synchronized (wg) {
 				wg.wait();	
 			}
 			// wait until the thread is ready
 		} catch (InterruptedException e) {
 		}
 	}
 	
 	public int getMpiRank(){
 		return mpiRank;
 	}
 	
 	private Airport homeAirport; 
 	
 	public Airport getHomeAirport(){
 		return homeAirport;
 	}
 	
 	public void initWorld(int mpiRank){
 		int n = 30; 
 		// Random Generator:
 		Random rand = new Random(1234);
 
 		// create airports
 		String [] airportNames = {"ZUERICH","GENF","BASEL"};
 		Airport ap = new Airport("ZUERICH", 0, 684000, 256000, 683000, 259000);
 		world.addAirport(ap);
 		
 		if(mpiRank == 0){
 			this.homeAirport = ap;
 		}
 		
 		ap = new Airport("GENF", 1, 497000, 120000, 499000, 122000); 
 		world.addAirport(ap);
 		
 		if(mpiRank == 1){
 			this.homeAirport = ap;
 		}
 		
 		ap = new Airport("BASEL", 2, 599000, 287000, 601000, 288000); 
 		world.addAirport(ap);
 		
 		if(mpiRank == 2){
 			this.homeAirport = ap;
 		}
 		
		// create 30 aircrafts and choose an arbitrary airport for this airport
 		for (int i=0;i<n;i++){
			// Random Airport:
 			ap = world.getAirport(airportNames[mpiRank]);
 			Aircraft ac = new Aircraft("X" + (mpiRank + 1) * 1000 +i, ap);
			world.addAircraft(ac);			
 		}
 		// create FlightPlans for all aircrafts
 		for (int i=0;i<n;i++){
 			// Random Airport:
 			Aircraft ac = world.getAircraft("X" + (mpiRank + 1) * 1000+i);
 			// first Flight:
 			ap = world.getAirport(airportNames[rand.nextInt(3)]);
 			while (ap == ac.getCurrentAirPort()){
 				ap = world.getAirport(airportNames[rand.nextInt(3)]);
 			}			
 			Flight f = new Flight(rand.nextInt(10),ap);
 			ac.getFlightPlan().addFlight(f);
 			// Return flight
 			f = new Flight(rand.nextInt(1000), ac.getCurrentAirPort());
 		}
 		
 		// System.out.println(world);
 		// schedule initial events
 		for (int i=0;i<n;i++){
 			Aircraft ac = world.getAircraft("X" + (mpiRank + 1) * 1000+i);
 			Flight f = ac.getFlightPlan().removeNextFlight();
 			ac.setDestination(f.getDestination());
 			ap = ac.getCurrentAirPort();
 			Event e = new Event(Event.READY_FOR_DEPARTURE, ap, f.getTimeGap(), ap, ac);
 			scheduleEvent(e);
 		}
 	}
 	
 	public WorldClock getClock() {
 		return clock;
 	}
 
 	static public void main(String [] argv){
 		
 		// initialize MPI
 		MPI.Init(argv);
 		
 		// initialize simulation
 		SimWorld.getInstance().setArgs(argv);
 		Simulator sim = new Simulator(SimWorld.getInstance());
 		sim.init(MPI.COMM_WORLD.Rank());
 		
 		// main simulation loop
 		sim.runSimulation(); 
 		
 		// exit MPI
 		MPI.Finalize();
 	}
 	
 }
