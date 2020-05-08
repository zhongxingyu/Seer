 package nz.ac.victoria.ecs.kpsmart;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Carrier;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Route;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.TransportMeans;
 import nz.ac.victoria.ecs.kpsmart.state.manipulation.StateManipulator;
 
 import com.google.inject.Inject;
 
 final class Data {
 	@Inject 
 	private StateManipulator sm;
 
 	@InjectOnCall
 	public void createData() {
 		/*
 		 * 
 		 * Carriers
 		 * 
 		 */
 		Carrier c1 = new Carrier();
 		{
 			c1.setName("a");
 		}
 		sm.save(c1);
 		
 		/*
 		 * 
 		 * Locations
 		 * 
 		 */
 		Location l1 = createLocation(true, 42.988576, 12.414551, "Rome");
 		sm.save(l1);
 		Location l2 = createLocation(false, -41.013066, 174.605713, "Wellington");
 		sm.save(l2);
 		Location l3 = createLocation(false, -36.694851, 175.155029, "Auckland");
 		sm.save(l3);
 		Location l4 = createLocation(true, 42.654162, 23.365173, "Sofia");
 		sm.save(l4);
 		Location l5 = createLocation(true, 52.749594, 5.998535, "Amsterdam");
 		sm.save(l5);
 		Location l6 = createLocation(false, -1, -1, "Christchurch");
 		sm.save(l6);
 		/*
 		 * 
 		 * Routes
 		 * 
 		 */
 		Route r1 = Route.newRoute();
 		try {
 			r1.setCarrier(sm.getCarrier(1));
 			r1.setCarrierVolumeUnitCost((float) 10.9);
 			r1.setCarrierWeightUnitCost((float)10.9);
 			r1.setDisabled(false);
 			r1.setDuration(10);
 			r1.setEndPoint(sm.getLocationForName("Rome"));
 			r1.setFrequency(10);
 			r1.setStartPoint(sm.getLocationForName("Wellington"));
 			r1.setStartingTime(new SimpleDateFormat("y-m-d h:m:s").parse("2012-03-27 11:18:18"));
 			r1.setTransportMeans(TransportMeans.Air);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 		sm.saveRoute(r1);
 		
 		Route r2 = Route.newRoute();
 		try {
 			r2.setCarrier(sm.getCarrier(1));
 			r2.setCarrierVolumeUnitCost((float) 8.0);
 			r2.setCarrierWeightUnitCost((float)8.9);
 			r2.setDisabled(false);
 			r2.setDuration(1);
 			r2.setEndPoint(sm.getLocationForName("Auckland"));
 			r2.setFrequency(6);
 			r2.setStartPoint(sm.getLocationForName("Wellington"));
 			r2.setStartingTime(new SimpleDateFormat("y-m-d h:m:s").parse("2012-03-27 11:45:18"));
 			r2.setTransportMeans(TransportMeans.Air);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 		sm.saveRoute(r2);
 		
		Route r3 = Route.newRoute();
 		try {
 			r3.setCarrier(sm.getCarrier(1));
 			r3.setCarrierVolumeUnitCost((float) 8.0);
 			r3.setCarrierWeightUnitCost((float)8.9);
 			r3.setDisabled(false);
 			r3.setDuration(1);
 			r3.setEndPoint(sm.getLocationForName("Christchurch"));
 			r3.setFrequency(6);
 			r3.setStartPoint(sm.getLocationForName("Wellington"));
 			r3.setStartingTime(new SimpleDateFormat("y-m-d h:m:s").parse("2012-03-27 11:45:18"));
 			r3.setTransportMeans(TransportMeans.Air);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 		sm.save(r3);
 		
		Route r4 = Route.newRoute();
 		try {
 			r4.setCarrier(sm.getCarrier(1));
 			r4.setCarrierVolumeUnitCost((float) 8.0);
 			r4.setCarrierWeightUnitCost((float)8.9);
 			r4.setDisabled(false);
 			r4.setDuration(1);
 			r4.setEndPoint(sm.getLocationForName("Rome"));
 			r4.setFrequency(6);
 			r4.setStartPoint(sm.getLocationForName("Auckland"));
 			r4.setStartingTime(new SimpleDateFormat("y-m-d h:m:s").parse("2012-03-27 11:45:18"));
 			r4.setTransportMeans(TransportMeans.Air);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 		sm.save(r4);
 	}
 
 	public Location createLocation(boolean international, double d, double e, String name) {
 		Location l = new Location();
 		l.setInternational(international);
 		l.setLatitude((float) d);
 		l.setLongitude((float) e);
 		l.setName(name);
 		return l;
 	}
 }
