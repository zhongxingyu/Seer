 package turboprt;
 
 import gui.LoadCoordinates;
 import java.util.ArrayList;
 import wiiusej.wiiusejevents.physicalevents.ExpansionEvent;
 import wiiusej.wiiusejevents.physicalevents.IREvent;
 import wiiusej.wiiusejevents.physicalevents.MotionSensingEvent;
 import wiiusej.wiiusejevents.physicalevents.WiimoteButtonsEvent;
 import wiiusej.wiiusejevents.utils.WiimoteListener;
 import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.DisconnectionEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.NunchukInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.NunchukRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.StatusEvent;
 
 /**
  *
  * @author marcel
  */
 public class Track extends Thread implements WiimoteListener {
     public static int[][] locations = new int[Intersection.numberOfIntersections][Intersection.numberOfIntersections];
     public static int[][][] connections = {
         /*  Intersection 0  */ {{1,10},{4,5},{5,10}},//Intersect0 connection {{from 0 to 1 cost 5},{from 0 to 4 cost 10}},
         /*  Intersection 1  */ {{0,10},{2,12},{6,10}}, //Intersect1 connections  {{from  1 to 0 cost 1}, {from 1 to 2 cost 1}}
         /*  Intersection 2  */ {{1,12},{3,8},{7,12}},
         /*  Intersection 3  */ {{2,8},{8,14}},
        /*  Intersection 4  */ {{0,5},{5,5}},
        /*  Intersection 5  */ {{0,10},{4,5},{6,10},{9,10}},
         /*  Intersection 6  */ {{1,10},{5,10},{7,10}},
         /*  Intersection 7  */ {{2,12},{6,10},{8,12},{10,12}},
         /*  Intersection 8  */ {{3,14},{7,12},{13,16}},
         /*  Intersection 9  */ {{5,10},{10,16}},
         /*  Intersection 10 */ {{7,12},{9,16},{11,10},{12,10}},
         /*  Intersection 11 */ {{10,10},{13,8}},
         /*  Intersection 12 */ {{10,10},{13,16}},
        /*  Intersection 13 */ {{8,16},{11,8},{12,16}},
     };
 	
 	static ArrayList<Intersection> intersections;
 	static Intersection currentIntersection;
 	static LoadCoordinates loader;
 
     public static void init() {
 
         intersections = new ArrayList<Intersection>();
 
         //Loop to create all intersections
         for (int i = 0; i < Intersection.numberOfIntersections; i++) {
             intersections.add(i, new Intersection(i, new Location(locations[i][0], locations[i][1])));
         }
 
         
         //Loop for creating connections between intersections...
         for (int i = 0; i < Intersection.numberOfIntersections; i++) { //For every intersection,
             for (int j = 0; j < connections[i].length; j++) { //For evry connection in connections[][][]
                 //Add the connection and cost to the intersection object. 
                 intersections.get(i).addConnection(intersections.get(connections[i][j][0]), connections[i][j][1]);
             }
 
         }
     }
 	
 	public static void calibrate()
 	{
 		if(loader == null)
 		{
 			loader = new LoadCoordinates();
 			loader.setVisible(true);
 		}
 		
 		try
 		{
 			currentIntersection = getNextUnknownIntersection();
 			loader.displayIntersection(currentIntersection);
 		}
 		catch(Exception e)
 		{
 			loader.done();
 			loader = null;
 			//e.printStackTrace();
 		}
 	}
 	
 	public static Intersection getNextUnknownIntersection() throws Exception
 	{
 		for(Intersection intersection : intersections)
 		{
 			if(!intersection.isCalibrated())
 				return intersection;
 		}
 		throw new Exception("Unknown intersection");
 	}
 
 	@Override
 	public void onButtonsEvent(WiimoteButtonsEvent e) {
 	}
 
 	@Override
 	public void onIrEvent(IREvent e) {
 		//System.out.println("Got IR event");
 		
 		currentIntersection.setLocation(new Location(e.getX(), e.getY()));
 		
 //		for(Intersection intersection : intersections)
 //		{
 //			System.out.println(intersection.getID()+": "+intersection.getLocation());
 //		}
 		
 		Track.calibrate();
 	}
 
 	@Override
 	public void onMotionSensingEvent(MotionSensingEvent e) {
 	}
 
 	@Override
 	public void onExpansionEvent(ExpansionEvent e) {
 	}
 
 	@Override
 	public void onStatusEvent(StatusEvent e) {
 	}
 
 	@Override
 	public void onDisconnectionEvent(DisconnectionEvent e) {
 	}
 
 	@Override
 	public void onNunchukInsertedEvent(NunchukInsertedEvent e) {
 	}
 
 	@Override
 	public void onNunchukRemovedEvent(NunchukRemovedEvent e) {
 	}
 
 	@Override
 	public void onGuitarHeroInsertedEvent(GuitarHeroInsertedEvent e) {
 	}
 
 	@Override
 	public void onGuitarHeroRemovedEvent(GuitarHeroRemovedEvent e) {
 	}
 
 	@Override
 	public void onClassicControllerInsertedEvent(ClassicControllerInsertedEvent e) {
 	}
 
 	@Override
 	public void onClassicControllerRemovedEvent(ClassicControllerRemovedEvent e) {
 	}
 }
