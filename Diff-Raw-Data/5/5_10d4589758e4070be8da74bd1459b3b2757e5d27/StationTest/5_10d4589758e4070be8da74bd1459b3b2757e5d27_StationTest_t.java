 package org.jpc.examples.metro;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import org.jpc.examples.metro.model.Line;
 import org.jpc.examples.metro.model.Station;
 import org.junit.Test;
 
 public class StationTest extends MetroExampleTest {
 
 
 	@Test
 	public void testAllConnections() {
 		Station station1 = station("bond_street");
 		List<Station> connectedStations = station1.connected();
 		assertEquals(2, connectedStations.size());
 		
 //		System.out.println("Stations connected to " + station1 + ": " + connectedStations.size());
 //		for(IStation connectedStation: connectedStations) {
 //			System.out.println("- " + connectedStation);
 //		}
 		
 		Station station2 = station("inexisting_station");
 		assertEquals(0, station2.connected().size());
 	}
 	
 	@Test
 	public void testOneConnection() {
 		Station station = station("bond_street");
 		Line line1 = line("central");
 		Line line2 = line("northern");
 		Station connectedStation = station.connected(line1);
 		assertNotNull(connectedStation);
		
		assertEquals(connectedStation.getName(), "oxford_circus");
 		//System.out.println("The station " + station + " is connected with " + connectedStation + " by means of the line " + line1);
 		try {
 			connectedStation = station.connected(line2);  //no connected with any station by means of line2
 			fail();
 		} catch(NoSuchElementException e) {}
 	}
 	
 	@Test
 	public void testNumberConnections() {
 		Station station = station("bond_street");
 		assertEquals(2, station.numberConnections());
 		//System.out.println("Number of connections of " + station + ": " + station.numberConnections());
 	}
 	
 	@Test
 	public void testIsConnected() {
 		Station station1 = station("bond_street");
 		Station station2 = station("oxford_circus");
 		Station station3 = station("charing_cross");
 		assertTrue(station1.connected(station2));
 		assertFalse(station1.connected(station3));
 	}
 	
 	
 	
 	@Test
 	public void testAllNearbyStations() {
 		Station station = station("bond_street");
 		List<Station> nearbyStations = station.nearby();
 		assertEquals(4, nearbyStations.size());
 //		System.out.println("Stations nearby to " + station + ": " + nearbyStations.size());
 //		for(IStation nearbyStation: nearbyStations) {
 //			System.out.println("- " + nearbyStation);
 //		}
 		
 	}
 	
 	@Test
 	public void testNumberNearbyStations() {
 		Station station = station("bond_street");
 		assertEquals(4, station.numberNearbyStations());
 		//System.out.println("Number of nearby stations of " + station + ": " + station.numberNearbyStations());
 	}
 	
 	@Test
 	public void testIsNearby() {
 		Station station1 = station("bond_street");
 		Station station2 = station("oxford_circus");
 		Station station3 = station("charing_cross");
 		Station station4 = station("piccadilly_circus");
 		assertTrue(station1.nearby(station2));
 		assertTrue(station1.nearby(station3));
 		assertFalse(station1.nearby(station4));
 	}
 	
 	
 	
 	@Test
 	public void testIntermediateStations() {
 		Station station1 = station("bond_street");
 		Station station2 = station("oxford_circus");
 		Station station3 = station("piccadilly_circus");
 		Station station4 = station("inexisting_station");
 		
 		List<Station> intermediateStations = station1.intermediateStations(station2);
 		assertEquals(0, intermediateStations.size());
 		
 		intermediateStations = station1.intermediateStations(station3);
 		assertEquals(1, intermediateStations.size());
 		
 //		System.out.println("Intermediate stations from " + station1 + " to " + station3);
 //		for(IStation intermediateStation: intermediateStations) {
 //			System.out.println("- " + intermediateStation);
 //		}
 		try {
 			station1.intermediateStations(station4);
 			fail();
 		} catch(NoSuchElementException e){}
 	}
 	
 	@Test
 	public void testNumberReachableStations() {
 		Station station = station("bond_street");
 		assertEquals(22, station.numberReachableStations());
 		//System.out.println("Number of reachable stations from " + station + ": " + station.numberReachableStations());
 	}
 	
 	@Test
 	public void testIsReachable() {
 		Station station1 = station("bond_street");
 		Station station2 = station("oxford_circus");
 		Station station3 = station("charing_cross");
 		Station station4 = station("piccadilly_circus");
 		Station station5 = station("inexisting_station");
 		assertTrue(station1.reachable(station2));
 		assertTrue(station1.reachable(station3));
 		assertTrue(station1.reachable(station4));
 		assertFalse(station1.reachable(station5));
 	}
 	
 }
